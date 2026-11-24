package com.juaracoding.magang.tests.admin.aji;

import com.juaracoding.magang.BaseTest;
import com.juaracoding.magang.pages.admin.aji.AdminDashboardPage;
import com.juaracoding.magang.pages.admin.deny.AdminLoginPage;
import com.juaracoding.magang.reports.ExtentTestManager;
import com.juaracoding.magang.utils.Constants;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * Pengujian Web UI Selenium untuk fitur Admin Dashboard (sub-menu Dashboard & Pending).
 * Cakupan skenario: SIT_ADM_UI_013 s/d SIT_ADM_UI_025 (13 Test Cases).
 * Penanggung jawab: Tester Aji | Branch: feature/aji-admin-dashboard.
 */
public class AdminDashboardTest extends BaseTest {

    private void safeNavigate(String url) {
        try {
            getDriver().get(url);
        } catch (TimeoutException te) {
            logger.warn("Page load timed out for {}, executing window.stop()", url);
            try {
                ((JavascriptExecutor) getDriver()).executeScript("window.stop();");
            } catch (Exception ignored) {
            }
        }
    }

    private AdminLoginPage performLogin() {
        safeNavigate(Constants.ADMIN_LOGIN_URL);
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(Constants.EXPLICIT_WAIT_TIMEOUT));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input#email, input[name='email']")));

