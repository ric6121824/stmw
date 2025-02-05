import java.io.StringReader;
import java.io.File;
import java.nio.file.*;
import java.lang.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class xIndexer {
    public xIndexer() {}
    public static void main(String args[]) {
		String usage = "java xIndexer";
		rebuildIndexes("indexes");
    }

    /*public static void insertDoc(IndexWriter i, String doc_id, String line){
		Document doc = new Document();
		doc.add(new TextField("doc_id", doc_id, Field.Store.YES));
		doc.add(new TextField("line", line,Field.Store.YES));
		try { i.addDocument(doc); } catch (Exception e) { e.printStackTrace(); }
    }*/
	public static void insertDoc(IndexWriter i, Document docu){
		/*Document doc = new Document();
		doc.add(new TextField("id", id, Field.Store.YES));
		doc.add(new TextField("name", name, Field.Store.YES));
		doc.add(new TextField("price", price, Field.Store.YES));
		doc.add(new TextField("searchText", name + cats + desc, Field.Store.NO));*/
		try { i.addDocument(docu); } catch (Exception e) { e.printStackTrace(); }
	}
    public static void rebuildIndexes(String indexPath) {
	try {
	    Path path = Paths.get(indexPath);
	    System.out.println("Indexing to directory '" + indexPath + "'...\n");
	    Directory directory = FSDirectory.open(path);
	    IndexWriterConfig config = new IndexWriterConfig(new SimpleAnalyzer());
		//config.setRAMBufferSizeMB(96); //this appears to have improved some abysmal performance
	    //	    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
	    //IndexWriterConfig config = new IndexWriterConfig(new EnglishAnalyzer());
	    IndexWriter iw = new IndexWriter(directory, config);
	    
		iw.deleteAll();

		Connection conn = null;
		Statement stmt = null;
		Statement state2 = null;
		StringBuilder stb = null;
		try {
			conn = DbManager.getConnection(true);
			stmt = conn.createStatement();
			String qitems = "SELECT Items.ItemId AS id, Name, Currently, Description, Category FROM Items NATURAL JOIN Bids NATURAL JOIN Categories ORDER BY Items.ItemId;"; //fetching all data in one access, hopefully this improves performance (despite redundant data). ordered by item ID just to be sure they're in order.
			ResultSet ritems = stmt.executeQuery(qitems);
			int tracker = 0;
			String trackedID = "-1";
			Document doc = new Document();
			while(ritems.next()){
				tracker++;
				if(tracker % 1000 == 0) System.out.print("."); //print a . every 1000 entries for some progress indication

				if(!ritems.getString("id").equals(trackedID)){ //a different ID from before, a new item
					if(doc.get("id") != null){ //to prevent the first, empty document from being written. not sure what effect that'd have tbh
						doc.add(new TextField("searchableText", stb.toString(), Field.Store.NO));
						insertDoc(iw, doc);
						//doc.clear(); //not available in this version?
						doc = new Document();
					}
					
					stb = new StringBuilder();
					trackedID = ritems.getString("id");
					doc.add(new TextField("id", trackedID, Field.Store.YES));
					String iname = ritems.getString("Name");
					doc.add(new TextField("name", iname, Field.Store.YES));
					stb.append(iname);
					stb.append(" ");
					stb.append(ritems.getString("Description"));
					stb.append(" ");
					doc.add(new TextField("price", ritems.getString("Currently"), Field.Store.YES));
					/*String iid = ritems.getString("id");
					String iname = ritems.getString("item_name");
					String idesc = ritems.getString("description");
					String iprice = ritems.getString("current_price");*/
					stb.append(ritems.getString("Category") + " ");
				}
				else{ //still the same ID/item as before
					stb.append(ritems.getString("Category") + " ");
				}

				//this was from the first solution attempt, fetching categories separately for each ID. untenable performance!
				/*String qcats = "SELECT category_name FROM has_category WHERE item_id = '" + iid + "';";
				state2 = conn.createStatement();
				ResultSet rcats = state2.executeQuery(qcats);
				//collecting all categories for the current item
				while(rcats.next()){
					stb.append(rcats.getString("category_name") + " ");
				}
				rcats.close();
				insertDoc(iw, iid, iname, idesc, stb.toString(), iprice);*/
			}
			//adding the last document b/c we're not reentering the loop to write it
			//System.out.println(stb.toString());
			System.out.println(""); //line break after those .
			doc.add(new TextField("searchableText", stb.toString(), Field.Store.NO));
			insertDoc(iw, doc);
			ritems.close();
			conn.close();
		} catch (SQLException ex) {
			System.out.println(ex);
		}

		/*i.deleteAll();
	    insertDoc(i, "1", "The old night keeper keeps the keep in the town");
	    insertDoc(i, "2", "In the big old house in the big old gown.");
	    insertDoc(i, "3", "The house in the town had the big old keep");
	    insertDoc(i, "4", "Where the old night keeper never did sleep.");
	    insertDoc(i, "5", "The night keeper keeps the keep in the night");
	    insertDoc(i, "6", "And keeps in the dark and sleeps in the light.");
	    insertDoc(i, "7", "The house is the house.");
	    insertDoc(i, "8", "The-the");
	    insertDoc(i, "9", "the-the.");
	    insertDoc(i, "10", "the");
	    insertDoc(i, "11", "the the");
	    insertDoc(i, "12", "the the the");
	    insertDoc(i, "13", "the the the the");
	    //	    insertDoc(i, "3", "the-the-the.");
	    //	    insertDoc(i, "4", "the-thethe__the.");
	    //	    insertDoc(i, "5", "The__the");
	    //	    insertDoc(i, "6", "The-the");
	    //	    insertDoc(i, "14", "The a b c");
	    //	    insertDoc(i, "15", "The a b.");
	    //	    insertDoc(i, "16", "The a.");
	    //	    insertDoc(i, "17", "The the the the.");
	    //	    insertDoc(i, "18", "The the the the the the the the the.");*/
	    iw.close();
	    directory.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
