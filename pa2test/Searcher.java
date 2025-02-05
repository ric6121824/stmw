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

            if (args.length < 2 ) {
                System.out.println("Usage: java Searcher \"list_of_keywords\" number_of_results [-x longitude -y latitude -w width]");
                return;
            }

            // Path where the index is stored
            String indexPath = "indexes"; 

            // First argument is the search query
            String searchQuery = args[0]; 
            
            // Second argument is the number of results to show
            int numResults = Integer.parseInt(args[1]); 

            // Optional spatial search parameters
            boolean spatialSearch = args.length >= 5;
            double longitude = 0, latitude = 0, width = 0;

            if (spatialSearch) {
                if ((args.length > 2 && args.length < 8) || args.length > 8){
                    System.out.println("Invalid spatial search arguments. Expected format: -x <longitude> -y <latitude> -w <width>");
                    return;
                } else {
                longitude = Double.parseDouble(args[3]);
                latitude = Double.parseDouble(args[5]);
                width = Double.parseDouble(args[7]); //Radius in Kilometers
                }
            }

            System.out.println("Searching for: " + searchQuery);
            search(indexPath, "SearchableText", searchQuery, numResults, spatialSearch, longitude, latitude, width);

            System.out.println("Done processing search results. Please be aware that the 4th input is longitude rather than latitude if you got less results than expected.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void search(String indexPath, String searchField, String searchQuery, int numResults, boolean spatialSearch, double longitude, double latitude, double width) {
        try {
			// Init index reader
            Path path = Paths.get(indexPath);
            Directory directory = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(new ClassicSimilarity());

			// Read and prepare query
            SimpleAnalyzer analyzer = new SimpleAnalyzer(); 
			QueryParser queryParser = new QueryParser(searchField, new SimpleAnalyzer());
            Query query = queryParser.parse(searchQuery);

			// Search index (get top results)
            TopDocs topDocs = indexSearcher.search(query, numResults);
            System.out.println();
            System.out.println("#----------------------------------------#");
            System.out.println("Number of hits: " + topDocs.totalHits.value());
            System.out.println("#----------------------------------------#");
			
            // Iterate over results and print them
            List<SearchResult> results = new ArrayList<>();

            for (ScoreDoc hit : topDocs.scoreDocs) {
                Document doc = indexSearcher.storedFields().document(hit.doc);
                String itemId = doc.get("ItemId");
                String itemName = doc.get("Name");
                double price = doc.get("Currently") != null ? Double.parseDouble(doc.get("Currently")) : 0.0;
                String latStr = doc.get("Latitude");
                String lonStr = doc.get("Longitude");
                double itemLat = (latStr != null) ? Double.parseDouble(latStr) : 0.0;
                double itemLon = (lonStr != null) ? Double.parseDouble(lonStr) : 0.0;

                double distance = 0.0;
                
                // **Bounding Box Filtering**
                
                if (spatialSearch) {
                    // System.out.println("Creating Bounding Box...");
                    double[] boundingBox = getBoundingBox(latitude, longitude, width);
                    double minLat = boundingBox[0];
                    double maxLat = boundingBox[1];
                    double minLon = boundingBox[2];
                    double maxLon = boundingBox[3];
                    // System.out.println(String.format("Boundary: MinLat = %.4f, MaxLat = %.4f, MinLon = %.4f, MaxLon = %.4f.", minLat, maxLat, minLon, maxLon));
                    
                    // System.out.println("Filtering through bounding box...");
                    if (itemLat < minLat || itemLat > maxLat || itemLon < minLon || itemLon > maxLon) {
                        continue; // Skip items outside bounding box
                    }

                    // **Final Precise Distance Calculation**
                    distance = haversine(latitude, longitude, itemLat, itemLon);

                    if (distance > width) {
                        continue;  // Exclude items beyond width
                    }
                }

                results.add(new SearchResult(itemId, itemName, hit.score, distance, price));
                // System.out.println("Filtered out ItemId: " + itemId + " (distance = " + distance + " km, width = " + width + ")");
                

                // double distance = 0.0;
                // if (spatialSearch) {
                //     if (itemLat != 0.0 && itemLon != 0.0) { // Ensure valid coordinates
                //         distance = haversine(latitude, longitude, itemLat, itemLon);
                //         if (distance > width) {
                //             continue; // Exclude items outside the search radius
                //         }
                //     } else {
                //         continue; // Skip documents with missing coordinates
                //     }
                // }
                // results.add(new SearchResult(itemId, itemName, hit.score, distance, price));

            }

            // **Sorting logic**:
            results.sort(Comparator
                    .comparing((SearchResult r) -> -r.luceneScore) // Higher Score first
                    .thenComparing(r -> r.distance) // Lower distance first
                    .thenComparing(r -> r.price)); //Lower price first

            // **Print sorted results**
            System.out.println("Total results found: " + results.size());

            for (SearchResult r : results){
                System.out.println(String.format(
                    "ItemId: %s, ItemName: \"%s,\" Score: %.2f%s, Price: %.2f",
                    r.itemId, r.itemName, r.luceneScore, 
                    (spatialSearch ? String.format(", Distance: %.2f km", r. distance) : ""), r.price
                ));
            }
            indexReader.close();
            directory.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // **Bounding Box Calculation**
    public static double[] getBoundingBox(double lat, double lon, double radiusKm){
        double latDiff = radiusKm / 111.0;
        double lonDiff = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latDiff;
        double maxLat = lat + latDiff;
        double minLon = lon - lonDiff;
        double maxLon = lon + lonDiff;

        // Fix for latitude limits
        if (minLat < -90) minLat = -90;
        if (maxLat >  90) maxLat =  90;

        // Fix for longitude limits
        if (minLon < -180) minLon += 360;
        if (maxLon >  180) maxLon -= 360;
        
        return new double[]{minLat, maxLat, minLon, maxLon};
    }

    // **Haversine formula to calculate distance between two geo-coordinates**
    private static double haversine(double lat1, double lon1, double lat2, double lon2){
        // System.out.println("Calculating distance: from (" + lat1 + ", " + lon1 + ") to (" + lat2 + ", " + lon2 + ")");
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
