package com.juaracoding.magang.tests.admin.deny;

import com.juaracoding.magang.BaseTest;
import com.juaracoding.magang.api.BaseApiTest;
import com.juaracoding.magang.api.helper.EmployeeApiHelper;
import com.juaracoding.magang.drivers.DriverFactory;
import com.juaracoding.magang.pages.admin.aji.AdminDashboardPage;
import com.juaracoding.magang.pages.admin.deny.AdminCrossModuleE2EPage;
import com.juaracoding.magang.pages.admin.deny.AdminLeavePage;
import com.juaracoding.magang.pages.admin.deny.AdminLoginPage;
import com.juaracoding.magang.pages.admin.deny.AdminManagementGroupAPage;
import com.juaracoding.magang.pages.admin.deny.AdminReportsGroupAPage;
import com.juaracoding.magang.reports.ExtentTestManager;
import com.juaracoding.magang.utils.Constants;
import com.juaracoding.magang.utils.SecurityUtil;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Pengujian Integrasi Lintas Modul E2E (Hybrid API + UI) untuk Tester Deny.
 * Cakupan SIT v4 (docs/SIT_Scenario_Magang_Hadir.xlsx):
 * SIT_ADM_UI_311, SIT_ADM_UI_312, SIT_ADM_UI_313, SIT_ADM_UI_314,
 * SIT_ADM_UI_315, dan SIT_ADM_UI_321 (6 Test Cases).
 * Penanggung jawab: Tester Deny | Branch: feature/deny-admin-e2e.
 */
public class AdminCrossModuleDenyE2ETest extends BaseTest {

