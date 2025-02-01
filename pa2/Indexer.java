import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Indexer {
    public static void main(String[] args) throws IOException {
        try {
            String indexPath = "indexes"; // Path where the index is stored
            rebuildIndexes(indexPath);  // Rebuild the index
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static void insertDoc(IndexWriter i, String doc_id, String line){
		Document doc = new Document();

		// Defining fields
		doc.add(new TextField("doc_id", doc_id, Field.Store.YES));
		doc.add(new TextField("line", line, Field.Store.YES));

		// Add document
		try { i.addDocument(doc); } catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void rebuildIndexes(String indexPath) {
		try {
			// Directory for the index
			Path path = Paths.get(indexPath);
			System.out.println("Indexing to directory: " + indexPath);
			Directory directory = FSDirectory.open(path);

			// Define analyzer and similarity
			IndexWriterConfig config = new IndexWriterConfig(new SimpleAnalyzer());
			config.setSimilarity(new ClassicSimilarity());
			IndexWriter i = new IndexWriter(directory, config);
			
			// Clear existing index
			i.deleteAll();
			
			// Add documents to index
			insertDoc(i, "1", "The old night keeper keeps the keep in the town");
			insertDoc(i, "2", "In the big old house in the big old gown.");
			insertDoc(i, "3", "The house in the town had the big old keep");
			insertDoc(i, "4", "Where the old night keeper never did sleep.");
			insertDoc(i, "5", "The night keeper keeps the keep in the night");
			insertDoc(i, "6", "And keeps in the dark and sleeps in the light.");

			//Close writer
			i.close();
			directory.close();

			System.out.println("Indexing finished");
		} catch (Exception e) { e.printStackTrace(); }
	}
}
