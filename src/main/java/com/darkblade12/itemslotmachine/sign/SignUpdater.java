package com.darkblade12.itemslotmachine.sign;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.darkblade12.itemslotmachine.settings.Settings;

public final class SignUpdater {
	private SignUpdater() {}

	public static void updateSign(Player p, int x, int y, int z, String[] lines, int... splittable) {
		
		try {
			World w = p.getWorld();
			Block b = w.getBlockAt(x, y, z);
			if (b.getState() instanceof Sign) {
				String[] validated = validateLines(lines, splittable);
				p.sendSignChange(b.getLocation(), validated);
			}
		} catch (Exception e) {
			if (Settings.isDebugModeEnabled())
				e.printStackTrace();
		}
	}

	public static void updateSign(Player p, Location l, String[] lines, int... splittable) {
		updateSign(p, l.getBlockX(), l.getBlockY(), l.getBlockZ(), lines, splittable);
	}

	public static String[] validateLines(String[] lines, int... splittable) {
		if (lines.length > 4)
			throw new IllegalArgumentException("The lines array has an invalid length");
		for (int s : splittable) {
			if (s >= 0 && s < lines.length - 1) {
				String a = lines[s];
				if (a.length() > 15) {
					String[] p = a.split(" ");
					lines[s] = p[0];
					lines[s + 1] = p[1];
				}
			}
		}
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			lines[i] = line.length() > 15 ? line.substring(0, 15) : line;
		}
		return lines;
	}
}