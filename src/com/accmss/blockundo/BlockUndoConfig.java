package com.accmss.blockundo;


//IMPORT - JAVA
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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


public class BlockUndoConfig {

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
public static int SyncVers = 2;
	
public static void LoadSettings(String file)
{

	//Slash
	SetSlash(file);

	//Ensure config
	EnsureConfig();

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
private static void EnsureConfig()
{

	File fileDir = new File("plugins" + SlashChar + "BlockUndo");
	String zFile = "plugins" + SlashChar + "BlockUndo" + SlashChar + "config.yml";
	File f = new File(zFile);

		//Directory
		if (!fileDir.exists())
		{
		fileDir.mkdir();
		}

		//File
		if(!f.isFile())
		{ 
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "§fWriting new configuration.yml.");
		CreateConfig(zFile);
		}
		else
		{
		BlockUndo.zConfig = BlockUndo.zPlugin.getConfig();
		}

		//Update
	    try
	    {ConfigYMLVer = BlockUndo.zConfig.getInt("Version.ConfigYMLVer", ConfigYMLVer);}
		catch (Exception e)
		{ConfigYMLVer = 0;}
	    
		if(ConfigYMLVer != SyncVers)
		{
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "§fUpdating new configuration.yml...");
		CreateConfig(zFile);
		}

}
private static void CreateConfig(String file) 
{

	try
	{
	InputStream is = BlockUndo.zPlugin.getClass().getResourceAsStream("/config.yml");
	OutputStream os = new FileOutputStream(file);  
	byte[] buffer = new byte[4096];  
	int bytesRead;  
		while ((bytesRead = is.read(buffer)) != -1)
		{  
		os.write(buffer, 0, bytesRead);  
		}  
	is.close();  
	os.close(); 
	BlockUndo.zConfig = BlockUndo.zPlugin.getConfig();
	} catch (Exception e) {
		BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "§fWriting new configuration.yml failed!");
	BlockUndoLib.Chat(BlockUndo.zPlugin.getServer().getConsoleSender(), "BlockUndo", "§4" + e.getCause() + ": " +  e.getMessage());
	}
	

}


}
