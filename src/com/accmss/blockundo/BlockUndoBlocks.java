package com.accmss.blockundo;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;


public class BlockUndoBlocks implements Listener{



public BlockUndoBlocks(BlockUndo xPlugin) 
{

}  


@EventHandler (priority = EventPriority.NORMAL)
public void onBlockPlace(final BlockPlaceEvent event) {

//VARS - Async!
final Block MyBlock;
final Player MyPlayer;
final World MyWorld;
final Location MyLocation;
final Material MyMaterial;

		//EXIT 1 - world
		if (!BlockUndoLib.IsWorldProtected(event.getBlock().getWorld().getName().toString())) 
		{
		return;	
		}

		//EXIT 2 - player
		if(event.getPlayer() == null)
		{
		return;	
		}

	//VARS - set locals (NOT globals, since globals are overwritten by code lag, especially in multiplayer environments)
	MyBlock = event.getBlock();
	MyPlayer = event.getPlayer();
	MyWorld = MyPlayer.getWorld();
	MyLocation = MyBlock.getLocation();
	MyMaterial = MyBlock.getType();
	
	
	
	
		if (MyMaterial.getId() == 7 && MyPlayer.hasPermission("blockundo.player"))
		{

			if (event.getPlayer().hasPermission("blockundo.player"))
			{
			BlockUndoLib.estimatedTime = System.nanoTime() - BlockUndoLib.startTime; 
		
			
		    new Thread(new Runnable() { 
		        public void run() { 
		        	//MyWorld = MyPlayer.getWorld();
		        	try {
						Thread.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					BlockUndoLib.Query(MyPlayer, MyLocation);
		        } 
		    }).start(); 
			
			//NEW cool off (manipulates variable from 1 const thread)
			BlockUndoLib.startTime = System.nanoTime();        

			/*
			Block MyBlock2;
			MyBlock2 = MyWorld.getBlockAt(MyLocation); //NEW cancel water and lava grief
				if (MyBlock2.getTypeId() > 7 && MyBlock2.getTypeId() < 12)
				{
				MyBlock2.setTypeId(0);
				event.setCancelled(false);
				return;
				}
		*/
			}



		event.setCancelled(true);
		return;
		}

	//NEW runable DelayedTask

		final Block MyBlock1;
		MyBlock1 = MyWorld.getBlockAt(MyLocation); //NEW we get the block into a local var from the actual world delyaed by 1 second.
		
		
		//ORIENTATION BLOCKS 
		if(MyBlock1.getTypeId() ==  53 || MyBlock1.getTypeId() ==  67 || MyBlock1.getTypeId() == 108
	    || MyBlock1.getTypeId() == 109 || MyBlock1.getTypeId() == 114 || MyBlock1.getTypeId() == 128
	    || MyBlock1.getTypeId() == 134 || MyBlock1.getTypeId() == 135 || MyBlock1.getTypeId() == 136
	    || MyBlock1.getTypeId() ==  75 || MyBlock1.getTypeId() ==  76 || MyBlock1.getTypeId() ==  50
	    || MyBlock1.getTypeId() ==  29 || MyBlock1.getTypeId() ==  33 || MyBlock1.getTypeId() ==  69
	    || MyBlock1.getTypeId() ==  77 || MyBlock1.getTypeId() == 127 || MyBlock1.getTypeId() == 145
	    || MyBlock1.getTypeId() ==  26 || MyBlock1.getTypeId() ==  27 || MyBlock1.getTypeId() ==  28
	    || MyBlock1.getTypeId() ==  64 || MyBlock1.getTypeId() ==  65 || MyBlock1.getTypeId() ==  66)
		{

			//ASYNC delay to get the orientation of the stairs or torch
			BlockUndo.zPlugin.getServer().getScheduler().runTaskLaterAsynchronously(BlockUndo.zPlugin, new Runnable() {
				public void run()
				{
				Block MyBlock2;
				MyBlock2 = MyWorld.getBlockAt(MyLocation); //NEW we get the block into a local var from the actual world delyaed by 1 second.
				BlockUndoLib.AddBlock(MyPlayer.getName(), MyLocation.getX(), MyLocation.getY(), MyLocation.getZ(), 0, MyBlock2.getTypeId(), (byte)0, MyBlock2.getData(), MyPlayer.getWorld().getName().toString());
				}
				}, 1L); //20 clicks to a second
			
		}
		else
		{
		BlockUndoLib.AddBlock(MyPlayer.getName(), MyLocation.getX(), MyLocation.getY(), MyLocation.getZ(), 0, MyBlock1.getTypeId(), (byte)0, MyBlock1.getData(), MyPlayer.getWorld().getName().toString());
		}
		



	
}

@EventHandler (priority = EventPriority.NORMAL)
public void onBlockBreak(final BlockBreakEvent event) {

	//EXIT 1 - world
	if (!BlockUndoLib.IsWorldProtected(event.getBlock().getWorld().getName().toString())) 
	{
	//BlockUndoLib.LogMessage("FAILED=", event.getBlock().getWorld().getName().toString());
	return;
	}
	
	//EXIT 2 - player
	if(event.getPlayer() == null)
	{
	return;	
	}

    BlockUndoLib.AddBlock(event.getPlayer().getName(), event.getBlock().getLocation().getX(), event.getBlock().getLocation().getY(), event.getBlock().getLocation().getZ(), event.getBlock().getTypeId(), 0, event.getBlock().getData(), (byte)0, event.getBlock().getWorld().getName().toString());

}



}
