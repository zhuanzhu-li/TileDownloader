package test.test;


import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class OwsUtils {
    public static void saveTile(String finalSourceUrl, String finalBashPath, String finalFileName) {
        try (InputStream in = (new URL(finalSourceUrl)).openStream()) {
            Path dir = Paths.get(finalBashPath, new String[0]);
            Files.createDirectories(dir, (FileAttribute<?>[])new FileAttribute[0]);
            Files.copy(in, Paths.get(finalBashPath + "/" + finalFileName, new String[0]), new java.nio.file.CopyOption[0]);
        } catch (Exception exception) {}
    }
}