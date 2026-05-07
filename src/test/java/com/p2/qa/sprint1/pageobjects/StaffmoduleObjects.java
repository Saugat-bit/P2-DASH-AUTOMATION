package com.p2.qa.sprint1.pageobjects;

import java.io.File;
import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.ConfigReader;
import utils.TestDataGenerator;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.Keys;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StaffmoduleObjects {
    private WebDriver driver;
    private WebDriverWait wait;
    private static String password = ConfigReader.get("strongpassword");
    private static final long REVIEW_PAUSE_MS = Long.getLong("ui.flow.review.pause.ms", 300L);
    private static final boolean REVIEW_SCREENSHOTS_ENABLED = Boolean.parseBoolean(
        System.getProperty("ui.flow.review.screenshots", "false")
    );
    private static final boolean REVIEW_HTML_ENABLED = Boolean.parseBoolean(
        System.getProperty("ui.flow.review.html", "false")
    );
    
    // Constructor
    public StaffmoduleObjects(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    // Locators - Staff List Page
    private By staffsLink = By.xpath("//a[@href='/staffs']");
    private By searchField = By.xpath("//input[@placeholder='Search...']");
    private By addStaffButton = By.xpath("//button[.='Add Staff']");
    private By staffTable = By.xpath("//table/tbody");
    private By staffTableRows = By.xpath("//table/tbody/tr");
    
    // Staff Form Locators (Add/Edit)
    private By nameField = By.xpath("//input[@name='name']");
    private By emailField = By.xpath("//input[@name='email']");
    private By passwordField = By.xpath("//input[@name='password']");
    private By phoneNumberField = By.xpath("//input[@name='phone_number']");
    private By policyfield= By.xpath("//button[.='Select a policy']");
    private By policyfieldAdmin= By.xpath("//div[.='Admin']");
    private By  servicecenterfield=By.xpath("//button[.='Select a service center']");
    
  
   

    // Form Action Buttons
    private By createStaffButton = By.xpath("//button[.='Add Staff']");
    private By updateStaffButton = By.xpath("//button[.='Update Staff']");
    private By cancelButton = By.xpath("//button[.='Cancel']");
    
    // Staff Table Columns (for first row)
    private By staffIdColumn = By.xpath("//table/tbody/tr[1]/td[1]");
    private By staffNameColumn = By.xpath("//table/tbody/tr[1]/td[2]");
    private By staffEmailColumn = By.xpath("//table/tbody/tr[1]/td[3]");
    private By staffPhoneColumn = By.xpath("//table/tbody/tr[1]/td[4]");
    private By staffStatusColumn = By.xpath("//table/tbody/tr[1]/td[5]");
    private By staffRolesColumn = By.xpath("//table/tbody/tr[1]/td[6]");
    private By staffAddedDateColumn = By.xpath("//table/tbody/tr[1]/td[7]");
    private By staffEditIcon = By.xpath("//table/tbody/tr[1]/td[8]");
    private By addStaffpopup=By.xpath("//button[.='Yes, Add Staff']");
    private By updateStaffpopup=By.xpath("//button[.='Yes, Update Staff']");
    // Error/Success Messages
    private By errorMessage = By.xpath("//div[contains(@class,'error') or contains(@class,'alert-danger')]");
    private By successMessage = By.xpath("//div[contains(@class,'success') or contains(@class,'alert-success')]");
    
    
    public void policyfieldselectAsAdmin()
    {
        WebElement policyFieldSelect = wait.until(ExpectedConditions.elementToBeClickable(policyfield));
        policyFieldSelect.click();
        WebElement policyFieldSelectAdmin = wait.until(ExpectedConditions.elementToBeClickable(policyfieldAdmin));
        policyFieldSelectAdmin.click();
        

    	
    }
    public String generateUniqueStaffName() {

    	String firstName = TestDataGenerator.getRandomName();
        
        return firstName ;
    }
    
    
    public String generateUniqueEmail() {
    	return TestDataGenerator.getRandomEmail();	
    	
    }
    
    
    public String generateUniquePhoneNumber() {
        return TestDataGenerator.getRandomPhoneNumber();
    }
   
    public String generateInvalidEmail() {
        String[] invalidEmails = {
            "invalid-email",
            "test@",
            "@domain.com",
            "t@est..test@domain.com",
            "test@domain",
            "test space@domain.com"
        };
        Random random=new Random();
        return invalidEmails[random.nextInt(invalidEmails.length)];
    }
    
    
   public void updateStaff()
   {
	   reviewBeforeAction("staff-confirm-before-update-staff");
	   wait.until(ExpectedConditions.elementToBeClickable(updateStaffpopup)).click();;
   }
    
    private By administrationLink = By.xpath("//span[text()='Administration']");

    public void navigateToStaffsPage() {
        if (fastNavigateToStaffsPage()) {
            return;
        }

        try {
            boolean isVisible = false;
            try {
                isVisible = driver.findElement(By.xpath("//a[@href='/staffs']")).isDisplayed();
            } catch (Exception ignore) {}
            
            if (!isVisible) {
                wait.until(ExpectedConditions.elementToBeClickable(administrationLink)).click();
                Thread.sleep(1000); // Wait for dropdown to expand
            }
        } catch (Exception e) {
            System.out.println("Could not click Administration menu: " + e.getMessage());
        }
        wait.until(ExpectedConditions.elementToBeClickable(staffsLink)).click();
      
    }

    private boolean fastNavigateToStaffsPage() {
        try {
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl != null && currentUrl.matches("https?://[^/]+/staffs([/?#].*)?$")) {
                return true;
            }

            WebElement link = new WebDriverWait(driver, Duration.ofMillis(300))
                .until(driver -> {
                    List<WebElement> links = driver.findElements(By.xpath("//a[@href='/staffs']"));
                    for (WebElement candidate : links) {
                        if (candidate.isDisplayed() && candidate.isEnabled()) {
                            return candidate;
                        }
                    }
                    return null;
                });
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
            return true;
        } catch (Exception e) {
            return navigateToStaffsByUrl();
        }
    }

    private boolean navigateToStaffsByUrl() {
        String baseUrl = ConfigReader.get("base.url");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return false;
        }

        driver.get(baseUrl.replaceAll("/+$", "") + "/staffs");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/staffs"),
                    ExpectedConditions.elementToBeClickable(addStaffButton)
                ));
        } catch (Exception ignored) {
        }
        return true;
    }
    
    public void clickAddStaffButton() {
        wait.until(ExpectedConditions.elementToBeClickable(addStaffButton)).click();
    }
    
    public void clickCancelButton() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton)).click();
    }
   
    public void clearAndFillName(String name) {
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(nameField));
        nameInput.clear();
        nameInput.sendKeys(name);
    }
    
    public void clearAndFillEmail(String email) {
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(emailField));
        emailInput.clear();
        emailInput.sendKeys(email);
    }
    
    public void clearAndFillPassword(String password) {
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        passwordInput.clear();
        passwordInput.sendKeys(password);
    }
    
    public void clearAndFillPhoneNumber(String phoneNumber) {
        WebElement phoneInput = wait.until(ExpectedConditions.elementToBeClickable(phoneNumberField));
        phoneInput.clear();
        phoneInput.sendKeys(phoneNumber);
    }
    
    public void fillStaffDetails(String name, String email, String password, String phoneNumber) {
        clearAndFillName(name);
        clearAndFillEmail(email);
        clearAndFillPassword(password);
        clearAndFillPhoneNumber(phoneNumber);
        policyfieldselectAsAdmin();
        
        
    }
 
    
    public void clickCreateStaffButton() {
        reviewBeforeAction("staff-form-before-add-staff");
        wait.until(ExpectedConditions.elementToBeClickable(createStaffButton)).click();
        reviewBeforeAction("staff-confirm-before-add-staff");
        wait.until(ExpectedConditions.elementToBeClickable(addStaffpopup)).click();
    }
    
    public void clickUpdateStaffButton() {
        reviewBeforeAction("staff-form-before-update-staff");
        wait.until(ExpectedConditions.elementToBeClickable(updateStaffButton)).click();
    }

    private void reviewBeforeAction(String name) {
        if (!REVIEW_SCREENSHOTS_ENABLED && !REVIEW_HTML_ENABLED) {
            return;
        }

        try {
            File dir = new File("screenshots/flow-review");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            long timestamp = System.currentTimeMillis();
            File image = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(image, new File(dir, timestamp + "_" + name + ".png"));
            if (REVIEW_HTML_ENABLED) {
                FileUtils.writeStringToFile(
                    new File(dir, timestamp + "_" + name + ".html"),
                    driver.getPageSource(),
                    "UTF-8"
                );
            }
            if (REVIEW_PAUSE_MS > 0) {
                Thread.sleep(REVIEW_PAUSE_MS);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not save review screenshot for " + name, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during review pause for " + name, e);
        }
    }

    public void searchStaff(String searchTerm) {
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(searchField));
        searchBox.clear();
        searchBox.sendKeys(searchTerm);
        searchBox.sendKeys(Keys.ENTER);
    }
    
    public void editStaffByIndex(int rowIndex) {
        By editIcon = By.xpath("//table/tbody/tr[" + rowIndex + "]/td[7]");
        wait.until(ExpectedConditions.elementToBeClickable(editIcon)).click();
    }
    
    public void editFirstStaff() {
        editStaffByIndex(1);
    }
    
    public void editStaffByName(String staffName) {
        searchStaff(staffName);
        editFirstStaff();
    }
    
    public void editStaffByEmail(String staffEmail) {
        searchStaff(staffEmail);
        editFirstStaff();
    }
    
   
    
    
    public int getStaffCount() {
        List<WebElement> staffRows = driver.findElements(staffTableRows);
        return staffRows.size();
    }
    
    // ==== VERIFICATION METHODS ====
    
    public boolean isStaffsPageDisplayed() {
        return wait.until(ExpectedConditions.elementToBeClickable(searchField)).isDisplayed();
    }
    
    public boolean isAddStaffFormDisplayed() {
        return wait.until(ExpectedConditions.elementToBeClickable(nameField)).isDisplayed();
    }
    
    public boolean isEditStaffFormDisplayed() {
        return wait.until(ExpectedConditions.elementToBeClickable(updateStaffButton)).isDisplayed();
    }
    
    public boolean isStaffTableDisplayed() {
        return wait.until(ExpectedConditions.elementToBeClickable(staffTable)).isDisplayed();
    }
    
    public boolean isStaffCreated() {
        return wait.until(ExpectedConditions.elementToBeClickable(staffIdColumn)).isDisplayed();
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            return driver.findElement(errorMessage).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isSuccessMessageDisplayed() {
        try {
            return driver.findElement(successMessage).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==== DATA RETRIEVAL METHODS ==== 
    
    public String getStaffId() {
        return wait.until(ExpectedConditions.elementToBeClickable(staffIdColumn)).getText();
    }
    
    public String getStaffName() {
        return driver.findElement(staffNameColumn).getText();
    }
    
    public String getStaffEmail() {
        return driver.findElement(staffEmailColumn).getText();
    }
    
    public String getStaffPhone() {
        return driver.findElement(staffPhoneColumn).getText();
    }
    
    public String getStaffStatus() {
        return driver.findElement(staffStatusColumn).getText();
    }
    
    public String getStaffRoles() {
        return driver.findElement(staffRolesColumn).getText();
    }
    
    public String getStaffAddedDate() {
        return driver.findElement(staffAddedDateColumn).getText();
    }
    
    public String getStaffDataByIndex(int rowIndex, int columnIndex) {
        By staffData = By.xpath("//table/tbody/tr[" + rowIndex + "]/td[" + columnIndex + "]");
        return driver.findElement(staffData).getText();
    }
    
    public String getErrorMessage() {
        try {
            return driver.findElement(errorMessage).getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    public String getSuccessMessage() {
        try {
            return driver.findElement(successMessage).getText();
        } catch (Exception e) {
            return "";
        }
    }
    
   
    public StaffData addStaffWithAdminRole() {
        String name = generateUniqueStaffName();
        String email = generateUniqueEmail();
        String password =StaffmoduleObjects.password;
        String phone = generateUniquePhoneNumber();
        
        navigateToStaffsPage();
        clickAddStaffButton();
        fillStaffDetails(name, email, password, phone);
      
        clickCreateStaffButton();
        
        return new StaffData(name, email, password, phone);
    }

    public StaffData editStaffDetails(String currentEmail, String newName, String newEmail, String newPhone) {
      //  navigateToStaffsPage();
        editStaffByEmail(currentEmail);
        clearAndFillName(newName);
        clearAndFillEmail(newEmail);
        clearAndFillPhoneNumber(newPhone);
        clickUpdateStaffButton();
        updateStaff();
        return new StaffData(newName, newEmail, "", newPhone);
    }
    
    
    public StaffData editStaffWithUniqueData(String currentEmail) {
        String newName = generateUniqueStaffName();
        String newEmail = generateUniqueEmail();
        String newPhone = generateUniquePhoneNumber();
      
        
        return editStaffDetails(currentEmail, newName, newEmail, newPhone);
    }
    
   
    public boolean addStaffWithInvalidEmail() {
        String name = generateUniqueStaffName();
        String invalidEmail = generateInvalidEmail();
        String password = StaffmoduleObjects.password;
        String phone = generateUniquePhoneNumber();
        
        navigateToStaffsPage();
        clickAddStaffButton();
        fillStaffDetails(name, invalidEmail, password, phone);
        clickCreateStaffButton();
        
        // Return true if error is displayed (expected behavior)
        return isErrorMessageDisplayed() || isAddStaffFormDisplayed();
    }
    
    
    public boolean verifyStaffExists(String searchTerm) {
        navigateToStaffsPage();
        searchStaff(searchTerm);
        return isStaffCreated();
    }
    
    public StaffData getFirstStaffData() {
        return new StaffData(
            getStaffName(),
            getStaffEmail(),
            "", // password not shown in table
            getStaffPhone()
        );
    }
    
    /**
     * Data class to hold staff information
     */
    public static class StaffData {
        private String name;
        private String email;
        private String password;
        private String phone;
      
        
        public StaffData(String name, String email, String password, String phone) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.phone = phone;
          
        }
        
        // Getters
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getPhone() { return phone; }
      
        
        // Setters
        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
	        public void setPassword(String password) { this.password = password; }
        public void setPhone(String phone) { this.phone = phone; }
        
        
        @Override
        public String toString() {
            return "StaffData{" +
                    "name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\''  +
                    '}';
        }
    }
}
