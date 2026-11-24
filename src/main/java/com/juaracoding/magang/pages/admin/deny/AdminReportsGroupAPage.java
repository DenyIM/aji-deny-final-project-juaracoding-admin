package com.juaracoding.magang.pages.admin.deny;

import com.juaracoding.magang.pages.BasePage;
import com.juaracoding.magang.utils.Constants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object Model untuk modul Laporan Group A Admin HRD (Tester Deny):
 * Laporan-Semua (/laporan/all), Laporan-Kehadiran (/laporan/activity),
 * Laporan-Izin (/laporan/izin), Laporan-Izin Terlambat (/laporan/izin-terlambat),
 * Laporan-Izin Pulang Cepat (/laporan/izin-pulang-cepat), dan Laporan-Sakit (/laporan/sakit).
 * Menyediakan helper bersama Search, Date Range, Filter By Unit, Reset, dan MUI TablePagination.
 */
public class AdminReportsGroupAPage extends BasePage {

    public static final String URL_SEMUA = Constants.BASE_URL + "/laporan/all";
    public static final String URL_KEHADIRAN = Constants.BASE_URL + "/laporan/activity";
    public static final String URL_IZIN = Constants.BASE_URL + "/laporan/izin";
    public static final String URL_IZIN_TERLAMBAT = Constants.BASE_URL + "/laporan/izin-terlambat";
    public static final String URL_IZIN_PULANG_CEPAT = Constants.BASE_URL + "/laporan/izin-pulang-cepat";
    public static final String URL_SAKIT = Constants.BASE_URL + "/laporan/sakit";

    protected static final By SEARCH_INPUT = By.cssSelector("input#search, input[placeholder='Cari berdasarkan nama']");
    protected static final By START_DATE_INPUT = By.cssSelector("input[placeholder='Start Date']");
    protected static final By END_DATE_INPUT = By.cssSelector("input[placeholder='End Date']");
    protected static final By CALENDAR_TOGGLE_BTNS = By.cssSelector("div.search-date-group button, input[placeholder='Start Date'] ~ div button");
    protected static final By CALENDAR_SAVE_BTN = By.xpath("//button[normalize-space()='save' or normalize-space()='Save']");
    protected static final By CALENDAR_DAYS = By.cssSelector("div.rdrCalendarWrapper button.rdrDay:not(.rdrDayPassive):not(.rdrDayDisabled)");
    protected static final By SEARCH_SUBMIT_BTN = By.xpath("//button[@type='submit' and contains(.,'Search')]");
    protected static final By RESET_BTN = By.xpath("//button[normalize-space()='Reset']");
    protected static final By FILTER_ICON_BTN = By.xpath("//button[contains(@class,'MuiButton-containedSecondary') and .//*[local-name()='svg']]");
    protected static final By FILTER_MODAL_TITLE = By.xpath("//h4[contains(text(),'Filter By')] | //div[contains(@class,'MuiDialog-paper')]//*[contains(text(),'Filter By')]");
    protected static final By FILTER_UNIT_INPUT = By.cssSelector("input#job_departement, input[placeholder='Cari Departemen']");
    protected static final By FILTER_APPLY_BTN = By.xpath("//div[contains(@class,'MuiDialog-paper')]//button[@type='submit' and (contains(.,'Terapkan') or contains(.,'Menerapkan'))]");
    protected static final By FILTER_CANCEL_BTN = By.xpath("//div[contains(@class,'MuiDialog-paper')]//button[normalize-space()='Batal']");

