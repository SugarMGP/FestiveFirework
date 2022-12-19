package io.github.sugarmgp.festivefirework;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimerManager {
    private FileConfiguration timerConfig;
    private File timerFile;
    private List<Map<?, ?>> timers;
    private SimpleDateFormat formatter;

    public TimerManager() {
        formatter = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        timerFile = new File(getPlugin().getDataFolder(), "timer.yml");
        reloadTimerConfig();
        timers = timerConfig.getMapList("timers");
    }

    public List<Map<?, ?>> getTimerList() {
        return timers;
    }

    public boolean addTimer(int type, Date date) {
        int flag = findTimer(date);
        if (flag != -1) {
            return false;
        }
        timers.add(createTimerMap(type, date));
        saveTimerConfig();
        return true;
    }

    public boolean delTimer(int num) {
        try {
            timers.remove(num);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        saveTimerConfig();
        return true;
    }

    public void reloadTimerConfig() {
        timerConfig = YamlConfiguration.loadConfiguration(timerFile);

        final InputStream defConfigStream = getPlugin().getResource("timer.yml");
        if (defConfigStream == null) {
            return;
        }

        timerConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    public void saveTimerConfig() {
        timerConfig.set("timers", timers);
        try {
            timerConfig.save(timerFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + timerFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if (!timerFile.exists()) {
            getPlugin().saveResource("timer.yml", false);
        }
    }

    private Logger getLogger() {
        return getPlugin().getLogger();
    }

    private Plugin getPlugin() {
        return FestiveFirework.getProvidingPlugin(FestiveFirework.class);
    }

    private Map<String, Object> createTimerMap(int type, Date date) {
        HashMap<String, Object> map = new HashMap<>();
        String dateString = formatter.format(date);
        map.put("type", type);
        map.put("date", dateString);
        return map;
    }

    private int findTimer(Date d) {
        int num = -1;
        String dateString = formatter.format(d);
        for (int i = 0; i < timers.size(); i++) {
            Map<?, ?> map = timers.get(i);
            String string = (String) map.get("date");
            if (string.equals(dateString)) {
                num = i;
                break;
            }
        }
        return num;
    }
}
