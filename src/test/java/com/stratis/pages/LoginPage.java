package com.stratis.pages;

import com.stratis.base.BasePage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class LoginPage extends BasePage {
    
    // iOS Page Elements
    @FindBy(className = "XCUIElementTypeTextField")
    private List<WebElement> iOSTextFields;
    
    @FindBy(className = "XCUIElementTypeSecureTextField")
    private List<WebElement> iOSSecureTextFields;
    
    @FindBy(className = "XCUIElementTypeButton")
    private List<WebElement> iOSButtons;
    
    // Android Page Elements
    @FindBy(className = "android.widget.EditText")
    private List<WebElement> androidEditTexts;
    
    @FindBy(className = "android.widget.Button")
    private List<WebElement> androidButtons;
    
    public LoginPage(AppiumDriver driver) {
        super(driver);
    }
    
    // Login Actions
    public boolean fillLoginCredentials(String username, String password) {
        logger.info("Attempting to fill login credentials for platform: " + (isAndroid() ? "Android" : "iOS"));
        
        // Always start in native context
        switchToNative();
        
        // Try platform-specific native approach first
        if (isAndroid()) {
            if (tryFillAndroidNative(username, password)) {
                return true;
            }
        } else {
            if (tryFillIOSNative(username, password)) {
                return true;
            }
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
        logger.info("Attempting to click login button for platform: " + (isAndroid() ? "Android" : "iOS"));
        
        // Try platform-specific native login button click
        if (isAndroid()) {
            if (tryClickAndroidLoginNative()) {
                return true;
            }
        } else {
            if (tryClickIOSLoginNative()) {
                return true;
            }
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
    
    // Android Native Implementation Methods
    private boolean tryFillAndroidNative(String username, String password) {
        try {
            logger.info("Trying Android native login form fill");
            
            // Strategy A: Android-specific XPath locators (from memory)
            WebElement usernameField = pollForElement(
                By.xpath("//android.widget.EditText[contains(@resource-id,'username') or contains(@text,'Username') or contains(@hint,'Username')]"),
                10
            );
            WebElement passwordField = pollForElement(
                By.xpath("//android.widget.EditText[contains(@resource-id,'password') or contains(@text,'Password') or contains(@hint,'Password')]"),
                10
            );
            
            // Strategy B: Generic EditText fallback
            if (usernameField == null || passwordField == null) {
                List<WebElement> editTexts = driver.findElements(By.className("android.widget.EditText"));
                if (editTexts.size() >= 2) {
                    usernameField = editTexts.get(0);
                    passwordField = editTexts.get(1);
                    logger.info("Using generic EditText elements for Android login");
                }
            }
            
            if (usernameField != null && passwordField != null) {
                typeInto(usernameField, username);
                typeInto(passwordField, password);
                logger.info("Android login credentials filled successfully");
                
                // Hide keyboard for Android
                hideKeyboard();
                return true;
            } else {
                logger.warn("Could not find Android username or password fields");
            }
            
        } catch (Exception e) {
            logger.error("Android native login form fill failed: " + e.getMessage());
        }
        return false;
    }
    
    private boolean tryClickAndroidLoginNative() {
        try {
            logger.info("Trying Android native login button click");
            
            // Android login button locators (from memory)
            WebElement loginButton = pollForElement(
                By.xpath("//android.widget.Button[contains(@text,'Login') or contains(@text,'Sign In') or contains(@resource-id,'login')]"),
                8
            );
            
            // Fallback to any button
            if (loginButton == null) {
                List<WebElement> buttons = driver.findElements(By.className("android.widget.Button"));
                if (!buttons.isEmpty()) {
                    loginButton = buttons.get(0);
                    logger.info("Using first available Android button");
                }
            }
            
            if (loginButton != null) {
                loginButton.click();
                logger.info("Successfully clicked Android login button");
                return true;
            } else {
                logger.warn("Could not find Android login button");
            }
            
        } catch (Exception e) {
            logger.error("Android native login button click failed: " + e.getMessage());
        }
        return false;
    }
    
    // iOS Native Implementation Methods - Based on working PerfectoIOSBasicTest
    private boolean tryFillIOSNative(String username, String password) {
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
                logger.info("iOS login credentials filled successfully using proven method");
                
                // Hide keyboard for iOS
                hideKeyboard();
                return true;
            } else {
                logger.warn("Could not find iOS username or password fields");
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
    
    private boolean tryClickIOSLoginNative() {
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
            if (isAndroid()) {
                // Check for Android login fields
                WebElement usernameField = findByClassName("android.widget.EditText");
                return usernameField != null;
            } else {
                // Check for iOS login fields
                WebElement usernameField = findByClassName("XCUIElementTypeTextField");
                WebElement passwordField = findByClassName("XCUIElementTypeSecureTextField");
                return usernameField != null && passwordField != null;
            }
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
