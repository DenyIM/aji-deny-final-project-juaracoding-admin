package com.juaracoding.magang.tests.admin.deny;

import com.juaracoding.magang.BaseTest;
import com.juaracoding.magang.pages.admin.deny.AdminLoginPage;
import com.juaracoding.magang.reports.ExtentTestManager;
import com.juaracoding.magang.utils.Constants;
import com.juaracoding.magang.utils.SecurityUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Pengujian Web UI Selenium untuk fitur Autentikasi & Keamanan Sesi Admin HRD.
 * Cakupan skenario: SIT_ADM_UI_001 s/d SIT_ADM_UI_012 (12 Test Cases).
 * Penanggung jawab: Tester Deny | Branch: feature/deny-admin-login.
 */
public class AdminLoginTest extends BaseTest {

    private AdminLoginPage openLoginPage() {
        AdminLoginPage loginPage = new AdminLoginPage(getDriver());
        loginPage.navigateToLoginPage();
        return loginPage;
    }

    @Test(priority = 1, description = "SIT_ADM_UI_001: Login Admin HRD dengan kredensial valid (admin@hadir.com)",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_001_ValidAdminLogin() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_001 - Login menggunakan email Admin valid & password hasil decode Base64.");
        loginPage.loginWithDefaultAdmin();

        boolean redirected = loginPage.waitForRedirectToPending();
        String currentUrl = loginPage.getCurrentUrl();
        ExtentTestManager.logInfo("Current URL setelah submit login: " + currentUrl);

        Assert.assertTrue(redirected || currentUrl.contains("/dashboards/"),
                "Admin harus berhasil login dan diarahkan ke /dashboards/pending. URL aktual: " + currentUrl
                        + (loginPage.isAlertMessageDisplayed() ? " | Alert: " + loginPage.getAlertText() : ""));
    }

    @Test(priority = 2, description = "SIT_ADM_UI_002: Login Admin HRD dengan password salah",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_002_InvalidPassword() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_002 - Menguji penolakan login Admin dengan password salah.");
        loginPage.login(Constants.ADMIN_EMAIL, "WrongPass123!");

        boolean alertShown = loginPage.isAlertMessageDisplayed();
        String alertText = loginPage.getAlertText();
        ExtentTestManager.logInfo("Alert error tampil: " + alertText);

        Assert.assertTrue(alertShown, "Alert error harus tampil saat password salah.");
        Assert.assertTrue(loginPage.getCurrentUrl().contains("/authentication/login"),
                "Halaman harus tetap berada di /authentication/login.");
    }

    @Test(priority = 3, description = "SIT_ADM_UI_003: Login Admin HRD dengan email tidak terdaftar",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_003_UnregisteredEmail() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_003 - Menguji penolakan login Admin dengan email tidak terdaftar.");
        loginPage.login("unknown@hadir.com", "AnyPass123!");

        boolean alertShown = loginPage.isAlertMessageDisplayed();
        String alertText = loginPage.getAlertText();
        ExtentTestManager.logInfo("Alert error tampil: " + alertText);

        Assert.assertTrue(alertShown, "Alert error harus tampil untuk email tidak terdaftar.");
        Assert.assertFalse(loginPage.getCurrentUrl().contains("/dashboards/"),
                "Akun tidak terdaftar tidak boleh redirect ke dashboard.");
    }

    @Test(priority = 4, description = "SIT_ADM_UI_004: Login Admin HRD dengan format email tanpa karakter @",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_004_InvalidEmailFormatWithoutAt() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_004 - Menguji validasi HTML5 format email tanpa '@'.");
        loginPage.login("adminhadir.com", "AnyPass123!");

        String validationMsg = loginPage.getEmailValidationMessage();
        ExtentTestManager.logInfo("HTML5 validationMessage: " + validationMsg);

        Assert.assertTrue(validationMsg != null && !validationMsg.isEmpty(),
                "Browser harus memunculkan validasi HTML5 untuk format email tanpa '@'.");
        Assert.assertTrue(loginPage.getCurrentUrl().contains("/authentication/login"),
                "Form tidak boleh terkirim dan tetap di /authentication/login.");
    }

