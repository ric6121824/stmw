package Classes;
public class Item {
    private String itemID;
    private String name;
    private String currently;
    private String buyPrice;
    private String firstBid;
    private int numberOfBids;
    private String location;
    private String latitude;
    private String longitude;
    private String country;
    private String started;
    private String ends;
    private String userID;
    private String description;

    public Item() {}

    public Item(String itemID) {
        this.itemID = itemID;
    }

    // Getters and setters for all fields
    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrently() {
        return currently;
    }

    public void setCurrently(String currently) {
        this.currently = currently;
    }

    public String getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(String buyPrice) {
        this.buyPrice = buyPrice;
    }

    public String getFirstBid() {
        return firstBid;
    }

    public void setFirstBid(String firstBid) {
        this.firstBid = firstBid;
    }

    public int getNumberOfBids() {
        return numberOfBids;
    }

    public void setNumberOfBids(int numberOfBids) {
        this.numberOfBids = numberOfBids;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getEnds() {
        return ends;
    }

    public void setEnds(String ends) {
        this.ends = ends;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toCSV() {
        // Escape and quote fields if they contain special characters
        return String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s",
            escapeCSV(itemID),
            escapeCSV(name),
            escapeCSV(currently),
            escapeCSV(buyPrice),
            escapeCSV(firstBid),
            numberOfBids,
            escapeCSV(location),
            escapeCSV(latitude),
            escapeCSV(longitude),
            escapeCSV(country),
            escapeCSV(started),
            escapeCSV(ends),
            escapeCSV(userID),
            escapeCSV(description)
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
}
