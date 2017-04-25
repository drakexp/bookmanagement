import javax.swing.*;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.io.*;

/**
 * FileChooserGUI is the GUI part of the program which calls to the DB when a menu item is interacted with
 * @author David
 *
 */

public class FileChooserGUI extends JFrame {
   public TextArea text;
   private File file;
   private String line;
   private Database thisDB;
   private Evaluator Eval;
   private boolean loggedIn = false; // variable to detect if user is logged in

   // CONSTRUCTOR
   public FileChooserGUI(Database DB) {
      thisDB = DB;
      Eval = new Evaluator(this, thisDB);
      text = new TextArea();
      text.setEditable(false);
      add(text);
      setTitle("Inventory");
      setSize(300, 400);
      setLocation(300, 300);
      createMenu();
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      pack();
      setVisible(true);
      updateText();
   }

   /**
    * Helper menu creator
    */
   private void createMenu() {
      JMenuItem item;
      JMenuBar menuBar = new JMenuBar();
      JMenu fileMenu = new JMenu("File");
      JMenu optionsMenu = new JMenu("Options");
      JMenu adminMenu = new JMenu("Admin");
      JMenu infoMenu = new JMenu("Info");
      FileMenuHandler fmh = new FileMenuHandler(this);
      OptionMenuHandler omh = new OptionMenuHandler(this);
      AdminMenuHandler amh = new AdminMenuHandler(this);  
      InfoMenuHandler imh = new InfoMenuHandler(this);

      item = new JMenuItem("Insert File");
      item.addActionListener(fmh);
      fileMenu.add(item);
      fileMenu.addSeparator();

      item = new JMenuItem("Quit");
      item.addActionListener(fmh);
      fileMenu.add(item); 
      
      item = new JMenuItem("Display Inventory");
      item.addActionListener(omh);
      optionsMenu.add(item);
      
      item = new JMenuItem("Insert Single Inventory");
      item.addActionListener(omh);
      optionsMenu.add(item);
      
      item = new JMenuItem("Update Inventory");
      item.addActionListener(omh);
      optionsMenu.add(item);
      
      item = new JMenuItem("Delete from Inventory");
      item.addActionListener(omh);
      optionsMenu.add(item);
      
      item = new JMenuItem("Register");
      item.addActionListener(amh);
      adminMenu.add(item);
      
      item = new JMenuItem("Login");
      item.addActionListener(amh);
      adminMenu.add(item);
      
      item = new JMenuItem("Inventory Transactions");
      item.addActionListener(amh);
      adminMenu.add(item);
      
      item = new JMenuItem("Reconstruct Inventory");
      item.addActionListener(amh);
      adminMenu.add(item);
      
      item = new JMenuItem("Get Single ISBN Info");
      item.addActionListener(imh);
      infoMenu.add(item);
      
      item = new JMenuItem("Get Stored ISBN Info");
      item.addActionListener(imh);
      infoMenu.add(item);
      
      item = new JMenuItem("Evaluate File");
      item.addActionListener(imh);
      infoMenu.add(item);
      
      item = new JMenuItem("Evaluate Inventory");
      item.addActionListener(imh);
      infoMenu.add(item);

      setJMenuBar(menuBar);
      menuBar.add(fileMenu);
      menuBar.add(optionsMenu);
      menuBar.add(adminMenu);
      menuBar.add(infoMenu);
   }
   
   /**
    * Helper file chooser
    */
   public void getFile() {
      JFileChooser chooser = new JFileChooser("./");
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.showOpenDialog(null);
      file = chooser.getSelectedFile();
      if (file != null) 
         readFile(file.getAbsolutePath());
   }

   /**
    * Read and insert the inventory information in the file 
    * and also retrieve information from Amazon.com and store in information table
    * @param file the .txt file required
    */
   public void readFile(String file) {
      try {
         FileReader fileReader = new FileReader(file);
         BufferedReader bufferedReader = new BufferedReader(fileReader);
         // much of the code will use the check boolean to check if the database operation was a success or not
         boolean check = true;
         while ((line = bufferedReader.readLine()) != null) {
            check = thisDB.insertInventory(line); // insert each line to the DB
            String isbn = line.substring(0, line.indexOf(';')); 
            Eval.getISBNInfo(isbn,false); // retrieve and store various information about the isbn 
            if (check == false) {
               break;
            }
         }
         if (check != false) {
            JOptionPane.showMessageDialog(null, "Successfully loaded inventory information into the database!");
            thisDB.getInventoryForGUI(text); // display database rows on GUI
         }
         bufferedReader.close();
      } catch (FileNotFoundException ex) { // if errors on reading the file
         JOptionPane.showMessageDialog(null, "Unable to open file " + file);
      } catch (IOException ex) {
         JOptionPane.showMessageDialog(null, "Error reading file " + file);
      } 
   }
   
