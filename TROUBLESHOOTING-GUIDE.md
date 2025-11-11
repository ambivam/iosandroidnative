# 🚨 Troubleshooting Guide - Perfecto iOS/Android Automation

## 🔍 **Current Issue Analysis**

### **Primary Issue: Video Stream Validation Failed**
```
SessionNotCreatedException: Failed to execute command handset open: validateVideo command failed. 
Reason: handset server: Failed to validate video stream
```

## 🛠️ **Solutions Applied**

### ✅ **Fixed Issues**
1. **iOS Version**: Corrected from `26.0` to `17.0` in `perfecto.properties`
2. **SLF4J Logging**: Updated to `log4j-slf4j2-impl` for proper SLF4J 2.x compatibility
3. **NullPointerException**: Added null checks in `captureScreenshot()` and `savePageSource()` methods
4. **Log Configuration**: Added `log4j2-test.xml` for proper logging setup

### 🔧 **Device/Video Stream Issue Solutions**

#### **Solution 1: Check Device Availability**
```bash
# Check if device is available in Perfecto cloud
# Login to Perfecto dashboard and verify:
# - Device ID: 00008150-000638CC3E98401C
# - Device Status: Available (not in use)
# - Device Model: iPhone-17 (verify this exists)
```

#### **Solution 2: Update Device Configuration**
Try alternative device configurations in `perfecto.properties`:

**Option A: Use Different iPhone Model**
```properties
perfecto.ios.device.id=DIFFERENT_DEVICE_ID
perfecto.ios.device.model=iPhone-15
perfecto.ios.os.version=16.0
```

**Option B: Use Device Selection by Model (Let Perfecto choose)**
```properties
# Comment out specific device ID and let Perfecto select
# perfecto.ios.device.id=00008150-000638CC3E98401C
perfecto.ios.device.model=iPhone.*
perfecto.ios.os.version=17.*
```

#### **Solution 3: Add Video Stream Capabilities**
Update `PerfectoIOSBasicTest.java` to disable video if causing issues:

```java
// Add these capabilities in setUp() method
caps.setCapability("perfecto:options.enableVideo", false);
caps.setCapability("perfecto:options.enableAudio", false);
```

#### **Solution 4: Network/Firewall Check**
```bash
# Test connectivity to Perfecto cloud
ping stratis-1.perfectomobile.com

# Check if corporate firewall is blocking video streams
# Contact IT to whitelist Perfecto domains
```

## 🎯 **Immediate Action Plan**

### **Step 1: Try Android Test First**
Since Android configuration looks good, test Android first:
```bash
mvn clean test -Dtest=PerfectoAndroidBasicTest
```

### **Step 2: Update iOS Device Configuration**
If iOS continues to fail, try these device configurations:

**Configuration A: Generic iPhone Selection**
```properties
perfecto.ios.device.model=iPhone.*
perfecto.ios.os.version=16.*
# perfecto.ios.device.id=  # Comment out to let Perfecto choose
```

**Configuration B: Specific Alternative Device**
```properties
perfecto.ios.device.id=ALTERNATIVE_DEVICE_ID
perfecto.ios.device.model=iPhone-15
perfecto.ios.os.version=16.0
```

### **Step 3: Disable Video Streaming**
Add to iOS test setup if video stream is the issue:
```java
Map<String, Object> perfectoOptions = new HashMap<>();
perfectoOptions.put("securityToken", TOKEN);
perfectoOptions.put("scriptName", "eStratis Smoke");
perfectoOptions.put("description", "Java/TestNG/Appium iOS on Perfecto");
perfectoOptions.put("enableVideo", false);  // Disable video
perfectoOptions.put("enableAudio", false);  // Disable audio
caps.setCapability("perfecto:options", perfectoOptions);
```

## 📋 **Verification Steps**

### **1. Check Perfecto Cloud Dashboard**
- Login to Perfecto dashboard
- Navigate to "Devices" section
- Verify device `00008150-000638CC3E98401C` status
- Check if device supports iOS 17.0

### **2. Test Token Validity**
```bash
# Token appears valid (issued recently)
# But verify in Perfecto dashboard under "Settings" > "Security Tokens"
```

### **3. Test Network Connectivity**
```bash
# Test basic connectivity
curl -I https://stratis-1.perfectomobile.com

# Test with authentication
curl -H "Authorization: Bearer YOUR_TOKEN" https://stratis-1.perfectomobile.com/nexperience/perfectomobile/wd/hub/status
```

## 🚀 **Alternative Execution Strategies**

### **Strategy 1: Use BDD Framework Instead**
```bash
# Try BDD tests which might have better error handling
mvn clean test -Dtest=SmokeTestRunner
```

### **Strategy 2: Use Android for Initial Testing**
```bash
# Android device configuration looks good
mvn clean test -Dtest=PerfectoAndroidBasicTest
```

### **Strategy 3: Local Appium Server (if available)**
```bash
# If you have local iOS simulator setup
# Update hub URL to local Appium server
```

## 📞 **Support Escalation**

### **Contact Perfecto Support If:**
1. Device appears available but video stream fails consistently
2. Multiple devices show same video stream error
3. Network connectivity tests pass but sessions fail

### **Information to Provide:**
- Account: stratis-1.perfectomobile.com
- Device ID: 00008150-000638CC3E98401C
- Error: "validateVideo command failed"
- Timestamp: Recent execution attempts
- Token: Last 4 characters of JWT token

## 🎉 **Success Indicators**

### **Test Should Pass When:**
- Device connects successfully
- App launches (eStratis.ipa)
- Screenshots are captured
- Login flow executes
- Reports are generated

### **Expected Output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

**Next Steps**: Try the Android test first, then apply iOS device configuration changes based on results.
