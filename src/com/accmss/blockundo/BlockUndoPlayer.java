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

	
	
	BlockUndo.zPlugin.getServer().getScheduler().runTaskLaterAsynchronously(BlockUndo.zPlugin, new Runnable()
	{
		public void run()
		{
    	BlockUndoLib.AddPlayer(event.getPlayer().getName(), event.getPlayer().getAddress().getAddress().getHostAddress());
		}
	}, 16L); //20 clicks to a second
	

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
						
					
					BlockUndo.zPlugin.getServer().getScheduler().runTaskLaterAsynchronously(BlockUndo.zPlugin, new Runnable()
					{
						public void run()
						{
						BlockUndoLib.Query(event.getPlayer(), event.getClickedBlock().getLocation());
						}
					}, 1L); //20 clicks to a second
					
			 


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