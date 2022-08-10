package com.redsifter.factions;

import com.redsifter.factions.files.FileManager;

import com.redsifter.factions.listener.Listen;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.Configuration;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class Factions extends JavaPlugin {

    public static FileManager fm;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new Listen(), this);
        getDataFolder().mkdir();
        try {
            fm = new FileManager("factions.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.saveDefaultConfig();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(label.equals("f")) {
            switch (args[0]) {
                case "create":
                    if(args.length < 2){
                        sender.sendMessage(ChatColor.RED+"Missing argument : /f create <NAME> !");
                        return false;
                    }
                    else if(args[1].length() > 50){
                        sender.sendMessage(ChatColor.RED+"The name is too long !");
                        return false;
                    }

                    try {
                        if(nameIsTaken(args[1])){
                            sender.sendMessage(ChatColor.RED+"The name is already taken!");
                            return false;
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        if(fm.getConfig().isConfigurationSection("count")){
                            fm.getConfig().set("count",0);
                        }
                        fm.getConfig().set("factions."+args[1]+".owner",sender.getName());
                        List<String> chunks = new ArrayList<>();
                        fm.getConfig().set("factions."+args[1]+".chunks",chunks);
                        fm.getConfig().set("factions."+args[1]+".players."+sender.getName()+".canClaim",true);
                        fm.saveConfig();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    sender.sendMessage(ChatColor.GREEN+"You successfully created "+args[1]+" !");

                    break;
                case "claim":
                    try {
                        String factionOfPlayer = playerHasFaction((Player)sender);
                        String factionOfChunk = chunkIsTaken(((Player)sender).getLocation().getChunk().getChunkKey());
                        if(factionOfPlayer.equals("")){
                            sender.sendMessage(ChatColor.RED+"You do not have a faction !");
                            return false;
                        }
                        else if(playerCanClaim((Player) sender, factionOfPlayer)){
                            sender.sendMessage(ChatColor.RED+factionOfPlayer+ "'s owner has not allowed you to claim chunks !!");
                            return false;
                        }
                        else if(factionOfChunk.equals(factionOfPlayer)){
                            sender.sendMessage(ChatColor.RED+"This chunk already belongs to your faction !");
                            return false;
                        }
                        else if(!factionOfChunk.equals("")){
                            sender.sendMessage(ChatColor.RED+"This chunk is taken by "+factionOfChunk+" !");
                            return false;
                        }

                        List<String> chunks = fm.getConfig().getStringList("factions."+factionOfPlayer+".chunks");
                        chunks.add(Long.toString(((Player)sender).getLocation().getChunk().getChunkKey()));
                        fm.getConfig().set("factions."+factionOfPlayer+".chunks",chunks);
                        fm.saveConfig();
                        sender.sendMessage(ChatColor.GREEN+"You successfully claimed that chunk !");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "unclaim":
                    try {
                        String factionOfPlayer = playerHasFaction((Player)sender);
                        String factionOfChunk = chunkIsTaken(((Player)sender).getLocation().getChunk().getChunkKey());
                        if(factionOfPlayer.equals("")){
                            sender.sendMessage(ChatColor.RED+"You do not have a faction !");
                            return false;
                        }
                        else if(playerCanClaim((Player) sender, factionOfPlayer)){
                            sender.sendMessage(ChatColor.RED+factionOfPlayer+ "'s owner has not allowed you to unclaim chunks !!");
                            return false;
                        }
                        else if(factionOfChunk.equals("")){
                            sender.sendMessage(ChatColor.RED+"This chunk is not currently claimed !");
                            return false;
                        }
                        else if(!factionOfChunk.equals(factionOfPlayer)){
                            sender.sendMessage(ChatColor.RED+"This chunk doesn't belong to your faction !");
                            return false;
                        }
                        List<String> chunks = (ArrayList<String>) fm.getConfig().getList("factions."+factionOfPlayer+".chunks");
                        chunks.remove(Long.toString(((Player)sender).getLocation().getChunk().getChunkKey()));
                        fm.getConfig().set("factions."+factionOfPlayer+".chunks",chunks);
                        fm.saveConfig();
                        sender.sendMessage(ChatColor.GREEN+"You successfully unclaimed that chunk !");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "disband":
                    try {
                        String factionOfPlayer = playerHasFaction((Player)sender);
                        if(!Objects.equals(fm.getConfig().getString("factions." + factionOfPlayer + ".owner"), sender.getName())){
                            sender.sendMessage(ChatColor.RED+"You are not the owner of the faction !");
                            return false;
                        }
                        deleteFaction(factionOfPlayer);
                        sender.sendMessage(ChatColor.GREEN+"You successfully deleted your faction !");
                        return false;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }
        }
        return true;
    }

    public boolean nameIsTaken(String str) throws FileNotFoundException {
        if(fm.getConfig().isConfigurationSection("factions."+str)){
            return true;
        }
        return false;
    }

    public static String chunkIsTaken(long key) throws FileNotFoundException {
        fm.reloadConfig();
        List<String> chunks;
        for(String f : fm.getConfig().getConfigurationSection("factions").getKeys(false)) {
            chunks = (ArrayList<String>) fm.getConfig().getList("factions." + f + ".chunks");
            if(chunks != null) {
                for (String i : chunks) {
                }
                if (chunks.contains(Long.toString(key))) {
                    return f;
                }
            }
        }

        return "";
    }

    public static String playerHasFaction(Player p) throws FileNotFoundException {
        fm.reloadConfig();
        for(String f : fm.getConfig().getConfigurationSection("factions").getKeys(false)) {
            if (fm.getConfig().getConfigurationSection("factions." + f + ".players").getKeys(false).contains(p.getName())) {
                return f;
            }
        }
        return "";
    }

    public boolean playerCanClaim(Player p, String faction) throws FileNotFoundException {
            if(fm.getConfig().isConfigurationSection("factions."+faction+".players."+p.getName())){
                return fm.getConfig().getBoolean("factions." + faction + ".players." + p.getName());
            }
        return false;
    }

    public void deleteFaction(String f) throws IOException {
        fm.reloadConfig();
        fm.getConfig().set("factions."+f,null);
        fm.saveConfig();
    }
}

