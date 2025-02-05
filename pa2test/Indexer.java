import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.IOException;
import java.sql.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;


public class Indexer {
    public static void main(String[] args) throws IOException {
        try {
            String indexPath = "/stmw/student_workspace/indexes"; // Path where the index is stored
            rebuildIndexes(indexPath);  // Rebuild the index
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static void rebuildIndexes(String indexPath) {
		try {
			// Create a Path object and ensure the directory exists
			Path path = Paths.get(indexPath);
			if (!java.nio.file.Files.exists(path)) {
				java.nio.file.Files.createDirectories(path);
			}
			System.out.println("Indexing to directory: " + path.toAbsolutePath());

			// 1. Create a SimpleFSDirectory instance with the NoLockFactory
			Directory directory = FSDirectory.open(path/* , NoLockFactory.INSTANCE*/);
			// // 2. Or set lock factory instead
			// FSDirectory directory = FSDirectory.open(path);
        	// directory.setLockFactory(NoLockFactory.INSTANCE);
			// // 3. Or use MMapDirectory instead
			// Directory directory = MMapDirectory.open(path);
			// // 4. Create a NIOFSDirectory with a NoLockFactory
			// Directory directory = new NIOFSDirectory(path, NoLockFactory.INSTANCE);

			//The NoSuchFileException or AlreadyClosedException issues are resolved by running in Windows, not in Mac.


			// Define analyzer and similarity
			IndexWriterConfig config = new IndexWriterConfig(new SimpleAnalyzer());
			// config.setUseCompoundFile(false);
			// config.setSimilarity(new ClassicSimilarity());
			IndexWriter i = new IndexWriter(directory, config);
			
			// Clear existing index
			i.deleteAll();

			// Connect to MySQL and index item datas
			indexDatabase(i);

			//Close writer
			i.close();
			directory.close();

			System.out.println("Indexing finished");
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static void indexDatabase(IndexWriter i) throws IOException {
		// Connecting to MySQL ad database
		String url = "jdbc:mysql://localhost:3306/ad"; // MySQL server
		String user = "root"; // Default MySQL user
		String password = ""; // Default MaSQL password

		String query = 	"SELECT it.ItemId, it.Name, it.Description, it.Currently, geo.Latitude, geo.Longitude FROM Items it INNER JOIN ItemLatLon geo ON it.ItemId = geo.ItemId WHERE geo.Latitude IS NOT NULL AND geo.Longitude IS NOT NULL ORDER BY ItemId;";
		
		try (Connection conn = DriverManager.getConnection(url, user, password);
			 PreparedStatement stmt = conn.prepareStatement(query);
			 ResultSet rs = stmt.executeQuery()){

				while (rs.next()) {
					// Read Data
					String itemId = rs.getString("ItemId");
					String name = rs.getString("Name");
					String description = rs.getString("Description");
					double currently = rs.getDouble("Currently");
					// String category = rs.getString("Category");
					double latitude = rs.getDouble("Latitude");
					double longitude = rs.getDouble("Longitude");
					
					// Create Lucene Document
					Document doc = new Document();

					// Defining fields
					doc.add(new TextField("ItemId", itemId, Field.Store.YES));
					doc.add(new TextField("Name", name, Field.Store.YES)); // Index the Name and Description as text for full-text search
					doc.add(new TextField("Description", description, Field.Store.YES)); 
					doc.add(new DoublePoint("Currently", currently));// Index the Currently (price) as a DoublePoint for numeric queries
					doc.add(new StoredField("Currently", currently));// To store the value so it can be retrieved
					// doc.add(new TextField("Category", category, Field.Store.YES)); // Index the Category as text for full-text search
					doc.add(new DoublePoint("Latitude", latitude));
					doc.add(new DoublePoint("Longitude", longitude));
					// Store the values for retrieval
					doc.add(new StoredField("Latitude", latitude));
					doc.add(new StoredField("Longitude", longitude));

					// Combine the text fields into a single field for searching across multiple attributes:
					String searchableText = name + " " + description /* + " " + category */;
					doc.add(new TextField("SearchableText", searchableText, Field.Store.NO));

					// Debug print:
    				// System.out.println("Indexing document: Searchable Texts=" + searchableText);

					// Add index
					i.addDocument(doc);
				}
			 } catch (SQLException e) {
				e.printStackTrace();
			 }


	}
}