    @Test(priority = 5, description = "SIT_ADM_UI_005: Login Admin HRD dengan field email dikosongkan",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_005_EmptyEmailField() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_005 - Menguji login dengan field email kosong.");
        loginPage.login("", SecurityUtil.decode(Constants.ADMIN_PASSWORD_ENCODED));

        String currentUrl = loginPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL setelah submit email kosong: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("/authentication/login"),
                "Admin harus tetap tertahan di halaman /authentication/login ketika email kosong.");
    }

    @Test(priority = 6, description = "SIT_ADM_UI_006: Login Admin HRD dengan field password dikosongkan",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_006_EmptyPasswordField() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_006 - Menguji login dengan field password kosong.");
        loginPage.login(Constants.ADMIN_EMAIL, "");

        String currentUrl = loginPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL setelah submit password kosong: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("/authentication/login"),
                "Admin harus tetap tertahan di halaman /authentication/login ketika password kosong.");
    }

    @Test(priority = 7, description = "SIT_ADM_UI_007: Login Admin HRD dengan seluruh field kosong",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_007_BothFieldsEmpty() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_007 - Menguji submit form dengan email & password kosong.");
        loginPage.login("", "");

        String currentUrl = loginPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL setelah submit form kosong: " + currentUrl);

        Assert.assertTrue(currentUrl.contains("/authentication/login"),
                "Form kosong tidak boleh mengizinkan akses ke dashboard.");
    }

    @Test(priority = 8, description = "SIT_ADM_UI_008: Verifikasi fungsi tombol ikon mata (Show/Hide Password)",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_008_ShowHidePasswordToggle() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_008 - Menguji toggle ikon mata pada field password.");
        loginPage.enterPassword("SecretCheck123!");

        Assert.assertEquals(loginPage.getPasswordFieldType(), "password",
                "Default tipe field password harus 'password'.");

        loginPage.clickShowHidePassword();
        String shownType = loginPage.getPasswordFieldType();
        ExtentTestManager.logInfo("Tipe field setelah klik ikon mata pertama: " + shownType);
        Assert.assertEquals(shownType, "text",
                "Setelah ikon mata diklik, tipe field harus berubah menjadi 'text'.");

        loginPage.clickShowHidePassword();
        String hiddenType = loginPage.getPasswordFieldType();
        ExtentTestManager.logInfo("Tipe field setelah klik ikon mata kedua: " + hiddenType);
        Assert.assertEquals(hiddenType, "password",
                "Setelah ikon mata diklik ulang, tipe field harus kembali ke 'password'.");
    }

    @Test(priority = 9, description = "SIT_ADM_UI_009: Uji keamanan SQL Injection pada field email Login Admin",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_009_SqlInjectionOnEmail() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_009 - Menguji ketahanan form login terhadap SQL Injection payload.");
        loginPage.login("' OR '1'='1' --", "Anything123!");

        String currentUrl = loginPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL setelah percobaan SQL Injection: " + currentUrl);

        Assert.assertFalse(currentUrl.contains("/dashboards/"),
                "Payload SQL Injection tidak boleh berhasil masuk ke dashboard.");
        Assert.assertTrue(currentUrl.contains("/authentication/login"),
                "Halaman harus tetap tertahan di /authentication/login.");
    }

    @Test(priority = 10, description = "SIT_ADM_UI_010: Uji keamanan XSS Injection pada field email Login Admin",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_010_XssInjectionOnEmail() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_010 - Menguji ketahanan form login terhadap XSS script payload.");
        loginPage.login("<script>alert('XSS')</script>", "Anything123!");

        Assert.assertFalse(loginPage.isBrowserJsAlertPresent(),
                "Script XSS tidak boleh mengeksekusi JS alert() di browser.");
        String currentUrl = loginPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL setelah percobaan XSS Injection: " + currentUrl);

        Assert.assertFalse(currentUrl.contains("/dashboards/"),
                "Payload XSS tidak boleh mengizinkan login ke dashboard.");
        Assert.assertTrue(currentUrl.contains("/authentication/login"),
                "Halaman harus tetap aman di /authentication/login.");
    }

    @Test(priority = 11, description = "SIT_ADM_UI_011: Akses langsung URL /dashboards/pending tanpa login",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_011_UnauthenticatedDirectAccess() {
        AdminLoginPage loginPage = new AdminLoginPage(getDriver());
        ExtentTestManager.logInfo("SIT_ADM_UI_011 - Mengakses langsung /dashboards/pending tanpa sesi login.");
        loginPage.navigateToProtectedDashboardDirectly();

        boolean redirectedToLogin = loginPage.waitForRedirectToLogin();
        String currentUrl = loginPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL hasil proteksi route tanpa sesi: " + currentUrl);

        Assert.assertTrue(redirectedToLogin || loginPage.isEmailFieldDisplayed(),
                "Akses tanpa sesi wajib diarahkan kembali ke /authentication/login. URL aktual: " + currentUrl);
    }

    @Test(priority = 12, description = "SIT_ADM_UI_012: Logout Admin HRD dan verifikasi penghapusan session",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_012_AdminLogoutAndSessionClear() {
        AdminLoginPage loginPage = openLoginPage();
        ExtentTestManager.logInfo("SIT_ADM_UI_012 - Login sebagai Admin lalu melakukan proses Logout.");
        loginPage.loginWithDefaultAdmin();

        boolean loggedIn = loginPage.waitForRedirectToPending();
        Assert.assertTrue(loggedIn, "Prasyarat login Admin harus berhasil masuk ke /dashboards/pending. URL aktual: "
                + loginPage.getCurrentUrl()
                + (loginPage.isAlertMessageDisplayed() ? " | Alert: " + loginPage.getAlertText() : ""));

        loginPage.performLogout();
        boolean backToLogin = loginPage.waitForRedirectToLogin();
        String urlAfterLogout = loginPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL setelah Logout: " + urlAfterLogout);

        Assert.assertTrue(backToLogin || urlAfterLogout.contains("/authentication/login"),
                "Setelah logout, Admin harus kembali ke /authentication/login.");
    }
}