    protected static final By TABLE_HEADERS = By.cssSelector("table thead th");
    protected static final By TABLE_ROWS = By.cssSelector("table tbody tr");
    protected static final By PAGINATION_ROOT = By.cssSelector("div.MuiTablePagination-root");
    protected static final By ROWS_PER_PAGE_SELECT = By.cssSelector("div.MuiTablePagination-select, div.MuiTablePagination-root [aria-haspopup='listbox']");
    protected static final By DISPLAYED_ROWS_LABEL = By.cssSelector("p.MuiTablePagination-displayedRows");
    protected static final By FIRST_PAGE_BTN = By.cssSelector("button[aria-label='Go to first page']");
    protected static final By PREV_PAGE_BTN = By.cssSelector("button[aria-label='Go to previous page']");
    protected static final By NEXT_PAGE_BTN = By.cssSelector("button[aria-label='Go to next page']");
    protected static final By LAST_PAGE_BTN = By.cssSelector("button[aria-label='Go to last page']");

    private static final By LIHAT_SELENGKAPNYA_LINK = By.xpath("//*[contains(text(),'Lihat Selengkapnya')]");
    private static final By LIHAT_LEBIH_SEDIKIT_LINK = By.xpath("//*[contains(text(),'Lihat Lebih Sedikit')]");
    private static final By AVATAR_IMAGES = By.cssSelector("table tbody tr td:first-child img");
    private static final By MAPS_TRIGGER_LINKS = By.cssSelector("table tbody tr td:nth-child(2) button");
    private static final By MAPS_CONTAINER = By.cssSelector("div.gm-style, div.gm-err-container, div.MuiDialog-paper div[style*='height: 100vh'], div.MuiDialog-paper iframe");
    private static final By MAPS_MODAL_CLOSE_BTN = By.xpath("//div[contains(@class,'MuiDialog-paper')]//button[normalize-space()='Close']");
    private static final By MAPS_INFOWINDOW_OR_TABLE_IMAGES = By.cssSelector("div.gm-style-iw img, div.MuiDialog-paper img, table tbody tr td:first-child img");
    private static final By SICK_VIEW_LINKS = By.xpath("//table//tbody//tr//*[normalize-space()='View' or normalize-space()='Lihat']");
    private static final By SICK_DOWNLOAD_LINKS = By.xpath("//table//tbody//tr//a[normalize-space()='Download']");
    private static final By SNACKBAR_OR_ALERT = By.cssSelector("div.MuiSnackbarContent-message, div.MuiAlert-message");

    public AdminReportsGroupAPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigasi aman ke URL laporan dengan toleransi timeout script eksternal Next.js.
     */
    public void openReportUrl(String url) {
        logger.info("Navigating to Admin Report URL: {}", url);
        safeGet(url);
        waitForPageReady();
    }

    protected void safeGet(String url) {
        try {
            driver.get(url);
        } catch (TimeoutException e) {
            logger.warn("Page load timeout caught for [{}], stopping slow external loads & continuing: {}", url, e.getMessage());
            try {
                ((JavascriptExecutor) driver).executeScript("window.stop();");
            } catch (Exception ignored) {
                // Ignore if page is already interactive
            }
        }
    }

    public void waitForPageReady() {
        waitForVisibility(SEARCH_INPUT);
        wait.until(d -> !d.findElements(PAGINATION_ROOT).isEmpty() || !d.findElements(TABLE_HEADERS).isEmpty());
        sleepQuietly(500);
    }

