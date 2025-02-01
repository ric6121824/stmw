/* Parser skeleton for processing item-???.xml files. Must be compiled in
 * JDK 1.5 or above.
 *
 * partners: Md Azijul Islam, Ping-Chung Tsai
 *
 * This program processes all files passed on the command line (to parse
 * an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 *
 */

import java.io.FileReader;
import java.io.*;
import java.text.*;
import java.util.*;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;


public class MySAX extends DefaultHandler
{
    final char DELIMCHAR = '<';
    final char EOLCHAR = '>';

    //this enum is used to track where in the xml file we are to decide what to do with the data currently being read
    enum ReadTag{ITEMS, ITEM, ITEMNAME, CATEGORY, CURRENTBID, FIRSTBID, BUYPRICE, BID, BIDDER, TIME, AMOUNT, ITEMLOCATION, ITEMCOUNTRY, BIDLOCATION, BIDCOUNTRY, STARTED, ENDS, DESCRIPTION}

    private HashSet<UserLoc> userls = new HashSet<UserLoc>();
    private HashSet<UserCountry> usercs = new HashSet<UserCountry>();
    private HashSet<Rating> ratings = new HashSet<Rating>();
    private HashSet<LatLong> latslongs = new HashSet<LatLong>();

    private ReadTag tag = ReadTag.ITEMS;    
    private Item currentItem = new Item("");
    private Bid currentBid = new Bid();
    private String currentSellerLocation, currentSellerCountry, currentSellerLat, currentSellerLong;
    
    private StringBuilder sb;
    private SimpleDateFormat dp = new SimpleDateFormat("MMM-dd-yy HH:mm:ss", Locale.ENGLISH);
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private FileWriter itemWriter, bidWriter, catWriter, bpWriter, brWriter, userWriter; //userwriter used for all user-related tables at the end
    
    //private boolean locmonitor = false;

    public static void main (String args[])
	throws Exception
    {
	XMLReader xr = XMLReaderFactory.createXMLReader();
	MySAX handler = new MySAX();
	xr.setContentHandler(handler);
	xr.setErrorHandler(handler);
    
    handler.setupWriters();
				// Parse each file provided on the
				// command line.
	for (int i = 0; i < args.length; i++) {
	    FileReader r = new FileReader(args[i]);
	    xr.parse(new InputSource(r));
	}
    handler.writeUserData();
    handler.closeWriters();
    }


    public MySAX ()
    {
	super();
    }
    
    public void setupWriters(){
        try{
            itemWriter = new FileWriter("items.csv");
            bidWriter = new FileWriter("bids.csv");
            catWriter = new FileWriter("categories.csv");
            bpWriter = new FileWriter("buyprice.csv");
            brWriter = new FileWriter("buyerrating.csv");
            userWriter = new FileWriter("sellerrating.csv");
        } catch(IOException e) {
            System.out.println(e.getMessage()); 
        }
    }
    
