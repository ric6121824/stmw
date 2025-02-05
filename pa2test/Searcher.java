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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.naming.directory.SearchResult;

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


public class Searcher {

    public Searcher() {
    }

    public static void main(String[] args) throws Exception {
        try {

            if (args.length < 2) {
                System.out.println("Usage: java Searcher \"searchQuery\" <numResults> [-x longitude -y latitude -w width]");
                return;
            }

            // Path where the index is stored
            String indexPath = "indexes"; 

            // First argument is the search query
            String searchQuery = args[0]; 
            
            // Second argument is the number of results to show
            int numResults = Integer.parseInt(args[1]); 
            
            // // Search index in the "line" field for the query and return a defined maximum number of results
            // search(indexPath, "line", searchQuery, numResults); 

            // Optional spatial search parameters
            boolean spatialSearch = args.length == 6;
            double longitude = 0, latitude = 0, width = 0;

            if (spatialSearch) {
                longitude = Double.parseDouble(args[3]);
                latitude = Double.parseDouble(args[5]);
                width = Double.parseDouble(args[7]); //Radius in Kilometers
            }

            System.out.println("Searching for: " + searchQuery);
            search(indexPath, searchQuery, numResults, spatialSearch, longitude, latitude, width);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void search(String indexPath, String searchQuery, int numResults, boolean spatialSearch, double longitude, double latitude, double width) {
        try {
			// Init index reader
            Path path = Paths.get(indexPath);
            Directory directory = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(new ClassicSimilarity());

			// Read and prepare query
            SimpleAnalyzer analyzer = new SimpleAnalyzer(); 
			QueryParser queryParser = new QueryParser("SearchableText", new SimpleAnalyzer());
            Query query = queryParser.parse(searchQuery);

			// Search index (get top results)
            TopDocs topDocs = indexSearcher.search(query, numResults);
            System.out.println();
            System.out.println("#----------------------------------------#");
            System.out.println("Number of hits: " + topDocs.totalHits.value());
            System.out.println("#----------------------------------------#");
			
            // // Iterate over results and print them
			// StoredFields storedFields = indexSearcher.storedFields();
            List<SearchResult> results = new ArrayList<>();
            for (ScoreDoc hit : topDocs.scoreDocs) {
                Document doc = indexSearcher.storedFields().document(hit.doc);
                String itemId = doc.get("ItemId");
                String itemName = doc.get("Name");
                double itemLat = Double.parseDouble(doc.get("Latitude"));
                double itemLon = Double.parseDouble(doc.get("Longitude"));
                double price = doc.get("Currently") != null ? Double.parseDouble(doc.get("Currently")) : 0.0;

                double distance = spatialSearch ? haversine(latitude, longitude, itemLat, itemLon) : 0;

                if (!spatialSearch || distance <= width) {
                    results.add(new SearchResult(itemId, itemName, hit.score, distance, price));
                }
            }

            // **Sorting logic**:
            results.sort(Comparator
                    .comparing((SearchResult r) -> -r.luceneScore) // Higher Score first
                    .thenComparing(r -> r.distance) // Lower distance first
                    .thenComparing(r -> r.price)); //Lower price first

            // **Print sorted results**
            for (SearchResult r : results){
                System.out.println("ItemId: " + r.itemId + ", ItemName: " + r.itemName + ", Score: " + r.luceneScore + (spatialSearch ? ", Distance: " + r. distance + " km" : "") + ", Price: " + r.price);
            }
            indexReader.close();
            directory.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // **Haversine formula to calculate distance between two geo-coordinates**
    private static double haversine(double lat1, double lon1, double lat2, double lon2){
        final double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // **Helper class to store results**
    static class SearchResult {
        String itemId;
        String itemName;
        double luceneScore;
        double distance;
        double price;
        
        SearchResult(String itemId, String itemName, double luceneScore, double distance, double price){
            this.itemId = itemId;
            this.itemName = itemName;
            this.luceneScore = luceneScore;
            this.distance = distance;
            this.price = price;
        }
    }
}
