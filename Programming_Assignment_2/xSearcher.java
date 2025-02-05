import java.io.StringReader;
import java.io.File;
import java.nio.file.*;
import java.lang.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import java.io.*;
import java.nio.file.Path;

public class xSearcher {

	final static double distLatDegree = 111.7; //one degree of latitude in km (maximum length for larger bounding box). nabbed from https://en.wikipedia.org/wiki/Latitude#Meridian_distance_on_the_ellipsoid
	final static double earthRadius = 6371;
	static double x = -181; //latlongwidth
	static double y = -181;
	static double w = -1;
	static boolean bigSearch = false;

	static IndexSearcher indexSearcher;

    public xSearcher() {}
    public static void main(String[] args) throws Exception {
		String usage = "java Searcher";
		String indexLoc = "indexes";
		//System.out.println(distOnEarth(10.0, 40.1, 10.1, 39.8));
		if(args.length < 1){
			System.out.println("Please specify a search term.");
			System.exit(0);
		}
		if(args.length > 1){
			if(parseArgType(Arrays.copyOfRange(args, 1, args.length)))
				bigSearch = true;
			else{
				System.out.println("Additional arguments couldn't be parsed.");
				System.exit(0);
			}
		}
		TopDocs topDocs = search(args[0], indexLoc);
		ArrayList<ItemResult> results = new ArrayList<ItemResult>();
		for(ScoreDoc sd : topDocs.scoreDocs){
			Document doc = indexSearcher.storedFields().document(sd.doc);
			results.add(new ItemResult(Integer.parseInt(doc.get("id")), doc.get("name"), Double.parseDouble(doc.get("price")), sd.score));
		}

		if(bigSearch){ //if the spatial arguments were added, oh boy! we're filtering the results
			//System.out.println("a bunch of other stuff");
			double boxLong = kmToDegreeLong(w, y);
			double boxLat = y / distLatDegree;
			ResultSet rs = retrieveItemsSpatial(boxLong, boxLat);
			//rs.last();
			//System.out.println(w + "; " + y + "; " + boxLong);
			//System.out.println(rs.getRow());
			ArrayList<ItemResult> keepers = new ArrayList<ItemResult>();
			for(ItemResult ir : results){
				rs.first();
				while(rs.next()){
					int rid = rs.getInt("id");
					if(rid > ir.id)
						break; //items are sorted by id in the query. if the id in the resultset is greater than the one we're looking for, it's not coming anymore
					if(ir.id == rid){
						ir.dist = distOnEarth(rs.getDouble("longitude"), rs.getDouble("latitude"), x, y);
						/*
						ir.lon = rs.getDouble("longitude");
						ir.lat = rs.getDouble("latitude");
						ir.queryLon = x;
						ir.queryLat = y;*/
						if(ir.dist <= w)
							keepers.add(ir); //i recall removing items during iteration could cause issues, so i'm transferring the wanted ones to another list instead.
					}
				}
			}
			results = keepers; //the big switcharoo!
		}

		Collections.sort(results);

		System.out.println("Total results: " + results.size());
		if(results.size() == 0)
			System.out.println("Better luck next time!");
		
		for(ItemResult res : results){
			System.out.println(elaborateResult(res));
		}

    }

	private static String elaborateResult(ItemResult res){
		StringBuilder sb = new StringBuilder();
		sb.append(res.id); //learned the hard way not to do new StringBuilder(res.id) instead :)
		sb.append(", ");
		sb.append(res.name);
		sb.append(", score: ");
		sb.append(res.score);
		if(res.dist != -1){
			sb.append(", dist: ");
			sb.append(res.dist);
		}
		sb.append(", price:");
		sb.append(res.price);
		return sb.toString();
	}
    
