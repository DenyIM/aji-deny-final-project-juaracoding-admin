package com.juaracoding.magang.pages.admin.deny;

import com.juaracoding.magang.pages.BasePage;
import com.juaracoding.magang.utils.Constants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

/**
 * Page Object Model untuk modul Admin Import (Tester Deny):
 * 1. Import Absen (/imports/import-absen-hadir)
 * 2. Import Status Aktif (/imports/import-status-aktif)
 * 3. Import Struktur User (/imports/import-struktur-users)
 * 4. Import Cuti (/imports/import-absen-cuti)
 */
public class AdminImportPage extends BasePage {

    public static final String URL_IMPORT_ABSEN = Constants.BASE_URL + "/imports/import-absen-hadir";
    public static final String URL_IMPORT_STATUS_AKTIF = Constants.BASE_URL + "/imports/import-status-aktif";
    public static final String URL_IMPORT_STRUKTUR_USER = Constants.BASE_URL + "/imports/import-struktur-users";
    public static final String URL_IMPORT_CUTI = Constants.BASE_URL + "/imports/import-absen-cuti";

    public static final String TEMPLATE_ABSEN_URL = "https://api-dev.dikahadir.com/hadir-api-magang/template/DATA_ABSEN_HADIR.xlsx";
    public static final String TEMPLATE_STATUS_AKTIF_URL = "https://api-dev.dikahadir.com/hadir-api-magang/template/DATA_STATUS_USER.xlsx";
    public static final String TEMPLATE_STRUKTUR_USER_URL = "https://api-dev.dikahadir.com/hadir-api-magang/template/DATA_STRUKTUR_USER.xlsx";
    public static final String TEMPLATE_CUTI_URL = "https://api-dev.dikahadir.com/hadir-api-magang/template/Template_Cuti_Hadir.xlsx";

    private static final By FILE_INPUT = By.cssSelector("input[type='file']");
    private static final By IMPORT_SUBMIT_BTN = By.xpath("//button[@type='submit' and (contains(.,'Import') or contains(.,'Memproses'))]");
    private static final By DOWNLOAD_TEMPLATE_BTN = By.xpath("//button[contains(normalize-space(),'Download Template')]");
    private static final By HELPER_ERROR_TEXT = By.cssSelector("p.MuiFormHelperText-root.Mui-error, .MuiFormHelperText-root");
    private static final By SNACKBAR_OR_ALERT = By.cssSelector("div.MuiSnackbarContent-message, div.MuiAlert-message");
    private static final By CARD_HEADER = By.cssSelector("div.MuiCard-root");
    private static final By BERSIHKAN_LOGS_BTN = By.xpath("//button[contains(normalize-space(),'Bersihkan logs')]");

    public AdminImportPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Membuka URL halaman Import dengan toleransi timeout aset eksternal Next.js.
     */
    public void openImportPage(String url) {
        logger.info("Navigating to Admin Import URL: {}", url);
        try {
            driver.get(url);
        } catch (TimeoutException e) {
            logger.warn("Page load timeout caught for [{}], stopping slow external loads: {}", url, e.getMessage());
            try {
                ((JavascriptExecutor) driver).executeScript("window.stop();");
            } catch (Exception ignored) {
                // Continue if DOM is ready
            }
        }
        waitForImportPageReady();
    }

    public void waitForImportPageReady() {
        waitForVisibility(CARD_HEADER);
        waitForVisibility(IMPORT_SUBMIT_BTN);
        waitForVisibility(DOWNLOAD_TEMPLATE_BTN);
        wait.until(d -> !d.findElements(FILE_INPUT).isEmpty());
    }

    public boolean isDownloadTemplateButtonDisplayed() {
        return isDisplayed(DOWNLOAD_TEMPLATE_BTN);
    }

    public boolean isImportButtonDisplayed() {
        return isDisplayed(IMPORT_SUBMIT_BTN);
    }

