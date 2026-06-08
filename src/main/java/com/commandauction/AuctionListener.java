package com.commandauction;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

public class AuctionListener implements Listener {
    private final CommandAuction plugin;
    private final Map<Player, Double> pendingBids = new HashMap<>();
    private final Map<Player, Auction> pendingAuctions = new HashMap<>();
    private final Map<Player, Auction> selectedAuctions = new HashMap<>();

    public AuctionListener(CommandAuction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (pendingBids.containsKey(player)) {
            event.setCancelled(true);
            
            String message = event.getMessage();
            Auction auction = pendingAuctions.get(player);
            double minBid = pendingBids.get(player);
            
            try {
                double amount = Double.parseDouble(message);
                
                if (amount < minBid) {
                    player.sendMessage("§c出价不能低于最低出价! 最低出价: " + plugin.getEconomyManager().formatMoney(minBid, auction.getEconomyType()));
                    return;
                }
                
                boolean success = plugin.getAuctionManager().placeBid(player, auction, amount);
                if (success) {
                    player.sendMessage("§a出价成功! 出价: " + plugin.getEconomyManager().formatMoney(amount, auction.getEconomyType()));
                    AuctionGUI.openGUI(player, plugin, auction);
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§c请输入有效的数字!");
                return;
            } finally {
                pendingBids.remove(player);
                pendingAuctions.remove(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getTitle() == null) return;
        
        if (inventory.getTitle().startsWith("§6拍卖界面")) {
            handleAuctionClick(event, inventory.getTitle());
        } else if (inventory.getTitle().equals("§6管理员拍卖配置")) {
            handleAdminGUIClick(event);
        } else if (inventory.getTitle().equals("§6选择模板")) {
            handleTemplateGUIClick(event);
        }
    }

    private void handleAuctionClick(InventoryClickEvent event, String title) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();
        Material type = event.getCurrentItem().getType();
        
        int currentPage = 0;
        if (title.contains("-")) {
            try {
                String pageStr = title.split("第")[1].split("/")[0];
                currentPage = Integer.parseInt(pageStr) - 1;
            } catch (Exception e) {
                currentPage = 0;
            }
        }

        if (slot >= 19 && slot <= 25) {
            Auction auction = plugin.getAuctionManager().getSlotAuction(slot);
            if (auction != null) {
                selectedAuctions.put(player, auction);
                AuctionGUI.openGUI(player, plugin, auction, currentPage);
            }
        } else if (type == Material.GOLD_INGOT) {
            Auction auction = selectedAuctions.get(player);
            if (auction != null) {
                handleBidClick(player, auction);
            } else {
                player.sendMessage("§c请先选择一个拍卖物品!");
            }
        } else if (type == Material.DIAMOND) {
            Auction auction = selectedAuctions.get(player);
            if (auction != null) {
                handleBuyNowClick(player, auction);
            } else {
                player.sendMessage("§c请先选择一个拍卖物品!");
            }
        } else if (type == Material.ARROW) {
            Auction selectedAuction = selectedAuctions.get(player);
            if (slot == 45) {
                AuctionGUI.openGUI(player, plugin, selectedAuction, currentPage - 1);
            } else if (slot == 53) {
                AuctionGUI.openGUI(player, plugin, selectedAuction, currentPage + 1);
            }
        }
    }

    private void handleBidClick(Player player, Auction auction) {
        double minBid = auction.getCurrentBid() > 0 ? auction.getCurrentBid() * 1.1 : auction.getStartPrice();
        
        player.closeInventory();
        player.sendMessage("§e请输入你的出价 (最低: " + plugin.getEconomyManager().formatMoney(minBid, auction.getEconomyType()) + "):");
        
        pendingBids.put(player, minBid);
        pendingAuctions.put(player, auction);
    }

    private void handleBuyNowClick(Player player, Auction auction) {
        plugin.getAuctionManager().buyNow(player, auction);
        selectedAuctions.remove(player);
        player.closeInventory();
    }

    private void handleAdminGUIClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        if (!player.hasPermission("commandauction.admin")) {
            player.sendMessage("§c你没有权限使用此功能!");
            player.closeInventory();
            return;
        }

        AdminGUI.handleClick(player, plugin, event.getCurrentItem());
    }

    public Map<Player, Double> getPendingBids() {
        return pendingBids;
    }

    public Map<Player, Auction> getPendingAuctions() {
        return pendingAuctions;
    }

    private void handleTemplateGUIClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        if (event.getCurrentItem().getType() == Material.ARROW) {
            AdminGUI.openGUI(player, plugin);
            return;
        }

        if (event.getCurrentItem().getType() == Material.PAPER) {
            String templateName = event.getCurrentItem().getItemMeta().getDisplayName().replace("§e", "");
            AdminGUI.useTemplate(player, plugin, templateName);
        }
    }
}