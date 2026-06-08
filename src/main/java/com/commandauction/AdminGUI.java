package com.commandauction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminGUI {
    private static final Map<Player, AuctionConfig> configMap = new HashMap<>();

    public static void openGUI(Player player, CommandAuction plugin) {
        if (!player.hasPermission("commandauction.admin")) {
            player.sendMessage("§c你没有权限使用此功能!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, "§6管理员拍卖配置");

        ItemStack startPriceItem = createItem(Material.GOLD_NUGGET, "§e起拍价", Arrays.asList("§7点击设置起拍价"));
        gui.setItem(10, startPriceItem);

        ItemStack buyNowPriceItem = createItem(Material.DIAMOND, "§e一口价", Arrays.asList("§7点击设置一口价"));
        gui.setItem(11, buyNowPriceItem);

        ItemStack durationItem = createItem(Material.WATCH, "§e持续时间", Arrays.asList("§7点击设置持续时间(秒)"));
        gui.setItem(12, durationItem);

        ItemStack economyItem = createItem(Material.PAPER, "§e经济类型", Arrays.asList("§7点击选择经济类型", "§7当前: §a" + plugin.getConfig().getString("settings.default_economy", "vault")));
        gui.setItem(13, economyItem);

        ItemStack commandItem = createItem(Material.BOOK, "§e执行命令", Arrays.asList("§7点击设置执行命令"));
        gui.setItem(14, commandItem);

        ItemStack displayItemItem = createItem(Material.CHEST, "§e显示物品", Arrays.asList("§7点击设置显示物品"));
        gui.setItem(15, displayItemItem);

        ItemStack displayNameItem = createItem(Material.NAME_TAG, "§e物品名称", Arrays.asList("§7点击设置物品名称"));
        gui.setItem(16, displayNameItem);

        ItemStack loreItem = createItem(Material.BOOK, "§e物品描述", Arrays.asList("§7点击设置物品描述"));
        gui.setItem(19, loreItem);

        ItemStack startAuctionItem = createItem(Material.EMERALD_BLOCK, "§a开始拍卖", Arrays.asList("§7点击开始拍卖"));
        gui.setItem(40, startAuctionItem);

        ItemStack templateItem = createItem(Material.BOOKSHELF, "§e使用模板", Arrays.asList("§7点击使用预设模板"));
        gui.setItem(31, templateItem);

        ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        
        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null || gui.getItem(i).getType() == Material.AIR) {
                gui.setItem(i, glassPane);
            }
        }

        configMap.put(player, new AuctionConfig());
        player.openInventory(gui);
    }

    private static ItemStack createItem(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void handleClick(Player player, CommandAuction plugin, ItemStack clickedItem) {
        AuctionConfig config = configMap.get(player);
        if (config == null) {
            config = new AuctionConfig();
            configMap.put(player, config);
        }

        switch (clickedItem.getType()) {
            case GOLD_NUGGET:
                player.closeInventory();
                player.sendMessage("§e请输入起拍价:");
                config.setEditingField("startPrice");
                break;
            case DIAMOND:
                player.closeInventory();
                player.sendMessage("§e请输入一口价:");
                config.setEditingField("buyNowPrice");
                break;
            case WATCH:
                player.closeInventory();
                player.sendMessage("§e请输入持续时间(秒):");
                config.setEditingField("duration");
                break;
            case PAPER:
                player.closeInventory();
                player.sendMessage("§e请输入经济类型 (vault/playerpoints):");
                config.setEditingField("economyType");
                break;
            case COMMAND:
                player.closeInventory();
                player.sendMessage("§e请输入执行命令 (不带/):");
                config.setEditingField("command");
                break;
            case CHEST:
                player.closeInventory();
                player.sendMessage("§e请输入显示物品名称 (如: DIAMOND, GOLD_INGOT):");
                config.setEditingField("displayItem");
                break;
            case NAME_TAG:
                player.closeInventory();
                player.sendMessage("§e请输入物品名称:");
                config.setEditingField("displayName");
                break;
            case BOOK:
                player.closeInventory();
                player.sendMessage("§e请输入物品描述 (用|分隔多行):");
                config.setEditingField("lore");
                break;
            case EMERALD_BLOCK:
                startAuction(player, plugin, config);
                break;
            case BOOKSHELF:
                openTemplateGUI(player, plugin);
                break;
        }
    }

    private static void startAuction(Player player, CommandAuction plugin, AuctionConfig config) {
        if (!config.isValid()) {
            player.sendMessage("§c请先完成所有配置!");
            return;
        }

        Material material = Material.matchMaterial(config.getDisplayItem());
        if (material == null) {
            material = Material.CHEST;
        }

        plugin.getAuctionManager().startAuction(
                player,
                config.getStartPrice(),
                config.getBuyNowPrice(),
                config.getDuration(),
                config.getCommand(),
                config.getEconomyType(),
                material,
                config.getDisplayName(),
                config.getLore()
        );

        player.closeInventory();
        configMap.remove(player);
    }

    private static void openTemplateGUI(Player player, CommandAuction plugin) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6选择模板");

        java.util.Set<String> templates = plugin.getConfig().getConfigurationSection("auction_templates").getKeys(false);
        int slot = 0;
        for (String templateName : templates) {
            ItemStack templateItem = createItem(Material.PAPER, "§e" + templateName, Arrays.asList("§7点击使用此模板"));
            gui.setItem(slot++, templateItem);
        }

        ItemStack backItem = createItem(Material.ARROW, "§c返回", Arrays.asList("§7点击返回"));
        gui.setItem(26, backItem);

        player.openInventory(gui);
    }

    public static void useTemplate(Player player, CommandAuction plugin, String templateName) {
        AuctionConfig config = configMap.get(player);
        if (config == null) {
            config = new AuctionConfig();
            configMap.put(player, config);
        }

        String path = "auction_templates." + templateName + ".";
        String displayItem = plugin.getConfig().getString(path + "display_item", "CHEST");
        String displayName = plugin.getConfig().getString(path + "display_name", "§e神秘宝箱");
        java.util.List<String> lore = plugin.getConfig().getStringList(path + "lore");

        config.setDisplayItem(displayItem);
        config.setDisplayName(displayName);
        config.setLore(lore);

        player.sendMessage("§a已应用模板: " + templateName);
        openGUI(player, plugin);
    }

    public static Map<Player, AuctionConfig> getConfigMap() {
        return configMap;
    }

    public static class AuctionConfig {
        private double startPrice = 0;
        private double buyNowPrice = 0;
        private int duration = 300;
        private String economyType = "vault";
        private String command = "";
        private String displayItem = "CHEST";
        private String displayName = "§e神秘宝箱";
        private java.util.List<String> lore = Arrays.asList("§7这是一个神秘的拍卖物品");
        private String editingField;

        public boolean isValid() {
            return startPrice > 0 && buyNowPrice > 0 && duration > 0 && !command.isEmpty();
        }

        public double getStartPrice() {
            return startPrice;
        }

        public void setStartPrice(double startPrice) {
            this.startPrice = startPrice;
        }

        public double getBuyNowPrice() {
            return buyNowPrice;
        }

        public void setBuyNowPrice(double buyNowPrice) {
            this.buyNowPrice = buyNowPrice;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getEconomyType() {
            return economyType;
        }

        public void setEconomyType(String economyType) {
            this.economyType = economyType;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getDisplayItem() {
            return displayItem;
        }

        public void setDisplayItem(String displayItem) {
            this.displayItem = displayItem;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public java.util.List<String> getLore() {
            return lore;
        }

        public void setLore(java.util.List<String> lore) {
            this.lore = lore;
        }

        public String getEditingField() {
            return editingField;
        }

        public void setEditingField(String editingField) {
            this.editingField = editingField;
        }
    }
}