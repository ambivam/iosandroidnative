package com.stratis.base;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.MutableCapabilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DriverManager {
    
    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    private static ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    private static Properties iosProperties;
    private static Properties androidProperties;
    private static String currentPlatform;
    
    static {
        iosProperties = new Properties();
        androidProperties = new Properties();
        try {
            iosProperties.load(DriverManager.class.getClassLoader().getResourceAsStream("perfecto.properties"));
            androidProperties.load(DriverManager.class.getClassLoader().getResourceAsStream("perfecto-android.properties"));
        } catch (IOException e) {
            logger.error("Failed to load properties files", e);
            throw new RuntimeException("Configuration files not found", e);
        }
    }
    
    public static void initializeDriver(String platform) throws MalformedURLException {
        currentPlatform = platform.toLowerCase();
        logger.info("=== PLATFORM SELECTION DEBUG ===");
        logger.info("Requested platform: {}", platform);
        logger.info("Current platform set to: {}", currentPlatform);
        logger.info("Initializing {} driver for Perfecto cloud", platform);
        
        final String PERFECTO_HUB = "https://stratis-1.perfectomobile.com/nexperience/perfectomobile/wd/hub";
        
        AppiumDriver appiumDriver;
        if ("ios".equalsIgnoreCase(platform)) {
            appiumDriver = initializeIOSDriver(PERFECTO_HUB);
        } else if ("android".equalsIgnoreCase(platform)) {
            appiumDriver = initializeAndroidDriver(PERFECTO_HUB);
        } else {
            throw new IllegalArgumentException("Unsupported platform: " + platform + ". Use 'iOS' or 'Android'");
        }
        
        appiumDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.set(appiumDriver);
        logger.info("{} driver initialized successfully", platform);
    }
    
    private static IOSDriver initializeIOSDriver(String hubUrl) throws MalformedURLException {
        final String TOKEN = iosProperties.getProperty("perfecto.token");
        
        if (TOKEN == null || TOKEN.isEmpty() || TOKEN.equals("YOUR_PERFECTO_TOKEN_HERE")) {
            throw new IllegalStateException("Perfecto token not configured in perfecto.properties file");
        }
        
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "iOS");
        caps.setCapability("appium:automationName", "XCUITest");
        
        // Only set device ID if it's not empty
        String deviceId = iosProperties.getProperty("perfecto.ios.device.id");
        if (deviceId != null && !deviceId.trim().isEmpty()) {
            caps.setCapability("appium:udid", deviceId);
        }
        
        caps.setCapability("appium:deviceName", iosProperties.getProperty("perfecto.ios.device.model"));
        caps.setCapability("appium:platformVersion", iosProperties.getProperty("perfecto.ios.os.version"));
        caps.setCapability("appium:app", iosProperties.getProperty("perfecto.ios.app.path"));
        caps.setCapability("appium:bundleId", iosProperties.getProperty("perfecto.ios.bundle.id"));
        caps.setCapability("appium:autoAcceptAlerts", true);
        caps.setCapability("appium:newCommandTimeout", 180);
        caps.setCapability("appium:noReset", true);
        
        Map<String, Object> perfectoOptions = new HashMap<>();
        perfectoOptions.put("securityToken", TOKEN);
        perfectoOptions.put("scriptName", "eStratis iOS Automation");
        perfectoOptions.put("description", "Java/Cucumber/TestNG/Appium iOS on Perfecto");
        caps.setCapability("perfecto:options", perfectoOptions);
        
        return new IOSDriver(new URL(hubUrl), caps);
    }
    
    private static AndroidDriver initializeAndroidDriver(String hubUrl) throws MalformedURLException {
        logger.info("=== ANDROID DRIVER INITIALIZATION ===");
        logger.info("Loading Android properties from perfecto-android.properties");
        
        final String TOKEN = androidProperties.getProperty("perfecto.token");
        logger.info("Android token loaded: {}", TOKEN != null ? "✓ Present" : "✗ Missing");
        
        if (TOKEN == null || TOKEN.isEmpty() || TOKEN.equals("YOUR_PERFECTO_TOKEN_HERE")) {
            throw new IllegalStateException("Perfecto token not configured in perfecto-android.properties file");
        }
        
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:automationName", "UiAutomator2");
        caps.setCapability("appium:udid", androidProperties.getProperty("perfecto.android.device.id"));
        caps.setCapability("appium:deviceName", androidProperties.getProperty("perfecto.android.model"));
        caps.setCapability("appium:platformVersion", androidProperties.getProperty("perfecto.android.os.version"));
        caps.setCapability("appium:app", androidProperties.getProperty("perfecto.android.app.path"));
        caps.setCapability("appium:appPackage", androidProperties.getProperty("perfecto.android.app.package"));
        caps.setCapability("appium:newCommandTimeout", 180);
        caps.setCapability("appium:noReset", true);
        
        Map<String, Object> perfectoOptions = new HashMap<>();
        perfectoOptions.put("securityToken", TOKEN);
        perfectoOptions.put("scriptName", "eStratis Android Automation");
        perfectoOptions.put("description", "Java/Cucumber/TestNG/Appium Android on Perfecto");
        caps.setCapability("perfecto:options", perfectoOptions);
        
        return new AndroidDriver(new URL(hubUrl), caps);
    }
    
    // Backward compatibility - defaults to iOS
    public static void initializeDriver() throws MalformedURLException {
        initializeDriver("iOS");
    }
    
    public static AppiumDriver getDriver() {
        return driver.get();
    }
    
    // Backward compatibility for iOS-specific access
    public static IOSDriver getIOSDriver() {
        AppiumDriver appiumDriver = driver.get();
        if (appiumDriver instanceof IOSDriver) {
            return (IOSDriver) appiumDriver;
        }
        throw new IllegalStateException("Current driver is not an iOS driver");
    }
    
    // Android-specific access
    public static AndroidDriver getAndroidDriver() {
        AppiumDriver appiumDriver = driver.get();
        if (appiumDriver instanceof AndroidDriver) {
            return (AndroidDriver) appiumDriver;
        }
        throw new IllegalStateException("Current driver is not an Android driver");
    }
    
    public static void quitDriver() {
        if (driver.get() != null) {
            try {
                driver.get().quit();
                logger.info("Driver quit successfully");
            } catch (Exception e) {
                logger.error("Error while quitting driver", e);
            } finally {
                driver.remove();
                currentPlatform = null;
            }
        }
    }
    
    public static Properties getProperties() {
        if ("android".equalsIgnoreCase(currentPlatform)) {
            return androidProperties;
        }
        return iosProperties; // Default to iOS
    }
    
    public static Properties getIOSProperties() {
        return iosProperties;
    }
    
    public static Properties getAndroidProperties() {
        return androidProperties;
    }
    
    public static String getCurrentPlatform() {
        return currentPlatform;
    }
}
