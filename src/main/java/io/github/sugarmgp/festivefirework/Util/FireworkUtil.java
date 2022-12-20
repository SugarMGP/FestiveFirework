package io.github.sugarmgp.festivefirework.Util;

import io.github.sugarmgp.festivefirework.FestiveFirework;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class FireworkUtil {
    private static boolean isWorking = false;

    private static BukkitTask task = null;

    public static void start(int interval, List<Map<?, ?>> points) {
        isWorking = true;
        work(interval, points);
    }

    public static void stop() {
        isWorking = false;
        task.cancel();
    }

    private static void work(int interval, List<Map<?, ?>> points) {
        Plugin plugin = FestiveFirework.getProvidingPlugin(FestiveFirework.class);
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Map<?, ?> map : points) {
                String worldName = (String) map.get("world");
                World world = Bukkit.getWorld(worldName);
                double x = (Double) map.get("x");
                double y = (Double) map.get("y");
                double z = (Double) map.get("z");
                Location location = new Location(world, x, y, z);

                if (Bukkit.getOnlinePlayers().size() == 0)
                    continue;
                if (!location.getChunk().isLoaded())
                    continue;

                FireworkEffect.Builder fb = FireworkEffect.builder();
                Random r = new Random();

                //随机颜色
                fb.withColor(
                        Color.fromRGB(r.nextInt(156) + 100, r.nextInt(156) + 100, r.nextInt(156) + 100),
                        Color.fromRGB(r.nextInt(136) + 120, r.nextInt(136) + 120, r.nextInt(136) + 120),
                        Color.fromRGB(r.nextInt(116) + 140, r.nextInt(116) + 140, r.nextInt(116) + 140),
                        Color.fromRGB(r.nextInt(96) + 160, r.nextInt(96) + 160, r.nextInt(96) + 160)
                );
                fb.withFade(
                        Color.fromRGB(r.nextInt(255), r.nextInt(255), r.nextInt(255)),
                        Color.fromRGB(r.nextInt(255), r.nextInt(255), r.nextInt(255))
                );

                //随机形状
                FireworkEffect.Type[] type = FireworkEffect.Type.values();
                fb.with(type[r.nextInt(type.length)]);

                //随机效果
                int t = r.nextInt(64);
                if (t % 2 == 0) {
                    fb.withFlicker();
                }
                if (t % 3 == 0 || t % 13 == 0) {
                    fb.withTrail();
                }

                int power = r.nextInt(3) + 2;

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Firework fw = (Firework) world.spawnEntity(location, EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();
                    fwm.clearEffects();
                    fwm.addEffect(fb.build());
                    fwm.setPower(power);
                    fw.setPersistent(false);
                    fw.setFireworkMeta(fwm);
                    Bukkit.getScheduler().runTaskLater(plugin, new RemoveFirework(fw), power * 30 + 40);
                });
            }
        }, 1, interval);
    }

    public static boolean getStatus() {
        return isWorking;
    }
}