   /**
    * this method is called from a menuhandler class which then calls the DB method to show inventory data
    */
   public void updateText() {
      thisDB.getInventoryForGUI(text);
   }
   
   /**
    * This method is called from a menuhandler class which opens an input dialog to insert inventory to the DB
    */
   public void insertData() {
      String inventoryInfo = JOptionPane.showInputDialog("Enter your inventory information in the following format and separated by semi-colons as shown:\n"
            + "ISBN;Author First Name;Author Last;Title;Publication Year;Publication Company; Quantity Available");
      if (inventoryInfo != null) {
         boolean check = thisDB.insertInventory(inventoryInfo); // insert inventory
         String isbn = inventoryInfo.substring(0, inventoryInfo.indexOf(';'));
         Eval.getISBNInfo(isbn,false);
         if (check == true) {
            JOptionPane.showMessageDialog(null, "Successfully inserted inventory information into the database!");
            thisDB.getInventoryForGUI(text);   
         }
      }
   }
   
   /**
    * This method is called from a menuhandler class that updates inventory info
    */
   public void updateData() {
      String isbn = JOptionPane.showInputDialog("Enter the ISBN number you want to update");
      if (isbn != null) {
         String inventory = thisDB.getInventoryInfo(isbn); // first get the inventory info
         if(inventory == "None") { // if Database class returns None then there is no inventory in DB
            JOptionPane.showMessageDialog(null, "No such inventory found!"); 
         }
         else {
            String newInventory = JOptionPane.showInputDialog("The current values for " + isbn
                  + " is " + inventory + "\nPlease enter new information for this inventory in the same format as above");
            if (newInventory != null) {
               boolean check = thisDB.updateInventory(isbn, newInventory); // update inventory
               if (check == true) {
                  JOptionPane.showMessageDialog(null, "Successfully updated inventory");
                  thisDB.getInventoryForGUI(text);
               }
               else 
                  JOptionPane.showMessageDialog(null, "Update was unsuccessful. Try again!");        
            }
         }
      }
   }

   /**
    * method is called from menuhandler which deletes an inventory row from DB using isbn input
    */
   public void deleteData() {
      String isbn = JOptionPane.showInputDialog("Enter the isbn of the inventory you want to delete");
      if (isbn != null) {
         boolean check = thisDB.removeInventory(isbn); // remove inventory based on isbn given
         if (check == true) {
            JOptionPane.showMessageDialog(null, "Successfull removed inventory from database.");
            thisDB.getInventoryForGUI(text);
         }
         else 
            JOptionPane.showMessageDialog(null, "Could not remove inventory from the database. Go to options > display to see if that isbn exists!");
      }
   }
   
   /**
    * called from the menu Register which shows and input dialog with username and password inputs
    */
   public void register() {
      JTextField username = new JTextField(20);
      JTextField password = new JTextField(20);

      JPanel myPanel = new JPanel();
      myPanel.setLayout(new GridLayout(0,1));
      myPanel.add(new JLabel("Username: "));
      myPanel.add(username);
      myPanel.add(Box.createVerticalGlue()); // a spacer
      myPanel.add(new JLabel("Password:"));
      myPanel.add(password);

      int result = JOptionPane.showConfirmDialog(null, myPanel, 
               "Please Enter Username and Password", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
         boolean check = false;
         check = thisDB.registerinDB(username.getText(), password.getText()); // insert into DB
         if (check == true) {
            JOptionPane.showMessageDialog(null, "Account created. Please log in");
         }
         else 
            JOptionPane.showMessageDialog(null, "Failed to create your account. Try again");
      }
   }
   
   /**
    * Called from menu Login 
    */
   public void login() {
      JTextField username = new JTextField(20);
      JTextField password = new JTextField(20);

      JPanel myPanel = new JPanel();
      myPanel.setLayout(new GridLayout(0,1));
      myPanel.add(new JLabel("Username: "));
      myPanel.add(username);
      myPanel.add(Box.createVerticalGlue()); // a spacer
      myPanel.add(new JLabel("Password:"));
      myPanel.add(password);

      int result = JOptionPane.showConfirmDialog(null, myPanel, 
               "Log in with your username and password", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
         boolean check = false;
         check = thisDB.dbLogin(username.getText(), password.getText()); // retrieve user if true then user exists
         if (check == true) {
            JOptionPane.showMessageDialog(null, "Successfully logged in!");
            loggedIn = true; // user logged in
         }
         else {
            JOptionPane.showMessageDialog(null, "Failed to log in");
            loggedIn = false; // user not logged in
         }
      }
   }
   
