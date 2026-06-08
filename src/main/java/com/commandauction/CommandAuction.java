package com.commandauction;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandAuction extends JavaPlugin {
    private AuctionManager auctionManager;
    private EconomyManager economyManager;
    private AuctionListener listener;

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        economyManager = new EconomyManager(this);
        auctionManager = new AuctionManager(this);
        listener = new AuctionListener(this);
        
        PluginCommand cmd = getCommand("cmdauc");
        if (cmd != null) {
            cmd.setExecutor(new AuctionCommand(this));
        }
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(listener, this);
        
        getLogger().info("CommandAuction v" + getDescription().getVersion() + " 已启用!");
    }

    @Override
    public void onDisable() {
        if (auctionManager != null) {
            for (Auction auction : auctionManager.getAuctions()) {
                auctionManager.endAuction(auction, true);
            }
        }
        
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
        
        PluginCommand cmd = getCommand("cmdauc");
        if (cmd != null) {
            cmd.setExecutor(null);
        }
        
        auctionManager = null;
        economyManager = null;
        listener = null;
        
        getLogger().info("CommandAuction 已禁用!");
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}