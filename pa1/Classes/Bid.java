package Classes;
public class Bid {
    
    private String itemID;
    private String userID;
    private String time;
    private String amount;

    public Bid() {}

    // Getters and setters for all fields
    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String toCSV() {
        return String.format("%s,%s,%s,%s", itemID, userID, time, amount);
    }
}
