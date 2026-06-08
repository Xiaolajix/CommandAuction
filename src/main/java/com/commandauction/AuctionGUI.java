package com.commandauction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AuctionGUI {
    private static final int ITEMS_PER_PAGE = 7;
    
    public static void openGUI(Player player, CommandAuction plugin, Auction selectedAuction) {
        openGUI(player, plugin, selectedAuction, 0);
    }
    
    public static void openGUI(Player player, CommandAuction plugin, Auction selectedAuction, int page) {
        List<Auction> auctions = plugin.getAuctionManager().getAuctions();
        
        if (auctions.isEmpty()) {
            player.sendMessage("§c当前没有正在进行的拍卖!");
            return;
        }

        int totalPages = (int) Math.ceil((double) auctions.size() / ITEMS_PER_PAGE);
        if (page < 0) page = 0;
        if (page >= totalPages) page = Math.max(0, totalPages - 1);

        Inventory gui = Bukkit.createInventory(null, 54, "§6拍卖界面 - 第" + (page + 1) + "/" + totalPages + "页");

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, auctions.size());
        
        int slot = 19;
        for (int i = startIndex; i < endIndex; i++) {
            Auction auction = auctions.get(i);
            
            ItemStack displayItem = auction.getDisplayItem().clone();
            ItemMeta displayMeta = displayItem.getItemMeta();
            if (displayMeta != null) {
                displayMeta.setDisplayName(auction.getDisplayName());
                
                List<String> newLore = new ArrayList<>();
                if (auction.getLore() != null) {
                    newLore.addAll(auction.getLore());
                }
                newLore.addAll(Arrays.asList(
                        "",
                        "§7起拍价: §6" + plugin.getEconomyManager().formatMoney(auction.getStartPrice(), auction.getEconomyType()),
                        "§7一口价: §6" + plugin.getEconomyManager().formatMoney(auction.getBuyNowPrice(), auction.getEconomyType()),
                        "§7当前出价: §6" + plugin.getEconomyManager().formatMoney(auction.getCurrentBid(), auction.getEconomyType()),
                        "§7剩余时间: §6" + auction.getTimeLeft() + "秒"
                ));
                displayMeta.setLore(newLore);
                
                if (selectedAuction != null && selectedAuction.equals(auction)) {
                    displayMeta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                    displayMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                
                displayItem.setItemMeta(displayMeta);
            }
            
            plugin.getAuctionManager().setSlotAuction(slot, auction);
            gui.setItem(slot, displayItem);
            
            slot++;
        }

        if (selectedAuction != null) {
            ItemStack displayItem = selectedAuction.getDisplayItem().clone();
            ItemMeta displayMeta = displayItem.getItemMeta();
            if (displayMeta != null) {
                displayMeta.setDisplayName(selectedAuction.getDisplayName());
                displayMeta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                displayMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                
                List<String> newLore = new ArrayList<>();
                if (selectedAuction.getLore() != null) {
                    newLore.addAll(selectedAuction.getLore());
                }
                newLore.addAll(Arrays.asList(
                        "",
                        "§7起拍价: §6" + plugin.getEconomyManager().formatMoney(selectedAuction.getStartPrice(), selectedAuction.getEconomyType()),
                        "§7一口价: §6" + plugin.getEconomyManager().formatMoney(selectedAuction.getBuyNowPrice(), selectedAuction.getEconomyType()),
                        "§7当前出价: §6" + plugin.getEconomyManager().formatMoney(selectedAuction.getCurrentBid(), selectedAuction.getEconomyType()),
                        "§7剩余时间: §6" + selectedAuction.getTimeLeft() + "秒"
                ));
                displayMeta.setLore(newLore);
                displayItem.setItemMeta(displayMeta);
            }
            gui.setItem(4, displayItem);

            ItemStack bidItem = new ItemStack(Material.GOLD_INGOT);
            ItemMeta bidMeta = bidItem.getItemMeta();
            bidMeta.setDisplayName("§e出价");
            bidMeta.setLore(Arrays.asList("§7点击参与竞拍"));
            bidItem.setItemMeta(bidMeta);
            gui.setItem(38, bidItem);

            ItemStack buyNowItem = new ItemStack(Material.DIAMOND);
            ItemMeta buyNowMeta = buyNowItem.getItemMeta();
            buyNowMeta.setDisplayName("§e一口价购买");
            buyNowMeta.setLore(Arrays.asList("§7点击以一口价购买"));
            buyNowItem.setItemMeta(buyNowMeta);
            gui.setItem(42, buyNowItem);
        }

        if (totalPages > 1) {
            if (page > 0) {
                ItemStack prevPage = new ItemStack(Material.ARROW);
                ItemMeta prevMeta = prevPage.getItemMeta();
                prevMeta.setDisplayName("§e上一页");
                prevMeta.setLore(Arrays.asList("§7点击查看上一页"));
                prevPage.setItemMeta(prevMeta);
                gui.setItem(45, prevPage);
            }
            
            if (page < totalPages - 1) {
                ItemStack nextPage = new ItemStack(Material.ARROW);
                ItemMeta nextMeta = nextPage.getItemMeta();
                nextMeta.setDisplayName("§e下一页");
                nextMeta.setLore(Arrays.asList("§7点击查看下一页"));
                nextPage.setItemMeta(nextMeta);
                gui.setItem(53, nextPage);
            }
        }

        player.openInventory(gui);
    }

    public static void openGUI(Player player, CommandAuction plugin) {
        openGUI(player, plugin, null, 0);
    }
    
    public static int getItemsPerPage() {
        return ITEMS_PER_PAGE;
    }
}