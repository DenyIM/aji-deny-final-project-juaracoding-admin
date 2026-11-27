package com.juaracoding.magang.api.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juaracoding.magang.api.BaseApiTest;
import com.juaracoding.magang.api.helper.EmployeeApiHelper;
import com.juaracoding.magang.reports.ExtentTestManager;
import com.juaracoding.magang.utils.Constants;
import com.juaracoding.magang.utils.SecurityUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.restassured.RestAssured.given;

/**
 * Pengujian Otomatisasi REST Assured API — Portal Admin Group A (Tester Deny).
 * Mencakup 65 Test Cases (SIT_ADM_API_001–030 & SIT_ADM_API_061–095):
 * 1. Auth & JWT Security (001–010: 10 TCs)
 * 2. Users CRUD, Filter & Pagination (011–022: 12 TCs)
 * 3. Attendance & Corrections GET (023–030: 8 TCs)
 * 4. Bulk Import Excel Multipart (061–071: 11 TCs)
 * 5. Binary Export & Report Generation (072–081: 10 TCs)
 * 6. Upload Image Security & Static Host (082–086: 5 TCs)
 * 7. Sync Approval, Dashboard/Leave Summary, Contract & RBAC/IDOR (087–095: 9 TCs)
 */
public class AdminGroupAApiTest extends BaseApiTest {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String adminToken;
    private String employeeToken;
    private String employeeUserId;
    private String employeeNik;
    private String companyId;
    private String divisionId;

    private String crudTestUserId;
    private String uploadedPhotoFilename;
    private final Set<String> disposableUserIds = ConcurrentHashMap.newKeySet();

    @BeforeClass(alwaysRun = true)
    public void initGroupAApiContext() {
        setupApiEngine();

        Response adminAuth = authenticate(Constants.ADMIN_EMAIL, Constants.ADMIN_PASSWORD_ENCODED);
        Assert.assertTrue(
                adminAuth.getStatusCode() == 200 || adminAuth.getStatusCode() == 201,
                "Prasyarat login Admin API harus berhasil, HTTP aktual: " + adminAuth.getStatusCode()
        );
        adminToken = adminAuth.jsonPath().getString("accessToken");
        Assert.assertNotNull(adminToken, "Admin accessToken tidak boleh null");

        EmployeeApiHelper.EmployeeSession empSession = EmployeeApiHelper.loginAsDefaultEmployee();
        employeeToken = empSession.token();
        employeeUserId = empSession.userId();

        // Ambil company_id, division_id & nik aktual dari akun karyawan referensi untuk filter export & CRUD
        Response empProfile = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("email", Constants.HYBRID_EMPLOYEE_EMAIL)
        .when()
                .get(Constants.API_USERS_ENDPOINT)
        .then()
                .extract()
                .response();

        if (empProfile.getStatusCode() == 200) {
            companyId = firstNonBlank(
                    empProfile.jsonPath().getString("data[0].company_id"),
                    empProfile.jsonPath().getString("data[0].company.id")
            );
            divisionId = empProfile.jsonPath().getString("data[0].division_id");
            employeeNik = firstNonBlank(
                    empProfile.jsonPath().getString("data[0].nik"),
                    Constants.HYBRID_EMPLOYEE_NIK
            );
        } else {
            employeeNik = Constants.HYBRID_EMPLOYEE_NIK;
        }

        if (companyId == null || companyId.isBlank()) {
            Response anyUsers = given()
                    .spec(getAuthenticatedSpec(adminToken))
                    .queryParam("$limit", 25)
            .when()
                    .get(Constants.API_USERS_ENDPOINT)
            .then()
                    .extract()
                    .response();
            List<String> cids = anyUsers.jsonPath().getList("data.company_id");
            if (cids != null) {
                for (String cid : cids) {
                    if (cid != null && !cid.isBlank()) {
                        companyId = cid;
                        break;
                    }
                }
            }
        }

        cleanupOrphanedTempUsersQuietly();
        logger.info("AdminGroupAApiTest context ready: employeeUserId={}, employeeNik={}, companyId={}, divisionId={}",
                employeeUserId, employeeNik, companyId, divisionId);
    }

    @AfterClass(alwaysRun = true)
    public void cleanupGroupAApiResources() {
        for (String uid : disposableUserIds) {
            deleteUserQuietlyWithAdminToken(uid);
        }
        disposableUserIds.clear();
        if (employeeUserId != null && !employeeUserId.isBlank() && adminToken != null) {
            given()
                    .spec(getAuthenticatedSpec(adminToken))
                    .body(Map.of("is_active", true, "isVerified", true))
            .when()
                    .patch(Constants.API_USERS_ENDPOINT + "/" + employeeUserId);
        }
    }

    // =========================================================================
    // HELPER UTILITIES
    // =========================================================================

