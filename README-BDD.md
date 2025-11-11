# eStratis iOS Automation Framework - BDD Enhanced

## Overview
This is an enhanced version of the eStratis iOS automation framework that now supports:
- **Cucumber BDD** for behavior-driven development
- **Extent Reports** for comprehensive test reporting
- **Page Object Model (POM)** for better code organization and maintainability
- **Maven** build management with enhanced dependencies

## Framework Architecture

### Technology Stack
- **Java 11** - Programming language
- **Maven** - Build and dependency management
- **TestNG** - Test execution framework
- **Cucumber 7.18.0** - BDD framework
- **Appium 8.5.1** - Mobile automation
- **Selenium 4.8.3** - WebDriver
- **Extent Reports 5.1.1** - Advanced reporting
- **Log4j2** - Logging framework
- **Perfecto Cloud** - Mobile device cloud platform

### Project Structure
```
perfecto-ios-automation/
├── src/test/java/com/stratis/
│   ├── base/
│   │   ├── DriverManager.java          # Driver initialization and management
│   │   └── BasePage.java               # Base page class with common methods
│   ├── pages/
│   │   └── LoginPage.java              # Login page object model
│   ├── stepdefinitions/
│   │   ├── LoginSteps.java             # Cucumber step definitions
│   │   └── Hooks.java                  # Setup and teardown hooks
│   └── runners/
│       ├── TestRunner.java             # Main Cucumber test runner
│       └── SmokeTestRunner.java        # Smoke test runner
├── src/test/resources/
│   ├── features/
│   │   └── Login.feature               # BDD feature files
│   ├── perfecto.properties             # Configuration file
│   ├── extent.properties               # Extent Reports configuration
│   ├── extent-config.xml               # Extent Reports styling
│   └── log4j2.xml                      # Logging configuration
├── pom.xml                             # Maven dependencies
├── testng-bdd.xml                      # TestNG suite configuration
└── README-BDD.md                       # This documentation
```

## Key Features

### 1. Behavior-Driven Development (BDD)
- **Gherkin syntax** for writing test scenarios in plain English
- **Feature files** describing application behavior
- **Step definitions** linking Gherkin steps to Java code
- **Tags** for organizing and filtering test execution

### 2. Page Object Model (POM)
- **BasePage class** with common functionality
- **Page-specific classes** encapsulating page elements and actions
- **Separation of concerns** between test logic and page interactions
- **Reusable components** across different test scenarios

### 3. Enhanced Reporting
- **Extent Reports** with rich HTML reports
- **Screenshot attachment** for failed scenarios
- **Detailed test execution logs**
- **Multiple report formats** (HTML, JSON, XML)

### 4. Robust Element Detection
- **Multiple locator strategies** with fallback mechanisms
- **Context switching** between Native and WebView
- **Perfecto visual commands** as last resort
- **Polling mechanisms** for element availability

## Configuration

### 1. Perfecto Configuration
Update `src/test/resources/perfecto.properties`:
```properties
perfecto.token=YOUR_PERFECTO_JWT_TOKEN
perfecto.ios.device.id=YOUR_DEVICE_ID
perfecto.ios.device.model=iPhone-17
perfecto.ios.os.version=17.0
perfecto.ios.app.path=PRIVATE:eStratis.ipa
perfecto.ios.bundle.id=com.stratis.estaffing
```

### 2. Extent Reports Configuration
Configure reporting in `src/test/resources/extent.properties`:
```properties
extent.reporter.spark.start=true
extent.reporter.spark.out=target/extent-reports/ExtentReport.html
extent.reporter.spark.config=src/test/resources/extent-config.xml
```

## Running Tests

### 1. Maven Commands

#### Run All Tests
```bash
mvn clean test
```

#### Run Smoke Tests Only
```bash
mvn clean test -Dtest=SmokeTestRunner
```

#### Run with Specific Tags
```bash
mvn clean test -Dcucumber.filter.tags="@smoke"
```

#### Run with TestNG Suite
```bash
mvn clean test -DsuiteXmlFile=testng-bdd.xml
```

