/* Parser skeleton for processing item-???.xml files. Must be compiled in
 * JDK 1.5 or above.
 *
 * Instructions:
 *
 * This program processes all files passed on the command line (to parse
 * an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 *
 */

 import java.io.*;
 import java.text.*;
 import java.util.*;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.*;
 import org.xml.sax.helpers.DefaultHandler;

import Classes.Bid;
import Classes.Category;
import Classes.Item;
import Classes.User;


public class PingsSAX extends DefaultHandler {
	private StringBuilder categoryBuilder;

	//Variables to hold current data
	private Set<User> users = new HashSet<>();
	private Set<Item> items = new HashSet<>();
	private Set<Category> categories = new HashSet<>();
	private Set<Bid> bids = new HashSet<>();
	
	// Current element being processed
	private String currentElement = "";
	
	// Temporary storage for current item being processed
	private Item currentItem = null;
	private User currentUser = null;
	private Bid currentBid = null;

	

	public PingsSAX() {
		super();
		categoryBuilder = new StringBuilder();
	}

	/*
	 * Returns the amount (in XXXXX.xx format) denoted by a money-string
	 * like $3,453.23. Returns the input if the input is an empty string.
	 */
    static String strip(String money) {
        if (money.equals(""))
            return money;
        else {
            double amount = 0.0;
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            try {
                amount = nf.parse(money).doubleValue();
            } catch (ParseException e) {
                System.err.println("Invalid money format: " + money);
                System.exit(20);
            }
            nf.setGroupingUsed(false);
            return nf.format(amount).substring(1);
        }
    }

	////////////////////////////////////////////////////////////////////
	// Event handlers.
	////////////////////////////////////////////////////////////////////

	@Override
	public void startDocument() {
		System.out.println("Start document");
	}

	@Override
	public void endDocument() {
		System.out.println("End document");
    	writeUsersToCSV();
    	writeItemsToCSV();
    	writeCategoriesToCSV();
    	writeBidsToCSV();
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		currentElement = qName;

		switch (qName) {
			case "Category":
				// categoryBuilder = new StringBuilder();
				categoryBuilder.setLength(0);
				System.out.println("Start Element: Category - Cleared categoryBuilder");
				break;
			case "Item":
				currentItem = new Item();
				currentItem.setItemID(atts.getValue(("ItemID")));
				break;
			case "Seller":
			case "Bidder":
				currentUser = new User();
				if (atts.getValue("UserID") != null) {
					currentUser.setUserID(atts.getValue("UserID"));
				}
				if (atts.getValue("Rating") != null) {
					currentUser.setRating(atts.getValue("Rating"));
				}
				break;
			case "Location":
				if (atts.getValue("Latitude") != null){
					currentItem.setLatitude(atts.getValue("Latitude"));
				}
				if (atts.getValue("Longitude") != null) {
					currentItem.setLongitude(atts.getValue("Longitude"));
				}
			case "Country":
				break;
		}

		// Initialize objects based on element
		if ("".equals(uri))
			System.out.println("Start element: " + qName);
		else
			System.out.println("Start element: {" + uri + "}" + name);
		for (int i = 0; i < atts.getLength(); i++) {
			System.out.println("Attribute: " + atts.getLocalName(i) + "=" + atts.getValue(i));
		}
		
	}


