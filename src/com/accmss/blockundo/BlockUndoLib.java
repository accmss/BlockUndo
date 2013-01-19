package com.accmss.blockundo;


//IMPORTS - JAVA
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;


//IMPORTS - BUKKIT
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


//IMPORTS - GOOGLE
import com.google.common.base.Stopwatch;


public class BlockUndoLib {

	
//SQL
private static String insert_worlds = "INSERT IGNORE INTO worlds (name) VALUES ('name_');";
private static String insert_player = "INSERT IGNORE INTO players (name, ip, entered) VALUES ('name_', 'ip_', now());";
private static String insert_blocks = "REPLACE INTO blocks (idplayer, idworld, x, y, z, b1val, b1dat, b2val, b2dat, `purge`, `entered`) VALUES (idplayer_, idworld_, x_, y_, z_,  b1val_, b1dat_, b2val_, b2dat_, 'N', now()); ";


//VARS
static boolean executing = false;
static int count = 0;
static StringBuilder LastQuery = new StringBuilder();
static Location LastLocation;
static Stopwatch sw1 = new Stopwatch();
static NumberFormat formatter = new DecimalFormat("#0.00");
static NumberFormat formatter2 = new DecimalFormat("#000");
static NumberFormat formatter3 = new DecimalFormat("00.0");
public static long startTime = 0;
public static long estimatedTime = 0;
public static StringBuilder sb = new StringBuilder();
static int griefblocks = 0;


//MYSQL
private static void TransLock()
{

	executing = true;

}
private static void TransUnlock()
{

	BlockUndoMySQL.CloseRS();
	executing = false;

}
public static void AddPlayers()
{
	Player[] players;
	
	players = BlockUndo.zPlugin.getServer().getOnlinePlayers();
  
	    for (Player player : players) 
	    {
	    BlockUndoLib.AddPlayer(player.getName(), player.getPlayer().getAddress().getAddress().getHostAddress());
	    }
	    
}
public static void AddPlayer(String name, String IP)
{

	String insert = insert_player;
	insert = insert.replace("ip_", IP);
	insert = insert.replace("name_", name); //FIX add name last so name is allowed to contain ip_

		try 
		{
			if (BlockUndoMySQL.mysql_online)
			{
			BlockUndoMySQL.mysql_sta.addBatch(insert);
			}
		} catch (SQLException e) 
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e.getErrorCode() + "]", e.getMessage());
		}
	
}
public static void AddWorlds()
{

		for (String s : BlockUndoConfig.Worlds)
		{
		BlockUndoLib.AddWorld(s.toLowerCase());
		}

}
public static void AddWorld(String name)
{

	String insert = insert_worlds;
	insert = insert.replace("name_", name);

		try 
		{
			if (BlockUndoMySQL.mysql_online)
			{
			BlockUndoMySQL.Update(insert);
			}
		} catch (Exception e) 
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e.getCause() + "]", e.getMessage());
		}
	
}
public static void AddBlock(String name, Double x1, Double y1, Double z1, int val1, int val2, byte data1, byte data2, String xWorld)  
{

	int x = (int)Math.round(x1);
	int y = (int)Math.round(y1);
	int z = (int)Math.round(z1);
	
	count++;
	String insert = insert_blocks;
	insert = insert.replace("x_", Integer.toString(x));
	insert = insert.replace("y_", Integer.toString(y));
	insert = insert.replace("z_", Integer.toString(z));
	insert = insert.replace("b1val_", Integer.toString(val1));
	insert = insert.replace("b1dat_", Byte.toString(data1));
	insert = insert.replace("b2val_", Integer.toString(val2));
	insert = insert.replace("b2dat_",  Byte.toString(data2));
	insert = insert.replace("idplayer_", "(SELECT id FROM players WHERE name = '" + name + "')");//FIX add id player last, so any name values dont conflict with x_ and y_ or _z_ ect..
	insert = insert.replace("idworld_", GetWorldID(xWorld));
	
		try 
		{
			if (BlockUndoMySQL.mysql_online)
			{
			BlockUndoMySQL.mysql_sta.addBatch(insert);
			}
		} 
		catch (SQLException e) 
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e.getErrorCode() + "]"," [" + e.getMessage() + "]");
		}
		
}
public static void CommitAll(String Caller)
{

		if (count == 0) return;

		if (BlockUndoMySQL.mysql_online)
		{
			if (!executing)
			{
			executing = true;	
			CommitAllAsync(Caller);
			executing = false;
			}	
		}
		

}
public static void CommitAllAsync(String Caller) 
{

	int queries1 = 0;
	long threadId = Thread.currentThread().getId(); 
	
	sw1.reset();
	sw1.start();
		
	queries1 = count;
	count = 0;

		try
		{
			if (queries1 > 0)
			{
			BlockUndoMySQL.mysql_sta.clearWarnings();
			BlockUndoMySQL.mysql_sta.executeBatch();	
			}
		}
		
		catch (NumberFormatException e1)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", " NumberFormatException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getCause() + "]", e1.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getClass().getName() + "]", e1.getStackTrace().toString());
		RepairConnection();
		}
		catch (SQLException e2)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", " SQLException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getErrorCode() + "]", e2.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getClass().getName() + "]", e2.getStackTrace().toString());
		RepairConnection();
		}
		catch (Exception e3)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", " Exception");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getCause() + "]", e3.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getClass().getName() + "]", e3.getStackTrace().toString());
		RepairConnection();
		}

	sw1.stop();
	double secs = sw1.elapsedMillis() / 1000D;
	double capacity = (secs / 60 ) *100;
			if (capacity > 80)
			{
			BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", formatter2.format(queries1) + " Updates on thread " + formatter2.format(BlockUndo.thread) + "\\" + formatter2.format(threadId) + " in " + formatter.format(secs) + "s - " + formatter3.format(capacity) + "% Net capacity");
			}

}
public static void RepairConnection()
{

	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", "Checking connection..");
	BlockUndoLib.CheckConnection();
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", "Adding olnine players..");
	BlockUndoLib.AddPlayers();

}
public static void CheckConnection()
{

		try
		{
			if (!BlockUndoMySQL.mysql_con.isValid(32))
			{
			BlockUndoMySQL.Disconnenct();
			BlockUndoMySQL.Connect();
			}
		}
		catch (NumberFormatException e1)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", " NumberFormatException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getCause() + "]", e1.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getClass().getName() + "]", e1.getStackTrace().toString());
		BlockUndoMySQL.mysql_online = false;
		} 
		catch (SQLException e2)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", " SQLException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getErrorCode() + "]", e2.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getClass().getName() + "]", e2.getStackTrace().toString());
		BlockUndoMySQL.mysql_online = false;
		}
		catch (Exception e3)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", " Exception");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getCause() + "]", e3.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getClass().getName() + "]", e3.getStackTrace().toString());
		BlockUndoMySQL.mysql_online = false;
		}

}


