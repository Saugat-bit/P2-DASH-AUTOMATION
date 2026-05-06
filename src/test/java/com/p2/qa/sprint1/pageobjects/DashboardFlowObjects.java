package com.p2.qa.sprint1.pageobjects;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.commons.io.FileUtils;

import utils.ConfigReader;
import utils.TestDataGenerator;

public class DashboardFlowObjects {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public DashboardFlowObjects(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public CustomerData createCustomer(String label) {
        CustomerData customer = CustomerData.random(label);
        navigateTo("Customers", "customer", "customers");
        clickFirstButton("Register New Customer", "Add Customer", "Create Customer", "Register Customer");
        fillFirstVisibleInput(customer.email, "email", "Enter email", "Your email");
        clickFirstButton("Continue", "Next");
        fillIfPresent(customer.firstName, "firstName", "first_name", "First Name", "First");
        fillIfPresent(customer.lastName, "lastName", "last_name", "Last Name", "Last");
        fillIfPresent(customer.phone, "phone", "phone_number", "Phone Number", "Phone");
        selectFirstOptionForLabels("Gender");
        selectDateIfPresent("Date of Birth", "Birth");
        reviewBeforeAction("customer-form-before-submit");
        clickFirstButton("Submit", "Register", "Create", "Save");
        reviewBeforeAction("customer-confirm-before-ok");
        confirmIfPresent("Yes", "Register", "Create", "Add", "Save");
        waitForPageToSettle();
        customer.id = findCurrentOrFirstTableId(customer.email);
        return customer;
    }

    public void assignFirstAvailableBikeToCustomer(CustomerData customer) {
        String bikeId = getFirstBikeId();
        navigateTo("Bikes", "ownership", "ownerships");
        clickFirstButton("Add Ownership", "Assign Bike", "Create Ownership", "Add");
        fillIfPresent(customer.id, "customer_id", "Customer ID", "Customer");
        fillIfPresent(bikeId, "bike_id", "Bike ID", "Bike");
        selectDateIfPresent("Purchase Date", "Purchased Date", "Assigned Date", "Start Date");
        fillIfPresent("Initial automated ownership assignment", "remarks", "Remarks", "Note");
        reviewBeforeAction("ownership-form-before-submit");
        clickFirstButton("Submit", "Create", "Assign", "Save");
        reviewBeforeAction("ownership-confirm-before-ok");
        confirmIfPresent("Yes", "Assign", "Create", "Add", "Save");
        waitForPageToSettle();
    }

    public void makePaymentForCustomer(CustomerData customer) {
        navigateTo("Payments", "payments", "payment");
        clickFirstButton("Add Payment", "Make Payment", "Create Payment", "Add");
        fillIfPresent(customer.id, "customer_id", "Customer ID", "Customer");
        selectFirstOptionForLabels("Customer", "Owner");
        selectFirstOptionForLabels("Bike", "Ownership", "VIN");
        fillIfPresent("25000", "amount", "Amount", "Payment Amount");
        fillIfPresent("FLOW-" + System.currentTimeMillis(), "reference", "Reference", "Transaction ID");
        selectFirstOptionForLabels("Payment Method", "Method", "Payment Type");
        fillIfPresent("Automated payment from UI end-to-end flow", "remarks", "Remarks", "Note");
        reviewBeforeAction("payment-form-before-submit");
        clickFirstButton("Submit", "Pay", "Create", "Save");
        reviewBeforeAction("payment-confirm-before-ok");
        confirmIfPresent("Yes", "Pay", "Create", "Add", "Save");
        waitForPageToSettle();
    }

    public void transferOwnership(CustomerData fromCustomer, CustomerData toCustomer) {
        String bikeId = getFirstBikeId();
        navigateTo("Bikes", "ownership", "ownerships");
        clickFirstButton("Transfer Ownership", "Transfer", "Change Owner");
        fillIfPresent(fromCustomer.id, "current_customer_id", "from_customer_id", "Current Owner", "From Customer");
        fillIfPresent(toCustomer.id, "new_customer_id", "to_customer_id", "New Owner", "To Customer");
        fillIfPresent(bikeId, "bike_id", "Bike ID", "Bike");
        selectFirstOptionForLabels("Current Owner", "From Customer", "From Owner", "Customer");
        selectFirstOptionForLabels("New Owner", "To Customer", "To Owner", "Customer");
        selectFirstOptionForLabels("Bike", "VIN", "Ownership");
        selectDateIfPresent("Transfer Date", "Ownership Date", "Date");
        fillIfPresent("Automated ownership transfer to " + toCustomer.email, "remarks", "Remarks", "Note");
        reviewBeforeAction("transfer-form-before-submit");
        clickFirstButton("Submit", "Transfer", "Save");
        reviewBeforeAction("transfer-confirm-before-ok");
        confirmIfPresent("Yes", "Transfer", "Update", "Save");
        waitForPageToSettle();
    }

    public void navigateTo(String menuText, String... hrefOrTextHints) {
        for (String hint : hrefOrTextHints) {
            if (clickIfPresent(By.xpath("//a[contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"
                + hint.toLowerCase() + "')]"), 2)) {
                waitForPageToSettle();
                return;
            }
        }

        clickIfPresent(By.xpath("//*[self::span or self::div][normalize-space()='" + menuText + "']"), 3);

        for (String hint : hrefOrTextHints) {
            if (clickIfPresent(By.xpath("//a[contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"
                + hint.toLowerCase() + "') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"
                + hint.toLowerCase() + "')]"), 5)) {
                waitForPageToSettle();
                return;
            }
        }
    }

    private void clickFirstButton(String... texts) {
        for (String text : texts) {
            By button = By.xpath("//button[contains(normalize-space(.), '" + text + "')]"
                + " | //a[contains(normalize-space(.), '" + text + "')]");
            if (clickIfPresent(button, 3)) {
                return;
            }
        }
    }

    private void confirmIfPresent(String... words) {
        for (String word : words) {
            By button = By.xpath("//button[contains(normalize-space(.), '" + word + "')]");
            if (clickIfPresent(button, 2)) {
                waitForPageToSettle();
                return;
            }
        }
    }

    private void fillFirstVisibleInput(String value, String... hints) {
        for (String hint : hints) {
            WebElement input = findInput(hint, 3);
            if (input != null) {
                fillInput(input, value);
                return;
            }
        }
    }

    private void fillIfPresent(String value, String... hints) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        for (String hint : hints) {
            WebElement input = findInput(hint, 1);
            if (input != null) {
                fillInput(input, value);
                return;
            }
        }
    }

