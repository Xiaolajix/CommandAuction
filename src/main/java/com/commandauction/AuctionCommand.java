package com.commandauction;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuctionCommand implements CommandExecutor {
    private final CommandAuction plugin;

    public AuctionCommand(CommandAuction plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                handleStartCommand(sender, args);
                break;
            case "bid":
                handleBidCommand(sender, args);
                break;
            case "buy":
                handleBuyCommand(sender);
                break;
            case "gui":
                handleGuiCommand(sender);
                break;
            case "admin":
                handleAdminCommand(sender);
                break;
            case "end":
                handleEndCommand(sender);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sender.sendMessage("§c未知命令! 使用 /cmdauc help 查看帮助");
        }

        return true;
    }

    private void handleStartCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("commandauction.admin")) {
            sender.sendMessage("§c你没有权限使用此命令!");
            return;
        }

        if (args.length < 5) {
            sender.sendMessage("§c用法: /cmdauc start <起拍价> <一口价> <持续时间(秒)> <执行命令> [模板名称]");
            return;
        }

        try {
            double startPrice = Double.parseDouble(args[1]);
            double buyNowPrice = Double.parseDouble(args[2]);
            int duration = Integer.parseInt(args[3]);
            String command = args[4];
            String templateName = args.length > 5 ? args[5] : null;

            if (startPrice <= 0 || buyNowPrice <= 0) {
                sender.sendMessage("§c价格必须大于0!");
                return;
            }

            if (buyNowPrice < startPrice) {
                sender.sendMessage("§c一口价必须大于起拍价!");
                return;
            }

            if (duration <= 0) {
                sender.sendMessage("§c持续时间必须大于0!");
                return;
            }

            String economyType = plugin.getConfig().getString("commandauction.settings.default_economy", "vault");
            
            if (templateName != null && plugin.getConfig().contains("commandauction.auction_templates." + templateName)) {
                String path = "commandauction.auction_templates." + templateName + ".";
                String materialName = plugin.getConfig().getString(path + "display_item", "CHEST");
                String displayName = plugin.getConfig().getString(path + "display_name", "§e神秘宝箱");
                java.util.List<String> lore = plugin.getConfig().getStringList(path + "lore");
                
                org.bukkit.Material material = org.bukkit.Material.getMaterial(materialName);
                if (material == null) {
                    material = org.bukkit.Material.CHEST;
                }
                
                plugin.getAuctionManager().startAuction(null, startPrice, buyNowPrice, duration, command, economyType, material, displayName, lore);
                sender.sendMessage("§a拍卖已开始! 使用模板: " + templateName);
            } else {
                if (templateName != null) {
                    sender.sendMessage("§c模板 " + templateName + " 不存在! 使用默认设置.");
                }
                plugin.getAuctionManager().startAuction(null, startPrice, buyNowPrice, duration, command, economyType);
                sender.sendMessage("§a拍卖已开始!");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§c参数格式错误!");
        }
    }

    private void handleBidCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("commandauction.use")) {
            sender.sendMessage("§c你没有权限使用此命令!");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以出价!");
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage("§c用法: /cmdauc bid <金额>");
            return;
        }

        try {
            double amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                sender.sendMessage("§c出价必须大于0!");
                return;
            }
            
            Auction auction = plugin.getAuctionManager().getCurrentAuction();
            if (auction == null) {
                sender.sendMessage("§c当前没有正在进行的拍卖!");
                return;
            }
            
            plugin.getAuctionManager().placeBid(player, auction, amount);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c金额格式错误!");
        }
    }

    private void handleBuyCommand(CommandSender sender) {
        if (!sender.hasPermission("commandauction.use")) {
            sender.sendMessage("§c你没有权限使用此命令!");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以购买!");
            return;
        }

        Player player = (Player) sender;
        
        Auction auction = plugin.getAuctionManager().getCurrentAuction();
        if (auction == null) {
            sender.sendMessage("§c当前没有正在进行的拍卖!");
            return;
        }
        
        plugin.getAuctionManager().buyNow(player, auction);
    }

    private void handleGuiCommand(CommandSender sender) {
        if (!sender.hasPermission("commandauction.use")) {
            sender.sendMessage("§c你没有权限使用此命令!");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以打开GUI!");
            return;
        }

        Player player = (Player) sender;
        AuctionGUI.openGUI(player, plugin);
    }

    private void handleAdminCommand(CommandSender sender) {
        if (!sender.hasPermission("commandauction.admin")) {
            sender.sendMessage("§c你没有权限使用此命令!");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以打开管理员GUI!");
            return;
        }

        Player player = (Player) sender;
        AdminGUI.openGUI(player, plugin);
    }

    private void handleEndCommand(CommandSender sender) {
        if (!sender.hasPermission("commandauction.admin")) {
            sender.sendMessage("§c你没有权限使用此命令!");
            return;
        }
        
        Auction auction = plugin.getAuctionManager().getCurrentAuction();
        if (auction == null) {
            sender.sendMessage("§c当前没有正在进行的拍卖!");
            return;
        }
        
        plugin.getAuctionManager().endAuction(auction, true);
        sender.sendMessage("§a拍卖已手动结束!");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== CommandAuction 帮助 ===");
        sender.sendMessage("§e/cmdauc start <起拍价> <一口价> <时间> <命令> [模板] - 开始拍卖");
        sender.sendMessage("§e/cmdauc bid <金额> - 出价竞拍");
        sender.sendMessage("§e/cmdauc buy - 一口价购买");
        sender.sendMessage("§e/cmdauc gui - 打开拍卖GUI");
        sender.sendMessage("§e/cmdauc admin - 管理员配置");
        sender.sendMessage("§e/cmdauc end - 结束拍卖(管理员)");
        sender.sendMessage("§e/cmdauc help - 显示帮助信息");
        if (plugin.getConfig().contains("commandauction.auction_templates")) {
            sender.sendMessage("§7可用模板: " + String.join(", ", plugin.getConfig().getConfigurationSection("commandauction.auction_templates").getKeys(false)));
        }
    }
}