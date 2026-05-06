package com.p2.qa.sprint1.testcases;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;
import com.p2.qa.sprint1.pageobjects.BikeObjects;
import com.p2.qa.sprint1.pageobjects.BikePartsObjects;
import com.p2.qa.sprint1.pageobjects.VendorPageObject;

import utils.ConfigReader;
import utils.TestDataGenerator;

@Listeners(utils.AllureTestListener.class)
public class BulkBikeCreation_Testcases extends Base {
    private WebDriver driver;
    private VendorPageObject vendorPage;
    private BikePartsObjects bikePartsPage;
    private BikeObjects bikePage;
    private int bikeCount;
    private int startIndex;
    private int retryCount;
    private boolean createPartsForEachBike;
    private String vendorName;

    @BeforeClass
    public void setUp() {
        if (!isBulkCreationConfirmed()) {
            throw new SkipException(
                "Bulk bike creation is guarded. Run with -Dbulk.bike.confirm=true to create bikes in QA."
            );
        }

        bikeCount = getIntProperty("bulk.bike.count", 1000);
        if (bikeCount < 1) {
            throw new IllegalArgumentException("bulk.bike.count must be at least 1");
        }
        startIndex = getIntProperty("bulk.bike.start.index", 1);
        if (startIndex < 1) {
            throw new IllegalArgumentException("bulk.bike.start.index must be at least 1");
        }
        retryCount = getIntProperty("bulk.bike.retry.count", 2);
        if (retryCount < 1) {
            throw new IllegalArgumentException("bulk.bike.retry.count must be at least 1");
        }
        createPartsForEachBike = Boolean.parseBoolean(getProperty("bulk.bike.create.parts", "false"));

        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login loginPage = new Login();
        driver = loginPage.loginAs(driver, "admin");

        vendorPage = new VendorPageObject(driver);
        bikePartsPage = new BikePartsObjects(driver);
        bikePage = new BikeObjects(driver);
    }

    @Test(description = "Create a configurable number of bikes with fresh required parts for each bike")
    public void createBulkBikes() throws InterruptedException {
        if (createPartsForEachBike) {
            createBulkVendor();
        }

        int endIndex = startIndex + bikeCount - 1;
        for (int i = startIndex; i <= endIndex; i++) {
            createBikeWithRetry(i);

            int completed = i - startIndex + 1;
            if (completed % 25 == 0 || completed == bikeCount) {
                System.out.println("Bulk bike progress: " + completed + "/" + bikeCount
                    + " bikes submitted. Last index: " + i);
            }
        }

        Assert.assertTrue(true, "Submitted " + bikeCount + " bike creation requests");
    }

    private void createBulkVendor() throws InterruptedException {
        vendorName = "Bulk Vendor " + TestDataGenerator.getRandomName();
        String country = getProperty("bulk.bike.vendor.country", "nepal");
        String remarks = getProperty("bulk.bike.vendor.remarks", "Bulk bike automation vendor");

        vendorPage.navigateToVendorPage();
        vendorPage.clickAddNewVendor();
        vendorPage.createVendor(vendorName, country, remarks);
        Thread.sleep(2000);

        Assert.assertEquals(vendorPage.getFirstVendorName(), vendorName, "Bulk vendor was not created");
    }

    private void createBikeWithRetry(int index) throws InterruptedException {
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                System.out.println("Creating bulk bike index " + index + " attempt " + attempt + "/" + retryCount);
                if (createPartsForEachBike) {
                    createRequiredPartsForBike(index);
                }
                createBike(index);
                return;
            } catch (RuntimeException e) {
                lastFailure = e;
                System.out.println("Bulk bike index " + index + " failed on attempt " + attempt + ": " + e.getMessage());
                recoverUi();
            }
        }

        throw new AssertionError("Could not create bulk bike index " + index
            + ". Resume with -Dbulk.bike.start.index=" + index, lastFailure);
    }

    private void createRequiredPartsForBike(int index) {
        int latestVendorIndex = 1;

        createPart("battery", index, latestVendorIndex);
        createPart("display", index, latestVendorIndex);
        createPart("charger", index, latestVendorIndex);
        createPart("motorcontroller", index, latestVendorIndex);
        createPart("vcu", index, latestVendorIndex);
        createPart("motors", index, latestVendorIndex);
        createPart("commboard", index, latestVendorIndex);
    }

    private void createPart(String partType, int index, int vendorIndex) {
        bikePartsPage.navigateToBikeParts();
        bikePartsPage.selectPartType(partType);
        bikePartsPage.clickAddPart();

        if ("battery".equalsIgnoreCase(partType)) {
            bikePartsPage.createBatteryPart(generateHexIdentifier(partType, index), vendorIndex);
        } else if ("display".equalsIgnoreCase(partType)) {
            bikePartsPage.createDisplay(generateHexIdentifier(partType, index), vendorIndex, "1.0.0", "1.0.0", "1.0.0");
        } else if ("vcu".equalsIgnoreCase(partType)) {
            bikePartsPage.createVCU(generateHexIdentifier(partType, index), vendorIndex, "1.0.0");
        } else {
            String identifier = generateHexIdentifier(partType, index);
            bikePartsPage.createBasicPart(identifier, vendorIndex);
        }
    }

    private String generateHexIdentifier(String partType, int index) {
        String hexTime = Long.toHexString(System.currentTimeMillis()).toUpperCase();
        String hexIndex = Integer.toHexString(index).toUpperCase();
        String hexPart = Integer.toHexString(Math.abs(partType.hashCode())).toUpperCase();
        String seed = hexPart + hexTime + hexIndex;
        return seed.substring(seed.length() - 8);
    }

    private void createBike(int index) throws InterruptedException {
        String bikeName = "Auto Bike " + TestDataGenerator.getRandomName();
        String vin = generateHexIdentifier("vin", index);

        bikePage.createBikeWithFirstAvailableParts(bikeName, vin);
    }

    private void recoverUi() {
        try {
            driver.navigate().refresh();
            Thread.sleep(3000);
        } catch (Exception e) {
            System.out.println("Bulk recovery refresh failed: " + e.getMessage());
        }
    }

    private boolean isBulkCreationConfirmed() {
        return Boolean.parseBoolean(getProperty("bulk.bike.confirm", "false"));
    }

    private int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Integer.parseInt(value);
    }

    private String getProperty(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue.trim();
        }

        String configValue = ConfigReader.get(key);
        if (configValue != null && !configValue.trim().isEmpty()) {
            return configValue.trim();
        }

        return defaultValue;
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
