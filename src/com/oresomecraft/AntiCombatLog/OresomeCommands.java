package com.oresomecraft.AntiCombatLog;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class OresomeCommands {
    AntiCombatLog plugin;

    public OresomeCommands(AntiCombatLog pl) {
        plugin = pl;
    }

    @Command(aliases = {"deleteviolations", "clearlog"},
            desc = "Clears a person's violation count.",
            usage = "<player>",
            min = 1,
            max = 1)
    @CommandPermissions({"oresomebattles.rank.admin"})
    public void clearlog(CommandContext args, CommandSender sender) {
        AntiLogPlayer alp = AntiLogPlayer.getAntiLogPlayer(args.getString(0));
        if (alp == null) {
            sender.sendMessage(ChatColor.RED + "That person is not online!");
            return;
        }
        if (alp.getViolations() <= 0) {
            sender.sendMessage(ChatColor.RED + "That person has no violations!");
            return;
        }
        Utility.resetViolations(args.getString(0));
        sender.sendMessage(ChatColor.GREEN + args.getString(0) + "'s violations was set to 0!");
    }

    @Command(aliases = {"viewviolations", "viewlog"},
            desc = "View a person's violation count.",
            usage = "<player>",
            min = 1,
            max = 1)
    @CommandPermissions({"oresomenotes.staff"})
    public void viewlog(CommandContext args, CommandSender sender) {
        AntiLogPlayer alp = AntiLogPlayer.getAntiLogPlayer(args.getString(0));
        if (alp == null) {
            sender.sendMessage(ChatColor.RED + "That person is not online!");
            return;
        }
        sender.sendMessage(ChatColor.RED + args.getString(0) + " has " + alp.getViolations() + " violations.");
    }
}