package com.juaracoding.magang.pages;

import com.juaracoding.magang.utils.Constants;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Kelas induk abstrak untuk seluruh Page Object Model (POM) Admin.
 * Menyediakan explicit wait wrappers dan interaksi aman untuk komponen React/MUI.
 */
public abstract class BasePage {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Constants.EXPLICIT_WAIT_TIMEOUT));
        PageFactory.initElements(driver, this);
    }

    protected WebElement waitForVisibility(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickability(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected WebElement waitForClickability(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean waitForInvisibility(By locator) {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean waitForUrlContains(String urlFraction) {
        try {
            return wait.until(ExpectedConditions.urlContains(urlFraction));
        } catch (Exception e) {
            return getCurrentUrl().contains(urlFraction);
        }
    }

    protected void click(WebElement element) {
        try {
            waitForClickability(element).click();
        } catch (Exception e) {
            logger.warn("Standard click failed, falling back to JS click: {}", e.getMessage());
            scrollToElement(element);
            clickWithJS(element);
        }
    }

    protected void click(By locator) {
        WebElement element = waitForClickability(locator);
        click(element);
    }

    protected void clickWithJS(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void type(WebElement element, String text) {
        WebElement el = waitForVisibility(element);
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        el.clear();
        if (text != null && !text.isEmpty()) {
            el.sendKeys(text);
        }
    }

    protected void type(By locator, String text) {
        type(waitForVisibility(locator), text);
    }

    protected String getText(WebElement element) {
        return waitForVisibility(element).getText().trim();
    }

    protected String getText(By locator) {
        return waitForVisibility(locator).getText().trim();
    }

    protected String getAttribute(WebElement element, String attribute) {
        return waitForVisibility(element).getAttribute(attribute);
    }

    protected boolean isDisplayed(WebElement element) {
        try {
            return waitForVisibility(element).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isDisplayed(By locator) {
        try {
            return waitForVisibility(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'instant', block: 'center'});", element);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }
}

