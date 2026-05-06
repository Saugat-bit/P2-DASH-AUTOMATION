package utils;

import org.apache.commons.lang3.RandomStringUtils;
import java.util.HashMap;
import java.util.Map;

public class TestDataGenerator {
    private static final Map<String, Integer> emailCounters = new HashMap<>();
    private static int phoneCounter = 1;

    public static String getRandomEmail() {
        return getSimpleYopmailEmail("staff");
    }

    public static synchronized String getSimpleYopmailEmail(String prefix) {
        String normalizedPrefix = prefix.toLowerCase().replaceAll("[^a-z0-9]", "");
        int nextValue = emailCounters.getOrDefault(normalizedPrefix, 0) + 1;
        emailCounters.put(normalizedPrefix, nextValue);
        return normalizedPrefix + nextValue + "@yopmail.com";
    }

    public static String getRandomName() {
        return "User" + RandomStringUtils.randomAlphabetic(6);
    }

    public static String getRandomPhoneNumber() {
        return getValidNepaliPhoneNumber();
    }

    public static synchronized String getValidNepaliPhoneNumber() {
        String suffix = String.format("%06d", phoneCounter++ % 1000000);
        return "9745" + suffix;
    }

    public static String getRandomAlphanumeric(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
    public static String getRandomIdentifier() {
        return "BAT_" + System.currentTimeMillis();
    }

    public static String getRandomDayBefore(int referenceDay) {
        return String.valueOf(referenceDay - 1);
    }

    public static String getRandomDayAfter(int referenceDay) {
        return String.valueOf(referenceDay );
    }
}
