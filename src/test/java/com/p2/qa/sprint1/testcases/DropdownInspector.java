package com.p2.qa.sprint1.testcases;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;
import com.p2.qa.sprint1.pageobjects.BikePartsObjects;

public class DropdownInspector extends Base {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();
        driver = new Login().loginAs(driver, "admin");
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void inspectVendorDropdown() throws Exception {
        BikePartsObjects parts = new BikePartsObjects(driver);
        parts.navigateToBikeParts();
        parts.selectPartType("display");
        parts.clickAddPart();

        WebElement vendor = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//label[contains(., 'Vendor')]/following-sibling::button")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", vendor);
        Thread.sleep(1000);

        File dir = new File("dropdown-inspection");
        dir.mkdirs();
        FileWriter writer = new FileWriter(new File(dir, "display-vendor-open.html"));
        writer.write(driver.getPageSource());
        writer.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
