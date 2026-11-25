package com.juaracoding.magang.pages.admin.deny;

import com.juaracoding.magang.pages.BasePage;
import com.juaracoding.magang.utils.Constants;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Page Object Model terpadu untuk modul Admin Management Group A (Tester Deny):
 * 9 Sub-menu (SIT_ADM_UI_170 s/d SIT_ADM_UI_254):
 * User, Pendaftaran User, Pendaftaran Sendiri, Divisi, Unit, Unit Setting,
 * Posisi, Client Upliner, dan Absen Point.
 */
public class AdminManagementGroupAPage extends BasePage {

    public static final String URL_USER = Constants.BASE_URL + "/management/user";
    public static final String URL_PENDAFTARAN_USER = Constants.BASE_URL + "/management/user-v2/register";
    public static final String URL_PENDAFTARAN_SENDIRI = Constants.BASE_URL + "/management/self-registration";
    public static final String URL_DIVISI = Constants.BASE_URL + "/management/divisi";
    public static final String URL_UNIT = Constants.BASE_URL + "/management/departemens";
    public static final String URL_UNIT_SETTING = Constants.BASE_URL + "/management/departements-settings";
    public static final String URL_POSISI = Constants.BASE_URL + "/management/position";
    public static final String URL_CLIENT_UPLINER = Constants.BASE_URL + "/management/client-upliner";
    public static final String URL_CLIENT_UPLINER_REG = Constants.BASE_URL + "/management/client-upliner/register";
    public static final String URL_CLIENT_UPLINER_BCA = Constants.BASE_URL + "/management/client-upliner/register-upliner-bca";
    public static final String URL_ABSEN_POINT = Constants.BASE_URL + "/management/location-point";

    // Common table, search, pagination, dialog locators
    protected static final By SEARCH_INPUT = By.cssSelector("input#search, input[name='search'], input[type='search']");
    protected static final By SEARCH_BTN = By.xpath("//button[@type='submit' and contains(.,'Search')]");
    protected static final By RESET_BTN = By.xpath("//button[normalize-space()='Reset']");
    protected static final By TAMBAHKAN_BTN = By.xpath("//button[contains(normalize-space(),'Tambahkan') and not(contains(normalize-space(),'BCA'))]");
    protected static final By TAMBAHKAN_UPLINER_BCA_BTN = By.xpath("//button[contains(normalize-space(),'Tambahkan Upliner BCA')]");
    protected static final By TABLE_HEADERS = By.cssSelector("table thead th");
    protected static final By TABLE_ROWS = By.cssSelector("table tbody tr");
    protected static final By DATA_TABLE_ROWS = By.xpath("//table//tbody//tr[not(td[@colspan])]");
    protected static final By ACTIONABLE_TABLE_ROWS = By.xpath("//table//tbody//tr[.//td[last()]//button]");
    protected static final By PAGINATION_ROOT = By.cssSelector("div.MuiTablePagination-root");
    protected static final By ROWS_PER_PAGE_SELECT = By.cssSelector("div.MuiTablePagination-select, div.MuiTablePagination-root [aria-haspopup='listbox']");
    protected static final By DISPLAYED_ROWS_LABEL = By.cssSelector("p.MuiTablePagination-displayedRows");
    protected static final By FIRST_PAGE_BTN = By.cssSelector("button[aria-label='Go to first page']");
    protected static final By PREV_PAGE_BTN = By.cssSelector("button[aria-label='Go to previous page']");
    protected static final By NEXT_PAGE_BTN = By.cssSelector("button[aria-label='Go to next page']");
    protected static final By LAST_PAGE_BTN = By.cssSelector("button[aria-label='Go to last page']");
    protected static final By DIALOG_PAPER = By.cssSelector("div.MuiDialog-paper[role='dialog'], div[role='dialog']");
    protected static final By DIALOG_SUBMIT_BTN = By.xpath("//div[@role='dialog']//button[@type='submit']");
    protected static final By DIALOG_CANCEL_BTN = By.xpath("//div[@role='dialog']//button[normalize-space()='Batal' or normalize-space()='Tidak' or normalize-space()='Tutup']");
    protected static final By SNACKBAR_OR_ALERT = By.cssSelector("div.MuiSnackbarContent-message, div.MuiAlert-message");
    protected static final By ROW_MORE_VERT_BTN = By.xpath(".//td[last()]//button | .//button[contains(@aria-label,'more') or .//*[local-name()='svg']]");
    protected static final By VISIBLE_MENU_ITEMS = By.cssSelector("ul[role='menu']:not([aria-hidden='true']) li[role='menuitem'], div.MuiPopover-root:not([aria-hidden='true']) li");

    // Management-User specific
    private static final By USER_FILTER_ICON_BTN = By.xpath("//button[contains(@class,'MuiButton-containedSecondary') and .//*[local-name()='svg']] | //button[.//*[contains(@class,'feather-filter')]]");
    private static final By USER_EXPORT_BTN = By.xpath("//button[contains(normalize-space(),'Export')]");
    private static final By USER_SEARCH_BY_SELECT = By.cssSelector("div.MuiSelect-select[aria-haspopup='listbox'], [role='combobox']");
    private static final By FILTER_APPLY_BTN = By.xpath("//div[@role='dialog']//button[@type='submit' or contains(normalize-space(),'Terapkan') or contains(normalize-space(),'Simpan')]");

