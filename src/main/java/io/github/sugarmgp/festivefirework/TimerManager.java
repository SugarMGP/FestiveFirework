package io.github.sugarmgp.festivefirework;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
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
        saveDefaultConfig();
        formatter = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        timerFile = new File(getPlugin().getDataFolder(), "timer.yml");
        reloadTimerConfig();
        timers = timerConfig.getMapList("timers");
        timerWork();
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

    private void timerWork() {
        Plugin plugin = FestiveFirework.getProvidingPlugin(FestiveFirework.class);
        Logger logger = plugin.getLogger();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (int i = 0; i < timers.size(); i++) {
                Map<?, ?> map = timers.get(i);
                int type = (Integer) map.get("type");
                String dateString = (String) map.get("date");
                String nowString = formatter.format(new Date());
                if (dateString.equals(nowString)) {
                    if (type == 1) {
                        if (FireworkUtil.getStatus()) {
                            logger.info("由于烟花燃放已经开始，将忽略 " + i + " 号定时器");
                            break;
                        }
                        FileConfiguration config = plugin.getConfig();
                        List<Map<?, ?>> points = config.getMapList("points");
                        if (points.isEmpty()) {
                            logger.info("由于燃放点列表为空，将忽略 " + i + " 号定时器");
                            break;
                        }
                        int interval = config.getInt("interval");
                        if (interval < 5) {
                            interval = 5;
                        }
                        FireworkUtil.start(interval, points);
                        logger.info("已激活 " + i + " 号定时器，开始燃放烟花");
                    } else {
                        if (!FireworkUtil.getStatus()) {
                            logger.info("由于烟花燃放已经停止，将忽略 " + i + " 号定时器");
                            break;
                        }
                        FireworkUtil.stop();
                        logger.info("已激活 " + i + " 号定时器，停止燃放烟花");
                    }
                    break;
                }
            }
        }, 1, 20);
    }

    private void reloadTimerConfig() {
        timerConfig = YamlConfiguration.loadConfiguration(timerFile);

        final InputStream defConfigStream = getPlugin().getResource("timer.yml");
        if (defConfigStream == null) {
            return;
        }

        timerConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    private void saveTimerConfig() {
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
