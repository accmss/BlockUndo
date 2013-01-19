package com.accmss.blockundo;


//IMPORT - JAVA
import java.io.File;
import java.util.List;

/*
# BlockUndo

Database:
  MySQLMachine: 127.0.0.1
  MySQLTCPPort: 3306
  MySQLDatabse: blockundo
  MySQLUserAcc: root
  MySQLPasswrd:''
Worlds:
  - world
  - world_nether
  - world_the_end
Regeneration:
  InfiniteOres: 14,15,16,56,73,82,74,129,153
  RegenOreDays: 1
Version:
  ConfigYMLVer: 1
 */


//SYNC TO VERSION: 1


public class BlockUndoSettings {

	//VARS - SETTINGS
public static String MySQLMachine = null;
public static String MySQLTCPPort = null;
public static String MySQLDatabse = null;
public static String MySQLUserAcc = null;
public static String MySQLPasswrd = null;
                     
public static List<String>  Worlds = null;

public static String InfiniteOres = null;
public static int RegenOreDays = 0;

public static int ConfigYMLVer = 0;


//VARS - SETTINGS
public static String SlashChar = null;
public static int SyncVers = 1;
	
public static void LoadSettings(String file)
{

	//Slash
	SetSlash(file);

	//Ensure config
	File f = new File("plugins" + SlashChar + "BlockUndo" + SlashChar + "config.yml");
		if(!f.exists())
		{ 
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "§fWriting new configuration.yml.");
		CreateConfig();
		}
		else
		{
		BlockUndo.zConfig = BlockUndo.zPlugin.getConfig();
		}

	//Update config
	ConfigYMLVer = BlockUndo.zConfig.getInt("Version.ConfigYMLVer", ConfigYMLVer);
		if(ConfigYMLVer != SyncVers)
		{ 
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "§fUpdating new configuration.yml.");
		CreateConfig();
		BlockUndo.zConfig.set("Version.ConfigYMLVer", SyncVers);
		BlockUndo.zPlugin.saveConfig();
		}

	//1 MySQL
	MySQLMachine = BlockUndo.zConfig.getString("Database.MySQLMachine", MySQLMachine);
	MySQLTCPPort = BlockUndo.zConfig.getString("Database.MySQLTCPPort", MySQLTCPPort);
	MySQLDatabse = BlockUndo.zConfig.getString("Database.MySQLDatabse", MySQLDatabse);
	MySQLUserAcc = BlockUndo.zConfig.getString("Database.MySQLUserAcc", MySQLUserAcc);
	MySQLPasswrd = BlockUndo.zConfig.getString("Database.MySQLPasswrd", MySQLPasswrd);

	//2 World
	Worlds = BlockUndo.zConfig.getStringList("Worlds");

	//3 Regeneration
	InfiniteOres = BlockUndo.zConfig.getString("Regeneration.InfiniteOres", InfiniteOres);
	RegenOreDays = BlockUndo.zConfig.getInt("Regeneration.RegenOreDays", RegenOreDays);

}
public static void SetSlash(String file)
{

	if (file.contains("/"))
	{
	SlashChar = "/"; //Linux
	}
	else
	{
	SlashChar = "\\"; //Windows
	}
	
}
public static void CreateConfig()
{

		try
		{
		BlockUndo.zPlugin.saveDefaultConfig();
		BlockUndo.zConfig = BlockUndo.zPlugin.getConfig();
		} catch (Exception e) {
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "§fWriting new configuration.yml failed!");
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "§4" + e.getCause() + ": " +  e.getMessage());
		}

}


}