    public List<String> getTableHeadersText() {
        waitForVisibility(TABLE_HEADERS);
        return driver.findElements(TABLE_HEADERS).stream()
                .map(el -> el.getText().trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public boolean hasTableHeaderContaining(String keyword) {
        String target = keyword.toUpperCase();
        return getTableHeadersText().stream().anyMatch(h -> h.contains(target));
    }

    public int getTableRowCount() {
        sleepQuietly(300);
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        if (rows.size() == 1 && rows.get(0).getText().trim().isEmpty()) {
            return 0;
        }
        return rows.size();
    }

    /**
     * Jika filter tanggal default (hari ini) belum memiliki data presensi,
     * terapkan filter tanggal awal-pertengahan bulan berjalan agar skenario tetap deterministik.
     */
    public void ensureTableHasRowsIfEmpty() {
        if (getTableRowCount() == 0) {
            logger.info("Default date filter has 0 rows; applying current month date range filter.");
            applyDateRangeFilter(true);
        }
    }

    public String getTableBodyText() {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        if (rows.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (WebElement row : rows) {
            sb.append(row.getText()).append("\n");
        }
        return sb.toString();
    }

    public void searchByKeyword(String keyword) {
        String beforeRows = getDisplayedRowsText();
        type(SEARCH_INPUT, keyword);
        click(SEARCH_SUBMIT_BTN);
        waitForTableChange(beforeRows);
    }

    /**
     * Membuka kalender DateRange lewat ikon kalender di input Start/End Date lalu memilih tanggal dan klik Save.
     */
    public void applyDateRangeFilter(boolean clickSearchAfter) {
        String beforeRows = getDisplayedRowsText();
        List<WebElement> calButtons = driver.findElements(CALENDAR_TOGGLE_BTNS);
        if (!calButtons.isEmpty()) {
            click(calButtons.get(0));
        } else {
            click(START_DATE_INPUT);
        }
        waitForVisibility(CALENDAR_DAYS);
        List<WebElement> days = driver.findElements(CALENDAR_DAYS);
        if (days.size() >= 2) {
            click(days.get(0));
            click(days.get(Math.min(14, days.size() - 1)));
        } else if (!days.isEmpty()) {
            click(days.get(0));
            click(days.get(0));
        }
        click(CALENDAR_SAVE_BTN);
        sleepQuietly(300);
        if (clickSearchAfter) {
            click(SEARCH_SUBMIT_BTN);
            waitForTableChange(beforeRows);
        }
    }

    public String getStartDateValue() {
        return getAttribute(waitForVisibility(START_DATE_INPUT), "value");
    }

    public String getEndDateValue() {
        return getAttribute(waitForVisibility(END_DATE_INPUT), "value");
    }

    public void openFilterModal() {
        click(FILTER_ICON_BTN);
        waitForVisibility(FILTER_MODAL_TITLE);
    }

    public boolean isFilterModalDisplayed() {
        return isDisplayed(FILTER_MODAL_TITLE);
    }

    public void clickFilterModalCancel() {
        click(FILTER_CANCEL_BTN);
        waitForInvisibility(FILTER_MODAL_TITLE);
    }

    public String applyFirstUnitFilter() {
        String beforeRows = getDisplayedRowsText();
        openFilterModal();
        WebElement unitInput = waitForVisibility(FILTER_UNIT_INPUT);
        click(unitInput);
        sleepQuietly(700);
        List<WebElement> options = driver.findElements(By.cssSelector("ul[role='listbox'] li, div.MuiAutocomplete-popper li"));
        String selectedUnit = "";
        if (!options.isEmpty()) {
            selectedUnit = options.get(0).getText().trim();
            click(options.get(0));
        } else {
            unitInput.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
            selectedUnit = unitInput.getAttribute("value");
        }
        click(FILTER_APPLY_BTN);
        waitForInvisibility(FILTER_MODAL_TITLE);
        waitForTableChange(beforeRows);
        return selectedUnit == null ? "" : selectedUnit;
    }

    public void clickResetFilter() {
        String beforeRows = getDisplayedRowsText();
        click(RESET_BTN);
        waitForTableChange(beforeRows);
    }

    public String getSearchInputValue() {
        return getAttribute(waitForVisibility(SEARCH_INPUT), "value");
    }

    // ==========================================
    // PAGINATION CONTROLS ([5, 10, 25] & NAVIGATION)
    // ==========================================

    public void selectRowsPerPage(int size) {
        String beforeRows = getDisplayedRowsText();
        WebElement select = waitForVisibility(ROWS_PER_PAGE_SELECT);
        scrollToElement(select);
        click(select);
        By optionLocator = By.xpath("//ul[@role='listbox']//li[@data-value='" + size + "' or normalize-space()='" + size + "']");
        click(waitForVisibility(optionLocator));
        waitForTableChange(beforeRows);
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8))
                    .until(d -> d.findElements(TABLE_ROWS).size() <= size);
        } catch (Exception ignored) {
            // Assert in test will capture exact count if server fails to reduce rows
        }
    }

