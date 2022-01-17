package com.darkblade12.itemslotmachine.slotmachine;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.coin.CoinConfig;
import com.darkblade12.itemslotmachine.nameable.NameGenerator;
import com.darkblade12.itemslotmachine.nameable.NameableComparator;
import com.darkblade12.itemslotmachine.nameable.NameableList;
import com.darkblade12.itemslotmachine.settings.Settings;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class SlotMachineConfig implements NameGenerator {
    private String SLOT_NAME;
    private String DEFAULT_NAME;

    private CoinConfig coinConfig;
    private File DIRECTORY;

    private NameableComparator<SlotMachine> comparator;
    private NameableList<SlotMachine> slotMachines;

    private boolean LIMITED_USAGE;
    private int MAX_USAGE;

    public SlotMachineConfig(String slot_name, String default_name) {
        SLOT_NAME = slot_name;
        DEFAULT_NAME = default_name;
        comparator = new NameableComparator<SlotMachine>(default_name);
        slotMachines = new NameableList<SlotMachine>();
        DIRECTORY = new File("plugins/ItemSlotMachine/slot machines/" + SLOT_NAME + "/");
    }

    public void deactivateSlotMachines(){
        for (int i = 0; i < slotMachines.size(); i++)
            slotMachines.get(i).deactivate();
    }

    public String generateNames(){
        Set<Integer> used = new HashSet<Integer>();
        for (String name : getNames())
            if (name.contains(DEFAULT_NAME))
                try {
                    used.add(Integer.parseInt(name.replace(DEFAULT_NAME, "")));
                } catch (Exception e) {
                    /* custom ids are ignored */
                }
        int n = 1;
        while (used.contains(n))
            n++;
        return DEFAULT_NAME.replace("<num>", Integer.toString(n));
    }

    public NameableComparator<SlotMachine> getComparator() {
        return comparator;
    }

    public String getSLOT_NAME() {
        return SLOT_NAME;
    }

    public void setSLOT_NAME(String SLOT_NAME) {
        this.SLOT_NAME = SLOT_NAME;
    }

    public String getDEFAULT_NAME() {
        return DEFAULT_NAME;
    }

    public void setDEFAULT_NAME(String DEFAULT_NAME) {
        this.DEFAULT_NAME = DEFAULT_NAME;
    }

    public boolean isLIMITED_USAGE() {
        return LIMITED_USAGE;
    }

    public void setLIMITED_USAGE(boolean LIMITED_USAGE) {
        this.LIMITED_USAGE = LIMITED_USAGE;
    }

    public int getMAX_USAGE() {
        return MAX_USAGE;
    }

    public void setMAX_USAGE(int MAX_USAGE) {
        this.MAX_USAGE = MAX_USAGE;
    }

    @Override
    public String generateName() {
        Set<Integer> used = new HashSet<>();
        for (String name : getNames())
            if (name.contains(SLOT_NAME))
                try {
                    used.add(Integer.parseInt(name.replace(SLOT_NAME, "")));
                } catch (Exception e) {
                    /* custom ids are ignored */
                }
        int n = 1;
        while (used.contains(n))
            n++;
        return DEFAULT_NAME.replace("<num>", Integer.toString(n));
    }

    private void sort() {
        Collections.sort(slotMachines, comparator);
    }

    public void loadSlotMachines(ItemSlotMachine plugin) {
        slotMachines = new NameableList<SlotMachine>(true);
        for (String name : getNames())
            try {
                slotMachines.add(SlotMachine.load(plugin, name, getSLOT_NAME()));
            } catch (Exception e) {
                plugin.l.warning("Failed to load slot machine '" + name + "'! Cause: " + e.getMessage());
                if (Settings.isDebugModeEnabled())
                    e.printStackTrace();
            }
        sort();
        int amount = slotMachines.size();
        plugin.l.info(amount + " slot machine" + (amount == 1 ? "" : "s") + " loaded.");
    }

    public void register(SlotMachine s) {
        slotMachines.add(s);
        sort();
    }

    public void unregister(SlotMachine s) {
        slotMachines.remove(s.getName());
        sort();
        s.destruct();
    }

    public void reload(ItemSlotMachine plugin, SlotMachine s) throws Exception {
        s.deactivate();
        slotMachines.remove(s.name);
        slotMachines.add(SlotMachine.load(plugin, s.name, SLOT_NAME));
    }

    public void deactivateUsed(Player p) {
        for (int i = 0; i < slotMachines.size(); i++) {
            SlotMachine s = slotMachines.get(i);
            if (s.isUser(p))
                s.deactivate();
        }
    }

    @Override
    public Set<String> getNames() {
        Set<String> names = new HashSet<>();
        if (DIRECTORY.exists() && DIRECTORY.isDirectory()){
            for (File f : DIRECTORY.listFiles()) {
                String name = f.getName();
                if (name.endsWith(".yml"))
                    names.add(name.replace(".yml", ""));
            }
        }
        return names;
    }

    public boolean hasName(String name) {
        for (String n : getNames())
            if (n.equalsIgnoreCase(name))
                return true;
        return false;
    }

    public SlotMachine getSlotMachine(String name) {
        return slotMachines.get(name);
    }

    public List<SlotMachine> getSlotMachines() {
        return Collections.unmodifiableList(slotMachines);
    }

    public SlotMachine getSlotMachine(Location l) {
        for (int i = 0; i < slotMachines.size(); i++) {
            SlotMachine s = slotMachines.get(i);
            if (s.isInsideRegion(l))
                return s;
        }
        return null;
    }



    public boolean hasSlotMachine(String name) {
        return slotMachines.getNames().contains(name);
    }
    public boolean hasSlotMachine(SlotMachine name) {
        return slotMachines.contains(name);
    }

    public int getSlotMachineAmount() {
        return slotMachines.size();
    }

    public CoinConfig getCoinConfig() {
        return coinConfig;
    }

    public void setCoinConfig(CoinConfig coinConfig) {
        this.coinConfig = coinConfig;
    }
}
