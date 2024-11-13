package dev.neuralnexus.playtimeranks.common.config.sections;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/** A class for parsing database configurations. */
@ConfigSerializable
public class DatabaseConfig {
    @Setting private String address;
    @Setting private String database;
    @Setting private String username;
    @Setting private String password;

    /**
     * Get the database address.
     *
     * @return The database address.
     */
    public String address() {
        return address;
    }

    /**
     * Get the database name.
     *
     * @return The database name.
     */
    public String database() {
        return database;
    }

    /**
     * Get the database username.
     *
     * @return The database username.
     */
    public String username() {
        return username;
    }

    /**
     * Get the database password.
     *
     * @return The database password.
     */
    public String password() {
        return password;
    }
}
