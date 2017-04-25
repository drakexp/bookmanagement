import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class handles the Options menu
 * @author David
 *
 */

public class OptionMenuHandler implements ActionListener {
   FileChooserGUI thisGUI;
   
   /**
    * @param GUI The GUI it belongs to
    */
   public OptionMenuHandler(FileChooserGUI GUI) {
      thisGUI = GUI;
   }
   
   /**
    * Action events for the menu Options
    */
   public void actionPerformed(ActionEvent event) {
      String menuName = event.getActionCommand();
      if (menuName.equals("Display Inventory"))
         thisGUI.updateText();
      else if (menuName.equals("Insert Single Inventory"))
         thisGUI.insertData();
      else if (menuName.equals("Update Inventory"))
         thisGUI.updateData();
      else if (menuName.equals("Delete from Inventory"))
         thisGUI.deleteData();
   }
}