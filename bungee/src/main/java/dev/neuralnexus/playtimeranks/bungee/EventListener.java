package dev.neuralnexus.playtimeranks.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class EventListener implements Listener {
    private final PlaytimeRanks plugin = PlaytimeRanks.getInstance();

    // Login event handler
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ScheduledTask scheduledTask = plugin.getProxy().getScheduler().schedule(plugin, () -> {
            // Player object
            ProxiedPlayer player = event.getPlayer();

            // Get playtime from database
            int playtime = plugin.getPlaytime(player);

            // Update player's rank
            String rank = plugin.updateRank(player, playtime);

            // Send new rank to player
            if (rank != null) {
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7[&6PlaytimeRanks&7] &fYour new rank is &6" + rank + "&f!")));
            }
        }, 0, TimeUnit.SECONDS);
    }
}
