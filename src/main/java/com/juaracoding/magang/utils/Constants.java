package com.juaracoding.magang.utils;

/**
 * Sentralisasi konfigurasi URL, endpoint backend FeathersJS terverifikasi,
 * kredensial ter-encode Base64, timeout, dan path laporan pengujian Admin.
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // ==========================================
    // WEB UI URLS
    // ==========================================
    public static final String BASE_URL = "https://magang.dikahadir.com";
    public static final String ADMIN_LOGIN_URL = BASE_URL + "/authentication/login";
    public static final String ADMIN_DASHBOARD_URL = BASE_URL + "/dashboards/dashboard";
    public static final String ADMIN_PENDING_URL = BASE_URL + "/dashboards/pending";
    public static final String ADMIN_SISA_CUTI_URL = BASE_URL + "/laporan/sisa-cuti";
    public static final String USER_LOGIN_URL = BASE_URL + "/absen/login";
    public static final String USER_LEAVE_URL = BASE_URL + "/apps/absent/leave";
    public static final String USER_LEAVE_CLIENT_URL = BASE_URL + "/apps/absent/leave-client";
    public static final String USER_SICK_URL = BASE_URL + "/apps/absent/sick";

    // ==========================================
    // REAL BACKEND API URI & ENDPOINTS
    // ==========================================
    public static final String API_BASE_URI = "https://api-dev.dikahadir.com/hadir-api-magang";
    public static final String API_STATIC_UPLOADS_BASE_URI = "https://api-dev.dikahadir.com/uploads";
    public static final String API_AUTH_ENDPOINT = "/authentication";
    public static final String API_UPLOAD_ENDPOINT = "/upload";
    public static final String API_DASHBOARD_ENDPOINT = "/dashboard";
    public static final String API_SYNC_APPROVAL_ENDPOINT = "/sync-approval";
    public static final String API_EXPORT_USER_ENDPOINT = "/export-user";
    public static final String API_EXPORT_LEAVE_ENDPOINT = "/export-leave";
    public static final String API_EXPORT_ATTENDANCE_SUMMARY_ENDPOINT = "/export-attendance-summary";
    public static final String API_GENERATE_REPORT_ENDPOINT = "/generate-report";
    public static final String API_GENERATE_REPORT_TYPE_ENDPOINT = "/generate-report-type";
    public static final String API_DAILY_REPORT_ENDPOINT = "/daily-report";
    public static final String API_IMPORT_ABSEN_ENDPOINT = "/import-absen-users";
    public static final String API_IMPORT_STATUS_AKTIF_ENDPOINT = "/upload-excel-status-users";
    public static final String API_IMPORT_STRUKTUR_USER_ENDPOINT = "/import-update-users";
    public static final String API_IMPORT_LEAVE_ENDPOINT = "/import-leave";
    public static final String API_USERS_ENDPOINT = "/users";
    public static final String API_ACTIVITY_ENDPOINT = "/activity";
    public static final String API_CORRECTIONS_ENDPOINT = "/activity-correction";
    public static final String API_OVERTIME_ENDPOINT = "/overtime";
    public static final String API_OFF_PERMIT_ENDPOINT = "/off-permit";
    public static final String API_LATE_PERMIT_ENDPOINT = "/late-permit";
    public static final String API_EARLY_LEAVE_ENDPOINT = "/early-leave";
    public static final String API_BUSINESS_TRIPS_ENDPOINT = "/business-trips";
    public static final String API_PROJECT_LEAVE_ENDPOINT = "/project-leave";
    public static final String API_DEPARTEMENTS_ENDPOINT = "/job-departements";
    public static final String API_DIVISIONS_ENDPOINT = "/division";
    public static final String API_POSITIONS_ENDPOINT = "/job-position";
    public static final String API_LOCATIONS_ENDPOINT = "/location-point";
    public static final String API_SHIFTING_ENDPOINT = "/shifting";
    public static final String API_SCHEDULE_ENDPOINT = "/schedule";
    public static final String API_CALENDAR_ENDPOINT = "/calendar";
    public static final String API_UNIT_LEAVE_ENDPOINT = "/unit-leave";
    public static final String API_MONTHLY_SCHEDULE_ENDPOINT = "/monthly-schedule";
    public static final String API_JOB_LEVEL_ENDPOINT = "/job-level";
    public static final String API_OT_REASON_ENDPOINT = "/overtime-reason";
    public static final String API_HOLIDAYS_ENDPOINT = "/holidays";
    public static final String API_MEETINGS_ENDPOINT = "/meetings";

    // ==========================================
    // CREDENTIALS (BASE64 ENCODED - NO PLAINTEXT)
    // ==========================================
    public static final String ADMIN_EMAIL = "admin@hadir.com";
    public static final String ADMIN_PASSWORD_ENCODED = "TWFnYW5nU1FBX0pDQDEyMw==";

    // Kredensial Employee khusus untuk Hybrid @Before API Seeding
    public static final String HYBRID_EMPLOYEE_EMAIL = "hadirsqa1@gmail.com";
    public static final String HYBRID_EMPLOYEE_NAME = "Hadir SQA Testing 1";
    public static final String HYBRID_EMPLOYEE_NIK = "D6201031";
    public static final String HYBRID_EMPLOYEE_PASSWORD_ENCODED = "U1FBQEhhZGlyMTIzNDU=";

    // Pool NIK valid dari dokumen referensi untuk pengujian registrasi / pencarian user
    public static final String[] VALID_NIK_POOL = {
        "F1240001", "D6240005", "D7240031", "D8240010", "D7240029"
    };

    // ==========================================
    // TIMEOUTS (SECONDS)
    // ==========================================
    public static final int EXPLICIT_WAIT_TIMEOUT = 15;
    public static final int PAGE_LOAD_TIMEOUT = 30;
    public static final int SCRIPT_TIMEOUT = 30;

    // ==========================================
    // REPORTS & TEST DATA PATHS
    // ==========================================
    public static final String EXTENT_REPORT_DIR = "reports/";
    public static final String EXTENT_REPORT_FILE = EXTENT_REPORT_DIR + "ExtentReport.html";
    public static final String SCREENSHOTS_DIR = "reports/screenshots/";
    public static final String SIT_EXCEL_PATH = "docs/SIT_Scenario_Magang_Hadir.xlsx";
    public static final String TESTDATA_DIR = "src/test/resources/testdata/";
}

