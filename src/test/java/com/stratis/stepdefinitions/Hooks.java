package com.stratis.stepdefinitions;

import com.stratis.base.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.net.MalformedURLException;

public class Hooks {
    
    private static final Logger logger = LogManager.getLogger(Hooks.class);
    
    @Before
    public void setUp(Scenario scenario) throws MalformedURLException {
        logger.info("Setting up test environment for scenario: " + scenario.getName());
        
        try {
            DriverManager.initializeDriver();
            logger.info("Driver initialized successfully for scenario: " + scenario.getName());
        } catch (Exception e) {
            logger.error("Failed to initialize driver for scenario: " + scenario.getName(), e);
            throw e;
        }
    }
    
    @After
    public void tearDown(Scenario scenario) {
        logger.info("Tearing down test environment for scenario: " + scenario.getName());
        
        try {
            // Capture screenshot if scenario failed
            if (scenario.isFailed()) {
                logger.info("Scenario failed, capturing screenshot");
                byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failed Scenario Screenshot");
            }
            
            // Capture final screenshot for all scenarios
            byte[] finalScreenshot = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            String screenshotName = scenario.getStatus().toString().toLowerCase() + "_final_screenshot";
            scenario.attach(finalScreenshot, "image/png", screenshotName);
            
        } catch (Exception e) {
            logger.error("Error during screenshot capture: " + e.getMessage());
        } finally {
            // Always quit the driver
            DriverManager.quitDriver();
            logger.info("Driver quit successfully for scenario: " + scenario.getName());
        }
    }
}
