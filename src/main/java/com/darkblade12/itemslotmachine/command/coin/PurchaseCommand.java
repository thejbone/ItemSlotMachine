package com.darkblade12.itemslotmachine.command.coin;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.coin.CoinConfig;
import com.darkblade12.itemslotmachine.coin.CoinManager;
import com.darkblade12.itemslotmachine.command.CommandDetails;
import com.darkblade12.itemslotmachine.command.ICommand;
import com.darkblade12.itemslotmachine.hook.VaultHook;
import com.darkblade12.itemslotmachine.item.ItemList;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandDetails(name = "purchase", params = "<type> <amount>", executableAsConsole = false, permission = "ItemSlotMachine.coin.purchase")
public final class PurchaseCommand implements ICommand {
    @SuppressWarnings("deprecation")
    @Override
    public void execute(ItemSlotMachine plugin, CommandSender sender, String label, String[] params) {
        Player p = (Player) sender;
        if (!VaultHook.isEnabled()) {
            p.sendMessage(plugin.messageManager.coin_purchase_disabled());
            return;
        }
        String coinType = params[0];
        if(plugin.coinManager.isCoin(coinType)){
            String coinAmt = params[1];
            int amount;
            try {
                amount = Integer.parseInt(coinAmt);
            } catch (Exception e) {
                p.sendMessage(plugin.messageManager.input_not_numeric(coinAmt));
                return;
            }
            if (amount < 1) {
                p.sendMessage(plugin.messageManager.invalid_amount(plugin.messageManager.lower_than_number(1)));
                return;
            }
            double price = plugin.coinManager.calculatePrice(coinType,amount);
            if (VaultHook.getBalance(p) < price) {
                p.sendMessage(plugin.messageManager.coin_purchase_not_enough_money(amount, price));
                return;
            }
            ItemStack i = plugin.coinManager.getCoin(coinType, amount);
            if (!ItemList.hasEnoughSpace(p, i)) {
                p.sendMessage(plugin.messageManager.player_not_enough_space());
                return;
            }
            VaultHook.ECONOMY.withdrawPlayer(p.getName(), price);
            p.getInventory().addItem(i);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.messageManager.coin_purchase(plugin.coinManager.getCoin(coinType), amount, price)));
        }
    }
}