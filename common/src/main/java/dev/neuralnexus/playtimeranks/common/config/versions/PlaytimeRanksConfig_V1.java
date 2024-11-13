package dev.neuralnexus.playtimeranks.common.config.versions;

import dev.neuralnexus.playtimeranks.common.config.PlaytimeRanksConfig;
import dev.neuralnexus.playtimeranks.common.config.sections.DatabaseConfig;
import dev.neuralnexus.playtimeranks.common.config.sections.RankConfig;

import java.util.List;

/** A class for PlaytimeRanks configuration. */
public record PlaytimeRanksConfig_V1(
        int version, int prefixWeight, List<RankConfig> ranks, DatabaseConfig data)
        implements PlaytimeRanksConfig {}
