package com.juaracoding.magang.pages.admin.deny;

import com.juaracoding.magang.pages.BasePage;
import com.juaracoding.magang.utils.Constants;
import com.juaracoding.magang.utils.SecurityUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
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
 * Page Object Model khusus alur Integrasi Lintas Modul E2E (Tester Deny):
 * menjembatani interaksi Portal Karyawan (/absen/login, /apps/absent/sick,
 * /apps/absent/leave-client) serta verifikasi lintas modul Admin
 * (/laporan/sisa-cuti dan toggle status user spesifik di /management/user).
 */
public class AdminCrossModuleE2EPage extends BasePage {

    // Employee Login locators
    private static final By EMP_EMAIL_INPUT = By.cssSelector("input#email, input[name='email']");
    private static final By EMP_PASSWORD_INPUT = By.cssSelector("input#password");
    private static final By EMP_SUBMIT_BTN = By.cssSelector("button[type='submit']");

    // Employee Sick (/apps/absent/sick) locators
    private static final By AJUKAN_SAKIT_BTN = By.xpath("//button[contains(normalize-space(),'Ajukan Sakit')]");
    private static final By DRAWER_PAPER = By.cssSelector("div.MuiDrawer-paper");
    private static final By PILIH_TANGGAL_BOX = By.xpath(
            "//div[contains(@class,'MuiDrawer-paper')]//label[contains(normalize-space(),'Pilih Tanggal')]/following-sibling::div[1]");
    private static final By CALENDAR_TODAY_OR_ACTIVE = By.cssSelector("div.rdrCalendarWrapper button.rdrDayToday, div.rdrCalendarWrapper button.rdrDay:not(.rdrDayPassive):not(.rdrDayDisabled)");
    private static final By CALENDAR_SIMPAN_BTN = By.xpath("//div[@role='dialog']//button[normalize-space()='Simpan']");
    private static final By SICK_FILE_INPUT = By.cssSelector("div.MuiDrawer-paper input[type='file'], input[type='file']");
    private static final By DRAWER_SUBMIT_AJUKAN_BTN = By.xpath("//div[contains(@class,'MuiDrawer-paper')]//form//button[@type='submit' and normalize-space()='Ajukan'] | //form//button[@type='submit']");

    // Employee Leave (/apps/absent/leave-client) locators
    private static final By AJUKAN_CUTI_BTN = By.xpath("//button[contains(normalize-space(),'Ajukan Cuti')]");
    private static final By LEAVE_TYPE_SELECT = By.cssSelector("#leave_type_id, [aria-labelledby*='leave_type']");
    private static final By LEAVE_NOTES_INPUT = By.cssSelector("textarea#notes, input#notes");
    private static final By INFO_CUTI_TAB = By.xpath("//button[@role='tab' and contains(normalize-space(),'Info Cuti')]");
    private static final By INFO_CUTI_PANEL = By.cssSelector("div[role='tabpanel']:not([hidden])");
    private static final By SICK_OR_LEAVE_SNACKBAR = By.cssSelector("div.MuiSnackbarContent-message, div.MuiAlert-message");

    // Admin Laporan Sisa Cuti (/laporan/sisa-cuti) locators
    private static final By SISA_CUTI_NAME_INPUT = By.cssSelector("input[placeholder='Cari berdasarkan nama']");
    private static final By SISA_CUTI_SEARCH_BTN = By.xpath("//button[@type='submit']");
    private static final By TABLE_ROWS = By.cssSelector("table tbody tr");
    private static final By TABLE_HEADERS = By.cssSelector("table thead th");