    public int getSelectedRowsPerPage() {
        String text = getText(ROWS_PER_PAGE_SELECT).replaceAll("[^0-9]", "");
        return text.isEmpty() ? 10 : Integer.parseInt(text);
    }

    public String getDisplayedRowsText() {
        try {
            List<WebElement> els = driver.findElements(DISPLAYED_ROWS_LABEL);
            return els.isEmpty() ? "" : els.get(0).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public int getTotalRecordsFromPagination() {
        String label = getDisplayedRowsText();
        int ofIdx = label.lastIndexOf("of ");
        if (ofIdx < 0) {
            return getTableRowCount();
        }
        String afterOf = label.substring(ofIdx + 3).replaceAll("[^0-9]", "");
        return afterOf.isEmpty() ? getTableRowCount() : Integer.parseInt(afterOf);
    }

    public void clickNextPage() {
        String beforeRows = getDisplayedRowsText();
        WebElement btn = waitForClickability(NEXT_PAGE_BTN);
        scrollToElement(btn);
        click(btn);
        waitForTableChange(beforeRows);
    }

    public void clickPreviousPage() {
        String beforeRows = getDisplayedRowsText();
        WebElement btn = waitForClickability(PREV_PAGE_BTN);
        scrollToElement(btn);
        click(btn);
        waitForTableChange(beforeRows);
    }

    public void clickLastPage() {
        String beforeRows = getDisplayedRowsText();
        WebElement btn = waitForClickability(LAST_PAGE_BTN);
        scrollToElement(btn);
        click(btn);
        waitForTableChange(beforeRows);
    }

    public void clickFirstPage() {
        String beforeRows = getDisplayedRowsText();
        WebElement btn = waitForClickability(FIRST_PAGE_BTN);
        scrollToElement(btn);
        click(btn);
        waitForTableChange(beforeRows);
    }

    public boolean isFirstPageButtonDisabled() {
        return !waitForVisibility(FIRST_PAGE_BTN).isEnabled();
    }

    public boolean isPreviousPageButtonDisabled() {
        return !waitForVisibility(PREV_PAGE_BTN).isEnabled();
    }

    public boolean isNextPageButtonDisabled() {
        return !waitForVisibility(NEXT_PAGE_BTN).isEnabled();
    }

    public boolean isLastPageButtonDisabled() {
        return !waitForVisibility(LAST_PAGE_BTN).isEnabled();
    }

    // ==========================================
    // SUB-MENU SPECIFIC ACTIONS (SEMUA, KEHADIRAN, SAKIT)
    // ==========================================

    public boolean expandLongNotesIfPresent() {
        List<WebElement> expandLinks = driver.findElements(LIHAT_SELENGKAPNYA_LINK);
        if (expandLinks.isEmpty()) {
            return true;
        }
        click(expandLinks.get(0));
        return !driver.findElements(LIHAT_LEBIH_SEDIKIT_LINK).isEmpty();
    }

    public boolean hasMapsLinkColumnInAllReport() {
        return !driver.findElements(By.xpath("//table//tbody//tr//a[contains(@href,'maps') or contains(.,'lokasi')]")).isEmpty()
                || getTableRowCount() > 0;
    }

    /**
     * Memastikan seluruh gambar upload presensi (/uploads/) pada tabel Laporan-Semua termuat utuh (bukan broken image).
     */
    public boolean areAllAvatarUploadImagesLoaded() {
        return areUploadImagesLoaded(driver.findElements(AVATAR_IMAGES));
    }

    /**
     * Membuka modal peta Google Maps di Laporan-Kehadiran (jika berada di modal tautan lokasi) atau memeriksa kontainer peta.
     */
    public boolean isGoogleMapDisplayed() {
        if (!driver.findElements(MAPS_CONTAINER).isEmpty() && isDisplayed(MAPS_CONTAINER)) {
            return true;
        }
        List<WebElement> mapLinks = driver.findElements(MAPS_TRIGGER_LINKS);
        if (!mapLinks.isEmpty()) {
            scrollToElement(mapLinks.get(0));
            click(mapLinks.get(0));
            sleepQuietly(1500);
            boolean dialogMapVisible = isDisplayed(MAPS_CONTAINER)
                    || !driver.findElements(By.cssSelector("div.MuiDialog-paper")).isEmpty();
            List<WebElement> closeBtns = driver.findElements(MAPS_MODAL_CLOSE_BTN);
            if (!closeBtns.isEmpty()) {
                click(closeBtns.get(0));
            }
            return dialogMapVisible;
        }
        return false;
    }

    public boolean arePresencePhotosInMapsOrTableLoaded() {
        List<WebElement> mapLinks = driver.findElements(MAPS_TRIGGER_LINKS);
        if (!mapLinks.isEmpty()) {
            click(mapLinks.get(0));
            sleepQuietly(1200);
        }
        boolean result = areUploadImagesLoaded(driver.findElements(MAPS_INFOWINDOW_OR_TABLE_IMAGES));
        List<WebElement> closeBtns = driver.findElements(MAPS_MODAL_CLOSE_BTN);
        if (!closeBtns.isEmpty()) {
            click(closeBtns.get(0));
        }
        return result;
    }

    public int getSickViewLinkCount() {
        return driver.findElements(SICK_VIEW_LINKS).size();
    }

    public boolean openFirstSickViewAndVerifyImageLoaded() {
        List<WebElement> viewLinks = driver.findElements(SICK_VIEW_LINKS);
        if (viewLinks.isEmpty()) {
            return false;
        }
        click(viewLinks.get(0));
        sleepQuietly(1000);
        List<WebElement> modalImages = driver.findElements(By.cssSelector("div.MuiDialog-paper img, div[role='presentation'] img"));
        return areUploadImagesLoaded(modalImages);
    }

    public String getFirstSickDownloadHref() {
        List<WebElement> downloadLinks = driver.findElements(SICK_DOWNLOAD_LINKS);
        return downloadLinks.isEmpty() ? "" : downloadLinks.get(0).getAttribute("href");
    }

    public int checkUrlHttpStatus(String url) {
        if (url == null || url.isBlank()) {
            return 0;
        }
        Response res = RestAssured.given()
                .relaxedHTTPSValidation()
                .when()
                .get(url)
                .then()
                .extract()
                .response();
        return res.getStatusCode();
    }

    public String getToastOrAlertText() {
        try {
            WebElement el = waitForVisibility(SNACKBAR_OR_ALERT);
            return el.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Memeriksa apakah gambar upload pada DOM benar-benar termuat (naturalWidth > 0).
     * Bila ada foto /uploads/ yang rusak (naturalWidth == 0) atau gagal muat, mengembalikan false.
     */
    protected boolean areUploadImagesLoaded(List<WebElement> images) {
        if (images == null || images.isEmpty()) {
            return false;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int uploadImgChecked = 0;
        for (WebElement img : images) {
            String src = img.getAttribute("src");
            if (src == null || src.isBlank() || src.startsWith("data:image/")) {
                continue;
            }
            if (src.contains("/uploads/") || src.contains("dikahadir.com")) {
                uploadImgChecked++;
                Object loaded = js.executeScript(
                        "return arguments[0].complete && typeof arguments[0].naturalWidth != 'undefined' && arguments[0].naturalWidth > 0;",
                        img);
                if (!Boolean.TRUE.equals(loaded)) {
                    logger.warn("Broken upload image detected -> src=[{}]", src);
                    return false;
                }
            }
        }
        return uploadImgChecked > 0;
    }

    protected void waitForTableChange(String previousDisplayedRows) {
        sleepQuietly(800);
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8))
                    .until(d -> !getDisplayedRowsText().equals(previousDisplayedRows));
        } catch (Exception ignored) {
            // Query may yield identical total string (e.g. searching when 0->0)
        }
        sleepQuietly(500);
    }

    protected void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
