package cc.i9mc.qiandao;

import cc.i9mc.qiandao.Utils.SQLUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Qiandao extends JavaPlugin implements Listener {
    public static String ip;
    public static String database;
    public static String username;
    public static String password;

    private static int getCurrentMonthLastDay() {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        return a.get(Calendar.DATE);
    }

    public void onLoad() {
        saveDefaultConfig();
    }

    public void onEnable() {
        ip = getConfig().getString("mysql.ip");
        database = getConfig().getString("mysql.database");
        username = getConfig().getString("mysql.username");
        password = getConfig().getString("mysql.password");

        try {
            Connection conn = SQLUtil.getInstance().getConnection();
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `qiandao` (`player` varchar(64) NOT NULL,`qd1` varchar(2) DEFAULT NULL,`qd2` varchar(2) DEFAULT NULL,`qd3` varchar(2) DEFAULT NULL,`qd4` varchar(2) DEFAULT NULL,`qd5` varchar(2) DEFAULT NULL,`qd6` varchar(2) DEFAULT NULL,`qd7` varchar(2) DEFAULT NULL,`qd8` varchar(2) DEFAULT NULL,`qd9` varchar(2) DEFAULT NULL,`qd10` varchar(2) DEFAULT NULL,`qd11` varchar(2) DEFAULT NULL,`qd12` varchar(2) DEFAULT NULL,`qd13` varchar(2) DEFAULT NULL,`qd14` varchar(2) DEFAULT NULL,`qd15` varchar(2) DEFAULT NULL,`qd16` varchar(2) DEFAULT NULL,`qd17` varchar(2) DEFAULT NULL,`qd18` varchar(2) DEFAULT NULL,`qd19` varchar(2) DEFAULT NULL,`qd20` varchar(2) DEFAULT NULL,`qd21` varchar(2) DEFAULT NULL,`qd22` varchar(2) DEFAULT NULL,`qd23` varchar(2) DEFAULT NULL,`qd24` varchar(2) DEFAULT NULL,`qd25` varchar(2) DEFAULT NULL,`qd26` varchar(2) DEFAULT NULL,`qd27` varchar(2) DEFAULT NULL,`qd28` varchar(2) DEFAULT NULL,`qd29` varchar(2) DEFAULT NULL,`qd30` varchar(2) DEFAULT NULL,`qd31` varchar(2) DEFAULT NULL,PRIMARY KEY (`player`)) ENGINE=MyISAM DEFAULT CHARSET=gbk;");
        } catch (SQLException e1) {
            Bukkit.getConsoleSender().sendMessage("§c连接数据库失败！");
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (label.equalsIgnoreCase("qiandao")) {
            openQd(p);
        }
        return true;
    }

    private void openQd(Player player) {
        Connection connection = null;
        ResultSet resultSet = null;

        try {
            connection = SQLUtil.getInstance().getConnection();
            if (connection == null || connection.isClosed()) {
                return;
            }

            PreparedStatement ps = connection.prepareStatement("SELECT * FROM qiandao WHERE player=?");
            ps.setString(1, player.getName());
            resultSet = ps.executeQuery();

            Inventory inventory = getServer().createInventory(null, 27, "§c每日签到");

            ItemStack itemStack = new ItemStack(Material.SLIME_BALL, 1, (short) 0);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§a签到~");
            List<String> lore = new ArrayList<>();

            int day = 0;

            if (resultSet.next()) {
                for (int i = 1; i < getCurrentMonthLastDay(); i++) {
                    if (resultSet.getInt("qd" + (i < 10 ? "0" + i : i)) == Integer.valueOf(new SimpleDateFormat("MM").format(new Date()))) {
                        day++;
                    }
                }
            } else {
                ps = connection.prepareStatement("INSERT INTO qiandao VALUES (?,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)");
                ps.setString(1, player.getName());
                ps.executeUpdate();
            }

            int finalDay = day;
            getConfig().getStringList("lore").forEach((s) -> {
                lore.add(s.replace("%day%", String.valueOf(finalDay)));
            });

            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(13, itemStack);
            player.openInventory(inventory);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getWhoClicked() instanceof Player) {
            if (evt.getClickedInventory() == null) {
                return;
            }
            if (evt.getCurrentItem().getType() == Material.AIR || evt.getCurrentItem().getType() == null || !evt.getCurrentItem().hasItemMeta() || !evt.getCurrentItem().getItemMeta().hasDisplayName()) {
                return;
            }
            Player player = (Player) evt.getWhoClicked();

            if(evt.getCurrentItem().hasItemMeta() && !evt.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("§a签到~")){
                return;
            }

            evt.setCancelled(true);

            Connection connection = null;
            ResultSet resultSet = null;

            try {
                connection = SQLUtil.getInstance().getConnection();
                if (connection == null || connection.isClosed()) {
                    return;
                }

                PreparedStatement ps = connection.prepareStatement("SELECT * FROM qiandao WHERE player=?");
                ps.setString(1, player.getName());
                resultSet = ps.executeQuery();

                if (resultSet.next()) {
                    if (resultSet.getInt("qd" + new SimpleDateFormat("dd").format(new Date())) == Integer.valueOf(new SimpleDateFormat("MM").format(new Date()))) {
                        player.sendMessage("§e你明天才能签到!");
                    } else {
                        ps = connection.prepareStatement("UPDATE qiandao SET qd" + new SimpleDateFormat("dd").format(new Date()) + "=? WHERE player=?");
                        ps.setInt(1, Integer.valueOf(new SimpleDateFormat("MM").format(new Date())));
                        ps.setString(2, player.getName());
                        ps.executeUpdate();

                        List<String> list = getConfig().getStringList("qiandao.今天");
                        getServer().dispatchCommand(getServer().getConsoleSender(), list.get((int) (Math.random() * (list.size() - 1))).replace("%player%", player.getName()));
                        player.sendMessage("§a今日成功签到,获得随机奖励！~");
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);

                        int day = 0;
                        for (int i = 1; i < getCurrentMonthLastDay(); i++) {
                            if (resultSet.getInt("qd" + (i < 10 ? "0" + i : i)) == Integer.valueOf(new SimpleDateFormat("MM").format(new Date()))) {
                                day++;
                            }
                        }


                        for (String id : getConfig().getConfigurationSection("qiandao.累计").getKeys(false)) {
                            if (Integer.valueOf(id) == day) {
                                getConfig().getStringList("qiandao.累计." + id).forEach(s -> getServer().dispatchCommand(getServer().getConsoleSender(), s.replace("%player%", player.getName())));
                                player.sendMessage("§a累计签到达到§b" + id + "§a次,获得神秘大礼！");
                            }
                        }

                        openQd(player);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (resultSet != null) try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (connection != null) try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
