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
    private String commboardId;
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
    public void testCreateVendor() {
        String uniqueName = TestDataGenerator.getRandomName();
        
        vendorPage.navigateToVendorPage();
        vendorPage.clickAddNewVendor();
        vendorPage.createVendor(
            uniqueName, 
            "Nepal", 
            "Automated vendor creation"
        );
        vendorId = vendorPage.getFirstVendorId();
        Assert.assertNotNull(vendorId, "Vendor ID should not be null");
        Assert.assertFalse(vendorId.trim().isEmpty(), "Vendor ID should not be empty");
        Assert.assertEquals(vendorPage.getFirstVendorName(), uniqueName, "Created vendor should be visible in the table");
    }

    @Test(priority = 2, dependsOnMethods = {"testCreateVendor"})
    public void testCreateAllParts() {
        bikePartsPage.navigateToBikeParts();
        
        // Use generic vendor index 1 for simplicity since table sorts it to top usually, or custom generic flow
        // To be safe, we'll assume the parts creation uses the latest vendor.
        int latestVendorIndex = 1;
        
        batteryId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("battery");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBatteryPart(batteryId, latestVendorIndex);
        verifyCreatedPart("battery", batteryId);
        
        controllerId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("motorcontroller");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBasicPart(controllerId, latestVendorIndex);
        verifyCreatedPart("motorcontroller", controllerId);
        
        vcuId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("vcu");
        bikePartsPage.clickAddPart();
        bikePartsPage.createVCU(vcuId, latestVendorIndex, "1.0.0");
        verifyCreatedPart("vcu", vcuId);
        
        motorId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("motors");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBasicPart(motorId, latestVendorIndex);
        verifyCreatedPart("motors", motorId);
        
        displayId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("display");
        bikePartsPage.clickAddPart();
        bikePartsPage.createDisplay(displayId, latestVendorIndex, "MCU_1.0", "ARM_1.0", "FEX_1.0");
        verifyCreatedPart("display", displayId);
        
        chargerId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("charger");
        bikePartsPage.clickAddPart();
        bikePartsPage.createBasicPart(chargerId, latestVendorIndex);
        verifyCreatedPart("charger", chargerId);
        
        keyfobId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("keyfob");
        bikePartsPage.clickAddPart();
        bikePartsPage.createKeyFob(keyfobId, latestVendorIndex, "BLE_" + keyfobId);
        verifyCreatedPart("keyfob", keyfobId);

        commboardId = TestDataGenerator.getRandomIdentifier();
        bikePartsPage.selectPartType("commboard");
        bikePartsPage.clickAddPart();
        bikePartsPage.createCommboard(commboardId, latestVendorIndex);
        verifyCreatedPart("commboard", commboardId);
    }

    @Test(priority = 3, dependsOnMethods = {"testCreateAllParts"})
    public void testCreateBike() {
        bikePage.navigateToBikes();
        
        String uniqueBikeName = TestDataGenerator.getRandomName();
        bikeVin = TestDataGenerator.getRandomIdentifier();
        
        bikePage.fillAddBikeForm(uniqueBikeName, bikeVin);
        bikePage.clickSubmit();

        Assert.assertTrue(
            bikePage.isBikeCreationSuccessful(bikeVin),
            "Bike creation did not show the created VIN or a success message"
        );
    }

    @Test(priority = 4, dependsOnMethods = {"testCreateBike"})
    public void testCreateCustomer() {
        customerPage.navigateToCustomers();
        customerPage.clickRegisterCustomer();
        
        String uniqueEmail = TestDataGenerator.getSimpleYopmailEmail("customer");
        customerPage.fillEmailStep(uniqueEmail);
        
        String firstName = TestDataGenerator.getRandomName();
        String lastName = TestDataGenerator.getRandomName();
        String phone = TestDataGenerator.getValidNepaliPhoneNumber();
        
        customerPage.fillDetailsStep(firstName, lastName, phone);
        customerPage.clickSubmit();

        Assert.assertTrue(
            customerPage.isCustomerCreationSuccessful(uniqueEmail),
            "Customer registration did not show the created email or a success message"
        );
    }

    private void verifyCreatedPart(String partType, String identifier) {
        bikePartsPage.navigateToBikeParts();
        bikePartsPage.selectPartType(partType);
        Assert.assertTrue(
            bikePartsPage.isPartVisible(identifier),
            "Created " + partType + " part should be visible with identifier: " + identifier
        );
    }

    @AfterClass
    public void tearDown() {
        if(driver != null) {
            driver.quit();
        }
    }
}
