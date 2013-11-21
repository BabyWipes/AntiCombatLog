package com.oresomecraft.AntiCombatLog;

import com.oresomecraft.AntiCombatLog.db.MySQL;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class AntiCombatLog extends JavaPlugin {

    private static AntiCombatLog plugin;
    public static Logger logger = Logger.getLogger("Minecraft");
    public static ArrayList<String> recentlyDamaged = new ArrayList<String>();
    public static ArrayList<String> Input = new ArrayList<String>();
    private static HashMap<String, AntiLogPlayer> playerObjects = new HashMap<String, AntiLogPlayer>();

    //SQL stuff, notouch.
    public static int storagePort = 0;
    public static String storageHostname = null;
    public static String storageUsername = null;
    public static String storagePassword = null;
    public static String storageDatabase = null;

    public static AntiCombatLog getInstance() {
        return plugin;
    }

    public static HashMap<String, AntiLogPlayer> getPlayerObjects() {
        return playerObjects;
    }

    @Override
    public void onEnable() {

        plugin = this;

        createConfig();

        // SQL stuff, before anything.
        storagePort = getConfig().getInt("database.port");
        storageHostname = getConfig().getString("database.hostname");
        storageUsername = getConfig().getString("database.username");
        storagePassword = getConfig().getString("database.password");
        storageDatabase = getConfig().getString("database.database");


        MySQL mysql = new MySQL(logger,
                "[AntiCombatLogDB] ",
                storageHostname,
                storagePort,
                storageDatabase,
                storageUsername,
                storagePassword);

        if (mysql.open()) {
            System.out.println("MySQL connected successfully!");
            Utility.createTables();
            mysql.close();
        } else {
            System.out.println("We couldn't connect to the SQL, see ya!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        manageListeners();

        registerCommands();

        cache();

    }

    private void manageListeners() {
        this.getServer().getPluginManager().registerEvents(new OresomeListener(), this);
    }

    public static void logTimer(final String target) {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
            public void run() {
                recentlyDamaged.remove(target);
            }
        }, 4 * 20);
    }

    public static int Cache;

    public static void cache() {
        Cache = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                if (Input.size() <= 0) return;

                MySQL mysql = new MySQL(logger,
                        "[AntiCombatLogDB] ",
                        storageHostname,
                        storagePort,
                        storageDatabase,
                        storageUsername,
                        storagePassword);

                mysql.open();
                while (Input.size() > 0) {
                    String s = Input.get(0);
                    try {
                        mysql.query(s);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    Input.remove(0);
                }
                mysql.close();
            }
        }, 3 * 20, 3 * 20);
    }

    private void createConfig() {
        boolean exists = new File("plugins/AntiCombatLog/config.yml").exists();
        if (!exists) {
            new File("plugins/AntiCombatLog").mkdir();
            getConfig().options().header("AntiCombatLog, made by R3creat3!");
            getConfig().set("database.hostname", "some_pointless_hostname");
            getConfig().set("database.port", 3306);
            getConfig().set("database.username", "oresomecraft");
            getConfig().set("database.password", "banana");
            getConfig().set("database.database", "AntiCombatLog");
            try {
                getConfig().save("plugins/AntiCombatLog/config.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * *******************************************************************
     * Code to use for sk89q's command framework goes below this comment! *
     * ********************************************************************
     */

    private CommandsManager<CommandSender> commands;
    private boolean opPermissions;

    private void registerCommands() {
        final AntiCombatLog plugin = this;
        // Register the commands that we want to use
        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender player, String perm) {
                return plugin.hasPermission(player, perm);
            }
        };
        commands.setInjector(new SimpleInjector(this));
        final CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, commands);

        cmdRegister.register(OresomeCommands.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

    public boolean hasPermission(CommandSender sender, String perm) {
        if (!(sender instanceof Player)) {
            if (sender.hasPermission(perm)) {
                return ((sender.isOp() && (opPermissions || sender instanceof ConsoleCommandSender)));
            }
        }
        return hasPermission(sender, ((Player) sender).getWorld(), perm);
    }

    public boolean hasPermission(CommandSender sender, World world, String perm) {
        if ((sender.isOp() && opPermissions) || sender instanceof ConsoleCommandSender || sender.hasPermission(perm)) {
            return true;
        }

        return false;
    }

    public void checkPermission(CommandSender sender, String perm)
            throws CommandPermissionsException {
        if (!hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
        }
    }

    public void checkPermission(CommandSender sender, World world, String perm)
            throws CommandPermissionsException {
        if (!hasPermission(sender, world, perm)) {
            throw new CommandPermissionsException();
        }
    }

}