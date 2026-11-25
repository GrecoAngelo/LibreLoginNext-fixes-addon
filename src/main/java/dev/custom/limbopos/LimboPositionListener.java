package dev.custom.limbopos;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core fix.
 *
 * LibreLogin Next (like most limbo-based auth plugins) always places an
 * unauthenticated player at the limbo world's spawn point on join - it does
 * not remember where they were standing in limbo before they disconnected.
 *
 * This listener:
 *  1. On quit, if the player is currently inside a configured limbo world,
 *     remembers their exact location.
 *  2. On join, if they land back in a limbo world and we have a saved
 *     location for it, teleports them there a couple of ticks later
 *     (after LibreLogin Next's own join-teleport has already happened).
 *  3. On PlayerChangedWorldEvent, if they just left a limbo world (i.e. they
 *     authenticated and got sent to the real lobby/server), the saved
 *     location is discarded since it's no longer relevant.
 *
 * This does not call any LibreLogin Next API directly, so it keeps working
 * even across LibreLogin Next updates as long as the limbo world names in
 * config.yml still match LibreLogin Next's own "limbo" list.
 */
public class LimboPositionListener implements Listener {

    private final LimboPositionFix plugin;
    private final Map<UUID, Location> savedLimboLocations = new ConcurrentHashMap<>();
    private List<String> limboWorlds;
    private long restoreDelayTicks;

    public LimboPositionListener(LimboPositionFix plugin) {
        this.plugin = plugin;
        reloadSettings();
    }

    public void reloadSettings() {
        this.limboWorlds = plugin.getLimboWorlds();
        this.restoreDelayTicks = plugin.getRestoreDelayTicks();
    }

    public void clearAll() {
        savedLimboLocations.clear();
    }

    private boolean isLimboWorld(String worldName) {
        return limboWorlds.stream().anyMatch(w -> w.equalsIgnoreCase(worldName));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        if (isLimboWorld(worldName)) {
            savedLimboLocations.put(player.getUniqueId(), player.getLocation().clone());
            plugin.getLogger().fine("Saved limbo location for " + player.getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!savedLimboLocations.containsKey(uuid)) {
            return;
        }

        // Wait a couple of ticks so this runs AFTER LibreLogin Next's own
        // teleport-to-limbo-spawn on join, so we overwrite it rather than
        // get overwritten by it.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            Location saved = savedLimboLocations.get(uuid);
            if (saved == null) {
                return;
            }

            String currentWorld = player.getWorld().getName();

            // Only restore if the player is still sitting in a limbo world
            // (i.e. still unauthenticated). If LibreLogin Next already sent
            // them to the real lobby (fast autologin, premium, etc.) do
            // nothing and let onChangedWorld below clean up the entry.
            if (isLimboWorld(currentWorld) && saved.getWorld() != null
                    && saved.getWorld().getName().equalsIgnoreCase(currentWorld)) {
                player.teleport(saved);
                plugin.getLogger().fine("Restored limbo location for " + player.getName());
            }
        }, restoreDelayTicks);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String fromWorld = event.getFrom().getName();
        String toWorld = player.getWorld().getName();

        // Left a limbo world for a non-limbo world -> they authenticated.
        // The saved position is no longer needed.
        if (isLimboWorld(fromWorld) && !isLimboWorld(toWorld)) {
            savedLimboLocations.remove(player.getUniqueId());
            plugin.getLogger().fine("Cleared limbo location for " + player.getName() + " (authenticated)");
        }
    }
}
