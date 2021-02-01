package com.untamedears.JukeAlert.supersnitch;

import com.untamedears.JukeAlert.model.SuperSnitch;
import org.bukkit.entity.Player;

public class DefaultSuperSnitchHandler implements SuperSnitchHandler {

	@Override
	public boolean placeSnitch(Player player, SuperSnitch snitch) {
		return false;
	}

	@Override
	public void breakSnitch(Player player, SuperSnitch snitch) {

	}

	@Override
	public boolean refuelSnitch(Player player, SuperSnitch snitch, int max) {
		return false;
	}
}
