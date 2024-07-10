package phase1;

import common.Indexer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import utils.IO;
import utils.MyFile;
import utils.Parser;
import common.Queries;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class LuceneApp {
    // Create index flag
    private static boolean createIndex = true;
    // Index directory
    private static final String index = "index/myIndex";
    // Default documents file
    private static String rawDocumentsFile = "IR2024/documents.txt";
    // Default queries file
    private static String rawQueriesFile = "IR2024/queries.txt";
    // Single query option entered by user (queriesFile is ignored)
    private static String query = "";
    // Number of documents to return
    private static int k = 50;

    private static String outputFile = "res/phase1/results.txt";

    // Additions
    private static Similarity similarity = new ClassicSimilarity();

    public static void setSimilarity(Similarity similarity) {
        LuceneApp.similarity = similarity;
    }

    public static void setOutputFile(String outputFile) {
        LuceneApp.outputFile = outputFile;
    }

    public static void main(String[] args)  {
        // Parse arguments
        run(args);
        System.out.println("Similarity: " + similarity.toString() + "\n");

        // Index creation
        if (createIndex) {
            Indexer indexer = new Indexer(new EnglishAnalyzer(), similarity);
            indexer.createIndex(index, rawDocumentsFile, createIndex);
        }
        else {
            Indexer.checkIndexExistence();
        }

        // Search
        processQueries();
//        System.exit(0);
    }

    private static void processQueries() {
        try {
            Searcher searcher = new Searcher(similarity);

            List<MyFile> proc_queries;
            if (query.isEmpty()) {
                proc_queries = Queries.fetchQueries(rawQueriesFile);
            }
            else {
                proc_queries = Parser.parseOne(query);
            }

            StringBuilder res = new StringBuilder();
            for (MyFile query: proc_queries) {

                System.out.println("Processing query: " + query.id());

                TopDocs result = searcher.search(query.text(), k);
                SortedMap<Float, String> scores = searcher.fetchScores(result);

                for (Map.Entry<Float, String> entry : scores.entrySet()) {
                    res.append(query.id()).append("\t0\t").append(entry.getValue()).append("\t0\t").append(entry.getKey()).append("\tLuceneApp\n");
                }
            }
            searcher.close();

            // Write results to file
            IO.writeResults(res.toString(), outputFile);
            System.out.println("\nResults written to '" + outputFile + "'.");
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void run(String[] args) {
        //Check for arguments
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-cindex":
                        createIndex = true;
                        break;
                    case "-dfile":
                        if (createIndex) {
                            if (i < args.length - 1) {
                                rawDocumentsFile = args[++i];
                            } else {
                                System.out.println("Missing value for -dfile option! Enter filename after -dfile.");
                                throw new IllegalArgumentException();
                            }
                        }
                        else {
                            System.out.println("The -dfile option can only be used with -cindex option!");
                            throw new IllegalArgumentException();
                        }
                        break;
                    case "-qfile":
                        if (i < args.length - 1) {
                            rawQueriesFile = args[++i];
                        } else {
                            System.out.println("Missing value for -qfile option! Enter filename after -qfile.");
                            throw new IllegalArgumentException();
                        }
                        break;
                    case "-q":
                        if (i < args.length - 1) {
                            query = args[++i];
                        } else {
                            System.out.println("Missing value for -q option! Enter the query after using '_' to separate words after -q.");
                            throw new IllegalArgumentException();
                        }
                        break;
                    case "-k":
                        if (i < args.length - 1) {
                            k = Integer.parseInt(args[++i]);
                        } else {
                            System.out.println("Missing value for -k option! Enter the number of docs you want to be returned after -k.");
                            throw new IllegalArgumentException();
                        }
                        break;
                }
            }

            if (!query.isEmpty() && !rawQueriesFile.equals("IR2024/queries.txt")) {
                System.out.println("The -q option can only be used without the -qfile option!");
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException e) {
            System.exit(1);
        }
    }
}
