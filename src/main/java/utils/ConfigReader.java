package utils;

import java.io.FileInputStream;

import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties prop;

    static {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            prop = new Properties();
            prop.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("config.properties file not found");
        }
    }

    public static String get(String key) {
        return prop.getProperty(key);
    }
}