    private String getFirstBikeId() {
        String configuredBikeId = ConfigReader.get("ui.flow.bike.id");
        if (configuredBikeId != null && !configuredBikeId.trim().isEmpty()) {
            return configuredBikeId.trim();
        }

        navigateTo("Bikes", "bikes", "bike");
        String id = firstTableCellText(1);
        if (id == null || id.trim().isEmpty()) {
            id = firstNumericTextNear("Bike ID", "ID", "VIN");
        }
        if (id == null || id.trim().isEmpty()) {
            reviewBeforeAction("bike-list-before-id-lookup-failed");
            return "1";
        }
        return id.trim();
    }

    private String firstNumericTextNear(String... hints) {
        for (String hint : hints) {
            String lower = hint.toLowerCase();
            By locator = By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"
                + lower + "')]/following::*[normalize-space()][position() <= 20]");
            String value = firstNumericText(locator);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return firstNumericText(By.xpath("//table//tbody//tr[1]//td[normalize-space()]"));
    }

    private String firstNumericText(By locator) {
        try {
            java.util.List<WebElement> elements = new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
            for (WebElement element : elements) {
                String text = element.getText();
                if (text != null && text.trim().matches("[0-9]+")) {
                    return text.trim();
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private String findCurrentOrFirstTableId(String searchValue) {
        if (searchValue != null && !searchValue.trim().isEmpty()) {
            WebElement search = findInput("Search", 1);
            if (search != null) {
                fillInput(search, searchValue);
                search.sendKeys(Keys.ENTER);
                waitForPageToSettle();
            }
        }

        String id = firstTableCellText(1);
        return id == null ? "" : id.trim();
    }

    private String firstTableCellText(int columnIndex) {
        try {
            WebElement cell = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//table/tbody/tr[1]/td[" + columnIndex + "]")
                ));
            return cell.getText();
        } catch (Exception e) {
            return "";
        }
    }

    private WebElement findInput(String hint, int timeoutSeconds) {
        String lower = hint.toLowerCase();
        By locator = By.xpath("//input[contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + lower + "')"
            + " or contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + lower + "')]"
            + " | //label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + lower
            + "')]/following::input[1]"
            + " | //textarea[contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + lower + "')"
            + " or contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + lower + "')]"
            + " | //label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + lower
            + "')]/following::textarea[1]");
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(driver -> firstDisplayed(driver.findElements(locator)));
        } catch (Exception e) {
            return null;
        }
    }

    private void fillInput(WebElement input, String value) {
        scrollTo(input);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(value);
    }

    private void selectFirstOptionForLabels(String... labels) {
        for (String label : labels) {
            WebElement select = findNativeSelect(label);
            if (select != null) {
                Select nativeSelect = new Select(select);
                if (nativeSelect.getOptions().size() > 1) {
                    nativeSelect.selectByIndex(1);
                    return;
                }
            }

            WebElement button = findSelectButton(label);
            if (button != null && selectRadixOption(button)) {
                return;
            }
        }
    }

    private WebElement findNativeSelect(String label) {
        try {
            String lower = label.toLowerCase();
            By locator = By.xpath("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"
                + lower + "')]/following::select[1]"
                + " | //select[contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '" + lower + "')]");
            return firstDisplayed(driver.findElements(locator));
        } catch (Exception e) {
            return null;
        }
    }

    private WebElement findSelectButton(String label) {
        try {
            String lower = label.toLowerCase();
            By locator = By.xpath("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"
                + lower + "')]/following::button[1]");
            return new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(driver -> firstDisplayed(driver.findElements(locator)));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean selectRadixOption(WebElement button) {
        try {
            scrollTo(button);
            clickElement(button);
            WebElement option = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(driver -> firstDisplayed(driver.findElements(
                    By.xpath("//*[@role='listbox' and @data-state='open']//*[@role='option' and not(@aria-disabled='true')]")
                )));
            clickElement(option);
            waitForPageToSettle();
            return true;
        } catch (Exception e) {
            try {
                new Actions(driver).sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ENTER).perform();
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    private void selectDateIfPresent(String... labels) {
        for (String label : labels) {
            WebElement button = findSelectButton(label);
            if (button == null) {
                continue;
            }
            clickElement(button);
            By date = By.xpath("(//button[normalize-space()='4' and not(@disabled)])[last()]");
            if (clickIfPresent(date, 3)) {
                return;
            }
        }
    }

    private boolean clickIfPresent(By locator, int timeoutSeconds) {
        try {
            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.elementToBeClickable(locator));
            clickElement(element);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void clickElement(WebElement element) {
        scrollTo(element);
        try {
            new Actions(driver).moveToElement(element).click().perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private WebElement firstDisplayed(java.util.List<WebElement> elements) {
        for (WebElement element : elements) {
            if (element.isDisplayed() && element.isEnabled()) {
                return element;
            }
        }
        return null;
    }

    private void scrollTo(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    private void waitForPageToSettle() {
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void reviewBeforeAction(String name) {
        try {
            File dir = new File("screenshots/flow-review");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String safeName = name.replaceAll("[^A-Za-z0-9._-]", "_");
            long timestamp = System.currentTimeMillis();
            File image = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(image, new File(dir, timestamp + "_" + safeName + ".png"));
            FileUtils.writeStringToFile(
                new File(dir, timestamp + "_" + safeName + ".html"),
                driver.getPageSource(),
                "UTF-8"
            );
            Thread.sleep(1200);
        } catch (IOException e) {
            throw new RuntimeException("Could not save review screenshot for " + name, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during review pause for " + name, e);
        }
    }

    public static class CustomerData {
        public final String firstName;
        public final String lastName;
        public final String email;
        public final String phone;
        public String id;

        private CustomerData(String label) {
            this.firstName = label + TestDataGenerator.getRandomName();
            this.lastName = "Flow";
            this.email = TestDataGenerator.getSimpleYopmailEmail(label);
            this.phone = TestDataGenerator.getRandomPhoneNumber();
        }

        public static CustomerData random(String label) {
            return new CustomerData(label);
        }
    }
}
