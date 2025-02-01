package Classes;
public class User {
    private String userID;
    private String rating;
    private String location;
    private String country;

    public User() {}

    public User(String userID, String rating, String location, String country) {
        this.userID = userID;
        this.rating = rating;
        this.location = location;
        this.country = country;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String toCSV() {
        // Escape and quote fields if they contain special characters
        return String.format("%s,%s,%s,%s",
            escapeCSV(userID),
            escapeCSV(rating),
            escapeCSV(location),
            escapeCSV(country)
        );
    }

    // Helper method to escape special characters
    private String escapeCSV(String value) {
        if (value == null || value.isEmpty()) {
            return ""; // Return empty string for null or empty values
        }
        // Escape double quotes and wrap the value in quotes if it contains special characters
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\""); // Escape internal double quotes
            return "\"" + value + "\"";
        }
        // System.out.println("Escaping: " + value + " -> " + escapeCSV(value));

        return value;
    }
}