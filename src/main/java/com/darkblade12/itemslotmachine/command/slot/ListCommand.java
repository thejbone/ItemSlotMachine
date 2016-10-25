package com.darkblade12.itemslotmachine.command.slot;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.command.CommandDetails;
import com.darkblade12.itemslotmachine.command.ICommand;
import org.bukkit.command.CommandSender;

@CommandDetails(name = "list", permission = "ItemSlotMachine.slot.list")
public final class ListCommand implements ICommand {
    @Override
    public void execute(ItemSlotMachine plugin, CommandSender sender, String label, String[] params) {
        if (plugin.slotMachineManager.getSlotMachineAmount() == 0) {
            sender.sendMessage(plugin.messageManager.slot_machine_list_empty());
            return;
        }
        sender.sendMessage(plugin.messageManager.slot_machine_list());
    }
}