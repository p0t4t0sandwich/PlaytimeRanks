package dev.neuralnexus.playtimeranks.common.config;

import dev.neuralnexus.playtimeranks.common.config.sections.DatabaseConfig;
import dev.neuralnexus.playtimeranks.common.config.sections.RankConfig;
import dev.neuralnexus.playtimeranks.common.config.versions.PlaytimeRanksConfig_V1;
import dev.neuralnexus.taterapi.logger.Logger;
import dev.neuralnexus.taterapi.util.ConfigUtil;

import io.leangen.geantyref.TypeToken;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** A class for loading PlaytimeRanks configuration. */
public class PlaytimeRanksConfigLoader {
    private static final Logger logger = Logger.create("PlaytimeRanksConfigLoader");
    private static final Path configPath =
            Paths.get(
                    "plugins"
                            + File.separator
                            + "playtimeranks"
                            + File.separator
                            + "playtimeranks"
                            + ".conf");
    private static final String defaultConfigPath = "source." + "playtimeranks" + ".conf";
    private static final TypeToken<Integer> versionType = new TypeToken<>() {};
    private static final TypeToken<Integer> prefixWeight = new TypeToken<>() {};
    private static final TypeToken<List<RankConfig>> ranksType = new TypeToken<>() {};
    private static final TypeToken<DatabaseConfig> dataType = new TypeToken<>() {};
    private static PlaytimeRanksConfig config;

    /** Load the configuration from the file. */
    public static void load() {
        ConfigUtil.copyDefaults(
                PlaytimeRanksConfigLoader.class, configPath, defaultConfigPath, logger);

        final HoconConfigurationLoader loader =
                HoconConfigurationLoader.builder().path(configPath).build();
        CommentedConfigurationNode root = ConfigUtil.getRoot(loader, logger);
        if (root == null) {
            return;
        }

        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt(1);
        int prefixWeight = root.node("prefixWeight").getInt(413);
        List<RankConfig> ranks = ConfigUtil.get(root, ranksType, "server", logger);
        DatabaseConfig data = ConfigUtil.get(root, dataType, "data", logger);

        switch (version) {
            case 1:
                config = new PlaytimeRanksConfig_V1(version, prefixWeight, ranks, data);
                break;
            default:
                System.err.println("Unknown configuration version: " + version);
        }
    }

    /** Unload the configuration. */
    public static void unload() {
        config = null;
    }

    /** Save the configuration to the file. */
    public static void save() {
        if (config == null) {
            return;
        }
        final HoconConfigurationLoader loader =
                HoconConfigurationLoader.builder().path(configPath).build();
        CommentedConfigurationNode root = ConfigUtil.getRoot(loader, logger);
        if (root == null) {
            return;
        }

        ConfigUtil.set(root, versionType, "version", config.version(), logger);
        ConfigUtil.set(root, prefixWeight, "prefixWeight", config.prefixWeight(), logger);
        ConfigUtil.set(root, ranksType, "ranks", config.ranks(), logger);
        ConfigUtil.set(root, dataType, "data", config.data(), logger);

        try {
            loader.save(root);
        } catch (ConfigurateException e) {
            logger.error("An error occurred while saving this configuration: ", e);
        }
    }

    /**
     * Get the loaded configuration.
     *
     * @return The loaded configuration.
     */
    public static PlaytimeRanksConfig config() {
        if (config == null) {
            load();
        }
        return config;
    }
}
