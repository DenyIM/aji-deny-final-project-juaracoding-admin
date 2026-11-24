package com.juaracoding.magang.pages.admin.aji;

import com.juaracoding.magang.pages.BasePage;
import com.juaracoding.magang.utils.Constants;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page Object Model untuk Admin Dashboard (sub-menu Dashboard & Pending).
 * Mengakomodasi skenario SIT_ADM_UI_013 s/d SIT_ADM_UI_025.
 * Penanggung jawab: Tester Aji | Package: pages.admin.aji
 */
public class AdminDashboardPage extends BasePage {

    // ==========================================
    // LOCATORS: SUB-MENU DASHBOARD-DASHBOARD
    // ==========================================
    @FindBy(xpath = "//h3[normalize-space()='Total semua karyawan']")
    private WebElement totalKaryawanCardTitle;

    @FindBy(xpath = "//h3[normalize-space()='Total semua karyawan']/ancestor::div[contains(@class,'MuiCard-root')]//p")
    private WebElement totalKaryawanCountText;

    @FindBy(xpath = "//h3[normalize-space()='Total absen hari ini']")
    private WebElement totalAbsenCardTitle;

    @FindBy(xpath = "//h3[normalize-space()='Total absen hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//p")
    private WebElement totalAbsenCountText;

    @FindBy(xpath = "//h3[normalize-space()='Total absen hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//*[contains(@class,'apexcharts-canvas') or @type='donut' or contains(@class,'apexcharts-svg')]")
    private WebElement donutChartElement;

    @FindBy(xpath = "//h3[normalize-space()='Karyawan yang sedang WFH hari ini']")
    private WebElement wfhCardTitle;

    @FindBy(xpath = "//h3[normalize-space()='Karyawan yang sedang WFH hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//div[contains(@class,'custom__scroll')]")
    private WebElement wfhScrollContainer;

    @FindBy(xpath = "//h3[normalize-space()='Karyawan yang sedang cuti hari ini']")
    private WebElement cutiCardTitle;

    @FindBy(xpath = "//h3[normalize-space()='Karyawan yang sedang cuti hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//div[contains(@class,'custom__scroll')]")
    private WebElement cutiScrollContainer;

    @FindBy(xpath = "//h3[normalize-space()='Karyawan yang sedang sakit hari ini']")
    private WebElement sakitCardTitle;

    @FindBy(xpath = "//h3[normalize-space()='Karyawan yang sedang sakit hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//div[contains(@class,'custom__scroll')]")
    private WebElement sakitScrollContainer;

    @FindBy(xpath = "//h3[normalize-space()='Karyawan yang sedang Izin hari ini']")
    private WebElement izinCardTitle;

    @FindBy(xpath = "//h3[normalize-space()='Karyawan yang sedang Izin hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//div[contains(@class,'custom__scroll')]")
    private WebElement izinScrollContainer;

    // Sidebar Dashboard item
    @FindBy(xpath = "//a[@href='/dashboards/dashboard'] | //span[normalize-space()='Dashboard' or normalize-space()='Dashboards']")
    private WebElement sidebarDashboardLink;

    // ==========================================
    // LOCATORS: SUB-MENU DASHBOARD-PENDING
    // ==========================================
    @FindBy(xpath = "//h2[normalize-space()='Validator 1']")
    private WebElement validator1Header;

    @FindBy(xpath = "//h2[normalize-space()='Validator 2']")
    private WebElement validator2Header;

    @FindBy(xpath = "//h2[normalize-space()='Validator 3']")
    private WebElement validator3Header;

    @FindBy(xpath = "//input[@placeholder='Start Date']")
    private WebElement startDateInput;

    @FindBy(xpath = "//input[@placeholder='End Date']")
    private WebElement endDateInput;

    @FindBy(xpath = "//input[@placeholder='Start Date']/following-sibling::div//button | //input[@placeholder='Start Date']/..//button")
    private WebElement startDateCalendarButton;

    @FindBy(xpath = "//input[@placeholder='End Date']/following-sibling::div//button | //input[@placeholder='End Date']/..//button")
    private WebElement endDateCalendarButton;

    @FindBy(xpath = "//button[@type='submit' and contains(.,'Search')]")
    private WebElement searchButton;

    @FindBy(xpath = "//button[@type='button' and contains(.,'Reset')]")
    private WebElement resetButton;

    @FindBy(xpath = "//*[local-name()='svg' and contains(@class,'feather-filter')]/ancestor::button")
    private WebElement filterModalButton;

    @FindBy(xpath = "//div[@role='dialog' and .//h2[contains(text(),'Filter By')]]")
    private WebElement filterModalDialog;

    @FindBy(xpath = "//input[@id='job_departement']")
    private WebElement jobDepartementInput;

