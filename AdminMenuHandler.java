import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class handles the Admin menu
 * @author David
 *
 */

public class AdminMenuHandler implements ActionListener {
   FileChooserGUI thisGUI;
   
   /**
    * @param GUI The GUI it belongs to
    */
   public AdminMenuHandler(FileChooserGUI GUI) {
      thisGUI = GUI;
   }
   
   /**
    * Action events for the menu Options
    */
   public void actionPerformed(ActionEvent event) {
      String menuName = event.getActionCommand();
      if (menuName.equals("Register"))
         thisGUI.register();
      else if (menuName.equals("Login"))
         thisGUI.login();
      else if (menuName.equals("Inventory Transactions"))
         thisGUI.transactions();
      else if (menuName.equals("Reconstruct Inventory"))
         thisGUI.reconstruct();
   }
}