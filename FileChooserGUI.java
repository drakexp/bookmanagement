import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.io.*;

/**
 * FileChooserGUI is the GUI part of the program which calls to the DB when a menu item is interacted with
 * @author David
 *
 */

public class FileChooserGUI extends JFrame {
   private TextArea text;
   private File file;
   private String line;
   private Database thisDB;

   /**
    * FileChooserGUI creates the GUI for the application
    * 
    * @param DB the database
    */
   public FileChooserGUI(Database DB) {
      thisDB = DB;
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
      FileMenuHandler fmh = new FileMenuHandler(this);
      OptionMenuHandler omh = new OptionMenuHandler(this);

      item = new JMenuItem("Open");
      item.addActionListener(fmh);
      fileMenu.add(item);
      fileMenu.addSeparator();

      item = new JMenuItem("Quit");
      item.addActionListener(fmh);
      fileMenu.add(item);
      
      item = new JMenuItem("Display");
      item.addActionListener(omh);
      optionsMenu.add(item);
      
      item = new JMenuItem("Insert");
      item.addActionListener(omh);
      optionsMenu.add(item);
      
      item = new JMenuItem("Update");
      item.addActionListener(omh);
      optionsMenu.add(item);
      
      item = new JMenuItem("Delete");
      item.addActionListener(omh);
      optionsMenu.add(item);

      setJMenuBar(menuBar);
      menuBar.add(fileMenu);
      menuBar.add(optionsMenu);
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
    * Read and insert the lines of the file
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
            if (check == false) {
               JOptionPane.showMessageDialog(null, "Failed to insert at least one line of your file! Try again");
               break;
            }
         }
         if (check == true) {
            JOptionPane.showMessageDialog(null, "Successfully loaded information into the database!");
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
    * this method is called from a menuhandler class which then calls the DB method to show player data on GUI
    */
   public void updateText() {
      thisDB.getInventoryForGUI(text);
   }
   
   /**
    * This method is called from a menuhandler class which opens an input dialog to insert a player to the DB
    */
//   public void insertData() {
//      String playerInfo = JOptionPane.showInputDialog("Enter a basketball player with the format of Name, Position, "
//            + "Age, Team, Height\nAll separated by commas and Height in feet\'inches\" format");
//      if (playerInfo != null) {
//         boolean check = thisDB.insertPlayer(playerInfo); // insert playerinfo
//         if (check == true) {
//            JOptionPane.showMessageDialog(null, "Successfully inserted information into the database!");
//            thisDB.getPlayersForGUI(text);   
//         }
//         else 
//            JOptionPane.showMessageDialog(null, "Insertion was unsuccessful. Try again!");
//      }
//   }
//   
   /**
    * This method is called from a menuhandler class that updates player info
    */
//   public void updateData() {
//      String playerName = JOptionPane.showInputDialog("Enter the player name you want to update");
//      if (playerName != null) {
//         String playerInfo = thisDB.getPlayerInfo(playerName); // first get the playerinfo
//         if(playerInfo == "None") { // if Database class returns None then there is no player
//            JOptionPane.showMessageDialog(null, "No player found!"); 
//         }
//         else {
//            String newPlayerInfo = JOptionPane.showInputDialog("The current information for player " + playerName
//                  + " is " + playerInfo + "\nPlease enter new information for this player in the same format as above");
//            if (newPlayerInfo != null) {
//               boolean check = thisDB.updatePlayer(playerName, newPlayerInfo); // update player
//               if (check == true) {
//                  JOptionPane.showMessageDialog(null, "Successfully updated player information");
//                  thisDB.getPlayersForGUI(text);
//               }
//               else 
//                  JOptionPane.showMessageDialog(null, "Update was unsuccessful. Try again!");        
//            }
//         }
//      }
//   }

   /**
    * method is called from menuhandler which deletes a player from DB using input for name
    */
//   public void deleteData() {
//      String playerName = JOptionPane.showInputDialog("Enter the player name you want to delete");
//      if (playerName != null) {
//         boolean check = thisDB.removePlayer(playerName); // remove player
//         if (check == true) {
//            JOptionPane.showMessageDialog(null, "Successfull removed player from database.");
//            thisDB.getPlayersForGUI(text);
//         }
//         else 
//            JOptionPane.showMessageDialog(null, "Could not remove player from the database. Go to options > display to see if that player exists!");
//      }
//   }
   
   /**
    * on exit menu item shutdown DB and exit program
    */
   public void exit() { 
      thisDB.shutdown();
      System.exit(0);
   }
}