//SUPPORT
public static Boolean IsWorldProtected(String world)
{

	    for (String s : BlockUndoConfig.Worlds)
	    {
			if (s.equalsIgnoreCase(world))
			{
			return true;
			}
	    }

	return false;

}
public static String GetWorldID(String world)
{

	String[] val1;
	
	    for (String s : BlockUndoMySQL.mysql_worlds)
	    {
	
			if (s.startsWith(world))
			{
			val1= s.split(";");
			return val1[1];
			}
	    }
	
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo",  ChatColor.WHITE + "World not indexed");
    return "-1";
 
}
public static String GetNumber(int num, ChatColor color, String format, Boolean SpaceZeros)
{
	
DecimalFormat myFormatter = new DecimalFormat(format); 
String output = "";
String split[] = null;
int k = 0;

	//1 - format
    output = myFormatter.format(num); 
	split = output.split(",");
	output = "";
	
		//2 - rebuild output with spaces
		if (SpaceZeros)
		{
			
			for(k = 0; k < split.length; k++)
			{
				
	
				if (k < split.length-1)
				{
				split[k] = split[k].replaceAll("000", "   ");	
				}
				else
				{
				split[k] = split[k].replaceAll("000", "  0");		
				}
				
				if (split[k].equalsIgnoreCase("0"))
				{
				split[k] = " ";
				}

			output = output + split[k] + ",";
			}
			
		//3 - remove trailing ","
		output = output.substring(0, output.length()-1);
		}
		else
		{
		output = myFormatter.format(num); //NEW hack to get around output variable clearing
		}

	//4 - color numbers
	output = color + output;
	
	//4 - color commas
	output = output.replaceAll(",", ChatColor.GRAY + "," + color);
	return output;

}


//LOGGING
public static void Chat(CommandSender sender, String PluginName, String message)
{

	sender.sendMessage("[" + PluginName + "] " + message);
	
}
public static void LogCommand(String player, String command){
	 
	BlockUndo.zLogger.info("[PLAYER_COMMAND] " + player + ": " + command);

}