    private void loginAsAdminWithRetry() {
        AdminLoginPage loginPage = new AdminLoginPage(getDriver());
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                loginPage.navigateToLoginPage();
                loginPage.loginWithDefaultAdmin();
                if (loginPage.waitForRedirectToPending()) {
                    return;
                }
            } catch (Exception e) {
                logger.warn("Admin login attempt {} failed: {}", attempt, e.getMessage());
                try {
                    ((JavascriptExecutor) getDriver()).executeScript("window.stop();");
                } catch (Exception ignored) {
                    // Ignore
                }
                sleepQuietly(600);
            }
        }
        Assert.fail("Prasyarat login Admin gagal.");
    }

    private void resetBrowserSession() {
        DriverFactory.quitDriver();
        String browser = System.getProperty("browser", "chrome");
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        DriverFactory.initDriver(browser, headless);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test(priority = 311,
            description = "SIT_ADM_UI_311: [E2E] Karyawan registrasi mandiri (/absen/register) -> muncul di Admin Management-Pendaftaran Sendiri untuk verifikasi",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_311_SelfRegistrationAppearsInAdmin() {
        ExtentTestManager.logInfo("SIT_ADM_UI_311 [Integrasi Lintas Modul E2E] Severity: Critical | Priority: P1");

        long ts = System.currentTimeMillis();
        String nik = EmployeeApiHelper.findAvailableNikFromPool();
        String fullname = "E2E SelfReg " + (ts % 10000L);
        String email = "e2eselfreg_" + ts + "@hadir.com";
        String createdUserId = null;

        try {
            ExtentTestManager.logInfo("Step 1 (@Before API): Seed registrasi mandiri POST /users (is_mobile=true) NIK=" + nik);
            createdUserId = EmployeeApiHelper.createSelfRegisteredMobileUser(nik, fullname, email, "E2ePass123!");
            Assert.assertNotNull(createdUserId, "Seed registrasi mandiri via API harus berhasil membuat user baru.");

            ExtentTestManager.logInfo("Step 2 (Admin UI): Login Admin & buka /management/self-registration");
            loginAsAdminWithRetry();
            AdminManagementGroupAPage mgmtPage = new AdminManagementGroupAPage(getDriver());
            mgmtPage.openManagementUrl(AdminManagementGroupAPage.URL_PENDAFTARAN_SENDIRI);

            mgmtPage.searchByKeyword(fullname);
            if (mgmtPage.getTableRowCount() == 0) {
                mgmtPage.clickReset();
                mgmtPage.searchByKeyword(nik);
            }

            String tableText = mgmtPage.getTableBodyText();
            ExtentTestManager.logInfo("Hasil pencarian Pendaftaran Sendiri: " + tableText);
            Assert.assertTrue(mgmtPage.getTableRowCount() > 0 && (tableText.contains(fullname) || tableText.contains(nik)),
                    "Data pendaftar mandiri (" + fullname + " / " + nik + ") harus muncul di Management-Pendaftaran Sendiri.");
        } finally {
            EmployeeApiHelper.cleanupAdminRecordQuietly(Constants.API_USERS_ENDPOINT, createdUserId);
        }
    }

    @Test(priority = 312,
            description = "SIT_ADM_UI_312: [E2E] Karyawan submit pengajuan Cuti -> muncul di Admin Laporan-Cuti & Admin Cancel Cuti memperbarui status",
            groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_UI_312_EmployeeLeaveCancelAndQuotaSync() {
        ExtentTestManager.logInfo("SIT_ADM_UI_312 [Integrasi Lintas Modul E2E] Severity: Critical | Priority: P1");

        EmployeeApiHelper.EmployeeSession empSession = EmployeeApiHelper.loginAsDefaultEmployee();
        Response initialLeaveApi = EmployeeApiHelper.getProjectLeaveSummaryByUserId(empSession.token(), empSession.userId());
        ExtentTestManager.logInfo("Step 1 (@Before API): Kuota cuti awal user = " + initialLeaveApi.asString());
        Assert.assertEquals(initialLeaveApi.getStatusCode(), 200, "GET /project-leave/{userId} harus HTTP 200.");

        AdminCrossModuleE2EPage e2ePage = new AdminCrossModuleE2EPage(getDriver());
        ExtentTestManager.logInfo("Step 2 (Employee UI): Login Karyawan & coba submit cuti di /apps/absent/leave-client");
        if (e2ePage.loginDefaultEmployeePortal()) {
            String leaveSnack = e2ePage.submitEmployeeLeaveRequest("E2E Leave Cancel " + System.currentTimeMillis());
            ExtentTestManager.logInfo("Snackbar pengajuan cuti karyawan: " + leaveSnack);
        }

        ExtentTestManager.logInfo("Step 3 (Admin UI): Login Admin, buka /laporan/cuti & proses Cancel Cuti");
        resetBrowserSession();
        e2ePage = new AdminCrossModuleE2EPage(getDriver());
        loginAsAdminWithRetry();
        AdminLeavePage leavePage = new AdminLeavePage(getDriver());
        leavePage.openLeaveReport();

        leavePage.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        int cancellableRow = leavePage.findFirstCancellableRowIndex();
        if (cancellableRow < 0) {
            leavePage.selectRowsPerPage(25);
            cancellableRow = leavePage.findFirstCancellableRowIndex();
        }
        if (cancellableRow < 0) {
            leavePage.clickResetFilter();
            cancellableRow = leavePage.findFirstCancellableRowIndex();
            if (cancellableRow < 0) {
                leavePage.selectRowsPerPage(25);
                cancellableRow = leavePage.findFirstCancellableRowIndex();
            }
        }
        Assert.assertTrue(cancellableRow >= 0,
                "Prasyarat: Harus tersedia minimal 1 pengajuan cuti aktif di Laporan-Cuti untuk di-Cancel.");

        List<WebElement> rows = getDriver().findElements(By.cssSelector("table tbody tr"));
        String targetRowText = rows.get(cancellableRow).getText();
        String targetEmployeeName = targetRowText.contains(Constants.HYBRID_EMPLOYEE_NAME)
                ? Constants.HYBRID_EMPLOYEE_NAME
                : targetRowText.split("\\R")[0].trim();

        e2ePage.openCancelCutiModalOnTableRow(cancellableRow);
        leavePage.submitCancelCutiWithReason("E2E Cancel Cuti Admin - " + System.currentTimeMillis());
        String toast = leavePage.getToastOrAlertText().toLowerCase();
        ExtentTestManager.logInfo("Feedback Cancel Cuti Admin: " + toast);
        Assert.assertTrue(toast.contains("cancel") || toast.contains("berhasil") || !leavePage.isCancelCutiModalDisplayed(),
                "Cancel Cuti harus berhasil memproses pembatalan cuti.");

        ExtentTestManager.logInfo("Step 4 (Cross-Module Verify): Cek /laporan/sisa-cuti & API GET /project-leave");
        boolean sisaCutiLoaded = e2ePage.openLaporanSisaCutiAndSearch(targetEmployeeName);
        Assert.assertTrue(sisaCutiLoaded, "Halaman /laporan/sisa-cuti harus tampil setelah pencarian karyawan.");

        Response afterLeaveApi = EmployeeApiHelper.getProjectLeaveSummaryByUserId(empSession.token(), empSession.userId());
        Assert.assertEquals(afterLeaveApi.getStatusCode(), 200, "GET /project-leave/{userId} pasca Cancel harus HTTP 200.");
        Assert.assertNotNull(afterLeaveApi.jsonPath().get("sisa_cuti"),
                "Response /project-leave harus memuat field sisa_cuti.");
    }

    @Test(priority = 313,
            description = "SIT_ADM_UI_313: [E2E] Karyawan ajukan Izin Terlambat/Pulang Cepat -> data tampil di Admin Laporan-Izin Terlambat & Pulang Cepat",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_313_LateAndEarlyPermitAppearInReports() {
        ExtentTestManager.logInfo("SIT_ADM_UI_313 [Integrasi Lintas Modul E2E] Severity: Critical | Priority: P1");

        long ts = System.currentTimeMillis();
        String lateReason = "E2E Late Permit " + ts;
        String earlyReason = "E2E Early Leave " + ts;
        String lateId = null;
        String earlyId = null;

        try {
            ExtentTestManager.logInfo("Step 1 (@Before API): Seed POST /late-permit & POST /early-leave sebagai Karyawan");
            lateId = EmployeeApiHelper.createPendingLatePermit(EmployeeApiHelper.isoUtcTimestamp(0, 2), lateReason);
            earlyId = EmployeeApiHelper.createPendingEarlyLeave(EmployeeApiHelper.isoUtcTimestamp(0, 8), earlyReason);
            Assert.assertNotNull(lateId, "Seed POST /late-permit harus mengembalikan ID record.");
            Assert.assertNotNull(earlyId, "Seed POST /early-leave harus mengembalikan ID record.");

            ExtentTestManager.logInfo("Step 2 (Admin UI): Login Admin & verifikasi di /laporan/izin-terlambat");
            loginAsAdminWithRetry();
            AdminReportsGroupAPage reportsPage = new AdminReportsGroupAPage(getDriver());
            reportsPage.openReportUrl(AdminReportsGroupAPage.URL_IZIN_TERLAMBAT);
            reportsPage.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
            if (!reportsPage.getTableBodyText().contains(lateReason)) {
                reportsPage.applyDateRangeFilter(true);
                reportsPage.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
            }
            String lateBody = reportsPage.getTableBodyText();
            Assert.assertTrue(lateBody.contains(Constants.HYBRID_EMPLOYEE_NAME) && lateBody.contains(lateReason),
                    "Data Izin Terlambat beserta alasan [" + lateReason + "] harus tampil di /laporan/izin-terlambat. Aktual: " + lateBody);

            ExtentTestManager.logInfo("Step 3 (Admin UI): Verifikasi di /laporan/izin-pulang-cepat");
            reportsPage.openReportUrl(AdminReportsGroupAPage.URL_IZIN_PULANG_CEPAT);
            reportsPage.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
            if (!reportsPage.getTableBodyText().contains(earlyReason)) {
                reportsPage.applyDateRangeFilter(true);
                reportsPage.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
            }
            String earlyBody = reportsPage.getTableBodyText();
            Assert.assertTrue(earlyBody.contains(Constants.HYBRID_EMPLOYEE_NAME) && earlyBody.contains(earlyReason),
                    "Data Izin Pulang Cepat beserta alasan [" + earlyReason + "] harus tampil di /laporan/izin-pulang-cepat. Aktual: " + earlyBody);
        } finally {
            EmployeeApiHelper.cleanupAdminRecordQuietly(Constants.API_LATE_PERMIT_ENDPOINT, lateId);
            EmployeeApiHelper.cleanupAdminRecordQuietly(Constants.API_EARLY_LEAVE_ENDPOINT, earlyId);
        }
    }

    @Test(priority = 314,
            description = "SIT_ADM_UI_314: [E2E] Karyawan klaim Sakit -> data tampil di Admin Dashboard kartu Sakit & Laporan-Sakit",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_314_SickClaimSyncedToDashboardAndReport() {
        ExtentTestManager.logInfo("SIT_ADM_UI_314 [Integrasi Lintas Modul E2E] Severity: Critical | Priority: P1");

        AdminCrossModuleE2EPage e2ePage = new AdminCrossModuleE2EPage(getDriver());
        ExtentTestManager.logInfo("Step 1 (Employee UI): Login Karyawan & submit klaim Sakit hari ini di /apps/absent/sick");
        Assert.assertTrue(e2ePage.loginDefaultEmployeePortal(),
                "Login Karyawan (" + Constants.HYBRID_EMPLOYEE_EMAIL + ") di /absen/login harus berhasil.");
        String sickSnack = e2ePage.submitEmployeeSickClaimToday("valid_photo.jpg");
        ExtentTestManager.logInfo("Hasil submit klaim Sakit Karyawan: " + sickSnack);
        Assert.assertTrue(
                sickSnack.toLowerCase().contains("sukses")
                        || e2ePage.getSickPageText().contains(Constants.HYBRID_EMPLOYEE_NAME),
                "Klaim Sakit Karyawan harus tersubmit atau tampil di daftar /apps/absent/sick. Pesan: " + sickSnack);

        ExtentTestManager.logInfo("Step 2 (Admin UI): Clear session, login Admin & cek kartu Sakit di /dashboards/dashboard");
        resetBrowserSession();
        loginAsAdminWithRetry();
        AdminDashboardPage dashboardPage = new AdminDashboardPage(getDriver());
        dashboardPage.navigateToDashboard();
        Assert.assertTrue(dashboardPage.isSakitCardDisplayed(),
                "Kartu 'Karyawan yang sedang sakit hari ini' harus tampil di Dashboard Admin.");

        List<WebElement> sickItems = dashboardPage.getSakitListItems();
        StringBuilder sickCardText = new StringBuilder();
        for (WebElement item : sickItems) {
            sickCardText.append(item.getText()).append("\n");
        }
        ExtentTestManager.logInfo("Isi kartu Sakit Dashboard hari ini (" + sickItems.size() + " item): " + sickCardText);
        Assert.assertTrue(!sickItems.isEmpty() && sickCardText.toString().contains(Constants.HYBRID_EMPLOYEE_NAME),
                "Nama karyawan pengaju sakit (" + Constants.HYBRID_EMPLOYEE_NAME + ") harus tampil di kartu Sakit Dashboard.");

        ExtentTestManager.logInfo("Step 3 (Admin UI): Buka /laporan/sakit & cari karyawan pengaju sakit");
        AdminReportsGroupAPage reportsPage = new AdminReportsGroupAPage(getDriver());
        reportsPage.openReportUrl(AdminReportsGroupAPage.URL_SAKIT);
        reportsPage.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        if (reportsPage.getTableRowCount() == 0) {
            reportsPage.applyDateRangeFilter(true);
            reportsPage.searchByKeyword(Constants.HYBRID_EMPLOYEE_NAME);
        }
        String sakitReportText = reportsPage.getTableBodyText();
        Assert.assertTrue(reportsPage.getTableRowCount() > 0 && sakitReportText.contains(Constants.HYBRID_EMPLOYEE_NAME),
                "Karyawan pengaju sakit (" + Constants.HYBRID_EMPLOYEE_NAME + ") harus tampil di tabel /laporan/sakit.");
    }

    @Test(priority = 315,
            description = "SIT_ADM_UI_315: [E2E] Admin nonaktifkan status user di Management-User -> API/Login Karyawan ditolak",
            groups = {"regression", "negative"})
    public void testSIT_ADM_UI_315_DeactivateUserRejectsEmployeeAuth() {
        ExtentTestManager.logInfo("SIT_ADM_UI_315 [Integrasi Lintas Modul E2E] Severity: Critical | Priority: P1");

        long ts = System.currentTimeMillis();
        String nik = EmployeeApiHelper.findAvailableNikFromPool();
        String fullname = "E2E ToggleUser " + (ts % 10000L);
        String email = "e2etoggle_" + ts + "@hadir.com";
        String plainPass = "E2eActivePass123!";
        String encodedPass = SecurityUtil.encode(plainPass);
        String tempUserId = null;

        try {
            ExtentTestManager.logInfo("Step 1 (@Before API): Buat user uji temporer & aktifkan statusnya");
            tempUserId = EmployeeApiHelper.createSelfRegisteredMobileUser(nik, fullname, email, plainPass);
            Assert.assertNotNull(tempUserId, "Pembuatan user uji temporer harus berhasil.");
            Assert.assertTrue(EmployeeApiHelper.setUserActiveStatus(tempUserId, true),
                    "Aktivasi awal user uji temporer via PATCH /users/{id} harus berhasil.");

            Response baselineAuth = BaseApiTest.authenticate(email, encodedPass);
            Assert.assertTrue(baselineAuth.getStatusCode() == 200 || baselineAuth.getStatusCode() == 201,
                    "Baseline login user aktif harus berhasil sebelum dinonaktifkan. Status: " + baselineAuth.getStatusCode());

            ExtentTestManager.logInfo("Step 2 (Admin UI): Login Admin -> /management/user & nonaktifkan user uji");
            loginAsAdminWithRetry();
            AdminManagementGroupAPage mgmtPage = new AdminManagementGroupAPage(getDriver());
            mgmtPage.openManagementUrl(AdminManagementGroupAPage.URL_USER);
            mgmtPage.selectUserSearchMode("NIK");
            mgmtPage.searchByKeyword(nik);
            Assert.assertTrue(mgmtPage.getTableBodyText().contains(email),
                    "User uji (" + email + ") harus ditemukan di tabel Management-User.");

            AdminCrossModuleE2EPage e2ePage = new AdminCrossModuleE2EPage(getDriver());
            Assert.assertTrue(e2ePage.toggleUserActiveSwitchByIdentifierTo(email, false),
                    "Toggle switch status user uji ke Non-aktif harus berhasil.");

            ExtentTestManager.logInfo("Step 3 (Verify API): Panggil POST /authentication memakai user Non-aktif");
            Response rejectedAuth = BaseApiTest.authenticate(email, encodedPass);
            int rejectedStatus = rejectedAuth.getStatusCode();
            String rejectedToken = rejectedAuth.jsonPath().getString("accessToken");
            ExtentTestManager.logInfo("Response auth user Non-aktif -> HTTP " + rejectedStatus + " | body=" + rejectedAuth.asString());
            Assert.assertTrue(rejectedStatus != 200 && rejectedStatus != 201 && rejectedToken == null,
                    "User yang dinonaktifkan Admin tidak boleh diizinkan login via POST /authentication. Status: " + rejectedStatus);

            ExtentTestManager.logInfo("Step 4 (Restore): Aktifkan kembali user uji & verifikasi login pulih");
            // Next.js router.replace setelah toggle mereset tampilan tabel, sehingga perlu filter ulang NIK
            mgmtPage.selectUserSearchMode("NIK");
            mgmtPage.searchByKeyword(nik);
            Assert.assertTrue(e2ePage.toggleUserActiveSwitchByIdentifierTo(email, true),
                    "Pemulihan toggle switch status user uji kembali ke Aktif harus berhasil.");
            Response restoredAuth = BaseApiTest.authenticate(email, encodedPass);
            Assert.assertTrue(restoredAuth.getStatusCode() == 200 || restoredAuth.getStatusCode() == 201,
                    "Setelah status user dipulihkan menjadi Aktif, login API harus berhasil kembali.");
        } finally {
            EmployeeApiHelper.cleanupAdminRecordQuietly(Constants.API_USERS_ENDPOINT, tempUserId);
        }
    }

    @Test(priority = 321,
            description = "SIT_ADM_UI_321: [E2E] [Known Defect] Pendaftaran User dengan Jumlah Cuti -> kuota tersinkron ke Lihat Sisa Cuti Admin & user dapat ajukan cuti",
            groups = {"regression", "positive"})
    public void testSIT_ADM_UI_321_RegisterUserWithLeaveQuotaSyncE2E() {
        ExtentTestManager.logInfo("SIT_ADM_UI_321 [Integrasi Lintas Modul E2E - Known Defect Check] Severity: Critical | Priority: P1");

        long ts = System.currentTimeMillis();
        String testNik = EmployeeApiHelper.findAvailableNikFromPool();
        String fullname = "E2E CutiUser " + (ts % 10000L);
        String testEmail = "e2ecuti_" + ts + "@hadir.com";
        String testPass = "TestPass123!";
        String encodedPass = SecurityUtil.encode(testPass);
        String newUserId = null;

        try {
            ExtentTestManager.logInfo("Step 1 (Admin UI): Daftarkan user baru dengan Jumlah Cuti = 12 di /management/user-v2/register");
            loginAsAdminWithRetry();
            AdminManagementGroupAPage mgmtPage = new AdminManagementGroupAPage(getDriver());
            mgmtPage.openManagementUrl(AdminManagementGroupAPage.URL_PENDAFTARAN_USER);
            mgmtPage.fillRegistrationAccountAndWorkFields(testNik, fullname, testEmail, testPass, "12", false);
            mgmtPage.submitRegistrationForm();
            mgmtPage.waitForSnackbarOrBodyFeedback();

            newUserId = EmployeeApiHelper.findUserIdByEmail(testEmail);
            Assert.assertNotNull(newUserId, "User baru hasil pendaftaran Admin harus tercatat di backend /users.");

            ExtentTestManager.logInfo("Step 2 (Admin UI & API): Cek modal 'Lihat Sisa Cuti' & GET /project-leave/{id}");
            mgmtPage.openManagementUrl(AdminManagementGroupAPage.URL_USER);
            mgmtPage.selectUserSearchMode("Nama");
            mgmtPage.searchByKeyword(fullname);
            Assert.assertTrue(mgmtPage.getTableRowCount() > 0, "User baru (" + fullname + ") harus tampil di Management-User.");

            mgmtPage.openActionMenuAndClick(0, "Lihat Sisa Cuti");
            String adminLeaveModalText = mgmtPage.getVisibleDialogText();
            mgmtPage.closeVisibleDialogIfOpen();

            Response empAuth = BaseApiTest.authenticate(testEmail, encodedPass);
            String empToken = empAuth.jsonPath().getString("accessToken");
            Response projectLeaveRes = EmployeeApiHelper.getProjectLeaveSummaryByUserId(empToken, newUserId);
            String apiLeaveBody = projectLeaveRes.asString();
            ExtentTestManager.logInfo("Admin Modal Sisa Cuti=[" + adminLeaveModalText + "] | API /project-leave=[" + apiLeaveBody + "]");

            ExtentTestManager.logInfo("Step 3 (Employee UI): Login sebagai user baru & cek kuota di /apps/absent/leave-client");
            AdminCrossModuleE2EPage e2ePage = new AdminCrossModuleE2EPage(getDriver());
            e2ePage.clearSessionAndStorage();
            Assert.assertTrue(e2ePage.loginEmployeePortal(testEmail, testPass),
                    "User baru hasil Pendaftaran User Admin harus dapat login di /absen/login.");
            String userInfoCutiText = e2ePage.openLeaveClientAndReadInfoCuti();
            ExtentTestManager.logInfo("User UI Tab Info Cuti=[" + userInfoCutiText + "]");

            boolean syncedInAdminModal = adminLeaveModalText.contains("12")
                    && !adminLeaveModalText.toLowerCase().contains("tidak ditemukan");
            boolean syncedInApi = projectLeaveRes.getStatusCode() == 200
                    && apiLeaveBody.contains("12")
                    && projectLeaveRes.jsonPath().getInt("total_cuti") == 12;
            boolean syncedInEmployeeUi = userInfoCutiText.contains("12");

            Assert.assertTrue(syncedInAdminModal && syncedInApi && syncedInEmployeeUi,
                    "[KNOWN DEFECT] Kuota Jumlah Cuti (12) dari Pendaftaran User seharusnya langsung tersinkron ke "
                            + "modal Lihat Sisa Cuti Admin, endpoint GET /project-leave, dan halaman Cuti Karyawan. "
                            + "ModalAdmin=[" + adminLeaveModalText + "], API=[" + apiLeaveBody + "], UserUI=[" + userInfoCutiText + "]");
        } finally {
            EmployeeApiHelper.cleanupAdminRecordQuietly(Constants.API_USERS_ENDPOINT, newUserId);
        }
    }
}

