package phase3;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class Searcher {
    private final IndexReader indexReader;
    private final Word2Vec vec;
    private final String fieldName;

    public Searcher(IndexReader indexReader, Word2Vec vec, String fieldName) {
        this.indexReader = indexReader;
        this.vec = vec;
        this.fieldName = fieldName;
    }

    public void processQuery(String queryString, Map<String, Float> scoresDenseAvg, Map<String, Double> simScores) {
        try {
            String[] split = queryString.split(" ");

            // Option 1 : no smoothing (single call no for loop)
            INDArray denseAverageQueryVector = vec.getWordVectorsMean(Arrays.asList(split));

            // Find in index
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            Similarity similarity = new WordEmbeddingsSimilarity(vec, fieldName, WordEmbeddingsSimilarity.Smoothing.MEAN);
            indexSearcher.setSimilarity(similarity);

            QueryParser parser = new QueryParser(fieldName, new WhitespaceAnalyzer());
            Query query = parser.parse(queryString);

            TopDocs hits = indexSearcher.search(query, 50);
            ScoreDoc[] scores = hits.scoreDocs;

            for (ScoreDoc scoreDoc : scores) {
                if (scoreDoc.doc >= indexReader.maxDoc()){
                    continue;
                }
                Document doc = indexSearcher.doc(scoreDoc.doc);

                String id = doc.get("id");

                Terms docTerms = indexReader.getTermVector(scoreDoc.doc, fieldName);

                // Option 1
                INDArray denseAverageDocumentVector = VectorizeUtils.toDenseAverageVector(docTerms, vec);
                double cosSimDenseAvg = Transforms.cosineSim(denseAverageQueryVector, denseAverageDocumentVector);
//                System.out.println("cosineSimilarityDenseAvg = " + cosSimDenseAvg);

                // Keep score for trec_eval
                scoresDenseAvg.put(id, scoreDoc.score);
                simScores.put(id, cosSimDenseAvg);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("\nTerminating...");
            System.exit(1);
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }
}
