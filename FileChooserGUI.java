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
    * on exit menu item shutdown DB and exit program
    */
   public void exit() { 
      thisDB.shutdown();
      System.exit(0);
   }
}