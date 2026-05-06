package com.p2.automationbase;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.TimeoutException;
import utils.ConfigReader;

public class Login {

    // Refactored to use driver passed from test
    public WebDriver loginAs(WebDriver driver, String role) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        String username;
        String password;

        if (role.equalsIgnoreCase("admin")) {
            username = ConfigReader.get("email");
            password = ConfigReader.get("password");
        } else {
            throw new IllegalArgumentException("Unsupported role: " + role);
        }

        for (int attempt = 1; attempt <= 3; attempt++) {
            // Wait until username field is visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("new-email")));

            driver.findElement(By.id("new-email")).clear();
            driver.findElement(By.id("new-email")).sendKeys(username);
            driver.findElement(By.xpath("//input[@id='new-password']")).clear();
            driver.findElement(By.xpath("//input[@id='new-password']")).sendKeys(password);

            driver.findElement(By.xpath("//button[.='Login']")).click();

            try {
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Bikes']")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@href='/vendor' or @href='/bike-parts' or @href='/bikeParts']"))
                ));
                return driver;
            } catch (TimeoutException e) {
                if (attempt == 3) {
                    throw e;
                }
                driver.navigate().refresh();
            }
        }

        return driver; // returns the same driver (Grid or local)
    }
}
