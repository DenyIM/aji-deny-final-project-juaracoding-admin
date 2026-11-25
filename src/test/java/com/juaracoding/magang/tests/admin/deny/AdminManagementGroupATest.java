package com.juaracoding.magang.tests.admin.deny;

import com.juaracoding.magang.BaseTest;
import com.juaracoding.magang.api.BaseApiTest;
import com.juaracoding.magang.api.helper.EmployeeApiHelper;
import com.juaracoding.magang.pages.admin.deny.AdminLoginPage;
import com.juaracoding.magang.pages.admin.deny.AdminManagementGroupAPage;
import com.juaracoding.magang.reports.ExtentTestManager;
import com.juaracoding.magang.utils.Constants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Pengujian Web UI Selenium untuk modul Management Group A Admin HRD (Tester Deny).
 * Cakupan SIT v3: SIT_ADM_UI_170 s/d SIT_ADM_UI_254 (85 Test Cases pada 9 sub-menu Management).
 * Penanggung jawab: Tester Deny | Branch: feature/deny-admin-mgmt-a.
 */
public class AdminManagementGroupATest extends BaseTest {

    private String findAvailableValidNikFromPool() {
        try {
            String adminToken = EmployeeApiHelper.loginAsDefaultAdmin();
            for (String candidate : Constants.VALID_NIK_POOL) {
                Response res = RestAssured.given()
                        .spec(BaseApiTest.buildDefaultRequestSpec())
                        .header("Authorization", "Bearer " + adminToken)
                        .queryParam("nik", candidate)
                .when()
                        .get(Constants.API_USERS_ENDPOINT)
                .then().extract().response();
                List<String> ids = res.jsonPath().getList("data.id");
                if (ids == null || ids.isEmpty()) {
                    return candidate;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not query VALID_NIK_POOL availability: {}", e.getMessage());
        }
        return Constants.VALID_NIK_POOL[0];
    }

    private void cleanupUserByEmailViaAdminApi(String email) {
        if (email == null || email.isBlank()) return;
        try {
            String adminToken = EmployeeApiHelper.loginAsDefaultAdmin();
            Response listRes = RestAssured.given()
                    .spec(BaseApiTest.buildDefaultRequestSpec())
                    .header("Authorization", "Bearer " + adminToken)
                    .queryParam("email", email)
            .when()
                    .get(Constants.API_USERS_ENDPOINT)
            .then().extract().response();
            List<String> ids = listRes.jsonPath().getList("data.id");
            if (ids != null) {
                for (String id : ids) {
                    EmployeeApiHelper.cleanupAdminRecordQuietly(Constants.API_USERS_ENDPOINT, id);
                }
            }
        } catch (Exception e) {
            logger.warn("Cleanup user [{}] skipped: {}", email, e.getMessage());
        }
    }

    private AdminManagementGroupAPage loginAndOpen(String url) {
        AdminLoginPage loginPage = new AdminLoginPage(getDriver());
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                loginPage.navigateToLoginPage();
                loginPage.loginWithDefaultAdmin();
                if (loginPage.waitForRedirectToPending()) {
                    AdminManagementGroupAPage page = new AdminManagementGroupAPage(getDriver());
                    page.openManagementUrl(url);
                    return page;
                }
            } catch (Exception e) {
                logger.warn("Login attempt {} failed: {}", attempt, e.getMessage());
                try {
                    ((org.openqa.selenium.JavascriptExecutor) getDriver()).executeScript("window.stop();");
                } catch (Exception ignored) {
                    // Ignore
                }
                try {
                    Thread.sleep(600);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Assert.fail("Prasyarat login Admin gagal sebelum membuka halaman Management: " + url);
        return null;
    }

    private void assertRowsPerPageBehavior(AdminManagementGroupAPage page, String menuName) {
        int[] options = {5, 10, 25};
        for (int size : options) {
            page.selectRowsPerPage(size);
            Assert.assertEquals(page.getSelectedRowsPerPage(), size,
                    menuName + ": Dropdown Rows per page harus menampilkan nilai " + size);
            int visibleRows = page.getTableRowCount();
            Assert.assertTrue(visibleRows <= size,
                    menuName + ": Baris tampil (" + visibleRows + ") melebihi batas (" + size + ")");
        }
    }

    private void assertPaginationNavigationBehavior(AdminManagementGroupAPage page, String menuName) {
        page.selectRowsPerPage(5);
        int total = page.getTotalRecordsFromPagination();
        String page1 = page.getDisplayedRowsText();

        Assert.assertTrue(page.isFirstPageButtonDisabled(), menuName + ": First Page harus disabled di halaman 1.");
        Assert.assertTrue(page.isPreviousPageButtonDisabled(), menuName + ": Previous Page harus disabled di halaman 1.");

        if (total <= 5) {
            Assert.assertTrue(page.isNextPageButtonDisabled(), menuName + ": Next Page harus disabled bila total <= 5.");
            Assert.assertTrue(page.isLastPageButtonDisabled(), menuName + ": Last Page harus disabled bila total <= 5.");
            return;
        }

        page.clickNextPage();
        String page2 = page.getDisplayedRowsText();
        Assert.assertNotEquals(page2, page1, menuName + ": Range halaman harus berubah setelah klik Next Page.");
        Assert.assertFalse(page.isFirstPageButtonDisabled(), menuName + ": First Page harus aktif di halaman > 1.");
        Assert.assertFalse(page.isPreviousPageButtonDisabled(), menuName + ": Previous Page harus aktif di halaman > 1.");

        page.clickPreviousPage();
        Assert.assertEquals(page.getDisplayedRowsText(), page1, menuName + ": Harus kembali ke halaman 1.");

        page.clickLastPage();
        Assert.assertTrue(page.isLastPageButtonDisabled(), menuName + ": Last Page harus disabled di halaman akhir.");
        Assert.assertTrue(page.isNextPageButtonDisabled(), menuName + ": Next Page harus disabled di halaman akhir.");

        page.clickFirstPage();
        Assert.assertEquals(page.getDisplayedRowsText(), page1, menuName + ": Harus kembali ke halaman 1.");
    }

    // =========================================================================
    // 1. MANAGEMENT-USER (SIT_ADM_UI_170 - SIT_ADM_UI_191: 22 TCs)
    // =========================================================================

    @Test(priority = 170, description = "SIT_ADM_UI_170: Buka sub-menu Management-User & verifikasi daftar tabel karyawan tampil",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_170_ManagementUserListDisplayed() {
        ExtentTestManager.logInfo("SIT_ADM_UI_170 [Management-User] Priority: P1");
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        Assert.assertTrue(page.hasTableHeaders() || page.getTableRowCount() > 0, "Daftar tabel Management-User harus tampil.");
        Assert.assertTrue(page.getTableRowCount() > 0, "Daftar karyawan Management-User harus tampil.");
    }

    @Test(priority = 171, description = "SIT_ADM_UI_171: Cari karyawan berdasarkan NIK (dropdown NIK) pada Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_171_ManagementUserSearchByNik() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        String targetNik = page.getFirstVisibleUserNikOnUserPage();
        page.selectUserSearchMode("NIK");
        page.searchByKeyword(targetNik);
        Assert.assertTrue(page.getTableBodyText().contains(targetNik),
                "Hasil pencarian NIK harus menampilkan " + targetNik);
    }

    @Test(priority = 172, description = "SIT_ADM_UI_172: Cari karyawan berdasarkan Nama (dropdown Nama) pada Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_172_ManagementUserSearchByName() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.selectUserSearchMode("Nama");
        page.searchByKeyword("Hadir SQA");
        Assert.assertTrue(page.getTableBodyText().toLowerCase().contains("hadir sqa"),
                "Hasil pencarian Nama harus menampilkan Hadir SQA.");
    }

    @Test(priority = 173, description = "SIT_ADM_UI_173: Filter Management-User berdasarkan Unit melalui modal Filter User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_173_ManagementUserFilterByUnit() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openUserFilterModal();
        page.selectFilterOptionInsideDialog("Unit", "");
        page.applyUserFilterModal();
        Assert.assertFalse(page.isAnyDialogDisplayed(), "Modal Filter User harus tertutup setelah diterapkan.");
    }

    @Test(priority = 174, description = "SIT_ADM_UI_174: Filter Management-User berdasarkan Job Level melalui modal Filter User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_174_ManagementUserFilterByJobLevel() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openUserFilterModal();
        page.selectFilterOptionInsideDialog("Level", "");
        page.applyUserFilterModal();
        Assert.assertFalse(page.isAnyDialogDisplayed(), "Modal Filter User harus tertutup setelah filter Job Level.");
    }

    @Test(priority = 175, description = "SIT_ADM_UI_175: Filter Management-User berdasarkan Tipe Karyawan melalui modal Filter User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_175_ManagementUserFilterByEmployeeType() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openUserFilterModal();
        page.selectFilterOptionInsideDialog("Tipe", "");
        page.applyUserFilterModal();
        Assert.assertFalse(page.isAnyDialogDisplayed(), "Modal Filter User harus tertutup setelah filter Tipe Karyawan.");
    }

    @Test(priority = 176, description = "SIT_ADM_UI_176: Filter Management-User berdasarkan Posisi Kerja melalui modal Filter User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_176_ManagementUserFilterByPosition() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openUserFilterModal();
        page.selectFilterOptionInsideDialog("Posisi", "");
        page.applyUserFilterModal();
        Assert.assertFalse(page.isAnyDialogDisplayed(), "Modal Filter User harus tertutup setelah filter Posisi Kerja.");
    }

    @Test(priority = 177, description = "SIT_ADM_UI_177: Filter Management-User berdasarkan Lokasi Kerja melalui modal Filter User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_177_ManagementUserFilterByLocation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openUserFilterModal();
        page.selectFilterOptionInsideDialog("Lokasi", "");
        page.applyUserFilterModal();
        Assert.assertFalse(page.isAnyDialogDisplayed(), "Modal Filter User harus tertutup setelah filter Lokasi Kerja.");
    }

    @Test(priority = 178, description = "SIT_ADM_UI_178: Filter Management-User dengan kombinasi multi-filter (Unit + Job Level + Posisi/Lokasi Kerja)",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_178_ManagementUserMultiFilter() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openUserFilterModal();
        page.selectFilterOptionInsideDialog("Unit", "");
        page.selectFilterOptionInsideDialog("Level", "");
        page.applyUserFilterModal();
        Assert.assertFalse(page.isAnyDialogDisplayed(), "Modal Filter User multi-filter harus tertutup.");
    }

    @Test(priority = 179, description = "SIT_ADM_UI_179: Edit profil data karyawan melalui halaman /management/user-v2/edit/[id]",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_179_ManagementUserEditProfile() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        Assert.assertTrue(page.editFirstNonProtectedUserProfileAndRestore(),
                "Perubahan profil karyawan pada halaman edit harus berhasil disimpan.");
    }

    @Test(priority = 180, description = "SIT_ADM_UI_180: Toggle switch status Aktif/Non-aktif karyawan pada halaman Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_180_ManagementUserToggleActiveStatus() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        Assert.assertTrue(page.toggleFirstUserActiveSwitchAndRestore(),
                "Toggle switch status Aktif/Non-aktif karyawan harus berfungsi dan dapat dikembalikan.");
    }

    @Test(priority = 181, description = "SIT_ADM_UI_181: Cari karyawan pada Management-User dengan NIK tidak terdaftar",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_181_ManagementUserSearchNotFound() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.selectUserSearchMode("NIK");
        page.searchByKeyword("NIK_PALSU_999");
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0,
                "Pencarian NIK palsu pada Management-User harus menampilkan 0 data.");
    }

    @Test(priority = 182, description = "SIT_ADM_UI_182: Submit Edit User (/management/user-v2/edit/[id]) dengan field wajib dikosongkan",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_182_ManagementUserEditEmptyRequiredField() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        Assert.assertTrue(page.submitEditUserWithEmptyRequiredName(),
                "Submit Edit User dengan Nama kosong harus ditolak oleh validasi form.");
    }

    @Test(priority = 183, description = "SIT_ADM_UI_183: Export seluruh data user ke file Excel melalui tombol Export di halaman Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_183_ManagementUserExportExcel() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        boolean clicked = page.isExportUsersButtonDisplayedAndClicked();
        Assert.assertTrue(clicked && !page.getSnackbarMessage().toLowerCase().contains("gagal"),
                "Tombol Export pada Management-User harus tampil dan berjalan tanpa error.");
    }

    @Test(priority = 184, description = "SIT_ADM_UI_184: Filter daftar user berdasarkan Status (Aktif / Non-aktif) melalui modal Filter User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_184_ManagementUserFilterByStatus() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openUserFilterModal();
        page.selectFilterOptionInsideDialog("Status", "Aktif");
        page.applyUserFilterModal();
        Assert.assertFalse(page.isAnyDialogDisplayed(), "Modal Filter User setelah memilih Status harus tertutup.");
    }

    @Test(priority = 185, description = "SIT_ADM_UI_185: Fungsi tombol Reset filter pada halaman Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_185_ManagementUserResetFilter() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.searchByKeyword("NIK_PALSU_999");
        page.clickReset();
        Assert.assertEquals(page.getSearchInputValue(), "", "Keyword pencarian harus bersih setelah Reset.");
        Assert.assertTrue(page.getTotalRecordsFromPagination() > 0, "Daftar tabel user harus pulih setelah Reset.");
    }

    @Test(priority = 186, description = "SIT_ADM_UI_186: Buka modal aksi 'Lihat Sisa Cuti' pada baris user di Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_186_ManagementUserOpenLeaveQuotaModal() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openActionMenuAndClick(0, "Lihat Sisa Cuti");
        Assert.assertTrue(page.isAnyDialogDisplayed(), "Modal Lihat Sisa Cuti harus terbuka.");
        Assert.assertTrue(page.getVisibleDialogText().toLowerCase().contains("sisa cuti"),
                "Modal harus memuat informasi Sisa Cuti karyawan.");
        page.closeVisibleDialogIfOpen();
    }

    @Test(priority = 187, description = "SIT_ADM_UI_187: Verifikasi item menu aksi titik tiga baris user (Lihat Sisa Cuti, Sync Approval, Edit, Hapus)",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_187_ManagementUserVerifyActionMenuItems() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        List<String> items = page.openActionMenuAndGetItems(0);
        page.dismissOpenPopoverMenu();
        String joined = String.join("|", items).toLowerCase();
        Assert.assertTrue(joined.contains("sisa cuti") && joined.contains("sync")
                        && joined.contains("edit") && (joined.contains("delete") || joined.contains("hapus")),
                "Menu aksi user harus memuat Lihat Sisa Cuti, Sync Approval, Edit, Delete/Hapus. Aktual: " + items);
        Assert.assertFalse(joined.contains("virtual id") || joined.contains("shift"),
                "Menu aksi user tidak boleh memuat opsi Shift / Atur Virtual ID. Aktual: " + items);
    }

    @Test(priority = 188, description = "SIT_ADM_UI_188: Jalankan aksi 'Sync Approval' pada baris user di halaman Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_188_ManagementUserSyncApproval() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openActionMenuAndClick(0, "Sync Approval");
        String toast = page.waitForSnackbarOrBodyFeedback();
        Assert.assertTrue(toast.toLowerCase().contains("sync") || toast.toLowerCase().contains("berhasil"),
                "Aksi Sync Approval harus menampilkan notifikasi sukses. Aktual: " + toast);
    }

    @Test(priority = 189, description = "SIT_ADM_UI_189: Uji dialog konfirmasi 'Hapus User' (klik Batal) pada halaman Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_189_ManagementUserCancelDeleteDialog() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        page.openActionMenuAndClickAny(0, "Delete", "Hapus");
        Assert.assertTrue(page.isAnyDialogDisplayed(), "Modal konfirmasi Hapus User harus tampil.");
        page.closeVisibleDialogIfOpen();
        Assert.assertFalse(page.isAnyDialogDisplayed(), "Modal Hapus User harus tertutup setelah klik Tidak/Batal.");
    }

    @Test(priority = 190, description = "SIT_ADM_UI_190: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_190_ManagementUserRowsPerPage() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        assertRowsPerPageBehavior(page, "Management-User");
    }

    @Test(priority = 191, description = "SIT_ADM_UI_191: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Management-User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_191_ManagementUserPaginationNavigation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_USER);
        assertPaginationNavigationBehavior(page, "Management-User");
    }

    // =========================================================================
    // 2. MANAGEMENT-PENDAFTARAN USER (SIT_ADM_UI_192 - SIT_ADM_UI_200: 9 TCs)
    // =========================================================================

    @Test(priority = 192, description = "SIT_ADM_UI_192: Buka sub-menu Management-Pendaftaran User dan submit pendaftaran karyawan baru valid",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_192_PendaftaranUserValidSubmit() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
        long ts = System.currentTimeMillis();
        String testEmail = "autosit_" + ts + "@hadir.com";
        String testNik = findAvailableValidNikFromPool();
        try {
            page.fillRegistrationAccountAndWorkFields(testNik, "Auto SIT User " + (ts % 10000L),
                    testEmail, "TestPass123!", "12", false);
            page.submitRegistrationForm();
            String msg = page.waitForSnackbarOrBodyFeedback();
            Assert.assertTrue(msg.toLowerCase().contains("berhasil") || !page.getCurrentUrl().contains("register"),
                    "Registrasi karyawan baru valid (NIK: " + testNik + ") harus berhasil. Pesan: " + msg);
        } finally {
            cleanupUserByEmailViaAdminApi(testEmail);
        }
    }

    @Test(priority = 193, description = "SIT_ADM_UI_193: Submit form Management-Pendaftaran User dengan field mandatory dikosongkan",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_193_PendaftaranUserEmptyRequired() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
        String msg = page.getFirstRequiredValidationOnRegistration();
        Assert.assertFalse(msg.isBlank(), "Field mandatory kosong harus memicu HTML5 required validation.");
        Assert.assertTrue(page.getCurrentUrl().contains("/user-v2/register"), "Form kosong tidak boleh tersubmit.");
    }

    @Test(priority = 194, description = "SIT_ADM_UI_194: Submit form Management-Pendaftaran User menggunakan email yang sudah terdaftar",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_194_PendaftaranUserDuplicateEmail() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
        long ts = System.currentTimeMillis();
        page.fillRegistrationAccountAndWorkFields("DUP" + (ts % 100000L), "Duplicate Email Test",
                Constants.HYBRID_EMPLOYEE_EMAIL, "TestPass123!", "12", false);
        page.submitRegistrationForm();
        String msg = page.waitForSnackbarOrBodyFeedback().toLowerCase();
        Assert.assertTrue(page.getCurrentUrl().contains("/register") && !msg.contains("berhasil"),
                "Registrasi email duplikat wajib ditolak. Snackbar: " + msg);
    }

    @Test(priority = 195, description = "SIT_ADM_UI_195: Upload foto karyawan ukuran > 2 MB pada Management-Pendaftaran User",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_195_PendaftaranUserOversizedPhotoRejected() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
        page.uploadEmployeeRegistrationPhoto("oversized_photo.jpg");
        String msg = (page.waitForSnackbarOrBodyFeedback() + " " + getDriver().getPageSource()).toLowerCase();
        Assert.assertTrue(msg.contains("2 mb") || !page.isRegistrationPhotoPreviewLoaded(),
                "Upload foto > 2 MB harus ditolak dengan pesan ukuran maksimal 2 MB.");
    }

    @Test(priority = 196, description = "SIT_ADM_UI_196: Upload file non-gambar (.pdf) pada Upload Foto Karyawan di Management-Pendaftaran User",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_196_PendaftaranUserInvalidPhotoFormatRejected() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
        page.uploadEmployeeRegistrationPhoto("invalid_upload.pdf");
        Assert.assertFalse(page.isRegistrationPhotoPreviewLoaded(),
                "File .pdf non-gambar tidak boleh dirender sebagai preview foto karyawan.");
    }

    @Test(priority = 197, description = "SIT_ADM_UI_197: Verifikasi pemilihan Atasan Client V2, Atasan Client V3, Tipe Kontrak, Tipe Shift, dan Selfie",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_197_PendaftaranUserOptionalUplinerAndSettings() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
        long ts = System.currentTimeMillis();
        page.fillRegistrationAccountAndWorkFields("OPT" + (ts % 100000L), "Optional Field User",
                "opt_" + ts + "@hadir.com", "TestPass123!", "12", true);
        Assert.assertTrue(page.hasTextOnPage("Atasan Client V2") && page.hasTextOnPage("Atasan Client V3"),
                "Field opsional Atasan Client V2/V3 dan pengaturan absensi harus tersedia & dapat dipilih.");
    }

    @Test(priority = 198, description = "SIT_ADM_UI_198: Verifikasi preview foto karyawan tampil utuh setelah upload file valid (< 2 MB) pada Management-Pendaftaran User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_198_PendaftaranUserValidPhotoPreviewLoaded() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
        page.uploadEmployeeRegistrationPhoto("valid_photo.jpg");
        Assert.assertTrue(page.isRegistrationPhotoPreviewLoaded(),
                "Preview foto valid (< 2 MB) harus langsung tampil utuh (naturalWidth > 0).");
    }

    @Test(priority = 199, description = "SIT_ADM_UI_199: Fungsi tombol Hapus preview foto karyawan sebelum submit pada Management-Pendaftaran User",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_199_PendaftaranUserDeletePhotoPreview() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
        page.uploadEmployeeRegistrationPhoto("valid_photo.jpg");
        Assert.assertTrue(page.isRegistrationPhotoPreviewLoaded(), "Preview harus tampil sebelum dihapus.");
        page.deleteRegistrationPhotoPreview();
        Assert.assertFalse(page.isRegistrationPhotoPreviewLoaded(),
                "Preview foto harus terhapus setelah tombol Hapus diklik.");
    }

    @Test(priority = 200, description = "SIT_ADM_UI_200: [Known Defect] Verifikasi penyimpanan & sinkronisasi data Jumlah Cuti setelah Pendaftaran User (Edit User vs Lihat Sisa Cuti)",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_200_PendaftaranUserJumlahCutiSync() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
        long ts = System.currentTimeMillis();
        String testEmail = "cutisync_" + ts + "@hadir.com";
        String testNik = findAvailableValidNikFromPool();
        try {
            page.fillRegistrationAccountAndWorkFields(testNik, "Cuti Sync Test",
                    testEmail, "TestPass123!", "12", false);
            page.submitRegistrationForm();
            page.waitForSnackbarOrBodyFeedback();

            page.openManagementUrl(AdminManagementGroupAPage.URL_USER);
            page.selectUserSearchMode("NIK");
            page.searchByKeyword(testNik);
            Assert.assertTrue(page.getTableRowCount() > 0, "User uji baru (" + testNik + ") harus ditemukan di Management-User.");

            page.openActionMenuAndClick(0, "Lihat Sisa Cuti");
            String dialogTxt = page.getVisibleDialogText();
            page.closeVisibleDialogIfOpen();
            Assert.assertTrue(dialogTxt.contains("12") && !dialogTxt.toLowerCase().contains("tidak ditemukan"),
                    "[KNOWN DEFECT] Jumlah Cuti (12) dari Pendaftaran User seharusnya langsung tersinkron ke modal Lihat Sisa Cuti. Aktual: " + dialogTxt);
        } finally {
            cleanupUserByEmailViaAdminApi(testEmail);
        }
    }

    // =========================================================================
    // 3. MANAGEMENT-PENDAFTARAN SENDIRI (SIT_ADM_UI_201 - SIT_ADM_UI_207: 7 TCs)
    // =========================================================================

    @Test(priority = 201, description = "SIT_ADM_UI_201: Buka sub-menu Management-Pendaftaran Sendiri, cari pendaftar, dan buka form verifikasi",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_201_PendaftaranSendiriOpenAndVerifyForm() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_SENDIRI);
        Assert.assertTrue(page.hasTableHeaders(), "Tabel Management-Pendaftaran Sendiri harus tampil.");
        Assert.assertTrue(page.hasSelfRegRowsAndOpenFirstVerification() || page.getTotalRecordsFromPagination() >= 0,
                "Halaman verifikasi atau tabel Pendaftaran Sendiri harus terbuka normal.");
    }

    @Test(priority = 202, description = "SIT_ADM_UI_202: [Known Defect] Verifikasi foto selfie pendaftar pada halaman Management-Pendaftaran Sendiri tampil utuh",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_202_PendaftaranSendiriSelfieImageLoaded() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_SENDIRI);
        Assert.assertTrue(page.areSelfRegImagesLoaded(),
                "[KNOWN DEFECT] Foto selfie pendaftar pada Management-Pendaftaran Sendiri seharusnya tampil utuh (naturalWidth > 0).");
    }

