package com.p2.qa.sprint1.testcases;

import org.openqa.selenium.WebDriver;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;
import com.p2.qa.sprint1.pageobjects.BikeObjects;

import utils.TestDataGenerator;

public class BikeOnlySmoke_Testcases extends Base {
    private WebDriver driver;
    private BikeObjects bikePage;

    @BeforeClass
    public void setUp() {
        if (!Boolean.parseBoolean(System.getProperty("bike.only.confirm", "false"))) {
            throw new SkipException("Bike-only smoke is guarded. Run with -Dbike.only.confirm=true.");
        }

        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login loginPage = new Login();
        driver = loginPage.loginAs(driver, "admin");
        bikePage = new BikeObjects(driver);
    }

    @Test(description = "Create one bike using first available parts")
    public void createBikeWithAvailableParts() throws InterruptedException {
        String bikeName = "Auto Bike " + TestDataGenerator.getRandomName();
        String vin = Long.toHexString(System.currentTimeMillis()).toUpperCase();
        vin = vin.substring(vin.length() - 8);

        bikePage.createBikeWithFirstAvailableParts(bikeName, vin);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
