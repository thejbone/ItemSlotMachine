package com.darkblade12.itemslotmachine.command;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import org.bukkit.command.CommandSender;

public abstract interface ICommand {
    public abstract void execute(ItemSlotMachine plugin, CommandSender sender, String label, String[] params);
}