    @FindBy(xpath = "//div[@role='dialog']//button[@type='submit' and contains(.,'Terapkan')]")
    private WebElement terapkanButton;

    @FindBy(xpath = "//div[@role='dialog']//button[@type='button' and contains(.,'Batal')]")
    private WebElement batalButton;

    public AdminDashboardPage(WebDriver driver) {
        super(driver);
    }

    // ==========================================
    // NAVIGATIONS
    // ==========================================
    public void navigateToDashboard() {
        driver.get(Constants.ADMIN_DASHBOARD_URL);
        waitForDashboardLoaded();
        logger.info("Navigated to Admin Dashboard: {}", Constants.ADMIN_DASHBOARD_URL);
    }

    public void waitForDashboardLoaded() {
        waitForVisibility(totalKaryawanCardTitle);
    }

    public void navigateToPending() {
        driver.get(Constants.ADMIN_PENDING_URL);
        waitForPendingLoaded();
        logger.info("Navigated to Admin Pending: {}", Constants.ADMIN_PENDING_URL);
    }

    public void waitForPendingLoaded() {
        waitForVisibility(validator1Header);
    }

    public void clickSidebarDashboardMenu() {
        try {
            waitForClickability(sidebarDashboardLink);
            click(sidebarDashboardLink);
            waitForDashboardLoaded();
        } catch (Exception e) {
            logger.warn("Sidebar click failed, navigating directly: {}", e.getMessage());
            navigateToDashboard();
        }
    }

    // ==========================================
    // DASHBOARD-DASHBOARD METHODS (013-018)
    // ==========================================
    public boolean isTotalKaryawanCardDisplayed() {
        return isDisplayed(totalKaryawanCardTitle);
    }

    public String getTotalKaryawanCount() {
        return getText(totalKaryawanCountText);
    }

    public boolean isTotalAbsenCardDisplayed() {
        return isDisplayed(totalAbsenCardTitle);
    }

    public String getTotalAbsenCount() {
        return getText(totalAbsenCountText);
    }

    public boolean isDonutChartDisplayed() {
        try {
            waitForVisibility(donutChartElement);
            return donutChartElement.isDisplayed();
        } catch (Exception e) {
            return isDisplayed(By.cssSelector(".apexcharts-canvas, div[type='donut'], svg.apexcharts-svg"));
        }
    }

    public boolean isWfhCardDisplayed() {
        return isDisplayed(wfhCardTitle);
    }

    public boolean isWfhScrollListDisplayed() {
        return isDisplayed(wfhScrollContainer);
    }

    public List<WebElement> getWfhListItems() {
        return driver.findElements(By.xpath(
                "//h3[normalize-space()='Karyawan yang sedang WFH hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//div[contains(@class,'custom__scroll')]/*"));
    }

    public boolean isCutiCardDisplayed() {
        return isDisplayed(cutiCardTitle);
    }

    public boolean isCutiScrollListDisplayed() {
        return isDisplayed(cutiScrollContainer);
    }

    public List<WebElement> getCutiListItems() {
        return driver.findElements(By.xpath(
                "//h3[normalize-space()='Karyawan yang sedang cuti hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//div[contains(@class,'custom__scroll')]/*"));
    }

    public boolean isSakitCardDisplayed() {
        return isDisplayed(sakitCardTitle);
    }

    public boolean isSakitScrollListDisplayed() {
        return isDisplayed(sakitScrollContainer);
    }

    public List<WebElement> getSakitListItems() {
        return driver.findElements(By.xpath(
                "//h3[normalize-space()='Karyawan yang sedang sakit hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//div[contains(@class,'custom__scroll')]/*"));
    }

    public boolean isIzinCardDisplayed() {
        return isDisplayed(izinCardTitle);
    }

    public boolean isIzinScrollListDisplayed() {
        return isDisplayed(izinScrollContainer);
    }

    public List<WebElement> getIzinListItems() {
        return driver.findElements(By.xpath(
                "//h3[normalize-space()='Karyawan yang sedang Izin hari ini']/ancestor::div[contains(@class,'MuiCard-root')]//div[contains(@class,'custom__scroll')]/*"));
    }

    // ==========================================
    // DASHBOARD-PENDING METHODS (019-025)
    // ==========================================
    public boolean areAllThreeValidatorsDisplayed() {
        boolean v1 = isDisplayed(validator1Header);
        boolean v2 = isDisplayed(validator2Header);
        boolean v3 = isDisplayed(validator3Header);
        return v1 && v2 && v3;
    }

