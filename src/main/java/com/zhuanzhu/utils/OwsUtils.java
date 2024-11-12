package com.zhuanzhu.utils;


import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Liwq
 */
public class OwsUtils {
    public static void saveTile(String finalSourceUrl, String finalBashPath, String finalFileName) {
        try (InputStream in = (new URL(finalSourceUrl)).openStream()) {
            Path dir = Paths.get(finalBashPath);
            Files.createDirectories(dir);
            Files.copy(in, Paths.get(finalBashPath + "/" + finalFileName));
        } catch (Exception exception) {}
    }
}