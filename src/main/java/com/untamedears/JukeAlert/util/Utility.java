package com.untamedears.JukeAlert.util;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import net.md_5.bungee.api.chat.TextComponent;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

// Static methods only
public class Utility {

	private static boolean debugging_ = false;

	public static boolean isDebugging() {

		return debugging_;
	}

	public static void setDebugging(boolean debugging) {

		debugging_ = debugging;
	}

	public static void notifyGroup(Group g, TextComponent message) throws SQLException {

		if (g == null) {
			return;
		}
		final JukeAlert plugin = JukeAlert.getInstance();
		Set<String> skipUUID = plugin.getJaLogger().getIgnoreUUIDs(g.getName());
		if (skipUUID == null) {
			// This should be fine as it is how it used to be done
			skipUUID = null;
		}
		OnlineGroupMembers iter = OnlineGroupMembers.get(g.getName()).skipList(skipUUID);
		for (Player player : iter) {
			if (NameAPI.getGroupManager().hasAccess(g, player.getUniqueId(),
					PermissionType.getPermission("SNITCH_NOTIFICATIONS"))) {
				RateLimiter.sendMessage(player, message);
			}
		}
	}

	public static void notifyGroup(Snitch snitch, Function<Player, TextComponent> messageFunction) throws SQLException {

		Group sG = snitch.getGroup();
		if (sG == null) {
			return;
		}
		final JukeAlert plugin = JukeAlert.getInstance();
		Set<String> skipUUID = plugin.getJaLogger().getIgnoreUUIDs(sG.getName());
		if (skipUUID == null) {
			// This should be fine as it is how it used to be done
			skipUUID = null;
		}
		OnlineGroupMembers iter = OnlineGroupMembers.get(sG.getName()).reference(snitch.getLoc()).skipList(skipUUID);
		if (!snitch.shouldLog()) {
			iter.maxDistance(JukeAlert.getInstance().getConfigManager().getMaxAlertDistanceNs());
		}
		for (Player player : iter) {
			if (NameAPI.getGroupManager().hasAccess(snitch.getGroup(), player.getUniqueId(),
					PermissionType.getPermission("SNITCH_NOTIFICATIONS"))) {
				RateLimiter.sendMessage(player, messageFunction.apply(player));
			}
		}
	}

	public static boolean immuneToSnitch(Snitch snitch, UUID accountId) {
		if (snitch.isSoftCulled()) {
			return true;
		}

		Group group = snitch.getGroup();
		if (group == null) {
			return true;
		}
		// Group object might be outdated so use name
		return NameAPI.getGroupManager().hasAccess(group.getName(), accountId,
			PermissionType.getPermission("SNITCH_IMMUNE"));
	}

