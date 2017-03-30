import java.awt.TextArea;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Database class which handles all database calls
 * @author David
 *
 */

public class Database {

   private static String dbURL = "jdbc:mysql://localhost:3306/testDB";
   private static String tableName = "inventory";
   private static Connection conn = null;
   private static Statement stmt = null;
   private static Properties properties = new Properties();
   TextArea text;

   /**
    * When the database is created, create a connection and the table required for it
    */
   public Database() {
      createConnection();
      createTable();
   }
   
   /**
    * connect to the DB using parameters below and dbURL
    */
   private static void createConnection() {
      properties.setProperty("user", "testuser"); // user
      properties.setProperty("password", "test"); // pass
      properties.setProperty("useSSL", "false"); // SSL false
      properties.setProperty("createDatabaseIfNotExist","true"); // create DB if it doesn't exist
      try {
         Class.forName("com.mysql.jdbc.Driver");
         conn = DriverManager.getConnection(dbURL, properties);
      } catch (Exception except) {
         except.printStackTrace();
      }
   }
   
   /**
    * create tables for the project
    */
   private void createTable() {
      try {
         stmt = conn.createStatement();
         stmt.execute("create table if not exists "
               + tableName
               + "(isbn varchar(15) not null primary key,"
               + "author_first varchar(20) not null,"
               + "author_last varchar(20) not null,"
               + "title varchar(50) not null,"
               + "pub_year varchar(4),"
               + "publisher varchar(30),"
               + "quantity int,"
               + "purchase_date date,"
               + "system_date date,"
               + "modify_date date)");
         
         stmt.close();
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
      }
   }
   
   /**
    * helper to insert
    * @param s A string that contains inventory information 
    * formatted as ibsn;first;last;title;year;publisher;quantity
    * @return true if success false otherwise
    */
   public boolean insertInventory(String s) {
      return insert(s);
   }
   
   /**
    * Insert player to DB
    * @param s passed in by insertInventory method
    * @return true if success false otherwise
    */
   private static boolean insert(String s) {
      String[] inventory = s.split(";"); // split the semi-colons
      for (int i = 0; i < inventory.length; i++) { // replace extra spaces and single quotes with double quotes for sql syntax
         if(i != 3 && i != 5)
            inventory[i] = inventory[i].replaceAll("\\s","");
         inventory[i] = inventory[i].replaceAll("'","''");
      }
      try {
         stmt = conn.createStatement();
         Calendar currenttime = Calendar.getInstance();
         Date date = new Date((currenttime.getTime()).getTime());
         stmt.execute("insert into " 
               + tableName + " (isbn, author_first, author_last, title, pub_year, publisher, quantity, system_date) values ('" 
               + inventory[0] + "','" 
               + inventory[1] + "','" 
               + inventory[2] + "','"
               + inventory[3] + "','"
               + inventory[4] + "','"
               + inventory[5] + "',"
               + inventory[6] + ",'"
               + date + "')");
//         writeToLog("INSERT", playerInfo); // write to log
         stmt.close();
         return true;
      } catch (SQLIntegrityConstraintViolationException e) {
         return false;
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
         return false;
      }
   }
   
   // helper for getForGUi
   public void getInventoryForGUI(TextArea t) {
      text = t;
      getForGUI(text);
   }

   /**
    * This method retrieves all rows in the DB and displays them on the GUI
    * @param t the text area for the GUI
    */
   private static void getForGUI(TextArea t) {
      try {
         stmt = conn.createStatement();
         ResultSet results = stmt.executeQuery("select * from " + tableName);
         t.setText("");
         while(results.next()) {
            t.append("ISBN: " + results.getString("isbn")+" ");
            t.append("Author: " + results.getString("author_first")+" "+ results.getString("author_last")+" ");
            t.append("Title: " + results.getString("title")+" ");
            t.append("Publish Date: " + results.getString("pub_year"));
            t.append("Publisher: " + results.getString("publisher"));
            t.append("Quantity: " + Integer.toString(results.getInt("quantity"))+" ");
            t.append("\n");
            t.append("Purchased: " + results.getString("purchase_date") + " ");
            t.append("System: " + results.getString("system_date") + " ");
            t.append("Modified: " + results.getString("modify_date"));
            t.append("\n");
         }
         stmt.close();
         results.close();
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
      }
   }
   
   // helper for getPlayer
//   public String getPlayerInfo(String player) {
//      return getPlayer(player);
//   }
   
