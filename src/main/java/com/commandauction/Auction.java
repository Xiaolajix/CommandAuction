package com.commandauction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Auction {
    private final UUID uniqueId;
    private final Player starter;
    private final double startPrice;
    private final double buyNowPrice;
    private final int duration;
    private final String command;
    private final String economyType;
    
    private double currentBid;
    private UUID highestBidder;
    
    private ItemStack displayItem;
    private String displayName;
    private List<String> lore;
    
    private int timeLeft;

    public Auction(Player starter, double startPrice, double buyNowPrice, int duration, String command, String economyType) {
        this.uniqueId = UUID.randomUUID();
        this.starter = starter;
        this.startPrice = startPrice;
        this.buyNowPrice = buyNowPrice;
        this.duration = duration;
        this.command = command;
        this.economyType = economyType;
        this.currentBid = 0;
        this.highestBidder = null;
        this.timeLeft = duration;
        
        setDefaultDisplayItem();
    }

    public Auction(Player starter, double startPrice, double buyNowPrice, int duration, String command, String economyType, 
                   Material displayMaterial, String displayName, List<String> lore) {
        this.uniqueId = UUID.randomUUID();
        this.starter = starter;
        this.startPrice = startPrice;
        this.buyNowPrice = buyNowPrice;
        this.duration = duration;
        this.command = command;
        this.economyType = economyType;
        this.currentBid = 0;
        this.highestBidder = null;
        this.timeLeft = duration;
        
        setDisplayItem(displayMaterial, displayName, lore);
    }

    private void setDefaultDisplayItem() {
        this.displayItem = new ItemStack(Material.CHEST);
        this.displayName = "§e神秘宝箱";
        this.lore = new ArrayList<>();
        this.lore.add("§7这是一个神秘的拍卖物品");
        this.lore.add("§7点击参与竞拍");
    }

    private void setDisplayItem(Material material, String name, List<String> lore) {
        this.displayItem = new ItemStack(material);
        ItemMeta meta = this.displayItem.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        this.displayItem.setItemMeta(meta);
        
        this.displayName = name;
        this.lore = lore;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public Player getStarter() {
        return starter;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getBuyNowPrice() {
        return buyNowPrice;
    }

    public int getDuration() {
        return duration;
    }

    public String getCommand() {
        return command;
    }

    public double getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(double currentBid) {
        this.currentBid = currentBid;
    }

    public UUID getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(UUID highestBidder) {
        this.highestBidder = highestBidder;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getEconomyType() {
        return economyType;
    }
    
    public int getTimeLeft() {
        return timeLeft;
    }
    
    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }
}