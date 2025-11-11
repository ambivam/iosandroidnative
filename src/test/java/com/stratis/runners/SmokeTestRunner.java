package com.stratis.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.stratis.stepdefinitions"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/smoke",
        "json:target/cucumber-reports/smoke/Cucumber.json",
        "junit:target/cucumber-reports/smoke/Cucumber.xml",
        "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
    },
    monochrome = true,
    publish = true,
    tags = "@smoke"
)
public class SmokeTestRunner extends AbstractTestNGCucumberTests {
    
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
