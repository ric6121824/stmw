import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Arrays;
import java.util.Date;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class SampleSearcher {

    public SampleSearcher() {
    }

    public static void main(String[] args) throws Exception {
        try {
            // Path where the index is stored
            String indexPath = "indexes"; 

            if (args.length > 0) {
                // Second argument is the number of results to show
                int numResults = Integer.parseInt(args[1]); 

                // First argument is the search query
                String searchQuery = args[0]; 
                System.out.println("Searching for: " + searchQuery);
                
                // Search index in the "line" field for the query and return a defined maximum number of results
                search(indexPath, "line", searchQuery, numResults); 
            } else {
                System.out.println("Usage: java SampleSearcher <searchQuery> <numResults>");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void search(String indexPath, String searchField, String searchText, int numResults) {
        try {
			// Init index reader
            Path path = Paths.get(indexPath);
            Directory directory = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(new ClassicSimilarity());

			// Read and prepare query
            SimpleAnalyzer analyzer = new SimpleAnalyzer(); 
			QueryParser queryParser = new QueryParser(searchField, analyzer);
            Query query = queryParser.parse(searchText);

			// Search index (get top results)
            TopDocs topDocs = indexSearcher.search(query, numResults);
            System.out.println();
            System.out.println("#----------------------------------------#");
            System.out.println("Number of hits: " + topDocs.totalHits.value());
            System.out.println("#----------------------------------------#");
			
            // Iterate over results and print them
			StoredFields storedFields = indexSearcher.storedFields();
            for (ScoreDoc hit : topDocs.scoreDocs) {
                Document doc = storedFields.document(hit.doc);  // Get document from hit
				Explanation explanation = indexSearcher.explain(query, hit.doc);
				
                System.out.println("doc_id: " + doc.get("doc_id") + ", score: " + hit.score + " [" + doc.get("line") + "]");
				System.out.println("Explanation: " + explanation.toString());
            }

            indexReader.close();
            directory.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
