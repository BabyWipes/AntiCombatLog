package com.oresomecraft.AntiCombatLog;

import org.bukkit.Bukkit;

public class AntiLogPlayer {

    String playerID = "none";

    public AntiLogPlayer(String ID) {
        playerID = ID;
    }

    private int violations = 0;

    public void refreshViolations() {
        Bukkit.getScheduler().runTaskAsynchronously(AntiCombatLog.getInstance(), new Runnable() {
            public void run() {
                violations = Utility.checkViolationsFromDatabase(playerID);
            }
        });
    }

    public int getViolations() {
        return violations;
    }

    public void incrementViolations() {
        violations++;
    }

    public static void createInstance(String name) {
        AntiLogPlayer alp = new AntiLogPlayer(name);
        AntiCombatLog.getPlayerObjects().put(name, alp);
        alp.refreshViolations();
    }

    public static void removeInstance(String name) {
        AntiCombatLog.getPlayerObjects().remove(name);
    }

    public static AntiLogPlayer getAntiLogPlayer(String name) {
        return AntiCombatLog.getPlayerObjects().get(name);
    }

    public void setViolations(int increment){
        violations = increment;
    }
}
