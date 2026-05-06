package zaptest;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zaproxy.clientapi.core.ClientApiException;

import io.github.bonigarcia.wdm.WebDriverManager;
import utils.ZapUtil1;

public class ZapTest1 extends ZapUtil1 {

	WebDriver driver;
	private static String urlToTest = "https://p2-admin-dash-qa.vercel.app/";
	
	@BeforeMethod
	public void setUp() {
		try {
			// Verify ZAP is running before setting up WebDriver
			if (clientApi == null) {
				Assert.fail("ZAP Client is not initialized. Make sure OWASP ZAP is running on " + 
						zapAddress + ":" + zapPort);
			}
			
			ChromeOptions co = new ChromeOptions();
			co.setAcceptInsecureCerts(true);
			co.setProxy(ZapUtil1.proxy);
			co.addArguments("--ignore-certificate-errors");
			co.addArguments("--ignore-ssl-errors");
			co.addArguments("--disable-web-security");
			co.addArguments("--allow-running-insecure-content");
			co.addArguments("--disable-extensions");
			
			WebDriverManager.chromedriver().setup();
			driver = new ChromeDriver(co);
			
			System.out.println("WebDriver setup completed successfully");
			
		} catch (Exception e) {
			System.err.println("Error during setup: " + e.getMessage());
			e.printStackTrace();
			Assert.fail("Setup failed: " + e.getMessage());
		}
	}
	
	@Test(priority = 1)
	public void passiveScan() {
		try {
			System.out.println("Starting passive scan for: " + urlToTest);
			driver.get(urlToTest);
			
			// Wait for page to load completely
			Thread.sleep(5000);
			
			// Wait for passive scan to complete
			waitTillPassiveScanCompleted();
			
			System.out.println("Passive scan completed successfully");
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Assert.fail("Thread interrupted during passive scan: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Error during passive scan: " + e.getMessage());
			e.printStackTrace();
			Assert.fail("ZAP passive scan failed: " + e.getMessage());
		}
	}
	
	@Test(priority = 2, dependsOnMethods = {"passiveScan"})
	public void testActiveScan() {
		try {
			System.out.println("Starting active scan for: " + urlToTest);
			
			// Add URL to scan tree
			addURLToScanTree(urlToTest);
			
			// Perform active scan
			performActivescan(urlToTest);
			
			System.out.println("Active scan completed successfully");
			
		} catch (ClientApiException e) {
			System.err.println("ClientApi error during active scan: " + e.getMessage());
			e.printStackTrace();
			Assert.fail("Active scan failed: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Unexpected error during active scan: " + e.getMessage());
			e.printStackTrace();
			Assert.fail("Active scan failed: " + e.getMessage());
		}
	}
	
	@AfterMethod
	public void tearDown() {
		try {
			// Wait a bit before generating report
			Thread.sleep(3000);
			
			// Generate ZAP report
			generateZapReport(urlToTest);
			
			// Wait for report generation
			Thread.sleep(3000);
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Thread interrupted during teardown: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Error during teardown: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Always quit the driver
			if (driver != null) {
				try {
					driver.quit();
					System.out.println("WebDriver closed successfully");
				} catch (Exception e) {
					System.err.println("Error closing WebDriver: " + e.getMessage());
				}
			}
		}
	}
}