    public void closeWriters(){
        try{
            itemWriter.flush();
            itemWriter.close();
            bidWriter.flush();
            bidWriter.close();
            catWriter.flush();
            catWriter.close();
            bpWriter.flush();
            bpWriter.close();
            brWriter.flush();
            brWriter.close();
            userWriter.flush();
            userWriter.close();
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeUserData(){
        try{
            for(Rating r : ratings)
                writeRating(r);
            userWriter.flush();
            userWriter.close();
            userWriter = new FileWriter("locations.csv");
            for(UserLoc ul : userls)
                writeLocation(ul);
            userWriter.flush();
            userWriter.close();
            userWriter = new FileWriter("countries.csv");
            for(UserCountry uc : usercs)
                writeCountry(uc);
            userWriter.flush();
            userWriter.close();
            userWriter = new FileWriter("userlatlong.csv");
            for(LatLong ll : latslongs)
                writeLatLong(ll);
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    /* Returns the amount (in XXXXX.xx format) denoted by a money-string
     * like $3,453.23. Returns the input if the input is an empty string.
     */
    static String strip(String money) {
        if (money.equals(""))
            return money;
        else {
            double am = 0.0;
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            try { am = nf.parse(money).doubleValue(); }
            catch (ParseException e) {
                System.out.println("This method should work for all " +
                                   "money values you find in our data.");
                System.exit(20);
            }
            nf.setGroupingUsed(false);
            return nf.format(am).substring(1);
        }
    }
    
    private String dateFormatted(String date){
        try{
            return df.format(dp.parse(date));
        } catch(ParseException e){
            return "0000-01-01 00:00:00";
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////


    public void startDocument ()
    {
	    //System.out.println("Start document");
    }


    public void endDocument ()
    {
	    //System.out.println("End document");
    }
    
    public void startElement(String uri, String name, String qName, Attributes atts){
        switch(name){
            case "Item":
                tag = ReadTag.ITEM;
                currentItem = new Item(atts.getValue("ItemID"));
                break;
            case "Name":
                tag = ReadTag.ITEMNAME;
                break;
            case "Category":
                sb = new StringBuilder();
                tag = ReadTag.CATEGORY;
                break;
            case "Currently":
                tag = ReadTag.CURRENTBID;
                break;
            case "First_Bid":
                tag = ReadTag.FIRSTBID;
                break;
            case "Bid":
                tag = ReadTag.BID;
                currentBid = new Bid();
                currentBid.itemID = currentItem.itemID;
                break;
            case "Bidder":
                //locmonitor = true;
                currentBid.bidderID = atts.getValue("UserID");
                ratings.add(new Rating(currentBid.bidderID, atts.getValue("Rating"), false));
                break;
            case "Location":
                if(tag == ReadTag.ITEM){
                    tag = ReadTag.ITEMLOCATION;
                    if(atts.getValue("Latitude") != null){ 
                        currentSellerLat = atts.getValue("Latitude");
                        currentSellerLong = atts.getValue("Longitude");
                    }
                }    
                else if(tag == ReadTag.BID){
                    tag = ReadTag.BIDLOCATION;
                    if(atts.getValue("Latitude") != null)
                        latslongs.add(new LatLong(currentBid.bidderID, atts.getValue("Latitude"), atts.getValue("Longitude")));
                    
                    
                }
                sb = new StringBuilder();
                break;
            case "Country":
                if(tag == ReadTag.ITEM)
                    tag = ReadTag.ITEMCOUNTRY;
                else if(tag == ReadTag.BID)
                    tag = ReadTag.BIDCOUNTRY;
                break;
            case "Time":
                tag = ReadTag.TIME;
                break;
            case "Amount":
                tag = ReadTag.AMOUNT;
                break;
            case "Buy_Price":
                tag = ReadTag.BUYPRICE;
                break;
            case "Started":
                tag = ReadTag.STARTED;
                break;
            case "Ends":
                tag = ReadTag.ENDS;
                break;
            case "Seller":
                //read attributes directly.
                currentItem.sellerID = atts.getValue("UserID");
                ratings.add(new Rating(currentItem.sellerID, atts.getValue("Rating"), true));
                break;
            case "Description":
                tag = ReadTag.DESCRIPTION;
                sb = new StringBuilder();
                break;
                
        }
    }

    public void endElement(String uri, String name, String qName){
        switch(name){
            case "Item":
                writeItem(currentItem);
                if(currentSellerLocation != null)
                    userls.add(new UserLoc(currentItem.sellerID, currentSellerLocation));
                currentSellerLocation = null;
                if(currentSellerCountry != null)
                    usercs.add(new UserCountry(currentItem.sellerID, currentSellerCountry));
                currentSellerCountry = null;
                if(currentSellerLat != null)
                    latslongs.add(new LatLong(currentItem.sellerID, currentSellerLat, currentSellerLong));
                currentSellerLat = null;
                currentSellerLong = null;
                break;
            case "Location":
                if(tag == ReadTag.ITEMLOCATION){
                    tag = ReadTag.ITEM;
                    currentSellerLocation = sb.toString();
                }
                else{
                    tag = ReadTag.BID;
                    userls.add(new UserLoc(currentBid.bidderID, sb.toString()));
                }
                break;
            case "Country":
                if(tag == ReadTag.ITEMCOUNTRY)
                    tag = ReadTag.ITEM;
                else
                    tag = ReadTag.BID;
                break;
            case "Bid":
                writeBid(currentBid);
                tag = ReadTag.ITEM;
                break;
            case "Category":
                tag = ReadTag.ITEM;
                writeCategory(currentItem.itemID, sb.toString());
                break;
            case "Description":
                if(sb.length() > 4000)
                    currentItem.description = sb.substring(0,4000);
                else
                    currentItem.description = sb.toString();
                tag = ReadTag.ITEM;
                break;
            case "Ends":
                tag = ReadTag.ITEM;
                break;
            default:
                tag = ReadTag.ITEM;
                break;
        }
    }

    public void characters(char ch[], int start, int length){
        switch(tag){
            case ITEMNAME:
                currentItem.name = new String(ch, start, length);
                break;
            case CATEGORY:
                sb.append(new String(ch, start, length));
                break;
            case FIRSTBID:
                currentItem.firstBid = strip(new String(ch, start, length));
                break;
            case CURRENTBID:
                currentItem.currentBid = strip(new String(ch, start, length));
                break;
            case BUYPRICE:
                writeBuyPrice(currentItem.itemID, strip(new String(ch, start, length)));
                break;
            case STARTED:
                currentItem.startDate = dateFormatted(new String(ch, start, length));
                break;
            case ENDS:
                currentItem.endDate = dateFormatted(new String(ch, start, length));
                break;
            case DESCRIPTION:
                sb.append(new String(ch, start, length));
                break;
            case TIME:
                currentBid.time = dateFormatted(new String(ch, start, length));
                break;
            case AMOUNT:
                currentBid.amount = strip(new String(ch, start, length));
                break;
            case ITEMLOCATION:
                sb.append(new String(ch, start, length));
                break;
            case ITEMCOUNTRY:
                currentSellerCountry = new String(ch, start, length);
                break;
            case BIDLOCATION:
                sb.append(new String(ch, start, length));
                break;
            case BIDCOUNTRY:
                usercs.add(new UserCountry(currentBid.bidderID, new String(ch, start, length)));
                break;
        }
    }
    
    public void writeItem(Item item){
        
        try{
            sb = new StringBuilder(item.itemID).append(DELIMCHAR).append(item.name).append(DELIMCHAR).append(item.sellerID).append(DELIMCHAR).append(item.firstBid).append(DELIMCHAR).append(item.currentBid).append(DELIMCHAR).append(item.startDate).append(DELIMCHAR).append(item.endDate).append(DELIMCHAR).append(item.description).append(EOLCHAR);
            itemWriter.write(sb.toString());
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public void writeBid(Bid bid){
        try{
            sb = new StringBuilder(bid.itemID).append(DELIMCHAR).append(bid.bidderID).append(DELIMCHAR).append(bid.amount).append(DELIMCHAR).append(bid.time).append(EOLCHAR);
            bidWriter.write(sb.toString());
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void writeCategory(String itemID, String cat){
        try{
            sb = new StringBuilder(itemID).append(DELIMCHAR).append(cat).append(EOLCHAR);
            catWriter.write(sb.toString());
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void writeBuyPrice(String itemID, String bp){
        try{
            sb = new StringBuilder(itemID).append(DELIMCHAR).append(bp).append(EOLCHAR);
            bpWriter.write(sb.toString());
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void writeLocation(UserLoc ul){
        try{
            sb = new StringBuilder(ul.userID).append(DELIMCHAR).append(ul.location).append(EOLCHAR);
            userWriter.write(sb.toString());
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void writeCountry(UserCountry uc){
        try{
            sb = new StringBuilder(uc.userID).append(DELIMCHAR).append(uc.country).append(EOLCHAR);
            userWriter.write(sb.toString());
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void writeRating(Rating rating){
        if(rating.sellerRating)
            writeSellerRating(rating);
        else
            writeBuyerRating(rating);
    }
    public void writeSellerRating(Rating sr){
        try{
            sb = new StringBuilder(sr.userID).append(DELIMCHAR).append(sr.rating).append(EOLCHAR);
            userWriter.write(sb.toString());
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void writeBuyerRating(Rating br){
        try{
            sb = new StringBuilder(br.userID).append(DELIMCHAR).append(br.rating).append(EOLCHAR);
            brWriter.write(sb.toString());
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void writeLatLong(LatLong ll){
        try{
            sb = new StringBuilder(ll.userID).append(DELIMCHAR).append(ll.latitude).append(DELIMCHAR).append(ll.longitude).append(EOLCHAR);
            userWriter.write(sb.toString());
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    


}

class Item{
    public String itemID, name, sellerID, startDate, endDate, firstBid, currentBid, description;
    
    public Item(String id){ 
        itemID = id;
    }
}

class Bid{
    public String itemID, bidderID, time, amount;
}

class UserLoc{
    public String userID, location;
    public UserLoc(){}
    public UserLoc(String uid, String loc){
        userID = uid;
        location = loc;
    }
    public boolean equals(Object o){
        UserLoc other = (UserLoc) o;
        return userID.equals(other.userID); 
    }
    public int hashCode(){
        return userID.hashCode(); 
    }
}

class UserCountry{
    public String userID, country;
    public UserCountry(){}
    public UserCountry(String uid, String cnt){
        userID = uid;
        country = cnt;
    }
    public boolean equals(Object o){
        UserCountry other = (UserCountry) o;
        return userID.equals(other.userID);
    }
    public int hashCode(){
        return userID.hashCode(); 
    }
}

class Rating{
    public String userID, rating;
    public boolean sellerRating = false; 
    public boolean equals(Object o){
        Rating other = (Rating) o;
        return userID.equals(other.userID) && (sellerRating == other.sellerRating); 
    }
    public int hashCode(){
        return userID.hashCode() + (sellerRating ? 1 : 0); 
    }
    public Rating(){}
    public Rating(String id, String rat, boolean sell){
        userID = id;
        rating = rat;
        sellerRating = sell;
    }
}

class LatLong{
    public String userID, latitude, longitude;
    public LatLong(){}
    public LatLong(String uid, String lati, String longi){
        userID = uid;
        latitude = lati;
        longitude = longi;
    }
    public boolean equals(Object o){
        LatLong other = (LatLong) o;
        return userID.equals(other.userID);
    }
    public int hashCode(){
        return userID.hashCode();
    }
}
