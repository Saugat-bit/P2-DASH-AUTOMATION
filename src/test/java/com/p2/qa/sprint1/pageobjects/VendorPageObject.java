package com.p2.qa.sprint1.pageobjects;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

public class VendorPageObject {
	
    private WebDriver driver;
    
    // Vendor Navigation and Form Elements
    @FindBy(xpath = "//span[text()='Bikes']")
    private WebElement bikesMenu;
    
    @FindBy(xpath = "//a[@href='/vendor']")
    private WebElement vendorLink;
    
    @FindBy(xpath = "//button[.='Add new vendor']")
    private WebElement addNewVendorBtn;
    
    @FindBy(name = "vendor_name")
    private WebElement vendorNameField;
    
    @FindBy(xpath="//button[.='Select a Country']")
    private WebElement countryField;
    
    @FindBy(xpath="//button[.='Select a Country']")
    private WebElement country;
    
    @FindBy(name = "remarks")
    private WebElement remarksField;
    
    @FindBy(xpath = "//button[.='Save']")
    private WebElement saveBtn;
    
    // Vendor Table Elements
    @FindBy(xpath = "//table/tbody/tr[1]/td[1]")
    private WebElement firstVendorId;
    
    @FindBy(xpath = "//table/tbody/tr[1]/td[2]")
    private WebElement firstVendorName;
    
    @FindBy(xpath = "//table/tbody/tr[1]/td[6]")
    private WebElement firstVendorEditBtn;
    
    @FindBy(xpath = "//input")
    private WebElement searchField;
    
    public VendorPageObject(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    public void getfirstVendorName()
    {
    	firstVendorName.getText();
    }
    
    public void navigateToVendorPage() {
    	
    	   WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    	   try {
    	       boolean isVisible = false;
    	       try {
    	           isVisible = driver.findElement(By.xpath("//a[@href='/vendor']")).isDisplayed();
    	       } catch (Exception ignore) {}
    	       
    	       if (!isVisible) {
    	           wait.until(ExpectedConditions.elementToBeClickable(bikesMenu)).click();
    	           Thread.sleep(1000); // Wait for dropdown to expand
    	       }
    	   } catch (Exception e) {
    	       System.out.println("Could not click Bikes menu: " + e.getMessage());
    	   }
    	
    	    WebElement clickableVendorLink = wait.until(ExpectedConditions.elementToBeClickable(vendorLink));
    	    
    	    clickableVendorLink.click();
    	    
    	    // Wait for page to load
    	    wait.until(ExpectedConditions.urlContains("/vendor"));
    	
        
        Assert.assertTrue(driver.getCurrentUrl().contains("/vendor"), "Failed to navigate to vendor page");
    }
    
    public void clickAddNewVendor() {
        addNewVendorBtn.click();
    }
    
    public void createVendor(String vendorName, String country, String remarks) {
    	vendorNameField.clear();
        vendorNameField.sendKeys(vendorName);
        
        countryField.click();
        try { Thread.sleep(1000); } catch(Exception e) {}
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            String lowerCountry = country.toLowerCase();
            String xpath = "//div[translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='" + lowerCountry + "'] | //li[translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='" + lowerCountry + "'] | //span[translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='" + lowerCountry + "']";
            WebElement countryOpt = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", countryOpt);
        } catch (Exception e) {
            System.out.println("Failed to click country option: " + e.getMessage());
            // fallback: try ARROW_DOWN + ENTER
            org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
            actions.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN).sendKeys(org.openqa.selenium.Keys.ENTER).perform();
        }
        
        try { Thread.sleep(500); } catch(Exception e) {}
        
        if (remarks != null && !remarks.isEmpty()) {
        	remarksField.clear();
            remarksField.sendKeys(remarks);
        }
        saveBtn.click();
      
    }
    
    public void editFirstVendor() {
        firstVendorEditBtn.click();
    }
    
    public void searchVendor(String vendorName) {
        searchField.clear();
        searchField.sendKeys(vendorName);
    }
    
    public String getFirstVendorId() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table/tbody/tr[1]/td[1]"))).getText();
    }
    
    public String getFirstVendorName()
    {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table/tbody/tr[1]/td[2]"))).getText();
    }
}

// Bike Parts Page Object
