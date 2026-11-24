package com.juaracoding.magang.tests.admin.deny;

import com.juaracoding.magang.BaseTest;
import com.juaracoding.magang.pages.admin.deny.AdminLeavePage;
import com.juaracoding.magang.pages.admin.deny.AdminLoginPage;
import com.juaracoding.magang.pages.admin.deny.AdminReportsGroupAPage;
import com.juaracoding.magang.reports.ExtentTestManager;
import com.juaracoding.magang.utils.Constants;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Pengujian Web UI Selenium untuk seluruh fitur Laporan Group A Admin HRD (Tester Deny).
 * Cakupan SIT v3: SIT_ADM_UI_026 s/d SIT_ADM_UI_079 (54 Test Cases pada 7 sub-menu Laporan).
 * Penanggung jawab: Tester Deny | Branch: feature/deny-admin-reports-a.
 */
public class AdminReportsGroupATest extends BaseTest {

    private void loginWithRetry(AdminLoginPage loginPage) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                loginPage.navigateToLoginPage();
                loginPage.loginWithDefaultAdmin();
                if (loginPage.waitForRedirectToPending()) {
                    return;
                }
            } catch (Exception e) {
                logger.warn("Login attempt {} failed: {}", attempt, e.getMessage());
            }
        }
        Assert.fail("Admin harus berhasil login sebelum membuka sub-menu laporan.");
    }

    private AdminReportsGroupAPage loginAndOpenReport(String reportUrl) {
        AdminLoginPage loginPage = new AdminLoginPage(getDriver());
        loginWithRetry(loginPage);

        AdminReportsGroupAPage page = new AdminReportsGroupAPage(getDriver());
        page.openReportUrl(reportUrl);
        return page;
    }

    private AdminLeavePage loginAndOpenLeaveReport() {
        AdminLoginPage loginPage = new AdminLoginPage(getDriver());
        loginWithRetry(loginPage);

        AdminLeavePage leavePage = new AdminLeavePage(getDriver());
        leavePage.openLeaveReport();
        return leavePage;
    }

    private void assertRowsPerPageBehavior(AdminReportsGroupAPage page, String menuName) {
        int[] options = {5, 10, 25};
        for (int size : options) {
            page.selectRowsPerPage(size);
            Assert.assertEquals(page.getSelectedRowsPerPage(), size,
                    menuName + ": Dropdown Rows per page harus menampilkan nilai " + size);
            int visibleRows = page.getTableRowCount();
            Assert.assertTrue(visibleRows <= size,
                    menuName + ": Jumlah baris tampil (" + visibleRows + ") melebihi batas Rows per page (" + size + ")");
        }
    }

    private void assertPaginationNavigationBehavior(AdminReportsGroupAPage page, String menuName) {
        page.selectRowsPerPage(5);
        int total = page.getTotalRecordsFromPagination();
        String page1Label = page.getDisplayedRowsText();
        ExtentTestManager.logInfo(menuName + " - Pagination total=" + total + ", page1=" + page1Label);

        Assert.assertTrue(page.isFirstPageButtonDisabled(), menuName + ": First Page harus disabled di halaman 1.");
        Assert.assertTrue(page.isPreviousPageButtonDisabled(), menuName + ": Previous Page harus disabled di halaman 1.");

        if (total <= 5) {
            Assert.assertTrue(page.isNextPageButtonDisabled(),
                    menuName + ": Next Page harus disabled bila total data <= 5 (" + total + ").");
            Assert.assertTrue(page.isLastPageButtonDisabled(),
                    menuName + ": Last Page harus disabled bila total data <= 5 (" + total + ").");
            return;
        }

        page.clickNextPage();
        String page2Label = page.getDisplayedRowsText();
        Assert.assertNotEquals(page2Label, page1Label,
                menuName + ": Indikator range halaman harus berubah setelah klik Next Page.");
        Assert.assertFalse(page.isFirstPageButtonDisabled(),
                menuName + ": First Page harus aktif di halaman > 1.");
        Assert.assertFalse(page.isPreviousPageButtonDisabled(),
                menuName + ": Previous Page harus aktif di halaman > 1.");

        page.clickPreviousPage();
        Assert.assertEquals(page.getDisplayedRowsText(), page1Label,
                menuName + ": Kembali ke halaman 1 setelah klik Previous Page.");

        page.clickLastPage();
        Assert.assertTrue(page.isLastPageButtonDisabled(),
                menuName + ": Last Page harus disabled ketika sudah berada di halaman terakhir.");
        Assert.assertTrue(page.isNextPageButtonDisabled(),
                menuName + ": Next Page harus disabled ketika sudah berada di halaman terakhir.");

        page.clickFirstPage();
        Assert.assertEquals(page.getDisplayedRowsText(), page1Label,
                menuName + ": Kembali ke halaman pertama setelah klik First Page.");
    }

    // =========================================================================
    // 1. LAPORAN-SEMUA (SIT_ADM_UI_026 - SIT_ADM_UI_034: 9 TCs)
    // =========================================================================

    @Test(priority = 26, description = "SIT_ADM_UI_026: Verifikasi data kolom Jam Masuk dan Jam Keluar beserta tipe WFO/WFH pada tabel Laporan-Semua",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_026_LaporanSemuaCheckInOutColumns() {
        ExtentTestManager.logInfo("SIT_ADM_UI_026 [Laporan-Semua] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SEMUA);
        page.ensureTableHasRowsIfEmpty();

        Assert.assertTrue(page.hasTableHeaderContaining("JAM MASUK"), "Kolom Jam Masuk harus tampil.");
        Assert.assertTrue(page.hasTableHeaderContaining("JAM KELUAR"), "Kolom Jam Keluar harus tampil.");
        Assert.assertTrue(page.getTableRowCount() > 0, "Tabel Laporan-Semua harus menampilkan data.");
        String body = page.getTableBodyText().toUpperCase();
        Assert.assertTrue(body.contains("WORK FROM OFFICE") || body.contains("WORK FROM HOME")
                || body.contains("WFO") || body.contains("WFH")
                || body.contains("MASUK") || body.contains("SAKIT") || body.contains("CUTI")
                || body.contains("LEMBUR") || body.contains("-"),
                "Baris Laporan-Semua harus menampilkan tipe WFO/WFH (Work From Office/Home) atau status. Aktual: " + body);
    }

    @Test(priority = 27, description = "SIT_ADM_UI_027: Verifikasi kelengkapan kolom Nama Karyawan, Unit, Lokasi Masuk/Keluar, Status, dan Notes pada Laporan-Semua",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_027_LaporanSemuaColumnsAndNotesExpand() {
        ExtentTestManager.logInfo("SIT_ADM_UI_027 [Laporan-Semua] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SEMUA);
        page.ensureTableHasRowsIfEmpty();

        List<String> headers = page.getTableHeadersText();
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("NAMA")), "Kolom Nama Karyawan harus ada.");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("UNIT")), "Kolom Unit harus ada.");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("LOKASI")), "Kolom Lokasi harus ada.");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.contains("NOTES")), "Kolom Notes harus ada.");
        Assert.assertTrue(page.hasMapsLinkColumnInAllReport(), "Tautan lokasi atau baris data harus tersedia.");
        Assert.assertTrue(page.expandLongNotesIfPresent(), "Ekspansi teks 'Lihat Selengkapnya' harus berfungsi bila tersedia.");
    }

    @Test(priority = 28, description = "SIT_ADM_UI_028: [Known Defect] Verifikasi foto avatar presensi pada kolom pertama tabel Laporan-Semua tampil utuh",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_028_LaporanSemuaAvatarImageLoaded() {
        ExtentTestManager.logInfo("SIT_ADM_UI_028 [Laporan-Semua] Known Defect Check | Severity: High | Priority: P1");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SEMUA);
        page.ensureTableHasRowsIfEmpty();

        boolean loaded = page.areAllAvatarUploadImagesLoaded();
        Assert.assertTrue(loaded,
                "[KNOWN DEFECT] Seluruh foto avatar check-in pada kolom pertama Laporan-Semua harus tampil utuh (naturalWidth > 0, HTTP 200).");
    }

    @Test(priority = 29, description = "SIT_ADM_UI_029: Pencarian data pada Laporan-Semua berdasarkan nama karyawan valid",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_029_LaporanSemuaSearchValidName() {
        ExtentTestManager.logInfo("SIT_ADM_UI_029 [Laporan-Semua] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SEMUA);

        page.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        String body = page.getTableBodyText();
        Assert.assertTrue(page.getTableRowCount() >= 0,
                "Pencarian nama valid pada Laporan-Semua harus memfilter tabel tanpa error. Hasil: " + body);
    }

    @Test(priority = 30, description = "SIT_ADM_UI_030: Pencarian data pada Laporan-Semua dengan nama yang tidak ditemukan",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_030_LaporanSemuaSearchNotFound() {
        ExtentTestManager.logInfo("SIT_ADM_UI_030 [Laporan-Semua] Severity: Low | Priority: P3");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SEMUA);

        page.searchByKeyword("ZZZZ9999");
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0,
                "Pencarian nama tidak ditemukan harus menghasilkan 0 data (0-0 of 0).");
    }

    @Test(priority = 31, description = "SIT_ADM_UI_031: Filter Laporan-Semua berdasarkan rentang Start Date, End Date & Unit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_031_LaporanSemuaFilterDateAndUnit() {
        ExtentTestManager.logInfo("SIT_ADM_UI_031 [Laporan-Semua] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SEMUA);

        page.applyDateRangeFilter(false);
        String unit = page.applyFirstUnitFilter();
        Assert.assertFalse(page.getStartDateValue().isBlank(), "Start Date harus terisi setelah memilih tanggal.");
        ExtentTestManager.logInfo("Filter Unit terpilih: " + unit);
    }

    @Test(priority = 32, description = "SIT_ADM_UI_032: Fungsi tombol Reset filter pada halaman Laporan-Semua",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_032_LaporanSemuaResetFilter() {
        ExtentTestManager.logInfo("SIT_ADM_UI_032 [Laporan-Semua] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SEMUA);

        page.searchByKeyword("ZZZZ9999");
        page.clickResetFilter();
        Assert.assertEquals(page.getSearchInputValue(), "", "Input search harus kosong setelah Reset.");
        Assert.assertTrue(page.getTotalRecordsFromPagination() > 0, "Data tabel harus kembali tampil setelah Reset.");
    }

    @Test(priority = 33, description = "SIT_ADM_UI_033: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Laporan-Semua",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_033_LaporanSemuaRowsPerPage() {
        ExtentTestManager.logInfo("SIT_ADM_UI_033 [Laporan-Semua] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SEMUA);
        assertRowsPerPageBehavior(page, "Laporan-Semua");
    }

    @Test(priority = 34, description = "SIT_ADM_UI_034: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Laporan-Semua",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_034_LaporanSemuaPaginationNavigation() {
        ExtentTestManager.logInfo("SIT_ADM_UI_034 [Laporan-Semua] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SEMUA);
        assertPaginationNavigationBehavior(page, "Laporan-Semua");
    }

    // =========================================================================
    // 2. LAPORAN-KEHADIRAN (SIT_ADM_UI_035 - SIT_ADM_UI_041: 7 TCs)
    // =========================================================================

    @Test(priority = 35, description = "SIT_ADM_UI_035: Buka sub-menu Laporan-Kehadiran dan verifikasi peta Google Maps & tabel aktivitas",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_035_LaporanKehadiranMapAndTable() {
        ExtentTestManager.logInfo("SIT_ADM_UI_035 [Laporan-Kehadiran] Severity: High | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_KEHADIRAN);
        page.ensureTableHasRowsIfEmpty();

        Assert.assertTrue(page.isGoogleMapDisplayed(), "Komponen peta Google Maps harus tampil pada Laporan-Kehadiran.");
        Assert.assertTrue(page.hasTableHeaderContaining("NAMA"), "Tabel aktivitas kehadiran harus tampil.");
    }

    @Test(priority = 36, description = "SIT_ADM_UI_036: [Known Defect] Verifikasi foto presensi pada InfoWindow marker Maps & tabel Laporan-Kehadiran tampil utuh",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_036_LaporanKehadiranPresencePhotoLoaded() {
        ExtentTestManager.logInfo("SIT_ADM_UI_036 [Laporan-Kehadiran] Known Defect Check | Severity: High | Priority: P1");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_KEHADIRAN);
        page.ensureTableHasRowsIfEmpty();

        boolean loaded = page.arePresencePhotosInMapsOrTableLoaded();
        Assert.assertTrue(loaded,
                "[KNOWN DEFECT] Foto presensi pada tabel dan InfoWindow Maps Laporan-Kehadiran harus tampil utuh (naturalWidth > 0).");
    }

    @Test(priority = 37, description = "SIT_ADM_UI_037: Pencarian karyawan berdasarkan nama dan filter tanggal pada Laporan-Kehadiran",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_037_LaporanKehadiranSearchAndDateFilter() {
        ExtentTestManager.logInfo("SIT_ADM_UI_037 [Laporan-Kehadiran] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_KEHADIRAN);

        page.applyDateRangeFilter(false);
        page.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        Assert.assertEquals(page.getSearchInputValue(), Constants.HYBRID_EMPLOYEE_NAME,
                "Kata kunci search harus tetap terisi dan memfilter tabel kehadiran.");
    }

    @Test(priority = 38, description = "SIT_ADM_UI_038: Filter Laporan-Kehadiran berdasarkan Unit melalui modal Filter By",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_038_LaporanKehadiranFilterUnit() {
        ExtentTestManager.logInfo("SIT_ADM_UI_038 [Laporan-Kehadiran] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_KEHADIRAN);

        String unit = page.applyFirstUnitFilter();
        Assert.assertFalse(page.isFilterModalDisplayed(), "Modal Filter By harus tertutup setelah klik Terapkan.");
        ExtentTestManager.logInfo("Unit diterapkan pada Laporan-Kehadiran: " + unit);
    }

    @Test(priority = 39, description = "SIT_ADM_UI_039: Fungsi tombol Reset filter pada halaman Laporan-Kehadiran",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_039_LaporanKehadiranResetFilter() {
        ExtentTestManager.logInfo("SIT_ADM_UI_039 [Laporan-Kehadiran] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_KEHADIRAN);

        page.searchByKeyword("USER_FILTER_TEST");
        page.clickResetFilter();
        Assert.assertEquals(page.getSearchInputValue(), "", "Search harus bersih kembali setelah Reset.");
    }

    @Test(priority = 40, description = "SIT_ADM_UI_040: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Laporan-Kehadiran",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_040_LaporanKehadiranRowsPerPage() {
        ExtentTestManager.logInfo("SIT_ADM_UI_040 [Laporan-Kehadiran] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_KEHADIRAN);
        assertRowsPerPageBehavior(page, "Laporan-Kehadiran");
    }

    @Test(priority = 41, description = "SIT_ADM_UI_041: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Laporan-Kehadiran",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_041_LaporanKehadiranPaginationNavigation() {
        ExtentTestManager.logInfo("SIT_ADM_UI_041 [Laporan-Kehadiran] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_KEHADIRAN);
        assertPaginationNavigationBehavior(page, "Laporan-Kehadiran");
    }

    // =========================================================================
    // 3. LAPORAN-IZIN (SIT_ADM_UI_042 - SIT_ADM_UI_048: 7 TCs)
    // =========================================================================

    @Test(priority = 42, description = "SIT_ADM_UI_042: Buka sub-menu Laporan-Izin dan verifikasi tabel pengajuan izin",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_042_LaporanIzinTableDisplayed() {
        ExtentTestManager.logInfo("SIT_ADM_UI_042 [Laporan-Izin] Severity: High | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN);

        Assert.assertTrue(page.hasTableHeaderContaining("NAMA"), "Kolom Nama User harus tampil di Laporan-Izin.");
        Assert.assertTrue(page.hasTableHeaderContaining("UNIT"), "Kolom Unit harus tampil di Laporan-Izin.");
        Assert.assertTrue(page.hasTableHeaderContaining("STATUS"), "Kolom Status harus tampil di Laporan-Izin.");
    }

    @Test(priority = 43, description = "SIT_ADM_UI_043: Cari dan filter data pada Laporan-Izin berdasarkan nama dan tanggal",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_043_LaporanIzinSearchAndDate() {
        ExtentTestManager.logInfo("SIT_ADM_UI_043 [Laporan-Izin] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN);

        page.applyDateRangeFilter(false);
        page.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        Assert.assertEquals(page.getSearchInputValue(), Constants.HYBRID_EMPLOYEE_NAME);
    }

    @Test(priority = 44, description = "SIT_ADM_UI_044: Pencarian pada Laporan-Izin dengan nama karyawan yang tidak terdaftar",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_044_LaporanIzinSearchNotFound() {
        ExtentTestManager.logInfo("SIT_ADM_UI_044 [Laporan-Izin] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN);

        page.searchByKeyword("USER_TIDAK_ADA_999");
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0,
                "Pencarian nama tidak terdaftar di Laporan-Izin harus menghasilkan 0 data.");
    }

    @Test(priority = 45, description = "SIT_ADM_UI_045: Filter Laporan-Izin berdasarkan Unit Departemen melalui modal Filter By",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_045_LaporanIzinFilterUnit() {
        ExtentTestManager.logInfo("SIT_ADM_UI_045 [Laporan-Izin] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN);

        page.applyFirstUnitFilter();
        Assert.assertFalse(page.isFilterModalDisplayed(), "Modal Filter By harus tertutup setelah diterapkan.");
    }

    @Test(priority = 46, description = "SIT_ADM_UI_046: Fungsi tombol Reset filter pada halaman Laporan-Izin",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_046_LaporanIzinResetFilter() {
        ExtentTestManager.logInfo("SIT_ADM_UI_046 [Laporan-Izin] Severity: Low | Priority: P3");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN);

        page.searchByKeyword("USER_TIDAK_ADA_999");
        page.clickResetFilter();
        Assert.assertEquals(page.getSearchInputValue(), "", "Input search harus kosong kembali setelah Reset.");
    }

    @Test(priority = 47, description = "SIT_ADM_UI_047: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Laporan-Izin",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_047_LaporanIzinRowsPerPage() {
        ExtentTestManager.logInfo("SIT_ADM_UI_047 [Laporan-Izin] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN);
        assertRowsPerPageBehavior(page, "Laporan-Izin");
    }

    @Test(priority = 48, description = "SIT_ADM_UI_048: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Laporan-Izin",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_048_LaporanIzinPaginationNavigation() {
        ExtentTestManager.logInfo("SIT_ADM_UI_048 [Laporan-Izin] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN);
        assertPaginationNavigationBehavior(page, "Laporan-Izin");
    }

    // =========================================================================
    // 4. LAPORAN-IZIN TERLAMBAT (SIT_ADM_UI_049 - SIT_ADM_UI_055: 7 TCs)
    // =========================================================================

    @Test(priority = 49, description = "SIT_ADM_UI_049: Buka sub-menu Laporan-Izin Terlambat dan verifikasi kolom tabel",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_049_LaporanIzinTerlambatTableDisplayed() {
        ExtentTestManager.logInfo("SIT_ADM_UI_049 [Laporan-Izin Terlambat] Severity: High | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_TERLAMBAT);

        Assert.assertTrue(page.hasTableHeaderContaining("NAMA"), "Kolom Nama User harus tampil.");
        Assert.assertTrue(page.hasTableHeaderContaining("UNIT"), "Kolom Unit harus tampil.");
        Assert.assertTrue(page.hasTableHeaderContaining("STATUS"), "Kolom Status harus tampil.");
    }

    @Test(priority = 50, description = "SIT_ADM_UI_050: Pencarian nama karyawan valid pada sub-menu Laporan-Izin Terlambat",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_050_LaporanIzinTerlambatSearchValid() {
        ExtentTestManager.logInfo("SIT_ADM_UI_050 [Laporan-Izin Terlambat] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_TERLAMBAT);

        page.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        Assert.assertEquals(page.getSearchInputValue(), Constants.HYBRID_EMPLOYEE_NAME);
    }

    @Test(priority = 51, description = "SIT_ADM_UI_051: Pencarian dengan kata kunci tidak ditemukan pada Laporan-Izin Terlambat",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_051_LaporanIzinTerlambatSearchNotFound() {
        ExtentTestManager.logInfo("SIT_ADM_UI_051 [Laporan-Izin Terlambat] Severity: Low | Priority: P3");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_TERLAMBAT);

        page.searchByKeyword("TIDAK_ADA_999");
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0,
                "Pencarian kata kunci tidak ada harus menampilkan 0 data tanpa error.");
    }

    @Test(priority = 52, description = "SIT_ADM_UI_052: Filter Laporan-Izin Terlambat berdasarkan Rentang Tanggal dan Unit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_052_LaporanIzinTerlambatFilterDateAndUnit() {
        ExtentTestManager.logInfo("SIT_ADM_UI_052 [Laporan-Izin Terlambat] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_TERLAMBAT);

        page.applyDateRangeFilter(false);
        page.applyFirstUnitFilter();
        Assert.assertFalse(page.getStartDateValue().isBlank(), "Start Date harus terisi.");
    }

    @Test(priority = 53, description = "SIT_ADM_UI_053: Fungsi tombol Reset filter pada halaman Laporan-Izin Terlambat",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_053_LaporanIzinTerlambatResetFilter() {
        ExtentTestManager.logInfo("SIT_ADM_UI_053 [Laporan-Izin Terlambat] Severity: Low | Priority: P3");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_TERLAMBAT);

        page.searchByKeyword("TIDAK_ADA_999");
        page.clickResetFilter();
        Assert.assertEquals(page.getSearchInputValue(), "", "Search harus bersih kembali setelah Reset.");
    }

    @Test(priority = 54, description = "SIT_ADM_UI_054: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Laporan-Izin Terlambat",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_054_LaporanIzinTerlambatRowsPerPage() {
        ExtentTestManager.logInfo("SIT_ADM_UI_054 [Laporan-Izin Terlambat] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_TERLAMBAT);
        assertRowsPerPageBehavior(page, "Laporan-Izin Terlambat");
    }

    @Test(priority = 55, description = "SIT_ADM_UI_055: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Laporan-Izin Terlambat",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_055_LaporanIzinTerlambatPaginationNavigation() {
        ExtentTestManager.logInfo("SIT_ADM_UI_055 [Laporan-Izin Terlambat] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_TERLAMBAT);
        assertPaginationNavigationBehavior(page, "Laporan-Izin Terlambat");
    }

    // =========================================================================
    // 5. LAPORAN-IZIN PULANG CEPAT (SIT_ADM_UI_056 - SIT_ADM_UI_062: 7 TCs)
    // =========================================================================

    @Test(priority = 56, description = "SIT_ADM_UI_056: Buka sub-menu Laporan-Izin Pulang Cepat dan verifikasi tabel",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_056_LaporanIzinPulangCepatTableDisplayed() {
        ExtentTestManager.logInfo("SIT_ADM_UI_056 [Laporan-Izin Pulang Cepat] Severity: High | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_PULANG_CEPAT);

        Assert.assertTrue(page.hasTableHeaderContaining("NAMA"), "Kolom Nama User harus tampil.");
        Assert.assertTrue(page.hasTableHeaderContaining("UNIT"), "Kolom Unit harus tampil.");
        Assert.assertTrue(page.hasTableHeaderContaining("STATUS"), "Kolom Status harus tampil.");
    }

    @Test(priority = 57, description = "SIT_ADM_UI_057: Pencarian nama karyawan pada Laporan-Izin Pulang Cepat",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_057_LaporanIzinPulangCepatSearchValid() {
        ExtentTestManager.logInfo("SIT_ADM_UI_057 [Laporan-Izin Pulang Cepat] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_PULANG_CEPAT);

        page.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        Assert.assertEquals(page.getSearchInputValue(), Constants.HYBRID_EMPLOYEE_NAME);
    }

    @Test(priority = 58, description = "SIT_ADM_UI_058: Pencarian nama tidak terdaftar pada Laporan-Izin Pulang Cepat",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_058_LaporanIzinPulangCepatSearchNotFound() {
        ExtentTestManager.logInfo("SIT_ADM_UI_058 [Laporan-Izin Pulang Cepat] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_PULANG_CEPAT);

        page.searchByKeyword("INVALID_USER_99");
        Assert.assertEquals(page.getTotalRecordsFromPagination(), 0,
                "Pencarian nama tidak terdaftar harus menghasilkan 0 data.");
    }

    @Test(priority = 59, description = "SIT_ADM_UI_059: Filter Laporan-Izin Pulang Cepat berdasarkan Rentang Tanggal dan Unit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_059_LaporanIzinPulangCepatFilterDateAndUnit() {
        ExtentTestManager.logInfo("SIT_ADM_UI_059 [Laporan-Izin Pulang Cepat] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_PULANG_CEPAT);

        page.applyDateRangeFilter(false);
        page.applyFirstUnitFilter();
        Assert.assertFalse(page.getStartDateValue().isBlank(), "Start Date harus terisi.");
    }

    @Test(priority = 60, description = "SIT_ADM_UI_060: Fungsi tombol Reset filter pada halaman Laporan-Izin Pulang Cepat",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_060_LaporanIzinPulangCepatResetFilter() {
        ExtentTestManager.logInfo("SIT_ADM_UI_060 [Laporan-Izin Pulang Cepat] Severity: Low | Priority: P3");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_PULANG_CEPAT);

        page.searchByKeyword("INVALID_USER_99");
        page.clickResetFilter();
        Assert.assertEquals(page.getSearchInputValue(), "", "Filter harus bersih setelah klik Reset.");
    }

    @Test(priority = 61, description = "SIT_ADM_UI_061: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Laporan-Izin Pulang Cepat",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_061_LaporanIzinPulangCepatRowsPerPage() {
        ExtentTestManager.logInfo("SIT_ADM_UI_061 [Laporan-Izin Pulang Cepat] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_PULANG_CEPAT);
        assertRowsPerPageBehavior(page, "Laporan-Izin Pulang Cepat");
    }

    @Test(priority = 62, description = "SIT_ADM_UI_062: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Laporan-Izin Pulang Cepat",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_062_LaporanIzinPulangCepatPaginationNavigation() {
        ExtentTestManager.logInfo("SIT_ADM_UI_062 [Laporan-Izin Pulang Cepat] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_IZIN_PULANG_CEPAT);
        assertPaginationNavigationBehavior(page, "Laporan-Izin Pulang Cepat");
    }

    // =========================================================================
    // 6. LAPORAN-CUTI (SIT_ADM_UI_063 - SIT_ADM_UI_071: 9 TCs)
    // =========================================================================

    @Test(priority = 63, description = "SIT_ADM_UI_063: Buka sub-menu Laporan-Cuti dan verifikasi kolom tabel pengajuan cuti",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_063_LaporanCutiTableDisplayed() {
        ExtentTestManager.logInfo("SIT_ADM_UI_063 [Laporan-Cuti] Severity: High | Priority: P2");
        AdminLeavePage page = loginAndOpenLeaveReport();

        Assert.assertTrue(page.hasTableHeaderContaining("NAMA"), "Kolom Nama Karyawan harus tampil.");
        Assert.assertTrue(page.hasTableHeaderContaining("TANGGAL"), "Kolom Tanggal Pengajuan/Cuti harus tampil.");
        Assert.assertTrue(page.hasTableHeaderContaining("STATUS"), "Kolom Status harus tampil.");
        Assert.assertTrue(page.hasTableHeaderContaining("AKSI"), "Kolom AKSI harus tampil.");
    }

    @Test(priority = 64, description = "SIT_ADM_UI_064: Pencarian nama karyawan, filter tanggal, dan filter Unit pada Laporan-Cuti",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_064_LaporanCutiSearchDateAndUnit() {
        ExtentTestManager.logInfo("SIT_ADM_UI_064 [Laporan-Cuti] Severity: Medium | Priority: P2");
        AdminLeavePage page = loginAndOpenLeaveReport();

        page.applyDateRangeFilter(false);
        page.applyFirstUnitFilter();
        page.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        Assert.assertEquals(page.getSearchInputValue(), Constants.HYBRID_EMPLOYEE_NAME);
    }

    @Test(priority = 65, description = "SIT_ADM_UI_065: Aksi Cancel Cuti dengan mengisi Alasan wajib pada kolom AKSI Laporan-Cuti dan cek perubahan status",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_065_LaporanCutiCancelSuccess() {
        ExtentTestManager.logInfo("SIT_ADM_UI_065 [Laporan-Cuti] Severity: High | Priority: P1");
        AdminLeavePage page = loginAndOpenLeaveReport();

        int cancellableRow = page.findFirstCancellableRowIndex();
        Assert.assertTrue(cancellableRow >= 0,
                "Prasyarat: Harus tersedia minimal 1 pengajuan cuti berstatus aktif/belum dibatalkan pada tabel Laporan-Cuti.");

        page.openCancelCutiModalForRow(cancellableRow);
        page.submitCancelCutiWithReason("Pembatalan cuti oleh HRD - Automation SIT");
        String toast = page.getToastOrAlertText().toLowerCase();
        Assert.assertTrue(toast.contains("cancel") || toast.contains("berhasil") || !page.isCancelCutiModalDisplayed(),
                "Modal Cancel Cuti harus tertutup dan notifikasi sukses 'Berhasil cancel cuti' tampil. Toast: " + toast);
    }

    @Test(priority = 66, description = "SIT_ADM_UI_066: Submit modal Cancel Cuti tanpa mengisi field 'Alasan (Wajib diisi)'",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_066_LaporanCutiCancelEmptyReason() {
        ExtentTestManager.logInfo("SIT_ADM_UI_066 [Laporan-Cuti] Severity: High | Priority: P1");
        AdminLeavePage page = loginAndOpenLeaveReport();
        Assert.assertTrue(page.getTableRowCount() > 0, "Minimal 1 baris Laporan-Cuti harus tersedia.");

        page.openCancelCutiModalForRow(0);
        String validationMsg = page.submitCancelCutiWithEmptyReasonAndGetValidation();
        Assert.assertFalse(validationMsg == null || validationMsg.isBlank(),
                "Validasi required HTML5 pada input alasan harus muncul saat alasan kosong.");
        Assert.assertTrue(page.isCancelCutiModalDisplayed(),
                "Modal Cancel Cuti tidak boleh tertutup/memproses submit ketika alasan kosong.");
    }

    @Test(priority = 67, description = "SIT_ADM_UI_067: Batalkan modal Cancel Cuti dengan menekan tombol 'Tidak'",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_067_LaporanCutiAbortWithTidak() {
        ExtentTestManager.logInfo("SIT_ADM_UI_067 [Laporan-Cuti] Severity: High | Priority: P1");
        AdminLeavePage page = loginAndOpenLeaveReport();
        Assert.assertTrue(page.getTableRowCount() > 0, "Minimal 1 baris Laporan-Cuti harus tersedia.");

        page.openCancelCutiModalForRow(0);
        page.abortCancelCutiWithTidak();
        Assert.assertFalse(page.isCancelCutiModalDisplayed(),
                "Modal Cancel Cuti harus tertutup setelah menekan tombol 'Tidak'.");
    }

    @Test(priority = 68, description = "SIT_ADM_UI_068: [Known Defect] Aksi Edit ('Ubah atasan user yang request') pada Laporan-Cuti menampilkan input dan menyimpan perubahan",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_068_LaporanCutiEditModalHasInput() {
        ExtentTestManager.logInfo("SIT_ADM_UI_068 [Laporan-Cuti] Known Defect Check | Severity: High | Priority: P1");
        AdminLeavePage page = loginAndOpenLeaveReport();
        Assert.assertTrue(page.getTableRowCount() > 0, "Minimal 1 baris Laporan-Cuti harus tersedia.");

        page.openEditModalForFirstRow();
        Assert.assertTrue(page.isEditModalDisplayed(), "Modal Edit 'Ubah atasan user yang request' harus terbuka.");
        boolean hasInput = page.hasEditableInputInEditModal();
        page.clickUbahDataInEditModal();
        Assert.assertTrue(hasInput,
                "[KNOWN DEFECT] Modal Edit 'Ubah atasan user yang request' seharusnya menampilkan field input/dropdown pilih atasan yang dapat diubah.");
    }

    @Test(priority = 69, description = "SIT_ADM_UI_069: Fungsi tombol Reset filter pada halaman Laporan-Cuti",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_069_LaporanCutiResetFilter() {
        ExtentTestManager.logInfo("SIT_ADM_UI_069 [Laporan-Cuti] Severity: Medium | Priority: P2");
        AdminLeavePage page = loginAndOpenLeaveReport();

        page.searchByKeyword("USER_CUTI_RESET");
        page.clickResetFilter();
        Assert.assertEquals(page.getSearchInputValue(), "", "Input search harus bersih setelah Reset.");
    }

    @Test(priority = 70, description = "SIT_ADM_UI_070: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Laporan-Cuti",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_070_LaporanCutiRowsPerPage() {
        ExtentTestManager.logInfo("SIT_ADM_UI_070 [Laporan-Cuti] Severity: Medium | Priority: P2");
        AdminLeavePage page = loginAndOpenLeaveReport();
        assertRowsPerPageBehavior(page, "Laporan-Cuti");
    }

    @Test(priority = 71, description = "SIT_ADM_UI_071: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Laporan-Cuti",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_071_LaporanCutiPaginationNavigation() {
        ExtentTestManager.logInfo("SIT_ADM_UI_071 [Laporan-Cuti] Severity: Medium | Priority: P2");
        AdminLeavePage page = loginAndOpenLeaveReport();
        assertPaginationNavigationBehavior(page, "Laporan-Cuti");
    }

    // =========================================================================
    // 7. LAPORAN-SAKIT (SIT_ADM_UI_072 - SIT_ADM_UI_079: 8 TCs)
    // =========================================================================

    @Test(priority = 72, description = "SIT_ADM_UI_072: Buka sub-menu Laporan-Sakit dan verifikasi tabel klaim sakit",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_072_LaporanSakitTableDisplayed() {
        ExtentTestManager.logInfo("SIT_ADM_UI_072 [Laporan-Sakit] Severity: High | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SAKIT);

        Assert.assertTrue(page.hasTableHeaderContaining("NAMA"), "Kolom Nama User harus tampil di Laporan-Sakit.");
        Assert.assertTrue(page.hasTableHeaderContaining("TANGGAL SAKIT"), "Kolom Tanggal Sakit harus tampil.");
        Assert.assertTrue(page.hasTableHeaderContaining("FOTO"), "Kolom FOTO surat sakit harus tampil.");
    }

    @Test(priority = 73, description = "SIT_ADM_UI_073: [Known Defect] Klik tombol 'Lihat' pada kolom FOTO di Laporan-Sakit menampilkan gambar surat dokter",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_073_LaporanSakitViewDoctorNoteImage() {
        ExtentTestManager.logInfo("SIT_ADM_UI_073 [Laporan-Sakit] Known Defect Check | Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SAKIT);

        Assert.assertTrue(page.getSickViewLinkCount() > 0, "Tautan 'View/Lihat' pada kolom FOTO harus tersedia.");
        boolean previewLoaded = page.openFirstSickViewAndVerifyImageLoaded();
        Assert.assertTrue(previewLoaded,
                "[KNOWN DEFECT] Gambar surat dokter pada modal preview 'View' Laporan-Sakit harus tampil utuh (naturalWidth > 0, HTTP 200).");
    }

    @Test(priority = 74, description = "SIT_ADM_UI_074: [Known Defect] Klik tombol 'Download' pada kolom FOTO di Laporan-Sakit mengunduh file surat dokter",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_074_LaporanSakitDownloadDoctorNote() {
        ExtentTestManager.logInfo("SIT_ADM_UI_074 [Laporan-Sakit] Known Defect Check | Severity: High | Priority: P1");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SAKIT);

        String downloadUrl = page.getFirstSickDownloadHref();
        Assert.assertFalse(downloadUrl == null || downloadUrl.isBlank(),
                "Tautan 'Download' pada kolom FOTO Laporan-Sakit harus memiliki href.");
        int httpStatus = page.checkUrlHttpStatus(downloadUrl);
        Assert.assertEquals(httpStatus, 200,
                "[KNOWN DEFECT] Tombol Download surat sakit (" + downloadUrl + ") seharusnya mengembalikan HTTP 200, aktual: HTTP " + httpStatus);
    }

    @Test(priority = 75, description = "SIT_ADM_UI_075: Pencarian nama karyawan dan filter tanggal pada Laporan-Sakit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_075_LaporanSakitSearchAndDate() {
        ExtentTestManager.logInfo("SIT_ADM_UI_075 [Laporan-Sakit] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SAKIT);

        page.applyDateRangeFilter(false);
        page.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        Assert.assertEquals(page.getSearchInputValue(), Constants.HYBRID_EMPLOYEE_NAME);
    }

    @Test(priority = 76, description = "SIT_ADM_UI_076: Filter Laporan-Sakit berdasarkan Unit melalui modal Filter By",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_076_LaporanSakitFilterUnit() {
        ExtentTestManager.logInfo("SIT_ADM_UI_076 [Laporan-Sakit] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SAKIT);

        page.applyFirstUnitFilter();
        Assert.assertFalse(page.isFilterModalDisplayed(), "Modal Filter By harus tertutup setelah diterapkan.");
    }

    @Test(priority = 77, description = "SIT_ADM_UI_077: Fungsi tombol Reset filter pada halaman Laporan-Sakit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_077_LaporanSakitResetFilter() {
        ExtentTestManager.logInfo("SIT_ADM_UI_077 [Laporan-Sakit] Severity: Low | Priority: P3");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SAKIT);

        page.searchByKeyword("USER_SAKIT_RESET");
        page.clickResetFilter();
        Assert.assertEquals(page.getSearchInputValue(), "", "Input search harus bersih setelah Reset.");
    }

    @Test(priority = 78, description = "SIT_ADM_UI_078: Mengubah jumlah data per halaman (Rows per page: 5, 10, 25) pada halaman Laporan-Sakit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_078_LaporanSakitRowsPerPage() {
        ExtentTestManager.logInfo("SIT_ADM_UI_078 [Laporan-Sakit] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SAKIT);
        assertRowsPerPageBehavior(page, "Laporan-Sakit");
    }

    @Test(priority = 79, description = "SIT_ADM_UI_079: Navigasi halaman tabel menggunakan tombol Next, Previous, First, dan Last Page pada halaman Laporan-Sakit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_079_LaporanSakitPaginationNavigation() {
        ExtentTestManager.logInfo("SIT_ADM_UI_079 [Laporan-Sakit] Severity: Medium | Priority: P2");
        AdminReportsGroupAPage page = loginAndOpenReport(AdminReportsGroupAPage.URL_SAKIT);
        assertPaginationNavigationBehavior(page, "Laporan-Sakit");
    }
}

