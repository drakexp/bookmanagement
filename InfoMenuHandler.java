import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class handles the Info menu
 * @author David
 *
 */

public class InfoMenuHandler implements ActionListener {
   FileChooserGUI thisGUI;
   
   /**
    * @param GUI The GUI it belongs to
    */
   public InfoMenuHandler(FileChooserGUI GUI) {
      thisGUI = GUI;
   }
   
   /**
    * Action events for the menu Options
    */
   public void actionPerformed(ActionEvent event) {
      String menuName = event.getActionCommand();
      if (menuName.equals("Get Single ISBN Info"))
         thisGUI.getISBNInfo();
      else if (menuName.equals("Get Stored ISBN Info"))
         thisGUI.getDBISBNInfo();
      else if (menuName.equals("Evaluate File"))
         thisGUI.evaluateFile("None", "None");
      else if (menuName.equals("Evaluate Inventory"))
         thisGUI.evaluateInventory();
   }
}