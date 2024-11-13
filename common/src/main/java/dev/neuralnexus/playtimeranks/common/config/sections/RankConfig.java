package dev.neuralnexus.playtimeranks.common.config.sections;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/** A class for parsing rank configurations. */
@ConfigSerializable
public class RankConfig {
    @Setting private String name;
    @Setting private int time;
    @Setting private String prefix;

    /**
     * Get the rank's name.
     *
     * @return The rank's name.
     */
    public String name() {
        return name;
    }

    /**
     * Get the rank's playtime.
     *
     * @return The rank's playtime.
     */
    public int time() {
        return time;
    }

    /**
     * Get the rank's prefix.
     *
     * @return The rank's prefix.
     */
    public String prefix() {
        return prefix;
    }
}