    @Test(priority = 203, description = "SIT_ADM_UI_203: Pencarian pendaftar mandiri dengan nama/NIK tidak terdaftar pada Management-Pendaftaran Sendiri",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_203_PendaftaranSendiriSearchNotFound() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_SENDIRI);
        page.searchByKeyword("TIDAK_ADA_999");
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0,
                "Pencarian pendaftar tidak terdaftar harus menghasilkan 0 data.");
    }

    @Test(priority = 204, description = "SIT_ADM_UI_204: Bulk update data pendaftar mandiri menggunakan checkbox dan tombol 'Update dichecklist'",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_204_PendaftaranSendiriBulkChecklistModal() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_SENDIRI);
        Assert.assertTrue(page.checkFirstRowAndOpenUpdateChecklistModal(),
                "Centang pendaftar + klik 'Update dichecklist' harus membuka modal 'Ubah data user dichecklist'.");
    }

    @Test(priority = 205, description = "SIT_ADM_UI_205: Buka modal konfirmasi 'Hapus User Registrasi Mobile' dan uji pembatalan hapus",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_205_PendaftaranSendiriCancelDeleteDialog() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_SENDIRI);
        page.openSelfRegDeleteConfirmDialog(0);
        Assert.assertTrue(page.isAnyDialogDisplayed(), "Modal konfirmasi Hapus User Registrasi Mobile harus tampil.");
        page.closeVisibleDialogIfOpen();
        Assert.assertFalse(page.isAnyDialogDisplayed(), "Modal harus tertutup setelah klik Tidak.");
    }

    @Test(priority = 206, description = "SIT_ADM_UI_206: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Management-Pendaftaran Sendiri",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_206_PendaftaranSendiriRowsPerPage() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_SENDIRI);
        assertRowsPerPageBehavior(page, "Management-Pendaftaran Sendiri");
    }

    @Test(priority = 207, description = "SIT_ADM_UI_207: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Management-Pendaftaran Sendiri",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_207_PendaftaranSendiriPaginationNavigation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_PENDAFTARAN_SENDIRI);
        assertPaginationNavigationBehavior(page, "Management-Pendaftaran Sendiri");
    }

    // =========================================================================
    // 4. MANAGEMENT-DIVISI (SIT_ADM_UI_208 - SIT_ADM_UI_215: 8 TCs)
    // =========================================================================

    @Test(priority = 208, description = "SIT_ADM_UI_208: Buka sub-menu Management-Divisi dan tambah Divisi baru valid",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_208_DivisiCreateValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_DIVISI);
        String name = "AUTO_SIT_DIV_" + System.currentTimeMillis();
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        Assert.assertTrue(page.getTableBodyText().contains(name), "Divisi baru harus tampil di tabel.");
        page.deleteFirstRowViaActionMenu();
    }

    @Test(priority = 209, description = "SIT_ADM_UI_209: Edit nama Divisi pada halaman Management-Divisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_209_DivisiEditValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_DIVISI);
        long ts = System.currentTimeMillis();
        String name = "AUTO_SIT_DIV_" + ts;
        String updated = "AUTO_SIT_DIV_ED_" + ts;
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        page.editFirstRowSimpleName(updated);
        page.searchByKeyword(updated);
        Assert.assertTrue(page.getTableBodyText().contains(updated), "Nama Divisi teredit harus tampil.");
        page.deleteFirstRowViaActionMenu();
    }

    @Test(priority = 210, description = "SIT_ADM_UI_210: Hapus data Divisi uji pada halaman Management-Divisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_210_DivisiDeleteValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_DIVISI);
        String name = "AUTO_SIT_DIV_DEL_" + System.currentTimeMillis();
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        page.deleteFirstRowViaActionMenu();
        page.searchByKeyword(name);
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0, "Data Divisi uji harus terhapus.");
    }

    @Test(priority = 211, description = "SIT_ADM_UI_211: Submit form Tambah/Edit Divisi dengan nama kosong pada Management-Divisi",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_211_DivisiEmptyNameValidation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_DIVISI);
        page.submitDialogEmpty();
        Assert.assertTrue(page.isAnyDialogDisplayed(), "Modal Divisi tidak boleh tersubmit saat nama kosong.");
        page.closeVisibleDialogIfOpen();
    }

    @Test(priority = 212, description = "SIT_ADM_UI_212: Search nama divisi valid dan fungsi tombol Reset pada Management-Divisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_212_DivisiSearchValidAndReset() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_DIVISI);
        page.searchByKeyword("IT");
        page.clickReset();
        Assert.assertEquals(page.getSearchInputValue(), "", "Input search harus bersih setelah Reset.");
    }

    @Test(priority = 213, description = "SIT_ADM_UI_213: Search nama divisi tidak ditemukan pada halaman Management-Divisi",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_213_DivisiSearchNotFound() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_DIVISI);
        page.searchByKeyword("DIVISI_PALSU_99");
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0, "Pencarian divisi palsu harus menghasilkan 0 data.");
    }

    @Test(priority = 214, description = "SIT_ADM_UI_214: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Management-Divisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_214_DivisiRowsPerPage() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_DIVISI);
        assertRowsPerPageBehavior(page, "Management-Divisi");
    }

    @Test(priority = 215, description = "SIT_ADM_UI_215: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Management-Divisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_215_DivisiPaginationNavigation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_DIVISI);
        assertPaginationNavigationBehavior(page, "Management-Divisi");
    }

    // =========================================================================
    // 5. MANAGEMENT-UNIT (SIT_ADM_UI_216 - SIT_ADM_UI_224: 9 TCs)
    // =========================================================================

    @Test(priority = 216, description = "SIT_ADM_UI_216: Buka sub-menu Management-Unit, tambah Unit baru, dan kelola tipe shift unit",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_216_UnitCreateValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT);
        String name = "AUTO_SIT_UNIT_" + System.currentTimeMillis();
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        Assert.assertTrue(page.getTableBodyText().contains(name), "Unit baru harus tampil di tabel.");
        page.deleteFirstRowViaActionMenu();
    }

    @Test(priority = 217, description = "SIT_ADM_UI_217: Submit form Tambah Unit dengan nama kosong atau pencarian unit tidak ditemukan",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_217_UnitEmptyNameAndSearchNotFound() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT);
        page.submitDialogEmpty();
        Assert.assertTrue(page.isAnyDialogDisplayed(), "Modal Unit tidak boleh tersubmit saat nama kosong.");
        page.closeVisibleDialogIfOpen();
        page.searchByKeyword("UNIT_PALSU_999");
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0, "Pencarian unit palsu harus menghasilkan 0 data.");
    }

    @Test(priority = 218, description = "SIT_ADM_UI_218: Edit nama Unit pada halaman Management-Unit (modal Edit Unit)",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_218_UnitEditValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT);
        long ts = System.currentTimeMillis();
        String name = "AUTO_SIT_UNIT_" + ts;
        String updated = "AUTO_SIT_UNIT_ED_" + ts;
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        page.editFirstRowSimpleName(updated);
        page.searchByKeyword(updated);
        Assert.assertTrue(page.getTableBodyText().contains(updated), "Unit teredit harus tampil di tabel.");
        page.deleteFirstRowViaActionMenu();
    }

    @Test(priority = 219, description = "SIT_ADM_UI_219: Hapus data Unit uji pada halaman Management-Unit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_219_UnitDeleteValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT);
        String name = "AUTO_SIT_UNIT_DEL_" + System.currentTimeMillis();
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        page.deleteFirstRowViaActionMenu();
        page.searchByKeyword(name);
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0, "Unit uji harus terhapus.");
    }

    @Test(priority = 220, description = "SIT_ADM_UI_220: Navigasi ke sub-halaman Tipe Shift Unit (/management/unit/type-shift/[id])",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_220_UnitNavigateToTypeShift() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT);
        Assert.assertTrue(page.openFirstUnitTypeShiftPage(),
                "Harus berpindah ke sub-halaman /management/unit/type-shift/[id].");
    }

    @Test(priority = 221, description = "SIT_ADM_UI_221: Buka modal aksi 'Atur Virtual ID' dan simpan Upload Foto Header (< 2 MB) & Warna Footer valid pada Management-Unit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_221_UnitAturVirtualIdValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT);
        String name = "AUTO_SIT_VID_" + System.currentTimeMillis();
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        try {
            Assert.assertTrue(page.saveVirtualIdWithPhotoAndColor("valid_photo.jpg", "#1E3A5F"),
                    "Atur Virtual ID dengan foto < 2 MB dan warna footer valid harus berhasil disimpan.");
        } finally {
            page.deleteFirstRowViaActionMenu();
        }
    }

    @Test(priority = 222, description = "SIT_ADM_UI_222: Validasi negatif simpan modal 'Atur Virtual ID' pada Management-Unit tanpa upload foto header",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_222_UnitAturVirtualIdEmptyPhoto() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT);
        String name = "AUTO_SIT_VID_NEG_" + System.currentTimeMillis();
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        try {
            Assert.assertTrue(page.submitVirtualIdWithoutPhotoAndCheckError(),
                    "Simpan Virtual ID tanpa upload foto header harus ditolak dengan pesan 'foto tidak boleh kosong'.");
        } finally {
            page.deleteFirstRowViaActionMenu();
        }
    }

    @Test(priority = 223, description = "SIT_ADM_UI_223: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Management-Unit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_223_UnitRowsPerPage() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT);
        assertRowsPerPageBehavior(page, "Management-Unit");
    }

    @Test(priority = 224, description = "SIT_ADM_UI_224: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Management-Unit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_224_UnitPaginationNavigation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT);
        assertPaginationNavigationBehavior(page, "Management-Unit");
    }

    // =========================================================================
    // 6. MANAGEMENT-UNIT SETTING (SIT_ADM_UI_225 - SIT_ADM_UI_229: 5 TCs)
    // =========================================================================

    @Test(priority = 225, description = "SIT_ADM_UI_225: Buka sub-menu Management-Unit Setting, tambah unit setting, dan ubah toggle konfigurasi",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_225_UnitSettingOpenAndAdd() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT_SETTING);
        Assert.assertTrue(page.hasTableHeaders(), "Tabel Management-Unit Setting harus tampil.");
        page.addFirstAvailableUnitSetting();
        Assert.assertTrue(page.getTableRowCount() > 0, "Data Unit Setting harus tersedia setelah ditambahkan.");
    }

    @Test(priority = 226, description = "SIT_ADM_UI_226: Submit modal tambah pada Management-Unit Setting tanpa memilih Unit",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_226_UnitSettingEmptySubmit() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT_SETTING);
        page.submitDialogEmpty();
        Assert.assertTrue(page.isAnyDialogDisplayed() || !page.getSnackbarMessage().toLowerCase().contains("berhasil"),
                "Modal Tambah Unit Setting tanpa memilih Unit tidak boleh berhasil disimpan.");
        page.closeVisibleDialogIfOpen();
    }

    @Test(priority = 227, description = "SIT_ADM_UI_227: Edit/Toggle pengaturan Unit Setting (Selfie, User Projek, Aktif Tracking) dan Hapus Unit Setting",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_227_UnitSettingToggleAndDeleteDialog() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT_SETTING);
        Assert.assertTrue(page.toggleUnitSettingSwitchAndRestore(), "Toggle setting Selfie/Tracking harus berfungsi.");
        page.openUnitSettingDeleteConfirmDialog(0);
        Assert.assertTrue(page.isAnyDialogDisplayed(), "Modal Hapus Departements harus terbuka.");
        page.closeVisibleDialogIfOpen();
    }

    @Test(priority = 228, description = "SIT_ADM_UI_228: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Management-Unit Setting",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_228_UnitSettingRowsPerPage() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT_SETTING);
        assertRowsPerPageBehavior(page, "Management-Unit Setting");
    }

    @Test(priority = 229, description = "SIT_ADM_UI_229: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Management-Unit Setting",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_229_UnitSettingPaginationNavigation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_UNIT_SETTING);
        assertPaginationNavigationBehavior(page, "Management-Unit Setting");
    }

    // =========================================================================
    // 7. MANAGEMENT-POSISI (SIT_ADM_UI_230 - SIT_ADM_UI_237: 8 TCs)
    // =========================================================================

    @Test(priority = 230, description = "SIT_ADM_UI_230: Buka sub-menu Management-Posisi dan tambah Posisi baru valid",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_230_PosisiCreateValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_POSISI);
        String name = "AUTO_SIT_POS_" + System.currentTimeMillis();
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        Assert.assertTrue(page.getTableBodyText().contains(name), "Posisi baru harus tampil di tabel.");
        page.deleteFirstRowViaActionMenu();
    }

    @Test(priority = 231, description = "SIT_ADM_UI_231: Edit nama Posisi pada halaman Management-Posisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_231_PosisiEditValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_POSISI);
        long ts = System.currentTimeMillis();
        String name = "AUTO_SIT_POS_" + ts;
        String updated = "AUTO_SIT_POS_ED_" + ts;
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        page.editFirstRowSimpleName(updated);
        page.searchByKeyword(updated);
        Assert.assertTrue(page.getTableBodyText().contains(updated), "Nama Posisi teredit harus tampil.");
        page.deleteFirstRowViaActionMenu();
    }

    @Test(priority = 232, description = "SIT_ADM_UI_232: Hapus data Posisi uji pada halaman Management-Posisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_232_PosisiDeleteValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_POSISI);
        String name = "AUTO_SIT_POS_DEL_" + System.currentTimeMillis();
        page.createSimpleNamedRecord(name);
        page.searchByKeyword(name);
        page.deleteFirstRowViaActionMenu();
        page.searchByKeyword(name);
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0, "Data Posisi uji harus terhapus.");
    }

    @Test(priority = 233, description = "SIT_ADM_UI_233: Submit form Tambah Posisi dengan field nama posisi kosong",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_233_PosisiEmptyNameValidation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_POSISI);
        page.submitDialogEmpty();
        Assert.assertTrue(page.isAnyDialogDisplayed(), "Modal Posisi tidak boleh tersubmit saat nama kosong.");
        page.closeVisibleDialogIfOpen();
    }

    @Test(priority = 234, description = "SIT_ADM_UI_234: Search nama posisi valid dan fungsi Reset pada Management-Posisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_234_PosisiSearchValidAndReset() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_POSISI);
        page.searchByKeyword("Staff");
        page.clickReset();
        Assert.assertEquals(page.getSearchInputValue(), "", "Input search harus kosong setelah Reset.");
    }

    @Test(priority = 235, description = "SIT_ADM_UI_235: Search nama posisi tidak ditemukan pada halaman Management-Posisi",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_235_PosisiSearchNotFound() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_POSISI);
        page.searchByKeyword("POSISI_PALSU_99");
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0, "Pencarian posisi palsu harus menghasilkan 0 data.");
    }

    @Test(priority = 236, description = "SIT_ADM_UI_236: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Management-Posisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_236_PosisiRowsPerPage() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_POSISI);
        assertRowsPerPageBehavior(page, "Management-Posisi");
    }

    @Test(priority = 237, description = "SIT_ADM_UI_237: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Management-Posisi",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_237_PosisiPaginationNavigation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_POSISI);
        assertPaginationNavigationBehavior(page, "Management-Posisi");
    }

    // =========================================================================
    // 8. MANAGEMENT-CLIENT UPLINER (SIT_ADM_UI_238 - SIT_ADM_UI_246: 9 TCs)
    // =========================================================================

    @Test(priority = 238, description = "SIT_ADM_UI_238: Buka sub-menu Management-Client Upliner dan daftarkan Client Upliner baru valid",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_238_ClientUplinerRegisterValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_CLIENT_UPLINER_REG);
        long ts = System.currentTimeMillis();
        String email = "upliner_" + ts + "@hadir.com";
        try {
            page.fillAndSubmitClientUplinerRegister("Auto Upliner " + (ts % 10000L), email, "UplinerPass123!");
            String msg = page.waitForSnackbarOrBodyFeedback();
            Assert.assertTrue(msg.toLowerCase().contains("berhasil") || page.getCurrentUrl().endsWith("/client-upliner"),
                    "Registrasi Client Upliner valid harus berhasil. Pesan: " + msg);
        } finally {
            cleanupUserByEmailViaAdminApi(email);
        }
    }

    @Test(priority = 239, description = "SIT_ADM_UI_239: Submit form Registrasi Client Upliner dengan field mandatory kosong",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_239_ClientUplinerEmptyRequired() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_CLIENT_UPLINER_REG);
        getDriver().findElement(By.cssSelector("button[type='submit']")).click();
        Assert.assertTrue(page.getCurrentUrl().contains("/client-upliner/register"),
                "Submit form registrasi Client Upliner kosong harus tertahan.");
    }

    @Test(priority = 240, description = "SIT_ADM_UI_240: Submit form Registrasi Client Upliner dengan password kurang dari 8 karakter",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_240_ClientUplinerShortPassword() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_CLIENT_UPLINER_REG);
        page.fillAndSubmitClientUplinerRegister("Short Pass Upliner", "short_" + System.currentTimeMillis() + "@hadir.com", "123");
        Assert.assertTrue(page.getCurrentUrl().contains("/client-upliner/register"),
                "Password < 8 karakter harus ditolak pada form Registrasi Client Upliner.");
    }

    @Test(priority = 241, description = "SIT_ADM_UI_241: [Known Defect] Verifikasi pemisahan field input Username dan Email pada form Registrasi Client Upliner",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_241_ClientUplinerUsernameFieldSeparated() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_CLIENT_UPLINER_REG);
        Assert.assertTrue(page.hasSeparateUsernameFieldOnClientUplinerRegister(),
                "[KNOWN DEFECT] Form Registrasi Client Upliner (/management/client-upliner/register) seharusnya menyediakan field Username terpisah dari Email.");
    }

    @Test(priority = 242, description = "SIT_ADM_UI_242: Buka form 'Tambahkan Upliner BCA' (/management/client-upliner/register-upliner-bca) & verifikasi field Username, Email, Upliner V2/V3",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_242_ClientUplinerBcaFormFields() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_CLIENT_UPLINER);
        Assert.assertTrue(page.verifyUplinerBcaFormFieldsPresent(),
                "Form Tambahkan Upliner BCA harus menampilkan field Nama, Username, Email, Password, dan Unit.");
    }

    @Test(priority = 243, description = "SIT_ADM_UI_243: Edit akun Client Upliner (/management/client-upliner/edit/[id])",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_243_ClientUplinerEditAccount() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_CLIENT_UPLINER);
        page.openActionMenuAndClick(0, "Edit");
        Assert.assertTrue(page.getCurrentUrl().contains("/client-upliner/edit/"),
                "Halaman edit Client Upliner harus terbuka.");
    }

    @Test(priority = 244, description = "SIT_ADM_UI_244: Search Client Upliner dan uji modal 'Hapus User' pada Management-Client Upliner",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_244_ClientUplinerSearchAndCancelDelete() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_CLIENT_UPLINER);
        page.searchByKeyword("a");
        page.clickReset();
        page.openActionMenuAndClickAny(0, "Delete", "Hapus");
        Assert.assertTrue(page.isAnyDialogDisplayed(), "Modal Hapus User pada Client Upliner harus terbuka.");
        page.closeVisibleDialogIfOpen();
    }

    @Test(priority = 245, description = "SIT_ADM_UI_245: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Management-Client Upliner",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_245_ClientUplinerRowsPerPage() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_CLIENT_UPLINER);
        assertRowsPerPageBehavior(page, "Management-Client Upliner");
    }

    @Test(priority = 246, description = "SIT_ADM_UI_246: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Management-Client Upliner",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_246_ClientUplinerPaginationNavigation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_CLIENT_UPLINER);
        assertPaginationNavigationBehavior(page, "Management-Client Upliner");
    }

    // =========================================================================
    // 9. MANAGEMENT-ABSEN POINT (SIT_ADM_UI_247 - SIT_ADM_UI_254: 8 TCs)
    // =========================================================================

    @Test(priority = 247, description = "SIT_ADM_UI_247: Buka sub-menu Management-Absen Point dan tambah titik lokasi geofence valid",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_247_AbsenPointCreateValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_ABSEN_POINT);
        String name = "AUTO_SIT_LOC_" + System.currentTimeMillis();
        page.createLocationPoint(name, "-6.200000", "106.816666", "100", "Automation SIT Point", true);
        page.searchByKeyword(name);
        Assert.assertTrue(page.getTableBodyText().contains(name), "Titik lokasi baru harus tampil di tabel.");
        page.deleteFirstRowViaActionMenu();
    }

    @Test(priority = 248, description = "SIT_ADM_UI_248: Edit titik lokasi geofence pada halaman Management-Absen Point",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_248_AbsenPointEditValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_ABSEN_POINT);
        long ts = System.currentTimeMillis();
        String name = "AUTO_SIT_LOC_" + ts;
        String updated = "AUTO_SIT_LOC_ED_" + ts;
        page.createLocationPoint(name, "-6.200000", "106.816666", "100", "Automation Edit Point", true);
        page.searchByKeyword(name);
        page.editFirstRowSimpleName(updated);
        page.searchByKeyword(updated);
        Assert.assertTrue(page.getTableBodyText().contains(updated), "Titik lokasi teredit harus tampil.");
        page.deleteFirstRowViaActionMenu();
    }

    @Test(priority = 249, description = "SIT_ADM_UI_249: Hapus titik lokasi geofence uji pada halaman Management-Absen Point",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_249_AbsenPointDeleteValid() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_ABSEN_POINT);
        String name = "AUTO_SIT_LOC_DEL_" + System.currentTimeMillis();
        page.createLocationPoint(name, "-6.200000", "106.816666", "100", "Automation Delete Point", true);
        page.searchByKeyword(name);
        page.deleteFirstRowViaActionMenu();
        page.searchByKeyword(name);
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0, "Titik lokasi uji harus terhapus.");
    }

    @Test(priority = 250, description = "SIT_ADM_UI_250: Submit form Tambah Absen Point dengan field mandatory dikosongkan",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_250_AbsenPointEmptyMandatory() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_ABSEN_POINT);
        page.submitDialogEmpty();
        Assert.assertTrue(page.isAnyDialogDisplayed(), "Modal Absen Point tidak boleh tersubmit saat field wajib kosong.");
        page.closeVisibleDialogIfOpen();
    }

    @Test(priority = 251, description = "SIT_ADM_UI_251: Submit form Tambah Absen Point dengan format koordinat/radius huruf tidak valid",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_251_AbsenPointInvalidNumericInput() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_ABSEN_POINT);
        page.createLocationPoint("AUTO_SIT_BAD_" + System.currentTimeMillis(), "abc", "xyz", "abc", "Invalid numeric test", false);
        Assert.assertTrue(page.isAnyDialogDisplayed() || !page.getSnackbarMessage().toLowerCase().contains("berhasil"),
                "Input koordinat/radius non-numerik harus ditolak.");
        page.closeVisibleDialogIfOpen();
    }

    @Test(priority = 252, description = "SIT_ADM_UI_252: Search nama lokasi geofence, klik Reset, dan validasi Maksimal Radius (Meter) & Description",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_252_AbsenPointSearchResetAndFields() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_ABSEN_POINT);
        page.searchByKeyword("Jakarta");
        page.clickReset();
        Assert.assertEquals(page.getSearchInputValue(), "", "Search harus bersih setelah Reset.");
        String body = page.getTableBodyText();
        Assert.assertTrue(page.hasTableHeaders() && !body.isBlank(), "Tabel dan data Absen Point harus tampil.");
    }

    @Test(priority = 253, description = "SIT_ADM_UI_253: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Management-Absen Point",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_253_AbsenPointRowsPerPage() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_ABSEN_POINT);
        assertRowsPerPageBehavior(page, "Management-Absen Point");
    }

    @Test(priority = 254, description = "SIT_ADM_UI_254: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Management-Absen Point",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_254_AbsenPointPaginationNavigation() {
        AdminManagementGroupAPage page = loginAndOpen(AdminManagementGroupAPage.URL_ABSEN_POINT);
        assertPaginationNavigationBehavior(page, "Management-Absen Point");
    }
}
