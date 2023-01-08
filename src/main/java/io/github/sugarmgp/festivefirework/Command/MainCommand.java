package io.github.sugarmgp.festivefirework.Command;

import io.github.sugarmgp.festivefirework.FestiveFirework;
import io.github.sugarmgp.festivefirework.Util.FireworkUtil;
import io.github.sugarmgp.festivefirework.Util.TimerManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainCommand implements CommandExecutor, TabExecutor {
    private static void sendHelp(String head, CommandSender commandSender) {
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "插件用法如下：");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff point add <名称> - 添加当前玩家位置为一个燃放点");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff point del <名称> - 删除一个燃放点");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff point rename <名称> <新名称> - 重命名一个燃放点");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff point list - 查看燃放点列表");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff start - 开始燃放烟花");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff stop - 停止燃放烟花");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff timer add-start <yyyyMMdd-HHmmss> - 添加定时开始燃放烟花");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff timer add-stop <yyyyMMdd-HHmmss> - 添加定时停止燃放烟花");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff timer del <序号> - 删除一个定时器");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff timer list - 查看定时器列表");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff interval set <数值> - 设置间隔时间 (ticks)");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff interval get - 查看间隔时间");
        commandSender.sendMessage(head + ChatColor.DARK_AQUA + "/ff reload - 重载插件");
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FestiveFirework plugin = FestiveFirework.getPlugin(FestiveFirework.class);
        FileConfiguration config = plugin.getConfig();
        String msgHead = ChatColor.YELLOW + "【节日焰火】";
        if (!(commandSender.hasPermission("ff.commands.use"))) {
            commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "您没有权限使用此命令");
            return false;
        }
        if (strings.length == 0) {
            sendHelp(msgHead, commandSender);
        } else if (strings.length == 1) {
            String message0 = strings[0];
            if (message0.equals("start")) {
                if (FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "烟花燃放已经开始了");
                    return false;
                }
                List<Map<?, ?>> points = config.getMapList("points");
                if (points.isEmpty()) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "燃放点列表为空");
                    return false;
                }
                int interval = config.getInt("interval");
                FireworkUtil.start(interval, points);
                commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "开始燃放烟花");
            } else if (message0.equals("stop")) {
                if (!FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "烟花燃放已经停止了");
                    return false;
                }
                FireworkUtil.stop();
                commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "停止燃放烟花");
            } else if (message0.equals("reload")) {
                if (FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "请先停止烟花燃放");
                    return false;
                }
                plugin.reloadConfig();
                plugin.getTimerManager().reloadTimerConfig();
                commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "成功重载插件");
            } else if (message0.equals("help")) {
                sendHelp(msgHead, commandSender);
            } else {
                commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "语法错误");
            }
        } else if (strings.length == 2) {
            String message0 = strings[0];
            String message1 = strings[1];
            if (message0.equals("point") && message1.equals("list")) {
                List<Map<?, ?>> points = config.getMapList("points");
                if (points.isEmpty()) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "燃放点列表为空");
                    return false;
                }
                commandSender.sendMessage(msgHead + ChatColor.DARK_AQUA + "燃放点列表如下：");
                for (Map<?, ?> map : points) {
                    String name = (String) map.get("name");
                    String world = (String) map.get("world");
                    String x = String.format("%.2f", (Double) map.get("x"));
                    String y = String.format("%.2f", (Double) map.get("y"));
                    String z = String.format("%.2f", (Double) map.get("z"));
                    commandSender.sendMessage(msgHead + ChatColor.DARK_AQUA + name + " " + world + " " + x + " " + y + " " + z);
                }
            } else if (message0.equals("timer") && message1.equals("list")) {
                TimerManager timerManager = plugin.getTimerManager();
                List<Map<?, ?>> timers = timerManager.getTimerList();
                if (timers.isEmpty()) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "定时器列表为空");
                    return false;
                }
                commandSender.sendMessage(msgHead + ChatColor.DARK_AQUA + "定时器列表如下：");
                for (int i = 0; i < timers.size(); i++) {
                    Map<?, ?> map = timers.get(i);
                    int t = (Integer) map.get("type");
                    String date = (String) map.get("date");
                    String type = "start";
                    if (t == 2) {
                        type = "stop ";
                    }
                    commandSender.sendMessage(msgHead + ChatColor.DARK_AQUA + i + " " + type + " " + date);
                }
            } else if (message0.equals("interval") && message1.equals("get")) {
                int interval = config.getInt("interval");
                double second = interval / 20.0;
                if (interval == 1) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_AQUA + "间隔时间为 1 tick (0.05s)");
                } else {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_AQUA + "间隔时间为 " + interval + " ticks (" + String.format("%.2f", second) + "s)");
                }
            } else {
                commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "语法错误");
            }
        } else if (strings.length == 3) {
            String message0 = strings[0];
            String message1 = strings[1];
            String message2 = strings[2];
            if (message0.equals("point")) {
                if (message1.equals("add")) {
                    if (!(commandSender instanceof Player)) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "此命令无法在控制台调用");
                        return false;
                    }
                    if (FireworkUtil.getStatus()) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "请先停止烟花燃放");
                        return false;
                    }
                    Location location = ((Player) commandSender).getLocation();
                    message2 = message2.toLowerCase();
                    List<Map<?, ?>> points = config.getMapList("points");
                    int flag = findPoint(points, message2);
                    if (flag != -1) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "已存在同名燃放点");
                        return false;
                    }
                    points.add(createPoint(message2, location.getWorld().getName(), location.getX(), location.getY(), location.getZ()));
                    config.set("points", points);
                    plugin.saveConfig();
                    commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "成功添加 " + message2 + " 燃放点");
                } else if (message1.equals("del")) {
                    if (FireworkUtil.getStatus()) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "请先停止烟花燃放");
                        return false;
                    }
                    List<Map<?, ?>> points = config.getMapList("points");
                    message2 = message2.toLowerCase();
                    int flag = findPoint(points, message2);
                    if (flag == -1) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "找不到该燃放点");
                    } else {
                        points.remove(flag);
                        config.set("points", points);
                        plugin.saveConfig();
                        commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "成功删除 " + message2 + " 燃放点");
                    }
                } else {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "语法错误");
                }
            } else if (message0.equals("timer")) {
                TimerManager timerManager = plugin.getTimerManager();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
                if (message1.equals("add-start")) {
                    Date date;
                    try {
                        date = formatter.parse(message2);
                    } catch (ParseException e) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "时间格式错误");
                        return false;
                    }
                    boolean flag = timerManager.addTimer(1, date);
                    if (!flag) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "此时间已经被占用");
                    } else {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "成功添加定时器");
                    }
                } else if (message1.equals("add-stop")) {
                    Date date;
                    try {
                        date = formatter.parse(message2);
                    } catch (ParseException e) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "时间格式错误");
                        return false;
                    }
                    boolean flag = timerManager.addTimer(2, date);
                    if (!flag) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "此时间已经被占用");
                    } else {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "成功添加定时器");
                    }
                } else if (message1.equals("del")) {
                    int num;
                    try {
                        num = Integer.parseInt(message2);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "语法错误");
                        return false;
                    }
                    boolean flag = timerManager.delTimer(num);
                    if (!flag) {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "找不到该定时器");
                    } else {
                        commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "成功删除定时器");
                    }
                } else {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "语法错误");
                }
            } else if (message0.equals("interval") && message1.equals("set")) {
                if (FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "请先停止烟花燃放");
                    return false;
                }
                int num;
                try {
                    num = Integer.parseInt(message2);
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "语法错误");
                    return false;
                }
                if (num >= 1 && num <= 72000) {
                    config.set("interval", num);
                    plugin.saveConfig();
                    commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "成功设置间隔时间为 " + num);
                } else {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "间隔时间只能设置为 1 至 72000 的整数");
                }
            } else {
                commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "语法错误");
            }
        } else if (strings.length == 4) {
            String message0 = strings[0];
            String message1 = strings[1];
            String message2 = strings[2];
            String message3 = strings[3];
            if (message0.equals("point") && message1.equals("rename")) {
                if (FireworkUtil.getStatus()) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "请先停止烟花燃放");
                    return false;
                }
                List<Map<?, ?>> points = config.getMapList("points");
                message2 = message2.toLowerCase();
                message3 = message3.toLowerCase();
                int flag = findPoint(points, message2);
                if (flag == -1) {
                    commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "找不到该燃放点");
                } else {
                    Map<String, Object> point = (Map<String, Object>) points.get(flag);
                    point.put("name", message3);
                    points.set(flag, point);
                    config.set("points", points);
                    plugin.saveConfig();
                    commandSender.sendMessage(msgHead + ChatColor.DARK_GREEN + "成功重命名 " + message2 + " 燃放点为 " + message3 + " 燃放点");
                }
            } else {
                commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "语法错误");
            }
        } else {
            commandSender.sendMessage(msgHead + ChatColor.DARK_RED + "语法错误");
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
        if (args.length == 1) {
            ArrayList<String> commandTab = new ArrayList<String>();
            commandTab.add("point");
            commandTab.add("start");
            commandTab.add("stop");
            commandTab.add("timer");
            commandTab.add("interval");
            commandTab.add("reload");
            commandTab.add("help");
            return commandTab;
        } else if (args.length == 2) {
            if (args[0].equals("point")) {
                ArrayList<String> commandTab = new ArrayList<String>();
                commandTab.add("add");
                commandTab.add("del");
                commandTab.add("rename");
                commandTab.add("list");
                return commandTab;
            } else if (args[0].equals("timer")) {
                ArrayList<String> commandTab = new ArrayList<String>();
                commandTab.add("add-start");
                commandTab.add("add-stop");
                commandTab.add("del");
                commandTab.add("list");
                return commandTab;
            } else if (args[0].equals("interval")) {
                ArrayList<String> commandTab = new ArrayList<String>();
                commandTab.add("set");
                commandTab.add("get");
                return commandTab;
            }
        } else if (args.length == 3) {
            if (args[0].equals("point") && (args[1].equals("del") || args[1].equals("rename"))) {
                ArrayList<String> commandTab = new ArrayList<String>();
                FileConfiguration config = FestiveFirework.getProvidingPlugin(FestiveFirework.class).getConfig();
                List<Map<?, ?>> points = config.getMapList("points");
                for (Map<?, ?> map : points) {
                    commandTab.add((String) map.get("name"));
                }
                return commandTab;
            }
        }
        return null;
    }
}
