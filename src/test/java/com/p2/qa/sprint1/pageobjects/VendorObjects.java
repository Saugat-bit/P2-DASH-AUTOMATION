package com.p2.qa.sprint1.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.p2.automationbase.Login;

public class VendorObjects extends Login{
	    private WebDriver driver;
	    
	    
	    @BeforeMethod
	    public void setUp() {
	      //  driver = initializeBrowserAndOpenApplication();
	     //   driver = loginAs("admin");
	       
	    }
	    
	    @Test
	    	public void VendorobjectsAndBikePartsObjects()
	    {
	    	driver.findElement(By.xpath("//a[@href='/vendor']")).click();//this navigates to vendor page
	    	driver.findElement(By.xpath("//button[.='Add new vendor']")).click();//this nagivates to Add new vaendor page
	    	//vandor page starts from here
	    	driver.findElement(By.xpath("//input[@name='vendor_name']"));//this add vendor name andit should be unique it is a text field and it accepts alphabets only and it should be unique each time we create vendor
	    	driver.findElement(By.xpath("//input[@name='country']"));//this is contry textfield where we enter contry name of vandor to create vendor
	    	driver.findElement(By.xpath("//textarea[@name='remarks']"));//this is remarks text field and this is optional for vendor
	    	driver.findElement(By.xpath("//button[.='Save']")).click();//this is button to create a vendor
	    //vendor  table and details of new created vendor which is placed on first row of  vendor table
	    	driver.findElement(By.xpath("//table/tbody"));//it goes to vendor table where all created vendors will be kept
	    	driver.findElement(By.xpath("//table/tbody/tr[1]"));//this is a first index row where new created vendor will be kept with information and tr[1] for new one similary based on tr index all vendor details were kept in vendor page
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[1]"));//this is the  vendor id of vendor of tr[1] and ths should be unique
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[2]"));//this is the vendor name of tr[1] row  and it should be unique
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[3]"));//this is the vendor country of tr[1] 
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[4]"));//this is parts name and in this plce bike parts will be their and from the bike parts module we have to create bike part and assign to vendor name so after that only this section will be apped based on vendor id so this might require in next module test not in this module
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[5]"));//this is the remarks of vendor of tr[1]
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[6]")).click();//this is the edit section where we can edit vendor of tr[1]
	    	//so there might be many vendor lists based on aaded new one will be in the first row
	    	driver.findElement(By.xpath("//input")).sendKeys("vandorname");//this is the search txt field where based on vendor name it will search and  keep vendor information on 1st row
	    	
	    //	if we click on edit section it goes to Edit vendor page and it will show vendorname of selected vendor,country and remarks 	which is previusly filled with the created in formation so we have to clear it and the sendkeys
	    	
	    	driver.findElement(By.xpath("//input[@name='vendor_name']"));
	    	driver.findElement(By.xpath("//input[@name='country']"));
	    	driver.findElement(By.xpath("//textarea[@name='remarks']"));
	    	driver.findElement(By.xpath("//button[.='Save']")).click();//these are the inputs which is same as creating vendor and loc of element is also sme but here we have to clear existng name and then add new one
	    	
//BIKE PARTS OBJECTS
	    	driver.findElement(By.xpath("//a[@href='/bikeParts']")).click();//this nagivatesto bike parts page 
	  /*inside bike parts we have different parts which we can create 
	   * 1.battery
	   * 2.motors
	   * 3.motorcontroller
	   * 4.vcu
	   * 5.keyfob
	   * 6.charger
	   * 7.commboard
	   * 8.Display
	   */
	    	
	//1.Battery
	    
	    	driver.findElement(By.xpath("//button[.='Battery']")).click();//this nagivates to battery page
	    	driver.findElement(By.xpath("(//button[1])[4]")).click();//this nagivates to add battery page
	    	driver.findElement(By.xpath("//input[@name='identifier']")).sendKeys("unique id");//inside add battery page their is some filed to fill 1st one is identifier and this should be unique
	    	driver.findElement(By.xpath("(//span[.='Pick a date'])[1]")).click();//this is 2nd filed to fill manufactured date which shoud be erlier than purchased date and this is calender if we click once it will onen calander their
	    	driver.findElement(By.xpath("//button[.='5']")).click();//this is date 5 of current month so we should choose carefully and in this calander it is not clcikable of upcomming days
	    	WebElement dropdown = driver.findElement(By.xpath("//select"));//this is vendor dropdown in this page their is only one sekect class now from this thier will be dropdown of vendors and in dropdown new created vendor will be in 1st index
	    	
	    	Select sl=new Select(dropdown);
	    	sl.selectByIndex(1);//this will selects the 1st index in it smilarly based on index we can select vendors 
	    	
	    	driver.findElement(By.xpath("(//span[.='Pick a date'])[2]")).click();//this is 2nd field to fill purchase date which shoud be later than manufactered date and this is calender if we click once it will onen calander their
	    	driver.findElement(By.xpath("//button[.='15']")).click();//this is date 15 of current month so we should choose carefully and in this calander it is not clcikable of upcomming days
	  
	    	driver.findElement(By.xpath("//button[.='Save']")).click();//this will create new battery and it will be in battery page
	    	
	    	
	    	//Battery  table and details of new created battery which is placed on first row of  Battery  table
	    	driver.findElement(By.xpath("//table/tbody"));//it goes to vendor table where all created vendors will be kept
	    	driver.findElement(By.xpath("//table/tbody/tr[1]"));//this is a first index row where new created battery will be kept with information and tr[1] for new one similary based on tr index all battery details were kept in battery page
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[1]"));//this is the  battery id of added battery of tr[1] and ths should be unique
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[2]"));//this is the identifier of tr[1] row  and it should be unique
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[3]"));//this is the vendor id of the paticular battery the id of vendor after slecting dropddown while creating
	    	
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[6]")).click();//this is the edit section where we can edit battery of tr[1]
	    	//so there might be many vendor lists based on aaded new one will be in the first row
	    	driver.findElement(By.xpath("//input")).sendKeys("identifier");//this is the search txt field where based on identifier it will search and  keep battery information on 1st row
	    	
  //	if we click on edit section it goes to Edit battery page and it will show all details while creating,things and i edit section loc is same of create but we have to clear existing one and have to create new and click on svae
	    	
	  //motors
	    	driver.findElement(By.xpath("//button[.='Motors']")).click();//this nagivates to motors page 
	    	//all the flow and loc and every thing is same as battery just page is diffrent so refer battery 
	    	//except edit icon which is in index 7
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[7]")).click();
	    	
	   //Motorcontroller
	    	driver.findElement(By.xpath("//button[.='Motorcontroller']")).click();//this nagivates to Motorcontroller page 
	    	//all the flow and loc and every thing is same as motors just page is diffrent so refer motors
	    	
	    //VCU
	    	driver.findElement(By.xpath("//button[.='VCU']")).click();//this nagivates to VCU page 
	    	//all the flow and loc and every thing is same as motors just page is diffrent so refer motors
	    	//except while creating and editing one extra text field is their which i will meton hre
	    	
	    	driver.findElement(By.xpath("//input[@name='software_version']")).sendKeys("soome version");//in both creating and editing vcu this is extra
	    //KeyFob
	    	driver.findElement(By.xpath("//button[.='KeyFob']")).click();//this nagivates to keyfob page
	    	//all the flow and loc and every thing is same as motors just page is diffrent so refer motors
	    	//except while creating and editing one extra text field is their which i will meton hre
	    	
	    	driver.findElement(By.xpath("//input[@name='ble_name']")).sendKeys("some ble name");//in both creating and editing keyfob this is extra
	    //charger
	    	
	    	driver.findElement(By.xpath("//button[.='Charger']")).click();//this nagivates to charger page
	    	//all the flow and loc and every thing is same as motors just page is diffrent so refer motors
	    	
	    //Display
	    	driver.findElement(By.xpath("//button[.='Display']")).click();//this nagivates to charger page
	    	//all the flow and loc and every thing is same as motors just page is diffrent so refer motors
	    	//except edit icon which is in index 10
	    	driver.findElement(By.xpath("//table/tbody/tr[1]/td[10]")).click();
	    	//except while creating and editing one extra text field is their which i will meton hre
	    	driver.findElement(By.xpath("//input[@name='software_version_mcu']")).sendKeys("some software_version_mcu");
	    	driver.findElement(By.xpath("//input[@name='software_version_arm']")).sendKeys("some software_version_mcu");
	    	driver.findElement(By.xpath("//input[@name='software_version_fex']")).sendKeys("some software_version_mcu");

	    	
	    	
	    }
	    
	    @AfterMethod
	    public void tearDown() {
	        if (driver != null) {
	            driver.quit();
	        }
	    }
}
