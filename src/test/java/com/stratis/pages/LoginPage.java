package com.stratis.pages;

import com.stratis.base.BasePage;
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
    
    // Native Implementation Methods
    private boolean tryFillNative(String username, String password) {
        try {
            logger.info("Trying native login form fill");
            
            // Strategy A: Find by placeholder text
            WebElement usernameField = findByiOSClassChain(
                "**/XCUIElementTypeTextField[`value CONTAINS[c] 'Please enter your username' OR value CONTAINS[c] 'username'`]"
            );
            
            WebElement passwordField = findByiOSClassChain(
                "**/XCUIElementTypeSecureTextField[`value CONTAINS[c] 'Please enter your password' OR value CONTAINS[c] 'password'`]"
            );
            
            // Strategy B: Fallback to first available fields
            if (usernameField == null && !textFields.isEmpty()) {
                usernameField = textFields.get(0);
            }
            
            if (passwordField == null && !secureTextFields.isEmpty()) {
                passwordField = secureTextFields.get(0);
            }
            
            if (usernameField != null && passwordField != null) {
                safeType(usernameField, username);
                safeType(passwordField, password);
                hideKeyboard();
                logger.info("Successfully filled native login form");
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Native login form fill failed: " + e.getMessage());
        }
        return false;
    }
    
    private boolean tryClickLoginNative() {
        try {
            logger.info("Trying native login button click");
            
            // Strategy A: Exact text match
            WebElement loginButton = findByiOSNsPredicate(
                "(type == 'XCUIElementTypeButton' OR type == 'XCUIElementTypeOther') AND " +
                "(label == 'Log into my account' OR name == 'Log into my account')"
            );
            
            // Strategy B: Contains text match
            if (loginButton == null) {
                loginButton = findByiOSNsPredicate(
                    "(type == 'XCUIElementTypeButton' OR type == 'XCUIElementTypeOther') AND " +
                    "(label CONTAINS[c] 'Log into' OR name CONTAINS[c] 'Log into')"
                );
            }
            
            // Strategy C: Generic button fallback
            if (loginButton == null && !buttons.isEmpty()) {
                for (WebElement button : buttons) {
                    String text = button.getAttribute("label");
                    if (text != null && (text.toLowerCase().contains("log") || text.toLowerCase().contains("sign"))) {
                        loginButton = button;
                        break;
                    }
                }
            }
            
            if (loginButton != null) {
                safeClick(loginButton);
                logger.info("Successfully clicked native login button");
                return true;
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
    
    // Perfecto Visual Methods
    private boolean tryPerfectoVisualLogin(String username, String password) {
        try {
            logger.info("Trying Perfecto visual login");
            
            // Click username field
            if (!tryPerfectoVisualClick("Username")) {
                return false;
            }
            
            // Type username
            if (!tryPerfectoVisualType(username)) {
                return false;
            }
            
            // Click password field
            if (!tryPerfectoVisualClick("Password")) {
                return false;
            }
            
            // Type password
            if (!tryPerfectoVisualType(password)) {
                return false;
            }
            
            logger.info("Successfully filled login using Perfecto visual");
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
