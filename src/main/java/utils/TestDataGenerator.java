package utils;

import org.apache.commons.lang3.RandomStringUtils;

public class TestDataGenerator {

    public static String getRandomEmail() {
        return "auto" + RandomStringUtils.randomAlphanumeric(5) + "@example.com";
    }

    public static String getRandomName() {
        return "User" + RandomStringUtils.randomAlphabetic(6);
    }

    public static String getRandomPhoneNumber() {
        return "98" + RandomStringUtils.randomNumeric(8);
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
