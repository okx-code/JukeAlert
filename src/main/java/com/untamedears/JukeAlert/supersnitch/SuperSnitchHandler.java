package com.untamedears.JukeAlert.supersnitch;

import com.untamedears.JukeAlert.model.SuperSnitch;
import org.bukkit.entity.Player;

public interface SuperSnitchHandler {

	/**
	 * Called when a player places a super snitch.
	 * @param player the player placing the super snitch
	 * @param snitch the super snitch
	 * @return true if the player should be allowed to place the super snitch, false otherwise
	 */
	boolean placeSnitch(Player player, SuperSnitch snitch);

	/**
	 * Called when a player breaks a super snitch.
	 * @param player the player breaking the super snitch
	 * @param snitch the super snitch
	 */
	void breakSnitch(Player player, SuperSnitch snitch);

	/**
	 * Called when a player right clicks a super snitch that they have access to (specifically,
	 * have permission to place snitches on the group it is reinforced to)
	 * @param player the player who right clicked the snitch
	 * @param snitch the super snitch
	 * @param max maximum units to refuel, specified by the user
	 * @return true if the snitch was refueled
	 */
	boolean refuelSnitch(Player player, SuperSnitch snitch, int max);
}
