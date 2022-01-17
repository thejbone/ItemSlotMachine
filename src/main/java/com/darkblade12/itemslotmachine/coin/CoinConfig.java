package com.darkblade12.itemslotmachine.coin;

import com.darkblade12.itemslotmachine.item.ItemFactory;
import com.darkblade12.itemslotmachine.safe.SafeLocation;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CoinConfig {
    private String COIN_NAME;
    private ItemStack COIN_ITEM;
    private int COIN_PRICE;
    private String COIN_DISPLAY;

    public CoinConfig(String name, String display, ItemStack item, int price, String lore) {
       COIN_NAME = name;
       COIN_ITEM = item;
       COIN_PRICE = price;
       COIN_DISPLAY = display;
       COIN_ITEM = ItemFactory.setNameAndLore(COIN_ITEM, COIN_DISPLAY, lore);

    }

    public int getCOIN_PRICE() {
        return COIN_PRICE;
    }

    public void setCOIN_PRICE(int COIN_PRICE) {
        this.COIN_PRICE = COIN_PRICE;
    }

    public ItemStack getCOIN_ITEM() {
        return COIN_ITEM;
    }

    public void setCOIN_ITEM(ItemStack COIN_ITEM) {
        this.COIN_ITEM = COIN_ITEM;
    }

    public String getCOIN_NAME() {
        return COIN_NAME;
    }

    public void setCOIN_NAME(String COIN_NAME) {
        this.COIN_NAME = COIN_NAME;
    }

    public boolean isCoin(ItemStack item){
        return item.isSimilar(COIN_ITEM);
    }

    public String getCOIN_DISPLAY() {
        return COIN_DISPLAY;
    }

    public void setCOIN_DISPLAY(String COIN_DISPLAY) {
        this.COIN_DISPLAY = COIN_DISPLAY;
    }
}
