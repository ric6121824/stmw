import java.sql.*;

public class jdbc {
    public static void main(String[] args) {
        // SQL query to execute 
        String query = "SELECT * FROM Users LIMIT 10;";
        System.out.println("Query: " + query);

        // Establish the connection using the DbManager
        try (Connection conn = DbManager.getConnection(true); // Read-only connection
                Statement stmt = conn.createStatement(); // Statement to execute the query     
                ResultSet rs = stmt.executeQuery(query)) {  // Execute query

            // Process and print the results
            while (rs.next()) {
                // Get UserId and UserName attributes from the table
                int id = rs.getInt("UserId"); 
                String name = rs.getString("UserName"); 

                System.out.println("UserId: " + id + ", UserName: " + name);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
