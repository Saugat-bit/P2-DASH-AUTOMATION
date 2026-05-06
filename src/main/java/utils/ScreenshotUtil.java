package utils;

import java.io.File;
import java.io.IOException;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import io.qameta.allure.Attachment;

public class ScreenshotUtil {

    @Attachment(value = "Screenshot on failure", type = "image/png")
    public static byte[] captureScreenshot(WebDriver driver) {
        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

        // Store to disk (optional)
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            File destDir = new File("screenshots");
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            FileUtils.copyFile(srcFile, new File(destDir, "failure_" + System.currentTimeMillis() + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return screenshot;
    }
    }
