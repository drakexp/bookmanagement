import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * 
 * @author David
 * 
 * 
 */

public class Main {
   public static FileChooserGUI GUI;
   public static Database DB;
   private static String line;
   
   public static void main(String[] args) {
      DB = new Database();
      GUI = new FileChooserGUI(DB);
      if (args.length != 0) {
         readFile(args[0]);
      }
   }
   
   public static void readFile(String file) {
      try {
         FileReader fileReader = new FileReader(file);
         BufferedReader bufferedReader = new BufferedReader(fileReader);
         boolean check = false;
         while((line = bufferedReader.readLine()) != null) {
            check = DB.insertInventory(line);
            if (check == false) {
               JOptionPane.showMessageDialog(null, "Insertion was unsuccessful using arguments. Try again!");
               break;
            }
         }
         bufferedReader.close();
         if (check != false)
            JOptionPane.showMessageDialog(null, "Successfully inserted information into the database!");
      } catch (FileNotFoundException ex) {
         JOptionPane.showMessageDialog(null, "Unable to open file " + file);
      } catch (IOException ex) {
         JOptionPane.showMessageDialog(null, "Error reading file " + file);
      }
   }
}
