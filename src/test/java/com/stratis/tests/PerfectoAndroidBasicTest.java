package com.stratis.tests;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
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

public class PerfectoAndroidBasicTest {

    private AndroidDriver driver;
    private Properties p = new Properties();

    // Android-specific element locators using PageFactory
    @AndroidFindBy(xpath = "//android.widget.EditText[contains(@resource-id,'username') or contains(@text,'Username') or contains(@hint,'Username')]")
    private WebElement usernameField;

    @AndroidFindBy(xpath = "//android.widget.EditText[contains(@resource-id,'password') or contains(@text,'Password') or contains(@hint,'Password')]")
    private WebElement passwordField;

    @AndroidFindBy(xpath = "//android.widget.Button[contains(@text,'Login') or contains(@text,'Sign In') or contains(@resource-id,'login')]")
    private WebElement loginButton;

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Error') or contains(@text,'Invalid') or contains(@resource-id,'error')]")
    private WebElement errorMessage;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Welcome']")
    private WebElement welcomeMessage;

    @BeforeClass
    public void setUp() throws MalformedURLException, IOException {
        p.load(PerfectoAndroidBasicTest.class.getClassLoader().getResourceAsStream("perfecto-android.properties"));
        final String PERFECTO_HUB = "https://stratis-1.perfectomobile.com/nexperience/perfectomobile/wd/hub";
        final String TOKEN = p.getProperty("perfecto.token");
        if (TOKEN == null || TOKEN.isEmpty() || TOKEN.equals("YOUR_PERFECTO_TOKEN_HERE")) {
            throw new IllegalStateException("Perfecto token not configured in perfecto-android.properties file");
        }

        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName", "Appium");
        caps.setCapability("appium:platformVersion", p.getProperty("perfecto.android.os.version"));
        caps.setCapability("appium:manufacturer", p.getProperty("perfecto.android.manufacturer"));
        caps.setCapability("appium:model", p.getProperty("perfecto.android.model"));
        caps.setCapability("appium:deviceName", p.getProperty("perfecto.android.device.id"));
        caps.setCapability("appium:app", p.getProperty("perfecto.android.app.path"));
        caps.setCapability("appium:appPackage", p.getProperty("perfecto.android.app.package"));
        caps.setCapability("appium:newCommandTimeout", 180);
        caps.setCapability("appium:noReset", true);

        Map<String, Object> perfectoOptions = new HashMap<>();
        perfectoOptions.put("securityToken", TOKEN);
        perfectoOptions.put("scriptName", "eStratis Android Smoke");
        perfectoOptions.put("description", "Java/TestNG/Appium Android on Perfecto");
        caps.setCapability("perfecto:options", perfectoOptions);

        driver = new AndroidDriver(new URL(PERFECTO_HUB), caps);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        // Initialize PageFactory elements
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    @Test
    public void loginFlow() throws Exception {
        // 0) Save page source early to confirm what the hierarchy looks like
        captureScreenshot("before-login");
        savePageSource("before-login");

        // 1) Always start in NATIVE_APP
        switchToNative();

        // 2) Try PageFactory elements first
        if (tryFillWithPageFactory()) {
            tapLoginWithPageFactory();
            captureScreenshot("after-login-tap-pagefactory");
            return;
        }

        // 3) Try robust native locators as fallback
        if (tryFillNative()) {
            tapLoginNative();
            captureScreenshot("after-login-tap-native");
            return;
        }

        // 4) If not found natively, try WEBVIEW
        if (switchToAnyWebview()) {
            if (tryFillWebView()) {
                tapLoginWebView();
                captureScreenshot("after-login-tap-webview");
                // switch back if you need native again
                switchToNative();
                return;
            }
        }

        // 5) Last resort: try Perfecto visual fallback
        switchToNative();
        if (tryPerfectoVisualLogin()) {
            captureScreenshot("after-login-tap-visual");
            return;
        }

        // 6) Nothing matched: fail with helpful context
        throw new RuntimeException("Could not locate username/password in NATIVE, WEBVIEW, or VISUAL context. " +
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
        System.out.println("Switching to NATIVE_APP context");
        try { driver.context("NATIVE_APP"); } catch (Exception ignore) {}
    }

    private boolean switchToAnyWebview() {
        System.out.println("Switching to any WEBVIEW context");
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

    // --- PageFactory-based strategies (Primary approach) ---
    private boolean tryFillWithPageFactory() {
        try {
            if (usernameField != null && passwordField != null) {
                typeInto(usernameField, "rajanikanth.bathula@mystratis.com");
                typeInto(passwordField, "Notallowed@123");
                System.out.println("Login credentials filled successfully using PageFactory");
                try { driver.hideKeyboard(); } catch (Exception ignore) {}
                return true;
            }
        } catch (Exception e) {
            System.out.println("PageFactory fill failed: " + e.getMessage());
        }
        return false;
    }

    private void tapLoginWithPageFactory() {
        try {
            if (loginButton != null) {
                loginButton.click();
                System.out.println("Login button clicked using PageFactory");
            }
        } catch (Exception e) {
            System.out.println("PageFactory login button click failed: " + e.getMessage());
        }
    }

    // --- Native strategies (Fallback) ---
    private boolean tryFillNative() {
        // Strategy A: Android UiAutomator locators
        WebElement user = pollFor(
            By.xpath("//android.widget.EditText[contains(@resource-id,'username') or contains(@text,'Username') or contains(@hint,'Username')]"),
            10
        );
        WebElement pass = pollFor(
            By.xpath("//android.widget.EditText[contains(@resource-id,'password') or contains(@text,'Password') or contains(@hint,'Password')]"),
            10
        );

        // Strategy B: Generic EditText fallback
        if (user == null) {
            java.util.List<WebElement> editTexts = driver.findElements(By.className("android.widget.EditText"));
            if (editTexts.size() >= 2) {
                user = editTexts.get(0);
                pass = editTexts.get(1);
            }
        }

        if (user != null && pass != null) {
            typeInto(user, "rajanikanth.bathula@mystratis.com");
            typeInto(pass, "Notallowed@123");
            System.out.println("Login credentials filled successfully using native locators");
            try { driver.hideKeyboard(); } catch (Exception ignore) {}
            return true;
        }
        return false;
    }

    private void tapLoginNative() {
        // Button by Android-specific locators
        WebElement login = pollFor(
            By.xpath("//android.widget.Button[contains(@text,'Login') or contains(@text,'Sign In') or contains(@resource-id,'login')]"),
            8
        );
        
        // Fallback to any button
        if (login == null) {
            java.util.List<WebElement> buttons = driver.findElements(By.className("android.widget.Button"));
            if (!buttons.isEmpty()) {
                login = buttons.get(0);
            }
        }
        
        if (login != null) {
            login.click();
            System.out.println("Login button clicked using native locators");
        }
    }

    // --- WebView strategies ---
    private boolean tryFillWebView() {
        try {
            // CSS by placeholder (typical hybrid login)
            WebElement u = pollFor(By.cssSelector("input[placeholder*='username' i], input[name*='username' i]"), 8);
            WebElement p = pollFor(By.cssSelector("input[type='password'], input[placeholder*='password' i]"), 8);
            if (u != null && p != null) {
                typeInto(u, "rajanikanth.bathula@mystratis.com");
                typeInto(p, "Notallowed@123");
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
            typeParams.put("text", "rajanikanth.bathula@mystratis.com");
            driver.executeScript("mobile:type", typeParams);
            
            // Click password field
            params.clear();
            params.put("label", "Password");
            driver.executeScript("mobile:button-text:click", params);
            
            // Type password into focused field
            typeParams.clear();
            typeParams.put("text", "Notallowed@123");
            driver.executeScript("mobile:type", typeParams);
            
            // Click login button
            params.clear();
            params.put("label", "Login");
            driver.executeScript("mobile:button-text:click", params);
            
            return true;
        } catch (Exception e) {
            System.out.println("Perfecto visual fallback failed: " + e.getMessage());
            return false;
        }
    }
}
