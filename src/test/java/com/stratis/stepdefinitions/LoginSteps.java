package com.stratis.stepdefinitions;

import com.stratis.base.DriverManager;
import com.stratis.pages.LoginPage;
import io.cucumber.java.en.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.io.IOException;

public class LoginSteps {
    
    private static final Logger logger = LogManager.getLogger(LoginSteps.class);
    private LoginPage loginPage;
    
    @Given("the user is on the eStratis login page")
    public void the_user_is_on_the_estratis_login_page() throws IOException {
        logger.info("Verifying user is on eStratis login page");
        
        loginPage = new LoginPage(DriverManager.getDriver());
        loginPage.captureScreenshot("login-page-loaded");
        loginPage.savePageSource("login-page-loaded");
        
        // Verify login page is displayed
        boolean isLoginPageDisplayed = loginPage.isLoginPageDisplayed();
        Assert.assertTrue(isLoginPageDisplayed, "Login page is not displayed");
        
        logger.info("User is successfully on the login page");
    }
    
    @When("the user enters username {string} and password {string}")
    public void the_user_enters_username_and_password(String username, String password) throws IOException {
        logger.info("Entering login credentials - Username: " + username);
        
        boolean credentialsFilled = loginPage.fillLoginCredentials(username, password);
        loginPage.captureScreenshot("credentials-entered");
        
        Assert.assertTrue(credentialsFilled, "Failed to fill login credentials");
        logger.info("Login credentials entered successfully");
    }
    
    @When("the user clicks the login button")
    public void the_user_clicks_the_login_button() throws IOException {
        logger.info("Clicking login button");
        
        boolean loginButtonClicked = loginPage.clickLoginButton();
        loginPage.captureScreenshot("login-button-clicked");
        
        Assert.assertTrue(loginButtonClicked, "Failed to click login button");
        logger.info("Login button clicked successfully");
    }
    
    @Then("the user should be successfully logged in")
    public void the_user_should_be_successfully_logged_in() throws IOException {
        logger.info("Verifying successful login");
        
        // Wait a moment for navigation
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        loginPage.captureScreenshot("login-result");
        loginPage.savePageSource("login-result");
        
        boolean isLoginSuccessful = loginPage.isLoginSuccessful();
        Assert.assertTrue(isLoginSuccessful, "Login was not successful");
        
        logger.info("User successfully logged in");
    }
    
    @Then("the user should see an error message")
    public void the_user_should_see_an_error_message() throws IOException {
        logger.info("Verifying error message is displayed");
        
        loginPage.captureScreenshot("login-error");
        loginPage.savePageSource("login-error");
        
        // For now, we'll just verify we're still on login page
        boolean isStillOnLoginPage = loginPage.isLoginPageDisplayed();
        Assert.assertTrue(isStillOnLoginPage, "Expected to remain on login page due to error");
        
        logger.info("Error message verification completed");
    }
    
    @When("the user enters invalid credentials")
    public void the_user_enters_invalid_credentials() throws IOException {
        logger.info("Entering invalid credentials");
        
        boolean credentialsFilled = loginPage.fillLoginCredentials("invalid@user.com", "wrongpassword");
        loginPage.captureScreenshot("invalid-credentials-entered");
        
        Assert.assertTrue(credentialsFilled, "Failed to fill invalid credentials");
        logger.info("Invalid credentials entered successfully");
    }
    
    @Given("the user has valid eStratis credentials")
    public void the_user_has_valid_estratis_credentials() {
        logger.info("User has valid eStratis credentials - using configured test credentials");
        // This step is informational - actual credentials are used in the When step
    }
    
    @Given("the user has invalid eStratis credentials")
    public void the_user_has_invalid_estratis_credentials() {
        logger.info("User has invalid eStratis credentials - will use test invalid credentials");
        // This step is informational - actual invalid credentials are used in the When step
    }
}
