package com.juaracoding.magang.pages.admin.deny;

import com.juaracoding.magang.pages.BasePage;
import com.juaracoding.magang.utils.Constants;
import com.juaracoding.magang.utils.SecurityUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object Model untuk halaman Login Admin HRD
 * (https://magang.dikahadir.com/authentication/login).
 * Cakupan skenario: SIT_ADM_UI_001 s/d SIT_ADM_UI_012 (Tester Deny).
 */
public class AdminLoginPage extends BasePage {

    @FindBy(css = "input#email, input[name='email'], input[type='email']")
    private WebElement emailInput;

    @FindBy(css = "input#password")
    private WebElement passwordInput;

    @FindBy(css = "div.MuiInputAdornment-root button")
    private WebElement showHidePasswordButton;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    @FindBy(css = "div.MuiAlert-message")
    private WebElement alertMessage;

    @FindBy(xpath = "//header//button[.//div[contains(@class,'MuiAvatar-root')] or .//h5]")
    private WebElement profileMenuButton;

    @FindBy(xpath = "//button[normalize-space()='Logout' or contains(.,'Logout')] | //li[contains(.,'Logout')]")
    private WebElement logoutButton;

    public AdminLoginPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToLoginPage() {
        driver.get(Constants.ADMIN_LOGIN_URL);
        waitForVisibility(emailInput);
        logger.info("Navigated to Admin Login page: {}", Constants.ADMIN_LOGIN_URL);
    }

    public void navigateToProtectedDashboardDirectly() {
        driver.get(Constants.ADMIN_PENDING_URL);
        logger.info("Directly navigated to protected URL: {}", Constants.ADMIN_PENDING_URL);
    }

    public void enterEmail(String email) {
        type(emailInput, email);
    }

    public void enterPassword(String plainPassword) {
        type(passwordInput, plainPassword);
    }

    public void clickLoginButton() {
        click(loginButton);
    }

    public void login(String email, String plainPassword) {
        logger.info("Attempting Admin Login for email: [{}]", email);
        enterEmail(email);
        enterPassword(plainPassword);
        clickLoginButton();
    }

    public void loginWithDefaultAdmin() {
        String decodedPassword = SecurityUtil.decode(Constants.ADMIN_PASSWORD_ENCODED);
        login(Constants.ADMIN_EMAIL, decodedPassword);
    }

    public void clickShowHidePassword() {
        click(showHidePasswordButton);
    }

    public String getPasswordFieldType() {
        return getAttribute(passwordInput, "type");
    }

    public String getEmailValidationMessage() {
        return getAttribute(emailInput, "validationMessage");
    }

    public boolean isEmailFieldDisplayed() {
        return isDisplayed(emailInput);
    }

    public boolean isAlertMessageDisplayed() {
        try {
            WebElement el = waitForVisibility(alertMessage);
            return el.isDisplayed() && !el.getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public String getAlertText() {
        try {
            wait.until(d -> !alertMessage.getText().trim().isEmpty());
            return alertMessage.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean waitForRedirectToPending() {
        return waitForUrlContains("/dashboards/pending");
    }

    public boolean waitForRedirectToLogin() {
        return waitForUrlContains("/authentication/login");
    }

    public boolean isBrowserJsAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    public void performLogout() {
        logger.info("Performing Admin Logout from header profile menu.");
        try {
            click(profileMenuButton);
            click(logoutButton);
        } catch (Exception e) {
            logger.warn("Primary logout locator failed, trying header fallback: {}", e.getMessage());
            WebElement fallbackProfile = waitForClickability(
                    By.cssSelector("header button[aria-haspopup='true'], header button:last-of-type"));
            click(fallbackProfile);
            WebElement fallbackLogout = waitForClickability(
                    By.xpath("//button[contains(.,'Logout') or contains(.,'Keluar')]"));
            click(fallbackLogout);
        }
    }
}