    /**
     * Mengklik tombol Download Template lalu memverifikasi endpoint template mengembalikan
     * HTTP 200 dengan signature ZIP/XLSX valid ('PK'), dan membersihkan unduhan lokal bila ada.
     */
    public boolean clickDownloadTemplateAndVerifyEndpoint(String templateUrl) {
        String fileName = templateUrl.substring(templateUrl.lastIndexOf('/') + 1);
        Path downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads");
        Path possibleDownload = downloadsDir.resolve(fileName);
        boolean existedBefore = Files.exists(possibleDownload);

        click(DOWNLOAD_TEMPLATE_BTN);
        sleepQuietly(1500);

        Response res = RestAssured.given()
                .relaxedHTTPSValidation()
                .when()
                .get(templateUrl)
                .then()
                .extract()
                .response();

        byte[] bytes = res.asByteArray();
        boolean validZipSignature = bytes != null && bytes.length > 100 && bytes[0] == 'P' && bytes[1] == 'K';
        String contentType = res.getContentType() != null ? res.getContentType().toLowerCase() : "";
        boolean validStatusAndType = res.getStatusCode() == 200
                && (contentType.contains("spreadsheetml") || contentType.contains("excel") || contentType.contains("octet-stream"));

        if (!existedBefore) {
            try {
                Files.deleteIfExists(possibleDownload);
            } catch (Exception ignored) {
                // Ignore cleanup error if Chrome still holds handle briefly
            }
        }

        logger.info("Template [{}] -> HTTP {}, ContentType={}, bytes={}, validXlsx={}",
                fileName, res.getStatusCode(), contentType, bytes != null ? bytes.length : 0, validZipSignature);
        return validStatusAndType && validZipSignature;
    }

    /**
     * Mengisi input file dengan file uji dari src/test/resources/testdata/.
     */
    public void selectTestDataFile(String fileName) {
        Path filePath = Paths.get(Constants.TESTDATA_DIR, fileName).toAbsolutePath().normalize();
        if (!Files.exists(filePath)) {
            throw new IllegalStateException("Testdata file not found: " + filePath);
        }
        WebElement fileInput = wait.until(d -> d.findElement(FILE_INPUT));
        fileInput.sendKeys(filePath.toString());
        sleepQuietly(600);
    }

    public void clickImportSubmit() {
        click(IMPORT_SUBMIT_BTN);
    }

    /**
     * Mengirim form Import lalu menunggu hasil akhir (Snackbar atau selesai memproses).
     */
    public String submitImportAndWaitResult() {
        clickImportSubmit();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(25)).until(d -> {
                List<WebElement> snacks = d.findElements(SNACKBAR_OR_ALERT);
                boolean hasSnack = !snacks.isEmpty() && !snacks.get(0).getText().trim().isEmpty();
                boolean processing = !d.findElements(By.xpath("//button[contains(.,'Memproses')]")).isEmpty();
                boolean hasHelperErr = !getHelperErrorMessage().isBlank();
                return hasSnack || hasHelperErr || !processing;
            });
        } catch (TimeoutException ignored) {
            // Checked in caller via getSnackbarMessage() / getHelperErrorMessage()
        }
        sleepQuietly(400);
        return getSnackbarMessage();
    }

    public String getHelperErrorMessage() {
        List<WebElement> errs = driver.findElements(HELPER_ERROR_TEXT);
        for (WebElement el : errs) {
            String txt = el.getText().trim();
            if (!txt.isEmpty()) {
                return txt;
            }
        }
        return "";
    }

    public String getSnackbarMessage() {
        List<WebElement> els = driver.findElements(SNACKBAR_OR_ALERT);
        for (WebElement el : els) {
            String txt = el.getText().trim();
            if (!txt.isEmpty()) {
                return txt;
            }
        }
        return "";
    }

    public boolean isImportLeaveLogsDisplayed() {
        return !driver.findElements(BERSIHKAN_LOGS_BTN).isEmpty();
    }

    /**
     * Klik Import tanpa memilih file lalu ambil HTML5 validationMessage pada input[type='file'].
     */
    public String submitEmptyAndGetHtml5ValidationMessage() {
        clickImportSubmit();
        sleepQuietly(400);
        WebElement fileInput = driver.findElement(FILE_INPUT);
        Object msg = ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", fileInput);
        return msg == null ? "" : msg.toString().trim();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

