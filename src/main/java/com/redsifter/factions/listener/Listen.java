package com.redsifter.factions.listener;

import com.redsifter.factions.Factions;


import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.FileNotFoundException;

import static com.redsifter.factions.Factions.chunkIsTaken;

public class Listen implements Listener {


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) throws FileNotFoundException {

        if(!chunkIsTaken(e.getClickedBlock().getLocation().getChunk().getChunkKey()).equals("")){
            if(!chunkIsTaken(e.getClickedBlock().getLocation().getChunk().getChunkKey()).equals(Factions.playerHasFaction(e.getPlayer()))) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot interact with this chunk, it belongs to " + chunkIsTaken(e.getClickedBlock().getLocation().getChunk().getChunkKey()) + " !");
            }
        }

    }
}
