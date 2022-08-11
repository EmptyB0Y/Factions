package com.redsifter.factions.listener;

import com.redsifter.factions.Factions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.FileNotFoundException;
import java.util.HashMap;

import static com.redsifter.factions.Factions.chunkIsTaken;

public class Listen implements Listener {

    public HashMap<Player,String> locations = new HashMap<>();
    public static HashMap<Player, Location[]> selector = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) throws FileNotFoundException {

        if(selector.containsKey(e.getPlayer())){
            e.setCancelled(true);
            if(e.getAction() == Action.LEFT_CLICK_BLOCK){
                selector.replace(e.getPlayer(),new Location[]{e.getClickedBlock().getLocation()});
                e.getPlayer().sendMessage(ChatColor.DARK_GRAY+"l1 selected : X: "+e.getClickedBlock().getLocation().getX()+" Z: "+e.getClickedBlock().getLocation().getZ());
            }
            else if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
                selector.replace(e.getPlayer(),new Location[]{selector.get(e.getPlayer())[0],e.getClickedBlock().getLocation()});
                e.getPlayer().sendMessage(ChatColor.DARK_GRAY+"l2 selected : X: "+e.getClickedBlock().getLocation().getX()+" Z: "+e.getClickedBlock().getLocation().getZ());
            }
        }

        if(!chunkIsTaken(e.getClickedBlock().getLocation().getChunk().getChunkKey()).equals("")){
            if(!chunkIsTaken(e.getClickedBlock().getLocation().getChunk().getChunkKey()).equals(Factions.playerHasFaction(e.getPlayer()))) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot interact with this chunk, it belongs to " + chunkIsTaken(e.getClickedBlock().getLocation().getChunk().getChunkKey()) + " !");
            }
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) throws FileNotFoundException {
        String currentLocation = chunkIsTaken(e.getPlayer().getLocation().getChunk().getChunkKey());
        String display = ChatColor.GOLD+currentLocation;
        if(currentLocation.equals("")){
            currentLocation = Factions.isChunkClaimable(Long.toString(e.getPlayer().getLocation().getChunk().getChunkKey()));
            display = ChatColor.DARK_RED+currentLocation;
        }
        if(!currentLocation.equals(locations.get(e.getPlayer()))){
            e.getPlayer().sendTitle(display,"",20,30,20);
        }
        locations.put(e.getPlayer(),currentLocation);
    }
}
