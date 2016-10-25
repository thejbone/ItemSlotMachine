package com.darkblade12.itemslotmachine.reference;

import com.darkblade12.itemslotmachine.safe.SafeLocation;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

public final class ItemFrameFinder {

    private ItemFrameFinder() {
    }

    public static ItemFrame find(Location l) {
        for (Entity e : l.getChunk().getEntities())
            if (e instanceof ItemFrame)
                if (SafeLocation.noDistance(l, e.getLocation().getBlock().getLocation()))
                    return (ItemFrame) e;
        return null;
    }
}