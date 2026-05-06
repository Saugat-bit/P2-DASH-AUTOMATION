package com.p2.qa.sprint1.testcases;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;
import com.p2.qa.sprint1.pageobjects.BikeObjects;

import utils.TestDataGenerator;

public class BikeRequestRecorder_Testcases extends Base {
    private WebDriver driver;
    private BikeObjects bikePage;

    @BeforeClass
    public void setUp() {
        if (!Boolean.parseBoolean(System.getProperty("request.recorder.confirm", "false"))) {
            throw new SkipException("Request recorder is guarded. Run with -Drequest.recorder.confirm=true.");
        }

        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login loginPage = new Login();
        driver = loginPage.loginAs(driver, "admin");
        bikePage = new BikeObjects(driver);
    }

    @Test(description = "Record network requests made while creating one bike through the UI")
    public void recordBikeCreationRequests() throws InterruptedException {
        installRequestRecorder();

        String bikeName = "Api Probe " + TestDataGenerator.getRandomName();
        String vin = Long.toHexString(System.currentTimeMillis()).toUpperCase();
        vin = vin.substring(vin.length() - 8);

        bikePage.createBikeWithFirstAvailableParts(bikeName, vin);
        printRecordedRequests();
    }

    private void installRequestRecorder() {
        String script =
            "window.__p2Requests = [];"
                + "const normalizeHeaders = (headers) => {"
                + "  try { return headers && typeof headers.forEach === 'function'"
                + "    ? Array.from(headers.entries()) : headers; } catch (e) { return null; }"
                + "};"
                + "const originalFetch = window.fetch;"
                + "window.fetch = async function(input, init) {"
                + "  const url = typeof input === 'string' ? input : input.url;"
                + "  window.__p2Requests.push({ type: 'fetch', url, method: (init && init.method) || 'GET',"
                + "    body: init && init.body ? String(init.body) : null,"
                + "    headers: normalizeHeaders(init && init.headers) });"
                + "  return originalFetch.apply(this, arguments);"
                + "};"
                + "const originalOpen = XMLHttpRequest.prototype.open;"
                + "const originalSend = XMLHttpRequest.prototype.send;"
                + "XMLHttpRequest.prototype.open = function(method, url) {"
                + "  this.__p2Method = method; this.__p2Url = url;"
                + "  return originalOpen.apply(this, arguments);"
                + "};"
                + "XMLHttpRequest.prototype.send = function(body) {"
                + "  window.__p2Requests.push({ type: 'xhr', url: this.__p2Url,"
                + "    method: this.__p2Method, body: body ? String(body) : null });"
                + "  return originalSend.apply(this, arguments);"
                + "};";

        ((JavascriptExecutor) driver).executeScript(script);
    }

    @SuppressWarnings("unchecked")
    private void printRecordedRequests() {
        List<Map<String, Object>> requests = (List<Map<String, Object>>) ((JavascriptExecutor) driver)
            .executeScript("return window.__p2Requests || [];");

        System.out.println("Recorded request count: " + requests.size());
        for (Map<String, Object> request : requests) {
            String url = String.valueOf(request.get("url"));
            String body = String.valueOf(request.get("body"));
            if (isRelevant(url, body)) {
                System.out.println("P2_REQUEST " + request);
            }
        }
    }

    private boolean isRelevant(String url, String body) {
        String haystack = (url + " " + body).toLowerCase();
        return haystack.contains("bike")
            || haystack.contains("vin")
            || haystack.contains("battery")
            || haystack.contains("controller")
            || haystack.contains("vcu")
            || haystack.contains("motor")
            || haystack.contains("charger")
            || haystack.contains("display");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