//COMMANDS - CORE
private static int Undo(CommandSender sender, String player)
{
	
String playern = "";
String playerT = "";
String playerID = "";
int pblocks;

int c1 = 0;
int c2 = 0;
int c3 = 0;

String msg = "";
double secs = 0;
   
		//EXIT 1
		if (!BlockUndoMySQL.mysql_online)
		{
		BlockUndoLib.Chat(sender, "BlockUndo",  ChatColor.WHITE + "MySQL offline.");
		return 0;
		}
		
		//EXIT 2
		if (executing)
		{
		BlockUndoLib.Chat(sender, "BlockUndo",  ChatColor.WHITE + "Undo cancelled..");
		return 0;
		}

		
	BlockUndoLib.CommitAll("Undo");

	
	TransLock(); // ---->>
	sw1.reset();
	sw1.start();
	playerID = BlockUndoMySQL.Query("SELECT id FROM players WHERE name = '" + player + "'");
	
		if (playerID.equalsIgnoreCase("no results"))
		{
		playerT = player.substring(0, Math.min(4, player.length())); //4 chars only
		playern = BlockUndoMySQL.Query("SELECT GROUP_CONCAT(name) FROM (SELECT name FROM players WHERE name LIKE '%" + playerT + "%' ORDER BY name LIMIT 1) as t0;");
		BlockUndoLib.Chat(sender, "BlockUndo",  "Player not found.  Players found:  " + ChatColor.RED + "[" + ChatColor.WHITE + playern + ChatColor.RED +  "]" + ChatColor.WHITE + ".");
		TransUnlock(); // <<----
		return 0;
		}
		else
		{
		//BlockUndoLib.Chat(BlockUndo.PLAYA, "BlockUndo", playern + " playern=.");	
		playern = "0";
		playern = BlockUndoMySQL.Query("SELECT count(idplayer) AS ex1 FROM blocks WHERE idplayer = " + playerID + ";");
		pblocks = Integer.parseInt(playern);
			if (pblocks == 0) 
			{
			BlockUndoLib.Chat(sender, "BlockUndo",  "Blocks not found!");	
			TransUnlock(); // <<----
			return 0;
			}
		}


	c1 = UndoCore(sender, "SELECT CONCAT_WS(',', b1val, x, y, z, b1dat, `name`) AS ex1 FROM blocks INNER JOIN worlds ON worlds.id = blocks.idworld WHERE idplayer = " + playerID + " AND b1val     IN(1,2,3,4,5,7,12,13,14,15,16,17,18,19,20,21,22,23,24,25,35,41,42,43,44,45,46,47,48,49,56,57,60,73,74,79,80,82,87,88,89,98,99,100,110,112,129,133) ORDER BY entered ASC;");
	c2 = UndoCore(sender, "SELECT CONCAT_WS(',', b1val, x, y, z, b1dat, `name`) AS ex1 FROM blocks INNER JOIN worlds ON worlds.id = blocks.idworld WHERE idplayer = " + playerID + " AND b1val NOT IN(1,2,3,4,5,7,12,13,14,15,16,17,18,19,20,21,22,23,24,25,35,41,42,43,44,45,46,47,48,49,56,57,60,73,74,79,80,82,87,88,89,98,99,100,110,112,129,133) ORDER BY entered ASC;");
	c3 = c1 + c2;

	sw1.stop();
	secs = sw1.elapsedMillis() / 1000D;
	BlockUndoMySQL.Update("UPDATE blocks SET `purge` = 'Y' WHERE idplayer = " + playerID + ";");
	msg = ChatColor.GREEN + player + " " + GetNumber(c3, ChatColor.YELLOW, BlockUndo.format_4zeros, true) + ChatColor.WHITE + " Blocks changed in " + ChatColor.GRAY + formatter.format(secs)  + ChatColor.WHITE + " seconds.";
	
		//if (!sender.equals(BlockUndo.zPlugin.getServer().getConsoleSender())) 
		//{
	BlockUndoLib.Chat(sender, "BlockUndo",  msg);
		//}
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", msg);
	TransUnlock(); // <<----
	
	return 1;

}
private static int UndoCore(CommandSender sender, String sql)
{

int c = 0;
int id = 0;
byte data;
String[] split = null;

	//NEW restores blocks in the same order they were destroyed
	BlockUndoMySQL.Query(sql);
			
		try
		{
			do
			{
			split = BlockUndoMySQL.mysql_res.getString("ex1").split(",");
			c++;
			BlockUndo.WORLD = BlockUndo.zPlugin.getServer().getWorld(split[5]);
			BlockUndo.LOCTA.setWorld(BlockUndo.WORLD);
			BlockUndo.LOCTA.setX(Integer.parseInt(split[1]));
			BlockUndo.LOCTA.setY(Integer.parseInt(split[2]));
			BlockUndo.LOCTA.setZ(Integer.parseInt(split[3]));
			BlockUndo.BLOCK = BlockUndo.WORLD.getBlockAt(BlockUndo.LOCTA);
	
				try
				{
				id = Integer.parseInt(split[0]);
				data = Byte.parseByte(split[4]);
					if (BlockUndo.BLOCK.getTypeId() == id && BlockUndo.BLOCK.getData() == data)
					{
					//BlockUndoLib.Chat(sender, "Debug", "Optimized - skipped!");
					}
					else
					{
						if (id != 10 && id != 10) BlockUndo.BLOCK.setTypeIdAndData(id,data, true);
					}

				}
				catch (Exception e0)
				{
				BlockUndoLib.Chat(sender, "BlockUndo", c + " Failed to set type: " + Material.getMaterial(Integer.parseInt(split[0])) + " at " + BlockUndo.LOCTA.toVector().toString());
				BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo]", c + " Failed to set type: " + Material.getMaterial(Integer.parseInt(split[0])) + " at " + BlockUndo.LOCTA.toVector().toString());
				BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo]", "[" + e0.getMessage() + "]");
				BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e0.getClass().getName() + "]", "[" + e0.getStackTrace() + "]");
				}
			
			}
			while (BlockUndoMySQL.mysql_res.next());
	
		} 
		catch (NumberFormatException e1)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-Undo]", " NumberFormatException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getCause() + "]", e1.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getClass().getName() + "]", e1.getStackTrace().toString());
		} 
		catch (SQLException e2)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-Undo]", " SQLException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getErrorCode() + "]", e2.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getClass().getName() + "]", e2.getStackTrace().toString());
		}
		catch (Exception e3)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-Undo]", " Exception");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getCause() + "]", e3.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getClass().getName() + "]", e3.getStackTrace().toString());
		}

	return c;

}
private static int Redo(CommandSender sender, String player)
{

String playern = "";
String playerT = "";
String playerID = "";
int pblocks;
	
int c1 = 0;
int c2 = 0;
int c3 = 0;

String msg = "";
double secs = 0;

		//EXIT 1
		if (!BlockUndoMySQL.mysql_online)
		{
		BlockUndoLib.Chat(sender, "BlockUndo",  ChatColor.WHITE + "MySQL offline.");
		return 0;
		}
		
		//EXIT 2
		if (executing)
		{
		BlockUndoLib.Chat(sender, "BlockUndo",  ChatColor.WHITE + "Redo Cancelled..");
		return 0;
		}


	BlockUndoLib.CommitAll("Redo");
	
	TransLock(); // ---->>
	sw1.reset();
	sw1.start();
	playerID = BlockUndoMySQL.Query("SELECT id FROM players WHERE name = '" + player + "'");
		if (playerID.equalsIgnoreCase("no results"))
		{
		playerT = player.substring(0, Math.min(4, player.length())); //4 chars only
		playern = BlockUndoMySQL.Query("SELECT GROUP_CONCAT(name) FROM (SELECT name FROM players WHERE name LIKE '%" + playerT + "%' ORDER BY name LIMIT 1) as t0;");
		BlockUndoLib.Chat(sender, "BlockUndo",  "Player not found.  Players found: " + ChatColor.RED + "[" + ChatColor.WHITE + playern + ChatColor.RED +  "]" + ChatColor.WHITE + ".");
		TransUnlock(); // <<----
		return 0;
		}
		else
		{
		//BlockUndoLib.Chat(BlockUndo.PLAYA, "BlockUndo", playern + " playern=.");	
		playern = "0";
		playern = BlockUndoMySQL.Query("SELECT count(idplayer) AS ex1 FROM blocks WHERE idplayer = " + playerID + ";");
		pblocks = Integer.parseInt(playern);
			if (pblocks == 0) 
			{
			BlockUndoLib.Chat(sender, "BlockUndo",  "Blocks not found!");	
			TransUnlock(); // <<----
			return 0;
			}
		}
	
	//NEW restores blocks in the same order they were destroyed
	c1 = RedoCore(sender, "SELECT CONCAT_WS(',', b2val, x, y, z, b2dat, `name`) AS ex1 FROM blocks INNER JOIN worlds ON worlds.id = blocks.idworld WHERE idplayer = " + playerID + " AND b2val     IN(1,2,3,4,5,7,12,13,14,15,16,17,18,19,20,21,22,23,24,25,35,41,42,43,44,45,46,47,48,49,56,57,60,73,74,79,80,82,87,88,89,98,99,100,110,112,129,133) ORDER BY entered ASC;");
	c2 = RedoCore(sender, "SELECT CONCAT_WS(',', b2val, x, y, z, b2dat, `name`) AS ex1 FROM blocks INNER JOIN worlds ON worlds.id = blocks.idworld WHERE idplayer = " + playerID + " AND b2val NOT IN(1,2,3,4,5,7,12,13,14,15,16,17,18,19,20,21,22,23,24,25,35,41,42,43,44,45,46,47,48,49,56,57,60,73,74,79,80,82,87,88,89,98,99,100,110,112,129,133) ORDER BY entered ASC;");
    c3 = c1 + c2;

	sw1.stop();
	secs = sw1.elapsedMillis() / 1000D;
	BlockUndoMySQL.Update("UPDATE blocks SET `purge` = 'Y' WHERE idplayer = " + playerID + ";");
	msg = ChatColor.GREEN + player + " " + GetNumber(c3, ChatColor.YELLOW, BlockUndo.format_4zeros, true) + ChatColor.WHITE + " Blocks changed in " + ChatColor.GRAY  + formatter.format(secs)  + ChatColor.WHITE + " seconds.";
	
		//if (!sender.equals(BlockUndo.zPlugin.getServer().getConsoleSender())) 
		//{
	BlockUndoLib.Chat(sender, "BlockUndo", msg);
		//}
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", msg);
	TransUnlock(); // <<----

	return 1;

}
private static int RedoCore(CommandSender sender, String sql)
{

int c = 0;
int id = 0;
byte data;
String[] split = null;

	BlockUndoMySQL.Query(sql);
	
		try
		{
			do
			{
			split = BlockUndoMySQL.mysql_res.getString("ex1") .split(",");
			c++;
			BlockUndo.WORLD = BlockUndo.zPlugin.getServer().getWorld(split[5]);
			BlockUndo.LOCTA.setWorld(BlockUndo.WORLD);
			BlockUndo.LOCTA.setX(Integer.parseInt(split[1]));
			BlockUndo.LOCTA.setY(Integer.parseInt(split[2]));
			BlockUndo.LOCTA.setZ(Integer.parseInt(split[3]));
			BlockUndo.BLOCK = BlockUndo.WORLD.getBlockAt(BlockUndo.LOCTA);
			//BlockUndo.BLOCK.setTypeId(Integer.parseInt(split[0])); //NOTE only difference
			
				try
				{
					
				id = Integer.parseInt(split[0]);
				data = Byte.parseByte(split[4]);
					if (BlockUndo.BLOCK.getTypeId() == id && BlockUndo.BLOCK.getData() == data)
					{
					//BlockUndoLib.Chat(sender, "Debug", "Optimized - skipped!");
					}
					else
					{
						if (id != 10 && id != 10) BlockUndo.BLOCK.setTypeIdAndData(id, data, true);
					}
	
				}
				catch (Exception e0)
				{
	
				BlockUndoLib.Chat(sender, "BlockUndo", c + " Failed to set type: " + Material.getMaterial(Integer.parseInt(split[0])) + " at " + BlockUndo.LOCTA.toVector().toString());
				BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo]", c + " Failed to set type: " + Material.getMaterial(Integer.parseInt(split[0])) + " at " + BlockUndo.LOCTA.toVector().toString());
				BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo]", "[" + e0.getMessage() + "]");
				BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e0.getClass().getName() + "]", "[" + e0.getStackTrace() + "]");
				}
			
			}
			while (BlockUndoMySQL.mysql_res.next());
			
			
		} 
		catch (NumberFormatException e1)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-Redo]", " NumberFormatException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getCause() + "]", e1.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getClass().getName() + "]", e1.getStackTrace().toString());
		} 
		catch (SQLException e2)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-Redo]", " SQLException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getErrorCode() + "]", e2.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getClass().getName() + "]", e2.getStackTrace().toString());
		}
		catch (Exception e3)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-Redo]", " Exception");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getCause() + "]", e3.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getClass().getName() + "]", e3.getStackTrace().toString());
		}

	return c;

}
//COMMANDS - ADVANCED
private static void Vapor(CommandSender sender)
{

	int x = BlockUndo.PLAYA.getLocation().getBlockX();
	int y = 128; //BlockUndo.PLAYA.getLocation().getBlockY();
	int z = BlockUndo.PLAYA.getLocation().getBlockZ();
	
	griefblocks = 0;
	VaporZ(x+0, y, z+0);
	
	VaporZ(x+1, y, z+0);
	VaporZ(x-1, y, z+0);
	VaporZ(x+0, y, z+1);
	VaporZ(x+0, y, z-1);
	
	VaporZ(x-1, y, z-1);
	VaporZ(x+1, y, z+1);
	
	VaporZ(x-1, y, z+1);
	VaporZ(x+1, y, z-0);
	
	//VaporZ(x+0, y, z+0);
	
	VaporZ(x+2, y, z+0);
	VaporZ(x-2, y, z+0);
	VaporZ(x+0, y, z+2);
	VaporZ(x+0, y, z-2);
	
	VaporZ(x-2, y, z-2);
	VaporZ(x+2, y, z+2);
	
	VaporZ(x-2, y, z+2);
	VaporZ(x+2, y, z-0);

	BlockUndoLib.Chat(sender, "BlockUndo", "Vaporized " + griefblocks + " blocks");
	
}
private static void FixWater(CommandSender sender)
{

	int x = BlockUndo.PLAYA.getLocation().getBlockX();
	int y = 128; //BlockUndo.PLAYA.getLocation().getBlockY();
	int z = BlockUndo.PLAYA.getLocation().getBlockZ();
	
	griefblocks = 0;
	FixWaterCore(x+0, y, z+0);
	
	FixWaterCore(x+1, y, z+0);
	FixWaterCore(x-1, y, z+0);
	FixWaterCore(x+0, y, z+1);
	FixWaterCore(x+0, y, z-1);
	
	FixWaterCore(x-1, y, z-1);
	FixWaterCore(x+1, y, z+1);
	
	FixWaterCore(x-1, y, z+1);
	FixWaterCore(x+1, y, z-0);
	
	//VaporZ(x+0, y, z+0);
	
	FixWaterCore(x+2, y, z+0);
	FixWaterCore(x-2, y, z+0);
	FixWaterCore(x+0, y, z+2);
	FixWaterCore(x+0, y, z-2);
	
	FixWaterCore(x-2, y, z-2);
	FixWaterCore(x+2, y, z+2);
	
	FixWaterCore(x-2, y, z+2);
	FixWaterCore(x+2, y, z-0);

	BlockUndoLib.Chat(sender, "BlockUndo", "Flowed " + griefblocks + " blocks");
	
}
private static void VaporZ(int x, int y, int z)
{

		for(int i=1; i<128; i++)
		{
		BlockUndo.LOCTA.setWorld(BlockUndo.PLAYA.getWorld());
		BlockUndo.LOCTA.setX(x);
		BlockUndo.LOCTA.setY(y-i);
		BlockUndo.LOCTA.setZ(z);
		//GRIEF BLOCKS = cobble/all water/all lava/osidian
		BlockUndo.BLOCK = BlockUndo.WORLD.getBlockAt(BlockUndo.LOCTA);
			if (BlockUndo.BLOCK.getTypeId() == 4  ||
				BlockUndo.BLOCK.getTypeId() == 8  || BlockUndo.BLOCK.getTypeId() == 9 ||
				BlockUndo.BLOCK.getTypeId() == 10 || BlockUndo.BLOCK.getTypeId() == 11 ||
				BlockUndo.BLOCK.getTypeId() == 49  || BlockUndo.BLOCK.getTypeId() == 51)
				{
				griefblocks++;
				BlockUndo.BLOCK.setTypeId(0);
				}

		}

}
private static void FixWaterCore(int x, int y, int z)
{

		for(int i=1; i<128; i++)
		{
		BlockUndo.LOCTA.setWorld(BlockUndo.PLAYA.getWorld());
		BlockUndo.LOCTA.setX(x);
		BlockUndo.LOCTA.setY(i);
		BlockUndo.LOCTA.setZ(z);
		
		//GRIEF BLOCKS = cobble/all water/all lava/osidian
		BlockUndo.BLOCK = BlockUndo.WORLD.getBlockAt(BlockUndo.LOCTA);
		//BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), BlockUndo.BLOCK.getLocation().toString(), Integer.toString(BlockUndo.BLOCK.getTypeId()));
			if (BlockUndo.BLOCK.getTypeId() == 8)
				{
				griefblocks++;
				BlockUndo.BLOCK.setTypeId(9);
				}

		}

}


