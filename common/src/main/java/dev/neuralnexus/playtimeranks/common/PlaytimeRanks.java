package dev.neuralnexus.playtimeranks.common;

import dev.neuralnexus.playtimeranks.common.config.PlaytimeRanksConfig;
import dev.neuralnexus.playtimeranks.common.config.PlaytimeRanksConfigLoader;
import dev.neuralnexus.playtimeranks.common.config.sections.RankConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlaytimeRanks {
    private LuckPermsHook luckPerms;
    private DataStore dataStore;
    private PlaytimeRanksConfig config;

    public void init() {
        this.luckPerms = LuckPermsHook.get();
        this.config = PlaytimeRanksConfigLoader.config();
        this.dataStore = new DataStore(this.config.data());
    }

    public void close() {
        this.dataStore.close();
        PlaytimeRanksConfigLoader.unload();
    }

    /**
     * Get playtime for a player
     *
     * @param playerUuid The UUID of the player
     * @return The playtime of the player
     */
    public int getPlaytime(UUID playerUuid) {
        String player_uuid = playerUuid.toString();
        Connection con = null;
        int playtime = 0;
        try {
            con = dataStore.getConnection();

            // Get playtime from database
            String SQL_QUERY =
                    "SELECT * FROM playtime WHERE `player_id`=(SELECT player_id FROM `player_data` WHERE `player_uuid`='"
                            + player_uuid
                            + "')";
            //            String SQL_QUERY = "SELECT * FROM playtime WHERE `player_id`=(SELECT
            // player_id FROM `player_data` WHERE `player_uuid`=?)";
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
                    if (!Objects.equals(column, "playtime_id")
                            && !Objects.equals(column, "player_id")
                            && column != null) {
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

    // Update player's rank
    public String updateRank(UUID playerUuid, int playtime) {
        // Get rank from config
        String rank = null;
        int time;

        List<RankConfig> rankConfigs = config.ranks();
        for (RankConfig rankConfig : rankConfigs) {
            String previousRank = rank;

            rank = rankConfig.name();
            time = rankConfig.time();

            if (previousRank == null) {
                previousRank = rank;
            }

            // Check if player has permission
            if (!luckPerms.hasPermission(playerUuid, "group." + rank)) {
                // Check if player has enough playtime
                if (playtime >= time) {
                    // Add permission to player
                    luckPerms.addPermission(playerUuid, "group." + rank);
                    // Remove previous rank
                    luckPerms.removePermission(playerUuid, "group." + previousRank);
                    return rank;
                }
            }
        }
        return null;
    }
}
