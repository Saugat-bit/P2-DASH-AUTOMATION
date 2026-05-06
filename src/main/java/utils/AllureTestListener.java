package utils;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.openqa.selenium.WebDriver;
import com.p2.automationbase.*;
import utils.ScreenshotUtil;

public class AllureTestListener extends Base implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        Object testClass = result.getInstance();
        WebDriver driver = Base.driver;
        if (driver != null) {
            try {
                ScreenshotUtil.captureScreenshot(driver);
            } catch (Exception e) {
                System.out.println("Could not take screenshot: " + e.getMessage());
            }
        }
    }
}
