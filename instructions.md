# Perfecto iOS Automation - Instructions

## Overview
This project contains automated tests for iOS applications using Appium and TestNG on Perfecto's cloud platform. The tests are designed to run against the eStratis mobile application.

## Prerequisites

### 1. Software Requirements
- **Java 17** (Temurin/Adoptium recommended)
- **Apache Maven 3.6+**
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

#### JDK Setup (Important)
Install Temurin/Adoptium JDK 17 and configure environment variables:
```bash
# Set JAVA_HOME (adjust path as needed)
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17"
setx PATH "%JAVA_HOME%\bin;%PATH%"

# Verify installation
java -version
```
You should see: `openjdk version "17.0.x"`

**Note**: Use JDK 17 to run, but Maven compiles to Java 11 bytecode for compatibility.

### 2. Perfecto Cloud Setup
- Active Perfecto account with cloud access
- Valid Perfecto security token (JWT)
- iOS device allocated in your Perfecto cloud

### 3. Project Dependencies
All dependencies are managed through Maven and defined in `pom.xml`:
- Selenium WebDriver (4.8.3) - Stable version for Perfecto
- Appium Java Client (8.5.1) - Stable version for Perfecto  
- TestNG (7.10.2)
- Apache Commons IO (2.16.1)

**Note**: These specific versions (Selenium 4.8.3 + Appium 8.5.1) are proven stable with Perfecto cloud and avoid element-mapping issues.

## Configuration

### 1. Update Perfecto Token
Edit `src/test/resources/perfecto.properties` and replace the token:
```properties
perfecto.token=YOUR_ACTUAL_PERFECTO_JWT_TOKEN_HERE
```

### 2. Device Configuration
Verify device settings in `perfecto.properties`:
```properties
perfecto.ios.device.id=00008150-000638CC3E98401C
perfecto.ios.device.model=iPhone-17
perfecto.ios.os.version=26.0  # ⚠️ Update to valid iOS version (e.g., 16.0, 17.0)
perfecto.ios.app.path=PRIVATE:eStratis.ipa
perfecto.ios.bundle.id=com.stratis.estaffing
```

**Important**: Update `perfecto.ios.os.version` to a valid iOS version supported by Perfecto.

### 3. App Configuration
Ensure your iOS app (eStratis.ipa) is uploaded to Perfecto cloud under the `PRIVATE:` repository.

## Running the Tests

### Method 1: Maven Command Line (Recommended)

#### Run All Tests
```bash
cd C:\perfecto-ios-automation
mvn clean test
```

#### Run Specific Test Class
```bash
mvn test -Dtest=PerfectoIOSBasicTest
```

#### Run with TestNG Suite
```bash
mvn test -DsuiteXmlFile=testng.xml
```

#### Clean Cache and Rebuild (Recommended after version changes)
```bash
# Clean local Maven repository cache
mvn -U -q dependency:purge-local-repository

# Clean and rebuild with TestNG suite
mvn -q clean test -DsuiteXmlFile=testng.xml
```

#### Compile Only (for testing compilation)
```bash
mvn clean compile test-compile
```

### Method 2: IDE Execution

#### IntelliJ IDEA / Eclipse
1. Right-click on `PerfectoIOSBasicTest.java`
2. Select **Run As** → **TestNG Test**

#### Run TestNG Suite
1. Right-click on `testng.xml`
2. Select **Run As** → **TestNG Suite**

### Method 3: Command Line with Java
```bash
# Compile first
mvn clean compile test-compile

# Run with TestNG
java -cp "target/test-classes:target/dependency/*" org.testng.TestNG testng.xml
```

## Test Execution Flow

### What Happens During Test Run:
1. **Setup Phase** (`@BeforeClass`)
   - Loads configuration from `perfecto.properties`
   - Validates Perfecto token
   - Establishes connection to Perfecto cloud
   - Initializes iOS driver with specified capabilities

2. **Test Execution** (`@Test`)
   - Activates the eStratis app
   - Attempts to find login button (placeholder test)
   - Captures mid-test screenshot

3. **Cleanup Phase** (`@AfterMethod` & `@AfterClass`)
   - Captures final screenshot (PASS/FAIL)
   - Saves page source for debugging
   - Closes driver connection

## Output and Reports

### Generated Artifacts
```
target/
├── screenshots/              # Test screenshots with timestamps
│   ├── mid-test-YYYYMMDD-HHMMSS.png
│   └── PASS-sampleFlow-YYYYMMDD-HHMMSS.png
├── pagesource/              # XML page source files
│   └── PASS-sampleFlow-YYYYMMDD-HHMMSS.xml
├── surefire-reports/        # Maven Surefire test reports
│   ├── TEST-com.stratis.tests.PerfectoIOSBasicTest.xml
│   └── com.stratis.tests.PerfectoIOSBasicTest.txt
└── test-classes/            # Compiled test classes
```

### TestNG Reports
- **HTML Report**: `target/surefire-reports/index.html`
- **XML Report**: `target/surefire-reports/testng-results.xml`

## Troubleshooting

### Common Issues

#### 1. Compilation Errors
```bash
# Clean and recompile
mvn clean compile test-compile
```

#### 2. Token Authentication Failure
- Verify token is not expired
- Check token format in properties file
- Ensure no extra spaces or characters

#### 3. Device Not Available
- Check device ID exists in your Perfecto cloud
- Verify device is not in use by another session
- Update device model/OS version if needed

#### 4. App Not Found
- Ensure `eStratis.ipa` is uploaded to Perfecto
- Verify app path: `PRIVATE:eStratis.ipa`
- Check bundle ID matches the app

#### 5. Network/Connection Issues
```bash
# Test with verbose logging
mvn test -X
```

### Debug Mode
Enable detailed logging by adding to test execution:
```bash
mvn test -Dlog4j.configuration=file:log4j.properties -X
```

## Security Best Practices

### 1. Token Management
- **Never commit** actual tokens to version control
- Use environment variables for CI/CD:
  ```bash
  export PERFECTO_TOKEN="your-token-here"
  ```
- Consider using encrypted properties for production

### 2. Git Ignore
Add to `.gitignore`:
```
# Perfecto credentials
src/test/resources/perfecto-local.properties
*.token
```

## Extending the Tests

### Adding New Test Methods
```java
@Test
public void loginTest() throws IOException {
    // Your test logic here
    captureScreenshot("login-test");
}
```

### Page Object Model
Consider implementing Page Object Model for better maintainability:
```java
public class LoginPage {
    private IOSDriver driver;
    
    @FindBy(id = "login_button")
    private WebElement loginButton;
    
    // Page methods here
}
```

## Support and Resources

### Perfecto Documentation
- [Perfecto Developer Guide](https://developers.perfectomobile.com/)
- [Appium iOS Testing](https://appium.io/docs/en/drivers/ios-xcuitest/)

### Project Structure
```
perfecto-ios-automation/
├── src/test/java/com/stratis/tests/
│   └── PerfectoIOSBasicTest.java
├── src/test/resources/
│   └── perfecto.properties
├── pom.xml
├── testng.xml
└── instructions.md
```

---

**Ready to test!** Execute `mvn clean test` to start your iOS automation journey on Perfecto cloud.



mvn clean test -Dtest=SmokeTestRunner -Dcucumber.filter.tags="@android"

mvn clean test -Dtest=SmokeTestRunner
