package com.stratis.base;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public abstract class BasePage {
    
    protected AppiumDriver driver;
    protected WebDriverWait wait;
    protected static final Logger logger = LogManager.getLogger(BasePage.class);
    
    public BasePage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        PageFactory.initElements(driver, this);
    }
    
    // Context Management Methods
    protected void switchToNative() {
        logger.info("Switching to NATIVE_APP context");
        try {
            if (driver instanceof IOSDriver) {
                ((IOSDriver) driver).context("NATIVE_APP");
            } else if (driver instanceof AndroidDriver) {
                ((AndroidDriver) driver).context("NATIVE_APP");
            }
        } catch (Exception e) {
            logger.warn("Failed to switch to native context: " + e.getMessage());
        }
    }
    
    protected boolean switchToAnyWebview() {
        logger.info("Switching to any WEBVIEW context");
        try {
            Set<String> contexts = null;
            if (driver instanceof IOSDriver) {
                contexts = ((IOSDriver) driver).getContextHandles();
            } else if (driver instanceof AndroidDriver) {
                contexts = ((AndroidDriver) driver).getContextHandles();
            }
            
            if (contexts != null) {
                logger.info("Available contexts: " + contexts);
                for (String context : contexts) {
                    if (context.startsWith("WEBVIEW")) {
                        if (driver instanceof IOSDriver) {
                            ((IOSDriver) driver).context(context);
                        } else if (driver instanceof AndroidDriver) {
                            ((AndroidDriver) driver).context(context);
                        }
                        logger.info("Switched to context: " + context);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Context detection failed: " + e.getMessage());
        }
        return false;
    }
    
    // Element Interaction Methods
    protected WebElement pollForElement(By locator, long timeoutSeconds) {
        long endTime = System.currentTimeMillis() + timeoutSeconds * 1000;
        while (System.currentTimeMillis() < endTime) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                if (!elements.isEmpty()) {
                    return elements.get(0);
                }
            } catch (Exception ignored) {
                // Continue polling
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }
    
    protected void safeClick(WebElement element) {
        try {
            element.click();
            logger.info("Successfully clicked element");
        } catch (Exception e) {
            logger.error("Failed to click element: " + e.getMessage());
            throw e;
        }
    }
    
    protected void safeType(WebElement element, String text) {
        try {
            element.click();
            element.clear();
            element.sendKeys(text);
            logger.info("Successfully typed text into element");
        } catch (Exception e) {
            logger.error("Failed to type text: " + e.getMessage());
            throw e;
        }
    }
    
    protected void hideKeyboard() {
        try {
            if (driver instanceof IOSDriver) {
                ((IOSDriver) driver).hideKeyboard();
            } else if (driver instanceof AndroidDriver) {
                ((AndroidDriver) driver).hideKeyboard();
            }
        } catch (Exception e) {
            logger.debug("Keyboard hide failed or not needed: " + e.getMessage());
        }
    }
    
    // Screenshot and Page Source Methods
    public void captureScreenshot(String tag) throws IOException {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File dest = new File("target/screenshots/" + tag + "-" + timestamp + ".png");
        dest.getParentFile().mkdirs();
        FileUtils.copyFile(src, dest);
        logger.info("Screenshot captured: " + dest.getAbsolutePath());
    }
    
    public void savePageSource(String tag) throws IOException {
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        File dest = new File("target/pagesource/" + tag + "-" + timestamp + ".xml");
        dest.getParentFile().mkdirs();
        FileUtils.writeStringToFile(dest, driver.getPageSource(), "UTF-8");
        logger.info("Page source saved: " + dest.getAbsolutePath());
    }
    
    // Perfecto Visual Commands (Fallback Methods)
    protected boolean tryPerfectoVisualClick(String label) {
        try {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("label", label);
            driver.executeScript("mobile:button-text:click", params);
            logger.info("Successfully clicked using Perfecto visual: " + label);
            return true;
        } catch (Exception e) {
            logger.warn("Perfecto visual click failed for label '" + label + "': " + e.getMessage());
            return false;
        }
    }
    
    protected boolean tryPerfectoVisualType(String text) {
        try {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("text", text);
            driver.executeScript("mobile:type", params);
            logger.info("Successfully typed using Perfecto visual");
            return true;
        } catch (Exception e) {
            logger.warn("Perfecto visual type failed: " + e.getMessage());
            return false;
        }
    }
    
    // Common Locator Strategies
    protected WebElement findByAccessibilityId(String accessibilityId) {
        return pollForElement(AppiumBy.accessibilityId(accessibilityId), 10);
    }
    
    protected WebElement findByClassName(String className) {
        return pollForElement(AppiumBy.className(className), 10);
    }
    
    // iOS-specific Locator Strategies
    protected WebElement findByiOSClassChain(String classChain) {
        return pollForElement(AppiumBy.iOSClassChain(classChain), 10);
    }
    
    protected WebElement findByiOSNsPredicate(String predicate) {
        return pollForElement(AppiumBy.iOSNsPredicateString(predicate), 10);
    }
    
    // Android-specific Locator Strategies
    protected WebElement findByAndroidUIAutomator(String uiAutomatorText) {
        return pollForElement(AppiumBy.androidUIAutomator(uiAutomatorText), 10);
    }
    
    protected WebElement findByAndroidDataMatcher(String dataMatcherText) {
        return pollForElement(AppiumBy.androidDataMatcher(dataMatcherText), 10);
    }
    
    // Platform detection helper
    protected boolean isAndroid() {
        return driver instanceof AndroidDriver;
    }
    
    protected boolean isIOS() {
        return driver instanceof IOSDriver;
    }
}
