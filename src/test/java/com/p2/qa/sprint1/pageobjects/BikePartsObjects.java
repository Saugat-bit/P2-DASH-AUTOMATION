package com.p2.qa.sprint1.pageobjects;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import utils.ConfigReader;

public class BikePartsObjects {
    private WebDriver driver;
    private String manufactureddate=ConfigReader.get("mfd");
    private Boolean nativePartDropdownsAvailable = null;
    
    // Navigation Elements
    @FindBy(xpath = "//span[text()='Bikes']")
    private WebElement bikesMenu;
    
    @FindBy(xpath = "//a[@href='/bikeParts' or @href='/bike-parts']")
    private WebElement bikePartsLink;
    
    @FindBy(xpath = "//button[contains(., 'Battery')]")
    private WebElement batteryBtn;
    
    @FindBy(xpath = "//button[contains(., 'Motors')]")
    private WebElement motorsBtn;
    
    @FindBy(xpath = "//button[contains(., 'Motorcontroller') or contains(., 'Motor Controller')]")
    private WebElement motorControllerBtn;
    
    @FindBy(xpath = "//button[contains(., 'VCU')]")
    private WebElement vcuBtn;
    
    @FindBy(xpath = "//button[contains(., 'KeyFob') or contains(., 'Key Fob')]")
    private WebElement keyFobBtn;
    
    @FindBy(xpath = "//button[contains(., 'Charger')]")
    private WebElement chargerBtn;

    @FindBy(xpath = "//button[contains(., 'Commboard') or contains(., 'Communication Board')]")
    private WebElement commboardBtn;
    
    @FindBy(xpath = "//button[contains(., 'Display')]")
    private WebElement displayBtn;
    
    // Common Form Elements for all parts
    @FindBy(xpath = "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'add')]")
    private WebElement addPartBtn;
    
    @FindBy(name = "identifier")
    private WebElement identifierField;
    
    @FindBy(xpath = "(//button)[3]")
    private WebElement manufacturedDateField;
    
    @FindBy(xpath = "(//button)[4]")
    private WebElement purchaseDateField;
    
    @FindBy(xpath = "//select")
    private WebElement vendorDropdown;
    
    @FindBy(xpath = "//button[.='Save']")
    private WebElement saveBtn;
    
    // Specific fields for different parts
    @FindBy(name = "software_version")
    private WebElement softwareVersionField; // For VCU
    
    @FindBy(name = "ble_name")
    private WebElement bleNameField; // For KeyFob
    
    @FindBy(name = "software_version_mcu")
    private WebElement softwareVersionMcuField; // For Display
    
    @FindBy(name = "software_version_arm")
    private WebElement softwareVersionArmField; // For Display
    
    @FindBy(name = "software_version_fex")
    private WebElement softwareVersionFexField; // For Display
    
    // Table Elements
    @FindBy(xpath = "//table/tbody/tr[1]/td[1]")
    private WebElement firstPartId;
    @FindBy(xpath = "//table/tbody/tr[1]/td[3]")
    private WebElement firstPartvendorId;
    
    @FindBy(xpath = "//table/tbody/tr[1]/td[2]")
    private WebElement firstPartIdentifier;
    
    @FindBy(xpath = "//table/tbody/tr[1]/td[6]")
    private WebElement firstPartEditBtn; // Default for most parts
    
    @FindBy(xpath = "//table/tbody/tr[1]/td[7]")
    private WebElement motorsEditBtn; // For Motors
    
    @FindBy(xpath = "//table/tbody/tr[1]/td[10]")
    private WebElement displayEditBtn; // For Display
    
    @FindBy(xpath = "//input")
    private WebElement searchField;
    