    private static String firstNonBlank(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) {
                return c;
            }
        }
        return null;
    }

    private RequestSpecification unauthenticatedJsonSpec() {
        return given()
                .baseUri(Constants.API_BASE_URI)
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    private RequestSpecification multipartSpec(String bearerToken) {
        RequestSpecification spec = given()
                .baseUri(Constants.API_BASE_URI)
                .relaxedHTTPSValidation()
                .accept(ContentType.JSON);
        if (bearerToken != null && !bearerToken.isBlank()) {
            spec.header("Authorization", "Bearer " + bearerToken.trim());
        }
        return spec;
    }

    private RequestSpecification binarySpec(String bearerToken) {
        RequestSpecification spec = given()
                .baseUri(Constants.API_BASE_URI)
                .relaxedHTTPSValidation()
                .header("Accept", "*/*");
        if (bearerToken != null && !bearerToken.isBlank()) {
            spec.header("Authorization", "Bearer " + bearerToken.trim());
        }
        return spec;
    }

    private File getTestDataFile(String fileName) {
        Path p = Paths.get(Constants.TESTDATA_DIR, fileName).toAbsolutePath().normalize();
        Assert.assertTrue(Files.exists(p), "Fixture testdata wajib tersedia: " + p);
        return p.toFile();
    }

    private void assertValidXlsxBinary(Response res, String contextLabel) {
        int status = res.getStatusCode();
        Assert.assertTrue(status == 200 || status == 201,
                contextLabel + " seharusnya mengembalikan HTTP 200/201, aktual: HTTP " + status
                        + " | Body: " + safePreview(res));
        byte[] bytes = res.asByteArray();
        Assert.assertTrue(bytes != null && bytes.length > 4,
                contextLabel + " seharusnya mengembalikan file binary .xlsx berukuran > 0 byte");
        Assert.assertTrue(bytes[0] == 0x50 && bytes[1] == 0x4B,
                contextLabel + " seharusnya memiliki magic header ZIP/XLSX ('PK'), aktual byte[0..1]="
                        + String.format("0x%02X 0x%02X", bytes[0], bytes[1]));
    }

    private String safePreview(Response res) {
        try {
            String s = res.getBody() != null ? res.getBody().asString() : "";
            return s.length() > 240 ? s.substring(0, 240) + "..." : s;
        } catch (Exception e) {
            return "<binary>";
        }
    }

    private void deleteUserQuietlyWithAdminToken(String userId) {
        if (userId == null || userId.isBlank() || adminToken == null) {
            return;
        }
        try {
            Response r = given()
                    .spec(getAuthenticatedSpec(adminToken))
            .when()
                    .delete(Constants.API_USERS_ENDPOINT + "/" + userId);
            logger.info("Cleanup DELETE /users/{} -> HTTP {}", userId, r.getStatusCode());
        } catch (Exception e) {
            logger.warn("Cleanup skipped for /users/{}: {}", userId, e.getMessage());
        }
    }

    private void cleanupOrphanedTempUsersQuietly() {
        if (adminToken == null) {
            return;
        }
        String[] patterns = {"%testapi.deny.%", "%crud.api.%", "%rbac.patch.target%", "%rbac.delete.target%", "%escalation.fake.%"};
        for (String pat : patterns) {
            try {
                Response r = given()
                        .spec(getAuthenticatedSpec(adminToken))
                        .queryParam("email[$iLike]", pat)
                .when()
                        .get(Constants.API_USERS_ENDPOINT);
                List<String> ids = r.jsonPath().getList("data.id");
                if (ids != null) {
                    for (String id : ids) {
                        deleteUserQuietlyWithAdminToken(id);
                    }
                }
            } catch (Exception ignored) {
                // Best-effort pre-run cleanup
            }
        }
    }

    private String findUserIdByEmailFast(String email) {
        if (email == null || email.isBlank() || adminToken == null) {
            return null;
        }
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("email", email)
        .when()
                .get(Constants.API_USERS_ENDPOINT);
        List<String> ids = res.jsonPath().getList("data.id");
        return (ids != null && !ids.isEmpty()) ? ids.get(0) : null;
    }

    private String createDisposableUserViaAdmin(String prefix) {
        String masterNik = EmployeeApiHelper.findAvailableNikFromPool();
        long ts = System.currentTimeMillis();
        String uniqueEmail = prefix + "." + ts + "@hadir.com";
        String tempPass = "QaApi#" + ts;

        Map<String, Object> body = new HashMap<>();
        body.put("nik", masterNik);
        body.put("fullname", "QA API Temp " + ts);
        body.put("email", uniqueEmail);
        body.put("password", tempPass);
        body.put("company_code", "DIKA");
        body.put("is_mobile", true);
        if (divisionId != null && !divisionId.isBlank()) {
            body.put("division_id", divisionId);
        }

        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .body(body)
        .when()
                .post(Constants.API_USERS_ENDPOINT)
        .then()
                .extract()
                .response();

        if (res.getStatusCode() == 200 || res.getStatusCode() == 201) {
            String id = res.jsonPath().getString("id");
            if (id == null || id.isBlank()) {
                id = findUserIdByEmailFast(uniqueEmail);
            }
            if (id != null && !id.isBlank()) {
                disposableUserIds.add(id);
            }
            return id;
        }
        throw new IllegalStateException("Gagal membuat disposable user uji via POST /users: HTTP "
                + res.getStatusCode() + " - " + safePreview(res));
    }

    private String ensureCrudTestUser() {
        if (crudTestUserId == null || crudTestUserId.isBlank()) {
            crudTestUserId = createDisposableUserViaAdmin("crud.api");
        }
        return crudTestUserId;
    }

    // =========================================================================
    // BLOCK 1: AUTH & JWT SECURITY (SIT_ADM_API_001 - SIT_ADM_API_010)
    // =========================================================================

    @Test(priority = 1,
          description = "SIT_ADM_API_001 - Login Admin HRD dengan kredensial valid & verifikasi JWT",
          groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_API_001_LoginAdminValidAndVerifyJwt() {
        Response res = authenticate(Constants.ADMIN_EMAIL, Constants.ADMIN_PASSWORD_ENCODED);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /authentication (valid admin) -> HTTP " + status);

        Assert.assertTrue(status == 200 || status == 201,
                "Login Admin valid harus mengembalikan HTTP 200/201, aktual: " + status);
        String token = res.jsonPath().getString("accessToken");
        Assert.assertNotNull(token, "Response wajib memuat accessToken JWT");
        Assert.assertEquals(token.split("\\.").length, 3, "Format accessToken wajib berupa 3 segmen JWT");
        Assert.assertEquals(res.jsonPath().getString("user.email"), Constants.ADMIN_EMAIL,
                "Email user pada response harus sesuai akun Admin");
    }

    @Test(priority = 2,
          description = "SIT_ADM_API_002 - Login Admin HRD dengan password salah",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_002_LoginAdminWrongPassword() {
        Map<String, Object> body = Map.of(
                "email", Constants.ADMIN_EMAIL,
                "password", "WrongPass_Invalid999!",
                "strategy", "local"
        );
        Response res = unauthenticatedJsonSpec().body(body).post(Constants.API_AUTH_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /authentication (wrong password) -> HTTP " + status);

        Assert.assertTrue(status == 401 || status == 500,
                "Login dengan password salah wajib ditolak (401/500), aktual: " + status);
        Assert.assertNull(res.jsonPath().getString("accessToken"), "accessToken tidak boleh diterbitkan");
    }

    @Test(priority = 3,
          description = "SIT_ADM_API_003 - Login Admin HRD dengan email tidak terdaftar",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_003_LoginAdminUnregisteredEmail() {
        Map<String, Object> body = Map.of(
                "email", "no_admin_unregistered_999@hadir.com",
                "password", "Pass123!",
                "strategy", "local"
        );
        Response res = unauthenticatedJsonSpec().body(body).post(Constants.API_AUTH_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /authentication (unregistered email) -> HTTP " + status);

        Assert.assertTrue(status == 401 || status == 404 || status == 500,
                "Email tidak terdaftar wajib ditolak dengan NotAuthenticated, aktual: " + status);
        Assert.assertNull(res.jsonPath().getString("accessToken"), "accessToken tidak boleh diterbitkan");
    }

    @Test(priority = 4,
          description = "SIT_ADM_API_004 - Login Admin tanpa menyertakan properti strategy",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_004_LoginAdminMissingStrategy() {
        Map<String, Object> body = Map.of(
                "email", Constants.ADMIN_EMAIL,
                "password", SecurityUtil.decode(Constants.ADMIN_PASSWORD_ENCODED)
        );
        Response res = unauthenticatedJsonSpec().body(body).post(Constants.API_AUTH_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /authentication (missing strategy) -> HTTP " + status);

        Assert.assertTrue(status == 400 || status == 401 || status == 500,
                "Login tanpa field strategy wajib ditolak, aktual: " + status);
        Assert.assertNull(res.jsonPath().getString("accessToken"), "accessToken tidak boleh diterbitkan");
    }

    @Test(priority = 5,
          description = "SIT_ADM_API_005 - Login Admin dengan nilai strategy tidak didukung ('oauth2')",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_005_LoginAdminUnsupportedStrategy() {
        Map<String, Object> body = Map.of(
                "email", Constants.ADMIN_EMAIL,
                "password", SecurityUtil.decode(Constants.ADMIN_PASSWORD_ENCODED),
                "strategy", "oauth2"
        );
        Response res = unauthenticatedJsonSpec().body(body).post(Constants.API_AUTH_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /authentication (strategy=oauth2) -> HTTP " + status);

        Assert.assertTrue(status == 400 || status == 401 || status == 500,
                "Strategy autentikasi tidak didukung wajib ditolak, aktual: " + status);
        Assert.assertNull(res.jsonPath().getString("accessToken"), "accessToken tidak boleh diterbitkan");
    }

    @Test(priority = 6,
          description = "SIT_ADM_API_006 - Login Admin dengan body JSON kosong {}",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_006_LoginAdminEmptyBody() {
        Response res = unauthenticatedJsonSpec().body("{}").post(Constants.API_AUTH_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /authentication (empty JSON {}) -> HTTP " + status);

        Assert.assertTrue(status == 400 || status == 401 || status == 500,
                "Body JSON kosong wajib ditolak (400/401/500), aktual: " + status);
    }

    @Test(priority = 7,
          description = "SIT_ADM_API_007 - Login Admin dengan payload JSON rusak (Malformed JSON)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_007_LoginAdminMalformedJson() {
        String malformedJson = "{\"email\": \"admin@hadir.com\", broken_json";
        Response res = unauthenticatedJsonSpec().body(malformedJson).post(Constants.API_AUTH_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /authentication (malformed JSON) -> HTTP " + status);

        Assert.assertEquals(status, 400,
                "Payload JSON rusak wajib ditolak dengan HTTP 400 Bad Request, aktual: " + status);
    }

    @Test(priority = 8,
          description = "SIT_ADM_API_008 - Kirim request ke endpoint /authentication menggunakan method GET",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_008_GetAuthenticationMethodNotAllowed() {
        Response res = unauthenticatedJsonSpec().get(Constants.API_AUTH_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /authentication -> HTTP " + status);

        Assert.assertTrue(status == 404 || status == 405,
                "Method GET pada /authentication wajib ditolak 404/405 Method Not Allowed, aktual: " + status);
    }

    @Test(priority = 9,
          description = "SIT_ADM_API_009 - Verifikasi struktur klaim payload JWT Admin (sub, iat, exp)",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_009_VerifyAdminJwtPayloadClaims() throws Exception {
        String[] parts = adminToken.split("\\.");
        Assert.assertEquals(parts.length, 3, "Token JWT harus terdiri atas header.payload.signature");

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        JsonNode claims = OBJECT_MAPPER.readTree(payloadJson);
        ExtentTestManager.logInfo("Decoded JWT claim keys: subPresent=" + claims.hasNonNull("sub")
                + ", iat=" + claims.path("iat").asLong() + ", exp=" + claims.path("exp").asLong());

        Assert.assertTrue(claims.hasNonNull("sub") && !claims.get("sub").asText().isBlank(),
                "Payload JWT wajib memuat klaim user ID (sub)");
        long iat = claims.path("iat").asLong(0);
        long exp = claims.path("exp").asLong(0);
        Assert.assertTrue(iat > 0 && exp > iat,
                "Payload JWT wajib memiliki iat > 0 dan waktu kadaluarsa exp > iat");
    }

    @Test(priority = 10,
          description = "SIT_ADM_API_010 - Akses endpoint menggunakan token JWT yang telah dimodifikasi signature-nya",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_010_AccessUsersWithTamperedJwtSignature() {
        String[] parts = adminToken.split("\\.");
        String sig = parts[2];
        char replacement = sig.charAt(sig.length() - 1) == 'A' ? 'B' : 'A';
        String tamperedToken = parts[0] + "." + parts[1] + "." + sig.substring(0, sig.length() - 1) + replacement;

        Response res = given()
                .spec(getAuthenticatedSpec(tamperedToken))
                .queryParam("$limit", 1)
        .when()
                .get(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /users (tampered JWT signature) -> HTTP " + status);

        Assert.assertTrue(status == 401 || status == 403 || status == 500,
                "Token JWT dengan signature dimodifikasi wajib ditolak server, aktual: " + status);
    }

    // =========================================================================
    // BLOCK 2: USERS CRUD, PAGINATION & FILTER (SIT_ADM_API_011 - SIT_ADM_API_022)
    // =========================================================================

    @Test(priority = 11,
          description = "SIT_ADM_API_011 - Ambil daftar seluruh user menggunakan Bearer Token Admin valid",
          groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_API_011_GetUsersWithValidAdminToken() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
        .when()
                .get(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        List<Object> data = res.jsonPath().getList("data");
        ExtentTestManager.logInfo("GET /users -> HTTP " + status + ", returned rows=" + (data != null ? data.size() : 0));

        Assert.assertEquals(status, 200, "GET /users harus mengembalikan HTTP 200 OK");
        Assert.assertNotNull(data, "Field array 'data' wajib ada");
        Assert.assertFalse(data.isEmpty(), "Daftar user tidak boleh kosong");
    }

    @Test(priority = 12,
          description = "SIT_ADM_API_012 - Akses daftar seluruh user tanpa menyertakan Bearer Token",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_012_GetUsersWithoutBearerToken() {
        Response res = unauthenticatedJsonSpec().get(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /users (no token) -> HTTP " + status);

        Assert.assertTrue(status == 401 || status == 500,
                "GET /users tanpa Bearer Token wajib ditolak (401 NotAuthenticated), aktual: " + status);
    }

    @Test(priority = 13,
          description = "SIT_ADM_API_013 - Ambil daftar user dengan parameter pagination $limit=5&$skip=0",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_013_GetUsersWithPaginationLimit5() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("$limit", 5)
                .queryParam("$skip", 0)
        .when()
                .get(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        List<Object> data = res.jsonPath().getList("data");
        ExtentTestManager.logInfo("GET /users?$limit=5&$skip=0 -> HTTP " + status + ", count=" + (data != null ? data.size() : 0));

        Assert.assertEquals(status, 200, "GET /users dengan pagination harus 200 OK");
        Assert.assertNotNull(data, "Array data wajib ada");
        Assert.assertTrue(data.size() <= 5, "Jumlah item yang dikembalikan maksimal 5, aktual: " + data.size());
    }

    @Test(priority = 14,
          description = "SIT_ADM_API_014 - Ambil daftar user dengan nilai $limit negatif ($limit=-5)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_014_GetUsersWithNegativeLimit() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("$limit", -5)
        .when()
                .get(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /users?$limit=-5 -> HTTP " + status);

        // Server menangani limit negatif dengan aman (ditolak 400/500 terstruktur atau dibatasi aman <= default limit)
        Assert.assertTrue(status == 200 || status == 400 || status == 422 || status == 500,
                "Server harus merespons request $limit=-5 secara terdefinisi, aktual: " + status);
        if (status == 200) {
            List<Object> data = res.jsonPath().getList("data");
            Assert.assertTrue(data == null || data.size() <= 50,
                    "Jika dinormalisasi 200 OK, jumlah data tetap wajib dibatasi aman");
        }
    }

    @Test(priority = 15,
          description = "SIT_ADM_API_015 - Filter daftar user berdasarkan email=hadirsqa1@gmail.com",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_015_FilterUsersByEmail() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("email", Constants.HYBRID_EMPLOYEE_EMAIL)
        .when()
                .get(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        List<String> emails = res.jsonPath().getList("data.email");
        ExtentTestManager.logInfo("GET /users?email=" + Constants.HYBRID_EMPLOYEE_EMAIL + " -> HTTP " + status + ", emails=" + emails);

        Assert.assertEquals(status, 200, "Filter email harus mengembalikan HTTP 200 OK");
        Assert.assertNotNull(emails, "List data.email tidak boleh null");
        Assert.assertTrue(emails.contains(Constants.HYBRID_EMPLOYEE_EMAIL),
                "Hasil filter wajib memuat akun " + Constants.HYBRID_EMPLOYEE_EMAIL);
    }

    @Test(priority = 16,
          description = "SIT_ADM_API_016 - Filter daftar user berdasarkan NIK karyawan valid",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_016_FilterUsersByNik() {
        String targetNik = Constants.HYBRID_EMPLOYEE_NIK;
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("nik", targetNik)
        .when()
                .get(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        List<String> niks = res.jsonPath().getList("data.nik");

        // Jika NIK default pada shared staging sedang berubah (mis. diedit tester lain), gunakan NIK aktual akun referensi
        if (status == 200 && (niks == null || niks.isEmpty())
                && employeeNik != null && !employeeNik.equals(targetNik)) {
            targetNik = employeeNik;
            res = given()
                    .spec(getAuthenticatedSpec(adminToken))
                    .queryParam("nik", targetNik)
            .when()
                    .get(Constants.API_USERS_ENDPOINT);
            status = res.getStatusCode();
            niks = res.jsonPath().getList("data.nik");
        }
        ExtentTestManager.logInfo("GET /users?nik=" + targetNik + " -> HTTP " + status + ", niks=" + niks);

        Assert.assertEquals(status, 200, "Filter NIK harus mengembalikan HTTP 200 OK");
        Assert.assertNotNull(niks, "List data.nik tidak boleh null");
        Assert.assertTrue(niks.contains(targetNik),
                "Hasil filter wajib memuat NIK " + targetNik);
    }

    @Test(priority = 17,
          description = "SIT_ADM_API_017 - Ambil detail user dengan ID yang tidak ditemukan di database",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_017_GetUserByNonExistentId() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
        .when()
                .get(Constants.API_USERS_ENDPOINT + "/99999999");
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /users/99999999 -> HTTP " + status);

        Assert.assertTrue(status == 404 || status == 400,
                "Detail user dengan ID tidak terdaftar harus mengembalikan 404 Not Found (atau 400), aktual: " + status);
    }

    @Test(priority = 18,
          description = "SIT_ADM_API_018 - Tambah user karyawan baru melalui endpoint POST /users",
          groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_API_018_CreateNewUserViaPostUsers() {
        String masterNik = EmployeeApiHelper.findAvailableNikFromPool();
        long ts = System.currentTimeMillis();
        String testEmail = "testapi.deny." + ts + "@hadir.com";

        Map<String, Object> body = new HashMap<>();
        body.put("nik", masterNik);
        body.put("fullname", "Test API User " + ts);
        body.put("email", testEmail);
        body.put("password", "QaApi#" + ts);
        body.put("company_code", "DIKA");
        body.put("is_mobile", true);
        if (divisionId != null && !divisionId.isBlank()) {
            body.put("division_id", divisionId);
        }

        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .body(body)
        .when()
                .post(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /users [" + testEmail + "] -> HTTP " + status);

        Assert.assertTrue(status == 200 || status == 201,
                "Pembuatan user baru via POST /users harus 200/201, aktual: " + status + " | " + safePreview(res));
        crudTestUserId = res.jsonPath().getString("id");
        if (crudTestUserId == null || crudTestUserId.isBlank()) {
            crudTestUserId = findUserIdByEmailFast(testEmail);
        }
        Assert.assertNotNull(crudTestUserId, "User uji yang baru dibuat wajib memiliki ID");
        disposableUserIds.add(crudTestUserId);
    }

    @Test(priority = 19,
          description = "SIT_ADM_API_019 - Tambah user baru dengan email duplikat yang sudah terdaftar",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_019_CreateUserDuplicateEmail() {
        Map<String, Object> body = Map.of(
                "email", Constants.ADMIN_EMAIL,
                "fullname", "Duplicate Email Test",
                "password", "QaDup#12345"
        );
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .body(body)
        .when()
                .post(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /users (duplicate email) -> HTTP " + status);

        Assert.assertTrue(status >= 400,
                "Pembuatan user dengan email duplikat wajib ditolak (>= 400), aktual: " + status);
    }

    @Test(priority = 20,
          description = "SIT_ADM_API_020 - Tambah user baru dengan payload kosong tanpa field wajib",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_020_CreateUserEmptyPayload() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .body("{}")
        .when()
                .post(Constants.API_USERS_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /users (empty {}) -> HTTP " + status);

        Assert.assertTrue(status >= 400,
                "Pembuatan user dengan payload kosong wajib ditolak (>= 400), aktual: " + status);
    }

    @Test(priority = 21,
          description = "SIT_ADM_API_021 - Update data nama dan divisi karyawan via PATCH /users/{id}",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_021_UpdateUserViaPatchUsers() {
        String targetId = ensureCrudTestUser();
        String updatedName = "Test API Updated " + System.currentTimeMillis();
        Map<String, Object> patchBody = new HashMap<>();
        patchBody.put("fullname", updatedName);
        if (divisionId != null && !divisionId.isBlank()) {
            patchBody.put("division_id", divisionId);
        }

        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .body(patchBody)
        .when()
                .patch(Constants.API_USERS_ENDPOINT + "/" + targetId);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("PATCH /users/" + targetId + " -> HTTP " + status);

        Assert.assertEquals(status, 200,
                "Update data karyawan via PATCH /users/{id} harus 200 OK, aktual: " + status + " | " + safePreview(res));
        Assert.assertEquals(res.jsonPath().getString("fullname"), updatedName,
                "Nama karyawan harus terbarui sesuai payload PATCH");
    }

    @Test(priority = 22,
          description = "SIT_ADM_API_022 - Hapus data user uji melalui DELETE /users/{id}",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_022_DeleteTestUserViaDeleteUsers() {
        String targetId = ensureCrudTestUser();
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
        .when()
                .delete(Constants.API_USERS_ENDPOINT + "/" + targetId);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("DELETE /users/" + targetId + " -> HTTP " + status);

        Assert.assertEquals(status, 200, "DELETE /users/{id} harus mengembalikan HTTP 200 OK");
        disposableUserIds.remove(targetId);
        crudTestUserId = null;

        Response checkGone = given()
                .spec(getAuthenticatedSpec(adminToken))
        .when()
                .get(Constants.API_USERS_ENDPOINT + "/" + targetId);
        Assert.assertEquals(checkGone.getStatusCode(), 404,
                "User uji yang telah dihapus harus mengembalikan 404 saat diakses ulang");
    }

    // =========================================================================
    // BLOCK 3: ATTENDANCE & CORRECTIONS GET (SIT_ADM_API_023 - SIT_ADM_API_030)
    // =========================================================================

    @Test(priority = 23,
          description = "SIT_ADM_API_023 - Admin mengambil seluruh daftar presensi karyawan",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_023_GetAllAttendanceActivity() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
        .when()
                .get(Constants.API_ACTIVITY_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /activity -> HTTP " + status);

        Assert.assertEquals(status, 200, "GET /activity sebagai Admin harus 200 OK");
        Assert.assertNotNull(res.jsonPath().getList("data"), "Response wajib memiliki array 'data'");
    }

    @Test(priority = 24,
          description = "SIT_ADM_API_024 - Filter daftar presensi berdasarkan periode tanggal (created_at[$gte] & created_at[$lte])",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_024_FilterActivityByDateRange() {
        String endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T23:59:59.999Z";
        String startDate = LocalDate.now().minusDays(14).format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00.000Z";

        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("created_at[$gte]", startDate)
                .queryParam("created_at[$lte]", endDate)
        .when()
                .get(Constants.API_ACTIVITY_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /activity?created_at[$gte]=" + startDate
                + "&created_at[$lte]=" + endDate + " -> HTTP " + status);

        Assert.assertEquals(status, 200,
                "Filter presensi berdasarkan periode tanggal harus 200 OK, aktual: HTTP " + status + " | " + safePreview(res));
        Assert.assertNotNull(res.jsonPath().getList("data"), "Array data presensi wajib tersedia");
    }

    @Test(priority = 25,
          description = "SIT_ADM_API_025 - Filter daftar presensi berdasarkan userId tertentu",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_025_FilterActivityByUserId() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("user_id", employeeUserId)
        .when()
                .get(Constants.API_ACTIVITY_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /activity?user_id=" + employeeUserId + " -> HTTP " + status);

        Assert.assertEquals(status, 200, "Filter presensi berdasarkan user_id harus 200 OK");
        Assert.assertNotNull(res.jsonPath().getList("data"), "Array data wajib tersedia");
    }

    @Test(priority = 26,
          description = "SIT_ADM_API_026 - Filter daftar presensi khusus karyawan yang terlambat",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_026_FilterActivityByLateStatus() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("status", "late")
        .when()
                .get(Constants.API_ACTIVITY_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /activity?status=late -> HTTP " + status);

        Assert.assertEquals(status, 200, "Filter presensi status=late harus 200 OK");
        Assert.assertNotNull(res.jsonPath().getList("data"), "Array data wajib tersedia");
    }

    @Test(priority = 27,
          description = "SIT_ADM_API_027 - Akses endpoint GET /activity tanpa menyertakan token autentikasi",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_027_GetActivityWithoutToken() {
        Response res = unauthenticatedJsonSpec().get(Constants.API_ACTIVITY_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /activity (no token) -> HTTP " + status);

        Assert.assertTrue(status == 401 || status == 500,
                "GET /activity tanpa Authorization wajib ditolak (401/500), aktual: " + status);
    }

    @Test(priority = 28,
          description = "SIT_ADM_API_028 - Admin mengambil seluruh daftar jurnal aktivitas harian",
          groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_API_028_GetDailyActivityJournalList() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("$sort[created_at]", -1)
        .when()
                .get(Constants.API_ACTIVITY_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /activity (sorted journal) -> HTTP " + status);

        Assert.assertEquals(status, 200, "Pengambilan daftar jurnal aktivitas harian harus 200 OK");
        Assert.assertNotNull(res.jsonPath().getList("data"), "List data aktivitas wajib tersedia");
    }

    @Test(priority = 29,
          description = "SIT_ADM_API_029 - Ambil jurnal aktivitas dengan parameter pagination $limit=10",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_029_GetActivityWithPaginationLimit10() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("$limit", 10)
                .queryParam("$skip", 0)
        .when()
                .get(Constants.API_ACTIVITY_ENDPOINT);
        int status = res.getStatusCode();
        List<Object> rows = res.jsonPath().getList("data");
        ExtentTestManager.logInfo("GET /activity?$limit=10 -> HTTP " + status + ", count=" + (rows != null ? rows.size() : 0));

        Assert.assertEquals(status, 200, "Pagination GET /activity?$limit=10 harus 200 OK");
        Assert.assertNotNull(rows, "Array data wajib tersedia");
        Assert.assertTrue(rows.size() <= 10, "Jumlah record maksimal 10, aktual: " + rows.size());
    }

    @Test(priority = 30,
          description = "SIT_ADM_API_030 - Admin mengambil seluruh daftar pengajuan koreksi absen",
          groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_API_030_GetAllActivityCorrections() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
        .when()
                .get(Constants.API_CORRECTIONS_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /activity-correction -> HTTP " + status);

        Assert.assertEquals(status, 200, "GET /activity-correction sebagai Admin harus 200 OK");
        Assert.assertNotNull(res.jsonPath().getList("data"), "List koreksi absen wajib tersedia");
    }

    // =========================================================================
    // BLOCK 4: BULK IMPORT EXCEL MULTIPART (SIT_ADM_API_061 - SIT_ADM_API_071)
    // =========================================================================

    @Test(priority = 31,
          description = "SIT_ADM_API_061 - Import data absensi massal via multipart file .xlsx valid dengan Bearer Token Admin",
          groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_API_061_ImportAbsenUsersValidXlsx() {
        File excelFile = getTestDataFile("valid_absen.xlsx");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", excelFile, XLSX_MIME)
        .when()
                .post(Constants.API_IMPORT_ABSEN_ENDPOINT);
        int status = res.getStatusCode();
        String body = safePreview(res);
        ExtentTestManager.logInfo("POST /import-absen-users (valid_absen.xlsx) -> HTTP " + status + " | " + body);

        Assert.assertTrue(status == 200 || status == 201,
                "Import absensi valid harus 200/201 OK, aktual: " + status + " | " + body);
        Assert.assertFalse(body.contains("\"Invalid file\""),
                "File .xlsx valid tidak boleh dianggap Invalid file: " + body);
    }

    @Test(priority = 32,
          description = "SIT_ADM_API_062 - Import absensi menolak file non-Excel (.pdf) pada multipart upload",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_062_ImportAbsenRejectPdfFile() {
        File pdfFile = getTestDataFile("invalid_upload.pdf");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", pdfFile, "application/pdf")
        .when()
                .post(Constants.API_IMPORT_ABSEN_ENDPOINT);
        int status = res.getStatusCode();
        String body = safePreview(res);
        ExtentTestManager.logInfo("POST /import-absen-users (invalid_upload.pdf) -> HTTP " + status + " | " + body);

        boolean rejectedByStatus = status >= 400;
        boolean rejectedByPayload = body.toLowerCase().contains("error") || body.toLowerCase().contains("invalid");
        Assert.assertTrue(rejectedByStatus || rejectedByPayload,
                "File PDF non-Excel wajib ditolak oleh endpoint import absen, aktual HTTP " + status + ": " + body);
    }

    @Test(priority = 33,
          description = "SIT_ADM_API_063 - Import absensi tanpa melampirkan file pada request body",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_063_ImportAbsenWithoutFileAttachment() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .body("{}")
        .when()
                .post(Constants.API_IMPORT_ABSEN_ENDPOINT);
        int status = res.getStatusCode();
        String body = safePreview(res);
        ExtentTestManager.logInfo("POST /import-absen-users (no file) -> HTTP " + status + " | " + body);

        boolean rejected = status >= 400 || body.toLowerCase().contains("error") || body.toLowerCase().contains("invalid");
        Assert.assertTrue(rejected,
                "Import absensi tanpa file wajib ditolak server, aktual HTTP " + status + ": " + body);
    }

    @Test(priority = 34,
          description = "SIT_ADM_API_064 - Karyawan biasa mencoba hit endpoint Import Absen (Privilege Escalation RBAC)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_064_ImportAbsenRbacRegularEmployeeRejected() {
        File excelFile = getTestDataFile("valid_absen.xlsx");
        Response res = multipartSpec(employeeToken)
                .multiPart("uri", excelFile, XLSX_MIME)
        .when()
                .post(Constants.API_IMPORT_ABSEN_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /import-absen-users (EmployeeToken RBAC) -> HTTP " + status + " | " + safePreview(res));

        Assert.assertTrue(status == 401 || status == 403,
                "[SECURITY RBAC] Karyawan biasa seharusnya ditolak (401/403) saat memanggil POST /import-absen-users, aktual: HTTP "
                        + status + " | " + safePreview(res));
    }

    @Test(priority = 35,
          description = "SIT_ADM_API_065 - Import update status aktif karyawan massal via file .xlsx valid",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_065_ImportStatusAktifValidXlsx() {
        File excelFile = getTestDataFile("status_aktif.xlsx");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", excelFile, XLSX_MIME)
        .when()
                .post(Constants.API_IMPORT_STATUS_AKTIF_ENDPOINT);
        int status = res.getStatusCode();
        String body = safePreview(res);
        ExtentTestManager.logInfo("POST /upload-excel-status-users (status_aktif.xlsx) -> HTTP " + status + " | " + body);

        Assert.assertTrue(status == 200 || status == 201,
                "Import status aktif karyawan valid harus 200/201 OK, aktual: " + status + " | " + body);
        Assert.assertFalse(body.contains("\"Invalid file\""),
                "File status_aktif.xlsx valid tidak boleh dianggap Invalid file: " + body);
    }

    @Test(priority = 36,
          description = "SIT_ADM_API_066 - Import status aktif user tanpa menyertakan Authorization Bearer Token",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_066_ImportStatusAktifWithoutToken() {
        File excelFile = getTestDataFile("status_aktif.xlsx");
        Response res = multipartSpec(null)
                .multiPart("uri", excelFile, XLSX_MIME)
        .when()
                .post(Constants.API_IMPORT_STATUS_AKTIF_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /upload-excel-status-users (no token) -> HTTP " + status);

        Assert.assertEquals(status, 401,
                "Import status aktif tanpa Bearer Token wajib ditolak dengan HTTP 401 Unauthorized, aktual: " + status);
    }

    @Test(priority = 37,
          description = "SIT_ADM_API_067 - Import update struktur user (Atasan/Unit/Jabatan) via file .xlsx valid",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_067_ImportStrukturUserValidXlsx() {
        File excelFile = getTestDataFile("struktur_valid.xlsx");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", excelFile, XLSX_MIME)
        .when()
                .post(Constants.API_IMPORT_STRUKTUR_USER_ENDPOINT);
        int status = res.getStatusCode();
        String body = safePreview(res);
        ExtentTestManager.logInfo("POST /import-update-users (struktur_valid.xlsx) -> HTTP " + status + " | " + body);

        Assert.assertTrue(status == 200 || status == 201,
                "Import struktur user valid harus 200/201 OK, aktual: " + status + " | " + body);
        Assert.assertFalse(body.contains("\"Invalid file\""),
                "File struktur_valid.xlsx valid tidak boleh dianggap Invalid file: " + body);
    }

    @Test(priority = 38,
          description = "SIT_ADM_API_068 - Import struktur user menolak file corrupt / ekstensi .txt bukan Excel",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_068_ImportStrukturUserRejectTxtFile() {
        File txtFile = getTestDataFile("invalid_upload.txt");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", txtFile, "text/plain")
        .when()
                .post(Constants.API_IMPORT_STRUKTUR_USER_ENDPOINT);
        int status = res.getStatusCode();
        String body = safePreview(res);
        ExtentTestManager.logInfo("POST /import-update-users (invalid_upload.txt) -> HTTP " + status + " | " + body);

        boolean rejected = status >= 400 || body.toLowerCase().contains("error") || body.toLowerCase().contains("invalid");
        Assert.assertTrue(rejected,
                "File .txt non-Excel wajib ditolak pada import struktur user, aktual HTTP " + status + ": " + body);
    }

    @Test(priority = 39,
          description = "SIT_ADM_API_069 - [Known Defect] Import kuota cuti karyawan massal via file .xlsx template resmi",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_069_ImportLeaveValidXlsxKnownDefect() {
        File excelFile = getTestDataFile("cuti_valid.xlsx");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", excelFile, XLSX_MIME)
        .when()
                .post(Constants.API_IMPORT_LEAVE_ENDPOINT);
        int status = res.getStatusCode();
        String body = safePreview(res);
        ExtentTestManager.logInfo("POST /import-leave (cuti_valid.xlsx) -> HTTP " + status + " | " + body);

        Assert.assertTrue(status == 200 || status == 201,
                "[KNOWN DEFECT] POST /import-leave dengan file template cuti_valid.xlsx seharusnya berhasil 200/201 tanpa crash 500, aktual: HTTP "
                        + status + " | " + body);
    }

    @Test(priority = 40,
          description = "SIT_ADM_API_070 - Import cuti menolak file non-Excel (.pdf) tanpa menyebabkan unhandled exception",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_070_ImportLeaveRejectPdfGracefully() {
        File pdfFile = getTestDataFile("invalid_upload.pdf");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", pdfFile, "application/pdf")
        .when()
                .post(Constants.API_IMPORT_LEAVE_ENDPOINT);
        int status = res.getStatusCode();
        String body = safePreview(res);
        ExtentTestManager.logInfo("POST /import-leave (invalid_upload.pdf) -> HTTP " + status + " | " + body);

        Assert.assertTrue(status == 400 || status == 415 || status == 422,
                "[VALIDATION] POST /import-leave dengan file .pdf seharusnya ditolak secara elegan (400/415/422) tanpa HTTP 500 crash, aktual: HTTP "
                        + status + " | " + body);
    }

    @Test(priority = 41,
          description = "SIT_ADM_API_071 - Karyawan biasa mencoba hit POST /import-leave (RBAC Security Check)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_071_ImportLeaveRbacRegularEmployeeRejected() {
        File excelFile = getTestDataFile("cuti_valid.xlsx");
        Response res = multipartSpec(employeeToken)
                .multiPart("uri", excelFile, XLSX_MIME)
        .when()
                .post(Constants.API_IMPORT_LEAVE_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /import-leave (EmployeeToken RBAC) -> HTTP " + status + " | " + safePreview(res));

        Assert.assertTrue(status == 401 || status == 403,
                "[SECURITY RBAC] Karyawan biasa seharusnya ditolak (401/403) pada POST /import-leave, aktual: HTTP "
                        + status + " | " + safePreview(res));
    }

    // =========================================================================
    // BLOCK 5: BINARY EXCEL EXPORT & REPORTS (SIT_ADM_API_072 - SIT_ADM_API_081)
    // =========================================================================

    @Test(priority = 42,
          description = "SIT_ADM_API_072 - Export daftar karyawan ke file Excel (.xlsx) oleh Admin via GET /export-user",
          groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_API_072_ExportUsersExcelByAdmin() {
        Assert.assertNotNull(companyId, "Prasyarat companyId harus tersedia untuk GET /export-user");
        Response res = binarySpec(adminToken)
                .queryParam("company_id", companyId)
                .queryParam("nik", Constants.HYBRID_EMPLOYEE_NIK)
        .when()
                .get(Constants.API_EXPORT_USER_ENDPOINT);
        ExtentTestManager.logInfo("GET /export-user?company_id=" + companyId + " -> HTTP " + res.getStatusCode()
                + ", bytes=" + res.asByteArray().length);

        assertValidXlsxBinary(res, "GET /export-user (Admin)");
    }

    @Test(priority = 43,
          description = "SIT_ADM_API_073 - Export daftar karyawan dengan filter query company_id & nik via GET /export-user",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_073_ExportUsersFilteredByNik() {
        Response res = binarySpec(adminToken)
                .queryParam("company_id", companyId)
                .queryParam("nik", Constants.HYBRID_EMPLOYEE_NIK)
                .queryParam("fullname", "Hadir SQA")
        .when()
                .get(Constants.API_EXPORT_USER_ENDPOINT);
        ExtentTestManager.logInfo("GET /export-user (filtered nik & fullname) -> HTTP " + res.getStatusCode()
                + ", bytes=" + res.asByteArray().length);

        assertValidXlsxBinary(res, "GET /export-user filtered");
    }

    @Test(priority = 44,
          description = "SIT_ADM_API_074 - Karyawan biasa mencoba mengunduh database user via GET /export-user (RBAC)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_074_ExportUsersRbacRegularEmployeeRejected() {
        Response res = binarySpec(employeeToken)
                .queryParam("company_id", companyId)
                .queryParam("nik", Constants.HYBRID_EMPLOYEE_NIK)
        .when()
                .get(Constants.API_EXPORT_USER_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /export-user (EmployeeToken RBAC) -> HTTP " + status);

        Assert.assertTrue(status == 401 || status == 403,
                "[SECURITY RBAC] Karyawan biasa seharusnya ditolak (401/403) saat mengakses GET /export-user, aktual: HTTP " + status);
    }

    @Test(priority = 45,
          description = "SIT_ADM_API_075 - Export rekapitulasi Sisa Cuti karyawan dalam format binary .xlsx",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_075_ExportLeaveRecapBinaryXlsx() {
        Map<String, Object> body = new HashMap<>();
        body.put("company_id", companyId);
        body.put("user_id", employeeUserId);
        body.put("nik", Constants.HYBRID_EMPLOYEE_NIK);

        Response res = binarySpec(adminToken)
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post(Constants.API_EXPORT_LEAVE_ENDPOINT);
        ExtentTestManager.logInfo("POST /export-leave -> HTTP " + res.getStatusCode()
                + ", bytes=" + res.asByteArray().length);

        assertValidXlsxBinary(res, "POST /export-leave");
    }

    @Test(priority = 46,
          description = "SIT_ADM_API_076 - [Known Defect] Verifikasi endpoint GET /export-leave wajib menolak akses tanpa Bearer Token",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_076_GetExportLeaveWithoutTokenKnownDefect() {
        Response res = unauthenticatedJsonSpec().get(Constants.API_EXPORT_LEAVE_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /export-leave (no token) -> HTTP " + status);

        Assert.assertEquals(status, 401,
                "[KNOWN DEFECT] Endpoint GET /export-leave tanpa Bearer Token wajib ditolak dengan HTTP 401 Unauthorized, aktual: HTTP "
                        + status + " | " + safePreview(res));
    }

    @Test(priority = 47,
          description = "SIT_ADM_API_077 - Generate & export ringkasan kehadiran karyawan ke file Excel (.xlsx)",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_077_ExportAttendanceSummaryXlsx() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Map<String, Object> body = new HashMap<>();
        body.put("start_date", today);
        body.put("end_date", today);
        body.put("user_id", employeeUserId);
        body.put("nik", Constants.HYBRID_EMPLOYEE_NIK);

        Response res = binarySpec(adminToken)
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post(Constants.API_EXPORT_ATTENDANCE_SUMMARY_ENDPOINT);
        ExtentTestManager.logInfo("POST /export-attendance-summary -> HTTP " + res.getStatusCode()
                + ", bytes=" + res.asByteArray().length);

        assertValidXlsxBinary(res, "POST /export-attendance-summary");
    }

    @Test(priority = 48,
          description = "SIT_ADM_API_078 - Generate laporan kehadiran/lembur/koreksi (.xlsx) melalui POST /generate-report",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_078_GenerateReportBinaryXlsx() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Map<String, Object> body = new HashMap<>();
        body.put("date_from", today);
        body.put("date_to", today);
        body.put("company_id", companyId);
        body.put("user_id", employeeUserId);
        body.put("nik", Constants.HYBRID_EMPLOYEE_NIK);

        Response res = binarySpec(adminToken)
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post(Constants.API_GENERATE_REPORT_ENDPOINT);
        ExtentTestManager.logInfo("POST /generate-report -> HTTP " + res.getStatusCode()
                + ", bytes=" + res.asByteArray().length);

        assertValidXlsxBinary(res, "POST /generate-report");
    }

    @Test(priority = 49,
          description = "SIT_ADM_API_079 - Generate laporan per divisi/departemen melalui POST /generate-report-type",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_079_GenerateReportTypeBinaryXlsx() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Map<String, Object> body = new HashMap<>();
        body.put("date_from", today);
        body.put("date_to", today);
        body.put("company_id", companyId);
        body.put("user_id", employeeUserId);
        body.put("nik", Constants.HYBRID_EMPLOYEE_NIK);

        Response res = binarySpec(adminToken)
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post(Constants.API_GENERATE_REPORT_TYPE_ENDPOINT);
        ExtentTestManager.logInfo("POST /generate-report-type -> HTTP " + res.getStatusCode()
                + ", bytes=" + res.asByteArray().length);

        assertValidXlsxBinary(res, "POST /generate-report-type");
    }

    @Test(priority = 50,
          description = "SIT_ADM_API_080 - Generate laporan harian (Daily Report) tanggal valid ke format Excel",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_080_GenerateDailyReportBinaryXlsx() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Map<String, Object> body = new HashMap<>();
        body.put("report_date", today);
        body.put("company_id", companyId);
        body.put("user_id", employeeUserId);

        Response res = binarySpec(adminToken)
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post(Constants.API_DAILY_REPORT_ENDPOINT);
        ExtentTestManager.logInfo("POST /daily-report -> HTTP " + res.getStatusCode()
                + ", bytes=" + res.asByteArray().length);

        assertValidXlsxBinary(res, "POST /daily-report");
    }

    @Test(priority = 51,
          description = "SIT_ADM_API_081 - Akses endpoint POST /daily-report oleh User biasa atau tanpa token (RBAC Check)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_081_GenerateDailyReportUnauthenticatedOrEmployeeRejected() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Map<String, Object> body = Map.of("report_date", today);

        Response unauthRes = unauthenticatedJsonSpec()
                .body(body)
        .when()
                .post(Constants.API_DAILY_REPORT_ENDPOINT);
        int unauthStatus = unauthRes.getStatusCode();
        ExtentTestManager.logInfo("POST /daily-report (no token) -> HTTP " + unauthStatus);

        Assert.assertTrue(unauthStatus == 401 || unauthStatus == 403,
                "[SECURITY] POST /daily-report tanpa token wajib ditolak (401/403), aktual: HTTP " + unauthStatus);

        Response empRes = binarySpec(employeeToken)
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post(Constants.API_DAILY_REPORT_ENDPOINT);
        int empStatus = empRes.getStatusCode();
        ExtentTestManager.logInfo("POST /daily-report (EmployeeToken RBAC) -> HTTP " + empStatus);

        Assert.assertTrue(empStatus == 401 || empStatus == 403,
                "[SECURITY RBAC] Karyawan biasa seharusnya ditolak (401/403) pada POST /daily-report, aktual: HTTP " + empStatus);
    }

    // =========================================================================
    // BLOCK 6: UPLOAD SECURITY & STATIC ASSET (SIT_ADM_API_082 - SIT_ADM_API_086)
    // =========================================================================

    @Test(priority = 52,
          description = "SIT_ADM_API_082 - Upload file gambar valid (< 2 MB) ke endpoint POST /upload sebagai Admin",
          groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_API_082_UploadValidPhotoAsAdmin() {
        File photoFile = getTestDataFile("valid_photo.jpg");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", photoFile, "image/jpeg")
        .when()
                .post(Constants.API_UPLOAD_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /upload (valid_photo.jpg) -> HTTP " + status + " | " + safePreview(res));

        Assert.assertTrue(status == 200 || status == 201,
                "Upload foto valid (< 2 MB) harus mengembalikan HTTP 200/201, aktual: " + status);
        uploadedPhotoFilename = firstNonBlank(
                res.jsonPath().getString("id"),
                res.jsonPath().getString("filename"),
                res.jsonPath().getString("uri")
        );
        Assert.assertNotNull(uploadedPhotoFilename, "Response POST /upload wajib mengembalikan id/filename gambar");
    }

    @Test(priority = 53,
          description = "SIT_ADM_API_083 - Verifikasi file gambar yang baru diupload via /upload dapat diakses dan tidak broken",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_083_VerifyUploadedImageAccessibleNotBroken() {
        if (uploadedPhotoFilename == null || uploadedPhotoFilename.isBlank()) {
            uploadedPhotoFilename = EmployeeApiHelper.uploadTestPhoto("valid_photo.jpg");
        }
        Assert.assertNotNull(uploadedPhotoFilename, "Prasyarat nama file hasil upload harus tersedia");

        Response res = given()
                .baseUri(Constants.API_STATIC_UPLOADS_BASE_URI)
                .relaxedHTTPSValidation()
                .header("Accept", "image/*,*/*")
        .when()
                .get("/" + uploadedPhotoFilename);
        int status = res.getStatusCode();
        String contentType = res.getHeader("Content-Type");
        byte[] bytes = res.asByteArray();
        ExtentTestManager.logInfo("GET " + Constants.API_STATIC_UPLOADS_BASE_URI + "/" + uploadedPhotoFilename
                + " -> HTTP " + status + ", Content-Type=" + contentType + ", bytes=" + bytes.length);

        Assert.assertEquals(status, 200,
                "[KNOWN DEFECT - BROKEN STATIC UPLOADS] File gambar terunggah pada "
                        + Constants.API_STATIC_UPLOADS_BASE_URI + "/" + uploadedPhotoFilename
                        + " wajib dapat diakses dengan HTTP 200 OK (akar masalah broken image UI SIT_ADM_UI_028/037/075/202), aktual: HTTP " + status);
        Assert.assertTrue(contentType != null && contentType.toLowerCase().contains("image"),
                "Header Content-Type harus bertipe image/*, aktual: " + contentType);
        Assert.assertTrue(bytes.length > 0, "Ukuran bytes file gambar harus > 0");
    }

    @Test(priority = 54,
          description = "SIT_ADM_API_084 - Tolak upload file gambar melebihi batas 2 MB di sisi server (POST /upload)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_084_RejectOversizedPhotoUpload() {
        File oversized = getTestDataFile("oversized_photo.jpg");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", oversized, "image/jpeg")
        .when()
                .post(Constants.API_UPLOAD_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /upload (oversized_photo.jpg > 5 MB) -> HTTP " + status + " | " + safePreview(res));

        Assert.assertTrue(status == 400 || status == 413 || status == 422,
                "[VALIDATION] Upload gambar > 2 MB seharusnya ditolak di sisi server (400/413/422), aktual: HTTP "
                        + status + " | " + safePreview(res));
    }

    @Test(priority = 55,
          description = "SIT_ADM_API_085 - Tolak upload file non-gambar / berpotensi berbahaya (.html / .pdf) ke endpoint foto",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_085_RejectNonImagePdfUpload() {
        File pdfFile = getTestDataFile("invalid_upload.pdf");
        Response res = multipartSpec(adminToken)
                .multiPart("uri", pdfFile, "application/pdf")
        .when()
                .post(Constants.API_UPLOAD_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /upload (invalid_upload.pdf) -> HTTP " + status + " | " + safePreview(res));

        Assert.assertTrue(status >= 400,
                "Upload file PDF ke endpoint foto wajib ditolak server (>= 400), aktual: HTTP " + status);
    }

    @Test(priority = 56,
          description = "SIT_ADM_API_086 - Coba upload file ke POST /upload tanpa Authorization Bearer Token (Unauthenticated Upload)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_086_RejectUnauthenticatedPhotoUpload() {
        File photoFile = getTestDataFile("valid_photo.jpg");
        Response res = multipartSpec(null)
                .multiPart("uri", photoFile, "image/jpeg")
        .when()
                .post(Constants.API_UPLOAD_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /upload (no Authorization header) -> HTTP " + status + " | " + safePreview(res));

        Assert.assertEquals(status, 401,
                "[SECURITY] POST /upload tanpa Authorization Bearer Token wajib ditolak dengan HTTP 401 Unauthorized, aktual: HTTP "
                        + status + " | " + safePreview(res));
    }

    // =========================================================================
    // BLOCK 7: SYNC, SUMMARY, CONTRACT & RBAC/IDOR (SIT_ADM_API_087 - SIT_ADM_API_095)
    // =========================================================================

    @Test(priority = 57,
          description = "SIT_ADM_API_087 - Admin menjalankan Sync Approval data karyawan melalui POST /sync-approval",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_087_SyncApprovalValidUserByAdmin() {
        Map<String, Object> body = Map.of("user_id", employeeUserId);
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .body(body)
        .when()
                .post(Constants.API_SYNC_APPROVAL_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /sync-approval (valid user_id) -> HTTP " + status + " | " + safePreview(res));

        Assert.assertTrue(status == 200 || status == 201,
                "Sync Approval dengan user_id valid oleh Admin harus 200/201 OK, aktual: " + status + " | " + safePreview(res));
    }

    @Test(priority = 58,
          description = "SIT_ADM_API_088 - Sync Approval dengan user_id tidak valid / tidak ditemukan",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_088_SyncApprovalInvalidUserId() {
        Map<String, Object> body = Map.of("user_id", "00000000-0000-0000-0000-000000000000");
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .body(body)
        .when()
                .post(Constants.API_SYNC_APPROVAL_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /sync-approval (non-existent UUID) -> HTTP " + status + " | " + safePreview(res));

        Assert.assertTrue(status == 400 || status == 404 || status == 422,
                "Sync Approval dengan user_id tidak terdaftar seharusnya ditolak 400/404, aktual: HTTP "
                        + status + " | " + safePreview(res));
    }

    @Test(priority = 59,
          description = "SIT_ADM_API_089 - Karyawan biasa mencoba memicu POST /sync-approval (RBAC Security)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_089_SyncApprovalRbacRegularEmployeeRejected() {
        Map<String, Object> body = Map.of("user_id", employeeUserId);
        Response res = given()
                .spec(getAuthenticatedSpec(employeeToken))
                .body(body)
        .when()
                .post(Constants.API_SYNC_APPROVAL_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("POST /sync-approval (EmployeeToken RBAC) -> HTTP " + status + " | " + safePreview(res));

        Assert.assertTrue(status == 401 || status == 403,
                "[SECURITY RBAC] Karyawan biasa seharusnya ditolak (401/403) pada POST /sync-approval, aktual: HTTP "
                        + status + " | " + safePreview(res));
    }

    @Test(priority = 60,
          description = "SIT_ADM_API_090 - Admin mengambil ringkasan statistik pada Dashboard melalui GET /dashboard",
          groups = {"smoke", "regression", "positive"})
    public void testSIT_ADM_API_090_GetAdminDashboardSummary() {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
        .when()
                .get(Constants.API_DASHBOARD_ENDPOINT);
        int status = res.getStatusCode();
        ExtentTestManager.logInfo("GET /dashboard -> HTTP " + status + " | " + safePreview(res));

        Assert.assertEquals(status, 200,
                "GET /dashboard sebagai Admin harus mengembalikan HTTP 200 OK, aktual: " + status);
        Assert.assertTrue(res.getHeader("Content-Type").contains("application/json"),
                "Response GET /dashboard harus berformat JSON");
    }

    @Test(priority = 61,
          description = "SIT_ADM_API_091 - Admin mengambil daftar alokasi project-leave / sisa cuti karyawan",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_091_GetProjectLeaveAllocationByAdmin() {
        Response res = EmployeeApiHelper.getProjectLeaveSummaryByUserId(adminToken, employeeUserId);
        int status = res.getStatusCode();
        String body = safePreview(res);
        ExtentTestManager.logInfo("GET /project-leave/" + employeeUserId + " -> HTTP " + status + " | " + body);

        Assert.assertEquals(status, 200, "GET /project-leave/{userId} sebagai Admin harus 200 OK");
        Assert.assertTrue(body.contains("total_cuti") || body.contains("sisa_cuti"),
                "Response JSON wajib memuat atribut kuota cuti (total_cuti / sisa_cuti), aktual: " + body);
    }

    @Test(priority = 62,
          description = "SIT_ADM_API_092 - [Known Defect] Verifikasi integritas relasi job_departement pada data Client Upliner (API contract)",
          groups = {"regression", "positive"})
    public void testSIT_ADM_API_092_VerifyClientUplinerJobDepartementIntegrityKnownDefect() throws Exception {
        Response res = given()
                .spec(getAuthenticatedSpec(adminToken))
                .queryParam("role_id", "client")
                .queryParam("$limit", 50)
        .when()
                .get(Constants.API_USERS_ENDPOINT);
        Assert.assertEquals(res.getStatusCode(), 200, "GET /users?role_id=client harus 200 OK");

        JsonNode root = OBJECT_MAPPER.readTree(res.getBody().asString());
        JsonNode data = root.path("data");
        int totalChecked = 0;
        int nullDeptCount = 0;
        String sampleBrokenEmail = null;

        if (data.isArray()) {
            for (JsonNode item : data) {
                totalChecked++;
                JsonNode dept = item.get("job_departement");
                JsonNode deptId = item.get("job_departement_id");
                if ((dept == null || dept.isNull()) && (deptId == null || deptId.isNull())) {
                    nullDeptCount++;
                    if (sampleBrokenEmail == null) {
                        sampleBrokenEmail = item.path("email").asText();
                    }
                }
            }
        }
        ExtentTestManager.logInfo("Checked client upliners=" + totalChecked
                + ", null job_departement count=" + nullDeptCount + ", sample=" + sampleBrokenEmail);

        Assert.assertEquals(nullDeptCount, 0,
                "[KNOWN DEFECT] Seluruh akun Client Upliner wajib memiliki relasi job_departement non-null agar dropdown UI Atasan tidak mengalami TypeError crash (ditemukan "
                        + nullDeptCount + "/" + totalChecked + " record null, contoh: " + sampleBrokenEmail + ")");
    }

    @Test(priority = 63,
          description = "SIT_ADM_API_093 - Karyawan biasa mencoba mendaftarkan user baru via POST /users (Privilege Escalation)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_093_EmployeePrivilegeEscalationCreateUserRejected() {
        long ts = System.currentTimeMillis();
        String fakeEmail = "escalation.fake." + ts + "@hadir.com";
        String leakedId = null;

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("nik", EmployeeApiHelper.findAvailableNikFromPool());
            body.put("fullname", "Escalation Test " + ts);
            body.put("email", fakeEmail);
            body.put("password", "FakePass#" + ts);
            body.put("role_id", "admin");
            body.put("is_mobile", false);

            Response res = given()
                    .spec(getAuthenticatedSpec(employeeToken))
                    .body(body)
            .when()
                    .post(Constants.API_USERS_ENDPOINT);
            int status = res.getStatusCode();
            if (status == 200 || status == 201) {
                leakedId = firstNonBlank(res.jsonPath().getString("id"), findUserIdByEmailFast(fakeEmail));
            }
            ExtentTestManager.logInfo("POST /users (EmployeeToken role_id=admin) -> HTTP " + status + " | " + safePreview(res));

            Assert.assertTrue(status == 401 || status == 403,
                    "[SECURITY RBAC] Karyawan biasa seharusnya ditolak (401/403) saat membuat akun baru ber-role admin via POST /users, aktual: HTTP "
                            + status + " | " + safePreview(res));
        } finally {
            if (leakedId != null && !leakedId.isBlank()) {
                deleteUserQuietlyWithAdminToken(leakedId);
            }
        }
    }

    @Test(priority = 64,
          description = "SIT_ADM_API_094 - Karyawan biasa mencoba menonaktifkan akun atau menaikkan role milik user lain via PATCH /users/{id}",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_094_EmployeeIdorPatchOtherUserRejected() {
        String targetUserId = createDisposableUserViaAdmin("rbac.patch.target");
        try {
            Map<String, Object> body = Map.of(
                    "is_active", false,
                    "role_id", "admin"
            );
            Response res = given()
                    .spec(getAuthenticatedSpec(employeeToken))
                    .body(body)
            .when()
                    .patch(Constants.API_USERS_ENDPOINT + "/" + targetUserId);
            int status = res.getStatusCode();
            ExtentTestManager.logInfo("PATCH /users/" + targetUserId + " (EmployeeToken IDOR) -> HTTP " + status);

            Assert.assertTrue(status == 401 || status == 403,
                    "[SECURITY IDOR/RBAC] Karyawan biasa wajib ditolak (401/403) saat memodifikasi is_active/role_id user lain via PATCH /users/{id}, aktual: HTTP "
                            + status + " | " + safePreview(res));
        } finally {
            deleteUserQuietlyWithAdminToken(targetUserId);
            disposableUserIds.remove(targetUserId);
        }
    }

    @Test(priority = 65,
          description = "SIT_ADM_API_095 - Karyawan biasa mencoba menghapus user lain melalui DELETE /users/{id} (RBAC + IDOR)",
          groups = {"regression", "negative"})
    public void testSIT_ADM_API_095_EmployeeIdorDeleteOtherUserRejected() {
        String targetUserId = createDisposableUserViaAdmin("rbac.delete.target");
        try {
            Response res = given()
                    .spec(getAuthenticatedSpec(employeeToken))
            .when()
                    .delete(Constants.API_USERS_ENDPOINT + "/" + targetUserId);
            int status = res.getStatusCode();
            ExtentTestManager.logInfo("DELETE /users/" + targetUserId + " (EmployeeToken IDOR) -> HTTP " + status);

            Assert.assertTrue(status == 401 || status == 403,
                    "[SECURITY IDOR/RBAC] Karyawan biasa wajib ditolak (401/403) saat menghapus user lain via DELETE /users/{id}, aktual: HTTP "
                            + status + " | " + safePreview(res));
        } finally {
            deleteUserQuietlyWithAdminToken(targetUserId);
            disposableUserIds.remove(targetUserId);
        }
    }
}
