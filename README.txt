RUNNING THE PROGRAM
Read Installation.txt

When the program is run, it will automatically create a DB named testDB
Tables named inventory, accounts, information are created shortly after if they do not exist

INPUT/OUTPUT
For the majority of the program an input file refers to a file with the following formats:
ISBN;AuthorFirstName;AuthorLastName;Title;PublishedYear;Publisher;QuantityinInventory
Exactly as shown above and delimited as shown with semi-colons and a new line for each subsequent information

An output file can be a blank text file

ARGUMENTS
If no arguments are provided the program will run as a GUI but will also run as a GUI regardless of arguments

If one argument is given, it must be an input file. The program will insert the inventory data into the database
and will then retrieve each isbn information from Amazon.com and place them in the information table

If two arguments are given, the first must be an input file and the next must be an output file.
The program will read the input file then evaluate it. The evaluated information will be posted on the GUI and written to the output file.

The above functionalities can also be called from the GUI which always is shown when the program is run

GUI
The gui has various functionalities as will be explained below

FILE MENU
The file menu has two sub-menus:
   Insert File - This menu item opens up a file chooser which you can then choose an input file that will be inserted into the inventory table
                 It will also retrieve information from the given isbn's and store them in the database
   Quit - Self-explanatory
   
OPTIONS MENU
   Display Inventory - Pulls current inventory information from database and displays to the GUI
   Insert Single Inventory - Another GUI will appear giving instructions on how to insert a single inventory row
   Update Inventory - Another Gui will pop up with instructions on how to update a existing inventory row
   Delete From Inventory - Same as above
   
ADMIN MENU
   Register - GUI will appear with fields which a user can register in
   Login - Identical to the above but creates a boolean in which the program detects if the user has been logged in or not
   The below can only be accessed if user is logged in:
      Inventory Transactions - Will display to GUI full transactional(Insert,Delete,Update) logs to date
      Reconstruct Inventory - This menu item will use the transactional log to reverse all transactional activities to the beginning of the file
                              The transactional logs will also be cleared after
                          
INFO MENU
   Get Single ISBN Info - User can input one single isbn which will then be retrieved then displayed and stored in the database (Does not need be an ISBN from inventory)
   Get Stored ISBN Info - This will display all information which have been retrieved on Amazon and stored on the DB
   Evaluate File - This will open a file chooser menu which user can choose an input file. Program will display and write to generic output.txt file with evaluated information
   Evaluate Inventory - Will display and write to log evaluated information on ALL inventory rows