   /**
    * Called from menu Transactions
    * can only work if user is logged in 
    */
   public void transactions() { 
      if (loggedIn == false) { // if user not logged in
         JOptionPane.showMessageDialog(null, "Please log in first!");
      }
      else {
         try {
            String transactionDir = System.getProperty("user.dir") + "\\transactions.txt"; // get the txt
            FileReader fileReader = new FileReader(transactionDir); 
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            text.setText("");
            while((line = bufferedReader.readLine()) != null) {
               text.append(line + "\n"); // SHOW the transactions to GUI
            }
            bufferedReader.close();
         } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Unable to open file " + file);
         } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error reading file " + file);
         }
      }
   }
   
   /**
    * This menu can only be used when logged in
    * Calls a DB method which reconstructs the DB using the current transaction log
    */
   public void reconstruct() { 
      if(loggedIn == false) { // user not logged
         JOptionPane.showMessageDialog(null, "Please log in first!");
      }
      else {
         int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to reconstruct the database using the current transactional log?", 
               "Reconstruct", JOptionPane.OK_CANCEL_OPTION); 
         if (result == JOptionPane.OK_OPTION) {
            try {
               List<String> reverse = new ArrayList<String>();
               String transactionDir = System.getProperty("user.dir") + "\\transactions.txt"; // get transactions
               FileReader fileReader = new FileReader(transactionDir);
               BufferedReader bufferedReader = new BufferedReader(fileReader);
               text.setText("");
               while((line = bufferedReader.readLine()) != null) {
                  reverse.add(line); // reverse the transaction log into a list 
               }
               boolean check = thisDB.reconstructDB(reverse); // RECONSTRUCT DB
               if (check == false) {
                  JOptionPane.showMessageDialog(null, "Reconstruction failed");
               }
               else {
                  JOptionPane.showMessageDialog(null, "Reconstruction complete");
                  String currentDir = System.getProperty("user.dir") + "\\transactions.txt";
                  BufferedWriter writer = null;
                  try {
                     // CLEAR TRANSACTION LOG
                     writer = new BufferedWriter(new FileWriter(currentDir,false));
                     writer.write("TRANSACTIONAL LOG");
                  } catch (Exception e) {
                     e.printStackTrace();
                  } finally {
                     try {
                        writer.close();
                     } catch (Exception e) {
                        e.printStackTrace();
                     }
                  }
                  updateText();
               }
               bufferedReader.close();
            } catch (FileNotFoundException ex) {
               JOptionPane.showMessageDialog(null, "Unable to open file " + file);
            } catch (IOException ex) {
               JOptionPane.showMessageDialog(null, "Error reading file " + file);
            }
         }
      }
   }

   /**
    * Called from menu Get Single ISBN Info
    */
   public void getISBNInfo() {
      String isbn =
      JOptionPane.showInputDialog("Enter the ISBN number you want to see information on");
      if (isbn != null) {
         Eval.getISBNInfo(isbn,true); // retrieve information
      }
   }
   
   /**
    * Called from menu Get Stored Inventory Info
    * Gets all ISBN information using stored inventory data
    */
   public void getDBISBNInfo() {
      thisDB.getISBNInfo(text); // retrieve info
   }
   
   /**
    * Called from menu Evaluate File or from args
    * Reads the file chosen and retrieves information about the ISBN from Amazon 
    * Then evaluates the information to GUI and output
    * @param f file chosen
    * @param o output file
    */
   public void evaluateFile(String f, String o) {
      if (f == "None") { // if file is not passed through then choose
         JFileChooser chooser = new JFileChooser("./");
         chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         chooser.showOpenDialog(null);
         file = chooser.getSelectedFile();
      }
      else // file was a parameter
         file = new File(f);
      if (file != null) {
         try {
            FileReader fileReader = new FileReader(file.getAbsolutePath());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            text.setText("");
            while ((line = bufferedReader.readLine()) != null) {
               String isbn = line.substring(0, line.indexOf(';'));
               String q = line.substring(line.lastIndexOf(';')+1).replaceAll("\\s", "");
               int quantity = Integer.parseInt(q);
               Eval.getISBNInfo(isbn, false); // retrieve info
               Eval.evaluate(isbn, text, quantity, o); // evaluate and post
            }
            bufferedReader.close();
         } catch (FileNotFoundException ex) { // if errors on reading the file
            JOptionPane.showMessageDialog(null, "Unable to open file " + file);
         } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error reading file " + file);
         }
      }
   }
   
   /**
    * Called from menu Evaluate Inventory
    * Will evaluate all stored inventory information
    */
   public void evaluateInventory() {
      text.setText("");
      thisDB.evaluateDBInv(text);
   }
   
   /**
    * on exit menu item shutdown DB and exit program
    */
   public void exit() { 
      thisDB.shutdown();
      System.exit(0);
   }
}