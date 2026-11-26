package com.juaracoding.magang.api.helper;

import com.juaracoding.magang.api.BaseApiTest;
import com.juaracoding.magang.utils.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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
     * Mengunggah file gambar uji ke POST /upload (multipart form field 'uri').
     *
     * @param fileName nama file di src/test/resources/testdata/
     * @return ID file terunggah atau null jika gagal
     */
    public static String uploadTestPhoto(String fileName) {
        Path filePath = Paths.get(Constants.TESTDATA_DIR, fileName).toAbsolutePath().normalize();
        if (!Files.exists(filePath)) {
            throw new IllegalStateException("Testdata file not found: " + filePath);
        }
        String mimeType = fileName.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        Response res = given()
                .baseUri(Constants.API_BASE_URI)
                .relaxedHTTPSValidation()
                .multiPart("uri", filePath.toFile(), mimeType)
        .when()
                .post(Constants.API_UPLOAD_ENDPOINT)
        .then()
                .extract()
                .response();

        logger.info("Hybrid POST /upload [{}] -> HTTP {}", fileName, res.getStatusCode());
        if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            return res.jsonPath().getString("id");
        }
        logger.warn("Upload failed: {}", res.asString());
        return null;
    }

    /**
     * Mendaftarkan akun karyawan mandiri via POST /users (is_mobile=true, company_code=DIKA)
     * meniru alur /absen/register.
     *
     * @return userId yang baru dibuat atau null jika gagal
     */
    public static String createSelfRegisteredMobileUser(String nik, String fullname, String email, String plainPassword) {
        String photoId = uploadTestPhoto("valid_photo.jpg");
        Map<String, Object> body = new HashMap<>();
        body.put("nik", nik);
        body.put("fullname", fullname);
        body.put("email", email);
        body.put("password", plainPassword);
        body.put("company_code", "DIKA");
        body.put("is_mobile", true);
        if (photoId != null && !photoId.isBlank()) {
            body.put("photo", photoId);
        }

        Response res = given()
                .spec(BaseApiTest.buildDefaultRequestSpec())
                .body(body)
        .when()
                .post(Constants.API_USERS_ENDPOINT)
        .then()
                .extract()
                .response();

        logger.info("Hybrid Seed self-register POST /users [{}] -> HTTP {}", email, res.getStatusCode());
        if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            String id = res.jsonPath().getString("id");
            return (id != null && !id.isBlank()) ? id : findUserIdByEmail(email);
        }
        logger.warn("Seed self-register did not succeed: {}", res.asString());
        return null;
    }

    /**
     * Membuat pengajuan Izin Terlambat berstatus Pending sebagai Karyawan via POST /late-permit.
     *
     * @param lateTimeInIso waktu keterlambatan (ISO-8601)
     * @param reason        alasan keterlambatan
     * @return ID record late-permit yang dibuat
     */
    public static String createPendingLatePermit(String lateTimeInIso, String reason) {
        EmployeeSession session = loginAsDefaultEmployee();
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", session.userId());
        body.put("late_time_in", lateTimeInIso);
        body.put("late_reason", reason);
        body.put("time_zone", "+07:00");

        Response res = given()
                .spec(authSpec(session.token()))
                .body(body)
        .when()
                .post(Constants.API_LATE_PERMIT_ENDPOINT)
        .then()
                .extract()
                .response();

        logger.info("Hybrid Seed POST /late-permit -> HTTP {}", res.getStatusCode());
        if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            return res.jsonPath().getString("id");
        }
        logger.warn("Seed late-permit did not succeed: {}", res.asString());
        return null;
    }

    /**
     * Membuat pengajuan Izin Pulang Cepat berstatus Pending sebagai Karyawan via POST /early-leave.
     *
     * @param earlyTimeOutIso waktu pulang cepat (ISO-8601)
     * @param reason          alasan pulang cepat
     * @return ID record early-leave yang dibuat
     */
    public static String createPendingEarlyLeave(String earlyTimeOutIso, String reason) {
        EmployeeSession session = loginAsDefaultEmployee();
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", session.userId());
        body.put("early_time_out", earlyTimeOutIso);
        body.put("early_leave_reason", reason);
        body.put("time_zone", "+07:00");

        Response res = given()
                .spec(authSpec(session.token()))
                .body(body)
        .when()
                .post(Constants.API_EARLY_LEAVE_ENDPOINT)
        .then()
                .extract()
                .response();

        logger.info("Hybrid Seed POST /early-leave -> HTTP {}", res.getStatusCode());
        if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            return res.jsonPath().getString("id");
        }
        logger.warn("Seed early-leave did not succeed: {}", res.asString());
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
     * Mengambil ringkasan kuota cuti user tertentu melalui GET /project-leave/{userId}.
     */
    public static Response getProjectLeaveSummaryByUserId(String bearerToken, String userId) {
        String effectiveToken = (bearerToken != null && !bearerToken.isBlank()) ? bearerToken : loginAsDefaultAdmin();
        return given()
                .spec(authSpec(effectiveToken))
        .when()
                .get(Constants.API_PROJECT_LEAVE_ENDPOINT + "/" + userId)
        .then()
                .extract()
                .response();
    }

    /**
     * Mencari userId berdasarkan email melalui Admin API GET /users?email=...
     */
    public static String findUserIdByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String adminToken = loginAsDefaultAdmin();
        Response res = given()
                .spec(authSpec(adminToken))
                .queryParam("email", email)
        .when()
                .get(Constants.API_USERS_ENDPOINT)
        .then()
                .extract()
                .response();
        List<String> ids = res.jsonPath().getList("data.id");
        return (ids != null && !ids.isEmpty()) ? ids.get(0) : null;
    }

    /**
     * Mengubah status aktif/non-aktif user melalui Admin API PATCH /users/{userId}.
     */
    public static boolean setUserActiveStatus(String userId, boolean isActive) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        String adminToken = loginAsDefaultAdmin();
        Map<String, Object> body = new HashMap<>();
        body.put("is_active", isActive);
        body.put("isVerified", true);
        Response res = given()
                .spec(authSpec(adminToken))
                .body(body)
        .when()
                .patch(Constants.API_USERS_ENDPOINT + "/" + userId)
        .then()
                .extract()
                .response();
        logger.info("PATCH /users/{} (is_active={}) -> HTTP {}", userId, isActive, res.getStatusCode());
        return res.getStatusCode() == 200;
    }

    private static final String[] EXTENDED_VALID_NIK_POOL = {
            "D7231978", "D7231983", "D7231984", "D7231985", "D7232001",
            "D7232029", "D6230561", "D8230009", "D8230013", "D8230014",
            "D7240028", "D7240027", "D6240004", "D8240008", "D7240023",
            "D7240022", "D7240021", "D7240020", "D7240019", "D8240007",
            "D6240003", "D7240018", "D9240001", "D7240017", "D7240016",
            "D7240015", "D7240014", "D6240002", "D7240013", "D7240012",
            "F1240001", "D6240005", "D7240031", "D8240010", "D7240029"
    };

    /**
     * Mengambil NIK valid yang belum terpakai dari daftar NIK master DIKA.
     */
    public static String findAvailableNikFromPool() {
        try {
            String adminToken = loginAsDefaultAdmin();
            for (String candidate : EXTENDED_VALID_NIK_POOL) {
                Response res = given()
                        .spec(authSpec(adminToken))
                        .queryParam("nik", candidate)
                .when()
                        .get(Constants.API_USERS_ENDPOINT)
                .then()
                        .extract()
                        .response();
                List<String> ids = res.jsonPath().getList("data.id");
                if (ids == null || ids.isEmpty()) {
                    logger.info("Found available valid NIK from pool: {}", candidate);
                    return candidate;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not query VALID_NIK_POOL availability: {}", e.getMessage());
        }
        return Constants.VALID_NIK_POOL[0];
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

