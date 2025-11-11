package com.stratis.pages;

import com.stratis.base.BasePage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class LoginPage extends BasePage {
    
    // Page Elements using different locator strategies
    @FindBy(className = "XCUIElementTypeTextField")
    private List<WebElement> textFields;
    
    @FindBy(className = "XCUIElementTypeSecureTextField")
    private List<WebElement> secureTextFields;
    
    @FindBy(className = "XCUIElementTypeButton")
    private List<WebElement> buttons;
    
    public LoginPage(IOSDriver driver) {
        super(driver);
    }
    
    // Login Actions
    public boolean fillLoginCredentials(String username, String password) {
        logger.info("Attempting to fill login credentials");
        
        // Always start in native context
        switchToNative();
        
        // Try native approach first
        if (tryFillNative(username, password)) {
            return true;
        }
        
        // Try webview approach if native fails
        if (switchToAnyWebview()) {
            if (tryFillWebView(username, password)) {
                switchToNative(); // Switch back to native
                return true;
            }
        }
        
        // Try Perfecto visual approach as last resort
        return tryPerfectoVisualLogin(username, password);
    }
    
    public boolean clickLoginButton() {
        logger.info("Attempting to click login button");
        
        // Try native login button click
        if (tryClickLoginNative()) {
            return true;
        }
        
        // Try webview login button click
        if (switchToAnyWebview()) {
            if (tryClickLoginWebView()) {
                switchToNative();
                return true;
            }
        }
        
        // Try Perfecto visual click
        return tryPerfectoVisualClick("Log into my account");
    }
    
    // Native Implementation Methods - Based on working PerfectoIOSBasicTest
    private boolean tryFillNative(String username, String password) {
        try {
            logger.info("Trying native login form fill using proven strategies");
            
            // Strategy A: placeholder contains (from working test)
            WebElement usernameField = pollForElement(
                AppiumBy.iOSClassChain("**/XCUIElementTypeTextField[`value CONTAINS[c] 'Please enter your username' OR value CONTAINS[c] 'username'`]"),
                10
            );
            WebElement passwordField = pollForElement(
                AppiumBy.iOSClassChain("**/XCUIElementTypeSecureTextField[`value CONTAINS[c] 'Please enter your password' OR value CONTAINS[c] 'password'`]"),
                10
            );
            
            // Strategy B: first visible textfield + securetextfield (fallback from working test)
            if (usernameField == null) {
                java.util.List<WebElement> textFields = driver.findElements(AppiumBy.className("XCUIElementTypeTextField"));
                if (!textFields.isEmpty()) {
                    usernameField = textFields.get(0);
                    logger.info("Using first text field as username field");
                }
            }
            if (passwordField == null) {
                java.util.List<WebElement> secureFields = driver.findElements(AppiumBy.className("XCUIElementTypeSecureTextField"));
                if (!secureFields.isEmpty()) {
                    passwordField = secureFields.get(0);
                    logger.info("Using first secure text field as password field");
                }
            }
            
            if (usernameField != null && passwordField != null) {
                // Use the proven typeInto method from working test
                typeInto(usernameField, username);
                typeInto(passwordField, password);
                logger.info("Login credentials filled successfully using proven method");
                
                try { 
                    driver.hideKeyboard(); 
                } catch (Exception ignore) {
                    logger.debug("Keyboard hide not needed or failed");
                }
                return true;
            } else {
                logger.warn("Could not find username or password fields");
            }
            
        } catch (Exception e) {
            logger.error("Native login form fill failed: " + e.getMessage());
        }
        return false;
    }
    
    // Proven typeInto method from working PerfectoIOSBasicTest
    private void typeInto(WebElement element, String text) {
        try {
            element.click();
            try { 
                element.clear(); 
            } catch (Exception ignore) {
                logger.debug("Element clear failed or not needed");
            }
            element.sendKeys(text);
            logger.info("Successfully typed into element using proven method");
        } catch (Exception e) {
            logger.error("Failed to type into element: " + e.getMessage());
            throw e;
        }
    }
    
    private boolean tryClickLoginNative() {
        try {
            logger.info("Trying native login button click using proven strategies");
            
            // Button by exact text, then a relaxed contains fallback (from working test)
            WebElement loginButton = pollForElement(
                AppiumBy.iOSNsPredicateString("(type == 'XCUIElementTypeButton' OR type == 'XCUIElementTypeOther') AND " +
                                              "(label == 'Log into my account' OR name == 'Log into my account')"),
                8
            );
            
            if (loginButton == null) {
                loginButton = pollForElement(
                    AppiumBy.iOSNsPredicateString("(type == 'XCUIElementTypeButton' OR type == 'XCUIElementTypeOther') AND " +
                                                  "(label CONTAINS[c] 'Log into' OR name CONTAINS[c] 'Log into')"),
                    8
                );
            }
            
            if (loginButton != null) {
                loginButton.click();
                logger.info("Successfully clicked native login button using proven method");
                return true;
            } else {
                logger.warn("Could not find login button");
            }
            
        } catch (Exception e) {
            logger.error("Native login button click failed: " + e.getMessage());
        }
        return false;
    }
    
    // WebView Implementation Methods
    private boolean tryFillWebView(String username, String password) {
        try {
            logger.info("Trying webview login form fill");
            
            WebElement usernameField = pollForElement(
                By.cssSelector("input[placeholder*='username' i], input[name*='username' i], input[type='email']"), 8
            );
            
            WebElement passwordField = pollForElement(
                By.cssSelector("input[type='password'], input[placeholder*='password' i]"), 8
            );
            
            if (usernameField != null && passwordField != null) {
                safeType(usernameField, username);
                safeType(passwordField, password);
                logger.info("Successfully filled webview login form");
                return true;
            }
            
        } catch (Exception e) {
            logger.error("WebView login form fill failed: " + e.getMessage());
        }
        return false;
    }
    
    private boolean tryClickLoginWebView() {
        try {
            logger.info("Trying webview login button click");
            
            WebElement loginButton = pollForElement(
                By.cssSelector("button[type='submit'], button:contains('Log'), input[type='submit']"), 6
            );
            
            if (loginButton != null) {
                safeClick(loginButton);
                logger.info("Successfully clicked webview login button");
                return true;
            }
            
        } catch (Exception e) {
            logger.error("WebView login button click failed: " + e.getMessage());
        }
        return false;
    }
    
    // Perfecto Visual Methods - Based on working PerfectoIOSBasicTest
    private boolean tryPerfectoVisualLogin(String username, String password) {
        try {
            logger.info("Trying Perfecto visual login using proven method");
            
            // Verify username field is visible
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("content", "Username");
            driver.executeScript("mobile:checkpoint:text", params);
            
            // Click username field by visible text
            params.clear();
            params.put("label", "Username");
            driver.executeScript("mobile:button-text:click", params);
            
            // Type username into focused field
            java.util.Map<String, Object> typeParams = new java.util.HashMap<>();
            typeParams.put("text", username);
            driver.executeScript("mobile:type", typeParams);
            
            // Click password field
            params.clear();
            params.put("label", "Password");
            driver.executeScript("mobile:button-text:click", params);
            
            // Type password into focused field
            typeParams.clear();
            typeParams.put("text", password);
            driver.executeScript("mobile:type", typeParams);
            
            // Click login button
            params.clear();
            params.put("label", "Log into my account");
            driver.executeScript("mobile:button-text:click", params);
            
            logger.info("Successfully completed Perfecto visual login using proven method");
            return true;
            
        } catch (Exception e) {
            logger.error("Perfecto visual login failed: " + e.getMessage());
            return false;
        }
    }
    
    // Validation Methods
    public boolean isLoginPageDisplayed() {
        try {
            // Check for username field presence
            WebElement usernameField = findByClassName("XCUIElementTypeTextField");
            WebElement passwordField = findByClassName("XCUIElementTypeSecureTextField");
            
            return usernameField != null && passwordField != null;
        } catch (Exception e) {
            logger.error("Failed to verify login page display: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isLoginSuccessful() {
        try {
            // Wait for navigation away from login page
            Thread.sleep(3000);
            
            // Check if we're no longer on login page
            return !isLoginPageDisplayed();
        } catch (Exception e) {
            logger.error("Failed to verify login success: " + e.getMessage());
            return false;
        }
    }
}
