package com.stratis.base;

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
    private static ThreadLocal<IOSDriver> driver = new ThreadLocal<>();
    private static Properties properties;
    
    static {
        properties = new Properties();
        try {
            properties.load(DriverManager.class.getClassLoader().getResourceAsStream("perfecto.properties"));
        } catch (IOException e) {
            logger.error("Failed to load perfecto.properties", e);
            throw new RuntimeException("Configuration file not found", e);
        }
    }
    
    public static void initializeDriver() throws MalformedURLException {
        logger.info("Initializing iOS driver for Perfecto cloud");
        
        final String PERFECTO_HUB = "https://stratis-1.perfectomobile.com/nexperience/perfectomobile/wd/hub";
        final String TOKEN = properties.getProperty("perfecto.token");
        
        if (TOKEN == null || TOKEN.isEmpty() || TOKEN.equals("YOUR_PERFECTO_TOKEN_HERE")) {
            throw new IllegalStateException("Perfecto token not configured in perfecto.properties file");
        }
        
        MutableCapabilities caps = new MutableCapabilities();
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
        
        Map<String, Object> perfectoOptions = new HashMap<>();
        perfectoOptions.put("securityToken", TOKEN);
        perfectoOptions.put("scriptName", "eStratis BDD Automation");
        perfectoOptions.put("description", "Java/Cucumber/TestNG/Appium iOS on Perfecto");
        caps.setCapability("perfecto:options", perfectoOptions);
        
        IOSDriver iosDriver = new IOSDriver(new URL(PERFECTO_HUB), caps);
        iosDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        driver.set(iosDriver);
        logger.info("iOS driver initialized successfully");
    }
    
    public static IOSDriver getDriver() {
        return driver.get();
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
