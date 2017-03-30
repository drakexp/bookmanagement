import java.awt.event.*;

/**
 * This class handles the File menu
 * @author David
 *
 */
public class FileMenuHandler implements ActionListener {
   FileChooserGUI thisGUI;
   
   /**
    * @param GUI The GUI it belongs to
    */
   public FileMenuHandler(FileChooserGUI GUI) {
      thisGUI = GUI;
   }
   
   /**
    * Action events for this menu
    */
   public void actionPerformed(ActionEvent event) {
      String menuName = event.getActionCommand();
      if (menuName.equals("Quit"))
         thisGUI.exit();
      else if (menuName.equals("Open")) {
         thisGUI.getFile();
      }
   }
}