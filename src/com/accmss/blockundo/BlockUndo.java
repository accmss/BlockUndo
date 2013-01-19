package com.accmss.blockundo;


//IMPORTS - JAVA
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;


//IMPORTS - BUKKIT
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;



public class BlockUndo extends JavaPlugin  {

	
public static BlockUndo zPlugin;
protected static FileConfiguration zConfig;
public static Logger zLogger = Logger.getLogger("Minecraft");


//VARS
public static World    WORLD;
public static Player   PLAYA;
public static Location LOCTA;
//public static Vector   VECTA;
public static Block    BLOCK;

//CONST
public static int blocks_cache_redo = 7;



public static boolean CMDinProgrss  = false; 

public static String format_7zeros = "0,000,000";
public static String format_4zeros = "0000";


public static long thread = 0L;
public static long ticks = 0L;

static long idelay = 0L;
static long repeat = 1200L * 1L; // 1200 = 60 seconds

@Override
public void onEnable() {

	Calendar calendar = new GregorianCalendar();
	int ss = calendar.get(Calendar.SECOND);
	int xx = calendar.get(Calendar.MILLISECOND);
	idelay = (1000 - xx) / 50;
	idelay = idelay + ((60 - ss) * 20);
	idelay = idelay - 16; 
	
	zPlugin = this;

	//Settings
	BlockUndoConfig.LoadSettings(zPlugin.getFile().getAbsolutePath());
	
	//MySQL
	BlockUndoMySQL.Connect();

	
		if (!BlockUndoMySQL.mysql_online)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", ChatColor.GRAY + BlockUndoMySQL.mysql_version + " : " +  BlockUndoMySQL.mysql_url + ChatColor.RED + " Offline.");
		}

	WORLD = this.getServer().getWorld(this.getServer().getWorlds().get(0).getName());
	LOCTA = WORLD.getSpawnLocation();
	BlockUndoLib.LastLocation = LOCTA;

	//Every 1 minute we divide clicks by idleM 
	getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
		public void run()
		{
		BlockUndoLib.CommitAll("1MTimer");
		ticks++;
			if (ticks > 59)
			{
			BlockUndoLib.RegenOres();
			ticks = 0L;
			}
	    }
	}, idelay + 4, repeat); //20 clicks to a second

	/*
	thread = Thread.currentThread().getId(); 
	String threadNm = Thread.currentThread().getName(); 
	String OSversion = System.getProperty("os.version");
	String JAVAversion = System.getProperty("java.version");
	int cores = Runtime.getRuntime().availableProcessors();
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "OS     : " + OSversion);
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "Java   : " + JAVAversion);
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "Cores  : " + cores);
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "Thread : " + thread + "(" + threadNm + ")");
	*/

	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo",  "   " + BlockUndoMySQL.mysql_version + " " +  BlockUndoMySQL.mysql_url + "§f Connected.");
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo",  BlockUndoLib.GetNumber(BlockUndoMySQL.mysql_players, ChatColor.GREEN, format_7zeros, true) + ChatColor.GRAY + " Players");
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo",  BlockUndoLib.GetNumber(BlockUndoMySQL.mysql_worlds.length, ChatColor.GREEN, format_7zeros, true) + ChatColor.GRAY + " Worlds");
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo",  BlockUndoLib.GetNumber(BlockUndoMySQL.mysql_blocks, ChatColor.GREEN, format_7zeros, true) + ChatColor.GRAY + " Blocks");
	BlockUndoLib.Purge(getServer().getConsoleSender());
	
		//Metrics
		try
		{
		BlockUndoMetricsLite metrics = new BlockUndoMetricsLite(this);
		metrics.start();
		} catch (IOException e) {
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[MetricsLite]", e.getCause() + " : " + e.getMessage());
		}

	//Listners
	getServer().getPluginManager().registerEvents(new BlockUndoPlayer(this), this);
	getServer().getPluginManager().registerEvents(new BlockUndoBlocks(this), this);
		
}
@Override
public void onDisable() 
{

	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo",  ChatColor.WHITE + "MySQL offline.");
	BlockUndoLib.CommitAll("onDisable");
	BlockUndoMySQL.Disconnenct();
	
}


@EventHandler
public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, final String[] args){


int icase = 0;

	CMDinProgrss = true;



		if(sender instanceof Player)
		{
		PLAYA = (Player)sender;
		//BlockUndoLib.LogCommand(PLAYA.getName(), cmd.toString());
		}
		else
		{
		//BlockUndoLib.LogCommand(sender.getName(), cmd.toString());
		}


		//bounces commands back to user when database is offline
		if (cmd.getName().equalsIgnoreCase("blockundo")) icase = 0;
		if (cmd.getName().equalsIgnoreCase("undo")) icase = 1;
		if (cmd.getName().equalsIgnoreCase("redo"))	 icase = 2;
		if (cmd.getName().equalsIgnoreCase("query")) icase = 3;
		if (cmd.getName().equalsIgnoreCase("purge")) icase = 4;
		if (cmd.getName().equalsIgnoreCase("vaporize")) icase = 5;
		
		
		if (!BlockUndoMySQL.mysql_online) 
		{
			BlockUndoLib.Chat(sender, "BlockUndo",  ChatColor.WHITE + "MySQL offline.");
		return true;
		}
		
		switch (icase) 
		{
		case 0:
			if (args.length == 1) //assume reload
			{
			reloadConfig();
			}
		CMDinProgrss = false;
		return true;
		
		case 1:
		
		if (args.length == 1) BlockUndoLib.UndoW(sender, args[0].toLowerCase());
		CMDinProgrss = false;
		return true;
		
		case 2:
			
		if (args.length == 1) BlockUndoLib.RedoW(sender, args[0].toLowerCase());
		CMDinProgrss = false;
		return true;
		
		case 3:
		BlockUndoLib.GiveQ(sender, PLAYA);
		CMDinProgrss = false;
		return true;

		case 4:
		BlockUndoLib.PurgeW(sender);
		CMDinProgrss = false;
		return true;
		
		case 5:
		BlockUndoLib.VaporW(sender);
		CMDinProgrss = false;
		return true;
		
		//case 6:
		//BlockUndoLib.WaterW(sender);
		//CMDinProgrss = false;
		//return true;
		
		
		}
		
		return false; 

	}
		


}