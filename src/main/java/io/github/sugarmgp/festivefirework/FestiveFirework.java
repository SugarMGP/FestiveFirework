package io.github.sugarmgp.festivefirework;

import io.github.sugarmgp.festivefirework.command.MainCommand;
import io.github.sugarmgp.festivefirework.util.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class FestiveFirework extends JavaPlugin {
    private TimerManager timerManager;

    @Override
    public void onEnable() {
        timerManager = new TimerManager();
        saveDefaultConfig();
        reloadConfig();
        getCommand("festivefirework").setExecutor(new MainCommand());
        timerManager.timerWork();
    }

    @Override
    public void onDisable() {
        saveConfig();
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        timerManager.saveDefaultConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        FileConfiguration config = getConfig();
        int interval = config.getInt("interval");
        if (!(interval >= 1 && interval <= 72000)) {
            config.set("interval", 15);
            saveConfig();
        }
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }
}
