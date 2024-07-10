package phase1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.nio.file.Paths;
import java.util.*;

public class Searcher {
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    private final String field = "text";
    private QueryParser parser;

    // Addition
    public Searcher(Similarity similarity){
        try {
            String indexDir = "index/myIndex";

            Directory dir = FSDirectory.open(Paths.get(indexDir));
            indexReader = DirectoryReader.open(dir);
            indexSearcher = new IndexSearcher(indexReader);

            // Addition
            indexSearcher.setSimilarity(similarity);

            Analyzer analyzer = new EnglishAnalyzer();
            parser = new QueryParser(field, analyzer);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void close() {
        try {
            indexReader.close();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public TopDocs search(String query, int k) {

        try{
            Query q = parser.parse(query);
            System.out.println("> Searching for: " + q.toString(field));

            TopDocs results = indexSearcher.search(q, k);
            long numTotalHits = results.totalHits;
            System.out.println(" - " + numTotalHits + " total matching documents");

            return results;
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    public SortedMap<Float, String> fetchScores(TopDocs results) {
        SortedMap<Float, String> scores = new TreeMap<>(Comparator.reverseOrder());
        ScoreDoc[] hits = results.scoreDocs;
        try{
            for (ScoreDoc hit: hits) {
                String docID = indexSearcher.doc(hit.doc).get("id");
                float score = hit.score;
                scores.put(score, docID);
            }
            System.out.println(" - Best doc: " + scores.get(scores.firstKey()) + " | score: " + scores.firstKey() + "\n");
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }
        return scores;
    }
}
