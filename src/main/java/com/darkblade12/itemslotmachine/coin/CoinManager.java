package com.darkblade12.itemslotmachine.coin;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.manager.Manager;
import com.darkblade12.itemslotmachine.safe.SafeLocation;
import com.darkblade12.itemslotmachine.settings.Settings;
import com.darkblade12.itemslotmachine.sign.SignUpdater;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
public final class CoinManager extends Manager {
    private HashMap<String, CoinConfig> coins;
    private HashMap<String, SafeLocation> lastShop;
    private HashMap<String, Integer> shopCoins;
    private BukkitTask task;

    public CoinManager(ItemSlotMachine plugin) {
        super(plugin);
        onInitialize();
    }

    @Override
    public boolean onInitialize() {
        coins = new HashMap<>();
        lastShop = new HashMap<>();
        shopCoins = new HashMap<>();
        coins.putAll(Settings.getCoinConfigs());
//        task = new BukkitRunnable() {
//            @Override
//            public void run() {
//                for (Entry<String, SafeLocation> e : lastShop.entrySet()) {
//                    String name = e.getKey();
//                    SafeLocation s = e.getValue();
//                    Player p = Bukkit.getPlayerExact(name);
//                    if (p == null) {
//                        resetShop(name);
//                    } else {
//                        Location l = p.getLocation();
//                        coins.forEach((a,b) -> {
//                            if (!s.getWorldName().equals(l.getWorld().getName()) || s.distanceSquared(l) > 64) {
//                                updateShop(p, s.getBukkitLocation(), 1, b);
//                                resetShop(name);
//                            }
//                        });
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 10, 10);
        registerEvents();
        return true;
    }

    @Override
    public void onDisable() {
//        task.cancel();
        unregisterAll();
    }

    public double calculatePrice(int price, int amt) {
        return amt * price;
    }
    public double calculatePrice(String coin, int amt) {
        return amt * coins.get(coin).getCOIN_PRICE();
    }

    private void updateShop(Player p, Location l, int coins, CoinConfig coin) {
        String name = p.getName();
        lastShop.put(name, SafeLocation.fromBukkitLocation(l));
        shopCoins.put(name, coins);
        SignUpdater.updateSign(p, l, new String[]
                {
                        plugin.messageManager.sign_coin_shop_header(),
                        coin.getCOIN_DISPLAY(),
                        plugin.messageManager.sign_coin_shop_coins(coins),
                        plugin.messageManager.sign_coin_shop_price(calculatePrice(coin.getCOIN_PRICE(), coins))

                }, 2);
    }

    private void resetLastShop(String name) {
        lastShop.remove(name);
    }

    private void resetShopCoins(String name) {
        lastShop.remove(name);
    }

    private void resetShopCoins(Player p) {
        resetShopCoins(p.getName());
    }

    private void resetShop(String name) {
        resetLastShop(name);
        resetShopCoins(name);
    }

    public ItemStack getCoin(String name, int amount) {
        ItemStack i = coins.get(name).getCOIN_ITEM().clone();
        i.setAmount(amount);
        return i;
    }

    public boolean isCoin(ItemStack i) {
        AtomicBoolean similar = new AtomicBoolean(false);
        coins.forEach((a,b) ->
        {
            if(i.isSimilar(b.getCOIN_ITEM())){
                similar.set(true);
            }
        });
        return similar.get();
    }
    public boolean isCoin(String s) {
        for(Map.Entry<String, CoinConfig> c : coins.entrySet()){
            if(s.contains(c.getValue().getCOIN_DISPLAY())){
                return true;
            }
        }
        return coins.containsKey(s);
    }

    private SafeLocation getLastShop(Player p) {
        String name = p.getName();
        return lastShop.getOrDefault(name, null);
    }

    private int getShopCoins(Player p) {
        String name = p.getName();
        return shopCoins.getOrDefault(name, 1);
    }

    private boolean isShop(Sign s) {
        return s.getLine(0).equals(plugin.messageManager.sign_coin_shop_header());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase("[CoinShop]")) {
            if(isCoin(ChatColor.stripColor(event.getLine(1)))){
                CoinConfig coin = coins.get(ChatColor.stripColor(event.getLine(1)));
                String[] lines = SignUpdater.validateLines(
                        new String[]{
                                plugin.messageManager.sign_coin_shop_header(),
                                ChatColor.translateAlternateColorCodes('&',
                                        coin.getCOIN_DISPLAY()),
                                plugin.messageManager.sign_coin_shop_coins(1),
                                plugin.messageManager.sign_coin_shop_price(coin.getCOIN_PRICE())
                        }, 2);
                event.setLine(0, lines[0]);
                event.setLine(1, lines[1]);
                event.setLine(2, lines[2]);
                event.setLine(3, lines[3]);
            }
        }
    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
//        int previous = event.getPreviousSlot();
//        int next = event.getNewSlot();
//        if (next != previous) {
//            Player p = event.getPlayer();
//            Sign s;
//            String coinType;
//            try {
//                s = (Sign) p.getTargetBlock((Set<Material>) null, 6).getState();
//                if (!isShop(s))
//                    return;
//            } catch (Exception e) {
//                return;
//            }
//            coinType = s.getLine(1);
//            CoinConfig coin = this.coins.get(coinType);
//            Location l = s.getLocation();
//            SafeLocation last = getLastShop(p);
//            if (last != null && !last.noDistance(l)) {
//                updateShop(p, last.getBukkitLocation(), 1, coin);
//                resetShopCoins(p);
//            }
//            int coins2 = getShopCoins(p);
//            int amount = p.isSneaking() ? 10 : 1;
//            int i = coins2 + amount <= 100 ? amount : 100 - coins2;
//            if (next > previous)
//                coins2 += next == 8 && previous == 0 ? i : coins2 - amount > 0 ? -amount : -coins2 + 1;
//            else
//                coins2 += next == 0 && previous == 8 ? coins2 - amount > 0 ? -amount : -coins2 + 1 : i;
//            updateShop(p, l, coins2, coin);
//        }
//    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Sign s;
            try {
                s = (Sign) event.getClickedBlock().getState();
            } catch (Exception e) {
                return;
            }
            if (isShop(s)) {
                String coinType = ChatColor.stripColor(s.getLine(1));
                event.setCancelled(true);
                Player p = event.getPlayer();
                plugin.coinCommandHandler.getCommand("purchase").execute(plugin, p, "coin", new String[]{getCoinName(coinType),Integer.toString(getShopCoins(p))});
                p.updateInventory();
            }
        }
    }

    private String getCoinName(String line) {
        for(Map.Entry<String, CoinConfig> c : coins.entrySet()){
            if(c.getValue().getCOIN_DISPLAY().contains(line)){
                return c.getValue().getCOIN_NAME();
            }
        }
        return "";
    }

    public void addCoin(String name, CoinConfig newCoin) {
        coins.put(name,newCoin);
    }

    public String getCoin(String coinType) {
        return this.coins.get(coinType).getCOIN_DISPLAY();
    }
}