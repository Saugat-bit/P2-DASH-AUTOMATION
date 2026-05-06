package com.p2.qa.sprint1.testcases;

import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.p2.automationbase.Base;
import com.p2.automationbase.Login;
import com.p2.qa.sprint1.pageobjects.StaffmoduleObjects;

public class TokenTest extends Base{
	 private WebDriver driver;
	   
	    
	    @BeforeMethod
	    public void setUp() {
	        driver = initializeBrowserAndOpenApplication();
	        driver.manage().deleteAllCookies();
	        Login loginPage = new Login();
	         driver = loginPage.loginAs(driver, "admin");
	      //  driver.manage().deleteAllCookies();
	    }
	    
	    @AfterMethod
	    public void tearDown() {
	        if (driver != null) {
	            driver.quit();
	        }
	    }
	    @Test
	    
public void LoginTestAndTokenGeneration() throws InterruptedException
{
	 Thread.sleep(2000);
     // Wait for dashboard URL and ensure cookies are set
	 WebDriverWait wait=new WebDriverWait(driver,Duration.ofSeconds(20) );
     wait.until(ExpectedConditions.urlContains("dash"));
     // Add a small delay to ensure cookies are populated (adjust as needed)
     try {
         Thread.sleep(3000); // Increased delay to 3 seconds
     } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
     }

     // Retrieve and log all cookies for debugging
     Set<Cookie> allCookies = driver.manage().getCookies();
     System.out.println("All Cookies: ");
     for (Cookie cookie : allCookies) {
         System.out.println("Name: " + cookie.getName() + ", Value: " + cookie.getValue() +
                          ", Domain: " + cookie.getDomain() + ", Path: " + cookie.getPath());
     }

     // Extract tokens from cookies with correct names
     Cookie accessTokenCookie = driver.manage().getCookieNamed("accessToken"); 
     Cookie refreshTokenCookie = driver.manage().getCookieNamed("refreshToken"); 

     // Assert and log the extracted tokens
     assert accessTokenCookie != null : "Access token not found in cookies";
     assert refreshTokenCookie != null : "Refresh token not found in cookies";

     String accessToken = accessTokenCookie.getValue();
     String refreshToken = refreshTokenCookie.getValue();

     System.out.println("Extracted Access Token: " + accessToken);
     System.out.println("Extracted Refresh Token: " + refreshToken);
     
     
     //refresh and check it is generating  new acessToken and resetToken 



}

	    
	    
	    
	    
	    
	    
}