//METHODS
public static void Purge(CommandSender sender)
{

	//EXIT 1
	if (!BlockUndoMySQL.mysql_online)
	{
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo",  ChatColor.WHITE + "MySQL offline.");
	return;
	}

	//EXIT 2
	if (executing)
	{
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo",  ChatColor.WHITE + "Purge Cancelled..");
	return;
	}
	
	//BlockUndoLib.CommitAll("Purge");

	TransLock(); // ---->>
	//DELETE BLOCKS
	int res = BlockUndoMySQL.Update("DELETE FROM blocks WHERE `purge` = 'Y' AND DATEDIFF(now(), `entered`) >= " + BlockUndo.blocks_cache_redo + " ;");

		if (res > 0 )
		{
		//DELETE PLAYERS
		BlockUndoMySQL.Update("DELETE players FROM players LEFT OUTER JOIN blocks AS t1 ON t1.idplayer = players.id WHERE t1.idplayer IS NULL;");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", GetNumber(res, ChatColor.LIGHT_PURPLE, BlockUndo.format_7zeros, true) + ChatColor.WHITE + " Blocks purged!");
		}

	TransUnlock(); // <<----

}
public static void Query(Player player, Location loca)
{

int x = loca.getBlockX();
int y = loca.getBlockY();
int z = loca.getBlockZ();

int c = 0;
int k = 0;
String res;
String val="";
String sqls = "SELECT CONCAT_WS(',', DATEDIFF(blocks.`entered`, now()), b1val, b2val, `name`) AS ex1 FROM blocks INNER JOIN players ON blocks.idplayer = players.id WHERE idworld = " + GetWorldID(loca.getWorld().getName().toString()) + " AND x = " + Integer.toString(x) + " AND Y = " + Integer.toString(y) + " AND z = " + Integer.toString(z) + " ORDER BY blocks.`entered` ASC;";
String[] split;
String[] msgs;
String msg;
String qry;


		//EXIT 1
		if (!BlockUndoMySQL.mysql_online)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockQuery",  ChatColor.WHITE + "MySQL offline.");
		return;
		}
	
		//EXIT 2
		if (executing)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockQuery",  ChatColor.WHITE + "Query Cancelled..");
		return;
		}
	
		qry = ChatColor.WHITE + "Location " + ChatColor.DARK_GRAY + "(" + ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GRAY + loca.getWorld().getName().toString() + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + GetNumber(loca.getBlockX(), ChatColor.DARK_GRAY, BlockUndo.format_4zeros, false)  + ", " + GetNumber(loca.getBlockY(), ChatColor.DARK_GRAY, BlockUndo.format_4zeros, false) + ", " + GetNumber(loca.getBlockZ(), ChatColor.DARK_GRAY, BlockUndo.format_4zeros, false) + ")";
	
		//EXIT 3
		if (loca.getBlockX() == LastLocation.getBlockX() && loca.getBlockY() == LastLocation.getBlockY() && loca.getBlockZ() == LastLocation.getBlockZ())
		{
		
		//BlockUndoLib.LogCommand(player.getName(), "Query: " + );
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockQuery", ChatColor.WHITE + player.getName() + ": Query: " + qry + " [cached]");

			if (LastQuery.toString().equalsIgnoreCase("No results."))
			{
			BlockUndoLib.Chat(player, "BlockQuery", LastQuery.toString());
			}
			else
			{
			msgs = LastQuery.toString().split(",");
			BlockUndoLib.Chat(player, "BlockQuery", qry);

				for(k = 0; k < msgs.length; k++)
				{
				BlockUndoLib.Chat(player, "BlockQuery", msgs[k]);
				}
			}
	
		return;
		}
	
		//NEW cooling off (ONLY ALLOW Querying every [3] game ticks) = 5 commands/sec
		if (estimatedTime < (150 * 1000000))
		{
		BlockUndoLib.Chat(player, "BlockQuery",  ChatColor.WHITE + "Cooling off.."); 
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockQuery", ChatColor.WHITE + player.getName() + ": Query: Cooling off..");
		//BlockUndoLib.LogCommand(player.getName(), ChatColor.WHITE + ");
		return;
		}
	
	LastQuery = new StringBuilder();
	
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockQuery", ChatColor.WHITE+  player.getName() + ": Query: " + qry);
	//BlockUndoLib.LogCommand(player.getName(), "Query: " + qry);
	LastLocation = loca;
	TransLock(); // ---->>
	res = BlockUndoMySQL.Query(sqls);

		try
		{
			if (res.equalsIgnoreCase("no results"))
			{
			msg = "No results.";
			LastQuery.append(msg);
			BlockUndoLib.Chat(player, "BlockQuery", msg);
			TransUnlock(); // <<----
			return;
			}
			else
			{
			BlockUndoLib.Chat(player, "BlockQuery", qry);
			}

			
			do
			{	
			c++;
			
			val = BlockUndoMySQL.mysql_res.getString("ex1");  //ERROR NULL POINTER -> Operation not allowed after resultset closed.
			split = val.split(","); 	
				if (split.length == 1) 
				{
				BlockUndoLib.Chat(player, "BlockQuery", split[0]);
				}
				else
				{
					
					if (split[1].equalsIgnoreCase("0"))
					{
					msg = Integer.toString(c) + ChatColor.GREEN + " " + split[3] + ChatColor.WHITE + " placed " + ChatColor.GRAY + Material.getMaterial(Integer.parseInt(split[2])) + ChatColor.WHITE + " " + split[0] + " days ago";
					LastQuery.append(msg + ",");
					BlockUndoLib.Chat(player, "BlockQuery", msg);
					}
					else
					{
					msg = Integer.toString(c) + ChatColor.GREEN + " "  + split[3] + ChatColor.WHITE + " removed " + ChatColor.GRAY + Material.getMaterial(Integer.parseInt(split[1])) + ChatColor.WHITE + " " + split[0] + " days ago";
					LastQuery.append(msg + ",");
					BlockUndoLib.Chat(player, "BlockQuery", msg);
					}

				}

			}
			while (BlockUndoMySQL.mysql_res.next());

		} 
		catch (NumberFormatException e1)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", " NumberFormatException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getCause() + "]", e1.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e1.getClass().getName() + "]", e1.getStackTrace().toString());
		} 
		catch (SQLException e2)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", " SQLException");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getErrorCode() + "]", e2.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e2.getClass().getName() + "]", e2.getStackTrace().toString());
		}
		catch (Exception e3)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[BlockUndo-CommitAllAsync]", " Exception");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getCause() + "]", e3.getMessage());
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "[" + e3.getClass().getName() + "]", e3.getStackTrace().toString());
		}

	TransUnlock(); // <<----

}
public static void RegenOres()
{

	int c = 0;
	c = BlockUndoMySQL.Update("UPDATE blocks SET `purge` = 'O'  WHERE b1val     IN(" + BlockUndoConfig.InfiniteOres + ")  AND DATEDIFF(now(), `entered`) >= " + BlockUndoConfig.RegenOreDays);
		if (c > 0)
		{
		c = UndoCore(BlockUndo.zPlugin.getServer().getConsoleSender(), "SELECT CONCAT_WS(',', b1val, x, y, z, b1dat, `name`) AS ex1 FROM blocks INNER JOIN worlds ON worlds.id = blocks.idworld WHERE `purge` = 'O' ORDER BY entered ASC;");
		BlockUndoMySQL.Update("DELETE FROM blocks WHERE `purge` = 'O'");
		}

		if (c> 0) BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo",  GetNumber(c, ChatColor.GREEN, BlockUndo.format_4zeros, true) + ChatColor.GRAY + " Ores regenerated");

}


