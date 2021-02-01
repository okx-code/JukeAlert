package com.untamedears.JukeAlert.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.bukkit.Location;
import vg.civcraft.mc.namelayer.group.Group;

public class SuperSnitch extends Snitch {
	private static final int MAX_DAYS = 182;
	private static final int DAYS_PER_UNIT = 14;

	private Instant fuel;

	public SuperSnitch(Location loc, Group group, boolean shouldToggleLevers, Instant fuel) {
		super(loc, group, false, shouldToggleLevers);
		this.fuel = fuel;
	}

	@Override
	public void calculateDimensions() {
		super.calculateDimensions();
		this.miny = -1;
		if (getY() >= 254) {
			this.maxy = 300;
		} else {
			this.maxy = getY();
		}
	}

	public Instant getFuel() {
		return fuel;
	}

	public boolean refuel(int units) {
		Instant newFuel;
		if (isSoftCulled()) {
			newFuel = Instant.now();
		} else {
			newFuel = fuel;
		}
		newFuel = newFuel.plus(DAYS_PER_UNIT * (long) units, ChronoUnit.DAYS);
		if (Instant.now().until(newFuel, ChronoUnit.DAYS) > MAX_DAYS) {
			return false;
		} else {
			this.fuel = newFuel;
			return true;
		}
	}

	public int maxRefuel() {
		if (isSoftCulled()) {
			return MAX_DAYS / DAYS_PER_UNIT;
		}

		long maxDaysAdd = fuel.until(Instant.now().plus(MAX_DAYS, ChronoUnit.DAYS), ChronoUnit.DAYS);
		return ((int) maxDaysAdd) / DAYS_PER_UNIT;
	}

	@Override
	public boolean isSoftCulled() {
		return fuel.isBefore(Instant.now());
	}
}
