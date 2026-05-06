package com.p2.qa.sprint1.testcases;

import org.openqa.selenium.By;

import org.openqa.selenium.WebDriver;



import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;
import com.p2.qa.sprint1.pageobjects.BikePartsObjects ;


import com.p2.qa.sprint1.pageobjects.VendorPageObject;

import io.qameta.allure.*;

import utils.ConfigReader;
@Listeners(utils.AllureTestListener.class)


public class Vendor_BikeParts_Testcases extends Base {
    private WebDriver driver;
    private VendorPageObject VendorPageObject ;
    private BikePartsObjects bikePartsObjects;
   
    String vendorName = utils.TestDataGenerator.getRandomName();
      String identifier=utils.TestDataGenerator.getRandomIdentifier();
    @BeforeClass
    public void setUp() {
    	  String browser = ConfigReader.get("browser"); 
          driver = initializeBrowserAndOpenApplication();
         // driver.manage().deleteAllCookies();
           Login loginPage = new Login();
          driver = loginPage.loginAs(driver, "admin");
          VendorPageObject = new VendorPageObject(driver);
          bikePartsObjects=new BikePartsObjects(driver);
    }
    

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    public  void create_Vendor() 
    {
    	vendorName = utils.TestDataGenerator.getRandomName();
    	identifier = utils.TestDataGenerator.getRandomIdentifier();
    	
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	VendorPageObject.navigateToVendorPage();
    	VendorPageObject.clickAddNewVendor();
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        VendorPageObject.createVendor(vendorName,ConfigReader.get("country"),ConfigReader.get("remarks"));
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        // DEBUG: Check for any error toasts or messages on the page
        java.util.List<org.openqa.selenium.WebElement> toasts = driver.findElements(By.xpath("//*[contains(@class, 'toast') or contains(@class, 'error') or contains(@class, 'alert')]"));
        if (!toasts.isEmpty()) {
            System.out.println("DEBUG: Found " + toasts.size() + " potential error messages on the page!");
            for (org.openqa.selenium.WebElement toast : toasts) {
                System.out.println("MESSAGE TEXT: " + toast.getText());
            }
        }
    }
    
    
    @Severity(SeverityLevel.CRITICAL)
    @Test(priority = 1, description = "check vendor is created sucessfully")
    public void NT_VBP_001_createVendor()
    
