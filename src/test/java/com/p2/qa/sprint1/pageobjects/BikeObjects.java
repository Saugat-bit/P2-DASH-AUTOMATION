package com.p2.qa.sprint1.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import com.p2.automationbase.Base;

import java.time.Duration;

public class BikeObjects extends Base {
    
    WebDriver driver;
    WebDriverWait wait;
    Actions actions;

    // Locators
    By bikesMenu = By.xpath("//div[span[span[text()='Bikes']]] | //li[.//span[text()='Bikes']]/div");
    By bikesLink = By.xpath("//a[@href='/bikes' or @href='/production' or @href='/bike']");
    By addNewBikeBtn = By.xpath("//button[contains(., 'Add New Bike')]");
    
    // Add Bike Form Locators (Wait for form to load by checking for Bike Model label)
    By bikeModelBtn = By.xpath("//label[contains(., 'Bike Model')]/following-sibling::button");
    By bikeNameInput = By.xpath("//label[contains(., 'Bike Name')]/following-sibling::input");
    By bikeColorBtn = By.xpath("//label[contains(., 'Bike Color')]/following-sibling::button");
    By mfgDateBtn = By.xpath("//label[contains(., 'Manufactured Date')]/following-sibling::button");
    
    // Identifiers
    By fixedBatteryBtn = By.xpath("//label[contains(., 'Fixed Battery Identifier')]/following-sibling::button");
    By displayBtn = By.xpath("//label[contains(., 'Display Identifier')]/following-sibling::button");
    By vinInput = By.xpath("//label[contains(., 'VIN Number')]/following-sibling::input");
    By chargerBtn = By.xpath("//label[contains(., 'Charger Identifier')]/following-sibling::button");
    By controllerBtn = By.xpath("//label[contains(., 'Controller Identifier')]/following-sibling::button");
    By vcuBtn = By.xpath("//label[contains(., 'VCU Identifier')]/following-sibling::button");
    By motorBtn = By.xpath("//label[contains(., 'Motor Identifier')]/following-sibling::button");
    By communicationBoardBtn = By.xpath("//label[contains(., 'Communication Board') or contains(., 'Commboard') or contains(., 'Comm-Board')]/following-sibling::button");
    
    By submitBtn = By.xpath("//button[contains(., 'Submit')]");
    By toastOrAlert = By.xpath("//*[contains(@class, 'toast') or contains(@class, 'alert') or contains(@class, 'error')]");

    public BikeObjects(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.actions = new Actions(driver);
    }
    
    public void navigateToBikes() {
        try {
            boolean isVisible = false;
            try {
                isVisible = driver.findElement(bikesLink).isDisplayed();
            } catch (Exception ignore) {}
            
            if (!isVisible) {
                wait.until(ExpectedConditions.elementToBeClickable(bikesMenu)).click();
                Thread.sleep(1000); // Wait for dropdown to expand
            }
        } catch (Exception e) {
            System.out.println("Could not click Bikes menu: " + e.getMessage());
        }
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(bikesLink));
        link.click();

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(addNewBikeBtn));
        addButton.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(bikeModelBtn));
    }
    
    private void selectDropdown(By locator, String value) throws InterruptedException {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        WebElement option = findVisibleDropdownOption(value);
        if (option != null) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
        } else {
            new Actions(driver).sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ENTER).perform();
        }
        waitForRadixSelectToClose();
    }

    private WebElement findVisibleDropdownOption(String value) {
        WebDriverWait dropdownWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        By radixOption = By.xpath("//*[@role='listbox' and @data-state='open']//*[@role='option' and not(@aria-disabled='true')]");
        By exactTextOption = By.xpath("//*[normalize-space()='" + value + "' and not(self::label) and not(self::button)]");

        try {
            return dropdownWait.until(ExpectedConditions.visibilityOfElementLocated(radixOption));
        } catch (Exception ignore) {
        }

        if (value != null && !"1".equals(value)) {
            try {
                return dropdownWait.until(ExpectedConditions.visibilityOfElementLocated(exactTextOption));
            } catch (Exception ignore) {
            }
        }

        return null;
    }

    public void fillAddBikeForm(String bikeName, String vin) throws InterruptedException {
        // Model
        selectDropdown(bikeModelBtn, "P2"); // Assuming we want first option
        
        // Name
        wait.until(ExpectedConditions.elementToBeClickable(bikeNameInput)).sendKeys(bikeName);
        
        // Color
        selectDropdown(bikeColorBtn, "Red"); 
        
        // Date - This is a datepicker button. 
        selectManufacturedDate();
        
        // Battery
        selectDropdown(fixedBatteryBtn, "1");
        
        // Display
        selectDropdown(displayBtn, "1");
        
        // VIN
        wait.until(ExpectedConditions.elementToBeClickable(vinInput)).sendKeys(vin);
        
        // Charger
        selectDropdown(chargerBtn, "1");
        
        // Controller
        selectDropdown(controllerBtn, "1");
        
        // VCU
        selectDropdown(vcuBtn, "1");
        
        // Motor
        selectDropdown(motorBtn, "1");

        // Communication Board
        selectDropdown(communicationBoardBtn, "1");
    }
    
    public void clickSubmit() {
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
        confirmAddBikeIfPresent();
    }

    private void selectManufacturedDate() throws InterruptedException {
        WebElement dateBtn = wait.until(ExpectedConditions.elementToBeClickable(mfgDateBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dateBtn);
        Thread.sleep(500);

        WebElement dayButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("(//button[normalize-space()='4' and not(@disabled)])[last()]")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dayButton);
        Thread.sleep(500);
    }

    private void confirmAddBikeIfPresent() {
        try {
            WebElement confirm = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(., 'Yes, Add Bike') or contains(., 'Yes, Add')]")
                ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirm);
            new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//div[@data-state='open' and contains(@class, 'fixed') and contains(@class, 'inset-0')]")
                ));
        } catch (Exception ignore) {
        }
    }

    private void waitForRadixSelectToClose() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//*[@role='listbox' and @data-state='open']")
                ));
        } catch (Exception ignore) {
            ((JavascriptExecutor) driver).executeScript("if (document.activeElement) { document.activeElement.blur(); }");
        }
    }

    public void createBikeWithFirstAvailableParts(String bikeName, String vin) throws InterruptedException {
        navigateToBikes();
        fillAddBikeForm(bikeName, vin);
        clickSubmit();
        Thread.sleep(2000);
    }

    public String getVisibleFeedbackText() {
        try {
            WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(toastOrAlert));
            return message.getText();
        } catch (Exception e) {
            return "";
        }
    }
}