    private static TopDocs search(String searchText, String p) {   
		//System.out.println("Running search(" + searchText + ")");
		try {   
			Path path = Paths.get(p);
			Directory directory = FSDirectory.open(path);       
			IndexReader indexReader =  DirectoryReader.open(directory);
			indexSearcher = new IndexSearcher(indexReader);
			QueryParser queryParser = new QueryParser("searchableText", new SimpleAnalyzer());  
			Query query = queryParser.parse(searchText);
			TopDocs topDocs = indexSearcher.search(query,10000);
			/*System.out.println("Number of Hits: " + topDocs.totalHits);
			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {           
			Document document = indexSearcher.doc(scoreDoc.doc);
			System.out.println("item ID: " + document.get("id") 
					+ ", score: " + scoreDoc.score + " [" + document.get("name") +"]");
			}*/
			return topDocs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }


	enum ArgType {
		None, X, Y, W;
	}

	private static boolean parseArgType(String[] rgs){ //returns true if the arguments can be parsed, false if not
		ArgType arg = ArgType.None;
        try {
            for (String rg : rgs) {
				switch (arg) {
				case X: //already read a -x, parse this as x and so on.
					x = Double.parseDouble(rg);
					arg = ArgType.None;
					break;
				case Y:
					y = Double.parseDouble(rg);
					arg = ArgType.None;
					break;
				case W:
					w = Double.parseDouble(rg);
					/*if(w > earthRadius * Math.PI){ //contingency? decided it's not necessary...
						System.out.println("Search radius spans entire globe")
					}*/
					arg = ArgType.None;
					break;
				case None: //next argument should be an instructive one (-xyw)
					switch (rg) {
					case "-x":
						arg = ArgType.X;
						break;
					case "-y":
						arg = ArgType.Y;
						break;
					case "-w":
						arg = ArgType.W;
						break;
					default:
                        return false; //don't know what to do with this arguments, parsing failed, try again
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
		if(x != -181 && y != -181 && w != -1) //all arguments have changed from the default
			return true;
		else
			return false;
	}

	private static double kmToDegreeLong(double km, double lat){ //converts a distance at a certain latitude to degrees in longitude
		//adapted this method from https://en.wikipedia.org/wiki/Longitude#Length_of_a_degree_of_longitude
		return km / (Math.PI * earthRadius * Math.cos(Math.toRadians(lat)) / 180);
	}


	private static double degreeBounder(double degLong){ //calculates correct degree of longitude if values exceed 180/-180
		double corDeg = degLong % 360;
		if(corDeg > 180)
			corDeg -= 360;
		else if(corDeg < -180)
			corDeg += 360;
		return corDeg;
	}


	private static ResultSet retrieveItemsSpatial(double degLong, double degLat){ //retrieves items within a bounding box from the sql database
		StringBuilder sQuery = new StringBuilder("SELECT Geocoordinates.item_id AS id, latitude, longitude FROM Geocoordinates NATURAL JOIN ItemLatLon WHERE MBRContains("); //spatial query beginning
		sQuery.append(buildBoundingBox(x - degLong, x + degLong, y + degLat, y - degLat));
		sQuery.append(", longlat)");

		if((x - degLong) < -180){//in case the box should stretch over the switch from -180 to 180
			sQuery.append(" OR MBRContains(");
			sQuery.append(buildBoundingBox(degreeBounder(x - degLong), 180, y + degLat, y - degLat));
			sQuery.append(", longlat)");
		}
		if((x + degLong) > 180){
			sQuery.append(" OR MBRContains(");
			sQuery.append(buildBoundingBox(-180, degreeBounder(x + degLong), y + degLat, y - degLat));
			sQuery.append(", longlat)");
		}
		sQuery.append(" ORDER BY id ASC;");
		//System.out.println(sQuery.toString());
		try{
			Connection conn = DbManager.getConnection(true);
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			return stmt.executeQuery(sQuery.toString());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	private static String buildBoundingBox(double wLong, double eLong, double tLat, double bLat){ //builds a string representing a polygon in sql to draw bounding boxes. west long, east long, top lat, bottom lat
		StringBuilder box = new StringBuilder("ST_GeomFromText('Polygon((");
		box.append(wLong);
		box.append(" ");
		box.append(tLat);
		box.append(", ");
		box.append(eLong);
		box.append(" ");
		box.append(tLat);
		box.append(", ");
		box.append(eLong);
		box.append(" ");
		box.append(bLat);
		box.append(", ");
		box.append(wLong);
		box.append(" ");
		box.append(bLat);
		box.append(", ");
		box.append(wLong);
		box.append(" ");
		box.append(tLat);
		box.append("))')");
		return box.toString();
	}

	public static double distOnEarth(double lon1, double lat1, double lon2, double lat2){
		//adapted from https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula because 3d geometry is too intense for me
		//tested on some test places from google maps. note: google maps displays latitude first :upside_down:
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1); 
		double a = 
			Math.sin(dLat/2) * Math.sin(dLat/2) +
			Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
			Math.sin(dLon/2) * Math.sin(dLon/2)
			; 
		return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	}

}

class ItemResult implements Comparable<ItemResult>{ //convenience class to save any combination of values we have to work with
	public int id; //was kinda worried int wouldn't suffice and we'd have to use longs but seems fine looking at item IDs DESC
	//public double lon = -181;
	//public double lat = -181;
	public double dist = -1;

	public String name = null;
	public double score = -1;
	public double price = -1;

	//public double queryLon = -181; //the latlong of the call to the program. kind of an ugly type of hack to have these in here also but it'll do
	//public double queryLat = -181;

	public ItemResult(){}

	public ItemResult(int i){
		id = i;
	}

	public ItemResult(int i, String nam, double pric, double scor){
		id = i;
		name = nam;
		price = pric;
		score = scor;
	}

	/*public boolean isInRange(double range){ //whether this item is in range
		if(lat == -181 || lon == -181) //latlong not been set. shouldn't occur with the flow of the program but safety second, or so
			return false;
		return (Searcher.distOnEarth(lon, lat, queryLon, queryLat) < range);
	}*/

	public int compareTo(ItemResult other){ //works in multiple modes to be compatible with different sets of available data
		if(score != other.score){
			return Double.compare(other.score, score);
		}
		if(dist != -1 && other.dist != -1 && dist != other.dist){
			return Double.compare(dist, other.dist);
		}
		/*if(lon != -1 && other.lon != -1){
			int res1 = Double.compare(Searcher.distOnEarth(other.lon, other.lat, other.queryLon, other.queryLat), Searcher.distOnEarth(lon, lat, queryLon, queryLat)); //reverse order b/c lower distance is better
			if(res1 != 0)
				return res1;
		}*/
		return Double.compare(price, other.price);

	}
}
