package utils;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static List<MyFile> parseAll(String fileString) {
        System.out.println("Parsing documents...");
        String[] raw_docs = fileString.split("///");

        List<MyFile> parsed_docs = new ArrayList<>();
        for (String raw_doc : raw_docs) {
            String[] doc_contents = raw_doc.strip().split("\r|\n");

            String id = doc_contents[0];

            StringBuilder text = new StringBuilder();
            for (int j = 1; j < doc_contents.length; j++) {
                text.append(doc_contents[j]);
            }

            MyFile doc = new MyFile(id, text.toString());
            parsed_docs.add(doc);
        }
        System.out.println("Parsed: " + parsed_docs.size() + " files.\n");
        return parsed_docs;
    }

    public static List<MyFile> parseOne(String raw_query) {
        System.out.println("Parsing query...");

        String id = "Q01";
        String text = raw_query.strip().replace("_", " ");

        MyFile query = new MyFile(id, text);
        List<MyFile> parsed_query = new ArrayList<>();
        parsed_query.add(query);
        return parsed_query;
    }
}
