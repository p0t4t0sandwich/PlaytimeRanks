package dev.neuralnexus.playtimeranks.common.config;

import dev.neuralnexus.playtimeranks.common.config.sections.DatabaseConfig;
import dev.neuralnexus.playtimeranks.common.config.sections.RankConfig;

import java.util.List;

/** A class for PlaytimeRanks configuration. */
public interface PlaytimeRanksConfig {
    /**
     * Get the version of the configuration.
     *
     * @return The version of the configuration.
     */
    int version();

    /**
     * Get the prefix weight.
     *
     * @return The prefix weight.
     */
    int prefixWeight();

    /**
     * Get a list of ranks.
     *
     * @return A list of ranks.
     */
    List<RankConfig> ranks();

    /**
     * Get the database configuration.
     *
     * @return The database configuration.
     */
    DatabaseConfig data();
}
