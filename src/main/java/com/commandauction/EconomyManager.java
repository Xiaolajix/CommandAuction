package com.commandauction;

import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final CommandAuction plugin;
    private Object economy;
    private boolean playerPointsEnabled = false;
    private Object playerPointsAPI;

    public EconomyManager(CommandAuction plugin) {
        this.plugin = plugin;
        setupEconomy();
        checkPlayerPoints();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault 插件未找到!");
            return;
        }
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = plugin.getServer().getServicesManager().getRegistration(economyClass);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Vault API 初始化失败: " + e.getMessage());
        }
    }

    private void checkPlayerPoints() {
        if (plugin.getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            playerPointsEnabled = true;
            try {
                Class<?> playerPointsClass = Class.forName("org.black_ixx.playerpoints.PlayerPoints");
                Object playerPointsPlugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
                Method getAPI = playerPointsClass.getMethod("getAPI");
                playerPointsAPI = getAPI.invoke(playerPointsPlugin);
            } catch (Exception e) {
                plugin.getLogger().warning("PlayerPoints API 初始化失败: " + e.getMessage());
                playerPointsEnabled = false;
            }
        }
    }

    public boolean hasEnoughMoney(OfflinePlayer player, double amount, String economyType) {
        if ("playerpoints".equalsIgnoreCase(economyType) && playerPointsEnabled) {
            return getPlayerPoints(player) >= (int) amount;
        }
        if (economy == null) return false;
        try {
            Method has = economy.getClass().getMethod("has", OfflinePlayer.class, double.class);
            return (Boolean) has.invoke(economy, player, amount);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean withdrawMoney(OfflinePlayer player, double amount, String economyType) {
        if ("playerpoints".equalsIgnoreCase(economyType) && playerPointsEnabled) {
            return setPlayerPoints(player, getPlayerPoints(player) - (int) amount);
        }
        if (economy == null) return false;
        try {
            Method withdrawPlayer = economy.getClass().getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            Object result = withdrawPlayer.invoke(economy, player, amount);
            Method transactionSuccess = result.getClass().getMethod("transactionSuccess");
            return (Boolean) transactionSuccess.invoke(result);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean depositMoney(OfflinePlayer player, double amount, String economyType) {
        if ("playerpoints".equalsIgnoreCase(economyType) && playerPointsEnabled) {
            return setPlayerPoints(player, getPlayerPoints(player) + (int) amount);
        }
        if (economy == null) return false;
        try {
            Method depositPlayer = economy.getClass().getMethod("depositPlayer", OfflinePlayer.class, double.class);
            Object result = depositPlayer.invoke(economy, player, amount);
            Method transactionSuccess = result.getClass().getMethod("transactionSuccess");
            return (Boolean) transactionSuccess.invoke(result);
        } catch (Exception e) {
            return false;
        }
    }

    public String formatMoney(double amount, String economyType) {
        if ("playerpoints".equalsIgnoreCase(economyType)) {
            return String.format("%.0f 点券", amount);
        }
        if (economy == null) return String.valueOf(amount);
        try {
            Method format = economy.getClass().getMethod("format", double.class);
            return (String) format.invoke(economy, amount);
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }

    private int getPlayerPoints(OfflinePlayer player) {
        if (playerPointsAPI == null) return 0;
        try {
            Method look = playerPointsAPI.getClass().getMethod("look", UUID.class);
            return (int) look.invoke(playerPointsAPI, player.getUniqueId());
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean setPlayerPoints(OfflinePlayer player, int amount) {
        if (playerPointsAPI == null) return false;
        try {
            Method set = playerPointsAPI.getClass().getMethod("set", UUID.class, int.class);
            set.invoke(playerPointsAPI, player.getUniqueId(), amount);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasEnoughMoney(OfflinePlayer player, double amount) {
        return hasEnoughMoney(player, amount, plugin.getConfig().getString("commandauction.settings.default_economy", "vault"));
    }

    public boolean withdrawMoney(OfflinePlayer player, double amount) {
        return withdrawMoney(player, amount, plugin.getConfig().getString("commandauction.settings.default_economy", "vault"));
    }

    public boolean depositMoney(OfflinePlayer player, double amount) {
        return depositMoney(player, amount, plugin.getConfig().getString("commandauction.settings.default_economy", "vault"));
    }

    public String formatMoney(double amount) {
        return formatMoney(amount, plugin.getConfig().getString("commandauction.settings.default_economy", "vault"));
    }

    public boolean isPlayerPointsEnabled() {
        return playerPointsEnabled;
    }

    public Object getEconomy() {
        return economy;
    }
}