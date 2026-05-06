package com.p2.qa.sprint1.testcases;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;
import com.p2.qa.sprint1.pageobjects.DashboardFlowObjects;
import com.p2.qa.sprint1.pageobjects.DashboardFlowObjects.CustomerData;
import com.p2.qa.sprint1.pageobjects.StaffmoduleObjects;
import com.p2.qa.sprint1.pageobjects.StaffmoduleObjects.StaffData;

public class DashboardEndToEndFlow_Testcases extends Base {
    private WebDriver driver;
    private DashboardFlowObjects flow;
    private StaffmoduleObjects staffPage;

    @BeforeClass
    public void setUp() {
        if (!Boolean.parseBoolean(System.getProperty("ui.flow.confirm", "false"))) {
            throw new SkipException("UI end-to-end flow is guarded. Run with -Dui.flow.confirm=true.");
        }

        driver = initializeBrowserAndOpenApplication();
        driver.manage().deleteAllCookies();

        Login loginPage = new Login();
        driver = loginPage.loginAs(driver, "admin");

        flow = new DashboardFlowObjects(driver);
        staffPage = new StaffmoduleObjects(driver);
    }

    @Test(description = "UI flow: create customers, assign bike, payment, transfer ownership, create staff")
    public void createCustomerBikePaymentOwnershipStaffFlow() {
        CustomerData firstCustomer = flow.createCustomer("customer");
        Assert.assertTrue(firstCustomer.email.endsWith("@yopmail.com"), "Customer email must use yopmail.com");

        CustomerData transferCustomer = flow.createCustomer("transfer");
        Assert.assertTrue(transferCustomer.email.endsWith("@yopmail.com"), "Transfer customer email must use yopmail.com");

        flow.assignFirstAvailableBikeToCustomer(firstCustomer);
        flow.makePaymentForCustomer(firstCustomer);
        flow.transferOwnership(firstCustomer, transferCustomer);

        StaffData staff = staffPage.addStaffWithAdminRole();
        Assert.assertTrue(staff.getEmail().endsWith("@yopmail.com"), "Staff email must use yopmail.com");
        Assert.assertTrue(staffPage.isStaffCreated(), "Staff was not created successfully");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