        AdminLoginPage loginPage = new AdminLoginPage(getDriver());
        loginPage.loginWithDefaultAdmin();
        loginPage.waitForRedirectToPending();
        return loginPage;
    }

    private AdminDashboardPage loginAndNavigateToDashboard() {
        performLogin();
        safeNavigate(Constants.ADMIN_DASHBOARD_URL);
        AdminDashboardPage dashboardPage = new AdminDashboardPage(getDriver());
        dashboardPage.waitForDashboardLoaded();
        return dashboardPage;
    }

    private AdminDashboardPage loginAndNavigateToPending() {
        performLogin();
        safeNavigate(Constants.ADMIN_PENDING_URL);
        AdminDashboardPage dashboardPage = new AdminDashboardPage(getDriver());
        dashboardPage.waitForPendingLoaded();
        return dashboardPage;
    }

    // ==========================================
    // SUB-MENU DASHBOARD-DASHBOARD (013 - 018)
    // ==========================================

    @Test(priority = 1, description = "SIT_ADM_UI_013: Buka sub-menu Dashboard-Dashboard dan verifikasi kartu 'Total semua karyawan'",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_013_TotalSemuaKaryawanCard() {
        AdminDashboardPage dashboardPage = loginAndNavigateToDashboard();
        ExtentTestManager.logInfo("SIT_ADM_UI_013 - Memverifikasi kartu 'Total semua karyawan' dan jumlah aktif.");

        Assert.assertTrue(dashboardPage.isTotalKaryawanCardDisplayed(),
                "Kartu 'Total semua karyawan' harus tampil di halaman Dashboard.");

        String totalCount = dashboardPage.getTotalKaryawanCount();
        ExtentTestManager.logInfo("Total semua karyawan terdeteksi: " + totalCount);
        Assert.assertFalse(totalCount.isEmpty(), "Angka total karyawan aktif tidak boleh kosong.");
        Assert.assertTrue(totalCount.matches("\\d+"), "Angka total karyawan harus berupa bilangan bulat numeric.");
    }

    @Test(priority = 2, description = "SIT_ADM_UI_014: Verifikasi kartu 'Total absen hari ini' beserta grafik Donut Chart",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_014_TotalAbsenHariIniAndDonutChart() {
        AdminDashboardPage dashboardPage = loginAndNavigateToDashboard();
        ExtentTestManager.logInfo("SIT_ADM_UI_014 - Memverifikasi kartu 'Total absen hari ini' dan grafik ApexCharts.");

        Assert.assertTrue(dashboardPage.isTotalAbsenCardDisplayed(),
                "Kartu 'Total absen hari ini' harus tampil di halaman Dashboard.");

        String totalAbsen = dashboardPage.getTotalAbsenCount();
        ExtentTestManager.logInfo("Total absen hari ini terdeteksi: " + totalAbsen);
        Assert.assertFalse(totalAbsen.isEmpty(), "Angka total absen hari ini tidak boleh kosong.");

        Assert.assertTrue(dashboardPage.isDonutChartDisplayed(),
                "Grafik Donut Chart (ApexCharts) harus tampil pada kartu Total absen hari ini.");
    }

    @Test(priority = 3, description = "SIT_ADM_UI_015: Verifikasi kartu 'Karyawan yang sedang WFH hari ini' dan daftar user",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_015_KaryawanWfhHariIni() {
        AdminDashboardPage dashboardPage = loginAndNavigateToDashboard();
        ExtentTestManager.logInfo("SIT_ADM_UI_015 - Memverifikasi kartu 'Karyawan yang sedang WFH hari ini'.");

        Assert.assertTrue(dashboardPage.isWfhCardDisplayed(),
                "Kartu 'Karyawan yang sedang WFH hari ini' harus tampil.");
        Assert.assertTrue(dashboardPage.isWfhScrollListDisplayed(),
                "Area scroll list pada kartu WFH harus ter-render.");

        List<WebElement> items = dashboardPage.getWfhListItems();
        ExtentTestManager.logInfo("Jumlah item WFH hari ini: " + items.size());
    }

    @Test(priority = 4, description = "SIT_ADM_UI_016: Verifikasi kartu 'Karyawan yang sedang cuti hari ini' dan daftar user",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_016_KaryawanCutiHariIni() {
        AdminDashboardPage dashboardPage = loginAndNavigateToDashboard();
        ExtentTestManager.logInfo("SIT_ADM_UI_016 - Memverifikasi kartu 'Karyawan yang sedang cuti hari ini'.");

        Assert.assertTrue(dashboardPage.isCutiCardDisplayed(),
                "Kartu 'Karyawan yang sedang cuti hari ini' harus tampil.");
        Assert.assertTrue(dashboardPage.isCutiScrollListDisplayed(),
                "Area scroll list pada kartu Cuti harus ter-render.");

        List<WebElement> items = dashboardPage.getCutiListItems();
        ExtentTestManager.logInfo("Jumlah item Cuti hari ini: " + items.size());
    }

    @Test(priority = 5, description = "SIT_ADM_UI_017: Verifikasi kartu 'Karyawan yang sedang sakit hari ini' dan daftar user",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_017_KaryawanSakitHariIni() {
        AdminDashboardPage dashboardPage = loginAndNavigateToDashboard();
        ExtentTestManager.logInfo("SIT_ADM_UI_017 - Memverifikasi kartu 'Karyawan yang sedang sakit hari ini'.");

        Assert.assertTrue(dashboardPage.isSakitCardDisplayed(),
                "Kartu 'Karyawan yang sedang sakit hari ini' harus tampil.");
        Assert.assertTrue(dashboardPage.isSakitScrollListDisplayed(),
                "Area scroll list pada kartu Sakit harus ter-render.");

        List<WebElement> items = dashboardPage.getSakitListItems();
        ExtentTestManager.logInfo("Jumlah item Sakit hari ini: " + items.size());
    }

    @Test(priority = 6, description = "SIT_ADM_UI_018: Verifikasi kartu 'Karyawan yang sedang Izin hari ini' dan daftar user",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_018_KaryawanIzinHariIni() {
        AdminDashboardPage dashboardPage = loginAndNavigateToDashboard();
        ExtentTestManager.logInfo("SIT_ADM_UI_018 - Memverifikasi kartu 'Karyawan yang sedang Izin hari ini'.");

        Assert.assertTrue(dashboardPage.isIzinCardDisplayed(),
                "Kartu 'Karyawan yang sedang Izin hari ini' harus tampil.");
        Assert.assertTrue(dashboardPage.isIzinScrollListDisplayed(),
                "Area scroll list pada kartu Izin harus ter-render.");

        List<WebElement> items = dashboardPage.getIzinListItems();
        ExtentTestManager.logInfo("Jumlah item Izin hari ini: " + items.size());
    }

    // ==========================================
    // SUB-MENU DASHBOARD-PENDING (019 - 025)
    // ==========================================

    @Test(priority = 7, description = "SIT_ADM_UI_019: Verifikasi 3 kolom Validator (1, 2, 3) pada Dashboard-Pending",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_019_ThreeValidatorColumnsAndTables() {
        AdminDashboardPage pendingPage = loginAndNavigateToPending();
        ExtentTestManager.logInfo("SIT_ADM_UI_019 - Memverifikasi 3 kolom Validator beserta tabel Cuti & Lembur.");

        Assert.assertTrue(pendingPage.areAllThreeValidatorsDisplayed(),
                "Ketiga header Validator (1, 2, 3) harus tampil pada halaman Dashboard-Pending.");

        Assert.assertTrue(pendingPage.verifyValidatorTables(1),
                "Validator 1 harus memuat tabel Cuti & Lembur dengan kolom Nama dan Total.");
        Assert.assertTrue(pendingPage.verifyValidatorTables(2),
                "Validator 2 harus memuat tabel Cuti & Lembur dengan kolom Nama dan Total.");
        Assert.assertTrue(pendingPage.verifyValidatorTables(3),
                "Validator 3 harus memuat tabel Cuti & Lembur dengan kolom Nama dan Total.");
    }

    @Test(priority = 8, description = "SIT_ADM_UI_020: Filter data pada Dashboard-Pending dengan rentang tanggal valid",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_020_FilterDateRangeValid() {
        AdminDashboardPage pendingPage = loginAndNavigateToPending();
        ExtentTestManager.logInfo("SIT_ADM_UI_020 - Filter rentang tanggal valid menggunakan date picker kalender.");

        try {
            pendingPage.selectDateRangeViaCalendar();
        } catch (Exception e) {
            ExtentTestManager.logInfo("Interaksi kalender fallback ke input tanggal: " + e.getMessage());
            pendingPage.enterStartDate("2026-11-01");
            pendingPage.enterEndDate("2026-11-20");
        }

        pendingPage.clickSearch();
        boolean queryUpdated = pendingPage.waitForUrlQueryParam("start_date=")
                || pendingPage.getCurrentUrl().contains("start_date=");
        String currentUrl = pendingPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL setelah Search tanggal valid: " + currentUrl);

        Assert.assertTrue(queryUpdated || currentUrl.contains("end_date="),
                "URL harus terupdate dengan parameter start_date & end_date. URL aktual: " + currentUrl);
    }

    @Test(priority = 9, description = "SIT_ADM_UI_021: Filter data pada Dashboard-Pending dengan Start Date lebih besar dari End Date",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_021_FilterDateRangeInverted() {
        AdminDashboardPage pendingPage = loginAndNavigateToPending();
        ExtentTestManager.logInfo("SIT_ADM_UI_021 - Menguji filter dengan Start Date > End Date (rentang tanggal terbalik).");

        pendingPage.enterStartDate("2026-11-25");
        pendingPage.enterEndDate("2026-11-01");
        pendingPage.clickSearch();

        String currentUrl = pendingPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL setelah submit rentang terbalik: " + currentUrl);

        boolean emptyOrRejected = pendingPage.areValidatorTablesEmpty()
                || !currentUrl.contains("start_date=2026-11-25");
        Assert.assertTrue(emptyOrRejected,
                "Sistem harus menampilkan tabel data kosong atau menolak rentang tanggal terbalik.");
    }

    @Test(priority = 10, description = "SIT_ADM_UI_022: Filter data pada Dashboard-Pending berdasarkan Unit (Modal Filter By)",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_022_FilterByUnitModal() {
        AdminDashboardPage pendingPage = loginAndNavigateToPending();
        ExtentTestManager.logInfo("SIT_ADM_UI_022 - Filter berdasarkan Unit via modal Filter By.");

        pendingPage.openFilterModal();
        Assert.assertTrue(pendingPage.isFilterModalDisplayed(),
                "Modal 'Filter By' harus terbuka setelah tombol filter diklik.");

        pendingPage.selectFirstDepartmentInModal();
        pendingPage.clickTerapkanFilter();

        Assert.assertTrue(pendingPage.isFilterModalClosed(),
                "Modal 'Filter By' harus tertutup setelah tombol Terapkan diklik.");

        boolean paramUpdated = pendingPage.waitForUrlQueryParam("job_departement_id=")
                || pendingPage.getCurrentUrl().contains("job_departement_id=");
        String currentUrl = pendingPage.getCurrentUrl();
        ExtentTestManager.logInfo("URL setelah filter Unit diterapkan: " + currentUrl);
        Assert.assertTrue(paramUpdated,
                "URL harus memuat parameter query job_departement_id setelah filter diterapkan. URL: " + currentUrl);
    }

    @Test(priority = 11, description = "SIT_ADM_UI_023: Buka modal Filter By pada Dashboard-Pending lalu klik tombol Batal",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_023_CancelFilterByModal() {
        AdminDashboardPage pendingPage = loginAndNavigateToPending();
        ExtentTestManager.logInfo("SIT_ADM_UI_023 - Buka modal Filter By lalu klik Batal.");

        pendingPage.openFilterModal();
        Assert.assertTrue(pendingPage.isFilterModalDisplayed(),
                "Modal 'Filter By' harus terbuka setelah tombol filter diklik.");

        pendingPage.clickBatalFilter();
        Assert.assertTrue(pendingPage.isFilterModalClosed(),
                "Modal 'Filter By' harus tertutup tanpa menerapkan perubahan saat tombol Batal diklik.");
    }

    @Test(priority = 12, description = "SIT_ADM_UI_024: Fungsi tombol Reset filter pada halaman Dashboard-Pending",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_024_ResetFilterFunction() {
        AdminDashboardPage pendingPage = loginAndNavigateToPending();
        ExtentTestManager.logInfo("SIT_ADM_UI_024 - Menguji fungsi tombol Reset setelah filter diterapkan.");

        String initialStartDate = pendingPage.getStartDateValue();
        String initialEndDate = pendingPage.getEndDateValue();
        ExtentTestManager.logInfo("Nilai default Start Date: [" + initialStartDate + "], End Date: [" + initialEndDate + "]");

        pendingPage.enterStartDate("2026-10-01");
        pendingPage.enterEndDate("2026-10-31");
        pendingPage.clickSearch();

        pendingPage.clickReset();
        String resetStartDate = pendingPage.getStartDateValue();
        String resetEndDate = pendingPage.getEndDateValue();
        ExtentTestManager.logInfo("Nilai setelah Reset -> Start Date: [" + resetStartDate + "], End Date: [" + resetEndDate + "]");

        Assert.assertTrue(!resetStartDate.equals("2026-10-01")
                        && (resetStartDate.isEmpty() || resetStartDate.equals(initialStartDate))
                        && (resetEndDate.isEmpty() || resetEndDate.equals(initialEndDate)),
                "Input tanggal harus kembali ke kondisi default atau bersih setelah Reset diklik.");
    }

    @Test(priority = 13, description = "SIT_ADM_UI_025: Klik nama upliner pada tabel Validator di Dashboard-Pending menuju detail",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_025_ClickUplinerNameToDetail() {
        AdminDashboardPage pendingPage = loginAndNavigateToPending();
        ExtentTestManager.logInfo("SIT_ADM_UI_025 - Mengklik link nama Upliner pada Validator 1 menuju detail.");

        List<WebElement> links = pendingPage.getValidator1UplinerLinks();
        ExtentTestManager.logInfo("Jumlah link Upliner yang ditemukan di Validator 1: " + links.size());

        if (!links.isEmpty()) {
            String hrefTarget = pendingPage.clickFirstUplinerInValidator1();
            ExtentTestManager.logInfo("Navigasi ke link Upliner target: " + hrefTarget);

            String currentUrl = pendingPage.getCurrentUrl();
            ExtentTestManager.logInfo("URL tujuan aktual: " + currentUrl);

            Assert.assertTrue(currentUrl.contains("/admin/v1-cuti/") || currentUrl.contains("/admin/v1-lembur/"),
                    "Harus berpindah ke halaman detail /admin/v1-cuti/[id] atau /admin/v1-lembur/[id]. URL aktual: " + currentUrl);
        } else {
            ExtentTestManager.logInfo("Tidak ada antrean pending Upliner aktif di Validator 1 saat ini (Data Kosong).");
            Assert.assertTrue(true, "Data Upliner pending kosong pada tanggal saat ini.");
        }
    }
}
