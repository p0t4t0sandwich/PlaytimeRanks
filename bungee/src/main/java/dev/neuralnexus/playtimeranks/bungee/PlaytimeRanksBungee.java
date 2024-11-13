package dev.neuralnexus.playtimeranks.bungee;

import dev.neuralnexus.playtimeranks.common.PlaytimeRanks;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PlaytimeRanksBungee extends Plugin implements Listener {
    private static final PlaytimeRanks playtimeRanks = new PlaytimeRanks();

    @Override
    public void onEnable() {
        playtimeRanks.init();
        getProxy().getPluginManager().registerListener(this, this);
        getLogger().info("PlaytimeRanks has been enabled.");
    }

    @Override
    public void onDisable() {
        playtimeRanks.close();
        getLogger().info("PlaytimeRanks has been disabled.");
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        this.getProxy()
                .getScheduler()
                .schedule(
                        this,
                        () -> {
                            UUID playerUuid = event.getPlayer().getUniqueId();

                            // Get playtime from database
                            int playtime = playtimeRanks.getPlaytime(playerUuid);

                            // Update player's rank
                            String rank = playtimeRanks.updateRank(playerUuid, playtime);

                            // Send new rank to player
                            if (rank != null) {
                                event.getPlayer()
                                        .sendMessage(
                                                new TextComponent(
                                                        ChatColor.translateAlternateColorCodes(
                                                                '&',
                                                                "&7[&6PlaytimeRanks&7] &fYour new rank is &6"
                                                                        + rank
                                                                        + "&f!")));
                            }
                        },
                        0,
                        TimeUnit.SECONDS);
    }
}
