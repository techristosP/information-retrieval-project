package common;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.nd4j.common.io.ClassPathResource;
import utils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/*
    * Indexer class to create an index from a raw documents file.
    * The index can be created using a custom analyzer and/or similarity.
    * Default analyzer is StandardAnalyzer and similarity is BM25Similarity.
 */
public class Indexer {
    private Analyzer analyzer;
    private Similarity similarity;
    private boolean useFieldTypeForWordEmbeddings = false;

    public Indexer() {
    }

    public Indexer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }
    public Indexer(Similarity similarity) {
        this.similarity = similarity;
    }
    public Indexer(Analyzer analyzer, Similarity similarity) {
        this.analyzer = analyzer;
        this.similarity = similarity;
    }


    public void createIndex(String index, String rawDocumentsFile, boolean createNewIndex) {
        try {

            Directory indexDir = FSDirectory.open(Paths.get(index));
            System.out.println("Indexing to directory '" + index + "'...");
            if (indexDir.listAll().length > 0) {
                if (!createNewIndex) {
                    System.out.println("Index already exists. Terminating...");
                    return;
                }

                System.out.println("Index already exists. Deleting old files...");
                for (String file : indexDir.listAll()) {
                    Files.delete(Paths.get(index + "/" + file));
                }
            }

            // Analyzer: Default is StandardAnalyzer if not provided
            Analyzer analyzer = Objects.requireNonNullElseGet(this.analyzer, StandardAnalyzer::new);

            // IndexWriterConfig
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            // Similarity: Default is BM25Similarity if not provided
            if (similarity != null) {
                iwc.setSimilarity(similarity);
            }

            // New index creation
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            // Create index writer with the configuration
            IndexWriter indexWriter = new IndexWriter(indexDir, iwc);

            // FieldType settings for word embeddings
            FieldType ft = null;
            if (useFieldTypeForWordEmbeddings) {
                ft = getFieldTypeForWordEmbeddings();
            }

            String documentsFile = new ClassPathResource(
                    rawDocumentsFile).getFile()
                    .getAbsolutePath();

            String rawDocuments = IO.readFile(documentsFile);
            List<MyFile> docs = Parser.parseAll(rawDocuments);
            for (MyFile doc: docs) {
                indexDoc(indexWriter, doc, ft);
            }
            indexWriter.commit();

            System.out.println("Indexed: " + docs.size() + " documents.\n");

            indexWriter.close();
            indexDir.close();
        }

        catch(IOException e){
            System.err.println(e.getMessage());
            System.err.println("\nError creating index. Terminating...");
            System.exit(1);
        }
    }

    public static void checkIndexExistence() {
        try {
            String indexDir = "index";
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            if (dir.listAll().length == 0) {
                System.err.println("Index does not exist! Please create the index first.");
                System.exit(1);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void indexDoc(IndexWriter indexWriter, MyFile doc, FieldType ft) {
        Document luceneDoc = new Document();

        // Add fields to the document
        StoredField id = new StoredField("id", doc.id());
        luceneDoc.add(id);

        Field text;
        if (useFieldTypeForWordEmbeddings) {
            text = new Field("text", doc.text(), ft);
        }
        else {
            text = new TextField("text", doc.text(), Field.Store.NO);
        }
        luceneDoc.add(text);

        try {
            if (indexWriter.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
//                System.out.println("adding " + doc);
                indexWriter.addDocument(luceneDoc);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void setFieldTypeForWordEmbeddings(boolean useFieldTypeForWordEmbeddings) {
        this.useFieldTypeForWordEmbeddings = useFieldTypeForWordEmbeddings;
    }

    private FieldType getFieldTypeForWordEmbeddings() {
        FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        ft.setTokenized(true);
        ft.setStored(true);
        ft.setStoreTermVectors(true);
        ft.setStoreTermVectorOffsets(true);
        ft.setStoreTermVectorPositions(true);
        return ft;
    }

}
