package phase2;// This is the second phase of the project
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import phase1.LuceneApp;

public class Main {

    public static void main(String[] args) {

        // BM25
        float[] bValues = {0.0f, 0.0f, 1.0f, 0.3f, 0.4f, 0.6f, 0.75f};
        float[] k1Values = {0.0f, 3.0f, 3.0f, 0.3f, 0.5f, 2.5f, 1.5f};
        for (int i = 0; i < bValues.length; i++) {
            Similarity simBM25 = new BM25Similarity(k1Values[i], bValues[i]);
            LuceneApp.setSimilarity(simBM25);
            LuceneApp.setOutputFile("res/phase2/results_BM25_k1_" + k1Values[i] + "_b_" + bValues[i] + ".txt");
            LuceneApp.main(args);
        }

        // LM Jelinek-Mercer
        float[] lambdaValues = {0.001f, 0.35f, 0.5f, 0.8f, 0.91f, 0.999f, 0.0f, 1.0f};
        for (int i = 0; i < lambdaValues.length; i++) {
            Similarity simLMJelinekMercer = new LMJelinekMercerSimilarity(lambdaValues[i]);
            LuceneApp.setSimilarity(simLMJelinekMercer);
            LuceneApp.setOutputFile("res/phase2/results_LMJelinekMercer_lambda_" + lambdaValues[i] + ".txt");
            LuceneApp.main(args);
        }

    }
}