### 2. IDE Execution
- Right-click on `TestRunner.java` → Run As → TestNG Test
- Right-click on `testng-bdd.xml` → Run As → TestNG Suite

### 3. Command Line with Cucumber Tags
```bash
# Run only smoke tests
mvn test -Dcucumber.filter.tags="@smoke"

# Run login tests
mvn test -Dcucumber.filter.tags="@login"

# Run positive tests only
mvn test -Dcucumber.filter.tags="@positive"

# Exclude negative tests
mvn test -Dcucumber.filter.tags="not @negative"
```

## Feature Files

### Sample Feature File Structure
```gherkin
Feature: eStratis Login Functionality
  As an eStratis user
  I want to be able to log into the application
  So that I can access the eStratis features

  Background:
    Given the user is on the eStratis login page

  @smoke @login @positive
  Scenario: Successful login with valid credentials
    Given the user has valid eStratis credentials
    When the user enters username "user@example.com" and password "password123"
    And the user clicks the login button
    Then the user should be successfully logged in
```

## Reports and Artifacts

### Generated Reports
```
target/
├── extent-reports/
│   └── ExtentReport.html               # Main Extent Report
├── cucumber-reports/
│   ├── index.html                      # Cucumber HTML Report
│   ├── Cucumber.json                   # JSON Report
│   └── Cucumber.xml                    # XML Report
├── screenshots/                        # Test screenshots
├── pagesource/                         # Page source files
├── logs/
│   └── automation.log                  # Application logs
└── surefire-reports/                   # TestNG reports
```

### Viewing Reports
1. **Extent Report**: Open `target/extent-reports/ExtentReport.html`
2. **Cucumber Report**: Open `target/cucumber-reports/index.html`
3. **TestNG Report**: Open `target/surefire-reports/index.html`

## Adding New Tests

### 1. Create Feature File
```gherkin
# src/test/resources/features/NewFeature.feature
Feature: New Feature
  Scenario: New test scenario
    Given some precondition
    When some action is performed
    Then some result is expected
```

### 2. Create Step Definitions
```java
// src/test/java/com/stratis/stepdefinitions/NewFeatureSteps.java
@Given("some precondition")
public void some_precondition() {
    // Implementation
}
```

### 3. Create Page Object (if needed)
```java
// src/test/java/com/stratis/pages/NewPage.java
public class NewPage extends BasePage {
    public NewPage(IOSDriver driver) {
        super(driver);
    }
    // Page methods
}
```

## Best Practices

### 1. Feature Files
- Use descriptive scenario names
- Keep scenarios focused and atomic
- Use Background for common setup steps
- Apply appropriate tags for test organization

### 2. Step Definitions
- Keep step methods simple and focused
- Use page objects for UI interactions
- Add appropriate logging and assertions
- Handle exceptions gracefully

### 3. Page Objects
- Extend BasePage for common functionality
- Use meaningful method names
- Implement multiple locator strategies
- Add validation methods

### 4. Reporting
- Capture screenshots for important steps
- Add detailed logging for debugging
- Use meaningful test names and descriptions

## Troubleshooting

### Common Issues
1. **Build Errors**: Run `mvn clean compile` to resolve dependency issues
2. **Driver Issues**: Verify Perfecto token and device availability
3. **Element Not Found**: Check page source and update locators
4. **Report Generation**: Ensure extent.properties is correctly configured

### Debug Mode
Enable detailed logging:
```bash
mvn test -Dlog4j.configurationFile=src/test/resources/log4j2.xml -X
```

## Dependencies
All dependencies are managed through Maven. Key dependencies include:
- Cucumber Java & TestNG integration
- Extent Reports with Cucumber adapter
- Appium Java Client
- Selenium WebDriver
- Log4j2 for logging

## Support
For issues and questions:
1. Check the logs in `target/logs/automation.log`
2. Review screenshots and page source in target directories
3. Consult Perfecto documentation for cloud-specific issues
4. Review Cucumber and Extent Reports documentation for framework-specific questions
