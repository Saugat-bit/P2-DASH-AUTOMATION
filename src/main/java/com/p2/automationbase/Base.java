package com.p2.automationbase;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import utils.ConfigReader;
import org.openqa.selenium.JavascriptExecutor;

public class Base {
    public static WebDriver driver;

    public WebDriver initializeBrowserAndOpenApplication() {
        boolean isGridEnabled = Boolean.parseBoolean(ConfigReader.get("grid.enabled"));
        String browserName = ConfigReader.get("browsersel"); // For grid or local
        String hubHost = ConfigReader.get("grid.hub.host");
        String gridUrlFormat = ConfigReader.get("grid.url.format");

        if (isGridEnabled) {
            driver = setupGridDriver(browserName, hubHost, gridUrlFormat);
        } else {
            driver = setupLocalDriver(browserName);
        }

        driver.manage().window().maximize();

        long implicitWait = Long.parseLong(ConfigReader.get("implicit.wait"));
        long pageLoadTimeout = Long.parseLong(ConfigReader.get("page.load.timeout"));

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        driver.get(ConfigReader.get("base.url"));

        // Zoom out
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.body.style.zoom='50%'");

        return driver;
    }

    // ---------------------- LOCAL DRIVER -----------------------
    private WebDriver setupLocalDriver(String browserName) {

        switch (browserName.toLowerCase()) {
            case "chrome":
                return io.github.bonigarcia.wdm.WebDriverManager.chromedriver().create();

            case "firefox":
                return io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().create();

            case "edge":
                return io.github.bonigarcia.wdm.WebDriverManager.edgedriver().create();

            case "safari":
                return new org.openqa.selenium.safari.SafariDriver();

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserName);
        }
    }

    // ---------------------- GRID DRIVER ------------------------
    private WebDriver setupGridDriver(String browserName, String hubHost, String gridUrlFormat) {
        URL gridUrl;

        try {
            gridUrl = new URL(String.format(gridUrlFormat, hubHost));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Grid URL", e);
        }

        switch (browserName.toLowerCase()) {

            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setCapability("browserName", "chrome");
                return new RemoteWebDriver(gridUrl, chromeOptions);

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setCapability("browserName", "firefox");
                return new RemoteWebDriver(gridUrl, firefoxOptions);

            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.setCapability("browserName", "edge");
                return new RemoteWebDriver(gridUrl, edgeOptions);

            case "safari":
                SafariOptions safariOptions = new SafariOptions();
                safariOptions.setCapability("browserName", "safari");
                return new RemoteWebDriver(gridUrl, safariOptions);

            default:
                throw new IllegalArgumentException("Unsupported browser for Grid: " + browserName);
        }
    }
}
