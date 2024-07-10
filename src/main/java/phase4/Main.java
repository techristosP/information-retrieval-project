package phase4;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.jetbrains.annotations.Nullable;
import common.Indexer;
import phase3.WordEmbeddingsSimilarity;
import phase3.WordVectors;
import utils.IO;
import utils.MyFile;
import common.Queries;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private static final String rawDocumentsFile = "IR2024/documents.txt";
    private static final String rawQueriesFile = "IR2024/queries.txt";

    private static final String index = "index";
    private static final boolean createNewIndex = true; // If false, but index does not exist, it will be created

    private static final String fieldName = "text";
    private static final String model = "models";

    private static final String outputFile = "res/phase4/";


    public static void main(String[] args) {

        try {
            // Index for Word Embeddings
            String indexForWordEmbeddings = index + "/wordEmbeddingsIndex";
            Indexer indexerForWordEmbeddings = new Indexer(new WhitespaceAnalyzer());
            indexerForWordEmbeddings.setFieldTypeForWordEmbeddings(true);
            indexerForWordEmbeddings.createIndex(indexForWordEmbeddings, rawDocumentsFile, createNewIndex);

            // Index Reader for Word Embeddings
            Directory indexDirForWordEmbeddings = FSDirectory.open(Paths.get(indexForWordEmbeddings));
            IndexReader indexReaderForWordEmbeddings = DirectoryReader.open(indexDirForWordEmbeddings);

            // Train Word2Vec from index
            WordVectors wordVectors = new WordVectors(model, indexReaderForWordEmbeddings, fieldName);
            Word2Vec vec = wordVectors.getWord2VecModel(false);


            Similarity[] similarities = new Similarity[] {
                new ClassicSimilarity(),
                new BM25Similarity(2.5f, 0.6f),
                new LMJelinekMercerSimilarity(0.9f),
                new WordEmbeddingsSimilarity(vec, fieldName, WordEmbeddingsSimilarity.Smoothing.MEAN)
            };

            MultiSimilarity CLASSIC_BM25 = new MultiSimilarity(new Similarity[]{similarities[0], similarities[1]});
            MultiSimilarity CLASSIC_LMJM = new MultiSimilarity(new Similarity[]{similarities[0], similarities[2]});
            MultiSimilarity BM25_LMJM = new MultiSimilarity(new Similarity[]{similarities[1], similarities[2]});
            MultiSimilarity BM25_WV = new MultiSimilarity(new Similarity[]{similarities[1], similarities[3]});
            MultiSimilarity LMJM_WV = new MultiSimilarity(new Similarity[]{similarities[2], similarities[3]});
            MultiSimilarity CLASSIC_WV = new MultiSimilarity(new Similarity[]{similarities[0], similarities[3]});


            List<MyFile> queries = Queries.fetchQueries(rawQueriesFile);

            Analyzer englishAnalyzer = new EnglishAnalyzer();
            Analyzer whiteSpaceAnalyzer = new WhitespaceAnalyzer();

            executeCase(CLASSIC_BM25, englishAnalyzer,queries, "CLASSIC_BM25");
            executeCase(CLASSIC_LMJM, englishAnalyzer, queries, "CLASSIC_LMJM");
            executeCase(BM25_LMJM, englishAnalyzer, queries, "BM25_LMJM");
            executeCase(BM25_WV, whiteSpaceAnalyzer, queries, "BM25_WV");
            executeCase(LMJM_WV, whiteSpaceAnalyzer, queries, "LMJM_WV");
            executeCase(CLASSIC_WV, whiteSpaceAnalyzer, queries, "CLASSIC_WV");

            indexReaderForWordEmbeddings.close();
            indexDirForWordEmbeddings.close();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Nullable
    private static ScoreDoc[] processQuery(String queryText, IndexSearcher indexSearcher, Analyzer analyzer) {
        try {
            QueryParser parser = new QueryParser(fieldName, analyzer);
            Query query = parser.parse(queryText);

            TopDocs hits = indexSearcher.search(query, 50);

            return hits.scoreDocs;
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    private static void executeCase(MultiSimilarity multiSim, Analyzer analyzer, List<MyFile> queries, String caseName) {
        try {
            System.out.println("MultiSimilarity: " + caseName + "\n");

            if (analyzer instanceof EnglishAnalyzer) {
                // Create Index
                Indexer indexer = new Indexer(analyzer, multiSim);
                indexer.createIndex(index+"/myIndex", rawDocumentsFile, createNewIndex);
            }
            // Index Reader
            Directory indexDir;
            if (analyzer instanceof EnglishAnalyzer) {
                indexDir = FSDirectory.open(Paths.get(index+"/myIndex"));
            }
            else {
                indexDir = FSDirectory.open(Paths.get(index + "/wordEmbeddingsIndex"));
            }
            IndexReader indexReader = DirectoryReader.open(indexDir);

            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(multiSim);

            StringBuilder res = new StringBuilder();
            for (MyFile query : queries) {
                System.out.println(query.id() + ": " + query.text());
                ScoreDoc[] scores = processQuery(query.text(), indexSearcher, analyzer);

                if (scores == null || scores.length == 0) {
                    continue;
                }
                for (ScoreDoc scoreDoc : scores) {
                    if (scoreDoc.doc >= indexReader.maxDoc()){
                        continue;
                    }
                    String docId = indexSearcher.doc(scoreDoc.doc).get("id");
                    Float scoreValue = scoreDoc.score;
                    res.append(query.id()).append("\t0\t").append(docId).append("\t0\t").append(scoreValue).append("\tP4-").append(caseName).append("\n");
                }
                String bestDocId = indexSearcher.doc(scores[0].doc).get("id");
                System.out.println(" - Best doc: " + bestDocId + " with score: " + scores[0].score);
            }
            // Write results to file
            String fileName = "res_" + caseName;
            IO.writeResults(res.toString(), outputFile + fileName + ".txt");

            System.out.println("--------------------------------------------\n");

            indexReader.close();
            indexDir.close();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
