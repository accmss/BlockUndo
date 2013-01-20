package com.accmss.blockundo;


//IMPORTS - BUKKIT
import org.bukkit.World;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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


@EventHandler (priority = EventPriority.NORMAL)
public void onPlayerInteract(final PlayerInteractEvent event) {
	
	World MyWorld = event.getPlayer().getWorld();
	
		if (BlockUndoLib.IsWorldProtected(MyWorld.getName().toString())) 
		{

			if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{


				switch (event.getPlayer().getItemInHand().getType())
				{
				case GOLD_PICKAXE:

					if (event.getPlayer().hasPermission("blockundo.player"))
					{
					BlockUndoLib.estimatedTime = System.nanoTime() - BlockUndoLib.startTime;
						
				    new Thread(new Runnable() { 
				        public void run() { 
				        	try {
								Thread.sleep(2);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							BlockUndoLib.Query(event.getPlayer(), event.getClickedBlock().getLocation());
				        	} 
				    }).start(); 
				    


					//NEW cool off (manipulates variable from 1 const thread)
					BlockUndoLib.startTime = System.nanoTime();        
					}
				
					

					default:
					break;
					}
			
			}
			

		}
			

}

}