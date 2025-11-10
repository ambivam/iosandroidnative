package com.stratis.tests;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
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
        caps.setCapability("appium:autoAcceptAlerts", true);
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
    public void loginFlow() throws Exception {
        // 0) Save page source early to confirm what the hierarchy looks like
        captureScreenshot("before-login");
        savePageSource("before-login");

        // 1) Always start in NATIVE_APP
        switchToNative();

        // 2) Try robust native locators first
        if (tryFillNative()) {
            tapLoginNative();
            captureScreenshot("after-login-tap-native");
            return;
        }

        // 3) If not found natively, try WEBVIEW
        if (switchToAnyWebview()) {
            if (tryFillWebView()) {
                tapLoginWebView();
                captureScreenshot("after-login-tap-webview");
                // switch back if you need native again
                switchToNative();
                return;
            }
        }

        // 4) Nothing matched: fail with helpful context
        throw new RuntimeException("Could not locate username/password in NATIVE or WEBVIEW context. " +
                "Check latest target/pagesource/*.xml and contexts printed in logs.");
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

    // --- Context helpers ---
    private void switchToNative() {
        try { driver.context("NATIVE_APP"); } catch (Exception ignore) {}
    }

    private boolean switchToAnyWebview() {
        try {
            java.util.Set<String> ctxs = driver.getContextHandles();
            System.out.println("Available contexts: " + ctxs);
            for (String ctx : ctxs) {
                if (ctx.startsWith("WEBVIEW")) {
                    driver.context(ctx);
                    System.out.println("Switched to context: " + ctx);
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Context detection failed: " + e.getMessage());
        }
        return false;
    }

    // --- Polling wait (bypasses ExpectedConditions path that caused ClassCast) ---
    private WebElement pollFor(By by, long timeoutSec) {
        long end = System.currentTimeMillis() + timeoutSec * 1000;
        while (System.currentTimeMillis() < end) {
            try {
                java.util.List<WebElement> els = driver.findElements(by);
                if (!els.isEmpty()) return els.get(0);
            } catch (Exception ignore) {}
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    // --- Native strategies ---
    private boolean tryFillNative() {
        // Strategy A: placeholder contains (English screenshot text)
        WebElement user = pollFor(
            AppiumBy.iOSClassChain("**/XCUIElementTypeTextField[`value CONTAINS[c] 'Please enter your username' OR value CONTAINS[c] 'username'`]"),
            10
        );
        WebElement pass = pollFor(
            AppiumBy.iOSClassChain("**/XCUIElementTypeSecureTextField[`value CONTAINS[c] 'Please enter your password' OR value CONTAINS[c] 'password'`]"),
            10
        );

        // Strategy B: first visible textfield + securetextfield (fallback)
        if (user == null) {
            java.util.List<WebElement> tf = driver.findElements(AppiumBy.className("XCUIElementTypeTextField"));
            if (!tf.isEmpty()) user = tf.get(0);
        }
        if (pass == null) {
            java.util.List<WebElement> sf = driver.findElements(AppiumBy.className("XCUIElementTypeSecureTextField"));
            if (!sf.isEmpty()) pass = sf.get(0);
        }

        if (user != null && pass != null) {
            typeInto(user, "rajanikanth.bathula@mystratis.com");
            typeInto(pass, "Notallowed@123");
            System.out.println("Login credentials filled successfully");
            try { driver.hideKeyboard(); } catch (Exception ignore) {}
            return true;
        }
        return false;
    }

    private void tapLoginNative() {
        // Button by exact text, then a relaxed contains fallback
        WebElement login = pollFor(
            AppiumBy.iOSNsPredicateString("(type == 'XCUIElementTypeButton' OR type == 'XCUIElementTypeOther') AND " +
                                          "(label == 'Log into my account' OR name == 'Log into my account')"),
            8
        );
        if (login == null) {
            login = pollFor(
                AppiumBy.iOSNsPredicateString("(type == 'XCUIElementTypeButton' OR type == 'XCUIElementTypeOther') AND " +
                                              "(label CONTAINS[c] 'Log into' OR name CONTAINS[c] 'Log into')"),
                8
            );
        }
        if (login != null) login.click();
    }

    // --- WebView strategies ---
    private boolean tryFillWebView() {
        try {
            // CSS by placeholder (typical hybrid login)
            WebElement u = pollFor(By.cssSelector("input[placeholder*='username' i], input[name*='username' i]"), 8);
            WebElement p = pollFor(By.cssSelector("input[type='password'], input[placeholder*='password' i]"), 8);
            if (u != null && p != null) {
                typeInto(u, "your.user@stratis.com");
                typeInto(p, "SuperSecret123!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("WEBVIEW fill failed: " + e.getMessage());
        }
        return false;
    }

    private void tapLoginWebView() {
        try {
            WebElement btn = pollFor(By.cssSelector("button, [role='button']"), 6);
            if (btn != null) btn.click();
        } catch (Exception ignore) {}
    }

    // --- Safe typing utility ---
    private void typeInto(WebElement el, String text) {
        el.click();
        try { el.clear(); } catch (Exception ignore) {}
        el.sendKeys(text);
    }

    // --- Perfecto Visual Fallback Methods (Last Resort) ---
    private boolean tryPerfectoVisualLogin() {
        try {
            // Verify username field is visible
            Map<String, Object> params = new HashMap<>();
            params.put("content", "Username");
            driver.executeScript("mobile:checkpoint:text", params);
            
            // Click username field by visible text
            params.clear();
            params.put("label", "Username");
            driver.executeScript("mobile:button-text:click", params);
            
            // Type username into focused field
            Map<String, Object> typeParams = new HashMap<>();
            typeParams.put("text", "your.user@stratis.com");
            driver.executeScript("mobile:type", typeParams);
            
            // Click password field
            params.clear();
            params.put("label", "Password");
            driver.executeScript("mobile:button-text:click", params);
            
            // Type password into focused field
            typeParams.clear();
            typeParams.put("text", "SuperSecret123!");
            driver.executeScript("mobile:type", typeParams);
            
            // Click login button
            params.clear();
            params.put("label", "Log into my account");
            driver.executeScript("mobile:button-text:click", params);
            
            return true;
        } catch (Exception e) {
            System.out.println("Perfecto visual fallback failed: " + e.getMessage());
            return false;
        }
    }
}
