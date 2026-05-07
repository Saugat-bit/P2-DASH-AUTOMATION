package com.p2.qa.sprint1.pageobjects;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final long REVIEW_PAUSE_MS = Long.getLong("ui.flow.review.pause.ms", 300L);
    private static final long OPTIONAL_WAIT_MS = Long.getLong("ui.flow.optional.wait.ms", 250L);
    private static final long SETTLE_PAUSE_MS = Long.getLong("ui.flow.settle.pause.ms", 250L);
    private static final boolean DIRECT_NAVIGATION = Boolean.parseBoolean(
        System.getProperty("ui.flow.direct.navigation", "true")
    );
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
        fillIfPresent("Password@123", "password", "Password");
        fillIfPresent(customer.phone, "phone", "phone_number", "Phone Number", "Phone");
        selectFirstOptionForLabels("Gender");
        selectDateIfPresent("Date of Birth", "Birth");
        reviewBeforeAction("customer-form-before-submit");
        clickFirstButton("Submit", "Register", "Create", "Save");
        reviewBeforeAction("customer-confirm-before-ok");
        confirmIfPresent("Yes", "Register", "Create", "Add", "Save");
        waitForPageToSettle();
        customer.id = captureCustomerId(customer);
        if (customer.id == null || customer.id.trim().isEmpty()) {
            customer.id = System.getProperty("ui.flow.last.customer.id", "");
        } else {
            System.setProperty("ui.flow.last.customer.id", customer.id);
        }
        return customer;
    }

    private String captureCustomerId(CustomerData customer) {
        waitForPageToSettle();
        reviewBeforeAction("customer-after-register-before-id-capture");

        String id = customerIdFromUrl();
        if (hasText(id)) {
            return id;
        }

        openCustomerList();
        id = findCurrentOrFirstTableId(customer.email);
        if (hasText(id)) {
            return id;
        }

        id = findNumericTableIdInRowContaining(customer.email);
        if (hasText(id)) {
            return id;
        }

        id = firstNumericTextNear("Customer ID", "Customer Id");
        if (hasText(id)) {
            return id;
        }

        reviewBeforeAction("customer-id-lookup-failed");
        return "";
    }

    public void assignFirstAvailableBikeToCustomer(CustomerData customer) {
        String bikeId = getFirstBikeId();
        requireCustomerId(customer, "assign-ownership");
        navigateTo("Bikes", "ownership", "ownerships");
        clickFirstButton("Add Ownership", "Assign Bike", "Create Ownership", "Add");
        fillByExactLabel(customer.id, "Customer ID");
        fillByExactLabel(bikeId, "Bike ID");
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
        if (!clickFirstButtonFast("Create New Payment", "Create Payment")) {
            reviewBeforeAction("payment-create-button-not-found");
            throw new IllegalStateException("Could not open payment creation form");
        }
        waitForPaymentForm();
        fillIfPresentFast(customer.id, "customer_id", "Customer ID");
        selectFirstOptionForLabelsFast("Customer");
        selectFirstOptionForLabelsFast("Bike");
        fillIfPresentFast("25000", "amount", "Amount", "Payment Amount");
        fillIfPresentFast("FLOW-" + System.currentTimeMillis(), "reference", "Reference", "Transaction ID");
        selectFirstOptionForLabelsFast("Payment Method");
        fillIfPresentFast("Automated payment from UI end-to-end flow", "remarks", "Remarks", "Note");
        reviewBeforeAction("payment-form-before-submit");
        clickFirstButton("Submit", "Pay", "Create", "Save");
        reviewBeforeAction("payment-confirm-before-ok");
        confirmIfPresent("Yes", "Pay", "Create", "Add", "Save");
        waitForPageToSettle();
    }

    public void transferOwnership(CustomerData fromCustomer, CustomerData toCustomer) {
        String bikeId = getFirstBikeId();
        requireCustomerId(toCustomer, "transfer-ownership");
        navigateTo("Bikes", "ownership", "ownerships");
        clickFirstButton("Transfer Ownership", "Transfer", "Change Owner");
        fillByExactLabel(toCustomer.id, "New Customer ID");
        fillByExactLabel(bikeId, "Bike ID");
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
        if (DIRECT_NAVIGATION && navigateDirectly(menuText, hrefOrTextHints)) {
            return;
        }

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

    private boolean navigateDirectly(String menuText, String... hrefOrTextHints) {
        String route = directRouteFor(menuText, hrefOrTextHints);
        if (route == null) {
            return false;
        }

        String baseUrl = ConfigReader.get("base.url");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return false;
        }

        String normalizedBase = baseUrl.replaceAll("/+$", "");
        String targetUrl = normalizedBase + route;
        if (!driver.getCurrentUrl().startsWith(targetUrl)) {
            driver.get(targetUrl);
        }
        waitForPageToSettle();
        return true;
    }

    private String directRouteFor(String menuText, String... hrefOrTextHints) {
        String combined = (menuText + " " + String.join(" ", hrefOrTextHints)).toLowerCase();
        if (combined.contains("ownership")) {
            return "/ownership";
        }
        if (combined.contains("payment")) {
            return "/payments";
        }
        if (combined.contains("customer")) {
            return "/customer";
        }
        if (combined.contains("staff")) {
            return "/staffs";
        }
        if (combined.contains("bike")) {
            return "/bike";
        }
        return null;
    }

    private boolean clickFirstButton(String... texts) {
        for (String text : texts) {
            By button = By.xpath("//button[contains(normalize-space(.), '" + text + "')]"
                + " | //a[contains(normalize-space(.), '" + text + "')]");
            if (clickIfPresent(button, 3)) {
                return true;
            }
        }
        return false;
    }

    private boolean clickFirstButtonFast(String... texts) {
        for (String text : texts) {
            By button = By.xpath("//button[contains(normalize-space(.), '" + text + "')]"
                + " | //a[contains(normalize-space(.), '" + text + "')]");
            if (clickIfPresent(button, 1)) {
                return true;
            }
        }
        return false;
    }

    private void waitForPaymentForm() {
        By formLocator = By.xpath("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'amount')]"
            + " | //input[contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'amount')]"
            + " | //label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'customer')]/following::button[1]");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(4))
                .until(ExpectedConditions.visibilityOfElementLocated(formLocator));
        } catch (Exception e) {
            reviewBeforeAction("payment-form-not-visible");
            throw new IllegalStateException("Payment form did not open", e);
        }
    }

    private void fillIfPresentFast(String value, String... hints) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        for (String hint : hints) {
            WebElement input = findInput(hint, Duration.ofMillis(OPTIONAL_WAIT_MS));
            if (input != null) {
                fillInput(input, value);
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
            WebElement input = findInput(hint, Duration.ofMillis(OPTIONAL_WAIT_MS));
            if (input != null) {
                fillInput(input, value);
                return;
            }
        }
    }

    private void fillByExactLabel(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        String normalizedLabel = label.toLowerCase();
        By locator = By.xpath("//label[translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='"
            + normalizedLabel + "' or translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='"
            + normalizedLabel + "*']/following::input[1]");
        try {
            WebElement input = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(locator));
            fillInput(input, value);
        } catch (Exception e) {
            fillIfPresent(value, label);
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

    private void requireCustomerId(CustomerData customer, String stepName) {
        if (customer.id != null && !customer.id.trim().isEmpty()) {
            return;
        }
        reviewBeforeAction(stepName + "-missing-customer-id");
        throw new IllegalStateException("Could not capture customer ID for " + customer.email);
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
            WebElement search = findInput("Search", 3);
            if (search != null) {
                fillInput(search, searchValue);
                search.sendKeys(Keys.ENTER);
                waitForPageToSettle();
            }
        }

        String id = findNumericTableIdInRowContaining(searchValue);
        if (hasText(id)) {
            return id;
        }

        if (searchValue == null || searchValue.trim().isEmpty()) {
            id = firstTableCellText(1);
            return id == null ? "" : id.trim();
        }

        return "";
    }

    private void openCustomerList() {
        navigateTo("Customers", "customers", "customer");
        if (!driver.getCurrentUrl().toLowerCase().matches(".*/customers?/?$")) {
            String baseUrl = ConfigReader.get("base.url");
            if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                String normalizedBase = baseUrl.replaceAll("/+$", "");
                driver.get(normalizedBase + "/customer");
                waitForPageToSettle();
            }
        }
        clickIfPresent(
            By.xpath("//*[self::button or self::a or @role='tab' or self::div or self::span]"
                + "[normalize-space()='Customer Lists' or normalize-space()='Customer List']"),
            5
        );
        waitForPageToSettle();
    }

    private String customerIdFromUrl() {
        String url = driver.getCurrentUrl();
        if (url == null) {
            return "";
        }

        Matcher pathMatcher = Pattern.compile("(?i)/(?:customer|customers)/(\\d+)(?:\\b|/|\\?|#)").matcher(url);
        if (pathMatcher.find()) {
            return pathMatcher.group(1);
        }

        Matcher queryMatcher = Pattern.compile("(?i)(?:\\?|&)(?:id|customerId|customer_id)=(\\d+)").matcher(url);
        return queryMatcher.find() ? queryMatcher.group(1) : "";
    }

    private String findNumericTableIdInRowContaining(String value) {
        if (!hasText(value)) {
            return "";
        }

        String lower = value.toLowerCase();
        By rows = By.xpath("//table//tbody//tr[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"
            + lower + "')]");
        try {
            java.util.List<WebElement> matchingRows = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(rows));
            for (WebElement row : matchingRows) {
                for (WebElement cell : row.findElements(By.xpath("./td"))) {
                    String text = cell.getText();
                    if (text != null && text.trim().matches("[0-9]+")) {
                        return text.trim();
                    }
                }
            }
        } catch (Exception e) {
            return "";
        }

        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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
        return findInput(hint, Duration.ofSeconds(timeoutSeconds));
    }

    private WebElement findInput(String hint, Duration timeout) {
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
            return new WebDriverWait(driver, timeout)
                .until(driver -> firstDisplayed(driver.findElements(locator)));
        } catch (Exception e) {
            return null;
        }
    }

    private void fillInput(WebElement input, String value) {
        scrollTo(input);
        try {
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(value);
        } catch (Exception ignore) {
        }
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].value = arguments[1];"
                + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));"
                + "arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));",
            input,
            value
        );
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

    private void selectFirstOptionForLabelsFast(String... labels) {
        for (String label : labels) {
            WebElement select = findNativeSelect(label);
            if (select != null) {
                Select nativeSelect = new Select(select);
                if (nativeSelect.getOptions().size() > 1) {
                    nativeSelect.selectByIndex(1);
                    return;
                }
            }

            WebElement button = findSelectButton(label, Duration.ofMillis(OPTIONAL_WAIT_MS));
            if (button != null && selectRadixOption(button, Duration.ofSeconds(1))) {
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
        return findSelectButton(label, Duration.ofMillis(OPTIONAL_WAIT_MS));
    }

    private WebElement findSelectButton(String label, Duration timeout) {
        try {
            String lower = label.toLowerCase();
            By locator = By.xpath("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), '"
                + lower + "')]/following::button[1]");
            return new WebDriverWait(driver, timeout)
                .until(driver -> firstDisplayed(driver.findElements(locator)));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean selectRadixOption(WebElement button) {
        return selectRadixOption(button, Duration.ofSeconds(2));
    }

    private boolean selectRadixOption(WebElement button, Duration timeout) {
        try {
            scrollTo(button);
            clickElement(button);
            WebElement option = new WebDriverWait(driver, timeout)
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
            element.click();
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (Exception ignored) {
                new Actions(driver).moveToElement(element).click().perform();
            }
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
            if (SETTLE_PAUSE_MS > 0) {
                Thread.sleep(SETTLE_PAUSE_MS);
            }
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

    public static class CustomerData {
        public final String firstName;
        public final String lastName;
        public final String email;
        public final String phone;
        public String id;

        private CustomerData(String label) {
            this.firstName = label + TestDataGenerator.getRandomName();
            this.lastName = "Flow";
            this.email = TestDataGenerator.getSimpleYopmailEmail("customer");
            this.phone = TestDataGenerator.getValidNepaliPhoneNumber();
        }

        public static CustomerData random(String label) {
            return new CustomerData(label);
        }
    }
}
