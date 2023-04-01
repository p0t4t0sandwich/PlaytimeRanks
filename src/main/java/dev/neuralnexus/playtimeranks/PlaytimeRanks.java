package dev.neuralnexus.playtimeranks;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PlaytimeRanks extends Plugin implements Listener {
    public static YamlDocument config;
    private static HikariConfig dbconfig = new HikariConfig();
    private static HikariDataSource ds;
    private LuckPerms luckPerms;
    @Override
    public void onEnable() {
        // Config
        try {
            config = YamlDocument.create(new File(getDataFolder(), "config.yml"), getResourceAsStream("config.yml"));
            config.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Database
        dbconfig.setJdbcUrl("jdbc:mysql://" + config.getString("data.address") + "/" + config.getString("data.database"));
        dbconfig.setUsername(config.getString("data.username"));
        dbconfig.setPassword(config.getString("data.password"));

        dbconfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dbconfig.addDataSourceProperty("cachePrepStmts", "true");
        dbconfig.addDataSourceProperty("prepStmtCacheSize", "250");
        dbconfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(dbconfig);

        // Get LuckPerms API
        this.luckPerms = LuckPermsProvider.get();

        // Register event listeners
        getProxy().getPluginManager().registerListener(this, this);

        // Plugin enable message
        getLogger().info("PlaytimeRanks has been enabled.");
    }

    @Override
    public void onDisable() {
        ds.close();
        // Plugin disable message
        getLogger().info("PlaytimeRanks has been disabled.");
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    // Get Playtime from database
    public int getPlaytime(String player_uuid) {
        Connection con;
        int playtime = 0;
        try {
            con = getConnection();

            // Get playtime from database
            String SQL_QUERY = "SELECT * FROM playtime WHERE `player_id`=(SELECT player_id FROM `player_data` WHERE `player_uuid`='" + player_uuid + "')";
//            String SQL_QUERY = "SELECT * FROM playtime WHERE `player_id`=(SELECT player_id FROM `player_data` WHERE `player_uuid`=?)";
            PreparedStatement pst = con.prepareStatement(SQL_QUERY);
            // NOTE: Prepared statements did not work, but the query is safe
//            pst.setString(1, player_uuid);
            ResultSet rs = pst.executeQuery(SQL_QUERY);
            if (rs.next()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int count = metaData.getColumnCount();

                // Add all playtime columns together -- except playtime_id and player_id
                for (int i = 1; i <= count; i++) {
                    String column = metaData.getColumnLabel(i);
                    if (!Objects.equals(column, "playtime_id") && !Objects.equals(column, "player_id") && column != null) {
                        playtime += rs.getInt(column);
                    }
                }
            }
            // Close connections
            rs.close();
            con.close();
            return playtime;

        } catch (SQLException e) {
            e.printStackTrace();
            return playtime;
        }
    }

    // Check if player has a specific group
    public static boolean isPlayerInGroup(ProxiedPlayer player, String group) {
        return player.hasPermission("group." + group);
    }

    // Add permission to player
    public void addPermission(UUID userUuid, String permission) {
        // Load, modify, then save
        luckPerms.getUserManager().modifyUser(userUuid, user -> {
            // Add the permission
            user.data().add(Node.builder(permission).build());
        });
    }

    // Remove permission from player
    public void removePermission(UUID userUuid, String permission) {
        // Load, modify, then save
        luckPerms.getUserManager().modifyUser(userUuid, user -> {
            // Remove the permission
            user.data().remove(Node.builder(permission).build());
        });
    }

    // Update player's rank
    public String updateRank(ProxiedPlayer player, int playtime) {
        // Get rank from config
        String rank = null;
        int time = 0;

        List<Map<?,?>> maplist = config.getMapList("ranks");
        for (Map<?,?> map : maplist) {
            String previousRank = rank;

            // get Map keys
            for (Object key :  map.keySet()) {
                rank = key.toString();
                time = Integer.parseInt(map.get(key).toString());
            }

            if (previousRank == null) {
                previousRank = rank;
            }

            // Check if player has permission
            if (!isPlayerInGroup(player, rank)) {
                // Check if player has enough playtime
                if (playtime >= time) {
                    // Add permission to player
                    addPermission(player.getUniqueId(), "group." + rank);
                    // Remove previous rank
                    removePermission(player.getUniqueId(), "group." + previousRank);
                    return rank;
                }
            }
        }
        return null;
    }

    // Login event handler
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ScheduledTask scheduledTask = getProxy().getScheduler().schedule(this, () -> {
            // Player object
            ProxiedPlayer player = event.getPlayer();

            // Get playtime from database
            int playtime = getPlaytime(player.getUniqueId().toString());

            // Update player's rank
            String rank = updateRank(player, playtime);

            // Send new rank to player
            if (rank != null) {
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7[&6PlaytimeRanks&7] &fYour new rank is &6" + rank + "&f!")));
            }
        }, 0, TimeUnit.SECONDS);
    }
}
