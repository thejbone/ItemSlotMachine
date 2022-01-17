package com.darkblade12.itemslotmachine.command.slot;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.command.CommandDetails;
import com.darkblade12.itemslotmachine.command.ICommand;
import com.darkblade12.itemslotmachine.design.Design;
import com.darkblade12.itemslotmachine.settings.Settings;
import com.darkblade12.itemslotmachine.slotmachine.SlotMachine;
import com.darkblade12.itemslotmachine.slotmachine.SlotMachineConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandDetails(name = "build", params = "<design> <type> [name]", executableAsConsole = false, permission = "ItemSlotMachine.slot.build")
public final class BuildCommand implements ICommand {
    @Override
    public void execute(ItemSlotMachine plugin, CommandSender sender, String label, String[] params) {
        Player p = (Player) sender;
        Design d = plugin.designManager.getDesign(params[0]);
        SlotMachineConfig sc = plugin.slotMachineManager.getSlotMachineConfig(params[1]);
        if (d == null) {
            p.sendMessage(plugin.messageManager.design_not_existent());
            return;
        }
        if (sc == null){
            p.sendMessage(plugin.messageManager.slot_machine_config_does_not_exist());
            return;
        }
        String name;
        plugin.getLogger().info(params.toString());
        if (params.length >= 3) {
            name = params[2];
            plugin.getLogger().info(name);
            if (plugin.slotMachineManager.hasSlotMachine(name)) {
                p.sendMessage(plugin.messageManager.slot_machine_already_existent());
                return;
            }
        } else
            name = sc.generateName();
        try {
            plugin.slotMachineManager.register(sc,SlotMachine.create(plugin, name, d, p, sc.getSLOT_NAME()));
        } catch (Exception e) {
            p.sendMessage(plugin.messageManager.slot_machine_building_failure(e.getMessage()));
            if (Settings.isDebugModeEnabled())
                e.printStackTrace();
            return;
        }
        p.sendMessage(plugin.messageManager.slot_machine_building_success(name));
    }
}