	@Override
	public void characters(char ch[], int start, int length) {
		String value = new String(ch, start, length).trim();
		if (!value.isEmpty()) {

			// Process the element value based on currentElement
			switch (currentElement) {
				case "Name":
					currentItem.setName(value);
					break;
				case "Category":
					if (categoryBuilder != null) {
						categoryBuilder.append(value);
						System.out.println("Appending to categoryBuilder: " + value);
					}
					// categories.add(new Category(currentItem.getItemID(), value));
					break;
				case "Location":
					if (currentUser != null) {
						currentUser.setLocation(value);
					}
					break;
				case "Country":
					if (currentUser != null) {
						currentUser.setCountry(value);
					}
					break;
				// Add more cases for other elements here
				default:
					break;
			}
		} else return;

		// System.out.print("Characters:    \"");
		// for (int i = start; i < start + length; i++) {
		// 	switch (ch[i]) {
		// 		case '\\':
		// 			System.out.print("\\\\");
		// 			break;
		// 		case '"':
		// 			System.out.print("\\\"");
		// 			break;
		// 		case '\n':
		// 			System.out.print("\\n");
		// 			break;
		// 		case '\r':
		// 			System.out.print("\\r");
		// 			break;
		// 		case '\t':
		// 			System.out.print("\\t");
		// 			break;
		// 		default:
		// 			System.out.print(ch[i]);
		// 			break;
		// 	}
		// }
		// System.out.print("\"\n");
	}

	
	@Override
	public void endElement(String uri, String name, String qName) {
		// Add completed objects to their respective sets
		switch (qName) {
			case "Name":
				if (currentItem != null) {
					currentItem.setName(categoryBuilder.toString().trim());
				}
				break;
			case "Category":
				if (currentItem != null && categoryBuilder != null) {
					String category = categoryBuilder.toString().trim();
					System.out.println("End Element: Category - Finalized category: " + category);
					if (!category.isEmpty()) {
						categories.add(new Category(currentItem.getItemID(), category));
					} 
				} else {
					System.out.println("End Element: Category - categoryBuilder was null");
				}
				break;
			case "Item":
				if (currentItem != null) {
					items.add(currentItem);
					currentItem = null; // Reset for the next item
				}
				break;
			case "Seller":
			case "Bidder":
				if (currentUser != null) {
					users.add(currentUser);
					currentUser = null;
				}
				break;
			case "Bid":
				bids.add(currentBid);
				break;
		}
		if (currentUser == null) {
			// System.err.println("Error: currentUser is null when processing " + qName);
			return; // Prevent further execution to avoid crashing.
		} 
		else {
		System.out.println("UserID: " + currentUser.getUserID());
		System.out.println("Location: " + currentUser.getLocation());
		System.out.println("Country: " + currentUser.getCountry());
		}
		if ("Category".equals(currentElement)) {
			System.out.println("Accumulated Category Text: " + categoryBuilder.toString());
		}
		if ("Category".equals(qName)) {
			System.out.println("Processed Category: " + categoryBuilder.toString().trim());
		}
		
		if ("".equals(uri))
			System.out.println("End element: " + qName);
		else
			System.out.println("End element:   {" + uri + "}" + name);

	}
	
	// Methods to write data to CSV files
	private void writeUsersToCSV(){
		try {
			// Create the "Export" directory if it doesn't exist
			File exportDir = new File("Export");
			if (!exportDir.exists()){
				exportDir.mkdirs();
			}

			// Write the CSV file
			try (PrintWriter writer = new PrintWriter(new File(exportDir, "Users.csv"))) {
				writer.println("UserID,Rating,Location,Country");
				for (User user : users) {
					writer.println(user.toCSV());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void writeItemsToCSV(){
		try {
			// Create the "Export" directory if it doesn't exist
			File exportDir = new File("Export");
			if (!exportDir.exists()){
				exportDir.mkdirs();
			}
			
			// Write the CSV file
			try (PrintWriter writer = new PrintWriter(new File(exportDir, "Items.csv"))) {
				writer.println("ItemID,Name,Currently,Buy_Price,First_Bid,Number_Of_Bids,Location,Latitude,Longitude,Country,Started,Ends,UserID,Description");
				for (Item item : items) {
					writer.println(item.toCSV());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeCategoriesToCSV(){
		try {
			// Create the "Export" directory if it doesn't exist
			File exportDir = new File("Export");
			if (!exportDir.exists()){
				exportDir.mkdirs();
			}
			
			// Write the CSV file
			try (PrintWriter writer = new PrintWriter(new File(exportDir,"Catecories.csv"))) {
				writer.println("ItemID,Category");
				for (Category category : categories) {
					writer.println(category.toCSV());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeBidsToCSV(){
		try {
			// Create the "Export" directory if it doesn't exist
			File exportDir = new File("Export");
			if (!exportDir.exists()){
				exportDir.mkdirs();
			}
			
			// Write the CSV file
			try (PrintWriter writer = new PrintWriter(new File(exportDir,"Bids.csv"))) {
			writer.println("ItemID,UserID,Time,Amount");
			for (Bid bid : bids) {
				if (bid != null) {
					writer.println(bid.toCSV());
				} else {
					System.err.println("Warning: Encountered a null bid");
				}
			}
		}
		} catch (IOException e) {
			System.err.println("Error writing bids to CSV: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) throws Exception {
		// Create SAX parser instance
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		PingsSAX handler = new PingsSAX();

		// Parse each file provided on the command line
		for (String fileName : args) {
			System.out.println("Processing file: " + fileName);
			parser.parse(new InputSource(new FileReader(fileName)), handler);
		}
	}
}
