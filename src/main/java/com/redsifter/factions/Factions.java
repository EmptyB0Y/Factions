package com.redsifter.factions;

import com.redsifter.factions.files.FileManager;
import com.redsifter.factions.listener.Listen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Factions extends JavaPlugin {

    public static FileManager fm;
    public static HashMap<String,ArrayList<String>> noclaimchunks = new HashMap<>();

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
        List<Location> zones = null;
        try {
            for(String z : fm.getConfig().getConfigurationSection("no-claim-zones").getKeys(false)){
                zones = (List<Location>) fm.getConfig().getList("no-claim-zones."+z);
                Bukkit.getLogger().info(""+zones.get(0));
                Bukkit.getLogger().info(""+zones.get(1));
                Bukkit.getLogger().info("Setting up no-claim zone : "+z);
                setNoClaimZone(zones.get(0),zones.get(1),z);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getLogger().info(""+noclaimchunks);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0){
            args = new String[]{""};
        }
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
                        else if(!isChunkClaimable(Long.toString(((Player)sender).getLocation().getChunk().getChunkKey())).equals("")){
                            sender.sendMessage(ChatColor.RED+"This chunk is in a no-claim zone !");
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
                        try {
                            if(factionOfPlayer.equals("")){
                                sender.sendMessage(ChatColor.RED+"You do not have a faction !");
                                return false;
                            }
                            else if(!playerIsOwner(factionOfPlayer,(Player)sender)){
                                sender.sendMessage(ChatColor.RED+"You are not the owner of the faction !");
                            }
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        deleteFaction(factionOfPlayer);
                        sender.sendMessage(ChatColor.GREEN+"You successfully deleted your faction !");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "invite":
                    if(args.length < 2){
                        sender.sendMessage(ChatColor.RED+"Missing argument : /f invite <NAME> !");
                        return false;
                    }
                    try {
                        if(playerHasFaction((Player) sender).equals("")){
                            sender.sendMessage(ChatColor.RED+"You do not have a faction !");
                            return false;
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if(Bukkit.getPlayer(args[1]) != null) {
                        try {
                            playerInvite(playerHasFaction((Player) sender), Bukkit.getPlayer(args[1]));
                            sender.sendMessage(ChatColor.GREEN +" Successfully invited"+ args[1]+" to your faction !");
                            Bukkit.getPlayer(args[1]).sendMessage(ChatColor.GREEN+"You have been invited to join "+ChatColor.GOLD+playerHasFaction((Player) sender)+ChatColor.GREEN+" !");

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case "join":
                    if(args.length < 2){
                        sender.sendMessage(ChatColor.RED+"Missing argument : /f join <NAME> !");
                        return false;
                    }
                    try {
                        if(!fm.getConfig().isConfigurationSection("factions."+args[1])){
                            sender.sendMessage(ChatColor.RED+"Faction not found !");
                            return false;
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        if(fm.getConfig().isConfigurationSection("factions."+args[1])){
                            if(playerJoin(args[1],(Player)sender)){
                                sender.sendMessage(ChatColor.GREEN+"You successfully joined "+ChatColor.GOLD+playerHasFaction((Player) sender)+ChatColor.GREEN+" !");
                            }
                            else{
                                sender.sendMessage(ChatColor.DARK_GREEN+"You successfully requested to join "+ChatColor.GOLD+playerHasFaction((Player) sender)+ChatColor.GREEN+" !");
                            }

                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "leave":
                    try {
                        if(playerHasFaction((Player)sender).equals("")){
                            sender.sendMessage(ChatColor.RED+"You are not in any faction");
                            return false;
                        }
                        playerLeave(playerHasFaction((Player) sender),(Player) sender);
                        sender.sendMessage(ChatColor.GREEN+"You successfully left "+ChatColor.GOLD+playerHasFaction((Player) sender)+ ChatColor.GREEN+" !");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "accept":
                    if(args.length < 2){
                        sender.sendMessage(ChatColor.RED+"Missing argument : /f accept <NAME> !");
                        return false;
                    }
                    try {
                        if(playerHasFaction((Player)sender).equals("")){
                            sender.sendMessage(ChatColor.RED+"You do not have a faction !");
                            return false;
                        }
                        else if(!playerIsOwner(playerHasFaction((Player) sender),(Player)sender)){
                            sender.sendMessage(ChatColor.RED+"You are not the owner of the faction !");
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if(Bukkit.getPlayer(args[1]) != null) {
                        try {
                            if (playerAccept(playerHasFaction((Player) sender), Bukkit.getPlayer(args[1]))) {
                                sender.sendMessage(ChatColor.GREEN + args[1]+" successfully joined your faction !");

                            } else {
                                sender.sendMessage(ChatColor.RED + "This player has not requested to join your faction !");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else{
                        sender.sendMessage(ChatColor.RED+"Player not found !");
                    }
                    break;
                case "deny":
                    if(args.length < 2){
                        sender.sendMessage(ChatColor.RED+"Missing argument : /f deny <NAME> !");
                        return false;
                    }
                    try {
                        if(playerHasFaction((Player)sender).equals("")){
                            sender.sendMessage(ChatColor.RED+"You do not have a faction !");
                            return false;
                        }
                        else if(!playerIsOwner(playerHasFaction((Player) sender),(Player)sender)){
                            sender.sendMessage(ChatColor.RED+"You are not the owner of the faction !");
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if(Bukkit.getPlayer(args[1]) != null) {
                        try {
                            if (playerDeny(playerHasFaction((Player) sender), Bukkit.getPlayer(args[1]))) {
                                sender.sendMessage(ChatColor.GREEN + args[1]+"'s request to join your faction was denied !");

                            } else {
                                sender.sendMessage(ChatColor.RED + "This player has not requested to join your faction !");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else{
                        sender.sendMessage(ChatColor.RED+"Player not found !");
                    }
                    break;
                case "allowClaim":
                    try {
                        if(playerHasFaction((Player)sender).equals("")){
                            sender.sendMessage(ChatColor.RED+"You do not have a faction !");
                            return false;
                        }
                        else if(!playerIsOwner(playerHasFaction((Player) sender),(Player)sender)){
                            sender.sendMessage(ChatColor.RED+"You are not the owner of the faction !");
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if(args.length < 2){
                        try {
                            allowClaimAll(playerHasFaction((Player) sender));
                            sender.sendMessage(ChatColor.DARK_GRAY+"You allowed all the players of your factions from claiming chunks !");

                            return true;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(Bukkit.getPlayer(args[1]) != null) {
                        try {
                            allowClaim(playerHasFaction((Player) sender), Bukkit.getPlayer(args[1]));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else{
                        sender.sendMessage(ChatColor.RED+"Player not found !");
                    }
                    break;
                case "disallowClaim":
                    try {
                        if(playerHasFaction((Player)sender).equals("")){
                            sender.sendMessage(ChatColor.RED+"You do not have a faction !");
                            return false;
                        }
                        else if(!playerIsOwner(playerHasFaction((Player) sender),(Player)sender)){
                            sender.sendMessage(ChatColor.RED+"You are not the owner of the faction !");
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if(args.length < 2){
                        try {
                            disallowClaimAll(playerHasFaction((Player) sender));
                            sender.sendMessage(ChatColor.DARK_GRAY+"You disallowed all the players of your factions from claiming chunks !");
                            return true;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else if(Bukkit.getPlayer(args[1]) != null) {
                        try {
                            disallowClaim(playerHasFaction((Player) sender), Bukkit.getPlayer(args[1]));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else{
                        sender.sendMessage(ChatColor.RED+"Player not found !");
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.GOLD+"-----------------------------------------------------");
                    sender.sendMessage(ChatColor.GOLD+"FACTIONS COMMANDS :");
                    sender.sendMessage(ChatColor.GOLD+"f create <NAME> : "+ChatColor.GRAY+"Create faction");
                    sender.sendMessage(ChatColor.GOLD+"f claim : "+ChatColor.GRAY+"Claim chunk");
                    sender.sendMessage(ChatColor.GOLD+"f unclaim :"+ChatColor.GRAY+"Unclaim chunk");
                    sender.sendMessage(ChatColor.GOLD+"f disband :"+ChatColor.GRAY+"Disband faction");
                    sender.sendMessage(ChatColor.GOLD+"f invite <NAME> :"+ChatColor.GRAY+"Invite player to faction");
                    sender.sendMessage(ChatColor.GOLD+"f join <NAME> :"+ChatColor.GRAY+"Join faction");
                    sender.sendMessage(ChatColor.GOLD+"f leave:"+ChatColor.GRAY+"Leave faction");
                    sender.sendMessage(ChatColor.GOLD+"f accept <NAME> :"+ChatColor.GRAY+"Accept player into faction");
                    sender.sendMessage(ChatColor.GOLD+"f deny <NAME> :"+ChatColor.GRAY+"Deny player of getting into faction");
                    sender.sendMessage(ChatColor.GOLD+"f allowClaim [NAME] :"+ChatColor.GRAY+"Allow a player to claim, allow all player if NAME is not specified");
                    sender.sendMessage(ChatColor.GOLD+"f disallowClaim [NAME] :"+ChatColor.GRAY+"Disallow a player to claim, Disallow all player if NAME is not specified");
            }
        }
        else if(label.equals("fadmin"))
            {
            switch (args[0]){
                case "noclaim":
                    if(!Listen.selector.containsKey((Player) sender)) {
                        Listen.selector.put((Player) sender, null);
                        sender.sendMessage(ChatColor.GREEN+"Noclaim selector ON !");
                    }
                    else{
                        Listen.selector.remove((Player) sender);
                        sender.sendMessage(ChatColor.GREEN+"Noclaim selector OFF !");
                    }
                    break;
                case "set":
                    if(args.length < 2){
                        sender.sendMessage(ChatColor.RED+"You must set a name for your no-claim zone");
                        return false;
                    }
                    if(Listen.selector.get((Player) sender).length == 2) {
                        try {
                            setNoClaim(args[1],Listen.selector.get((Player) sender)[0],Listen.selector.get((Player) sender)[1]);
                            sender.sendMessage(ChatColor.GREEN+"No-claim zone defined successfully !");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Listen.selector.put((Player) sender, null);
                    }
                    else{
                        sender.sendMessage(ChatColor.RED+"You have not selected a region !");
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.GOLD+"-----------------------------------------------------");
                    sender.sendMessage(ChatColor.GOLD+"FACTIONS ADMIN COMMANDS :");
                    sender.sendMessage(ChatColor.GOLD+"fadmin noclaim : "+ChatColor.GRAY+"Toggle no-claim zone setter");
                    sender.sendMessage(ChatColor.GOLD+"fadmin set : "+ChatColor.GRAY+"Set no-claim zone");
            }
        }
        return true;
    }

    public static String isChunkClaimable(String key) throws FileNotFoundException {
        fm.reloadConfig();
        for(String z : fm.getConfig().getConfigurationSection("no-claim-zones").getKeys(false)){
           if(noclaimchunks.get(z).contains(key)){
               return z;
            }
        }
        return "";
    }
    public void setNoClaimZone(Location l1, Location l2,String name){

        noclaimchunks.put(name,new ArrayList<>());
        double l1x = l1.getX();
        double l2x = l2.getX();
        String resultx = "";

        double l1z = l1.getZ();
        double l2z = l2.getZ();
        String resultz = "";

        //X coord
        if(l1x > 0 && l2x > 0){
            if(l1x > l2x){
                //l1 >
                resultx = ">";
            }
            else if(l1x == l2x ) {
                //l1 =
                resultx = "=";
            }
            else{
                //l1 <
                resultx = "<";
            }
        }
        else if(l1x < 0 && l2x < 0){
            if(l1x > l2x){
                //l1 <
                resultx = ">";

            }
            else if(l1x == l2x) {
                //l1 =
                resultx = "=";
            }
            else{
                //l1 >
                resultx = "<";
            }
        }
        else if(l1x < 0 && l2x > 0){
            //l1 <
            resultx = "<";

        }
        else{
            //l1 >
            resultx = ">";
        }

        //Z coord
        if(l1z > 0 && l2z > 0){
            if(l1z > l2z){
                //l1 >
                resultz = ">";
            }
            else if(l1z == l2z) {
                //l1 =
                resultz = "=";
            }
            else{
                //l1 <
                resultz = "<";
            }
        }
        else if(l1z < 0 && l2z < 0){
            if(l1z > l2z){
                //l1 <
                resultz = ">";

            }
            else if(l1z == l2z) {
                //l1 =
                resultz = "=";
            }
            else{
                //l1 >
                resultz = "<";
            }
        }
        else if(l1z < 0 && l2z > 0){
            //l1 <
            resultz = "<";

        }
        else{
            //l1 >
            resultz = ">";
        }



        if(resultx.equals("=") && resultz.equals("=")){
            Bukkit.getLogger().info("=");
            noclaimchunks.get(name).add(Long.toString(l1.getChunk().getChunkKey()));
        }
        else if(resultx.equals(">") && resultz.equals(">")){
            Bukkit.getLogger().info(">");
            Location l = null;
            for(double x = l2x;x < l1x;x++){
                for(double z = l2z; x < l1z; x++){
                    l = new Location(l1.getWorld(),x,0,z);
                    if(!noclaimchunks.get(name).contains(Long.toString(l.getChunk().getChunkKey()))) {
                        noclaimchunks.get(name).add(Long.toString(l.getChunk().getChunkKey()));
                    }
                }
            }
        }
        else if(resultx.equals("<") && resultz.equals("<")){
            Bukkit.getLogger().info("<");
            Location l = null;
            for(double x = l2x;x > l1x;x--){
                for(double z = l2z;z > l1z; z--){
                    l = new Location(l1.getWorld(),x,0,z);
                    if(!noclaimchunks.get(name).contains(Long.toString(l.getChunk().getChunkKey()))) {
                        noclaimchunks.get(name).add(Long.toString(l.getChunk().getChunkKey()));
                    }
                }
            }
        }
        else if(resultx.equals(">") && resultz.equals("<")){
            Bukkit.getLogger().info("><");
            Location l = null;
            for(double x = l2x;x < l1x;x++){
                for(double z = l2z;z > l1z; z--){
                    l = new Location(l1.getWorld(),x,0,z);
                    if(!noclaimchunks.get(name).contains(Long.toString(l.getChunk().getChunkKey()))) {
                        noclaimchunks.get(name).add(Long.toString(l.getChunk().getChunkKey()));
                    }
                }
            }
        }
        else if(resultx.equals("<") && resultz.equals(">")){
            Bukkit.getLogger().info("<>");
            Location l = null;
            for(double x = l2x;x > l1x;x--){
                for(double z = l2z;z < l1z; z++){
                    l = new Location(l1.getWorld(),x,0,z);
                    if(!noclaimchunks.get(name).contains(Long.toString(l.getChunk().getChunkKey()))) {
                        noclaimchunks.get(name).add(Long.toString(l.getChunk().getChunkKey()));
                    }
                }
            }
        }
    }
    public void setNoClaim(String name,Location l1, Location l2) throws IOException {
        fm.reloadConfig();
        List<Location> locations = new ArrayList<>();
        locations.add(l1);
        locations.add(l2);
        fm.getConfig().set("no-claim-zones."+name,locations);
        fm.saveConfig();
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

    public void playerInvite(String f,Player p) throws IOException {
        fm.reloadConfig();
        fm.getConfig().set("factions."+f+".invited."+p.getName(),((System.currentTimeMillis()/1000)/60)/60+"/h");
        fm.saveConfig();
    }

    public boolean playerJoin(String f, Player p) throws IOException {
        fm.reloadConfig();
        if(fm.getConfig().isConfigurationSection("factions."+f+".invited."+p.getName())){
            fm.getConfig().set("factions."+f+".invited."+p.getName(),null);
            fm.getConfig().set("factions."+f+".players."+p.getName()+".canClaim",false);
            return true;
        }
        else{
            fm.getConfig().set("factions."+f+".requesting."+p.getName(),((System.currentTimeMillis()/1000)/60)/60+"/h");
        }
        fm.saveConfig();
        return false;
    }
    public boolean playerAccept(String f, Player p) throws IOException {
        fm.reloadConfig();
        if(fm.getConfig().isConfigurationSection("factions."+f+".requesting."+p.getName())){
            fm.getConfig().set("factions."+f+".requesting."+p.getName(),null);
            fm.getConfig().set("factions."+f+".players."+p.getName()+".canClaim",false);
        }
        else{
            return false;
        }
        fm.saveConfig();
        return true;
    }

    public boolean playerDeny(String f, Player p) throws IOException {
        fm.reloadConfig();
        if(fm.getConfig().isConfigurationSection("factions."+f+".requesting."+p.getName())){
            fm.getConfig().set("factions."+f+".requesting."+p.getName(),null);
        }
        else{
            return false;
        }
        fm.saveConfig();
        return true;
    }
    public void playerLeave(String f,Player p) throws IOException {
        fm.reloadConfig();
        fm.getConfig().set("factions."+playerHasFaction(p)+".players."+p.getName(),null);
        fm.saveConfig();
    }

    public boolean playerIsOwner(String f,Player p) throws FileNotFoundException {
        fm.reloadConfig();
        if(fm.getConfig().getString("factions."+f+".owner").equals(p.getName())){
            return true;
        }
        return false;
    }

    public void allowClaim(String f, Player p) throws IOException {
        fm.reloadConfig();
        fm.getConfig().set("factions."+f+".players."+p.getName()+".canClaim",true);
        fm.saveConfig();
    }

    public void allowClaimAll(String f) throws IOException {
        fm.reloadConfig();
        for(String p : fm.getConfig().getConfigurationSection("factions."+f+".players").getKeys(false)) {
            fm.getConfig().set("factions."+f+".players."+p+".canClaim",true);
        }
        fm.saveConfig();

    }
    public void disallowClaim(String f, Player p) throws IOException {
            fm.reloadConfig();
            if(fm.getConfig().isConfigurationSection("factions."+f+".owner."+p)){
                p.sendMessage(ChatColor.RED+"You cannot disallow yourself from claiming chunks !");
                return;
            }
            fm.getConfig().set("factions."+f+".players."+p.getName()+".canClaim",false);
            fm.saveConfig();
        }
    public void disallowClaimAll(String f) throws IOException {
        fm.reloadConfig();
        for(String p : fm.getConfig().getConfigurationSection("factions."+f+".players").getKeys(false)) {
            if(!fm.getConfig().isConfigurationSection("factions."+f+".owner."+p)) {
                fm.getConfig().set("factions." + f + ".players." + p + ".canClaim", false);
            }
        }
        fm.saveConfig();
    }
}