    public boolean verifyValidatorTables(int validatorIndex) {
        String baseXPath = "//h2[normalize-space()='Validator " + validatorIndex + "']/following-sibling::div";
        List<WebElement> tables = driver.findElements(By.xpath(baseXPath + "//table"));
        if (tables.size() < 2) {
            return false;
        }

        boolean hasCuti = !driver.findElements(By.xpath(baseXPath + "//*[normalize-space()='Cuti']")).isEmpty();
        boolean hasLembur = !driver.findElements(By.xpath(baseXPath + "//*[normalize-space()='Lembur']")).isEmpty();

        List<WebElement> headers = driver.findElements(By.xpath(baseXPath + "//th"));
        boolean hasNama = headers.stream().anyMatch(h -> h.getText().trim().equalsIgnoreCase("Nama"));
        boolean hasTotal = headers.stream().anyMatch(h -> h.getText().trim().equalsIgnoreCase("Total"));

        return hasCuti && hasLembur && hasNama && hasTotal;
    }

    public void openStartDateCalendar() {
        waitForVisibility(startDateCalendarButton);
        click(startDateCalendarButton);
        waitForVisibility(By.cssSelector(".rdrDateRangeWrapper, .rdrCalendarWrapper"));
    }

    public void selectDateRangeViaCalendar() {
        openStartDateCalendar();
        List<WebElement> activeDays = driver.findElements(By.cssSelector(".rdrDay:not(.rdrDayPassive) button.rdrDayNumber, .rdrDay:not(.rdrDayPassive)"));
        if (!activeDays.isEmpty()) {
            click(activeDays.get(0));
            if (activeDays.size() > 5) {
                click(activeDays.get(Math.min(5, activeDays.size() - 1)));
            }
        }
        clickCalendarSave();
    }

    public void clickCalendarSave() {
        WebElement saveBtn = waitForClickability(
                By.xpath("//div[contains(@class,'rdrDateRangeWrapper') or contains(@class,'MuiPaper-root')]//button[translate(normalize-space(),'SAVE','save')='save']"));
        click(saveBtn);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".rdrDateRangeWrapper")));
    }

    public void enterStartDate(String dateStr) {
        type(startDateInput, dateStr);
    }

    public void enterEndDate(String dateStr) {
        type(endDateInput, dateStr);
    }

    public void clickSearch() {
        click(searchButton);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    public void clickReset() {
        click(resetButton);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    public boolean waitForUrlQueryParam(String queryParam) {
        return waitForUrlContains(queryParam);
    }

    public String getStartDateValue() {
        return getAttribute(startDateInput, "value");
    }

    public String getEndDateValue() {
        return getAttribute(endDateInput, "value");
    }

    public void openFilterModal() {
        waitForVisibility(filterModalButton);
        click(filterModalButton);
        waitForVisibility(filterModalDialog);
    }

    public boolean isFilterModalDisplayed() {
        return isDisplayed(filterModalDialog);
    }

    public void selectFirstDepartmentInModal() {
        waitForVisibility(jobDepartementInput);
        click(jobDepartementInput);
        jobDepartementInput.sendKeys(Keys.ARROW_DOWN);
        WebElement firstOption = waitForVisibility(By.cssSelector("li.MuiAutocomplete-option"));
        click(firstOption);
    }

    public void clickTerapkanFilter() {
        click(terapkanButton);
        waitForInvisibility(By.xpath("//div[@role='dialog' and .//h2[contains(text(),'Filter By')]]"));
    }

    public void clickBatalFilter() {
        click(batalButton);
        waitForInvisibility(By.xpath("//div[@role='dialog' and .//h2[contains(text(),'Filter By')]]"));
    }

    public boolean isFilterModalClosed() {
        return waitForInvisibility(By.xpath("//div[@role='dialog' and .//h2[contains(text(),'Filter By')]]"));
    }

    public boolean areValidatorTablesEmpty() {
        List<WebElement> rows = driver.findElements(By.xpath(
                "//div[contains(@class,'MuiTableContainer-root')]//tbody//tr[not(contains(.,'No data'))]"));
        if (rows.isEmpty()) {
            return true;
        }
        boolean allEmpty = true;
        for (WebElement r : rows) {
            String text = r.getText().trim();
            if (!text.isEmpty() && !text.contains("0") && !text.contains("No data")) {
                allEmpty = false;
                break;
            }
        }
        return allEmpty;
    }

    public List<WebElement> getValidator1UplinerLinks() {
        return driver.findElements(By.xpath(
                "//h2[normalize-space()='Validator 1']/following-sibling::div//table//tbody//a[contains(@href,'/admin/v1-cuti/') or contains(@href,'/admin/v1-lembur/')]"));
    }

    public String clickFirstUplinerInValidator1() {
        List<WebElement> links = getValidator1UplinerLinks();
        if (links.isEmpty()) {
            return null;
        }
        WebElement link = links.get(0);
        String href = link.getAttribute("href");
        scrollToElement(link);
        click(link);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/admin/v1-cuti/"),
                ExpectedConditions.urlContains("/admin/v1-lembur/")
        ));
        return href;
    }
}
