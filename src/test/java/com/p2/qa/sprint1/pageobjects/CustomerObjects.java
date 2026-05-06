package com.p2.qa.sprint1.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import com.p2.automationbase.Base;

import java.time.Duration;

public class CustomerObjects extends Base {
    
    WebDriver driver;
    WebDriverWait wait;
    Actions actions;

    // Locators
    By customersMenu = By.xpath("//div[span[span[text()='Customers']]] | //li[.//span[text()='Customers']]/div");
    By customersLink = By.xpath("//a[@href='/customer' or @href='/customers']");
    By registerNewBtn = By.xpath("//button[contains(., 'Register New Customer')] | //a[contains(., 'Register New Customer')]");
    
    // Form Locators (First Step)
    By emailInput = By.xpath("//input[@placeholder='Enter email' or @name='email']");
    By continueBtn = By.xpath("//button[contains(., 'Continue')] | //button[contains(., 'Next')]");

    // Locators for second step (assuming these will appear after entering an unused email)
    By firstNameInput = By.xpath("//input[@name='firstName' or contains(@placeholder, 'First')]");
    By lastNameInput = By.xpath("//input[@name='lastName' or contains(@placeholder, 'Last')]");
    By phoneInput = By.xpath("//input[@name='phone' or contains(@placeholder, 'Phone')]");
    By submitBtn = By.xpath("//button[contains(., 'Submit') or contains(., 'Register') or contains(., 'Create')]");

    public CustomerObjects(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.actions = new Actions(driver);
    }
    
    public void navigateToCustomers() {
        try {
            boolean isVisible = false;
            try {
                isVisible = driver.findElement(customersLink).isDisplayed();
            } catch (Exception ignore) {}
            
            if (!isVisible) {
                wait.until(ExpectedConditions.elementToBeClickable(customersMenu)).click();
                Thread.sleep(1000); // Wait for dropdown to expand
            }
        } catch (Exception e) {
            System.out.println("Could not click Customers menu: " + e.getMessage());
        }
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(customersLink));
        link.click();
        
        wait.until(ExpectedConditions.urlContains("customer"));
    }
    
    public void clickRegisterCustomer() {
        wait.until(ExpectedConditions.elementToBeClickable(registerNewBtn)).click();
    }
    
    public void fillEmailStep(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(email);
        wait.until(ExpectedConditions.elementToBeClickable(continueBtn)).click();
    }
    
    public void fillDetailsStep(String firstName, String lastName, String phone) throws InterruptedException {
        // Wait for next step to load
        Thread.sleep(2000);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameInput)).sendKeys(firstName);
            driver.findElement(lastNameInput).sendKeys(lastName);
            driver.findElement(phoneInput).sendKeys(phone);
        } catch (Exception e) {
            System.out.println("Could not find standard details fields, continuing anyway: " + e.getMessage());
        }
    }
    
    public void clickSubmit() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
        } catch (Exception e) {
            System.out.println("Submit button not clicked, maybe not present.");
        }
    }
}