    {   
        create_Vendor();
        Assert.assertTrue(VendorPageObject.getFirstVendorName().equals(vendorName), "Vendor creation failed");
      
    }
 

@Severity(SeverityLevel.CRITICAL)
@Test(priority = 2,description = "go to vndor and edit and check vendor is sucessfully edited")
public void INT_VBP_002_editVendor()
{
	
	VendorPageObject.navigateToVendorPage();
	VendorPageObject.editFirstVendor();
	//since element is same and loc is also same so we can perform same as create vendor
	  VendorPageObject.createVendor(vendorName,ConfigReader.get("country"),ConfigReader.get("remarks"));
	Assert.assertTrue(VendorPageObject.getFirstVendorName().equals(vendorName),"vendor is not editted");

}


@Severity(SeverityLevel.CRITICAL)
@Test(priority = 3,description="go to vendor create a vendor go to bike parts click on add battery \r\n"
		+ "and create a battery with created vendor id and check battery is added in battery page\r\n"
		+ "with proper data")
public void INT_VBP_003_bikepartswithcreatedVendorid()
{
	create_Vendor();
	String createdVendorId = VendorPageObject.getFirstVendorId();
	System.out.println(createdVendorId+"is the created vendor id ");
	bikePartsObjects.navigateToBikeParts();
	bikePartsObjects.selectPartType("BATTERY");
	bikePartsObjects.clickAddPart();
	bikePartsObjects.createBasicPart(identifier, 1);
	 Assert.assertTrue(bikePartsObjects.getFirstPartIdentifier().equals(identifier), "Part creation failed");
	 Assert.assertTrue(bikePartsObjects.getFirstPartId().equals(createdVendorId), "vendor id didnot match"); 

}

@Severity(SeverityLevel.CRITICAL)
@Test(priority = 4,description="go to bike parts click on add battery add a battery go to created battery and edit it with different identifier and  vendor check it is editable ")
public void INT_VBP004_editbattery()
{
	bikePartsObjects.navigateToBikeParts();
	bikePartsObjects.selectPartType("BATTERY");
	bikePartsObjects.clickAddPart();
	bikePartsObjects.createBasicPart(identifier, 1);
	
	String created_identifier=identifier;
	System.out.println("1st created identifier"+ " "+created_identifier);
	bikePartsObjects.editPart();
	String newidentifier=utils.TestDataGenerator.getRandomIdentifier();
	bikePartsObjects.createBasicPart(newidentifier, 2);
	System.out.println("edited identifier"+ " "+newidentifier);
	
	Assert.assertTrue(bikePartsObjects.getFirstPartIdentifier().equals(newidentifier), 
	        "Battery edit failed - identifier should be updated to: " + newidentifier);
	
	
	
}

@Severity(SeverityLevel.CRITICAL)
@Test(priority = 5,description = "go to bike parts click on add motorcontroller and  add a motorcontroller go to created motorcontroller and edit it with different id and  vendor check it is editable ")
public void INT_VBP0015_ChangeMotorController()
{
	bikePartsObjects.navigateToBikeParts();
	bikePartsObjects.selectPartType("motorcontroller");
	bikePartsObjects.clickAddPart();
	bikePartsObjects.createBasicPart(identifier, 1);
	System.out.println("created identfier is"+identifier);
	String newidentifier=utils.TestDataGenerator.getRandomIdentifier();
	bikePartsObjects.editPartforMotor();
	bikePartsObjects.createBasicPart(newidentifier, 2);
	System.out.println("new identifier is"+newidentifier);
	Assert.assertTrue(bikePartsObjects.getFirstPartIdentifier().equals(newidentifier), 
	        "Battery edit failed - identifier should be updated to: " + newidentifier);
	
	
}

@Severity(SeverityLevel.CRITICAL)
@Test(priority = 6, description = "Search for a vendor by name")
public void INT_VBP_006_searchVendor() {
    create_Vendor();
    VendorPageObject.navigateToVendorPage();
    VendorPageObject.searchVendor(vendorName);
    Assert.assertTrue(VendorPageObject.getFirstVendorName().equals(vendorName), "Vendor search failed");
}

@Severity(SeverityLevel.CRITICAL)
@Test(priority = 7, description = "Create a VCU part and verify it")
public void INT_VBP_007_createVCU() {
    create_Vendor();
    String createdVendorId = VendorPageObject.getFirstVendorId();
    bikePartsObjects.navigateToBikeParts();
    bikePartsObjects.selectPartType("vcu");
    bikePartsObjects.clickAddPart();
    bikePartsObjects.createVCU(identifier, 1, "1.0.0");
    Assert.assertTrue(bikePartsObjects.getFirstPartIdentifier().equals(identifier), "VCU creation failed");
    Assert.assertTrue(bikePartsObjects.getFirstPartId().equals(createdVendorId), "VCU vendor id did not match");
}

@Severity(SeverityLevel.CRITICAL)
@Test(priority = 8, description = "Create a KeyFob part and verify it")
public void INT_VBP_008_createKeyFob() {
    create_Vendor();
    String createdVendorId = VendorPageObject.getFirstVendorId();
    bikePartsObjects.navigateToBikeParts();
    bikePartsObjects.selectPartType("keyfob");
    bikePartsObjects.clickAddPart();
    bikePartsObjects.createKeyFob(identifier, 1, "BLE_KEY_01");
    Assert.assertTrue(bikePartsObjects.getFirstPartIdentifier().equals(identifier), "KeyFob creation failed");
    Assert.assertTrue(bikePartsObjects.getFirstPartId().equals(createdVendorId), "KeyFob vendor id did not match");
}

@Severity(SeverityLevel.CRITICAL)
@Test(priority = 9, description = "Create a Display part and verify it")
public void INT_VBP_009_createDisplay() {
    create_Vendor();
    String createdVendorId = VendorPageObject.getFirstVendorId();
    bikePartsObjects.navigateToBikeParts();
    bikePartsObjects.selectPartType("display");
    bikePartsObjects.clickAddPart();
    bikePartsObjects.createDisplay(identifier, 1, "MCU_1.0", "ARM_1.0", "FEX_1.0");
    Assert.assertTrue(bikePartsObjects.getFirstPartIdentifier().equals(identifier), "Display creation failed");
    Assert.assertTrue(bikePartsObjects.getFirstPartId().equals(createdVendorId), "Display vendor id did not match");
}

}

