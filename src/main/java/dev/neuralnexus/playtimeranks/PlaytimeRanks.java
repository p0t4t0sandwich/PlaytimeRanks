package dev.neuralnexus.playtimeranks;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PlaytimeRanks extends Plugin {
    public static YamlDocument config;
    private static HikariConfig dbconfig = new HikariConfig();
    private static HikariDataSource ds;
    private LuckPerms luckPerms;

    // Singleton instance
    private static PlaytimeRanks instance;
    public static PlaytimeRanks getInstance() {
        return instance;
    }

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
        getProxy().getPluginManager().registerListener(this, new EventListener());

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
    public int getPlaytime(ProxiedPlayer player) {
        String player_uuid = player.getUniqueId().toString();
        Connection con = null;
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
            try {
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return playtime;
        }
    }

    // Check if player has a specific group
    public static boolean isPlayerInGroup(ProxiedPlayer player, String group) {
        return player.hasPermission("group." + group);
    }

    // Add permission to player
    public void addPermission(ProxiedPlayer player, String permission) {
        // Load, modify, then save
        luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
            // Add the permission
            user.data().add(Node.builder(permission).build());
        });
    }

    // Remove permission from player
    public void removePermission(ProxiedPlayer player, String permission) {
        // Load, modify, then save
        luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
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
                    addPermission(player, "group." + rank);
                    // Remove previous rank
                    removePermission(player, "group." + previousRank);
                    return rank;
                }
            }
        }
        return null;
    }
}
