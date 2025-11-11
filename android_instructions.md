# Android Test Execution Instructions

## Overview
This document provides step-by-step instructions for running Android automation tests using the Perfecto cloud platform with Appium and TestNG.

## Prerequisites

### 1. Environment Setup
- **Java**: JDK 8 or higher
- **Maven**: 3.6 or higher
- **IDE**: IntelliJ IDEA or Eclipse with TestNG plugin
- **Perfecto Account**: Valid token and device access

### 2. Dependencies
Ensure your `pom.xml` includes the following dependencies:
```xml
<dependency>
    <groupId>io.appium</groupId>
    <artifactId>java-client</artifactId>
    <version>8.5.1</version>
</dependency>
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.8.0</version>
</dependency>
```

## Configuration

### 1. Android Properties File
The Android tests use `perfecto-android.properties` located in `src/test/resources/`:

```properties
perfecto.token=YOUR_PERFECTO_TOKEN_HERE
perfecto.android.device.id=RZ8W80847PR
perfecto.android.model=Galaxy A24 4G
perfecto.android.manufacturer=Samsung
perfecto.android.os.version=13
perfecto.android.app.path=PRIVATE:app-V1.14.10-QA.apk
perfecto.android.app.package=com.stratis.estaffing
```

### 2. Device Configuration
- **Device**: Samsung Galaxy A24 4G
- **Device ID**: RZ8W80847PR
- **Android Version**: 13
- **Automation**: Appium with UiAutomator2

### 3. Application Configuration
- **APK**: `app-V1.14.10-QA.apk` (uploaded to Perfecto as PRIVATE asset)
- **Package**: `com.stratis.estaffing`

## Test Execution Methods

### Method 1: Maven Command Line

#### Run Single Android Test
```bash
mvn clean test -Dtest=PerfectoAndroidBasicTest
```

#### Run with Specific Test Method
```bash
mvn clean test -Dtest=PerfectoAndroidBasicTest#loginFlow
```

#### Run with Maven Profile (if configured)
```bash
mvn clean test -Pandroid
```

### Method 2: TestNG XML Configuration

Create `testng-android.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<suite name="Android Test Suite">
    <test name="Android Login Tests">
        <classes>
            <class name="com.stratis.tests.PerfectoAndroidBasicTest"/>
        </classes>
    </test>
</suite>
```

Run with:
```bash
mvn clean test -DsuiteXmlFile=testng-android.xml
```

mvn clean test "-DsuiteXmlFile=testngAndroid.xml"

mvn clean test "-DsuiteXmlFile=./testngAndroid.xml"

mvn clean test -Dsurefire.suiteXmlFiles=testngAndroid.xml

### Method 3: IDE Execution

#### IntelliJ IDEA
1. Right-click on `PerfectoAndroidBasicTest.java`
2. Select "Run 'PerfectoAndroidBasicTest'"
3. Or right-click on specific test method and run

#### Eclipse
1. Right-click on test class
2. Run As → TestNG Test

## Test Structure

### Element Locators
The Android test uses robust locator strategies:

```java
// Username field
@AndroidFindBy(xpath = "//android.widget.EditText[contains(@resource-id,'username') or contains(@text,'Username') or contains(@hint,'Username')]")
private WebElement usernameField;

// Password field  
@AndroidFindBy(xpath = "//android.widget.EditText[contains(@resource-id,'password') or contains(@text,'Password') or contains(@hint,'Password')]")
private WebElement passwordField;

// Login button
@AndroidFindBy(xpath = "//android.widget.Button[contains(@text,'Login') or contains(@text,'Sign In') or contains(@resource-id,'login')]")
private WebElement loginButton;
```

### Test Flow
1. **Setup**: Initialize AndroidDriver with Perfecto capabilities
2. **Context Switching**: Start in NATIVE_APP context
3. **Element Location**: Multiple fallback strategies:
   - PageFactory with @AndroidFindBy annotations
   - Native Android locators
   - WebView context (if hybrid app)
   - Perfecto visual commands (last resort)
4. **Login Flow**: Enter credentials and tap login
5. **Cleanup**: Capture screenshots and page source

## Debugging and Troubleshooting

### 1. Screenshots and Page Source
Test artifacts are saved to:
- **Screenshots**: `target/screenshots/`
- **Page Source**: `target/pagesource/`

### 2. Common Issues

#### Device Not Available
```
Error: Device RZ8W80847PR is not available
```
**Solution**: Check device availability in Perfecto dashboard or use different device ID

#### App Not Found
```
Error: App PRIVATE:app-V1.14.10-QA.apk not found
```
**Solution**: Verify APK is uploaded to Perfecto repository

#### Element Not Found
```
Error: Could not locate username/password
```
**Solution**: 
- Check page source XML files in `target/pagesource/`
- Verify app is loaded correctly
- Update locators based on actual UI structure

#### Token Issues
```
Error: Perfecto token not configured
```
**Solution**: Update `perfecto-android.properties` with valid token

### 3. Logging
Enable detailed logging by adding to test:
```java
System.setProperty("appium.logging.level", "DEBUG");
```

## Best Practices

### 1. Element Location
- Use multiple locator strategies for robustness
- Prefer resource-id over text-based locators
- Implement polling waits for dynamic elements

### 2. Context Management
- Always start in NATIVE_APP context
- Switch to WEBVIEW only when necessary
- Return to NATIVE_APP after webview operations

### 3. Error Handling
- Capture screenshots on failures
- Save page source for debugging
- Use try-catch blocks for unstable operations

### 4. Test Data
- Use parameterized test data
- Avoid hardcoded credentials in production
- Consider using test data providers

## Reporting

### TestNG Reports
Reports are generated in:
- `target/surefire-reports/`
- `test-output/`

### Perfecto Reports
Access execution reports in Perfecto dashboard:
1. Login to Perfecto
2. Navigate to Reports
3. Filter by script name: "eStratis Android Smoke"

## Continuous Integration

### Jenkins Pipeline Example
```groovy
pipeline {
    agent any
    stages {
        stage('Android Tests') {
            steps {
                sh 'mvn clean test -Dtest=PerfectoAndroidBasicTest'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/screenshots/**,target/pagesource/**'
                }
            }
        }
    }
}
```

## Support and Resources

### Perfecto Documentation
- [Perfecto Mobile Testing](https://developers.perfectomobile.com/)
- [Appium Android Documentation](http://appium.io/docs/en/drivers/android-uiautomator2/)

### Contact
For issues with test execution, contact the QA team or check the project README for additional support information.

---

**Last Updated**: November 2025  
**Version**: 1.0