	public static Snitch getSnitchUnderCursor(Player player) {

		SnitchManager manager = JukeAlert.getInstance().getSnitchManager();
		Iterator<Block> itr = new BlockIterator(player, 40); // Within 2.5 chunks
		while (itr.hasNext()) {
			final Block block = itr.next();
			final Material mat = block.getType();
			if (mat != Material.JUKEBOX) {
				continue;
			}
			final Snitch found = manager.getSnitch(block.getWorld(), block.getLocation());
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public static boolean doesSnitchExist(Snitch snitch, boolean shouldCleanup) {

		Location loc = snitch.getLoc();
		World world = loc.getWorld();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		Material type = world.getBlockAt(x, y, z).getType();
		boolean exists = Material.NOTE_BLOCK == type || Material.JUKEBOX == type || Material.CAULDRON == type;
		if (!exists && shouldCleanup) {
			final JukeAlert plugin = JukeAlert.getInstance();
			plugin.log("Removing ghost snitch '" + snitch.getName() + "' at x:" + x + " y:" + y + " z:" + z);
			plugin.getSnitchManager().removeSnitch(snitch);
			plugin.getJaLogger().logSnitchBreak(world.getName(), x, y, z);
		}
		return exists;
	}

	public static Snitch findClosestSnitch(Location loc, PermissionType perm, UUID player) {

		Snitch closestSnitch = null;
		double closestDistance = Double.MAX_VALUE;
		Set<Snitch> snitches = JukeAlert.getInstance().getSnitchManager().findSnitches(loc.getWorld(), loc);
		for (final Snitch snitch : snitches) {
			if (doesSnitchExist(snitch, true) && NameAPI.getGroupManager().hasAccess(snitch.getGroup(), player, perm)) {
				double distance = snitch.getLoc().distanceSquared(loc);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestSnitch = snitch;
				}
			}
		}
		return closestSnitch;
	}

	public static Snitch findLookingAtOrClosestSnitch(Player player, PermissionType perm) {

		Snitch cursorSnitch = getSnitchUnderCursor(player);
		if (cursorSnitch != null
				&& doesSnitchExist(cursorSnitch, true)
				&& NameAPI.getGroupManager().hasAccess(cursorSnitch.getGroup(), player.getUniqueId(), perm)) {
			return cursorSnitch;
		}
		return findClosestSnitch(player.getLocation(), perm, player.getUniqueId());
	}

	public static double getAngle(Snitch snitch, Player player) {
		int x = snitch.getX();
		int z = snitch.getZ();

		double px = player.getLocation().getX();
		double pz = player.getLocation().getZ();

		double angle = Math.toDegrees(Math.atan2(x - px, z - pz));
		if (angle < 0) {
			angle += 360;
		}
		return angle;
	}

	public static String getCompassDirection(double angle) {
		if (angle >= 337 || angle < 22) {
			return "S";
		} else if (angle < 67) {
			return "SE";
		} else if (angle < 112) {
			return "E";
		} else if (angle < 157) {
			return "NE";
		} else if (angle < 202) {
			return "N";
		} else if (angle < 247) {
			return "NW";
		} else if (angle < 292) {
			return "W";
		} else {
			return "SW";
		}
	}

	public static ItemStack materialToGuiItem(Material material) {
		if (material.isItem()) {
			return new ItemStack(material);
		} else {
			return new ItemStack(materialToItem(material));
		}
	}

	private static Material materialToItem(Material material) {
		// common materials that can't show up on a gui
		switch (material) {
			case CARROT:
				return Material.CARROT_ITEM;
			case CROPS:
				return Material.SEEDS;
			case POTATO:
				return Material.POTATO_ITEM;
			case BEETROOT_BLOCK:
				return Material.BEETROOT_SEEDS;
			case SUGAR_CANE_BLOCK:
				return Material.SUGAR_CANE;
			case BREWING_STAND:
				return Material.BREWING_STAND_ITEM;
			case CAULDRON:
				return Material.CAULDRON_ITEM;
			case SKULL:
				return Material.SKULL_ITEM;
			case WALL_SIGN:
			case SIGN_POST:
				return Material.SIGN;
			case STANDING_BANNER:
			case WALL_BANNER:
				return Material.BANNER;
			case FLOWER_POT:
				return Material.FLOWER_POT_ITEM;
			case REDSTONE_COMPARATOR_OFF:
			case REDSTONE_COMPARATOR_ON:
				return Material.REDSTONE_COMPARATOR;
			case DIODE_BLOCK_OFF:
			case DIODE_BLOCK_ON:
				// redstone repeater
				return Material.DIODE;
			case DARK_OAK_DOOR:
				return Material.DARK_OAK_DOOR_ITEM;
			case ACACIA_DOOR:
				return Material.ACACIA_DOOR_ITEM;
			case BIRCH_DOOR:
				return Material.BIRCH_DOOR_ITEM;
			case SPRUCE_DOOR:
				return Material.SPRUCE_DOOR_ITEM;
			case JUNGLE_DOOR:
				return Material.JUNGLE_DOOR_ITEM;
			case IRON_DOOR_BLOCK:
				return Material.IRON_DOOR;
			case WOODEN_DOOR:
				// minecraft why do you have wood door and wooden door??
				return Material.WOOD_DOOR;
			default:
				// fallback to just use a stone block if we can't get any other item on the gui
				return Material.STONE;
		}
	}
}
