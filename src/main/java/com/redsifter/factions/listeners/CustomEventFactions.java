package com.redsifter.factions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class CustomEventFactions extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String name;
    private double amount;
    private String faction;

    public CustomEventFactions(String name, Player player, double amount,String faction) {
        this.name = name;
        this.player = player;
        this.amount = amount;
        this.faction = faction;
    }

    public String getName() {
        return this.name;
    }

    public Player getPlayer(){
        return this.player;
    }

    public double getAmount(){
        return this.amount;
    }

    public String getFaction(){
        return this.faction;
    }
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
