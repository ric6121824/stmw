package Classes;
public class Category {
    private String itemID;
    private String category;

    // Correctly named constructor
    public Category(String itemID, String category) {
        this.itemID = itemID;
        this.category = category;
    }

    // Getters and setters for all fields
    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    // Getters and setters for all fields
    public String getCategory() {
        return itemID;
    }

    public void setCategory(String itemID) {
        this.itemID = itemID;
    }

    // Getter for CSV
    public String toCSV() {
        // Escape and quote fields if they contain special characters
        return String.format("%s,%s",
        escapeCSV(itemID),
        escapeCSV(category)
    );
    }

    // Helper method to escape special characters
    private String escapeCSV(String value) {
        if (value == null) {
            return ""; // Return empty string for null or empty values
        }
        // Escape double quotes and wrap the value in quotes if it contains special characters
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\""); // Escape internal double quotes
            return "\"" + value + "\"";
        }
        return value;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category other = (Category) obj;
        return itemID.equals(other.itemID) && category.equals(other.category);
    }

    // @Override
    // public int hashCode() {
    //     return Object.hash(itemID, category);
    // }
}
