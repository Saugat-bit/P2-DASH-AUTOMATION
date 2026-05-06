package com.p2.qa.sprint1.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.p2.automationbase.Base;

import java.time.Duration;
import java.util.List;

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
    
    private void selectDropdown(By locator, String value) {
        if (selectNativeDropdownNear(locator, value)) {
            return;
        }

        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(locator));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
                String beforeText = btn.getText();
                clickElement(btn);

                WebElement option = findVisibleDropdownOption(value);
                if (option != null) {
                    clickElement(option);
                } else {
                    chooseFocusedDropdownOption(value);
                }

                waitForRadixSelectToClose();
                waitUntilDropdownChanged(locator, beforeText);
                return;
            } catch (RuntimeException e) {
                lastError = e;
                dismissOpenDropdown();
            }
        }

        throw lastError;
    }

    private WebElement findVisibleDropdownOption(String value) {
        WebDriverWait dropdownWait = new WebDriverWait(driver, Duration.ofSeconds(3));
        By radixOption = By.xpath("//*[@role='listbox' and @data-state='open']//*[@role='option' and not(@aria-disabled='true')]");
        By exactTextOption = By.xpath("//*[normalize-space()='" + value + "' and not(self::label) and not(self::button)]");

        try {
            List<WebElement> options = dropdownWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(radixOption));
            if (options.isEmpty()) {
                return null;
            }

            for (WebElement option : options) {
                if (option.isDisplayed() && value != null && option.getText().trim().equalsIgnoreCase(value)) {
                    return option;
                }
            }

            return firstDisplayedElement(options);
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

    private boolean selectNativeDropdownNear(By buttonLocator, String value) {
        try {
            WebElement button = driver.findElement(buttonLocator);
            WebElement fieldContainer = button.findElement(By.xpath("./ancestor::*[.//label][1]"));
            List<WebElement> selects = fieldContainer.findElements(By.tagName("select"));
            if (selects.isEmpty()) {
                return false;
            }

            Select select = new Select(selects.get(0));
            try {
                select.selectByVisibleText(value);
            } catch (Exception e) {
                select.selectByIndex(1);
            }
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    private WebElement firstDisplayedElement(List<WebElement> elements) {
        for (WebElement element : elements) {
            if (element.isDisplayed()) {
                return element;
            }
        }
        return elements.get(0);
    }

    private void clickElement(WebElement element) {
        try {
            new Actions(driver).moveToElement(element).click().perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void chooseFocusedDropdownOption(String value) {
        Actions keySelection = new Actions(driver);
        if (value != null && !"1".equals(value.trim())) {
            keySelection.sendKeys(value);
        } else {
            keySelection.sendKeys(Keys.ARROW_DOWN);
        }
        keySelection.sendKeys(Keys.ENTER).perform();
    }

    private void waitUntilDropdownChanged(By locator, String beforeText) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(driver -> {
                WebElement selectedButton = driver.findElement(locator);
                String selectedText = selectedButton.getText();
                return selectedText != null
                    && !selectedText.trim().isEmpty()
                    && !selectedText.equals(beforeText)
                    && !selectedText.toLowerCase().contains("select");
            });
        } catch (Exception ignore) {
        }
    }

    private void dismissOpenDropdown() {
        try {
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            Thread.sleep(300);
        } catch (Exception ignore) {
        }
    }

    public void fillAddBikeForm(String bikeName, String vin) {
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

    private void selectManufacturedDate() {
        WebElement dateBtn = wait.until(ExpectedConditions.elementToBeClickable(mfgDateBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dateBtn);
        waitForDatePickerToOpen();

        WebElement dayButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("(//button[normalize-space()='4' and not(@disabled)])[last()]")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dayButton);
    }

    private void waitForDatePickerToOpen() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[normalize-space()='4' and not(@disabled)]")
                ));
        } catch (Exception ignore) {
        }
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
            dismissOpenDropdown();
            ((JavascriptExecutor) driver).executeScript("if (document.activeElement) { document.activeElement.blur(); }");
        }
    }

    public void createBikeWithFirstAvailableParts(String bikeName, String vin) throws InterruptedException {
        navigateToBikes();
        fillAddBikeForm(bikeName, vin);
        clickSubmit();
        waitForBikeSubmitToSettle();
    }

    private void waitForBikeSubmitToSettle() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.or(
                    ExpectedConditions.elementToBeClickable(addNewBikeBtn),
                    ExpectedConditions.urlContains("/bikes"),
                    ExpectedConditions.urlContains("/bike"),
                    ExpectedConditions.presenceOfElementLocated(toastOrAlert)
                ));
        } catch (Exception ignore) {
        }
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
