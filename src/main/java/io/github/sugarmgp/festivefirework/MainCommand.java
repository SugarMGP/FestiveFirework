package io.github.sugarmgp.festivefirework;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainCommand implements CommandExecutor, TabExecutor {
    private static boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }

    private static void sendHelp(String head, CommandSender commandSender) {
        commandSender.sendMessage(head + ChatColor.AQUA + "插件用法如下：");
        commandSender.sendMessage(head + ChatColor.AQUA + "/ff add <名称> - 添加当前玩家位置为一个燃放点");
        commandSender.sendMessage(head + ChatColor.AQUA + "/ff del <名称> - 删除一个燃放点");
        commandSender.sendMessage(head + ChatColor.AQUA + "/ff list - 查看燃放点列表");
        commandSender.sendMessage(head + ChatColor.AQUA + "/ff start - 开始燃放烟花");
        commandSender.sendMessage(head + ChatColor.AQUA + "/ff stop - 停止燃放烟花");
        commandSender.sendMessage(head + ChatColor.AQUA + "/ff reload - 重载插件");
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Plugin plugin = FestiveFirework.getProvidingPlugin(FestiveFirework.class);
        FileConfiguration config = plugin.getConfig();
        String msgHead = ChatColor.YELLOW + "【FestiveFirework】";
        if (!(commandSender.hasPermission("ff.commands.use"))) {
            commandSender.sendMessage(msgHead + ChatColor.RED + "您没有权限使用此命令");
            return false;
        }
        if (strings.length == 0) {
            sendHelp(msgHead, commandSender);
        } else if (strings.length == 2) {
            String message0 = strings[0];
            if (message0.equals("add")) {
                if (!(commandSender instanceof Player)) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "此命令无法在控制台调用");
                    return false;
                }
                if (FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "请先停止烟花燃放");
                    return false;
                }
                String message1 = strings[1];
                if (!isLetterDigit(message1)) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "燃放点名称只能包含数字和字母");
                    return false;
                }
                Location location = ((Player) commandSender).getLocation();
                message1 = message1.toLowerCase();
                List<Map<?, ?>> points = config.getMapList("points");
                int flag = findPoint(points, message1);
                if (flag != -1) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "已存在同名燃放点");
                    return false;
                }
                points.add(createPoint(message1, location.getWorld().getName(), location.getX(), location.getY(), location.getZ()));
                config.set("points", points);
                plugin.saveConfig();
                commandSender.sendMessage(msgHead + ChatColor.GREEN + "成功添加 " + message1 + " 燃放点");
            } else if (message0.equals("del")) {
                if (FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "请先停止烟花燃放");
                    return false;
                }
                String message1 = strings[1];
                if (!isLetterDigit(message1)) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "燃放点名称只能包含数字和字母");
                    return false;
                }
                List<Map<?, ?>> points = config.getMapList("points");
                message1 = message1.toLowerCase();
                int flag = findPoint(points, message1);
                if (flag == -1) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "找不到该燃放点");
                } else {
                    points.remove(flag);
                    config.set("points", points);
                    plugin.saveConfig();
                    commandSender.sendMessage(msgHead + ChatColor.GREEN + "成功删除 " + message1 + " 燃放点");
                }
            } else {
                commandSender.sendMessage(msgHead + ChatColor.RED + "语法错误");
            }
        } else if (strings.length == 1) {
            String message0 = strings[0];
            if (message0.equals("start")) {
                if (FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "烟花燃放已经开始了");
                    return false;
                }
                int interval = config.getInt("interval");
                if (interval < 10) {
                    commandSender.sendMessage(msgHead + ChatColor.GOLD + "由于性能原因，请不要设置 interval 小于 10");
                    commandSender.sendMessage(msgHead + ChatColor.GOLD + "将使用 10 作为 interval 值");
                    interval = 10;
                }
                FireworkUtil.start(interval, config.getMapList("points"));
                commandSender.sendMessage(msgHead + ChatColor.GREEN + "开始燃放烟花");
            } else if (message0.equals("stop")) {
                if (!FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "烟花燃放已经停止了");
                    return false;
                }
                FireworkUtil.stop();
                commandSender.sendMessage(msgHead + ChatColor.GREEN + "停止燃放烟花");
            } else if (message0.equals("list")) {
                List<Map<?, ?>> points = config.getMapList("points");
                if (points.isEmpty()) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "燃放点列表为空");
                    return false;
                }
                commandSender.sendMessage(msgHead + ChatColor.AQUA + "燃放点列表如下：");
                for (Map<?, ?> map : points) {
                    String name = (String) map.get("name");
                    String world = (String) map.get("world");
                    String x = String.format("%.1f", (Double) map.get("x"));
                    String y = String.format("%.1f", (Double) map.get("y"));
                    String z = String.format("%.1f", (Double) map.get("z"));
                    commandSender.sendMessage(msgHead + ChatColor.AQUA + name + " " + world + " " + x + " " + y + " " + z);
                }
            } else if (message0.equals("reload")) {
                if (FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.RED + "请先停止烟花燃放");
                    return false;
                }
                plugin.reloadConfig();
                commandSender.sendMessage(msgHead + ChatColor.GREEN + "成功重载插件");
            } else if (message0.equals("help")) {
                sendHelp(msgHead, commandSender);
            } else {
                commandSender.sendMessage(msgHead + ChatColor.RED + "语法错误");
            }
        } else {
            commandSender.sendMessage(msgHead + ChatColor.RED + "语法错误");
        }
        return false;
    }

    private Map<String, Object> createPoint(String name, String world, double x, double y, double z) {
        HashMap<String, Object> point = new HashMap<>();
        point.put("name", name);
        point.put("world", world);
        point.put("x", x);
        point.put("y", y);
        point.put("z", z);
        return point;
    }

    private int findPoint(List<Map<?, ?>> points, String str) {
        int num = -1;
        for (int i = 0; i < points.size(); i++) {
            Map<?, ?> map = points.get(i);
            String name = (String) map.get("name");
            if (name.equals(str)) {
                num = i;
                break;
            }
        }
        return num;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> commandTab = new ArrayList<String>();
        if (args.length == 1) {
            commandTab.add("add");
            commandTab.add("del");
            commandTab.add("list");
            commandTab.add("start");
            commandTab.add("stop");
            commandTab.add("help");
            commandTab.add("reload");
            return commandTab;
        } else if (args.length == 2 && args[0].equals("del")) {
            FileConfiguration config = FestiveFirework.getProvidingPlugin(FestiveFirework.class).getConfig();
            List<Map<?, ?>> points = config.getMapList("points");
            for (Map<?, ?> map : points) {
                commandTab.add((String) map.get("name"));
            }
            return commandTab;
        }
        return null;
    }
}
