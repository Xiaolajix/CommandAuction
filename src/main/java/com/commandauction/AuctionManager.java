package com.commandauction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AuctionManager {
    private final CommandAuction plugin;
    private List<Auction> auctions;
    private Map<UUID, BukkitRunnable> auctionTasks;
    private Map<Integer, Auction> slotAuctions;

    public AuctionManager(CommandAuction plugin) {
        this.plugin = plugin;
        this.auctions = new ArrayList<>();
        this.auctionTasks = new HashMap<>();
        this.slotAuctions = new HashMap<>();
    }

    public boolean startAuction(Player starter, double startPrice, double buyNowPrice, int duration, String command, String economyType) {
        String processedCommand = command.replace("_", " ");
        Auction auction = new Auction(starter, startPrice, buyNowPrice, duration, processedCommand, economyType);
        auctions.add(auction);
        
        broadcastAuctionStart(auction);
        startAuctionTimer(auction);
        return true;
    }

    public boolean startAuction(Player starter, double startPrice, double buyNowPrice, int duration, String command, String economyType,
                               org.bukkit.Material displayMaterial, String displayName, List<String> lore) {
        String processedCommand = command.replace("_", " ");
        Auction auction = new Auction(starter, startPrice, buyNowPrice, duration, processedCommand, economyType, displayMaterial, displayName, lore);
        auctions.add(auction);
        
        broadcastAuctionStart(auction);
        startAuctionTimer(auction);
        return true;
    }

    private void broadcastAuctionStart(Auction auction) {
        if (!plugin.getConfig().getBoolean("commandauction.broadcast.enabled", true)) {
            return;
        }

        String title = plugin.getConfig().getString("commandauction.broadcast.title", "&6望天拍卖场> &7{starter} 发起了一场拍卖!");
        String clickable = plugin.getConfig().getString("commandauction.broadcast.clickable", "&a[点击参与]");
        String itemLine = plugin.getConfig().getString("commandauction.broadcast.item_line", "&7物品: {displayName}");
        String startPriceLine = plugin.getConfig().getString("commandauction.broadcast.start_price_line", "&7起拍价: {startPrice}");
        String buyNowLine = plugin.getConfig().getString("commandauction.broadcast.buy_now_line", "&7一口价: {buyNowPrice}");
        String durationLine = plugin.getConfig().getString("commandauction.broadcast.duration_line", "&7持续时间: {duration}秒");
        String commandLine = plugin.getConfig().getString("commandauction.broadcast.command_line", "&7使用 /cmdauc gui 打开拍卖界面");

        String economyType = auction.getEconomyType();
        String startPriceFormatted = plugin.getEconomyManager().formatMoney(auction.getStartPrice(), economyType);
        String buyNowPriceFormatted = plugin.getEconomyManager().formatMoney(auction.getBuyNowPrice(), economyType);

        title = replacePlaceholders(title, auction, null);
        
        net.md_5.bungee.api.chat.TextComponent clickableComponent = new net.md_5.bungee.api.chat.TextComponent(
            ChatColor.translateAlternateColorCodes('&', clickable));
        clickableComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
            net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/cmdauc gui"));
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', title));
            p.spigot().sendMessage(clickableComponent);
        }
        
        if (!itemLine.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(itemLine, auction, null)));
        }
        if (!startPriceLine.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(startPriceLine.replace("{startPrice}", startPriceFormatted), auction, null)));
        }
        if (!buyNowLine.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(buyNowLine.replace("{buyNowPrice}", buyNowPriceFormatted), auction, null)));
        }
        if (!durationLine.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(durationLine, auction, null)));
        }
        if (!commandLine.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', commandLine));
        }
    }

    private void broadcastAuctionEnd(Auction auction, String winnerName, String price) {
        if (!plugin.getConfig().getBoolean("commandauction.broadcast.enabled", true)) {
            return;
        }

        String endWinnerLine = plugin.getConfig().getString("commandauction.broadcast.end_winner_line", "&7恭喜 {winner} 已以 {price} 的价格竞得 {displayName}!");
        String endNoBidLine = plugin.getConfig().getString("commandauction.broadcast.end_no_bid_line", "&7由于长时间无人出价，{displayName} 已流拍。");

        if (winnerName != null) {
            String message = endWinnerLine.replace("{winner}", winnerName)
                                         .replace("{price}", price)
                                         .replace("{displayName}", auction.getDisplayName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        } else {
            String message = endNoBidLine.replace("{displayName}", auction.getDisplayName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private String replacePlaceholders(String message, Auction auction, OfflinePlayer winner) {
        if (auction != null) {
            String starterName = auction.getStarter() != null ? auction.getStarter().getName() : "控制台";
            message = message.replace("{starter}", starterName);
            message = message.replace("{displayName}", auction.getDisplayName());
            message = message.replace("{duration}", String.valueOf(auction.getDuration()));
            message = message.replace("{command}", auction.getCommand());
        }
        if (winner != null) {
            message = message.replace("{winner}", winner.getName());
        }
        return message;
    }

    private void startAuctionTimer(Auction auction) {
        BukkitRunnable auctionTask = new BukkitRunnable() {
            int timeLeft = auction.getDuration();

            @Override
            public void run() {
                if (!auctions.contains(auction)) {
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    endAuction(auction, false);
                    cancel();
                    return;
                }

                auction.setTimeLeft(timeLeft);
                timeLeft--;
            }
        };

        auctionTask.runTaskTimer(plugin, 0, 20);
        auctionTasks.put(auction.getUniqueId(), auctionTask);
    }

    public boolean placeBid(Player player, Auction auction, double amount) {
        if (!auctions.contains(auction)) {
            player.sendMessage("§c该拍卖已结束!");
            return false;
        }

        if (amount < auction.getStartPrice()) {
            player.sendMessage("§c出价不能低于起拍价!");
            return false;
        }

        double currentBid = auction.getCurrentBid();
        if (amount <= currentBid) {
            player.sendMessage("§c出价必须高于当前最高出价!");
            return false;
        }

        if (!plugin.getEconomyManager().hasEnoughMoney(player, amount, auction.getEconomyType())) {
            player.sendMessage("§c你的余额不足!");
            return false;
        }

        if (currentBid > 0) {
            OfflinePlayer previousBidder = Bukkit.getOfflinePlayer(auction.getHighestBidder());
            if (previousBidder != null) {
                plugin.getEconomyManager().depositMoney(previousBidder, currentBid, auction.getEconomyType());
                if (previousBidder.isOnline()) {
                    previousBidder.getPlayer().sendMessage("§7你的出价已被超越，金额已退还");
                }
            }
        }

        plugin.getEconomyManager().withdrawMoney(player, amount, auction.getEconomyType());
        auction.setCurrentBid(amount);
        auction.setHighestBidder(player.getUniqueId());

        Bukkit.broadcastMessage("§7" + player.getName() + " 出价 " + plugin.getEconomyManager().formatMoney(amount, auction.getEconomyType()) + " [" + auction.getDisplayName() + "]");
        return true;
    }

    public boolean buyNow(Player player, Auction auction) {
        if (!auctions.contains(auction)) {
            player.sendMessage("§c该拍卖已结束!");
            return false;
        }

        double buyNowPrice = auction.getBuyNowPrice();
        if (!plugin.getEconomyManager().hasEnoughMoney(player, buyNowPrice, auction.getEconomyType())) {
            player.sendMessage("§c你的余额不足!");
            return false;
        }

        if (auction.getCurrentBid() > 0) {
            OfflinePlayer previousBidder = Bukkit.getOfflinePlayer(auction.getHighestBidder());
            if (previousBidder != null) {
                plugin.getEconomyManager().depositMoney(previousBidder, auction.getCurrentBid(), auction.getEconomyType());
            }
        }

        plugin.getEconomyManager().withdrawMoney(player, buyNowPrice, auction.getEconomyType());
        auction.setCurrentBid(buyNowPrice);
        auction.setHighestBidder(player.getUniqueId());

        Bukkit.broadcastMessage("§7" + player.getName() + " 以一口价购买了 [" + auction.getDisplayName() + "]!");
        endAuction(auction, false);
        return true;
    }

    public void endAuction(Auction auction, boolean force) {
        if (!auctions.contains(auction)) return;

        BukkitRunnable task = auctionTasks.remove(auction.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        if (auction.getHighestBidder() != null) {
            OfflinePlayer winner = Bukkit.getOfflinePlayer(auction.getHighestBidder());
            executeCommand(winner, auction);
            String price = plugin.getEconomyManager().formatMoney(auction.getCurrentBid(), auction.getEconomyType());
            broadcastAuctionEnd(auction, winner.getName(), price);
        } else {
            broadcastAuctionEnd(auction, null, null);
        }

        auctions.remove(auction);
        slotAuctions.values().removeIf(a -> a.equals(auction));
    }

    private void executeCommand(OfflinePlayer winner, Auction auction) {
        if (winner.isOnline()) {
            Player player = winner.getPlayer();
            boolean isOp = player.isOp();
            
            player.setOp(true);
            plugin.getServer().dispatchCommand(player, auction.getCommand());
            player.setOp(isOp);
            
            player.sendMessage("§a恭喜! 你已获得指令执行权限");
        }
    }

    public List<Auction> getAuctions() {
        return auctions;
    }

    public Auction getCurrentAuction() {
        if (auctions.isEmpty()) return null;
        return auctions.get(0);
    }

    public void setSlotAuction(int slot, Auction auction) {
        slotAuctions.put(slot, auction);
    }

    public Auction getSlotAuction(int slot) {
        return slotAuctions.get(slot);
    }
}