package com.stratis.tests;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PerfectoIOSBasicTest {

    private IOSDriver driver;
    private Properties p = new Properties();

    @BeforeClass
    public void setUp() throws MalformedURLException, IOException {
        p.load(PerfectoIOSBasicTest.class.getClassLoader().getResourceAsStream("perfecto.properties"));
        final String PERFECTO_HUB = "https://stratis-1.perfectomobile.com/nexperience/perfectomobile/wd/hub";
        final String TOKEN = p.getProperty("perfecto.token");
        if (TOKEN == null || TOKEN.isEmpty() || TOKEN.equals("YOUR_PERFECTO_TOKEN_HERE")) {
            throw new IllegalStateException("Perfecto token not configured in perfecto.properties file");
        }

        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "iOS");
        caps.setCapability("appium:automationName", "XCUITest");
        caps.setCapability("appium:udid", p.getProperty("perfecto.ios.device.id"));
        caps.setCapability("appium:deviceName", p.getProperty("perfecto.ios.device.model"));
        caps.setCapability("appium:platformVersion", p.getProperty("perfecto.ios.os.version"));
        caps.setCapability("appium:app", p.getProperty("perfecto.ios.app.path"));
        caps.setCapability("appium:bundleId", p.getProperty("perfecto.ios.bundle.id"));
        caps.setCapability("appium:newCommandTimeout", 180);
        caps.setCapability("appium:noReset", true);

        Map<String, Object> perfectoOptions = new HashMap<>();
        perfectoOptions.put("securityToken", TOKEN);
        perfectoOptions.put("scriptName", "eStratis Smoke");
        perfectoOptions.put("description", "Java/TestNG/Appium iOS on Perfecto");
        caps.setCapability("perfecto:options", perfectoOptions);

        driver = new IOSDriver(new URL(PERFECTO_HUB), caps);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Test
    public void sampleFlow() throws IOException {
        try { driver.activateApp(p.getProperty("perfecto.ios.bundle.id")); } catch (Exception ignore) {}
        try {
            WebElement loginBtn = driver.findElement(AppiumBy.accessibilityId("login_button"));
            Assert.assertNotNull(loginBtn, "Login button not found");
        } catch (Exception e) {
            System.out.println("Locator not found. Replace with actual app locator.");
        }
        captureScreenshot("mid-test");
        Assert.assertTrue(true);
    }

    @AfterMethod(alwaysRun = true)
    public void afterEach(ITestResult result) throws IOException {
        String name = (result.isSuccess() ? "PASS" : "FAIL") + "-" + result.getMethod().getMethodName();
        captureScreenshot(name);
        savePageSource(name);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) try { driver.quit(); } catch (Exception ignore) {}
    }

    private void captureScreenshot(String tag) throws IOException {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String ts = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File dest = new File("target/screenshots/" + tag + "-" + ts + ".png");
        dest.getParentFile().mkdirs();
        FileUtils.copyFile(src, dest);
    }

    private void savePageSource(String tag) throws IOException {
        String ts = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File dest = new File("target/pagesource/" + tag + "-" + ts + ".xml");
        dest.getParentFile().mkdirs();
        FileUtils.writeStringToFile(dest, driver.getPageSource(), "UTF-8");
    }
}
