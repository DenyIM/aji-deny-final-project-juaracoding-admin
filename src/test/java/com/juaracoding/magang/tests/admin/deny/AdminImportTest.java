package com.juaracoding.magang.tests.admin.deny;

import com.juaracoding.magang.BaseTest;
import com.juaracoding.magang.pages.admin.deny.AdminImportPage;
import com.juaracoding.magang.pages.admin.deny.AdminLoginPage;
import com.juaracoding.magang.reports.ExtentTestManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Pengujian Web UI Selenium untuk modul Admin Import (Tester Deny).
 * Cakupan SIT v3: SIT_ADM_UI_154 s/d SIT_ADM_UI_169 (16 Test Cases pada 4 sub-menu Import).
 * Penanggung jawab: Tester Deny | Branch: feature/deny-admin-import.
 */
public class AdminImportTest extends BaseTest {

    private AdminImportPage loginAndOpenImport(String importUrl) {
        AdminLoginPage loginPage = new AdminLoginPage(getDriver());
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                loginPage.navigateToLoginPage();
                loginPage.loginWithDefaultAdmin();
                if (loginPage.waitForRedirectToPending()) {
                    AdminImportPage importPage = new AdminImportPage(getDriver());
                    importPage.openImportPage(importUrl);
                    return importPage;
                }
            } catch (Exception e) {
                logger.warn("Login attempt {} failed: {}", attempt, e.getMessage());
            }
        }
        Assert.fail("Prasyarat login Admin gagal sebelum membuka halaman Import: " + importUrl);
        return null;
    }

    private void assertDownloadTemplateFlow(String importUrl, String templateUrl, String menuName) {
        AdminImportPage page = loginAndOpenImport(importUrl);
        Assert.assertTrue(page.isDownloadTemplateButtonDisplayed(), menuName + ": Tombol Download Template harus tampil.");
        Assert.assertTrue(page.isImportButtonDisplayed(), menuName + ": Tombol Import harus tampil.");

        boolean downloaded = page.clickDownloadTemplateAndVerifyEndpoint(templateUrl);
        Assert.assertTrue(downloaded, menuName + ": Template Excel resmi (" + templateUrl + ") harus berhasil terunduh (HTTP 200, .xlsx valid).");
    }

    private void assertValidExcelUploadFlow(String importUrl, String fixtureName, String menuName) {
        AdminImportPage page = loginAndOpenImport(importUrl);
        page.selectTestDataFile(fixtureName);

        String helperError = page.getHelperErrorMessage();
        Assert.assertTrue(helperError.isEmpty(),
                menuName + ": File valid " + fixtureName + " tidak boleh memicu error ekstensi. Aktual helper: " + helperError);

        String snackbar = page.submitImportAndWaitResult();
        ExtentTestManager.logInfo(menuName + " - Snackbar setelah upload [" + fixtureName + "]: " + snackbar);

        boolean hasSuccessSignal = snackbar.toLowerCase().contains("berhasil")
                || page.isImportLeaveLogsDisplayed();
        Assert.assertTrue(hasSuccessSignal,
                menuName + ": Upload file valid [" + fixtureName + "] harus diterima/berhasil diproses oleh sistem. Pesan aktual: [" + snackbar + "]");
    }

    private void assertInvalidFileRejectedFlow(String importUrl, String invalidFileName, String menuName) {
        AdminImportPage page = loginAndOpenImport(importUrl);
        page.selectTestDataFile(invalidFileName);

        String helperError = page.getHelperErrorMessage();
        ExtentTestManager.logInfo(menuName + " - Helper error setelah pilih [" + invalidFileName + "]: " + helperError);

        page.clickImportSubmit();
        String snackbar = page.getSnackbarMessage();
        ExtentTestManager.logInfo(menuName + " - Snackbar setelah submit non-Excel: " + snackbar);

        boolean rejectedByClient = helperError.toLowerCase().contains("excel") || helperError.toLowerCase().contains(".xlsx");
        boolean rejectedOnSubmit = !snackbar.toLowerCase().contains("berhasil");
        Assert.assertTrue(rejectedByClient && rejectedOnSubmit,
                menuName + ": File non-Excel [" + invalidFileName + "] wajib ditolak. Helper=[" + helperError + "], Snackbar=[" + snackbar + "]");
    }

    private void assertEmptyFileValidationFlow(String importUrl, String menuName) {
        AdminImportPage page = loginAndOpenImport(importUrl);
        String validationMsg = page.submitEmptyAndGetHtml5ValidationMessage();
        String helperError = page.getHelperErrorMessage();
        String snackbar = page.getSnackbarMessage();
        ExtentTestManager.logInfo(menuName + " - Empty submit validationMessage=[" + validationMsg + "], helper=[" + helperError + "]");

        boolean blocked = !validationMsg.isEmpty() || !helperError.isEmpty() || !snackbar.toLowerCase().contains("berhasil");
        Assert.assertTrue(blocked && page.getCurrentUrl().contains("/imports/"),
                menuName + ": Submit form tanpa memilih file harus ditahan/menampilkan validasi wajib pilih file.");
    }

    // =========================================================================
    // 1. IMPORT-IMPORT ABSEN (SIT_ADM_UI_154 - SIT_ADM_UI_157: 4 TCs)
    // =========================================================================

    @Test(priority = 154, description = "SIT_ADM_UI_154: Buka sub-menu Import-Import Absen dan download template Excel",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_154_ImportAbsenDownloadTemplate() {
        ExtentTestManager.logInfo("SIT_ADM_UI_154 [Import-Import Absen] Severity: High | Priority: P2");
        assertDownloadTemplateFlow(AdminImportPage.URL_IMPORT_ABSEN, AdminImportPage.TEMPLATE_ABSEN_URL, "Import-Import Absen");
    }

    @Test(priority = 155, description = "SIT_ADM_UI_155: Upload file Excel (.xlsx) valid pada halaman Import-Import Absen",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_155_ImportAbsenUploadValidExcel() {
        ExtentTestManager.logInfo("SIT_ADM_UI_155 [Import-Import Absen] Severity: High | Priority: P1");
        assertValidExcelUploadFlow(AdminImportPage.URL_IMPORT_ABSEN, "valid_absen.xlsx", "Import-Import Absen");
    }

    @Test(priority = 156, description = "SIT_ADM_UI_156: Upload file berekstensi salah (.pdf/.txt) pada Import-Import Absen",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_156_ImportAbsenUploadInvalidExtension() {
        ExtentTestManager.logInfo("SIT_ADM_UI_156 [Import-Import Absen] Severity: Medium | Priority: P2");
        assertInvalidFileRejectedFlow(AdminImportPage.URL_IMPORT_ABSEN, "invalid_upload.pdf", "Import-Import Absen");
    }

    @Test(priority = 157, description = "SIT_ADM_UI_157: Submit form Import-Import Absen tanpa memilih file",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_157_ImportAbsenSubmitEmptyFile() {
        ExtentTestManager.logInfo("SIT_ADM_UI_157 [Import-Import Absen] Severity: Medium | Priority: P2");
        assertEmptyFileValidationFlow(AdminImportPage.URL_IMPORT_ABSEN, "Import-Import Absen");
    }

    // =========================================================================
    // 2. IMPORT-IMPORT STATUS AKTIF (SIT_ADM_UI_158 - SIT_ADM_UI_161: 4 TCs)
    // =========================================================================

    @Test(priority = 158, description = "SIT_ADM_UI_158: Buka sub-menu Import-Import Status Aktif dan download template",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_158_ImportStatusAktifDownloadTemplate() {
        ExtentTestManager.logInfo("SIT_ADM_UI_158 [Import-Import Status Aktif] Severity: High | Priority: P2");
        assertDownloadTemplateFlow(AdminImportPage.URL_IMPORT_STATUS_AKTIF, AdminImportPage.TEMPLATE_STATUS_AKTIF_URL, "Import-Import Status Aktif");
    }

    @Test(priority = 159, description = "SIT_ADM_UI_159: Upload file Excel valid pada halaman Import-Import Status Aktif",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_159_ImportStatusAktifUploadValidExcel() {
        ExtentTestManager.logInfo("SIT_ADM_UI_159 [Import-Import Status Aktif] Severity: High | Priority: P1");
        assertValidExcelUploadFlow(AdminImportPage.URL_IMPORT_STATUS_AKTIF, "status_aktif.xlsx", "Import-Import Status Aktif");
    }

    @Test(priority = 160, description = "SIT_ADM_UI_160: Upload file non-Excel (.pdf/.txt) pada Import-Import Status Aktif",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_160_ImportStatusAktifUploadInvalidExtension() {
        ExtentTestManager.logInfo("SIT_ADM_UI_160 [Import-Import Status Aktif] Severity: Medium | Priority: P2");
        assertInvalidFileRejectedFlow(AdminImportPage.URL_IMPORT_STATUS_AKTIF, "invalid_upload.txt", "Import-Import Status Aktif");
    }

    @Test(priority = 161, description = "SIT_ADM_UI_161: Submit form Import-Import Status Aktif tanpa memilih file",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_161_ImportStatusAktifSubmitEmptyFile() {
        ExtentTestManager.logInfo("SIT_ADM_UI_161 [Import-Import Status Aktif] Severity: Medium | Priority: P2");
        assertEmptyFileValidationFlow(AdminImportPage.URL_IMPORT_STATUS_AKTIF, "Import-Import Status Aktif");
    }

    // =========================================================================
    // 3. IMPORT-IMPORT STRUKTUR USER (SIT_ADM_UI_162 - SIT_ADM_UI_165: 4 TCs)
    // =========================================================================

    @Test(priority = 162, description = "SIT_ADM_UI_162: Buka sub-menu Import-Import Struktur User dan download template",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_162_ImportStrukturUserDownloadTemplate() {
        ExtentTestManager.logInfo("SIT_ADM_UI_162 [Import-Import Struktur User] Severity: High | Priority: P2");
        assertDownloadTemplateFlow(AdminImportPage.URL_IMPORT_STRUKTUR_USER, AdminImportPage.TEMPLATE_STRUKTUR_USER_URL, "Import-Import Struktur User");
    }

    @Test(priority = 163, description = "SIT_ADM_UI_163: Upload file Excel valid pada halaman Import-Import Struktur User",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_163_ImportStrukturUserUploadValidExcel() {
        ExtentTestManager.logInfo("SIT_ADM_UI_163 [Import-Import Struktur User] Severity: High | Priority: P1");
        assertValidExcelUploadFlow(AdminImportPage.URL_IMPORT_STRUKTUR_USER, "struktur_valid.xlsx", "Import-Import Struktur User");
    }

    @Test(priority = 164, description = "SIT_ADM_UI_164: Upload file format tidak valid (.txt) pada Import-Import Struktur User",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_164_ImportStrukturUserUploadInvalidExtension() {
        ExtentTestManager.logInfo("SIT_ADM_UI_164 [Import-Import Struktur User] Severity: Medium | Priority: P2");
        assertInvalidFileRejectedFlow(AdminImportPage.URL_IMPORT_STRUKTUR_USER, "invalid_upload.txt", "Import-Import Struktur User");
    }

    @Test(priority = 165, description = "SIT_ADM_UI_165: Submit form Import-Import Struktur User tanpa memilih file",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_165_ImportStrukturUserSubmitEmptyFile() {
        ExtentTestManager.logInfo("SIT_ADM_UI_165 [Import-Import Struktur User] Severity: Medium | Priority: P2");
        assertEmptyFileValidationFlow(AdminImportPage.URL_IMPORT_STRUKTUR_USER, "Import-Import Struktur User");
    }

    // =========================================================================
    // 4. IMPORT-IMPORT CUTI (SIT_ADM_UI_166 - SIT_ADM_UI_169: 4 TCs)
    // =========================================================================

    @Test(priority = 166, description = "SIT_ADM_UI_166: Buka sub-menu Import-Import Cuti dan download template Excel",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_166_ImportCutiDownloadTemplate() {
        ExtentTestManager.logInfo("SIT_ADM_UI_166 [Import-Import Cuti] Severity: High | Priority: P2");
        assertDownloadTemplateFlow(AdminImportPage.URL_IMPORT_CUTI, AdminImportPage.TEMPLATE_CUTI_URL, "Import-Import Cuti");
    }

    @Test(priority = 167, description = "SIT_ADM_UI_167: Upload file Excel saldo cuti valid pada Import-Import Cuti",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_167_ImportCutiUploadValidExcel() {
        ExtentTestManager.logInfo("SIT_ADM_UI_167 [Import-Import Cuti] Severity: High | Priority: P1");
        assertValidExcelUploadFlow(AdminImportPage.URL_IMPORT_CUTI, "cuti_valid.xlsx", "Import-Import Cuti");
    }

    @Test(priority = 168, description = "SIT_ADM_UI_168: Upload file format salah (.pdf) pada Import-Import Cuti",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_168_ImportCutiUploadInvalidExtension() {
        ExtentTestManager.logInfo("SIT_ADM_UI_168 [Import-Import Cuti] Severity: Medium | Priority: P2");
        assertInvalidFileRejectedFlow(AdminImportPage.URL_IMPORT_CUTI, "invalid_upload.pdf", "Import-Import Cuti");
    }

    @Test(priority = 169, description = "SIT_ADM_UI_169: Submit form Import-Import Cuti tanpa memilih file",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_169_ImportCutiSubmitEmptyFile() {
        ExtentTestManager.logInfo("SIT_ADM_UI_169 [Import-Import Cuti] Severity: Medium | Priority: P2");
        assertEmptyFileValidationFlow(AdminImportPage.URL_IMPORT_CUTI, "Import-Import Cuti");
    }
}