   /**
    * This method retrieves a single player based on player name from the database
    * @param player The player name required for the query
    * @return a string that will be inserted into the input dialog for instructions otherwise None if there is no player
    */
//   private static String getPlayer(String player) {
//      try {
//         stmt = conn.createStatement();
//         ResultSet results = stmt.executeQuery("select * from " + tableName
//               + " where name =" + "'" + player + "'");
//         String result = null;
//         while(results.next()) {
//            result = results.getString("position") + ", " + Integer.toString(results.getInt("age"))
//                  + ", " + results.getString("team") + ", " + results.getString("height");
//         }
//         if (result != null)
//            return result;
//         else return "None";
//      } catch (SQLException sqlExcept) {
//         sqlExcept.printStackTrace();
//         return "None";
//      }
//   }
   
   // helper to update
//   public boolean updatePlayer(String pName, String pInfo) {
//      return update(pName, pInfo);
//   }
//   
   /**
    * This method updates a row on the database based on player name user enters
    * @param pName Name of the player
    * @param pInfo Other player information
    * @return true if success otherwise false
    */
//   public boolean update(String pName, String pInfo) {
//      String[] playerInfo = pInfo.split(",");
//      for (int i = 1; i < playerInfo.length; i++) {
//         playerInfo[i] = playerInfo[i].replaceAll("\\s","");
//         playerInfo[i] = playerInfo[i].replaceAll("'","''");
//      }
//      try {
//         stmt = conn.createStatement();
//         stmt.execute("update " + tableName + " set position='" + playerInfo[0]
//               + "',age=" + playerInfo[1] + ",team='" + playerInfo[2] 
//               + "',height='" + playerInfo[3] + "' where name='" +pName +"'");
//         stmt.close();
//         writeToLog("UPDATE for " + pName, playerInfo);
//         return true;
//      } catch (SQLException sqlExcept) {
//         sqlExcept.printStackTrace();
//         return false;
//      }
//   }
//   
//   // helper to remove
//   public boolean removePlayer(String pName) {
//      return remove(pName);
//   }
//   
   /**
    * This method deletes a row based on player name user enters from the database
    * @param pName player name required to execute statement
    * @return true if success otherwise false
    */
//   private static boolean remove(String pName) {
//      try {
//         stmt = conn.createStatement();
//         stmt.execute("delete from " + tableName + " where name='" + pName + "'");
//         stmt.close();
//         String[] player = new String[1];
//         player[0] = pName;
//         writeToLog("DELETE", player);
//         return true;
//      } catch (SQLException sqlExcept) {
//         sqlExcept.printStackTrace();
//         return false;
//      }    
//   }
   
   // helper to shutDown
   public void shutdown() {
      shutDown();
   }
   
   /**
    * Shuts down the DB when exited
    */
   private static void shutDown() {
       try {
           if (stmt != null) {
              stmt.close();
           }
           if (conn != null) {
              properties.setProperty("shutdown", "true");
              DriverManager.getConnection(dbURL, properties);
              conn.close();
           }           
       }
       catch (SQLException sqlExcept){   
          sqlExcept.printStackTrace();
       }
   }
   
   /**
    * This method is called after every successful DB transactional statement
    * @param transaction name of the transaction
    * @param parameters name relevant parameters
    */
//   private static void writeToLog(String transaction, String[] parameters) {
//      String currentDir = System.getProperty("user.dir") + "\\logfile.txt"; // current directory's logfile
//      BufferedWriter writer = null;
//      try {
//         Date dnow = new Date();
//         SimpleDateFormat ft = new SimpleDateFormat("E MM/dd/yyyy hh:mm:ss a");
//         writer = new BufferedWriter(new FileWriter(currentDir,true)); // create a new or append to current directory's logfile
//         if (transaction == "INSERT") {
//            writer.write("\n" + ft.format(dnow) + " " + transaction + " into "
//                  + tableName + ": ");
//         }
//         else {
//            writer.write("\n" + ft.format(dnow) + " " + transaction + " from "
//                  + tableName + ": ");
//         }
//         for (int i = 0; i < parameters.length; i++) { // write parameters
//            if (i == parameters.length - 1)
//               writer.write(parameters[i]);
//            else 
//               writer.write(parameters[i] + ", ");
//         }
//      } catch (Exception e) {
//         e.printStackTrace();
//      } finally {
//         try {
//            writer.close();
//         } catch (Exception e) {
//            e.printStackTrace();
//         }
//      }
//   }
}