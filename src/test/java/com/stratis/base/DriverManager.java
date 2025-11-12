package com.stratis.base;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.android.AndroidDriver;
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
    private static Properties properties;
    private static String currentPlatform;
    
    static {
        properties = new Properties();
        try {
            properties.load(DriverManager.class.getClassLoader().getResourceAsStream("perfecto.properties"));
        } catch (IOException e) {
            logger.error("Failed to load perfecto.properties", e);
            throw new RuntimeException("Configuration file not found", e);
        }
    }
    
    public static void initializeDriver(String platform) throws MalformedURLException {
        currentPlatform = platform.toLowerCase();
        logger.info("Initializing {} driver for Perfecto cloud", platform);
        
        final String PERFECTO_HUB = "https://stratis-1.perfectomobile.com/nexperience/perfectomobile/wd/hub";
        final String TOKEN = properties.getProperty("perfecto.token");
        
        if (TOKEN == null || TOKEN.isEmpty() || TOKEN.equals("YOUR_PERFECTO_TOKEN_HERE")) {
            throw new IllegalStateException("Perfecto token not configured in perfecto.properties file");
        }
        
        MutableCapabilities caps = new MutableCapabilities();
        Map<String, Object> perfectoOptions = new HashMap<>();
        perfectoOptions.put("securityToken", TOKEN);
        perfectoOptions.put("scriptName", "eStratis BDD Automation - " + platform);
        perfectoOptions.put("description", "Java/Cucumber/TestNG/Appium " + platform + " on Perfecto");
        caps.setCapability("perfecto:options", perfectoOptions);
        
        AppiumDriver appiumDriver;
        
        if ("ios".equalsIgnoreCase(platform)) {
            caps.setCapability("platformName", "iOS");
            caps.setCapability("appium:automationName", "XCUITest");
            caps.setCapability("appium:udid", properties.getProperty("perfecto.ios.device.id"));
            caps.setCapability("appium:deviceName", properties.getProperty("perfecto.ios.device.model"));
            caps.setCapability("appium:platformVersion", properties.getProperty("perfecto.ios.os.version"));
            caps.setCapability("appium:app", properties.getProperty("perfecto.ios.app.path"));
            caps.setCapability("appium:bundleId", properties.getProperty("perfecto.ios.bundle.id"));
            caps.setCapability("appium:autoAcceptAlerts", true);
            caps.setCapability("appium:newCommandTimeout", 180);
            caps.setCapability("appium:noReset", true);
            
            appiumDriver = new IOSDriver(new URL(PERFECTO_HUB), caps);
        } else if ("android".equalsIgnoreCase(platform)) {
            // Load Android-specific properties
            Properties androidProps = new Properties();
            try {
                androidProps.load(DriverManager.class.getClassLoader().getResourceAsStream("perfecto-android.properties"));
            } catch (IOException e) {
                logger.error("Failed to load perfecto-android.properties", e);
                throw new RuntimeException("Android configuration file not found", e);
            }
            
            caps.setCapability("platformName", "Android");
            caps.setCapability("appium:automationName", "UiAutomator2");
            caps.setCapability("appium:platformVersion", androidProps.getProperty("perfecto.android.os.version"));
            caps.setCapability("appium:manufacturer", androidProps.getProperty("perfecto.android.manufacturer"));
            caps.setCapability("appium:model", androidProps.getProperty("perfecto.android.model"));
            caps.setCapability("appium:deviceName", androidProps.getProperty("perfecto.android.device.id"));
            caps.setCapability("appium:app", androidProps.getProperty("perfecto.android.app.path"));
            caps.setCapability("appium:appPackage", androidProps.getProperty("perfecto.android.app.package"));
            caps.setCapability("appium:newCommandTimeout", 180);
            caps.setCapability("appium:noReset", true);
            
            appiumDriver = new AndroidDriver(new URL(PERFECTO_HUB), caps);
        } else {
            throw new IllegalArgumentException("Unsupported platform: " + platform + ". Supported platforms: iOS, Android");
        }
        
        appiumDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.set(appiumDriver);
        logger.info("{} driver initialized successfully", platform);
    }
    
    public static void initializeDriver() throws MalformedURLException {
        // Default to iOS for backward compatibility
        initializeDriver("iOS");
    }
    
    public static AppiumDriver getDriver() {
        return driver.get();
    }
    
    public static String getCurrentPlatform() {
        return currentPlatform;
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
            }
        }
    }
    
    public static Properties getProperties() {
        return properties;
    }
}
