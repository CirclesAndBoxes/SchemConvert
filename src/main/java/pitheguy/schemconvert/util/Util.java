package pitheguy.schemconvert.util;

public class Util {
    public static String stripExtension(String fileName) {
        if (!fileName.contains(".")) return fileName;
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static String getExtension(String fileName) {
        if (!fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