//WRAPPERS
public static void UndoW(final CommandSender sender, final String player)
{
	
	BlockUndo.zPlugin.getServer().getScheduler().runTaskAsynchronously(BlockUndo.zPlugin, new Runnable()
	{
		public void run()
		{
		BlockUndoLib.Undo(sender, player);
		}

	});

}
public static void RedoW(final CommandSender sender, final String player)
{
	

	BlockUndo.zPlugin.getServer().getScheduler().runTaskAsynchronously(BlockUndo.zPlugin, new Runnable()
	{
		public void run()
		{
		BlockUndoLib.Redo(sender, player);
		}

	});

}
public static void PurgeW(final CommandSender sender)
{

	BlockUndo.zPlugin.getServer().getScheduler().runTaskAsynchronously(BlockUndo.zPlugin, new Runnable()
	{
		public void run()
		{
		BlockUndoLib.Purge(sender);
		}

	});

}
public static void VaporW(final CommandSender sender)
{

	BlockUndo.zPlugin.getServer().getScheduler().runTaskAsynchronously(BlockUndo.zPlugin, new Runnable()
	{
		public void run()
		{
		BlockUndoLib.Vapor(sender);
		}

	});

}
public static void WaterW(final CommandSender sender)
{

	BlockUndo.zPlugin.getServer().getScheduler().runTaskAsynchronously(BlockUndo.zPlugin, new Runnable()
	{
		public void run()
		{
		BlockUndoLib.FixWater(sender);
		}

	});

}
public static void GiveQ(final CommandSender sender, final Player player)
{

	PlayerInventory inventory;
	ItemStack MyItemStack = new ItemStack(7, 1, (byte)0);
	inventory = BlockUndo.PLAYA.getInventory();
	inventory.addItem(MyItemStack);
	MyItemStack = new ItemStack(285, 1, (byte)0);
	inventory = BlockUndo.PLAYA.getInventory();
	inventory.addItem(MyItemStack);

}


}