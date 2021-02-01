package com.untamedears.JukeAlert.command.commands;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.SuperSnitch;
import com.untamedears.JukeAlert.util.Utility;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RefuelCommand extends PlayerCommand {

	public RefuelCommand() {
		super("Refuel");
		setDescription("Refuel super snitch");
		setUsage("/jarefuel <'max' | stamina(int)>");
		setArguments(0, 1);
		setIdentifier("jarefuel");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		if (args.length < 1) {
			SuperSnitch snitch = findClosestSnitch(player.getLocation(), PermissionType.getPermission("SNITCH_REFUEL"), player.getUniqueId());
			if (snitch.isSoftCulled()) {
				player.sendMessage(ChatColor.YELLOW + "Super snitch is currently soft culled.");
			} else {
				player.sendMessage(ChatColor.YELLOW + "Super snitch is fueled until "
						+ ChatColor.GRAY + DateTimeFormatter.ISO_LOCAL_DATE
						.format(snitch.getFuel().atOffset(ZoneOffset.UTC)));
			}
			return false;
		}

		int max;
		if (args[0].equalsIgnoreCase("max")) {
			max = 100;
		} else {
			try {
				max = Integer.parseInt(args[0]);
			} catch (NumberFormatException ex) {
				return false;
			}
		}
		SuperSnitch snitch = findClosestSnitch(player.getLocation(), PermissionType.getPermission("SNITCH_REFUEL"), player.getUniqueId());

		if (JukeAlert.getInstance().getSuperSnitchHandler().refuelSnitch(player, snitch, max)) {
			Location loc1 = snitch.getLoc();
			String loc = "(" + loc1.getBlockX() + " " + loc1.getBlockY() + " " + loc1.getBlockZ() + ")";
			player.sendMessage(ChatColor.YELLOW + "Super snitch at " + loc + " is now fueled until "
					+ ChatColor.GRAY + DateTimeFormatter.ISO_LOCAL_DATE.format(snitch.getFuel().atOffset(ZoneOffset.UTC)));
			Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(),
					() -> JukeAlert.getInstance().getJaLogger().updateSuperSnitch(snitch.getId(), snitch.getFuel()));
		}
		return true;
	}
	public static SuperSnitch findClosestSnitch(Location loc, PermissionType perm, UUID player) {
		SuperSnitch closestSnitch = null;
		double closestDistance = Double.MAX_VALUE;
		Set<Snitch> snitches = JukeAlert.getInstance().getSnitchManager().findSnitches(loc.getWorld(), loc);
		for (Snitch snitch : snitches) {
			if (snitch instanceof SuperSnitch
					&& Utility.doesSnitchExist(snitch, true)
					&& NameAPI.getGroupManager().hasAccess(snitch.getGroup(), player, perm)) {
				double distance = snitch.getLoc().distanceSquared(loc);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestSnitch = (SuperSnitch) snitch;
				}
			}
		}
		return closestSnitch;
	}

	@Override
	public List<String> tabComplete(CommandSender commandSender, String[] strings) {
		return null;
	}
}
