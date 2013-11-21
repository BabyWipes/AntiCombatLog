package com.oresomecraft.AntiCombatLog;

import com.oresomecraft.AntiCombatLog.db.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Utility {

    public static AntiCombatLog plugin = AntiCombatLog.getInstance();

    public synchronized static void createTables() {
        MySQL mysql = new MySQL(plugin.logger,
                "[AntiCombatLogDB] ",
                plugin.storageHostname,
                plugin.storagePort,
                plugin.storageDatabase,
                plugin.storageUsername,
                plugin.storagePassword);

        mysql.open();

        if (!mysql.isTable(plugin.storageDatabase)) {
            try {
                mysql.query("CREATE TABLE `" + plugin.storageDatabase + "` (" +
                        "`name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
                        "`violations` INT)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        mysql.close();
    }

    public synchronized static void logQuery(String query) {
        synchronized (plugin.Input) {
            plugin.Input.add(query);
        }
    }

    public synchronized static int checkViolationsFromDatabase(String player) {
        MySQL mysql = new MySQL(plugin.logger,
                "[AntiCombatLogDB] ",
                plugin.storageHostname,
                plugin.storagePort,
                plugin.storageDatabase,
                plugin.storageUsername,
                plugin.storagePassword);
        mysql.open();

        ResultSet rs = null;
        try {
            rs = mysql.query("SELECT * FROM " + plugin.storageDatabase);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            while (rs.next()) {
                if (rs.getString("name").equals(player)) return rs.getInt("violations");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        mysql.close();

        return 0;

    }

    @Deprecated
    public static int checkViolations(String player) {
        AntiLogPlayer alp = AntiLogPlayer.getAntiLogPlayer(player);
        if(alp == null) return 0;
        return alp.getViolations();
    }

    public static synchronized boolean profileExists(String player) {
        MySQL mysql = new MySQL(plugin.logger,
                "[AntiCombatLogDB] ",
                plugin.storageHostname,
                plugin.storagePort,
                plugin.storageDatabase,
                plugin.storageUsername,
                plugin.storagePassword);
        mysql.open();

        ResultSet rs = null;
        try {
            rs = mysql.query("SELECT * FROM " + plugin.storageDatabase);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            while (rs.next()) {
                if (rs.getString("name").equals(player)) return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        mysql.close();

        return false;
    }

    public synchronized static void createNewUserProfile(String player) {
        if (profileExists(player)) return;
        logQuery("INSERT INTO `" + plugin.storageDatabase + "` VALUES ('" + player + "', '0')");
    }

    public static synchronized void incrementViolations(String player) {
        AntiLogPlayer alp = AntiLogPlayer.getAntiLogPlayer(player);
        if (alp != null) {
            alp.incrementViolations();
        }
        logQuery("UPDATE " + plugin.storageDatabase + " SET violations='" + (checkViolationsFromDatabase(player) + 1) + "' WHERE name='" + player + "'");
    }

    public static synchronized void resetViolations(String player) {
        AntiLogPlayer alp = AntiLogPlayer.getAntiLogPlayer(player);
        alp.setViolations(0);
        logQuery("UPDATE " + plugin.storageDatabase + " SET violations='0' WHERE name='" + player + "'");
    }

    public static void autoPunish(String player) {
        plugin.recentlyDamaged.remove(player);
        notifyStaff(ChatColor.GOLD + "WARNING: " + ChatColor.RED + "Player " + player + " has combat logged and was automatically punished!");
        if (checkViolationsFromDatabase(player) < 3 || checkViolationsFromDatabase(player) == 0) return;
        if (checkViolationsFromDatabase(player) == 3) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "tempban " + player + " 3hour [Automated] Combat Logging");
            return;
        }
        if (checkViolationsFromDatabase(player) == 5) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "tempban " + player + " 1day [Automated] Combat Logging");
            return;
        }
        if (checkViolationsFromDatabase(player) > 5) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "tempban " + player + " " + checkViolations(player) + "day  [Automated] Combat Logging");
        }
    }

    public static void notifyStaff(String s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("oresomenotes.staff")) {
                p.sendMessage(s);
            }
        }
    }
}
