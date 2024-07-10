package phase3;

import org.apache.lucene.index.IndexReader;
import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

public class WordVectors {
    private final String myModel;
    private final String wikiModel;
    private final IndexReader indexReader;
    private final String field;

    public WordVectors(String model, IndexReader indexReader, String field) {
        this.myModel = model + "/mymodel/myModel.bin";
        this.wikiModel = model + "/wikimodel/model.txt";
        this.indexReader = indexReader;
        this.field = field;
    }


    private Word2Vec trainWord2Vec(String modelPath){
        SentenceIterator iter = new FieldValuesSentenceIterator(indexReader, field);

        // Train Word2Vec
        Word2Vec vec = new Word2Vec.Builder()
                .layerSize(200)
                .windowSize(7)
                .iterate(iter)
                .tokenizerFactory(new DefaultTokenizerFactory())
//                .elementsLearningAlgorithm(new CBOW<>())
                .elementsLearningAlgorithm(new SkipGram<>())
                .epochs(5)
                .seed(12345)
                .build();

        vec.fit();

        WordVectorSerializer.writeWord2VecModel(vec, modelPath);
        System.out.println("Model saved to " + modelPath);

        return vec;
    }

    public Word2Vec getWord2VecModel(boolean useWikiModel) {
        if (useWikiModel) {
            String wikiModelPath = Paths.get(wikiModel).toAbsolutePath().toString();
            return WordVectorSerializer.readWord2VecModel(wikiModelPath);
        }
        String modelPath = Paths.get(myModel).toAbsolutePath().toString();

        if (!Files.exists(Paths.get(modelPath))) {
            return trainWord2Vec(modelPath);
        }

        System.out.println("Loading model from " + modelPath);
        return WordVectorSerializer.readWord2VecModel(modelPath);
    }
}
