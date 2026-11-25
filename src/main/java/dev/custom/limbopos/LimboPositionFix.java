package dev.custom.limbopos;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LimboPositionFix extends JavaPlugin {

    private LimboPositionListener listener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.listener = new LimboPositionListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        getLogger().info("LimboPositionFix enabled. Tracking limbo worlds: " + getLimboWorlds());
    }

    @Override
    public void onDisable() {
        if (listener != null) {
            listener.clearAll();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("limboposreload")) {
            reloadConfig();
            listener.reloadSettings();
            sender.sendMessage("§a[LimboPositionFix] Config reloaded. Limbo worlds: " + getLimboWorlds());
            return true;
        }
        return false;
    }

    public List<String> getLimboWorlds() {
        return getConfig().getStringList("limbo-worlds");
    }

    public long getRestoreDelayTicks() {
        return getConfig().getLong("restore-delay-ticks", 2L);
    }
}
