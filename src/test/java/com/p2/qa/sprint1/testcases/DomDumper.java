package com.p2.qa.sprint1.testcases;

import org.openqa.selenium.WebDriver;
import com.p2.automationbase.Base;
import com.p2.automationbase.Login;
import java.io.FileWriter;

import org.testng.annotations.Test;

public class DomDumper {
    @Test
    public void dumpDom() throws Exception {
        Base base = new Base();
        WebDriver driver = base.initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();
        
        Login login = new Login();
        login.loginAs(driver, "admin");
        
        Thread.sleep(3000); // Wait for dashboard to fully render
        
        org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        
        try {
            // Expand Bikes
            org.openqa.selenium.WebElement bikesMenu = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(org.openqa.selenium.By.xpath("//span[text()='Bikes']")));
            bikesMenu.click();
            Thread.sleep(1000);
            
            // Try to find the bike-parts link
            org.openqa.selenium.WebElement bpLink = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(org.openqa.selenium.By.xpath("//a[@href='/bikeParts' or @href='/bike-parts']")));
            bpLink.click();
            Thread.sleep(5000); // wait for bike parts page to load
            
            // Try to click Add Battery
            org.openqa.selenium.WebElement addPartBtn = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(org.openqa.selenium.By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'add')]")));
            addPartBtn.click();
            Thread.sleep(3000);
            
            FileWriter writer = new FileWriter("dom_add_part.html");
            writer.write(driver.getPageSource());
            writer.close();
            System.out.println("BikeParts DOM successfully dumped!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            driver.navigate().refresh();
            Thread.sleep(3000);
            
            // Expand Bikes
            org.openqa.selenium.WebElement bikesMenu = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(org.openqa.selenium.By.xpath("//div[span[span[text()='Bikes']]] | //li[.//span[text()='Bikes']]/div")));
            bikesMenu.click();
            Thread.sleep(1000);
            
            org.openqa.selenium.WebElement bpLink = driver.findElement(org.openqa.selenium.By.xpath("//a[@href='/bikeParts' or @href='/bike-parts']"));
            bpLink.click();
            Thread.sleep(3000);
            
            FileWriter writer = new FileWriter("dom_bikeparts.html");
            writer.write(driver.getPageSource());
            writer.close();
            System.out.println("BikeParts DOM successfully dumped!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        driver.quit();
    }
}
