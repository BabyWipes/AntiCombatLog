package com.oresomecraft.AntiCombatLog;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

public class OresomeListener implements Listener {

    public AntiCombatLog plugin = AntiCombatLog.getInstance();

    public OresomeListener() {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                Utility.createNewUserProfile(p.getName());
            }
        });
        AntiLogPlayer.createInstance(p.getName());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (plugin.recentlyDamaged.contains(p.getName())) return;
        plugin.recentlyDamaged.add(p.getName());
        plugin.logTimer(p.getName());
    }

    @EventHandler
    public void onFalling(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (plugin.recentlyDamaged.contains(p.getName())) return;
        if (p.getVelocity().getY() > (-2)) return;
        plugin.recentlyDamaged.add(p.getName());
        plugin.logTimer(p.getName());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        if (e.getMessage().contains("leave")) {
            if (plugin.recentlyDamaged.contains(p.getName())) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    public void run() {
                        AntiLogPlayer alp = AntiLogPlayer.getAntiLogPlayer(p.getName());
                        Utility.incrementViolations(p.getName());
                        Utility.autoPunish(p.getName(), alp.getViolations());
                    }
                });
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        final AntiLogPlayer alp = AntiLogPlayer.getAntiLogPlayer(p.getName());
        if (plugin.recentlyDamaged.contains(p.getName()) || p.getLocation().getY() < 2) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                public void run() {
                    Utility.incrementViolations(p.getName());
                    Utility.autoPunish(p.getName(), alp.getViolations());
                }
            });
        }
        AntiLogPlayer.removeInstance(p.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKick(PlayerKickEvent e) {
        Player p = e.getPlayer();
        //Was the player kicked...?
        plugin.recentlyDamaged.remove(p.getName());
    }
}