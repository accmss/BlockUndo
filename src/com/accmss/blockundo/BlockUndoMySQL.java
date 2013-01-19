package com.accmss.blockundo;


//IMPORTS - JAVA
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;


public class BlockUndoMySQL {


public static Connection mysql_con = null;
public static Statement mysql_sta = null;
public static ResultSet mysql_res = null;

public static int mysql_int1 = 0;
public static int mysql_int2 = 0;
public static String mysql_str1 = "";
public static String mysql_str2 = "";


public static boolean mysql_online = false;
public static String mysql_version = "";


public static String[] mysql_worlds = null;
public static int mysql_players = 0;
public static int mysql_blocks = 0;


static String del_players = "DROP TABLE IF EXISTS `blockundo`.`players`;";
static String del_delblocks = "DROP TABLE IF EXISTS `blockundo`.`blocks`;";


static String create_players = "CREATE TABLE  `blockundo`.`players` (\n"
		+ "  `id` INTEGER  NOT NULL AUTO_INCREMENT,\n"
		+ "  `name` varchar(64) NOT NULL,\n"
		+ "  `ip` varchar(16) NOT NULL,\n"
		+ "  `entered` datetime NOT NULL,\n"
		+ "  PRIMARY KEY (`name`),\n"
		+ "  KEY `Index_1` (`id`) USING BTREE\n"
		+ ") ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;";

static String create_worlds = "CREATE TABLE  `blockundo`.`worlds` (\n"
		+ "  `id` INTEGER  NOT NULL AUTO_INCREMENT,\n"
		+ "  `name` varchar(64) NOT NULL,\n"
		+ "  PRIMARY KEY (`name`),\n"
		+ "  KEY `Index_1` (`id`) USING BTREE\n"
		+ ") ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;";

static String create_blocks = "CREATE TABLE  `blockundo`.`blocks` (\n"
		+ "  `idplayer` INTEGER  NOT NULL,\n"
		+ "  `idworld` INTEGER  NOT NULL,\n"
		+ "  `x` INTEGER  NOT NULL,\n"
		+ "  `y` INTEGER  NOT NULL,\n"
		+ "  `z` INTEGER  NOT NULL,\n"
		+ "  `b1val` INTEGER  NOT NULL,\n"
		+ "  `b1dat` INTEGER NOT NULL,\n"
		+ "  `b2val` INTEGER NOT NULL,\n"
		+ "  `b2dat` INTEGER NOT NULL,\n"
		+ "  `entered` datetime NOT NULL,\n"
		+ "  `purge` varchar(1) NOT NULL DEFAULT 'N',\n"
		+ "   PRIMARY KEY (`idplayer`,`idworld`,`x`,`y`,`z`) USING BTREE,\n"
		+ "  KEY `FK_blocks_1` (`idplayer`),\n"
		+ "  CONSTRAINT `FK_blocks_1` FOREIGN KEY (`idplayer`) REFERENCES `players` (`id`),\n"
		+ "  CONSTRAINT `FK_blocks_2` FOREIGN KEY (`idworld`) REFERENCES `worlds` (`id`)\n"
		+ "  ) ENGINE=InnoDB DEFAULT CHARSET=latin1;";


public static String mysql_url = "jdbc:mysql://" + BlockUndoSettings.MySQLMachine + ":" + BlockUndoSettings.MySQLTCPPort;// +"/" + BlockUndo.mysql_dbse;



public static void Connect()  {

		try
		{
		//CONNECT
		//Class.forName("com.mysql.jdbc.Driver"); 
		mysql_con = DriverManager.getConnection(mysql_url, BlockUndoSettings.MySQLUserAcc, BlockUndoSettings.MySQLPasswrd);
		mysql_sta  = mysql_con.createStatement();
		mysql_online = true;
				
		//GENERATE SCHEMA
		mysql_int1 = SafeUpdate("USE " + BlockUndoSettings.MySQLDatabse  + ";", "CREATE DATABASE " + BlockUndoSettings.MySQLDatabse + ";", "USE " + BlockUndoSettings.MySQLDatabse  + ";");
		mysql_str1 = SafeQuery("SELECT count(id) FROM worlds;", create_worlds.replaceAll("blockundo", BlockUndoSettings.MySQLDatabse), "ALTER TABLE worlds AUTO_INCREMENT = 1;");
		mysql_str1 = SafeQuery("SELECT count(id) FROM players;", create_players.replaceAll("blockundo", BlockUndoSettings.MySQLDatabse), "ALTER TABLE players AUTO_INCREMENT = 1;");
		mysql_str2 = SafeQuery("SELECT count(idplayer) FROM blocks;", create_blocks.replaceAll("blockundo", BlockUndoSettings.MySQLDatabse), "ALTER TABLE blocks AUTO_INCREMENT = 1;");
		
		//SYNC WORLDS
		BlockUndoLib.AddWorlds();
		
		//STATS
		mysql_version = Query("SELECT version();");
		mysql_worlds = Query("SELECT group_concat(concat_ws(';', name, id)) FROM worlds;").split(",");
		mysql_players = Integer.parseInt(Query("SELECT COUNT(id) FROM players;"));
		mysql_blocks = Integer.parseInt(Query("SELECT COUNT(idplayer) FROM blocks;"));

		}
		catch (NumberFormatException e1)
		{
		BlockUndoLib.LogMessage("[BlockUndo-Connect]", " NumberFormatException");
		BlockUndoLib.LogMessage("[" + e1.getCause() + "]", e1.getMessage());
		BlockUndoLib.LogMessage("[" + e1.getClass().getName() + "]", e1.getStackTrace().toString());
		mysql_online = false;
		return;
		} 
		catch (SQLException e2)
		{
		BlockUndoLib.LogMessage("[BlockUndo-Connect]", " SQLException");
		BlockUndoLib.LogMessage("[" + e2.getErrorCode() + "]", e2.getMessage());
		BlockUndoLib.LogMessage("[" + e2.getClass().getName() + "]", e2.getStackTrace().toString());
		mysql_online = false;
		return;
		}
		catch (Exception e3)
		{
		BlockUndoLib.LogMessage("[BlockUndo-Connect]", " Exception");
		BlockUndoLib.LogMessage("[" + e3.getCause() + "]", e3.getMessage());
		BlockUndoLib.LogMessage("[" + e3.getClass().getName() + "]", e3.getStackTrace().toString());
		mysql_online = false;
		return;
		}

	mysql_online = true;
	return;

}
public static void CloseST() {

    try
    {
        if (mysql_sta != null) 
        {
        BlockUndoLib.LogMessage("[BlockUndo]", "Closing statement");
        mysql_sta.close();
        }
    }
   catch (Exception ex)
   {
   BlockUndoLib.LogMessage(Level.SEVERE.toString(), ex.getCause() + " : " + ex.getMessage());
   }

}
public static void CloseRS() {

    try
    {
        if (mysql_res != null) 
        {
        mysql_res.close();
        
        }
    }
   catch (Exception ex)
   {
   BlockUndoLib.LogMessage(Level.SEVERE.toString(), ex.getCause() + " : " + ex.getMessage());
   }	
	
}
public static void CloseCon() {

    try
    {
        if (mysql_con != null) 
        {
        BlockUndoLib.LogMessage("[BlockUndo]", "Closing connection");
        mysql_con.close();
        }
    }
   catch (Exception ex)
   {
   BlockUndoLib.LogMessage(Level.SEVERE.toString(), ex.getCause().toString() + " : " + ex.getMessage());
   }
	
}
public static String Query(String query) {


    try
    {
    	mysql_res = null;
    	mysql_res  = mysql_sta.executeQuery(query);
    
	    if (mysql_res.next())
	    {
		return mysql_res.getString(1);
	    }
	    else
	    {
		return "no results";
	    }
	    
    }
	catch (NumberFormatException e1)
	{
	BlockUndoLib.LogMessage("[BlockUndo-Query]", " NumberFormatException");
	BlockUndoLib.LogMessage("[" + e1.getCause() + "]", e1.getMessage());
	BlockUndoLib.LogMessage("[" + e1.getClass().getName() + "]", e1.getStackTrace().toString());
    return e1.getMessage();
	} 
	catch (SQLException e2)
	{
	BlockUndoLib.LogMessage("[BlockUndo-Query]", " SQLException");
	BlockUndoLib.LogMessage("[" + e2.getErrorCode() + "]", e2.getMessage());
	BlockUndoLib.LogMessage("[" + e2.getClass().getName() + "]", e2.getStackTrace().toString());
    return e2.getMessage();
	}
	catch (Exception e3)
	{
	BlockUndoLib.LogMessage("[BlockUndo-Query]", " Exception");
	BlockUndoLib.LogMessage("[" + e3.getCause() + "]", e3.getMessage());
	BlockUndoLib.LogMessage("[" + e3.getClass().getName() + "]", e3.getStackTrace().toString());
    return e3.getMessage();
	}
    

}
public static int Update(String query) {

    try
    {
    mysql_int1  = mysql_sta.executeUpdate(query);
	return mysql_int1;
    }
	catch (NumberFormatException e1)
	{
	BlockUndoLib.LogMessage("[BlockUndo-Update]", " NumberFormatException");
	BlockUndoLib.LogMessage("[" + e1.getCause() + "]", e1.getMessage());
	BlockUndoLib.LogMessage("[" + e1.getClass().getName() + "]", e1.getStackTrace().toString());
    return mysql_int1;
	} 
	catch (SQLException e2)
	{
	BlockUndoLib.LogMessage("[BlockUndo-Update]", " SQLException");
	BlockUndoLib.LogMessage("[" + e2.getErrorCode() + "]", e2.getMessage());
	BlockUndoLib.LogMessage("[" + e2.getClass().getName() + "]", e2.getStackTrace().toString());
    return e2.getErrorCode();
	}
	catch (Exception e3)
	{
	BlockUndoLib.LogMessage("[BlockUndo-Update]", ": Exception");
	BlockUndoLib.LogMessage("[" + e3.getCause() + "]", e3.getMessage());
	BlockUndoLib.LogMessage("[" + e3.getClass().getName() + "]", e3.getStackTrace().toString());
    return mysql_int1;
	}

}
public static int SafeUpdate(String query1, String query2, String query3) {

    try
    {
    mysql_int1  = mysql_sta.executeUpdate(query1);
	return mysql_int1;
    }
    catch (SQLException ex1)
    {
    BlockUndoLib.LogMessage("[BlockUndo-SafeUpdate]", " SQLException");
    BlockUndoLib.LogMessage("[" + ex1.getErrorCode() + "]", ex1.getMessage());
    BlockUndoLib.LogMessage("[" + ex1.getClass().getName() + "]", ex1.getStackTrace().toString());
	
   	   try
	   {
   	   mysql_int1  = mysql_sta.executeUpdate(query2);
   	   mysql_int1  = mysql_sta.executeUpdate(query3);
	   return mysql_int1;
	   }
	   catch (SQLException ex2)
	   {
	   BlockUndoLib.LogMessage("[BlockUndo-SafeUpdate]", " SQLException");
	   BlockUndoLib.LogMessage("[" + ex2.getErrorCode() + "]",  ex2.getMessage());
	   BlockUndoLib.LogMessage("[" + ex2.getClass().getName() + "]", ex2.getStackTrace().toString());
	   return ex2.getErrorCode();
	   }

   }	

}
public static String SafeQuery(String query1, String query2, String query3) {

	try
	{
		mysql_res = mysql_sta.executeQuery(query1);
	    if (mysql_res.next())
	    {
		return mysql_res.getString(1);
	    }
	    else
	    {
		return "no results";
		}

    }
	catch (SQLException e2)
	{
	BlockUndoLib.LogMessage("[BlockUndo-SafeQuery]", " SQLException");
	BlockUndoLib.LogMessage("[" + e2.getErrorCode() + "]", e2.getMessage().toString());
	BlockUndoLib.LogMessage("[" + e2.getClass().getName() + "]", e2.getStackTrace().toString());

   	   try
	   {
   	   mysql_int1  = mysql_sta.executeUpdate(query2);
   	   mysql_int1  = mysql_sta.executeUpdate(query3);
	   return "success";
	   }
	   catch (SQLException e3)
	   {
	   BlockUndoLib.LogMessage("[BlockUndo-SafeQuery]", " SQLException");
	   BlockUndoLib.LogMessage("[" + e3.getErrorCode() + "]", e3.getMessage());
	   BlockUndoLib.LogMessage("[" + e3.getClass().getName() + "]", e3.getStackTrace().toString());
	   return e3.getMessage();
	   }	
 
	}
    


}
public static void Disconnenct() {

	CloseST();
	CloseRS();
	CloseCon();

}


}