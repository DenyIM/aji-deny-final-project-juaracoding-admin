package com.juaracoding.magang.pages.admin.deny;

import com.juaracoding.magang.utils.Constants;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object Model khusus sub-menu Laporan-Cuti (/laporan/cuti) untuk Tester Deny.
 * Mewarisi helper bersama Search, DateRange, Filter Unit, Reset, dan TablePagination
 * dari AdminReportsGroupAPage serta menambahkan aksi Cancel Cuti & Edit Atasan.
 */
public class AdminLeavePage extends AdminReportsGroupAPage {

    public static final String URL_CUTI = Constants.BASE_URL + "/laporan/cuti";

    private static final By ROW_ACTION_BUTTONS = By.cssSelector("table tbody tr button[aria-label='action']");
    private static final By ALL_MOUNTED_MENUS = By.cssSelector("ul[role='menu']");
    private static final By CANCEL_DIALOG_TITLE = By.xpath("//*[@id='alert-dialog-title' and contains(.,'Cancel Cuti')]");
    private static final By REJECT_REASON_INPUT = By.cssSelector("input#rejectReason");
    private static final By CANCEL_CONFIRM_YES_BTN = By.xpath("//div[contains(@class,'MuiDialog-paper')]//button[@type='submit' and normalize-space()='Iya']");
    private static final By CANCEL_ABORT_NO_BTN = By.xpath("//div[contains(@class,'MuiDialog-paper')]//button[normalize-space()='Tidak']");
    private static final By EDIT_MODAL_TITLE = By.xpath("//div[contains(@class,'MuiDialog-paper')]//*[contains(text(),'Ubah atasan user yang request')]");
    private static final By EDIT_MODAL_INPUTS = By.cssSelector("div.MuiDialog-paper input, div.MuiDialog-paper select, div.MuiDialog-paper [role='combobox']");
    private static final By EDIT_SUBMIT_BTN = By.xpath("//div[contains(@class,'MuiDialog-paper')]//button[@type='submit' and (contains(.,'Ubah Data') or contains(.,'Mengubah'))]");

    public AdminLeavePage(WebDriver driver) {
        super(driver);
    }

    public void openLeaveReport() {
        openReportUrl(URL_CUTI);
    }

    /**
     * Membuka menu aksi titik tiga pada baris cuti dan mengembalikan WebElement menu yang sedang terbuka.
     * Menangani MUI Menu keepMounted=true yang merender ul[role='menu'] tersembunyi di setiap baris.
     */
    public WebElement openActionMenuAtRow(int rowIndex) {
        List<WebElement> buttons = driver.findElements(ROW_ACTION_BUTTONS);
        if (buttons.isEmpty() || rowIndex >= buttons.size()) {
            throw new IllegalStateException("Tidak ditemukan tombol AKSI pada baris ke-" + rowIndex + " di Laporan-Cuti.");
        }
        WebElement btn = buttons.get(rowIndex);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior:'instant', block:'center', inline:'center'});", btn);
        sleepQuietly(300);
        try {
            btn.click();
        } catch (Exception e) {
            clickWithJS(btn);
        }

        return wait.until(d -> d.findElements(ALL_MOUNTED_MENUS).stream()
                .filter(m -> {
                    try {
                        return m.isDisplayed();
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .findFirst()
                .orElse(null));
    }

    /**
     * Mencari baris pengajuan cuti yang masih aktif (belum dibatalkan) agar aksi Cancel Cuti valid.
     */
    public int findFirstCancellableRowIndex() {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        for (int i = 0; i < rows.size(); i++) {
            String rowText = rows.get(i).getText().toUpperCase();
            boolean hasAction = !rows.get(i).findElements(By.cssSelector("button[aria-label='action']")).isEmpty();
            if (hasAction && !rowText.contains("CANCEL") && !rowText.contains("DIBATALKAN") && !rowText.contains("REJECTED")) {
                return i;
            }
        }
        return -1;
    }

    public void openCancelCutiModalForRow(int rowIndex) {
        WebElement visibleMenu = openActionMenuAtRow(rowIndex);
        WebElement cancelItem = visibleMenu.findElement(By.xpath(".//li[contains(.,'Cancel Cuti')]"));
        click(cancelItem);
        waitForVisibility(CANCEL_DIALOG_TITLE);
    }

    public void submitCancelCutiWithReason(String reason) {
        type(REJECT_REASON_INPUT, reason);
        click(CANCEL_CONFIRM_YES_BTN);
        sleepQuietly(1000);
    }

    public String submitCancelCutiWithEmptyReasonAndGetValidation() {
        type(REJECT_REASON_INPUT, "");
        click(CANCEL_CONFIRM_YES_BTN);
        return getAttribute(waitForVisibility(REJECT_REASON_INPUT), "validationMessage");
    }

    public void abortCancelCutiWithTidak() {
        click(CANCEL_ABORT_NO_BTN);
        waitForInvisibility(CANCEL_DIALOG_TITLE);
    }

    public boolean isCancelCutiModalDisplayed() {
        return isDisplayed(CANCEL_DIALOG_TITLE);
    }

    public void openEditModalForFirstRow() {
        WebElement visibleMenu = openActionMenuAtRow(0);
        WebElement editItem = visibleMenu.findElement(By.xpath(".//li[contains(.,'Edit')]"));
        click(editItem);
        waitForVisibility(EDIT_MODAL_TITLE);
    }

    public boolean isEditModalDisplayed() {
        return isDisplayed(EDIT_MODAL_TITLE);
    }

    public boolean hasEditableInputInEditModal() {
        List<WebElement> inputs = driver.findElements(EDIT_MODAL_INPUTS);
        for (WebElement input : inputs) {
            if (input.isDisplayed() && input.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void clickUbahDataInEditModal() {
        click(EDIT_SUBMIT_BTN);
        sleepQuietly(800);
    }
}
