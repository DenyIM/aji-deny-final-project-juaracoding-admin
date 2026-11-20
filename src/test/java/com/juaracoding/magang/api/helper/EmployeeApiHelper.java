package com.juaracoding.magang.api.helper;

import com.juaracoding.magang.api.BaseApiTest;
import com.juaracoding.magang.utils.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Helper untuk pola Hybrid API + UI di proyek Admin.
 * Menggunakan REST Assured dengan kredensial Karyawan (ter-decode dari Base64) pada tahap @Before
 * untuk membuat data pengajuan (Izin Off, Koreksi Absen, Lembur, Perjalanan Dinas)
 * sebelum Selenium memvalidasi aksi Approve / Reject / Cancel pada portal Admin.
 */
public final class EmployeeApiHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeApiHelper.class);

    public record EmployeeSession(String token, String userId) {}

    private EmployeeApiHelper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Login sebagai Karyawan default (hadirsqa1@gmail.com) via POST /authentication.
     *
     * @return EmployeeSession berisi JWT accessToken dan userId
     */
    public static EmployeeSession loginAsDefaultEmployee() {
        Response res = BaseApiTest.authenticate(
                Constants.HYBRID_EMPLOYEE_EMAIL,
                Constants.HYBRID_EMPLOYEE_PASSWORD_ENCODED
        );
        int status = res.getStatusCode();
        if (status != 200 && status != 201) {
            throw new IllegalStateException("Employee API authentication failed with HTTP " + status + ": " + res.asString());
        }
        String token = res.jsonPath().getString("accessToken");
        String userId = res.jsonPath().getString("user.id");
        logger.info("Hybrid Employee session acquired: userId={}", userId);
        return new EmployeeSession(token, userId);
    }

    /**
     * Login sebagai Admin default untuk kebutuhan query/cleanup di tahap @Before/@After.
     *
     * @return JWT accessToken Admin
     */
    public static String loginAsDefaultAdmin() {
        Response res = BaseApiTest.authenticate(Constants.ADMIN_EMAIL, Constants.ADMIN_PASSWORD_ENCODED);
        int status = res.getStatusCode();
        if (status != 200 && status != 201) {
            throw new IllegalStateException("Admin API authentication failed with HTTP " + status + ": " + res.asString());
        }
        return res.jsonPath().getString("accessToken");
    }

    private static RequestSpecification authSpec(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(BaseApiTest.buildDefaultRequestSpec())
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }

    /**
     * Membuat pengajuan Izin Off berstatus Pending sebagai Karyawan via POST /off-permit.
     *
     * @param dateOff Tanggal izin off (format yyyy-MM-dd)
     * @param reason  Alasan izin off
     * @return ID record off-permit yang baru dibuat (atau null jika ditolak server)
     */
    public static String createPendingOffPermit(String dateOff, String reason) {
        EmployeeSession session = loginAsDefaultEmployee();
        Map<String, Object> body = new HashMap<>();
        body.put("date_off", dateOff);
        body.put("reason", reason);

        Response res = given()
                .spec(authSpec(session.token()))
                .body(body)
        .when()
                .post(Constants.API_OFF_PERMIT_ENDPOINT)
        .then()
                .extract()
                .response();

        logger.info("Hybrid Seed POST /off-permit -> HTTP {}", res.getStatusCode());
        if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            return res.jsonPath().getString("id");
        }
        logger.warn("Seed off-permit did not succeed: {}", res.asString());
        return null;
    }

    /**
     * Membuat pengajuan Perjalanan Dinas berstatus Pending sebagai Karyawan via POST /business-trips.
     *
     * @param tripDateIso Tanggal perjalanan format ISO-8601 UTC
     * @param notes       Catatan dinas
     * @return ID record business-trip yang dibuat
     */
    public static String createPendingBusinessTrip(String tripDateIso, String notes) {
        EmployeeSession session = loginAsDefaultEmployee();
        Map<String, Object> body = new HashMap<>();
        body.put("trip_date", tripDateIso);
        body.put("type", "Perjalanan Dinas");
        body.put("notes", notes);
        body.put("time_zone", "+07:00");

        Response res = given()
                .spec(authSpec(session.token()))
                .body(body)
        .when()
                .post(Constants.API_BUSINESS_TRIPS_ENDPOINT)
        .then()
                .extract()
                .response();

        logger.info("Hybrid Seed POST /business-trips -> HTTP {}", res.getStatusCode());
        if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            return res.jsonPath().getString("id");
        }
        logger.warn("Seed business-trips did not succeed: {}", res.asString());
        return null;
    }

    /**
     * Membuat pengajuan Koreksi Absen berstatus Pending via POST /activity-correction.
     *
     * @param dateInIso  Waktu masuk koreksi (ISO-8601)
     * @param dateOutIso Waktu keluar koreksi (ISO-8601)
     * @param isWfh      true jika WFH, false jika WFO
     * @return ID record koreksi absen yang dibuat
     */
    public static String createPendingCorrection(String dateInIso, String dateOutIso, boolean isWfh) {
        EmployeeSession session = loginAsDefaultEmployee();
        Map<String, Object> body = new HashMap<>();
        body.put("date_in", dateInIso);
        body.put("date_out", dateOutIso);
        body.put("time_zone", "+07:00");
        body.put("is_wfh", isWfh);

        Response res = given()
                .spec(authSpec(session.token()))
                .body(body)
        .when()
                .post(Constants.API_CORRECTIONS_ENDPOINT)
        .then()
                .extract()
                .response();

        logger.info("Hybrid Seed POST /activity-correction -> HTTP {}", res.getStatusCode());
        if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            return res.jsonPath().getString("id");
        }
        logger.warn("Seed activity-correction did not succeed: {}", res.asString());
        return null;
    }

    /**
     * Membuat pengajuan Lembur berstatus Pending via POST /overtime.
     *
     * @param overtimeInIso  Jam mulai lembur (ISO-8601)
     * @param overtimeOutIso Jam selesai lembur (ISO-8601)
     * @param notes          Keterangan tugas lembur
     * @return ID record overtime yang dibuat
     */
    public static String createPendingOvertime(String overtimeInIso, String overtimeOutIso, String notes) {
        EmployeeSession session = loginAsDefaultEmployee();
        Map<String, Object> body = new HashMap<>();
        body.put("overtime_in", overtimeInIso);
        body.put("overtime_out", overtimeOutIso);
        body.put("notes", notes);
        body.put("time_zone", "+07:00");

        Response res = given()
                .spec(authSpec(session.token()))
                .body(body)
        .when()
                .post(Constants.API_OVERTIME_ENDPOINT)
        .then()
                .extract()
                .response();

        logger.info("Hybrid Seed POST /overtime -> HTTP {}", res.getStatusCode());
        if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            return res.jsonPath().getString("id");
        }
        logger.warn("Seed overtime did not succeed: {}", res.asString());
        return null;
    }

    /**
     * Mengambil ringkasan data cuti melalui endpoint /project-leave (strategi fallback
     * untuk prasyarat verifikasi UI Laporan-Cuti).
     *
     * @return Response JSON /project-leave
     */
    public static Response queryEmployeeLeaveSummary() {
        EmployeeSession session = loginAsDefaultEmployee();
        return given()
                .spec(authSpec(session.token()))
        .when()
                .get(Constants.API_PROJECT_LEAVE_ENDPOINT)
        .then()
                .extract()
                .response();
    }

    /**
     * Menghapus record uji secara aman di tahap @AfterMethod agar data tetap bersih.
     *
     * @param endpointPath path endpoint resource (misal Constants.API_OFF_PERMIT_ENDPOINT)
     * @param recordId     ID record yang akan dihapus
     */
    public static void cleanupAdminRecordQuietly(String endpointPath, String recordId) {
        if (recordId == null || recordId.isBlank()) {
            return;
        }
        try {
            String adminToken = loginAsDefaultAdmin();
            Response res = given()
                    .spec(authSpec(adminToken))
            .when()
                    .delete(endpointPath + "/" + recordId)
            .then()
                    .extract()
                    .response();
            logger.info("Cleanup DELETE {}/{} -> HTTP {}", endpointPath, recordId, res.getStatusCode());
        } catch (Exception e) {
            logger.warn("Cleanup skipped for {}/{}: {}", endpointPath, recordId, e.getMessage());
        }
    }

    /**
     * Helper format tanggal hari ini + offset hari dalam format yyyy-MM-dd.
     *
     * @param plusDays jumlah hari dari hari ini
     * @return string tanggal yyyy-MM-dd
     */
    public static String futureDateYmd(int plusDays) {
        return LocalDate.now().plusDays(plusDays).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Helper timestamp ISO UTC untuk kebutuhan payload tanggal pengajuan.
     *
     * @param plusDays  offset hari
     * @param hourUtc   jam UTC
     * @return string timestamp ISO-8601
     */
    public static String isoUtcTimestamp(int plusDays, int hourUtc) {
        Instant instant = LocalDate.now().plusDays(plusDays)
                .atTime(hourUtc, 0)
                .toInstant(ZoneOffset.UTC);
        return instant.toString();
    }
}

