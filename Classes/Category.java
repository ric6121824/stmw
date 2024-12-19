package Classes;
public class Category {
    private String itemID;
    private String category;

    // Correctly named constructor
    public Category(String itemID, String category) {
        this.itemID = itemID;
        this.category = category;
    }

    // Getter for CSV
    public String toCSV() {
        return String.format("%s,%s", itemID, category);
    }
}
