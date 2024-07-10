package utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class IO {

    public static String readFile(String file) {
        try {
            Scanner scanner = new Scanner(new FileInputStream(file));
            scanner.useDelimiter("\\A"); //\\A stands for :start of a string
            return scanner.next();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Terminating program...");
            System.exit(1);
        }
        return null;
    }

    public static void writeFile(String text, String file) {
        try {
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(text);
            myWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void writeResults(String results, String file) {
        Path res = Paths.get("res");
        if (!Files.exists(res)) {
            try {
                Files.createDirectory(res);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        writeFile(results, file);
    }
}
