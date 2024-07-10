package common;

import org.nd4j.common.io.ClassPathResource;
import utils.IO;
import utils.MyFile;
import utils.Parser;

import java.util.List;

public class Queries {

    public static List<MyFile> fetchQueries(String queriesFile) {
        List<MyFile> queries = null;
        try {
            String queryPath = new ClassPathResource(
                    queriesFile).getFile()
                    .getAbsolutePath();

            queries = Parser.parseAll(IO.readFile(queryPath));
            if (queries.isEmpty()) {
                throw new Exception("No queries found!");
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("\nTerminating...");
            System.exit(1);
        }
        return queries;
    }
}
