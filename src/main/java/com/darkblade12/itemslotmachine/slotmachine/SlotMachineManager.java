package com.darkblade12.itemslotmachine.slotmachine;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.coin.CoinConfig;
import com.darkblade12.itemslotmachine.manager.Manager;
import com.darkblade12.itemslotmachine.reader.TemplateReader;
import com.darkblade12.itemslotmachine.settings.Settings;
import com.darkblade12.itemslotmachine.statistic.StatisticComparator;
import com.darkblade12.itemslotmachine.statistic.Type;
import com.darkblade12.itemslotmachine.statistic.types.SlotMachineStatistic;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class SlotMachineManager extends Manager {
    private HashMap<String, SlotMachineConfig> machineConfigs;

    public SlotMachineManager(ItemSlotMachine plugin) {
        super(plugin);
        onInitialize();
    }

    @Override
    public boolean onInitialize() {
        machineConfigs = new HashMap<>();
        machineConfigs.putAll(Settings.getSlotMachineConfigs());

        for(Map.Entry<String, SlotMachineConfig> b : machineConfigs.entrySet()){
            b.getValue().loadSlotMachines(plugin);
        }
        registerEvents();
        return true;
    }

    @Override
    public void onDisable() {
        unregisterAll();
        machineConfigs.forEach((a,b) -> {
            b.deactivateSlotMachines();
        });
    }

    public SlotMachine getSlotMachine(Location block){
        for(Map.Entry<String, SlotMachineConfig> s : machineConfigs.entrySet()){
            if(s.getValue().getSlotMachine(block) != null){
                return s.getValue().getSlotMachine(block);
            }
        }
        return null;
    }
    public SlotMachine getSlotMachine(String name){
        for(Map.Entry<String, SlotMachineConfig> s : machineConfigs.entrySet()){
            if(s.getValue().getSlotMachine(name) != null){
                return s.getValue().getSlotMachine(name);
            }
        }
        return null;
    }

    SlotMachineConfig getSlotMachineConfig(SlotMachine slot){
        for(Map.Entry<String, SlotMachineConfig> s : machineConfigs.entrySet()){
            if(s.getValue().hasSlotMachine(slot)){
                return s.getValue();
            }
        }
        return null;
    }

    public ArrayList<SlotMachineConfig> getSlotMachineConfigs(){
        ArrayList<SlotMachineConfig> slotMachineConfigs = new ArrayList<>();
        machineConfigs.forEach((a,b) ->
        {
            slotMachineConfigs.add(b);
        });
        return slotMachineConfigs;
    }

    public SlotMachineConfig getSlotMachineConfig(String name){
        return machineConfigs.get(name);
    }

    private SlotMachine getInteractedSlotMachine(Location l) {
        for(Map.Entry<String, SlotMachineConfig> s : machineConfigs.entrySet()) {
            for (int i = 0; i < s.getValue().getSlotMachines().size(); i++) {
                SlotMachine slot = s.getValue().getSlotMachines().get(i);
                if (slot.hasInteracted(l))
                    return slot;
            }
        }
        return null;
    }

    private int getActivatedAmount(Player p) {
        int a = 0;
        for(Map.Entry<String, SlotMachineConfig> s : machineConfigs.entrySet()) {
            for (int i = 0; i < s.getValue().getSlotMachines().size(); i++) {
                SlotMachine slot = s.getValue().getSlotMachines().get(i);
                if (slot.isUser(p) && slot.isActive())
                    a++;
            }
        }
        return a;
    }

    public boolean doesCoinAndMachineMatch(ItemStack coin, SlotMachineConfig config){
        return coin.isSimilar(config.getCoinConfig().getCOIN_ITEM());
    }

    public List<SlotMachineStatistic> getTop(Type t) {
        List<SlotMachineStatistic> top = new ArrayList<SlotMachineStatistic>();
        for(Map.Entry<String, SlotMachineConfig> s : machineConfigs.entrySet()) {
            for (int i = 0; i < s.getValue().getSlotMachines().size(); i++)
                top.add(s.getValue().getSlotMachines().get(i).getStatistic());
            Collections.sort(top, new StatisticComparator(t));
        }
        return top;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player p = event.getPlayer();
        SlotMachine s = getSlotMachine(event.getBlock().getLocation());
        if (s != null && !s.isPermittedToModify(p)) {
            event.setCancelled(true);
            p.sendMessage(plugin.messageManager.slot_machine_modifying_not_allowed());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakEvent event) {
        SlotMachine s = getSlotMachine(event.getEntity().getLocation());
        if (s != null)
            if (event instanceof HangingBreakByEntityEvent) {
                Entity e = ((HangingBreakByEntityEvent) event).getRemover();
                if (e instanceof Player) {
                    Player p = (Player) e;
                    if (!s.isPermittedToModify(p)) {
                        event.setCancelled(true);
                        p.sendMessage(plugin.messageManager.slot_machine_modifying_not_allowed());
                    }
                } else
                    event.setCancelled(true);
            } else
                event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        SlotMachine s = getSlotMachine(event.getBlock().getLocation());
        if (s != null && !s.isPermittedToModify(p)) {
            event.setCancelled(true);
            p.sendMessage(plugin.messageManager.slot_machine_modifying_not_allowed());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        SlotMachine s = getSlotMachine(event.getBlock().getLocation());
        if (s == null)
            s = getSlotMachine(event.getBlockAgainst().getLocation());
        if (s != null && !s.isPermittedToModify(p)) {
            event.setCancelled(true);
            p.sendMessage(plugin.messageManager.slot_machine_modifying_not_allowed());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player p = event.getPlayer();
        Entity e = event.getRightClicked();
        if (e instanceof Hanging) {
            SlotMachine s = getSlotMachine(e.getLocation());
            if (s != null && !s.isPermittedToModify(p)) {
                event.setCancelled(true);
                p.sendMessage(plugin.messageManager.slot_machine_modifying_not_allowed());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity e = event.getEntity();
        if (e instanceof Hanging) {
            SlotMachine s = getSlotMachine(e.getLocation());
            if (s != null)
                if (event instanceof EntityDamageByEntityEvent) {
                    Entity d = ((EntityDamageByEntityEvent) event).getDamager();
                    if (d instanceof Player) {
                        Player p = (Player) d;
                        if (!s.isPermittedToModify(p)) {
                            event.setCancelled(true);
                            p.sendMessage(plugin.messageManager.slot_machine_modifying_not_allowed());
                        }
                    } else
                        event.setCancelled(true);
                } else
                    event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            ItemStack h = p.getItemInHand();
            Location l = event.getClickedBlock().getLocation();
            SlotMachine s = getSlotMachine(l);
            SlotMachineConfig slotMachineConfig = getSlotMachineConfig(s);
            if (h.getType() == Material.WATER_BUCKET || h.getType() == Material.LAVA_BUCKET) {
                if (s != null && !s.isPermittedToModify(p)) {
                    event.setCancelled(true);
                    p.sendMessage(plugin.messageManager.slot_machine_modifying_not_allowed());
                    return;
                }
                return;
            }
            if (s != null) {
                if (!plugin.coinManager.isCoin(h)) {
                    if (!h.getType().isBlock() || h.getType() == Material.AIR)
                        if (p.hasPermission("ItemSlotMachine.slot.check") || p.hasPermission("ItemSlotMachine.slot.*") || p.hasPermission("ItemSlotMachine.*"))
                            p.sendMessage(plugin.messageManager.slot_machine_clicked(s.getName()));
                } else if (s.hasInteracted(l)) {
                    event.setCancelled(true);
                    if (!s.isPermittedToUse(p)) {
                        p.sendMessage(plugin.messageManager.slot_machine_usage_not_allowed());
                    } else {
                        if (s.isBroken())
                            p.sendMessage(plugin.messageManager.slot_machine_broken());
                        else if (s.isActive()) {
                        }
                        else if (s.isPlayerLockEnabled() && !s.isLockExpired() && !s.isUser(p))
                            p.sendMessage(plugin.messageManager.slot_machine_locked(s.getUserName(), s.getRemainingLockTime()));
                        else if (p.getGameMode() == GameMode.CREATIVE && !s.isCreativeUsageEnabled())
                            p.sendMessage(plugin.messageManager.slot_machine_creative_not_allowed());
                        else {
                            assert slotMachineConfig != null;
                            if (!s.hasEnoughCoins(p, slotMachineConfig.getCoinConfig()))
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.messageManager.slot_machine_not_enough_coins(s.getActivationAmount(),slotMachineConfig.getCoinConfig().getCOIN_DISPLAY())));
                            else if (slotMachineConfig.isLIMITED_USAGE() && getActivatedAmount(p) + 1 > slotMachineConfig.getMAX_USAGE())
                                p.sendMessage(plugin.messageManager.slot_machine_limited_usage(slotMachineConfig.getMAX_USAGE()));
                            else
                                s.activate(p);
                        }
                    }
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            SlotMachine s = getInteractedSlotMachine(event.getClickedBlock().getLocation());
            if (s != null && s.isPermittedToHalt(p)) {
                event.setCancelled(true);
                s.halt();
            }
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        for(Map.Entry<String, SlotMachineConfig> s : machineConfigs.entrySet()){
            s.getValue().deactivateUsed(event.getPlayer());
        }
    }

    public boolean hasSlotMachine(String name) {
        for (Map.Entry<String, SlotMachineConfig> s : machineConfigs.entrySet()) {
            if(s.getValue().hasSlotMachine(name)){
                return true;
            }
        }
        return false;
    }

    public void register(SlotMachineConfig sc, SlotMachine slotMachine) {
        machineConfigs.forEach((a,b) -> {
            if(b.getSLOT_NAME().equals(sc.getSLOT_NAME())){
                b.register(slotMachine);
            }
        });
    }

    public void reload(SlotMachine s) {
        machineConfigs.forEach((a,b) -> {
            try {
                b.reload(plugin, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public int getSlotMachineAmount() {
        AtomicInteger totalMachines = new AtomicInteger();
        machineConfigs.forEach((a,b) -> {
            totalMachines.addAndGet(b.getSlotMachineAmount());
        });
        return totalMachines.intValue();
    }

    public List<SlotMachine> getAllSlotMachines() {
        List<SlotMachine> slots = new ArrayList<>();
        machineConfigs.forEach((a,b) -> {
            slots.addAll(b.getSlotMachines());
        });
        return slots;
    }

    public void unregister(SlotMachine s) {
        machineConfigs.forEach((a,b) -> {
            if(b.hasSlotMachine(s))
                b.unregister(s);
        });
    }
}