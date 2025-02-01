import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbManager {
	// The DbManager class is responsible for managing database connections to a MySQL database.
	// It provides a utility method to establish a connection to the database, with support for read-only connections.

    // Database connection configuration
    static private String databaseURL = "jdbc:mysql://localhost:3306/";  // The URL to connect to the MySQL database, including the host (localhost) and port (3306).
    static private String dbname = "ad";  // The name of the database we are connecting to.
    static private String username = "root"; // The username to authenticate the connection.
    static private String password = ""; // The password to authenticate the connection.

    /**
     * Opens a database connection.
     * 
     * @param dbName The name of the database.
     * @param readOnly True if the connection should be opened as read-only (no modifications allowed).
     * @return An open java.sql.Connection object.
     * @throws SQLException If there is an error while establishing the connection.
     */
    public static Connection getConnection(boolean readOnly) throws SQLException {        
        // Establish a connection to the MySQL database using the provided configuration details.
        // DriverManager.getConnection() attempts to connect to the database 
        Connection conn = DriverManager.getConnection(databaseURL + dbname, username, password);

        // Set the connection to be read-only or read-write based on the parameter passed.
        conn.setReadOnly(readOnly);        

        return conn;
    }

    // Private constructor to prevent instantiation of this class. This class is a utility class
    // designed to provide a static method to retrieve database connections, so no instances of DbManager are needed.
    private DbManager() {}

    // Static block to load the MySQL JDBC driver when the class is first loaded by the JVM.
    static {
        try {
            // Dynamically load the MySQL JDBC driver class. This step ensures that the necessary class is available
            // for creating database connections.
            Class.forName("com.mysql.cj.jdbc.Driver");  // Load the MySQL driver class.
        } catch (Exception e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
