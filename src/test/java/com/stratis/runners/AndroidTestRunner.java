package com.stratis.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.stratis.stepdefinitions"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/android-html-report.html",
        "json:target/cucumber-reports/android-json/Cucumber.json",
        "junit:target/cucumber-reports/android-xml/Cucumber.xml",
        "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
    },
    monochrome = true,
    publish = true,
    tags = "@android and (@smoke or @login)"
)
public class AndroidTestRunner extends AbstractTestNGCucumberTests {
    
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
