import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class calls evaluation methods to the DB and also gets information from Amazon.com and stores them in the DB
 * @author David
 *
 */
public class Evaluator {
   private static String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6";
   private FileChooserGUI thisGUI;
   private Database thisDB;

   public Evaluator(Database db) {
      thisDB = db;
   } // constructor
   
   public Evaluator(FileChooserGUI g, Database db) {
      thisGUI = g;
      thisDB = db;
   } // constructor
   
   /**
    * Calls getEvaluation method
    * @param isbn isbn of product
    * @param text TextArea
    * @param quantity quantity related to file or inventory
    * @param o output
    */
   public void evaluate(String isbn, TextArea text, int quantity, String o) {
      thisDB.getEvaluation(isbn, text, quantity, o);
   }

   
   /**
    * This method uses given isbn to extract information from Amazon.com
    * @param isbn isbn of the product
    * @param show boolean to check whether or not to display results to GUI
    */
   public void getISBNInfo(String isbn, boolean show) {
      isbn = isbn.replaceAll("\\s", "");
      String[] info = new String[11]; // this variable stores all the information to be passed on to the DB
      info[0] = isbn;
      for (int i = 1; i < info.length; i++) { // initlialize data
         info[i] = "None";
      }
      try {
         // get the information from the url
         String url = "https://www.amazon.com/dp/" + isbn;
         BufferedReader reader = read(url);
         String line = reader.readLine();
         if (show == true) {
            thisGUI.text.setText(""); // clear GUI textArea
         }
         while (line != null) {
            // match product title
            Pattern p0 = Pattern.compile("(?<=productTitle\")[^<]*");
            Matcher m0 = p0.matcher(line);
            if (m0.find()) {
               String title = m0.group();
               title = title.substring(title.indexOf('>') + 1);
               info[1] = title;
               if (show == true) {
                  thisGUI.text.append("Title: " + title + "\n");
               }
            }
            // match author 
            Pattern p1 = Pattern.compile("(?<=_cont_book_1\">)[^\"]*");
            Matcher m1 = p1.matcher(line);
            if (m1.find()) {
               String author = m1.group();
               author = author.substring(0, author.indexOf('<'));
               info[2] = author;
               if (show == true) {
                  thisGUI.text.append("Author: " + author + "\n");
               }
            }
            // match series name
            Pattern p2 = Pattern.compile("(?<=Series:</b>)[^\"]*");
            Matcher m2 = p2.matcher(line);
            if (m2.find()) {
               String series = m2.group();
               series = series.substring(1, series.indexOf('<'));
               info[3] = series;
               if (show == true) {
                  thisGUI.text.append("Series: " + series + "\n");
               }
            }
            // match # of pages
            Pattern p3 = Pattern.compile("(?<=Paperback:</b>)[^\"]*");
            Matcher m3 = p3.matcher(line);
            if (m3.find()) {
               String pages = m3.group();
               pages = pages.substring(1, pages.indexOf('<'));
               info[4] = pages;
               if (show == true) {
                  thisGUI.text.append("Pages: " + pages + "\n");
               }
            }
            // match publisher information
            Pattern p4 = Pattern.compile("(?<=Publisher:</b>)[^\"]*");
            Matcher m4 = p4.matcher(line);
            if (m4.find()) {
               String publisher = m4.group();
               publisher = publisher.substring(1, publisher.indexOf('<'));
               info[5] = publisher;
               if (show == true) {
                  thisGUI.text.append("Publisher: " + publisher + "\n");
               }
            }
            // match language of the product
            Pattern p5 = Pattern.compile("(?<=Language:</b>)[^\"]*");
            Matcher m5 = p5.matcher(line);
            if (m5.find()) {
               String language = m5.group();
               language = language.substring(1, language.indexOf('<'));
               info[6] = language;
               if (show == true) {
                  thisGUI.text.append("Language: " + language + "\n");
               }
            }
            // match weight of product
            Pattern p6 = Pattern.compile("(?<=Shipping Weight:</b>)[^\"]*");
            Matcher m6 = p6.matcher(line);
            if (m6.find()) {
               String weight = m6.group();
               weight = weight.substring(1, weight.indexOf('('));
               info[7] = weight;
               if (show == true) {
                  thisGUI.text.append("Shipping Weight: " + weight + "\n");
               }
            }
            line = reader.readLine();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      try {
         // now get pricing information
         String[] prices = new String[] { // index 0 is used, 1 is new, 2 is collectibles
               "ref=olp_f_used?ie=UTF8&f_used=true&f_usedAcceptable=true&f_usedGood=true&f_usedLikeNew=true&f_usedVeryGood=true",
               "ref=olp_f_new?ie=UTF8&f_collectible=true&f_new=true",
               "ref=olp_f_collectible?ie=UTF8&f_collectible=true" };
         
         for (int i = 0; i < prices.length; i++) { // iterate through
            // get the url
            String priceurl = "https://www.amazon.com/gp/offer-listing/" + isbn
                  + "/" + prices[i];
            BufferedReader reader = read(priceurl);
            String line = reader.readLine();
            while (line != null) {
               Pattern p = Pattern
                     .compile("(?<=olpOfferPrice a-text-bold\">)[^<]*"); // Find top price ie best price
               Matcher m = p.matcher(line);
               if (m.find()) {
                  String price = m.group();
                  price = price.replaceAll("\\s", "");
                  if (i == 0) // if used
                     info[8] = price;
                  else if (i == 1) // if new
                     info[9] = price;
                  else if (i == 2) // if collectibles
                     info[10] = price;
                  if (show == true) { // show to GUI if yes
                     if (i == 0)
                        thisGUI.text.append("Used Price: " + price + "\n");
                     else if (i == 1)
                        thisGUI.text.append("New Price: " + price + "\n");
                     else if (i == 2)
                        thisGUI.text.append("Collectible Price: " + price + "\n");
                  }
                  break;
               }
               Pattern p1 = Pattern
                     .compile("There are currently no listings for this search.");
               Matcher m1 = p1.matcher(line);
               if (m1.find()) {
                  if (i == 0) // if used has none
                     info[8] = "None";
                  else if (i == 1) // if new has none
                     info[9] = "None"; 
                  else if (i == 2) // if collectibles has noen
                     info[10] = "None";
                  if (show == true) { // show to GUI if true
                     if (i == 0)
                        thisGUI.text.append("Used Price: None Available\n");
                     else if (i == 1)
                        thisGUI.text.append("New Price: None Available\n");
                     else if (i == 2)
                        thisGUI.text.append("Collectible Price: None Available\n");
                  }
                  break;
               }
               line = reader.readLine();
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      // INSERT THE INFORMATION RETRIEVED TO THE DB
      thisDB.insertISBNInfo(info);
      if (show == true) {
         thisGUI.text.append("\n");
      }
   }

   public static InputStream getURLInputStream(String sURL) throws Exception {
      URLConnection oConnection = (new URL(sURL)).openConnection();
      oConnection.setRequestProperty("User-Agent", USER_AGENT);
      return oConnection.getInputStream();
   } // getURLInputStream

   public static BufferedReader read(String url) throws Exception {
      InputStream content = (InputStream) getURLInputStream(url);
      return new BufferedReader(new InputStreamReader(content));
   } // read
}