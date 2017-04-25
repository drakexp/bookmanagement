import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * The main class that runs the program
 * Creates DB, Evaluator and GUI
 * Takes in zero to two arguments
 * If 0 argument - works as GUI
 * 1 - inserts the file inventory information to DB and then works as GUI as well
 * 2 - evaluates the first argument and then writes to second 
 * @author David
 *
 */

public class Main {
   public static FileChooserGUI GUI;
   public static Database DB;
   public static Evaluator Eval;
   private static String line;
   
   public static void main(String[] args) {
      DB = new Database();
      Eval = new Evaluator(DB);
      GUI = new FileChooserGUI(DB);
      if (args.length == 1) {
         readFile(args[0]);
      }
      else if (args.length == 2) {
         getISBNInfoFile(args[0], args[1]);
      }
   }
   
   /**
    * Read the first argument and insert inventory information to DB 
    * Then retrieve Amazon.com information and store it as well
    * @param file Read this file
    */
   public static void readFile(String file) {
      try {
         FileReader fileReader = new FileReader(file);
         BufferedReader bufferedReader = new BufferedReader(fileReader);
         boolean check = false;
         while((line = bufferedReader.readLine()) != null) {
            check = DB.insertInventory(line);
            String isbn = line.substring(0, line.indexOf(';'));
            Eval.getISBNInfo(isbn, false);
            if (check == false) {
               break;
            }
         }
         bufferedReader.close();
         if (check != false)
            JOptionPane.showMessageDialog(null, "Successfully inserted inventory information into the database!");
      } catch (FileNotFoundException ex) {
         JOptionPane.showMessageDialog(null, "Unable to open file " + file);
      } catch (IOException ex) {
         JOptionPane.showMessageDialog(null, "Error reading file " + file);
      }
   }
   
   /**
    * Evaluate and write
    * @param input file
    * @param output file
    */
   public static void getISBNInfoFile(String input, String output) {
      GUI.evaluateFile(input, output);
   }
}
