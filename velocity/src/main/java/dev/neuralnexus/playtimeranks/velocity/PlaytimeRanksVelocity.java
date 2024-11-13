package dev.neuralnexus.playtimeranks.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;

import dev.neuralnexus.playtimeranks.common.PlaytimeRanks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.slf4j.Logger;

import java.util.UUID;

@Plugin(
        id = "playtimeranks",
        name = "PlaytimeRanks",
        version = "0.1.0-SNAPSHOT",
        authors = "p0t4t0sandwich",
        description = "A plugin that hooks into a MySQL database and updates a player's rank",
        url = "https://github.com/p0t4t0sandwich/PlaytimeRanks#readme",
        dependencies = {@Dependency(id = "luckperms")})
public final class PlaytimeRanksVelocity {
    private static final PlaytimeRanks playtimeRanks = new PlaytimeRanks();
    private final PluginContainer plugin;
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public PlaytimeRanksVelocity(PluginContainer plugin, ProxyServer server, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        playtimeRanks.init();
        logger.info("PlaytimeRanks has been enabled.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        playtimeRanks.close();
        logger.info("PlaytimeRanks has been disabled.");
    }

    @Subscribe
    public void onPostLogin(LoginEvent event) {
        server.getScheduler()
                .buildTask(
                        plugin,
                        () -> {
                            UUID playerUuid = event.getPlayer().getUniqueId();

                            // Get playtime from database
                            int playtime = playtimeRanks.getPlaytime(playerUuid);

                            // Update player's rank
                            String rank = playtimeRanks.updateRank(playerUuid, playtime);

                            // Send new rank to player
                            if (rank != null) {
                                Component message =
                                        Component.text()
                                                .content("[")
                                                .append(
                                                        Component.text(
                                                                "PlaytimeRanks",
                                                                NamedTextColor.GOLD))
                                                .append(Component.text("] ", NamedTextColor.GRAY))
                                                .append(
                                                        Component.text(
                                                                "Your new rank is ",
                                                                NamedTextColor.WHITE))
                                                .append(Component.text(rank, NamedTextColor.GOLD))
                                                .append(Component.text("!", NamedTextColor.WHITE))
                                                .build();
                                event.getPlayer().sendMessage(message);
                            }
                        })
                .schedule();
    }
}
