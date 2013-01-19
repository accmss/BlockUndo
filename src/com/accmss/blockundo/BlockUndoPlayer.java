package com.accmss.blockundo;


//IMPORTS - BUKKIT
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;


public class BlockUndoPlayer implements Listener 
{



public BlockUndoPlayer(BlockUndo xPlugin) 
{

}
@EventHandler (priority = EventPriority.NORMAL)
public void onPlayerJoin(final PlayerJoinEvent event)
{

	
    new Thread(new Runnable() { 
        public void run() { 
        	try {
			Thread.sleep(2);
			} catch (InterruptedException e) {
			e.printStackTrace();
			}
    		BlockUndoLib.AddPlayer(event.getPlayer().getName(), event.getPlayer().getAddress().getAddress().getHostAddress());
        } 
    }).start(); 

}


}