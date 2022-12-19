package io.github.sugarmgp.festivefirework;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class FestiveFirework extends JavaPlugin {
    private TimerManager timerManager;

    @Override
    public void onEnable() {
        timerManager = new TimerManager();
        saveDefaultConfig();
        getCommand("festivefirework").setExecutor(new MainCommand());
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

    public TimerManager getTimerManager() {
        return timerManager;
    }
}
