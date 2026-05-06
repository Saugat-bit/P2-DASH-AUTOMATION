package com.p2.qa.sprint1.testcases;

import org.openqa.selenium.WebDriver;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;

import com.p2.qa.sprint1.pageobjects.StaffmoduleObjects;
import com.p2.qa.sprint1.pageobjects.StaffmoduleObjects.StaffData;

import io.qameta.allure.*;

import utils.ConfigReader;
@Listeners(utils.AllureTestListener.class)


public class StaffModuleTestcases extends 	Base {
    private WebDriver driver;
    private StaffmoduleObjects staffPage;
    
    @BeforeClass
    public void setUp() {
    
          driver = initializeBrowserAndOpenApplication();
          driver.manage().deleteAllCookies();

          Login loginPage = new Login();
          driver = loginPage.loginAs(driver, "admin");
          staffPage = new StaffmoduleObjects(driver);
    }
    

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    @Severity(SeverityLevel.CRITICAL)
    @Test(priority = 1, description = "Login with valid credentials")
    public void tc001_loginWithValidCredentials() {
       
        Assert.assertTrue(driver.getCurrentUrl().contains("dash") || 
                         driver.getCurrentUrl().contains("p2"), 
                         "Login was not successful");
    }
    
    @Test(priority = 2, description = "Login with valid credentials and go to staffs page, add new staff with valid data")
    public void tc004_addStaffWithValidData() {
        StaffData staffData = staffPage.addStaffWithAdminRole();
        Assert.assertTrue(staffPage.isStaffCreated(), "Staff was not created successfully");
        
        // Verify staff details in the table
        StaffData createdStaff = staffPage.getFirstStaffData();
        Assert.assertEquals(createdStaff.getName(), staffData.getName(), "Staff name doesn't match");
        Assert.assertEquals(createdStaff.getEmail(), staffData.getEmail(), "Staff email doesn't match");
        Assert.assertEquals(createdStaff.getPhone(), staffData.getPhone(), "Staff phone doesn't match");
      
        System.out.println("Staff created successfully: " + staffData.toString());
    }
    
 
    
   
    
    @Test(priority = 4, description = "Login with valid credentials and go to Staffs Module add a staff and , edit same staff for Admin role")
    public void tc014_editStaffDetails() {
        // First create a staff
        StaffData originalStaff = staffPage.addStaffWithAdminRole();
        System.out.println("Original staff created: " + originalStaff.toString());
        try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
        StaffData updatedStaff = staffPage.editStaffWithUniqueData(originalStaff.getEmail());
        
        // Verify updated details
        StaffData editedStaff = staffPage.getFirstStaffData();
        Assert.assertEquals(editedStaff.getName(), updatedStaff.getName(), "Staff name was not updated");
        Assert.assertEquals(editedStaff.getEmail(), updatedStaff.getEmail(), "Staff email was not updated");
        Assert.assertEquals(editedStaff.getPhone(), updatedStaff.getPhone(), "Staff phone was not updated");
        
        System.out.println("Staff updated successfully: " + updatedStaff.toString());
    }
 
    
   
    

    
    @Test(priority = 5, description = "Edit staff multiple times with different data")
    public void tc018_editStaffMultipleTimes() {
        // Create initial staff
        StaffData originalStaff = staffPage.addStaffWithAdminRole();
        System.out.println("Original staff: " + originalStaff.toString());
        
        // First edit
        StaffData firstEdit = staffPage.editStaffWithUniqueData(originalStaff.getEmail());
        StaffData afterFirstEdit = staffPage.getFirstStaffData();
        Assert.assertEquals(afterFirstEdit.getName(), firstEdit.getName(), "First edit failed");
        System.out.println("First edit completed: " + firstEdit.toString());
        
        // Second edit
        StaffData secondEdit = staffPage.editStaffWithUniqueData(firstEdit.getEmail());
        StaffData afterSecondEdit = staffPage.getFirstStaffData();
        Assert.assertEquals(afterSecondEdit.getName(), secondEdit.getName(), "Second edit failed");
        System.out.println("Second edit completed: " + secondEdit.toString());
        
        // Verify final state
        Assert.assertNotEquals(afterSecondEdit.getName(), originalStaff.getName(), 
                              "Final name should be different from original");
        Assert.assertNotEquals(afterSecondEdit.getEmail(), originalStaff.getEmail(), 
                              "Final email should be different from original");
        
        System.out.println("Multiple edit test completed successfully");
    }

    @Test(priority = 6, description = "Attempt to add a staff with an invalid email")
    public void tc019_addStaffWithInvalidEmail() {
        boolean isErrorHandled = staffPage.addStaffWithInvalidEmail();
        Assert.assertTrue(isErrorHandled, "System should show error or remain on add staff form for invalid email");
        System.out.println("Add staff with invalid email handled correctly.");
    }
    
    @Test(priority = 7, description = "Verify searching a staff member by name")
    public void tc020_verifyStaffSearchByName() {
        StaffData newStaff = staffPage.addStaffWithAdminRole();
        boolean isFound = staffPage.verifyStaffExists(newStaff.getName());
        Assert.assertTrue(isFound, "Staff member should be found when searching by name: " + newStaff.getName());
        System.out.println("Staff search by name successful for: " + newStaff.getName());
    }

    @Test(priority = 8, description = "Verify searching a staff member by email")
    public void tc021_verifyStaffSearchByEmail() {
        StaffData newStaff = staffPage.addStaffWithAdminRole();
        boolean isFound = staffPage.verifyStaffExists(newStaff.getEmail());
        Assert.assertTrue(isFound, "Staff member should be found when searching by email: " + newStaff.getEmail());
        System.out.println("Staff search by email successful for: " + newStaff.getEmail());
    }

    @Test(priority = 9, description = "Verify cancel button functionality on add staff form")
    public void tc022_cancelAddStaff() {
        staffPage.navigateToStaffsPage();
        staffPage.clickAddStaffButton();
        Assert.assertTrue(staffPage.isAddStaffFormDisplayed(), "Add staff form should be displayed");
        
        staffPage.clearAndFillName("Cancel Test User");
        staffPage.clickCancelButton();
        
        Assert.assertTrue(staffPage.isStaffsPageDisplayed(), "Should return to staffs page after cancelling");
        System.out.println("Cancel add staff operation verified.");
    }
}
