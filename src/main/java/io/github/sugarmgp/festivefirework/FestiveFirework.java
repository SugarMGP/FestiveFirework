package io.github.sugarmgp.festivefirework;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class FestiveFirework extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("festivefirework").setExecutor(new MainCommand());
    }

    @Override
    public void onDisable() {
        saveConfig();
        Bukkit.getScheduler().cancelTasks(this);
    }
}
