import java.awt.TextArea;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import javax.swing.JOptionPane;

/**
 * Database class which handles all database calls
 * @author David
 *
 */

public class Database {

   private static String dbURL = "jdbc:mysql://localhost:3306/testDB";
   private static String tableName = "inventory";
   private static String accountsTable = "accounts";
   private static String infoTable = "information";
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
    * create tables for the app
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
               + "purchase_date varchar(30),"
               + "system_date varchar(30),"
               + "modify_date varchar(30))");
         stmt.execute("create table if not exists "
               + accountsTable
               + "(user varchar(20) not null,"
               + "pass varchar(20) not null)");
         stmt.execute("create table if not exists "
               + infoTable
               + "(isbn varchar(15) not null primary key,"
               + "author varchar(50),"
               + "title varchar(50),"
               + "publisher varchar(70),"
               + "series varchar(40),"
               + "pages varchar(20),"
               + "language varchar(30),"
               + "shipping_wt varchar(15),"
               + "used_price varchar(10),"
               + "new_price varchar(10),"
               + "collectible_price varchar(10))");
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
    * insert inventory information to the DB
    * @param s passed in by insertInventory method
    * @return true if success false otherwise
    */
   private static boolean insert(String s) {
      String[] inventory = s.split(";"); // split the semi-colons
      for (int i = 0; i < inventory.length; i++) { // replace extra spaces and single quotes with double quotes for sql syntax
         if(i != 3 && i != 5) // Don't do for Titles and Publisher names
            inventory[i] = inventory[i].replaceAll("\\s","");
         inventory[i] = inventory[i].replaceAll("'","''");
      }
      try {
         String previousRow = getInventoryForLog(inventory[0]); // used for log
         stmt = conn.createStatement();
         Date dnow = new Date();
         SimpleDateFormat ft = new SimpleDateFormat("E MM/dd/yyyy hh:mm:ss a");
         // insert now
         stmt.execute("insert into " 
               + tableName + " (isbn, author_first, author_last, title, pub_year, publisher, quantity, system_date) values ('" 
               + inventory[0] + "','" 
               + inventory[1] + "','" 
               + inventory[2] + "','"
               + inventory[3] + "','"
               + inventory[4] + "','"
               + inventory[5] + "',"
               + inventory[6] + ",'"
               + ft.format(dnow) + "')");
         writeToLog("INSERT", inventory, previousRow); // write to log
         stmt.close();
         return true;
      } catch (SQLIntegrityConstraintViolationException e) { // If row already exists then update quantity
         try {
            String previousRow = getInventoryForLog(inventory[0]);
            Date dnow = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("E MM/dd/yyyy hh:mm:ss a");
            stmt = conn.createStatement();
            // update now
            stmt.execute("update " + tableName + " set quantity=quantity+" + inventory[6]
                  + ", modify_date='" + ft.format(dnow) + "' where isbn='" + inventory[0] +"'");
            stmt.close();
            writeToLog("UPDATE for " + inventory[0], inventory, previousRow); // write to log
            JOptionPane.showMessageDialog(null, "Row " + s + " quantity has been updated");
            return true;  
         } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to insert at least one line of your file! Try again");
            return false;
         }
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
         JOptionPane.showMessageDialog(null, "Failed to insert at least one line of your file! Try again");
         return false;
      }
   }
   
   // helper for getForGUi
   public void getInventoryForGUI(TextArea t) {
      text = t;
      getForGUI(text);
   }

   /**
    * This method retrieves all rows from the inventory table in the DB and displays them on the GUI
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
            t.append("Publish Date: " + results.getString("pub_year")+" ");
            t.append("Publisher: " + results.getString("publisher")+ " ");
            t.append("Quantity: " + Integer.toString(results.getInt("quantity"))+" ");
            t.append("\n");
            t.append("Purchased: " + results.getString("purchase_date") + " ");
            t.append("Created: " + results.getString("system_date") + " ");
            t.append("Modified: " + results.getString("modify_date"));
            t.append("\n\n");
         }
         stmt.close();
         results.close();
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
      }
   }
   
   // helper for getInventory
   public String getInventoryInfo(String isbn) {
      return getInventory(isbn);
   }
   
   /**
    * This method retrieves a single inventory row based on isbn from the database
    * @param i The isbn number required for the query
    * @return a string that will be inserted into the input dialog for instructions otherwise None if there is no such inventory
    */
   private static String getInventory(String i) {
      try {
         stmt = conn.createStatement();
         ResultSet results = stmt.executeQuery("select * from " + tableName
               + " where isbn =" + "'" + i + "'");
         String result = null;
         while(results.next()) {
            result = results.getString("isbn") + "; " + results.getString("author_first") + "; "
                  + results.getString("author_last") + "; " + results.getString("title") + "; "
                  + results.getString("pub_year") + "; " + results.getString("publisher") + "; "
                  + Integer.toString(results.getInt("quantity"));
         }
         if (result != null)
            return result;
         else return "None";
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
         return "None";
      }
   }
   
   
   /**
    * get inventory information values for log
    * @param isbn isbn # to search
    * @return "None" if no result otherwise the inventory row as a string
    */
   private static String getInventoryForLog(String isbn) {
      try {
         stmt = conn.createStatement();
         ResultSet results = stmt.executeQuery("select * from " + tableName
               + " where isbn =" + "'" + isbn + "'");
         String result = null;
         while(results.next()) {
            result = results.getString("isbn") + "; " + results.getString("author_first") + "; "
                  + results.getString("author_last") + "; " + results.getString("title") + "; "
                  + results.getString("pub_year") + "; " + results.getString("publisher") + "; "
                  + Integer.toString(results.getInt("quantity")) + "; " + results.getString("purchase_date") +";"
                  + results.getString("system_date") + "; " + results.getString("modify_date");
         }
         if (result != null)
            return result;
         else return "None";
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
         return "None";
      }
   }
   
   // helper to update
   public boolean updateInventory(String isbnN, String newInfo) {
      return update(isbnN, newInfo);
   }
   
   /**
    * This method updates a row on the database based on the isbn user enters
    * @param isbn isbn of inventory
    * @param info New info
    * @return true if success otherwise false
    */
   public boolean update(String isbn, String info) {
      // split string and also replace white space and single quotes with two single quotes
      String[] invInfo = info.split(";");
      for (int i = 1; i < invInfo.length; i++) {
         if(i != 3 && i != 5)
            invInfo[i] = invInfo[i].replaceAll("\\s","");
         invInfo[i] = invInfo[i].replaceAll("'","''");
      }
      try {
         Date dnow = new Date();
         SimpleDateFormat ft = new SimpleDateFormat("E MM/dd/yyyy hh:mm:ss a");
         String previousRow = getInventoryForLog(isbn); // previous values for log
         stmt = conn.createStatement();
         // update now
         stmt.execute("update " + tableName + " set isbn='" + invInfo[0]
               + "',author_first='" + invInfo[1] + "',author_last='" + invInfo[2] 
               + "',title='" + invInfo[3] + "',pub_year='" + invInfo[4]
               + "',publisher='" + invInfo[5]+ "',quantity=" + invInfo[6]
               + ",modify_date='" + ft.format(dnow) + "' where isbn='" +isbn +"'");
         stmt.close();
         writeToLog("UPDATE for " + isbn, invInfo, previousRow); // write to log
         return true;
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
         return false;
      }
   }
   
   // helper to remove
   public boolean removeInventory(String i) {
      return remove(i);
   }
   
   /**
    * This method deletes a row based on an isbn user enters from the database
    * @param isbn isbn number required to execute statement
    * @return true if success otherwise false
    */
   private static boolean remove(String isbn) {
      try {
         String previousRow = getInventoryForLog(isbn); // previous values for log
         stmt = conn.createStatement();
         // delete now
         stmt.execute("delete from " + tableName + " where isbn='" + isbn + "'");
         stmt.close();
         String[] inventory = new String[1];
         inventory[0] = isbn;
         writeToLog("DELETE", inventory, previousRow); // write to log
         return true;
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
         return false;
      }    
   }
   
   // helper to register
   public boolean registerinDB(String u, String p) {
      return register(u,p);
   }
   
   /**
    * Register using username and password
    * @param user username
    * @param pass password
    * @return true if success false otherwise
    */
   private boolean register(String user, String pass) {
      try {
         stmt = conn.createStatement();
         // create account 
         stmt.execute("insert into " 
               + accountsTable + " (user, pass) values ('" 
               + user + "','" 
               + pass + "')");
         stmt.close();
         return true;
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
         return false;
      }
   }
   
   // helper to login
   public boolean dbLogin(String u, String p) {
      return login(u,p);
   }
   
   /**
    * Login using user and pass
    * @param user username
    * @param pass password
    * @return true if success false otherwise
    */
   private boolean login(String user, String pass) {
      try {
         stmt = conn.createStatement();
         // find the associated user/pass in DB
         ResultSet results = stmt.executeQuery("select * from " + accountsTable 
               + " where user='" + user + "' and pass='" + pass + "'");
         if(!results.next()) { // if no result return false
            stmt.close();
            results.close();
            return false;  
         }
         else {
            stmt.close();
            results.close();
            return true;
         }
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
         return false;
      }
   }
   
   // helper to reconstruct
   public boolean reconstructDB(List<String> t) {
      return reconstruct(t);
   }
   
   /**
    * This method reconstructs the DB based on the current transactional log "transactions.txt"
    * @param transactions List of the transactions
    * @return true on success false otherwise
    */
   private boolean reconstruct(List<String> transactions) {
      for(int i = transactions.size()-1; i >= 0; i--) {
         if (transactions.get(i) == "TRANSACTIONAL LOG") // Header
            continue;
         else {
            String line = transactions.get(i);
            if (line.contains("INSERT")) { // if line has INSERT as the statement
               line = line.substring(line.lastIndexOf("inventory:") + 10); // get values
               String isbn = line.substring(0, line.indexOf(',')-1); 
               isbn = isbn.replaceAll("\\s","");
               try {
                  stmt = conn.createStatement();
                  // reverse insert by deletion
                  stmt.execute("delete from " + tableName + " where isbn='" + isbn + "'");
                  stmt.close();
               } catch (SQLException sqlExcept) {
                  sqlExcept.printStackTrace();
                  return false;
               }    
            }
            else if (line.contains("UPDATE")) { // if line has UPDATE
               line = transactions.get(i+1); // go to the saved previous values before the update that is stored on the log
               line = line.substring(line.lastIndexOf("Previous Values:") + 16); // get values
               String[] invInfo = line.split(";");
               for (int j = 1; j < invInfo.length; j++) {
                  if(j != 3 && j != 5)
                     invInfo[j] = invInfo[j].replaceAll("\\s","");
                  invInfo[j] = invInfo[j].replaceAll("'","''");
               }
               try {
                  stmt = conn.createStatement();
                  // reverse the update by updating to previous values
                  stmt.execute("update " + tableName + " set isbn='" + invInfo[0]
                        + "',author_first='" + invInfo[1] + "',author_last='" + invInfo[2] 
                        + "',title='" + invInfo[3] + "',pub_year='" + invInfo[4]
                        + "',publisher='" + invInfo[5] + "',quantity=" + invInfo[6]
                        + ",purchase_date='" + invInfo[7] + "',system_date='" + invInfo[8]
                        + "',modify_date='" + invInfo[9] + "' where isbn='" + invInfo[0] +"'");
                  stmt.close();
               } catch (SQLException sqlExcept) {
                  sqlExcept.printStackTrace();
                  return false;
               }
            }
            else if (line.contains("DELETE")) { // if transaction was DELETE
               line = transactions.get(i+1); // get previous values
               line = line.substring(line.lastIndexOf("Previous Values:") + 16);
               String[] invInfo = line.split(";"); // split the semi-colons
               for (int j = 0; j < invInfo.length; j++) { // replace extra spaces and single quotes with double quotes for sql syntax
                  if(j != 3 && j != 5)
                     invInfo[j] = invInfo[j].replaceAll("\\s","");
                  invInfo[j] = invInfo[j].replaceAll("'","''");
               }
               try {
                  stmt = conn.createStatement();
                  // re-insert the previous values before the delete
                  stmt.execute("insert into " 
                        + tableName + " (isbn, author_first, author_last, title, pub_year, "
                        + "publisher, quantity, purchase_date, system_date, modify_date) values ('" 
                        + invInfo[0] + "','" 
                        + invInfo[1] + "','" 
                        + invInfo[2] + "','"
                        + invInfo[3] + "','"
                        + invInfo[4] + "','"
                        + invInfo[5] + "',"
                        + invInfo[6] + ",'"
                        + invInfo[7] + "','"
                        + invInfo[8] + "','"
                        + invInfo[9] + "')");
                  stmt.close();
               } catch (SQLIntegrityConstraintViolationException e) {
                  return false;
               } catch (SQLException sqlExcept) {
                  sqlExcept.printStackTrace();
                  return false;
               }
            }
         }
      }
      return true;
   }
   
   // helper to insertISBN
   public void insertISBNInfo(String[] s) {
      insertISBN(s);
   }
   
   /**
    * This method inserts the retrieved isbn information to the information table
    * @param info isbn information that has been retrieved
    */
   private void insertISBN(String[] info) {
      for (int i =0; i< info.length; i++) {
         info[i] = info[i].replaceAll("'","''");
         info[i] = info[i].replaceAll(";"," ");
      }
      try {
         stmt = conn.createStatement();
         // insert now
         stmt.execute("insert into "
               + infoTable
               + " (isbn, title, author, series, pages, publisher, language, shipping_wt, used_price, new_price, collectible_price) values ('"
               + info[0] + "','" + info[1] + "','" + info[2] + "','" + info[3]
               + "','" + info[4] + "','" + info[5] + "','" + info[6] + "','"
               + info[7] + "','" + info[8] + "','" + info[9] + "','" + info[10] + "')");
         stmt.close();
      } catch (SQLIntegrityConstraintViolationException e) { // If row already exists then update
         try {
            stmt = conn.createStatement();
            stmt.execute("update " + infoTable + " set title='" + info[1]
                  + "', author='" + info[2] + "', series='" + info[3]
                  + "', pages='" + info[4] + "', publisher='" + info[5]
                  + "', language='" + info[6] + "', shipping_wt='" + info[7]
                  + "', used_price='" + info[8] + "', new_price='" + info[9]
                  + "', collectible_price='" + info[10] + "' where isbn='"
                  + info[0] + "'");
            stmt.close();
         } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to place information for " + info[0] +  " in the DB");
         }
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
         JOptionPane.showMessageDialog(null, "Failed to place information for " + info[0] +  " in the DB");
      }
   }
   
   // helper for getISBNInfoGUI
   public void getISBNInfo(TextArea t) {
      text = t;
      getISBNInfoGUI(text);
   }

   /**
    * This method retrieves all rows in the DB and displays them on the GUI
    * @param t the text area for the GUI
    */
   private static void getISBNInfoGUI(TextArea t) {
      try {
         stmt = conn.createStatement();
         ResultSet results = stmt.executeQuery("select * from " + infoTable);
         t.setText("");
         while(results.next()) {
            t.append("ISBN: " + results.getString("isbn")+" ");
            t.append("Author: " + results.getString("author") + " ");
            t.append("Title: " + results.getString("title")+" ");
            t.append("Publisher: " + results.getString("publisher")+ " ");
            t.append("Series: " + results.getString("series") +" ");
            t.append("Language: " + results.getString("language") +" ");
            t.append("\n");
            t.append("Shipping Weight: " + results.getString("shipping_wt") +" ");
            t.append("Used Price: " + results.getString("used_price") + " ");
            t.append("New Price: " + results.getString("new_price") +" ");
            t.append("Collectible: " + results.getString("collectible_price"));
            t.append("\n\n");
         }
         stmt.close();
         results.close();
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
      }
   }
   
   //helper to getEval
   public void getEvaluation(String i, TextArea text, int q, String o) {
      getEval(i,text,q,o);
   }
   
   /**
    * This method retrieves evaluation information and places them in the GUI and the output file
    * @param isbn isbn of the product
    * @param t the textarea
    * @param quantity quantity related to file or DB inventorty
    * @param output output file for writing the report
    */
   private void getEval(String isbn, TextArea t, int quantity, String output) {
      try {
         stmt = conn.createStatement();
         // get the information of the isbn
         ResultSet results = stmt.executeQuery("select * from " + infoTable + " where isbn='" + isbn +"'");
         while(results.next()) {
            // calculate max and min price totals
            String new_price = results.getString("new_price");
            String used_price = results.getString("used_price");
            float new_price1 = 0, used_price1  = 0;
            if(new_price != "None") {
               new_price = new_price.replaceAll("[$]", "");
               new_price = new_price.replaceAll("\\s","");
               new_price1 = Float.parseFloat(new_price);
               new_price1 = new_price1 * quantity;
            }
            if(used_price != "None") {
               used_price = used_price.replaceAll("[$]", ""); 
               used_price = used_price.replaceAll("\\s","");
               used_price1 = Float.parseFloat(used_price);
               used_price1 = used_price1 * quantity;
            }
            // write to textarea in GUI
            t.append("ISBN: " + results.getString("isbn")+" ");
            t.append("Author: " + results.getString("author") + " ");
            t.append("Title: " + results.getString("title")+" ");
            t.append("\n");
            t.append("Shipping Weight: " + results.getString("shipping_wt") +" ");
            t.append("Used Price: " + results.getString("used_price") + " ");
            t.append("New Price: " + results.getString("new_price") +" ");
            t.append("Collectible: " + results.getString("collectible_price"));
            t.append("\n");
            if(new_price != "None") {
               t.append("Max Total: $" + new_price1 + " ");
            }
            if(used_price != "None") {
               t.append("Min Total: $" + used_price1 + " ");
            }
            t.append("\n\n");
            
            String outputDir = null;
            if (output == "None") // if output is none then create generic txt
               outputDir = System.getProperty("user.dir") + "\\reports.txt";
            else // else user input output
               outputDir = output;
            BufferedWriter writer = null;
            try {
               // write to output
               Date dnow = new Date();
               SimpleDateFormat ft = new SimpleDateFormat("E MM/dd/yyyy hh:mm:ss a");
               writer = new BufferedWriter(new FileWriter(outputDir,true));
               writer.write("Report created on: " + ft.format(dnow) +"\n");
               writer.write("ISBN: " + results.getString("isbn")+" ");
               writer.write("Author: " + results.getString("author") + " ");
               writer.write("Title: " + results.getString("title")+" ");
               writer.write("\n");
               writer.write("Shipping Weight: " + results.getString("shipping_wt") +" ");
               writer.write("Used Price: " + results.getString("used_price") + " ");
               writer.write("New Price: " + results.getString("new_price") +" ");
               writer.write("Collectible: " + results.getString("collectible_price"));
               writer.write("\n");
               if(new_price != "None") {
                  writer.write("Max Total: $" + new_price1 + " ");
               }
               if(used_price != "None") {
                  writer.write("Min Total: $" + used_price1 + " ");
               }
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
               try {
                  writer.close();
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }
         stmt.close();
         results.close();
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
      }
   }
   
   // helper to evaluateInv
   public void evaluateDBInv (TextArea text) {
      evaluateInv(text);
   }
   
   /**
    * This method gets all the inventory information from the DB and calls the getEval method above to evaluate them 
    * @param t
    */
   private void evaluateInv (TextArea t) {
      try {
         stmt = conn.createStatement();
         // get all inventory information in DB
         ResultSet results = stmt.executeQuery("select * from " + tableName);
         while(results.next()) {
            // evaluate them
            getEval(results.getString("isbn"), t, results.getInt("quantity"), "None");
         }
         stmt.close();
         results.close();
      } catch (SQLException sqlExcept) {
         sqlExcept.printStackTrace();
      }
   }
   
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
    * @param previous this string holds the previous values of the row before the transaction was made
    */
   private static void writeToLog(String transaction, String[] parameters, String previous) {
      String currentDir = System.getProperty("user.dir") + "\\transactions.txt"; // current directory's logfile
      BufferedWriter writer = null;
      try {
         Date dnow = new Date();
         SimpleDateFormat ft = new SimpleDateFormat("E MM/dd/yyyy hh:mm:ss a");
         writer = new BufferedWriter(new FileWriter(currentDir,true)); // create a new or append to current directory's logfile
         if (transaction == "INSERT") {
            writer.write("\n" + ft.format(dnow) + " " + transaction + " into "
                  + tableName + ": ");
         }
         else {
            writer.write("\n" + ft.format(dnow) + " " + transaction + " from "
                  + tableName + ": ");
         }
         for (int i = 0; i < parameters.length; i++) { // write parameters
            if (i == parameters.length - 1)
               writer.write(parameters[i]);
            else 
               writer.write(parameters[i] + ", ");
         }
         writer.write("\nPrevious Values: " + previous);
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            writer.close();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}