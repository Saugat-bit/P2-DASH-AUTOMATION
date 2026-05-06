package com.p2.qa.sprint1.testcases;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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

public class PartFormInspector extends Base {
    private WebDriver driver;
    private BikePartsObjects bikePartsPage;
    private WebDriverWait wait;
    private final File outputDir = new File("part-form-inspection");

    @BeforeClass
    public void setUp() {
        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login loginPage = new Login();
        driver = loginPage.loginAs(driver, "admin");

        bikePartsPage = new BikePartsObjects(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        outputDir.mkdirs();
    }

    @Test
    public void inspectPartForms() throws Exception {
        String[] partTypes = {"battery", "display", "charger", "motorcontroller", "vcu", "motors"};

        for (String partType : partTypes) {
            bikePartsPage.navigateToBikeParts();
            bikePartsPage.selectPartType(partType);
            bikePartsPage.clickAddPart();
            Thread.sleep(1500);

            capture(partType + "-before-save");
            dumpDom(partType + "-before-save");

            WebElement save = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[.='Save']")));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", save);
            Thread.sleep(1500);

            capture(partType + "-after-save-validation");
            dumpDom(partType + "-after-save-validation");
        }
    }

    private void capture(String name) throws Exception {
        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(source, new File(outputDir, name + ".png"));
    }

    private void dumpDom(String name) throws Exception {
        FileWriter writer = new FileWriter(new File(outputDir, name + ".html"));
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