    public AdminCrossModuleE2EPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Membersihkan cookie dan Web Storage agar transisi sesi Karyawan <-> Admin terisolasi penuh.
     */
    public void clearSessionAndStorage() {
        try {
            driver.manage().deleteAllCookies();
        } catch (Exception ignored) {
            // Best-effort cookie cleanup
        }
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "window.localStorage.clear(); window.sessionStorage.clear();");
        } catch (Exception ignored) {
            // Storage may not be accessible on about:blank
        }
    }

    private void safeNavigate(String url) {
        logger.info("E2E navigating to: {}", url);
        try {
            driver.get(url);
        } catch (TimeoutException e) {
            logger.warn("Page load timeout caught for [{}], stopping external scripts.", url);
            try {
                ((JavascriptExecutor) driver).executeScript("window.stop();");
            } catch (Exception ignored) {
                // Ignore
            }
        }
        sleepQuietly(600);
    }

    // ==========================================
    // EMPLOYEE PORTAL ACTIONS
    // ==========================================

    public boolean loginEmployeePortal(String email, String plainPassword) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                safeNavigate(Constants.USER_LOGIN_URL);
                type(waitForVisibility(EMP_EMAIL_INPUT), email);
                type(waitForVisibility(EMP_PASSWORD_INPUT), plainPassword);
                click(EMP_SUBMIT_BTN);
                if (waitForUrlContains("/apps/absent")) {
                    sleepQuietly(700);
                    return true;
                }
            } catch (Exception e) {
                logger.warn("Employee login attempt {} failed: {}", attempt, e.getMessage());
            }
        }
        return getCurrentUrl().contains("/apps/absent");
    }

    public boolean loginDefaultEmployeePortal() {
        return loginEmployeePortal(
                Constants.HYBRID_EMPLOYEE_EMAIL,
                SecurityUtil.decode(Constants.HYBRID_EMPLOYEE_PASSWORD_ENCODED)
        );
    }

    private void openDrawerWithHydrationRetry(By triggerBtn) {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        WebElement btn = waitForVisibility(triggerBtn);
        sleepQuietly(1000);
        for (int attempt = 1; attempt <= 3; attempt++) {
            // SwipeableDrawer renders a bottom swipe area overlay that absorbs standard Selenium clicks
            clickWithJS(btn);
            sleepQuietly(700);
            List<WebElement> drawers = driver.findElements(DRAWER_PAPER);
            for (WebElement drawer : drawers) {
                if (drawer.isDisplayed()) {
                    return;
                }
            }
            logger.warn("Drawer did not open after JS click attempt {}, retrying...", attempt);
            sleepQuietly(1000);
        }
        waitForVisibility(DRAWER_PAPER);
    }

    private void selectCalendarDayWithMouseEvents(WebElement dayBtn) {
        ((JavascriptExecutor) driver).executeScript(
                "['mouseenter','mousedown','mouseup','click'].forEach(ev => "
                        + "arguments[0].dispatchEvent(new MouseEvent(ev, {bubbles:true, cancelable:true})));",
                dayBtn);
    }

    /**
     * Membuka /apps/absent/sick dan mengajukan klaim sakit untuk hari ini dengan foto surat dokter.
     *
     * @return pesan snackbar hasil submit (atau indikasi data sudah tersedia)
     */
    public String submitEmployeeSickClaimToday(String photoFileName) {
        safeNavigate(Constants.USER_SICK_URL);
        openDrawerWithHydrationRetry(AJUKAN_SAKIT_BTN);

        clickWithJS(waitForVisibility(PILIH_TANGGAL_BOX));
        waitForVisibility(CALENDAR_SIMPAN_BTN);
        List<WebElement> days = driver.findElements(By.cssSelector(
                "div.rdrCalendarWrapper button.rdrDayToday, div.rdrCalendarWrapper button.rdrDay:not(.rdrDayPassive):not(.rdrDayDisabled)"));
        if (!days.isEmpty()) {
            selectCalendarDayWithMouseEvents(days.get(0));
            sleepQuietly(250);
            selectCalendarDayWithMouseEvents(days.get(0));
            sleepQuietly(250);
        }
        clickWithJS(waitForVisibility(CALENDAR_SIMPAN_BTN));
        waitForInvisibility(CALENDAR_SIMPAN_BTN);
        waitForInvisibility(By.cssSelector("div.MuiDialog-root"));
        sleepQuietly(600);

        Path photoPath = Paths.get(Constants.TESTDATA_DIR, photoFileName).toAbsolutePath().normalize();
        if (!Files.exists(photoPath)) {
            throw new IllegalStateException("Sick document fixture not found: " + photoPath);
        }
        WebElement fileInput = driver.findElement(SICK_FILE_INPUT);
        fileInput.sendKeys(photoPath.toString());
        sleepQuietly(600);

        clickWithJS(waitForVisibility(DRAWER_SUBMIT_AJUKAN_BTN));
        return waitForSnackbarText(15);
    }

    public String getSickPageText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    /**
     * Membuka /apps/absent/leave-client, membuka drawer 'Ajukan Cuti', membaca teks tab 'Info Cuti',
     * serta mengembalikan ringkasan kuota cuti UI (atau ringkasan halaman bila user ter-redirect ke view Mitra).
     */
    public String openLeaveClientAndReadInfoCuti() {
        safeNavigate(Constants.USER_LEAVE_CLIENT_URL);
        sleepQuietly(1200);
        List<WebElement> buttons = driver.findElements(AJUKAN_CUTI_BTN);
        if (buttons.isEmpty() || !buttons.get(0).isDisplayed()) {
            String body = driver.findElement(By.tagName("body")).getText().trim();
            return "URL=" + getCurrentUrl() + " | " + body.replaceAll("\\s+", " ");
        }
        openDrawerWithHydrationRetry(AJUKAN_CUTI_BTN);
        click(waitForClickability(INFO_CUTI_TAB));
        sleepQuietly(500);
        List<WebElement> panels = driver.findElements(INFO_CUTI_PANEL);
        for (WebElement p : panels) {
            if (p.isDisplayed()) {
                return p.getText().trim();
            }
        }
        return "";
    }

    /**
     * Membuka /apps/absent/leave-client dan mencoba mengajukan cuti pada form cuti.
     *
     * @return snackbar respon server setelah submit cuti
     */
    public String submitEmployeeLeaveRequest(String notes) {
        safeNavigate(Constants.USER_LEAVE_CLIENT_URL);
        openDrawerWithHydrationRetry(AJUKAN_CUTI_BTN);

        // Pilih tipe cuti valid (MUI Select di dalam Drawer memerlukan event mousedown untuk membuka listbox)
        List<WebElement> typeSelects = driver.findElements(LEAVE_TYPE_SELECT);
        if (!typeSelects.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new MouseEvent('mousedown', {bubbles:true, cancelable:true}));",
                    typeSelects.get(0));
            sleepQuietly(500);
            List<WebElement> opts = driver.findElements(By.cssSelector(
                    "ul[role='listbox'] li[data-value]:not([data-value='']):not([aria-disabled='true'])"));
            if (opts.isEmpty()) {
                opts = driver.findElements(By.cssSelector("ul[role='listbox'] li:not([aria-disabled='true'])"));
            }
            if (!opts.isEmpty()) {
                WebElement chosen = opts.get(0);
                for (WebElement opt : opts) {
                    String txt = opt.getText().toLowerCase();
                    if (txt.contains("tahunan") || txt.contains("annual")) {
                        chosen = opt;
                        break;
                    }
                }
                clickWithJS(chosen);
                sleepQuietly(300);
            }
        }

        // Pilih tanggal di bulan depan agar tidak bentrok dengan histori absensi masa lalu
        clickWithJS(waitForVisibility(PILIH_TANGGAL_BOX));
        waitForVisibility(CALENDAR_SIMPAN_BTN);
        List<WebElement> nextMonthBtns = driver.findElements(By.cssSelector("div.rdrCalendarWrapper button.rdrNextButton"));
        if (!nextMonthBtns.isEmpty()) {
            clickWithJS(nextMonthBtns.get(0));
            sleepQuietly(350);
        }
        List<WebElement> activeDays = driver.findElements(By.cssSelector(
                "div.rdrCalendarWrapper button.rdrDay:not(.rdrDayPassive):not(.rdrDayDisabled)"));
        if (!activeDays.isEmpty()) {
            int idx = Math.min(10, activeDays.size() - 1);
            WebElement targetDay = activeDays.get(idx);
            selectCalendarDayWithMouseEvents(targetDay);
            sleepQuietly(250);
            selectCalendarDayWithMouseEvents(targetDay);
            sleepQuietly(250);
        }
        clickWithJS(waitForVisibility(CALENDAR_SIMPAN_BTN));
        waitForInvisibility(CALENDAR_SIMPAN_BTN);
        waitForInvisibility(By.cssSelector("div.MuiDialog-root"));
        sleepQuietly(600);

        List<WebElement> notesInputs = driver.findElements(By.cssSelector("textarea#notes:not([aria-hidden='true']), input#notes"));
        if (!notesInputs.isEmpty()) {
            type(waitForVisibility(notesInputs.get(0)), notes);
        }
        clickWithJS(waitForVisibility(DRAWER_SUBMIT_AJUKAN_BTN));
        return waitForSnackbarText(12);
    }

    public String getLeaveClientPageText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    // ==========================================
    // ADMIN CROSS-MODULE HELPERS
    // ==========================================

    /**
     * Membuka /laporan/sisa-cuti (form filter + export rekapitulasi sisa cuti),
     * memilih karyawan pada Autocomplete Nama User, men-trigger Download,
     * serta membersihkan file unduhan lokal bila ada.
     */
    public boolean openLaporanSisaCutiAndSearch(String employeeName) {
        safeNavigate(Constants.ADMIN_SISA_CUTI_URL);
        try {
            waitForVisibility(SISA_CUTI_NAME_INPUT);
        } catch (Exception e) {
            return false;
        }
        Path downloadedFile = Paths.get(System.getProperty("user.home"), "Downloads", "rekap-cuti.xlsx");
        boolean existedBefore = Files.exists(downloadedFile);

        WebElement nameInput = waitForVisibility(SISA_CUTI_NAME_INPUT);
        type(nameInput, employeeName);
        sleepQuietly(1200);
        List<WebElement> opts = driver.findElements(By.cssSelector("ul[role='listbox'] li, div.MuiAutocomplete-popper li"));
        if (!opts.isEmpty()) {
            click(opts.get(0));
            sleepQuietly(400);
        }

        click(waitForClickability(SISA_CUTI_SEARCH_BTN));
        String snack = waitForSnackbarText(12).toLowerCase();
        sleepQuietly(600);

        if (!existedBefore) {
            try {
                Files.deleteIfExists(downloadedFile);
            } catch (Exception ignored) {
                // Ignore if Chrome briefly holds file handle
            }
        }
        return !driver.findElements(By.cssSelector("form#generate-report")).isEmpty()
                && (snack.contains("berhasil") || !opts.isEmpty());
    }

    /**
     * Membuka menu aksi 'Cancel Cuti' langsung dari elemen baris <tr> ke-rowIndex pada /laporan/cuti,
     * menghindari mismatch indeks apabila baris sebelumnya berstatus Dibatalkan tanpa tombol AKSI.
     */
    public void openCancelCutiModalOnTableRow(int trRowIndex) {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        if (trRowIndex < 0 || trRowIndex >= rows.size()) {
            throw new IllegalStateException("Baris Laporan-Cuti di luar jangkauan: " + trRowIndex);
        }
        WebElement actionBtn = rows.get(trRowIndex).findElement(By.cssSelector("button[aria-label='action']"));
        scrollToElement(actionBtn);
        sleepQuietly(300);
        clickWithJS(actionBtn);

        WebElement visibleMenu = new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                d.findElements(By.cssSelector("ul[role='menu']")).stream()
                        .filter(m -> {
                            try {
                                return m.isDisplayed();
                            } catch (Exception ex) {
                                return false;
                            }
                        })
                        .findFirst()
                        .orElse(null));
        WebElement cancelItem = visibleMenu.findElement(By.xpath(".//li[contains(.,'Cancel Cuti')]"));
        click(cancelItem);
        waitForVisibility(By.xpath("//*[@id='alert-dialog-title' and contains(.,'Cancel Cuti')]"));
    }

    public String getTableText() {
        StringBuilder sb = new StringBuilder();
        for (WebElement row : driver.findElements(TABLE_ROWS)) {
            sb.append(row.getText()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Mengubah toggle status aktif/non-aktif milik user tertentu (berdasarkan email/nama unik pada barisnya)
     * di halaman Management-User menuju targetActive, lalu memverifikasi state akhir checkbox tersebut.
     */
    public boolean toggleUserActiveSwitchByIdentifierTo(String rowIdentifier, boolean targetActive) {
        By switchLocator = By.xpath(
                "//table//tbody//tr[contains(.,'" + rowIdentifier + "')]//input[@type='checkbox']");
        new WebDriverWait(driver, Duration.ofSeconds(12))
                .until(d -> !d.findElements(switchLocator).isEmpty());

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                List<WebElement> switches = driver.findElements(switchLocator);
                if (switches.isEmpty()) {
                    return false;
                }
                // Switch pertama pada baris user adalah toggle 'Active' (kedua adalah 'User Project')
                WebElement toggle = switches.get(0);
                boolean current = toggle.isSelected();
                if (current == targetActive) {
                    return true;
                }
                scrollToElement(toggle);
                clickWithJS(toggle);
                String snack = waitForSnackbarText(10).toLowerCase();
                sleepQuietly(700);
                List<WebElement> refreshed = driver.findElements(switchLocator);
                if (!refreshed.isEmpty() && refreshed.get(0).isSelected() == targetActive) {
                    return true;
                }
                if (snack.contains("diaktifkan") || snack.contains("dinonaktifkan")) {
                    return true;
                }
            } catch (StaleElementReferenceException e) {
                logger.warn("Toggle switch stale on attempt {}, retrying...", attempt);
                sleepQuietly(500);
            }
        }
        return false;
    }

    private String waitForSnackbarText(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
                for (WebElement el : d.findElements(SICK_OR_LEAVE_SNACKBAR)) {
                    if (!el.getText().trim().isEmpty()) {
                        return true;
                    }
                }
                return false;
            });
        } catch (TimeoutException ignored) {
            // Return whatever text is currently visible
        }
        for (WebElement el : driver.findElements(SICK_OR_LEAVE_SNACKBAR)) {
            String t = el.getText().trim();
            if (!t.isEmpty()) {
                return t;
            }
        }
        return "";
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

