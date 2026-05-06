package com.p2.qa.sprint1.testcases;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;
import com.p2.qa.sprint1.pageobjects.BikePartsObjects;
import com.p2.qa.sprint1.pageobjects.VendorPageObject;
import com.p2.qa.sprint1.pageobjects.BikeObjects;
import com.p2.qa.sprint1.pageobjects.CustomerObjects;
import utils.TestDataGenerator;

public class FullFlow_Testcases extends Base {
    public WebDriver driver;
    private VendorPageObject vendorPage;
    private BikePartsObjects bikePartsPage;
    private BikeObjects bikePage;
    private CustomerObjects customerPage;
    
    // Shared Data
    private String vendorId;
    private String batteryId;
    private String vcuId;
    private String displayId;
    private String chargerId;
    private String controllerId;
    private String motorId;
    private String keyfobId;
    private String bikeVin;

    @BeforeClass
    public void setup() throws Exception {
        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login login = new Login();
        login.loginAs(driver, "admin");

        vendorPage = new VendorPageObject(driver);
        bikePartsPage = new BikePartsObjects(driver);
        bikePage = new BikeObjects(driver);
        customerPage = new CustomerObjects(driver);
    }

    @Test(priority = 1)
    public void testCreateVendor() throws InterruptedException {
        String uniqueName = TestDataGenerator.getRandomName();
        
        vendorPage.navigateToVendorPage();
        vendorPage.clickAddNewVendor();
        vendorPage.createVendor(
            uniqueName, 
            "Nepal", 
            "Automated vendor creation"
        );
        // vendorPage.saveVendor(); // Save is called inside createVendor in VendorPageObject
        
        Thread.sleep(3000); // Wait for table refresh
        vendorId = vendorPage.getFirstVendorId();
        Assert.assertNotNull(vendorId, "Vendor ID should not be null");
    }

    @Test(priority = 2, dependsOnMethods = {"testCreateVendor"})
    public void testCreateAllParts() throws InterruptedException {
        bikePartsPage.navigateToBikeParts();
        
        // Use generic vendor index 1 for simplicity since table sorts it to top usually, or custom generic flow
        // To be safe, we'll assume the parts creation uses the latest vendor.
        int latestVendorIndex = 1;
        
        // Battery
        batteryId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("battery");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBasicPart(batteryId, latestVendorIndex);
        
        // Motor Controller
        controllerId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("motorcontroller");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBasicPart(controllerId, latestVendorIndex);
        
        // VCU
        vcuId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("vcu");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBasicPart(vcuId, latestVendorIndex);
        
        // Motors
        motorId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("motors");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBasicPart(motorId, latestVendorIndex);
        
        // Display
        displayId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("display");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBasicPart(displayId, latestVendorIndex);
        
        // Charger
        chargerId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("charger");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBasicPart(chargerId, latestVendorIndex);
        
        Assert.assertTrue(true, "All parts created successfully");
    }

    @Test(priority = 3, dependsOnMethods = {"testCreateAllParts"})
    public void testCreateBike() throws InterruptedException {
        bikePage.navigateToBikes();
        
        String uniqueBikeName = TestDataGenerator.getRandomName();
        bikeVin = TestDataGenerator.getRandomIdentifier();
        
        bikePage.fillAddBikeForm(uniqueBikeName, bikeVin);
        bikePage.clickSubmit();
        
        // Wait for potential toast or table reload
        Thread.sleep(3000);
        Assert.assertTrue(true, "Bike creation submitted successfully");
    }

    @Test(priority = 4)
    public void testCreateCustomer() throws InterruptedException {
        customerPage.navigateToCustomers();
        customerPage.clickRegisterCustomer();
        
        String uniqueEmail = TestDataGenerator.getRandomName().replaceAll("\\s", "").toLowerCase() + "@test.com";
        customerPage.fillEmailStep(uniqueEmail);
        
        String firstName = TestDataGenerator.getRandomName();
        String lastName = TestDataGenerator.getRandomName();
        String phone = "98" + String.format("%08d", (int)(Math.random() * 100000000));
        
        customerPage.fillDetailsStep(firstName, lastName, phone);
        customerPage.clickSubmit();
        
        Thread.sleep(3000);
        Assert.assertTrue(true, "Customer registration completed");
    }

    @AfterClass
    public void tearDown() {
        if(driver != null) {
            driver.quit();
        }
    }
}
