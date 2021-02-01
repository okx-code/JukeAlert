package com.untamedears.JukeAlert.gui;

import com.untamedears.JukeAlert.model.SuperSnitch;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;

import com.untamedears.JukeAlert.model.Snitch;

public class SnitchOverviewGUI {

	private List<Snitch> snitches;

	private Player player;

	public SnitchOverviewGUI(Player p, List<Snitch> snitches) {
		this.snitches = snitches;
		this.player = p;
	}

	public void showScreen() {

		MultiPageView view = new MultiPageView(player, constructSnitchClickables(), "Nearby snitches", true);
		view.setMenuSlot(SnitchLogGUI.constructExitClick(), 3);
		view.showScreen();
	}

	private List<IClickable> constructSnitchClickables() {

		List<IClickable> clicks = new LinkedList<IClickable>();
		for (final Snitch snitch : snitches) {
			ItemStack is = new ItemStack(snitch instanceof SuperSnitch ? Material.CAULDRON_ITEM : (snitch.shouldLog() ? Material.JUKEBOX : Material.NOTE_BLOCK));
			ISUtils.setName(is, ChatColor.GOLD + snitch.getName());
			ISUtils.addLore(
				is, ChatColor.AQUA + "Located at " + snitch.getX() + ", " + snitch.getY() + ", " + snitch.getZ());
			ISUtils.addLore(is, ChatColor.YELLOW + "Group: " + snitch.getGroup().getName());
			ISUtils.addLore(is, ChatColor.GREEN + "Click to view the logs");
			if (snitch instanceof SuperSnitch) {
				ISUtils.addLore(is, ChatColor.GOLD + "Fueled until: " + ChatColor.GRAY
						+ DateTimeFormatter.ISO_LOCAL_DATE.format(((SuperSnitch) snitch).getFuel().atOffset(ZoneOffset.UTC)));
			}
			clicks.add(new Clickable(is) {
				@Override
				public void clicked(Player p) {
					SnitchLogGUI gui = new SnitchLogGUI(player, snitch);
					gui.showScreen();
				}
			});
		}
		return clicks;
	}
}
