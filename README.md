# Information Retrieval Project

This project implements an information retrieval system using Apache Lucene and Word2Vec for document indexing and querying. The project is divided into multiple phases, each enhancing the search capabilities and indexing strategies. The project also includes a command-line application for indexing and querying documents. 

### Project Structure

- **phase1**: Basic indexing and searching with Classic (TF-IDF) similarity and English analyzer.
- **phase2**: Enhancements with additional similarity measures and preprocessing (BM25 and LM Jelinek-Mercer).
- **phase3**: Implements a search mechanism using word embeddings and different smoothing techniques. Word2Vec is used to generate word embeddings. Using WhiteSpace analyzer.
- **phase4**: Combines various similarity scoring mechanisms including classic Classic, BM25, LM Jelinek-Mercer, and Word Embeddings.

#### Dependencies

- Apache Lucene 7.7.3
- DeepLearning4J
- Nd4j
- Java (JDK 8 or higher)
  

#### Configuration

- **rawDocumentsFile**: Name of the file containing the raw documents in Resources directory.
- **rawQueriesFile**: Name of the file containing the raw queries in Resources directory.
- **index**: Directory where the index will be stored.
- **model**: Directory containing the word embedding models.

To use wikimodel you need to download the model from [here](http://vectors.nlpl.eu/repository/20/7.zip) and extract it to the `models/wikimodel` directory!

#### Output

Results will be written to files in the `res/phase1`, `res/phase2`, `res/phase3`, and `res/phase4` directories. Use trec_eval to evaluate the results.

To use trec_eval you need to add the 3 files from `res` directory (`trec_eval.exe`, `cygwin1.dll`, `run_trec_eval.bat`) in each phase directory. Modify the `run_trec_eval.bat` file to search for the correctly named results file according to the name given by each phase's output variable in main class.

### LuceneApp - Executable Description

LuceneApp is a Java-based command-line application designed for Information Retrieval tasks using Apache Lucene. It allows indexing of documents and querying using various similarity models and analyzers.

#### Executable Options

LuceneApp supports the following command-line options:

- **-cindex**: Indicates the creation of a new index. If no index is found, the application terminates with code 1.

- **-dfile <filename>**: Specifies the file containing raw documents for indexing. Must be used with `-cindex`.

- **-qfile <filename>**: Specifies the file containing raw queries.

- **-q <query>**: Directly specifies a query without using a file. Use '_' to separate words in the query.

- **-k <number>**: Specifies the number of documents to be returned for each query. Default is 50.


#### Notes

- The `-dfile` option must be used with `-cindex` to specify the documents for indexing.

- The `-q` option and `-qfile` option are mutually exclusive. Use either to specify queries.

- If no `-dfile`, `-q`, or `-qfile` options are provided, LuceneApp defaults to searching for 'IR2024/documents.txt' and 'IR2024/queries.txt' files in the project directory.