    public BikePartsObjects (WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public void navigateToBikeParts() {
    	  WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
          if (driver.getCurrentUrl().contains("bike-parts") && !isPartFormOpen()) {
              return;
          }
    	  try {
    	      boolean isVisible = false;
    	      try {
    	          isVisible = driver.findElement(By.xpath("//a[@href='/bikeParts' or @href='/bike-parts']")).isDisplayed();
    	      } catch (Exception ignore) {}
    	      
    	      if (!isVisible) {
    	          wait.until(ExpectedConditions.elementToBeClickable(bikesMenu)).click();
    	          Thread.sleep(1000); // Wait for dropdown to expand
    	      }
    	  } catch (Exception e) {
    	      System.out.println("Could not click Bikes menu: " + e.getMessage());
    	  }
      	
  	    WebElement clickablebikepartLink = wait.until(ExpectedConditions.elementToBeClickable(bikePartsLink));
  	    clickablebikepartLink.click();
  	    wait.until(ExpectedConditions.urlContains("bike-parts"));
       
        Assert.assertTrue(driver.getCurrentUrl().contains("bike-parts"), "Failed to navigate to bike parts page");
    }
    
    private void jsClick(WebElement element, WebDriverWait wait) {
        try { Thread.sleep(1000); } catch(Exception e) {}
        try {
            ((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'});", element);
            Thread.sleep(500);
        } catch(Exception e) {}
        ((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", element);
    }
    
    public void selectPartType(String partType) {
        WebDriverWait wait=new WebDriverWait(driver, Duration.ofSeconds(10));
        switch (partType.toUpperCase()) {
            case "BATTERY": jsClick(batteryBtn, wait); break;
            case "MOTORS": jsClick(motorsBtn, wait); break;
            case "MOTORCONTROLLER": jsClick(motorControllerBtn, wait); break;
            case "VCU": jsClick(vcuBtn, wait); break;
            case "KEYFOB": jsClick(keyFobBtn, wait); break;
            case "CHARGER": jsClick(chargerBtn, wait); break;
            case "COMMBOARD": jsClick(commboardBtn, wait); break;
            case "COMMUNICATIONBOARD": jsClick(commboardBtn, wait); break;
            case "DISPLAY": jsClick(displayBtn, wait); break;
        }
        waitForPartListToLoad();
    }
    
    public void clickAddPart() {
        WebDriverWait wait=new WebDriverWait(driver, Duration.ofSeconds(10));
        waitForPartListToLoad();
        By addButton = By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'add')]");
        for (int attempt = 1; attempt <= 3; attempt++) {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(addButton));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
            try {
                new Actions(driver).moveToElement(btn).click().perform();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }

            try {
                new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.name("identifier")));
                return;
            } catch (Exception ignore) {
            }
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("identifier")));
    }
    
    public void createBasicPart(String identifier, int vendorIndex) {
        fillCommonPartFields(identifier, vendorIndex);
        saveCurrentPart();
    }

    private void fillCommonPartFields(String identifier, int vendorIndex) {
    	WebDriverWait wait=new WebDriverWait(driver, Duration.ofSeconds(10));
    	WebElement identifier1 = wait.until(ExpectedConditions.elementToBeClickable(identifierField));
   	    
        identifier1.sendKeys(identifier);
        selectDate("Manufactured date", "3");
        
        selectDropdownByNameOrLabel("vendor_id", "Vendor", vendorIndex);
        
        selectDate("Purchased date", "4");
    }

    private void saveCurrentPart() {
        try { Thread.sleep(500); } catch(Exception e){}
        ((org.openqa.selenium.JavascriptExecutor)driver).executeScript("arguments[0].click();", saveBtn);
        confirmAddIfPresent();
        waitForPartListAfterSave();
    }

    public void createBatteryPart(String identifier, int vendorIndex) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(identifierField)).sendKeys(identifier);
        selectDate("Manufactured date", "3");
        selectDropdownByNameOrLabel("vendor_id", "Vendor", vendorIndex);
        selectDate("Purchased date", "4");
        selectDropdownByNameOrLabel("bms_vendor_id", "BMS Company", vendorIndex);
        selectDropdownByNameOrLabel("battery_type", "Battery Type", 1);
        fillInputByName("cell_chemistry", "NMC");
        fillInputByName("series_parallel_string", "20S10P");
        fillInputByName("design_voltage", "72");
        fillInputByName("design_capacity", "50");
        fillInputByName("max_charging_current", "20");
        fillInputByName("max_discharging_current", "100");

        saveCurrentPart();
        try { Thread.sleep(1000); } catch(Exception e) {}
    }

    private void selectDate(String label, String day) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        By dateButton = By.xpath("//label[contains(., '" + label + "')]/following-sibling::button");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(dateButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        try { Thread.sleep(500); } catch(Exception e) {}

        WebElement dayButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("(//button[normalize-space()='" + day + "' and not(@disabled)])[last()]")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dayButton);
    }

    private void selectDropdownByName(String name, int index) {
        WebElement selectElement = new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.elementToBeClickable(By.name(name)));
        new Select(selectElement).selectByIndex(index);
    }

    private void selectDropdownByNameOrLabel(String name, String label, int index) {
        if (!Boolean.FALSE.equals(nativePartDropdownsAvailable)) {
            try {
                WebElement selectElement = new WebDriverWait(driver, Duration.ofSeconds(1))
                    .until(ExpectedConditions.elementToBeClickable(By.name(name)));
                new Select(selectElement).selectByIndex(index);
                nativePartDropdownsAvailable = true;
                return;
            } catch (Exception ignore) {
                nativePartDropdownsAvailable = false;
            }
        }

        selectComboboxByLabel(label, index);
    }

    private void selectDropdownByValue(String name, String value) {
        WebElement selectElement = new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.elementToBeClickable(By.name(name)));
        new Select(selectElement).selectByValue(value);
    }

    private void selectComboboxByLabel(String label, int index) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        By buttonLocator = By.xpath("//label[contains(., '" + label + "')]/following-sibling::button");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(buttonLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
        String beforeText = button.getText();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        try { Thread.sleep(500); } catch(Exception e) {}

        try {
            List<WebElement> options = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//*[@role='listbox' and @data-state='open']//*[@role='option' and not(@aria-disabled='true')]")
            ));
            int optionIndex = Math.max(0, index - 1);
            WebElement option = visibleElementAtOrLast(options, optionIndex);
            new Actions(driver).moveToElement(option).click().perform();
            waitForRadixSelectToClose();
            wait.until(driver -> {
                WebElement selectedButton = driver.findElement(buttonLocator);
                String selectedText = selectedButton.getText();
                return selectedText != null
                    && !selectedText.trim().isEmpty()
                    && !selectedText.equals(beforeText)
                    && !selectedText.toLowerCase().contains("select");
            });
            return;
        } catch (Exception ignore) {
        }

        Actions actions = new Actions(driver);
        int downPresses = Math.max(1, index);
        for (int i = 0; i < downPresses; i++) {
            actions.sendKeys(Keys.ARROW_DOWN);
        }
        actions.sendKeys(Keys.ENTER).perform();
        waitForRadixSelectToClose();
    }

    private void fillInputByName(String name, String value) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        waitForRadixSelectToClose();
        WebElement input = wait.until(driver -> {
            List<WebElement> inputs = driver.findElements(By.name(name));
            for (WebElement candidate : inputs) {
                if (candidate.isDisplayed() && candidate.isEnabled()) {
                    return candidate;
                }
            }
            return null;
        });

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", input);
        try {
            input.clear();
            input.sendKeys(value);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                    + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                    + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                input,
                value
            );
        }
    }

    private void selectRadixOptionByLabel(String label) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        By buttonLocator = By.xpath("//label[contains(., '" + label + "')]/following-sibling::button");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(buttonLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        try { Thread.sleep(500); } catch(Exception e) {}
        try {
            List<WebElement> options = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//*[@role='listbox' and @data-state='open']//*[@role='option' and not(@aria-disabled='true')]")
            ));
            WebElement option = visibleElementAtOrLast(options, 0);
            new Actions(driver).moveToElement(option).click().perform();
        } catch (Exception e) {
            new Actions(driver).sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ENTER).perform();
        }
        waitForRadixSelectToClose();
    }

    private WebElement visibleElementAtOrLast(List<WebElement> elements, int requestedIndex) {
        int visibleIndex = 0;
        WebElement lastVisible = null;
        for (WebElement element : elements) {
            if (element.isDisplayed()) {
                if (visibleIndex == requestedIndex) {
                    return element;
                }
                lastVisible = element;
                visibleIndex++;
            }
        }
        if (lastVisible != null) {
            return lastVisible;
        }
        return elements.get(Math.min(requestedIndex, elements.size() - 1));
    }

    private void waitForRadixSelectToClose() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//*[@role='listbox' and @data-state='open']")
                ));
        } catch (Exception ignore) {
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            try { Thread.sleep(300); } catch(Exception e) {}
        }
    }

    private boolean isPartFormOpen() {
        try {
            return driver.findElements(By.name("identifier")).stream()
                .anyMatch(element -> element.isDisplayed());
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForPartListToLoad() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'add')]")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//table")),
                    ExpectedConditions.presenceOfElementLocated(By.name("identifier"))
                ));
        } catch (Exception ignore) {
        }
    }

    private void waitForPartListAfterSave() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'add')]")),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//table"))
                ));
        } catch (Exception ignore) {
        }
    }

    private void confirmAddIfPresent() {
        try {
            WebElement confirm = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(., 'Yes, Add') or contains(., 'Yes, Save') or contains(., 'Yes, Update')]")
                ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirm);
            new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//div[@data-state='open' and contains(@class, 'fixed') and contains(@class, 'inset-0')]")
                ));
        } catch (Exception ignore) {
        }
    }
    
    public void createVCU(String identifier, int vendorIndex, String softwareVersion) {
        fillCommonPartFields(identifier, vendorIndex);
        fillInputByName("software_version", softwareVersion);
        selectRadixOptionByLabel("Hardware Version");
        saveCurrentPart();
    }
    
    public void createKeyFob(String identifier, int vendorIndex, String bleName) {
        fillCommonPartFields(identifier, vendorIndex);
        fillInputByName("ble_name", bleName);
        saveCurrentPart();
    }

    public void createCommboard(String identifier, int vendorIndex) {
        createBasicPart(identifier, vendorIndex);
    }
    
    public void createDisplay(String identifier, int vendorIndex, String mcuVersion, String armVersion, String fexVersion) {
        fillCommonPartFields(identifier, vendorIndex);
        fillInputByName("software_version_mcu", mcuVersion);
        fillInputByName("software_version_arm", armVersion);
        fillInputByName("software_version_fex", fexVersion);
        selectRadixOptionByLabel("Hardware Type");
        saveCurrentPart();
    }
    
    public void editPartForMtorAndDisplay(String partType) {
        switch (partType.toLowerCase()) {
            case "motors": motorsEditBtn.click(); break;
            case "display": displayEditBtn.click(); break;
            default: firstPartEditBtn.click(); break;
        }
    }
    public void editPart()
    {
    	firstPartEditBtn.click();
    }
    public void editPartforMotor()
    {
    	motorsEditBtn.click();
    }
    public void  clickSaveButton()
    {
    	  saveBtn.click();
    }
    
    public void searchPart(String identifier) {
        searchField.clear();
        searchField.sendKeys(identifier);
    }

    public boolean isPartVisible(String identifier) {
        searchPart(identifier);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.textToBePresentInElementLocated(
                    By.xpath("//table/tbody/tr[1]"),
                    identifier
                ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getFirstPartId() {
        return firstPartId.getText();
    }
    
    public String getFirstPartIdentifier() {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOf(firstPartIdentifier))
            .getText();
    }
    public String getFirstpartvendorid() {
    	System.out.println("part is cerated using "+firstPartvendorId.getText()+"vendor id");
    	return firstPartvendorId.getText();
    	
    	
    }
}
