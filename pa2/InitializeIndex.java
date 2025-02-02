import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

public class InitializeIndex {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java InitializeIndex <indexDir>");
            System.exit(1);
        }

        String indexPath = args[0];
        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new SimpleAnalyzer());
            IndexWriter writer = new IndexWriter(directory, config);

            // Commit and close the writer to properly create index files
            writer.commit();
            writer.close();
            
            System.out.println("Lucene index initialized at: " + indexPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