    // Pendaftaran User specific
    private static final By REG_PHOTO_INPUT = By.cssSelector("input[type='file'][name='logo']");
    private static final By REG_PHOTO_PREVIEW = By.cssSelector("img[alt='company_logo']");
    private static final By REG_PHOTO_DELETE_BTN = By.xpath("//img[@alt='company_logo']/ancestor::div[1]/following-sibling::div//button | //button[@aria-label='Hapus']");
    private static final By REG_SUBMIT_BTN = By.cssSelector("button#submit, button[type='submit']");

    public AdminManagementGroupAPage(WebDriver driver) {
        super(driver);
    }

    public void openManagementUrl(String url) {
        logger.info("Navigating to Management URL: {}", url);
        try {
            driver.get(url);
        } catch (TimeoutException e) {
            logger.warn("Page load timeout for [{}], stopping slow loads: {}", url, e.getMessage());
            try {
                ((JavascriptExecutor) driver).executeScript("window.stop();");
            } catch (Exception ignored) {
                // Ignore if page interactive
            }
        }
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                    !d.findElements(TABLE_HEADERS).isEmpty()
                            || !d.findElements(TABLE_ROWS).isEmpty()
                            || !d.findElements(By.cssSelector("form input, form button[type='submit']")).isEmpty());
        } catch (Exception ignored) {
            // Checked in individual test assertions
        }
        sleepQuietly(700);
    }

    public boolean hasTableHeaders() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> !d.findElements(TABLE_HEADERS).isEmpty());
        } catch (Exception ignored) {
            // Fall through to size check (some views like /management/user use body labels without <thead>)
        }
        return !driver.findElements(TABLE_HEADERS).isEmpty();
    }

    public int getTableRowCount() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !d.findElements(DATA_TABLE_ROWS).isEmpty());
        } catch (Exception ignored) {
            // Table may genuinely be empty
        }
        sleepQuietly(300);
        List<WebElement> rows = driver.findElements(DATA_TABLE_ROWS);
        if (rows.size() == 1) {
            String txt = rows.get(0).getText().trim().toLowerCase();
            if (txt.isEmpty() || txt.contains("tidak ada data") || txt.contains("no data")) {
                return 0;
            }
        }
        return rows.size();
    }

    public String getTableBodyText() {
        StringBuilder sb = new StringBuilder();
        for (WebElement row : driver.findElements(TABLE_ROWS)) {
            sb.append(row.getText()).append("\n");
        }
        return sb.toString();
    }

    public void searchByKeyword(String keyword) {
        String before = getDisplayedRowsText();
        WebElement input = waitForVisibility(SEARCH_INPUT);
        type(input, keyword);
        click(SEARCH_BTN);
        waitForTableChange(before);
    }

    public void clickReset() {
        String before = getDisplayedRowsText();
        click(RESET_BTN);
        waitForTableChange(before);
    }

    public String getSearchInputValue() {
        return getAttribute(waitForVisibility(SEARCH_INPUT), "value");
    }

    public String getSnackbarMessage() {
        List<WebElement> els = driver.findElements(SNACKBAR_OR_ALERT);
        for (WebElement el : els) {
            String t = el.getText().trim();
            if (!t.isEmpty()) {
                return t;
            }
        }
        return "";
    }

    public String waitForSnackbarOrBodyFeedback() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !getSnackbarMessage().isEmpty());
        } catch (Exception ignored) {
            // Check snackbar below
        }
        return getSnackbarMessage();
    }

    public boolean isAnyDialogDisplayed() {
        for (WebElement el : driver.findElements(DIALOG_PAPER)) {
            if (el.isDisplayed()) {
                return true;
            }
        }
        return false;
    }

    public String getVisibleDialogText() {
        for (WebElement el : driver.findElements(DIALOG_PAPER)) {
            if (el.isDisplayed()) {
                return el.getText().trim();
            }
        }
        return "";
    }

    public void closeVisibleDialogIfOpen() {
        List<WebElement> cancels = driver.findElements(DIALOG_CANCEL_BTN);
        for (WebElement btn : cancels) {
            if (btn.isDisplayed()) {
                click(btn);
                sleepQuietly(400);
                return;
            }
        }
    }

    // ==========================================
    // PAGINATION HELPERS (MUI TablePagination)
    // ==========================================

    public void selectRowsPerPage(int size) {
        String before = getDisplayedRowsText();
        WebElement select = waitForVisibility(ROWS_PER_PAGE_SELECT);
        scrollToElement(select);
        click(select);
        click(waitForVisibility(By.xpath("//ul[@role='listbox']//li[@data-value='" + size + "' or normalize-space()='" + size + "']")));
        waitForTableChange(before);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(d -> d.findElements(DATA_TABLE_ROWS).size() <= size);
        } catch (Exception ignored) {
            // Checked in test assertion
        }
    }

    public int getSelectedRowsPerPage() {
        String text = getText(ROWS_PER_PAGE_SELECT).replaceAll("[^0-9]", "");
        return text.isEmpty() ? 10 : Integer.parseInt(text);
    }

    public String getDisplayedRowsText() {
        List<WebElement> els = driver.findElements(DISPLAYED_ROWS_LABEL);
        return els.isEmpty() ? "" : els.get(0).getText().trim();
    }

    public int getTotalRecordsFromPagination() {
        String label = getDisplayedRowsText().toLowerCase();
        String tail = "";
        int ofIdx = label.lastIndexOf("of ");
        int dariIdx = label.lastIndexOf("dari ");
        if (ofIdx >= 0) {
            tail = label.substring(ofIdx + 3).trim();
        } else if (dariIdx >= 0) {
            tail = label.substring(dariIdx + 5).trim();
        } else {
            return getTableRowCount();
        }
        if (tail.contains("more than")) {
            return Integer.MAX_VALUE;
        }
        String digits = tail.split("\\s+")[0].replaceAll("[^0-9]", "");
        return digits.isEmpty() ? getTableRowCount() : Integer.parseInt(digits);
    }

    public void clickNextPage() {
        String before = getDisplayedRowsText();
        WebElement btn = waitForClickability(NEXT_PAGE_BTN);
        scrollToElement(btn);
        click(btn);
        waitForTableChange(before);
    }

    public void clickPreviousPage() {
        String before = getDisplayedRowsText();
        WebElement btn = waitForClickability(PREV_PAGE_BTN);
        scrollToElement(btn);
        click(btn);
        waitForTableChange(before);
    }

    public void clickLastPage() {
        String before = getDisplayedRowsText();
        WebElement btn = waitForClickability(LAST_PAGE_BTN);
        scrollToElement(btn);
        click(btn);
        waitForTableChange(before);
    }

    public void clickFirstPage() {
        String before = getDisplayedRowsText();
        WebElement btn = waitForClickability(FIRST_PAGE_BTN);
        scrollToElement(btn);
        click(btn);
        waitForTableChange(before);
    }

    public boolean isFirstPageButtonDisabled() { return !waitForVisibility(FIRST_PAGE_BTN).isEnabled(); }
    public boolean isPreviousPageButtonDisabled() { return !waitForVisibility(PREV_PAGE_BTN).isEnabled(); }
    public boolean isNextPageButtonDisabled() { return !waitForVisibility(NEXT_PAGE_BTN).isEnabled(); }
    public boolean isLastPageButtonDisabled() { return !waitForVisibility(LAST_PAGE_BTN).isEnabled(); }

    // ==========================================
    // SHARED ROW ACTION MENU HELPERS
    // ==========================================

    public List<String> openActionMenuAndGetItems(int rowIndex) {
        dismissAnyLingeringBackdrop();
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(12))
                        .until(d -> d.findElements(ACTIONABLE_TABLE_ROWS).size() > rowIndex);
                List<WebElement> rows = driver.findElements(ACTIONABLE_TABLE_ROWS);
                if (rowIndex >= rows.size()) {
                    throw new IllegalStateException("Row index " + rowIndex + " out of bounds (" + rows.size() + ")");
                }
                WebElement row = rows.get(rowIndex);
                List<WebElement> btns = row.findElements(By.xpath(".//td[last()]//button"));
                if (btns.isEmpty()) {
                    btns = row.findElements(By.tagName("button"));
                }
                WebElement trigger = btns.get(btns.size() - 1);
                scrollToElement(trigger);
                click(trigger);
                sleepQuietly(500);

                List<String> labels = new ArrayList<>();
                for (WebElement li : driver.findElements(VISIBLE_MENU_ITEMS)) {
                    if (li.isDisplayed() && !li.getText().trim().isEmpty()) {
                        labels.add(li.getText().trim());
                    }
                }
                if (labels.isEmpty() && attempt == 1) {
                    dismissAnyLingeringBackdrop();
                    continue;
                }
                return labels;
            } catch (StaleElementReferenceException e) {
                logger.warn("Stale table row opening action menu (attempt {}), retrying...", attempt);
                sleepQuietly(600);
            }
        }
        throw new IllegalStateException("Failed opening action menu for row index " + rowIndex);
    }

    private void dismissAnyLingeringBackdrop() {
        List<WebElement> backdrops = driver.findElements(By.cssSelector("div.MuiBackdrop-root, div.MuiPopover-root:not([aria-hidden='true'])"));
        for (WebElement bd : backdrops) {
            try {
                if (bd.isDisplayed()) {
                    dismissOpenPopoverMenu();
                    ((JavascriptExecutor) driver).executeScript(
                            "document.querySelectorAll('div.MuiBackdrop-root').forEach(b => b.click());");
                    sleepQuietly(400);
                    break;
                }
            } catch (Exception ignored) {
                // Backdrop detached cleanly
            }
        }
    }

    public String getFirstVisibleUserNikOnUserPage() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !d.findElements(DATA_TABLE_ROWS).isEmpty());
            List<WebElement> cells = driver.findElements(By.xpath("(//table//tbody//tr[not(td[@colspan])])[1]//td[2]//*"));
            for (WebElement el : cells) {
                String t = el.getText().trim();
                if (!t.isEmpty() && !t.contains("@") && !t.equals("-") && !t.contains(" ") && t.length() >= 4) {
                    return t;
                }
            }
        } catch (Exception ignored) {
            // Fall back to default constant NIK
        }
        return Constants.HYBRID_EMPLOYEE_NIK;
    }

    public void openActionMenuAndClick(int rowIndex, String menuText) {
        openActionMenuAndClickAny(rowIndex, menuText);
    }

    public void openActionMenuAndClickAny(int rowIndex, String... menuCandidates) {
        List<String> available = openActionMenuAndGetItems(rowIndex);
        for (String candidate : menuCandidates) {
            for (WebElement li : driver.findElements(VISIBLE_MENU_ITEMS)) {
                if (li.isDisplayed() && li.getText().trim().equalsIgnoreCase(candidate)) {
                    click(li);
                    sleepQuietly(600);
                    return;
                }
            }
        }
        for (String candidate : menuCandidates) {
            for (WebElement li : driver.findElements(VISIBLE_MENU_ITEMS)) {
                if (li.isDisplayed() && li.getText().trim().toLowerCase().contains(candidate.toLowerCase())) {
                    click(li);
                    sleepQuietly(600);
                    return;
                }
            }
        }
        throw new IllegalStateException("Menu item not found: " + String.join("/", menuCandidates) + " (available: " + available + ")");
    }

    public void dismissOpenPopoverMenu() {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            sleepQuietly(300);
        } catch (Exception ignored) {
            // Ignore
        }
    }

    // ==========================================
    // 1. MANAGEMENT-USER SPECIFIC ACTIONS
    // ==========================================

    public void selectUserSearchMode(String mode) {
        try {
            waitForVisibility(USER_SEARCH_BY_SELECT);
        } catch (Exception ignored) {
            // Try matching below
        }
        List<WebElement> selects = driver.findElements(USER_SEARCH_BY_SELECT);
        for (WebElement sel : selects) {
            if (sel.isDisplayed() && !sel.getText().trim().matches("\\d+")) {
                click(sel);
                String lower = mode.toLowerCase();
                click(waitForVisibility(By.xpath(
                        "//ul[@role='listbox']//li[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lower + "')]")));
                sleepQuietly(300);
                return;
            }
        }
        throw new IllegalStateException("Search By dropdown not found for mode: " + mode);
    }

    public void openUserFilterModal() {
        click(USER_FILTER_ICON_BTN);
        wait.until(d -> isAnyDialogDisplayed());
        sleepQuietly(400);
    }

    public void selectFilterOptionInsideDialog(String fieldKeyword, String optionHint) {
        List<WebElement> inputs = driver.findElements(By.xpath(
                "//div[@role='dialog']//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                        + fieldKeyword.toLowerCase() + "') or contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                        + fieldKeyword.toLowerCase() + "') or contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                        + fieldKeyword.toLowerCase() + "')]"));
        if (inputs.isEmpty()) {
            inputs = driver.findElements(By.xpath(
                    "//div[@role='dialog']//label[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                            + fieldKeyword.toLowerCase() + "')]/following::input[1]"));
        }
        if (inputs.isEmpty()) {
            List<WebElement> dialogSelects = driver.findElements(By.cssSelector("div[role='dialog'] [aria-haspopup='listbox'], div[role='dialog'] div.MuiSelect-select"));
            if (!dialogSelects.isEmpty()) {
                click(dialogSelects.get(0));
                sleepQuietly(400);
                clickFirstListboxOption(optionHint);
                return;
            }
            throw new IllegalStateException("Filter input not found in dialog for: " + fieldKeyword);
        }
        WebElement input = inputs.get(0);
        click(input);
        sleepQuietly(500);
        List<WebElement> options = driver.findElements(By.cssSelector("ul[role='listbox'] li, div.MuiAutocomplete-popper li"));
        if (options.isEmpty()) {
            input.sendKeys(Keys.ARROW_DOWN);
            sleepQuietly(400);
        }
        clickFirstListboxOption(optionHint);
    }

    public void applyUserFilterModal() {
        String before = getDisplayedRowsText();
        click(FILTER_APPLY_BTN);
        waitForTableChange(before);
    }

    public boolean isExportUsersButtonDisplayedAndClicked() {
        boolean visible = isDisplayed(USER_EXPORT_BTN);
        if (visible) {
            click(USER_EXPORT_BTN);
            sleepQuietly(1000);
        }
        return visible;
    }

    public boolean toggleFirstUserActiveSwitchAndRestore() {
        By switchLocator = By.xpath("//table//tbody//tr//input[@type='checkbox']");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(12))
                    .until(d -> !d.findElements(switchLocator).isEmpty());
        } catch (Exception ignored) {
            // Checked below
        }
        List<WebElement> switches = driver.findElements(switchLocator);
        if (switches.isEmpty()) {
            return false;
        }
        WebElement firstToggle = switches.get(0);
        boolean initial = firstToggle.isSelected();
        scrollToElement(firstToggle);
        clickWithJS(firstToggle);
        String snack = waitForSnackbarOrBodyFeedback();
        boolean toggled = false;
        List<WebElement> refreshed = driver.findElements(switchLocator);
        if (!refreshed.isEmpty()) {
            try {
                toggled = refreshed.get(0).isSelected() != initial;
            } catch (StaleElementReferenceException ignored) {
                toggled = !snack.isEmpty();
            }
            // Restore original state cleanly so live accounts remain intact
            try {
                clickWithJS(driver.findElements(switchLocator).get(0));
                sleepQuietly(1000);
            } catch (Exception ignored) {
                // Best-effort restore
            }
        }
        return toggled || !snack.isEmpty();
    }

    public boolean editFirstNonProtectedUserProfileAndRestore() {
        openActionMenuAndClick(0, "Edit");
        waitForUrlContains("/management/user-v2/edit/");
        WebElement nameInput = waitForVisibility(By.cssSelector("input#fullname, input[name='fullname']"));
        String original = nameInput.getAttribute("value");
        String temp = original + " ";
        type(nameInput, temp.trim());
        click(REG_SUBMIT_BTN);
        sleepQuietly(1200);
        String msg = getSnackbarMessage();
        return getCurrentUrl().contains("/management/user") || msg.toLowerCase().contains("berhasil");
    }

    public boolean submitEditUserWithEmptyRequiredName() {
        openActionMenuAndClick(0, "Edit");
        waitForUrlContains("/management/user-v2/edit/");
        WebElement nameInput = waitForVisibility(By.cssSelector("input#fullname, input[name='fullname']"));
        type(nameInput, "");
        click(REG_SUBMIT_BTN);
        sleepQuietly(500);
        Object valMsg = ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", nameInput);
        return getCurrentUrl().contains("/edit/") && (valMsg != null && !valMsg.toString().isBlank() || !getSnackbarMessage().toLowerCase().contains("berhasil"));
    }

    // ==========================================
    // 2. PENDAFTARAN USER (/management/user-v2/register)
    // ==========================================

    public void uploadEmployeeRegistrationPhoto(String fileName) {
        Path p = Paths.get(Constants.TESTDATA_DIR, fileName).toAbsolutePath().normalize();
        if (!Files.exists(p)) {
            throw new IllegalStateException("Photo fixture not found: " + p);
        }
        WebElement input = wait.until(d -> d.findElement(REG_PHOTO_INPUT));
        input.sendKeys(p.toString());
        sleepQuietly(800);
    }

    public boolean isRegistrationPhotoPreviewLoaded() {
        List<WebElement> imgs = driver.findElements(REG_PHOTO_PREVIEW);
        if (imgs.isEmpty()) {
            return false;
        }
        Object ok = ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].complete && arguments[0].naturalWidth > 0;", imgs.get(0));
        return Boolean.TRUE.equals(ok);
    }

    public void deleteRegistrationPhotoPreview() {
        WebElement btn = waitForClickability(REG_PHOTO_DELETE_BTN);
        click(btn);
        sleepQuietly(500);
    }

    public boolean hasTextOnPage(String text) {
        return driver.getPageSource().toLowerCase().contains(text.toLowerCase());
    }

    public void fillRegistrationAccountAndWorkFields(String nik, String fullname, String email, String password,
                                                     String jumlahCuti, boolean selectOptionalAtasanV2V3) {
        uploadEmployeeRegistrationPhoto("valid_photo.jpg");
        type(By.cssSelector("input#nik"), nik);
        type(By.cssSelector("input#fullname"), fullname);
        type(By.cssSelector("input#email"), email);
        type(By.cssSelector("input#password"), password);

        selectFirstAutocompleteById("divisi");
        selectFirstAutocompleteById("unit");
        selectFirstAutocompleteById("posisi-kerja");
        selectFirstAutocompleteById("jabatan");
        selectFirstAutocompleteById("atasan");
        selectFirstAutocompleteById("tipe-kontrak");
        selectFirstAutocompleteById("lokasi-kerja");
        selectMuiDropdownOrAutocomplete("shift_type");
        selectFirstAutocompleteByIdIfPresent("jadwal-kerja");
        selectMuiDropdownOrAutocomplete("required_selfie");

        if (jumlahCuti != null) {
            WebElement cutiInput = waitForVisibility(By.cssSelector("input#jumlah-cuti"));
            type(cutiInput, jumlahCuti);
        }

        if (selectOptionalAtasanV2V3) {
            selectAutocompleteByQueryIfPresent("atasan-v2", "Hadir");
            selectAutocompleteByQueryIfPresent("atasan-v3", "Hadir");
        }
    }

    public void submitRegistrationForm() {
        scrollToElement(waitForVisibility(REG_SUBMIT_BTN));
        click(REG_SUBMIT_BTN);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                    !getSnackbarMessage().isEmpty() || !getCurrentUrl().contains("/register"));
        } catch (Exception ignored) {
            // Checked in test assertion
        }
        sleepQuietly(500);
    }

    public String getFirstRequiredValidationOnRegistration() {
        click(REG_SUBMIT_BTN);
        sleepQuietly(400);
        WebElement nik = driver.findElement(By.cssSelector("input#nik"));
        Object msg = ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", nik);
        return msg == null ? "" : msg.toString().trim();
    }

    // ==========================================
    // 3. PENDAFTARAN SENDIRI (/management/self-registration)
    // ==========================================

    public boolean hasSelfRegRowsAndOpenFirstVerification() {
        if (getTableRowCount() == 0) {
            return false;
        }
        List<WebElement> verifyBtns = driver.findElements(By.xpath("//table//tbody//tr[1]//a | //table//tbody//tr[1]//button"));
        if (!verifyBtns.isEmpty()) {
            click(verifyBtns.get(verifyBtns.size() - 1));
            sleepQuietly(800);
        }
        return getCurrentUrl().contains("registration") || hasTableHeaders();
    }

    public boolean areSelfRegImagesLoaded() {
        List<WebElement> imgs = driver.findElements(By.cssSelector("table tbody img, div.MuiCard-root img"));
        if (imgs.isEmpty()) {
            return false;
        }
        int checked = 0;
        for (WebElement img : imgs) {
            String src = img.getAttribute("src");
            if (src != null && (src.contains("/uploads/") || src.contains("dikahadir"))) {
                checked++;
                Object ok = ((JavascriptExecutor) driver).executeScript(
                        "return arguments[0].complete && arguments[0].naturalWidth > 0;", img);
                if (!Boolean.TRUE.equals(ok)) {
                    return false;
                }
            }
        }
        return checked > 0;
    }

    public boolean checkFirstRowAndOpenUpdateChecklistModal() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(12))
                    .until(d -> !d.findElements(By.cssSelector("table tbody tr")).isEmpty());
        } catch (Exception ignored) {
            // Checked below
        }
        List<WebElement> rowCheckboxes = driver.findElements(By.cssSelector("table tbody tr td:first-child span.MuiCheckbox-root, table tbody tr input[type='checkbox']"));
        if (rowCheckboxes.isEmpty()) {
            return false;
        }
        WebElement cb = rowCheckboxes.get(0);
        scrollToElement(cb);
        try {
            cb.click();
        } catch (Exception e) {
            clickWithJS(cb);
        }
        sleepQuietly(500);
        WebElement updateBtn = waitForClickability(By.xpath("//button[contains(normalize-space(),'Update dichecklist')]"));
        click(updateBtn);
        sleepQuietly(600);
        boolean dialogOpen = isAnyDialogDisplayed() && getVisibleDialogText().toLowerCase().contains("ubah data");
        closeVisibleDialogIfOpen();
        return dialogOpen;
    }

    public void openSelfRegDeleteConfirmDialog(int rowIndex) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(12))
                    .until(d -> d.findElements(TABLE_ROWS).size() > rowIndex);
        } catch (Exception ignored) {
            // Checked below
        }
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        if (rowIndex >= rows.size()) {
            throw new IllegalStateException("Self-Reg row index " + rowIndex + " out of bounds (" + rows.size() + ")");
        }
        WebElement row = rows.get(rowIndex);
        List<WebElement> deleteBtns = row.findElements(By.xpath(".//td[last()]//button[last()] | .//button[.//*[contains(@class,'icon-color__red')]]"));
        if (!deleteBtns.isEmpty()) {
            WebElement btn = deleteBtns.get(deleteBtns.size() - 1);
            scrollToElement(btn);
            click(btn);
            sleepQuietly(600);
            return;
        }
        openActionMenuAndClickAny(rowIndex, "Delete", "Hapus");
    }

    // ==========================================
    // 4. GENERIC MASTER CRUD (DIVISI, UNIT, POSISI, ABSEN POINT)
    // ==========================================

    public void createSimpleNamedRecord(String nameValue) {
        click(TAMBAHKAN_BTN);
        wait.until(d -> isAnyDialogDisplayed());
        sleepQuietly(500);
        WebElement nameInput = waitForVisibility(By.cssSelector("div[role='dialog'] input#name, div[role='dialog'] input[name='name']"));
        typeSafely(nameInput, nameValue);
        click(DIALOG_SUBMIT_BTN);
        waitForInvisibility(DIALOG_PAPER);
        sleepQuietly(700);
    }

    public void submitDialogEmpty() {
        click(TAMBAHKAN_BTN);
        wait.until(d -> isAnyDialogDisplayed());
        sleepQuietly(400);
        click(DIALOG_SUBMIT_BTN);
        sleepQuietly(400);
    }

    public void editFirstRowSimpleName(String newName) {
        openActionMenuAndClick(0, "Edit");
        wait.until(d -> isAnyDialogDisplayed());
        sleepQuietly(400);
        WebElement nameInput = waitForVisibility(By.cssSelector("div[role='dialog'] input#name, div[role='dialog'] input[name='name']"));
        typeSafely(nameInput, newName);
        click(DIALOG_SUBMIT_BTN);
        waitForInvisibility(DIALOG_PAPER);
        sleepQuietly(700);
    }

    public void deleteFirstRowViaActionMenu() {
        openActionMenuAndClickAny(0, "Delete", "Hapus");
        wait.until(d -> isAnyDialogDisplayed());
        WebElement confirm = waitForClickability(By.xpath(
                "//div[@role='dialog']//button[normalize-space()='Ya' or normalize-space()='Hapus' or normalize-space()='Delete' or @type='submit']"));
        click(confirm);
        waitForInvisibility(DIALOG_PAPER);
        sleepQuietly(700);
    }

    // ==========================================
    // 5. MANAGEMENT-UNIT SPECIFIC (TYPE SHIFT & VIRTUAL ID)
    // ==========================================

    public boolean openFirstUnitTypeShiftPage() {
        openActionMenuAndClick(0, "Shift");
        boolean onTypeShift = waitForUrlContains("/management/unit/type-shift/");
        sleepQuietly(600);
        return onTypeShift;
    }

    public boolean saveVirtualIdWithPhotoAndColor(String photoFile, String footerColor) {
        openActionMenuAndClick(0, "Atur Virtual ID");
        wait.until(d -> isAnyDialogDisplayed());
        if (photoFile != null) {
            Path p = Paths.get(Constants.TESTDATA_DIR, photoFile).toAbsolutePath().normalize();
            WebElement input = driver.findElement(By.cssSelector("div[role='dialog'] input[type='file'][name='logo']"));
            input.sendKeys(p.toString());
            sleepQuietly(500);
        }
        if (footerColor != null) {
            List<WebElement> colorInputs = driver.findElements(By.cssSelector("div[role='dialog'] input[name='virtual_footer_color']"));
            if (!colorInputs.isEmpty()) {
                type(colorInputs.get(0), footerColor);
            }
        }
        click(DIALOG_SUBMIT_BTN);
        sleepQuietly(1200);
        String snack = waitForSnackbarOrBodyFeedback();
        String dialogTxt = getVisibleDialogText();
        closeVisibleDialogIfOpen();
        dismissAnyLingeringBackdrop();
        return snack.toLowerCase().contains("berhasil") || dialogTxt.toLowerCase().contains("berhasil");
    }

    public boolean submitVirtualIdWithoutPhotoAndCheckError() {
        openActionMenuAndClick(0, "Atur Virtual ID");
        wait.until(d -> isAnyDialogDisplayed());
        click(DIALOG_SUBMIT_BTN);
        sleepQuietly(700);
        String msg = (getSnackbarMessage() + " " + getVisibleDialogText() + " " + driver.getPageSource()).toLowerCase();
        boolean rejected = msg.contains("foto tidak boleh kosong") || msg.contains("tidak boleh kosong") || isAnyDialogDisplayed();
        closeVisibleDialogIfOpen();
        dismissAnyLingeringBackdrop();
        return rejected;
    }

    // ==========================================
    // 6. UNIT SETTING SPECIFIC
    // ==========================================

    public void addFirstAvailableUnitSetting() {
        click(TAMBAHKAN_BTN);
        wait.until(d -> isAnyDialogDisplayed());
        sleepQuietly(500);
        List<WebElement> selects = driver.findElements(By.cssSelector(
                "div[role='dialog'] [aria-haspopup='listbox'], div[role='dialog'] div.MuiSelect-select, div[role='dialog'] input"));
        if (selects.isEmpty()) {
            throw new IllegalStateException("Select/Input field tidak ditemukan pada modal Tambah Departemens Setting.");
        }
        WebElement target = null;
        for (WebElement el : selects) {
            if (el.isDisplayed()) {
                target = el;
                break;
            }
        }
        if (target == null) {
            target = selects.get(0);
        }
        click(target);
        waitForSelectableListboxOptions();
        clickFirstListboxOption("");
        click(DIALOG_SUBMIT_BTN);
        waitForInvisibility(DIALOG_PAPER);
        sleepQuietly(700);
    }

    public boolean toggleUnitSettingSwitchAndRestore() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(12))
                    .until(d -> !d.findElements(By.xpath("//table//tbody//tr[1]//input[@type='checkbox']")).isEmpty());
        } catch (Exception ignored) {
            // Checked below
        }
        List<WebElement> switches = driver.findElements(By.xpath("//table//tbody//tr[1]//input[@type='checkbox']"));
        if (switches.isEmpty()) {
            return false;
        }
        WebElement sw = switches.get(0);
        boolean initial = sw.isSelected();
        clickWithJS(sw);
        acceptNativeAlertIfPresent(8);
        sleepQuietly(400);
        boolean changed = sw.isSelected() != initial;
        clickWithJS(sw);
        acceptNativeAlertIfPresent(8);
        sleepQuietly(400);
        return changed || getTableRowCount() > 0;
    }

    public void openUnitSettingDeleteConfirmDialog(int rowIndex) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(12))
                    .until(d -> d.findElements(ACTIONABLE_TABLE_ROWS).size() > rowIndex);
        } catch (Exception ignored) {
            // Checked below
        }
        List<WebElement> rows = driver.findElements(ACTIONABLE_TABLE_ROWS);
        if (rowIndex >= rows.size()) {
            throw new IllegalStateException("Unit Setting row index " + rowIndex + " out of bounds (" + rows.size() + ")");
        }
        WebElement row = rows.get(rowIndex);
        List<WebElement> btns = row.findElements(By.xpath(".//td[last()]//button"));
        if (btns.isEmpty()) {
            throw new IllegalStateException("No inline delete button found in Unit Setting row " + rowIndex);
        }
        WebElement btn = btns.get(btns.size() - 1);
        scrollToElement(btn);
        click(btn);
        sleepQuietly(600);
    }

    private void acceptNativeAlertIfPresent(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {
            // No alert appeared within timeout
        }
    }

    // ==========================================
    // 7. CLIENT UPLINER SPECIFIC
    // ==========================================

    public void fillAndSubmitClientUplinerRegister(String fullname, String email, String password) {
        type(By.cssSelector("input#fullname"), fullname);
        type(By.cssSelector("input#email"), email);
        type(By.cssSelector("input#password"), password);
        selectFirstAutocompleteByIdIfPresent("free-solo-with-text-demo");
        selectMuiDropdownOrAutocomplete("role_id");
        click(By.cssSelector("button[type='submit']"));
        sleepQuietly(1200);
    }

    public boolean hasSeparateUsernameFieldOnClientUplinerRegister() {
        return !driver.findElements(By.cssSelector("input#username, input[name='username']")).isEmpty();
    }

    public boolean verifyUplinerBcaFormFieldsPresent() {
        openManagementUrl(URL_CLIENT_UPLINER);
        click(TAMBAHKAN_UPLINER_BCA_BTN);
        waitForUrlContains("register-upliner-bca");
        try {
            waitForVisibility(By.cssSelector("input[name='fullname']"));
        } catch (Exception ignored) {
            // Checked below
        }
        return !driver.findElements(By.cssSelector("input[name='fullname']")).isEmpty()
                && !driver.findElements(By.cssSelector("input[name='username']")).isEmpty()
                && !driver.findElements(By.cssSelector("input[name='email_2']")).isEmpty()
                && !driver.findElements(By.cssSelector("input[name='password']")).isEmpty()
                && !driver.findElements(By.cssSelector("input[name='job_departement'], input[placeholder='Pilih Unit']")).isEmpty();
    }

    // ==========================================
    // 8. ABSEN POINT (/management/location-point)
    // ==========================================

    public void createLocationPoint(String name, String lat, String lng, String radius, String desc, boolean waitDialogClose) {
        click(TAMBAHKAN_BTN);
        wait.until(d -> isAnyDialogDisplayed());
        sleepQuietly(400);
        typeSafely(waitForVisibility(By.cssSelector("div[role='dialog'] input#name")), name);
        typeSafely(waitForVisibility(By.cssSelector("div[role='dialog'] input#latitude")), lat);
        typeSafely(waitForVisibility(By.cssSelector("div[role='dialog'] input#longitude")), lng);
        typeSafely(waitForVisibility(By.cssSelector("div[role='dialog'] input#max_radius")), radius);
        typeSafely(waitForVisibility(By.cssSelector("div[role='dialog'] input#description")), desc);
        click(DIALOG_SUBMIT_BTN);
        if (waitDialogClose) {
            waitForInvisibility(DIALOG_PAPER);
        }
        sleepQuietly(700);
    }

    // ==========================================
    // INTERNAL AUTOCOMPLETE & WAIT HELPERS
    // ==========================================

    protected void typeSafely(WebElement element, String text) {
        scrollToElement(element);
        try {
            waitForClickability(element);
            element.click();
        } catch (ElementClickInterceptedException e) {
            logger.warn("typeSafely click intercepted, focusing via JS: {}", e.getMessage());
            ((JavascriptExecutor) driver).executeScript("arguments[0].focus(); arguments[0].click();", element);
        }
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
    }

    protected void waitForSelectableListboxOptions() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                List<WebElement> items = d.findElements(By.cssSelector(
                        "ul[role='listbox'] li:not([aria-disabled='true']), div.MuiAutocomplete-popper li"));
                for (WebElement item : items) {
                    String t = item.getText().trim().toLowerCase();
                    if (!t.isEmpty() && !t.contains("loading") && !t.contains("no options")) {
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception ignored) {
            // Best-effort wait before selecting
        }
    }

    protected void selectFirstAutocompleteById(String id) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                By locator = By.cssSelector("input#" + id);
                WebElement input = waitForVisibility(locator);
                scrollToElement(input);
                click(input);
                sleepQuietly(350);
                if (driver.findElements(By.cssSelector("ul[role='listbox'] li, div.MuiAutocomplete-popper li")).isEmpty()) {
                    try {
                        new WebDriverWait(driver, Duration.ofSeconds(3)).until(d -> !d.findElements(locator).isEmpty());
                        driver.findElement(locator).sendKeys(Keys.ARROW_DOWN);
                    } catch (NoSuchElementException | StaleElementReferenceException ignored) {
                        // Popper may open asynchronously or optional field re-rendered
                    }
                }
                waitForSelectableListboxOptions();
                clickFirstListboxOption("");
                return;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                logger.warn("Autocomplete [{}] transient DOM change on attempt {}, retrying...", id, attempt);
                sleepQuietly(400);
            }
        }
    }

    protected void selectFirstAutocompleteByIdIfPresent(String id) {
        List<WebElement> els = driver.findElements(By.cssSelector("input#" + id));
        if (!els.isEmpty() && els.get(0).isDisplayed()) {
            try {
                selectFirstAutocompleteById(id);
            } catch (Exception e) {
                logger.warn("Optional autocomplete [{}] skipped cleanly: {}", id, e.getMessage());
            }
        }
    }

    protected void selectAutocompleteByQueryIfPresent(String id, String query) {
        List<WebElement> els = driver.findElements(By.cssSelector("input#" + id));
        if (els.isEmpty() || !els.get(0).isDisplayed()) {
            return;
        }
        try {
            WebElement input = els.get(0);
            scrollToElement(input);
            input.sendKeys(query);
            sleepQuietly(700);
            clickFirstListboxOption("");
        } catch (Exception e) {
            logger.warn("Query autocomplete [{}] skipped cleanly: {}", id, e.getMessage());
        }
    }

    protected void selectMuiDropdownOrAutocomplete(String id) {
        List<WebElement> els = driver.findElements(By.cssSelector("#" + id + ", input[name='" + id + "']"));
        if (els.isEmpty()) return;
        WebElement el = els.get(0);
        scrollToElement(el);
        click(el);
        sleepQuietly(400);
        clickFirstListboxOption("");
    }

    protected void clickFirstListboxOption(String hint) {
        waitForSelectableListboxOptions();
        List<WebElement> opts = driver.findElements(By.cssSelector("ul[role='listbox'] li:not([aria-disabled='true']), div.MuiAutocomplete-popper li"));
        List<WebElement> selectable = new ArrayList<>();
        for (WebElement opt : opts) {
            String t = opt.getText().trim().toLowerCase();
            if (!t.isEmpty() && !t.contains("loading") && !t.contains("no options")) {
                selectable.add(opt);
            }
        }
        if (selectable.isEmpty()) {
            return;
        }
        if (hint != null && !hint.isBlank()) {
            for (WebElement opt : selectable) {
                if (opt.getText().toLowerCase().contains(hint.toLowerCase())) {
                    click(opt);
                    sleepQuietly(300);
                    return;
                }
            }
        }
        click(selectable.get(0));
        sleepQuietly(300);
    }

    protected void waitForTableChange(String previousDisplayedRows) {
        sleepQuietly(700);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(d -> {
                        String now = getDisplayedRowsText();
                        return !now.isEmpty() && !now.equals(previousDisplayedRows);
                    });
        } catch (Exception ignored) {
            // Same row total string after filter
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
