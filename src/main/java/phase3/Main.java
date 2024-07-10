package phase3;

import common.Indexer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.word2vec.Word2Vec;
import utils.IO;
import utils.MyFile;

import common.Queries;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static final String rawDocumentsFile = "IR2024/documents.txt";
    private static final String rawQueriesFile = "IR2024/queries.txt";

    private static final String index = "index/wordEmbeddingsIndex";
    private static final boolean createNewIndex = true; // If false, but index does not exist, it will be created

    private static final String fieldName = "text";
    private static final String model = "models";
    private static final boolean useWikiModel = false;

    public static Map<String, Float> scoresDenseAvg;
    public static Map<String, Double> simScores;

    private static final String outputFile = "res/phase3/results.txt";


    public static void main(String[] args) throws IOException {
        // Create Index
        Indexer indexer = new Indexer(new WhitespaceAnalyzer());
        indexer.setFieldTypeForWordEmbeddings(true);
        indexer.createIndex(index, rawDocumentsFile, createNewIndex);

        // Index Reader
        Directory indexDir = FSDirectory.open(Paths.get(index));
        IndexReader indexReader = DirectoryReader.open(indexDir);

        // Train Word2Vec from index
        WordVectors wordVectors = new WordVectors(model, indexReader, fieldName);
        Word2Vec vec = wordVectors.getWord2VecModel(useWikiModel);

        // Searcher
        Searcher searcher = new Searcher(indexReader, vec, fieldName);

        // Parse Queries
        List<MyFile> queries = Queries.fetchQueries(rawQueriesFile);

        StringBuilder res = new StringBuilder();

        for (MyFile query : queries) {

            System.out.println(query.id() + ": " + query.text());
            scoresDenseAvg = new HashMap<>();
            simScores = new HashMap<>();

            searcher.processQuery(query.text(), scoresDenseAvg, simScores);

            // Sort results
            List<Map.Entry<String, Float>> entryList = new ArrayList<>(scoresDenseAvg.entrySet());
            entryList.sort(Map.Entry.comparingByValue(Collections.reverseOrder()));
            Map<String, Float> sortedScoresDenseAvg = new LinkedHashMap<>();
            for (Map.Entry<String, Float> entry : entryList) {
                sortedScoresDenseAvg.put(entry.getKey(), entry.getValue());
            }
            String bestDocId = sortedScoresDenseAvg.entrySet().iterator().next().getKey();
            System.out.println(" - Best doc: " + bestDocId + " with score: " + sortedScoresDenseAvg.get(bestDocId));
            System.out.println(" - Best doc's cosine similarity: " + simScores.get(bestDocId));

            // Write results
            for (Map.Entry<String, Float> entry : sortedScoresDenseAvg.entrySet()) {
                res.append(query.id()).append("\t0\t").append(entry.getKey()).append("\t0\t").append(entry.getValue()).append("\tPhase3\n");
            }
        }

        // Write results to file
        IO.writeResults(res.toString(), outputFile);

        System.out.println("\nResults written to '" + outputFile + "'.");

        indexReader.close();
        indexDir.close();
    }





}
