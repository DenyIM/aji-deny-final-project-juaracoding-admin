# 📋 Implementation Plan: Enterprise Admin Automation Framework (`magang.dikahadir.com`)

## Goal Description
Membangun **Enterprise Web UI & API Test Automation Framework** khusus untuk **portal Admin HRD** pada platform **[magang.dikahadir.com](https://magang.dikahadir.com)** dengan mengacu pada struktur proyek standar industri di [reference.md](file:///c:/Users/Asus/Documents/JuaraCoding/Latihan/proyek/day31/reference.md). Kerangka kerja ini menggunakan **Java 21**, **Selenium WebDriver `4.52.0`** (default `headless = true`), **REST Assured `6.3.0`**, **TestNG `7.12.0`**, dan **ExtentReports `5.1.2`** (Dark Theme + Base64 failure screenshot).

Rencana ini mencakup:
1. **Keamanan Kredensial Base64 (`SecurityUtil`)**: Menyimpan seluruh password ter-encode Base64 di `Constants.java` dan men-decode di memori runtime agar aman dipublikasikan ke GitHub.
2. **Audit & Refactoring Matriks SIT v3 (5 Perbaikan Tester Aji + 10 Revisi Live Chunk)**: Pengelompokan modul kontigu (`0` blok terpecah), pemisahan perilaku defect dari `Expected Result` ke kolom `Actual Result / Defect Notes` (`Ready (Known Defect)`), penambahan kolom `Severity` & standarisasi `Priority`, pemecahan skenario *compound* menjadi atomik (sambil mempertahankan `SIT_ADM_UI_001–012` utuh), penambahan filter lengkap `Management-User` (`Job Level`, `Tipe Karyawan`, `Posisi Kerja`, `Lokasi Kerja`, `Status`, Search `Nama`/`NIK`), penghapusan asumsi keliru `Shift`/`Atur Virtual ID` di `Management-User` dan penambahan modal `Atur Virtual ID` di `Management-Unit`, upload foto *preview* & verifikasi `Jumlah Cuti` (`Known Defect`) di `Pendaftaran User`, **11 skenario Integrasi Lintas Modul Admin (`311–321`) + 11 User (`130–140`)**, **4 User `Download Report` (`126–129`)**, serta cakupan **Pagination `Rows per page` (`5/10/25`) + `Next/Prev/First/Last`** di seluruh 30 menu tabel Admin + `InfiniteScroll` di `Daily Report` & 6 list User.
3. **Inspeksi & Integrasi API Riil (REST Assured)**: Menggunakan endpoint backend FeathersJS terverifikasi (`https://api-dev.dikahadir.com/hadir-api-magang`, `POST /authentication`) beserta pembaruan seluruh **115 API TCs** (`/auth-management`, `/upload`, `/share-report`, `/users/{id}`, dll.).
4. **Arsitektur Hybrid API + UI (`EmployeeApiHelper`)**: Menggunakan REST Assured di tahap `@BeforeMethod` / `@BeforeClass` untuk membuat data pengajuan sebagai Karyawan (`hadirsqa1@gmail.com`), lalu Selenium menguji validasi **Approve / Reject / Cancel** dan **Integrasi Lintas Modul E2E** di UI Admin secara deterministik. (Untuk `Laporan-Cuti`, tahap `@Before` menggunakan strategi query data cuti aktif yang tersedia / fallback UI helper).
5. **Re-alokasi Seimbang SIT v3 untuk 2 Tester (Deny & Aji)**: Membagi pengerjaan fitur secara seimbang antara **Tester Deny** dan **Tester Aji** pada SIT v3 di proyek Admin (**203 : 178**, atau **53.3% : 46.7%**) maupun proyek Karyawan (**99 : 96**, atau **50.8% : 49.2%**).
6. **Tasklist Multi-Fase (`tasklist.md`) & Blueprint Proyek Karyawan (`WebKaryawanPrompt.md`)**: Kedua dokumen disiapkan di **Fase 1** agar selaras sejak awal antara repositori Admin dan repositori Karyawan.

---

## Ringkasan Keputusan & Temuan Utama

> [!IMPORTANT]
> **1. Penemuan API Riil untuk REST Assured & Hybrid API+UI**
> - Backend API asli aplikasi berjalan di **`https://api-dev.dikahadir.com/hadir-api-magang`** (framework **FeathersJS**), bukan `https://magang.dikahadir.com/api/...` (proxy Next.js yang saat dipanggil langsung mengembalikan `400 unable to verify the first certificate`).
> - Endpoint login asli adalah **`POST /authentication`** dengan payload `{email, password, strategy: "local"}` (mengembalikan **HTTP 201** dan `accessToken` JWT).
> - Endpoint resource asli antara lain: `/users`, `/activity`, `/activity-correction`, `/overtime`, `/off-permit`, `/late-permit`, `/early-leave`, `/business-trips`, `/project-leave`, `/job-departements`, `/division`, `/job-position`, `/location-point`, `/shifting`, `/schedule`, `/calendar`, `/unit-leave`, `/monthly-schedule`, `/job-level`, `/overtime-reason`, `/holidays`, `/meetings`, `/auth-management`, `/upload`, `/share-report`.
> - `BaseApiTest` wajib mengaktifkan **`RestAssured.useRelaxedHTTPSValidation()`** agar sertifikat TLS `api-dev.dikahadir.com` dapat diakses dengan lancar.

> [!IMPORTANT]
> **2. Pembagian Seimbang SIT v3 untuk 2 Tester (Total 576 TCs — 52.4% : 47.6%)**
> - **Proyek Admin (Total 381 TCs = 321 Web UI + 60 API)**:
>   - **Tester Deny**: **203 TCs** (**173 Web UI** + **30 API**)
>   - **Tester Aji**: **178 TCs** (**148 Web UI** + **30 API**)
> - **Proyek Karyawan (Total 195 TCs = 140 Web UI + 55 API — `WebKaryawanPrompt.md`)**:
>   - **Tester Deny**: **99 TCs** (**72 Web UI** + **27 API**)
>   - **Tester Aji**: **96 TCs** (**68 Web UI** + **28 API**)
> - **Total Gabungan 2 Proyek (576 TCs)**: **Tester Deny 302 TCs (52.4%)** | **Tester Aji 274 TCs (47.6%)**.

---

## Arsitektur Sistem

```mermaid
flowchart TD
    subgraph Core Foundation
        POM_XML[pom.xml - Java 21, Selenium 4.52.0, REST Assured 6.3.0, TestNG 7.12.0]
        SEC[SecurityUtil - Base64 Decode]
        CONST[Constants - URLs, Endpoints, Base64 Passwords]
        DF[DriverFactory - ThreadLocal, Chrome headless=true default]
        BAPI[BaseApiTest - useRelaxedHTTPSValidation & JWT]
        HYB[EmployeeApiHelper - Hybrid @Before Seeding]
    end

    subgraph Tester Deny Scope - 203 TCs
        D_UI1[AdminLoginPage & AdminLoginTest - 12 UI]
        D_UI2[AdminReportsGroupAPage & AdminReportsGroupATest - 54 UI]
        D_UI3[AdminImportPage & AdminImportTest - 16 UI]
        D_UI4[AdminMgmtGroupAPage & AdminMgmtGroupATest - 85 UI]
        D_E2E[AdminCrossModuleDenyE2ETest - 6 E2E UI]
        D_API[AdminGroupAApiTest - 30 API]
    end

    subgraph Tester Aji Scope - 178 TCs
        A_UI1[AdminDashboardPage & AdminDashboardTest - 13 UI]
        A_UI2[AdminReportsGroupBPage & AdminReportsGroupBTest - 74 UI]
        A_UI3[AdminMgmtGroupBPage & AdminMgmtGroupBTest - 56 UI]
        A_E2E[AdminCrossModuleAjiE2ETest - 5 E2E UI]
        A_API[AdminGroupBApiTest - 30 API]
    end

    subgraph Deliverables & Reporting
        EXT[reports/ExtentReport.html - Dark Theme + Base64 Screenshots]
        SIT[docs/SIT_Scenario_Magang_Hadir.xlsx]
        TASK[tasklist.md - 7 Phases]
        PROMPT[WebKaryawanPrompt.md - Fase 1 Deliverable]
    end

    HYB -->|@Before Seed Koreksi/IzinOff/Dinas/Lembur| A_UI1 & A_UI2 & A_E2E
    HYB -->|@Before Query/Seed Cuti & User| D_UI2 & D_E2E
    D_UI1 & D_UI2 & D_UI3 & D_UI4 & D_E2E & D_API --> EXT
    A_UI1 & A_UI2 & A_UI3 & A_E2E & A_API --> EXT
```

---

### 1. API Discovery Tools & Verified Backend Reference

| Kebutuhan | Tool yang Dipakai / Direkomendasikan | Penjelasan Singkat |
| :--- | :--- | :--- |
| **Inspeksi Endpoint dari Frontend** | **Next.js Chunk Analyzer (`Node.js fetch`)** & **Chrome DevTools → Network Tab (Fetch/XHR)** | Digunakan untuk membaca rute halaman (`_buildManifest.js`), *axios `baseURL`*, payload request, dan format response langsung dari kode frontend aplikasi. |
| **Verifikasi & Eksplorasi Manual API** | **Postman** atau **Bruno** (open-source, Git-friendly) + fitur *Copy as cURL* dari Chrome DevTools | Direkomendasikan untuk mencoba request `POST /authentication`, menyalin Bearer Token, dan menyimpan collection endpoint untuk referensi tim QA. |
| **Automated API & Hybrid Testing** | **REST Assured `6.3.0`** + `json-path` + `json-schema-validator` | Dijalankan otomatis melalui Maven/TestNG, terintegrasi dengan `ExtentReports` dan berfungsi sebagai `@Before` *test-data seeder* untuk pengujian Hybrid API + UI. |

---

### 2. Pembagian Tugas Adil 2 Tester (Proyek Admin — 381 TCs)

| Tester | Modul / Fitur yang Ditangani | Web UI (Selenium) | REST Assured API | Total |
| :--- | :--- | :---: | :---: | :---: |
| **👤 Tester Deny** | 1. **Autentikasi-Login** (`001–012`: 12 TCs, *preserved*)<br>2. **Laporan Group A** (7 sub-menu paginated: Semua `026–034`, Kehadiran `035–041`, Izin `042–048`, Izin Terlambat `049–055`, Izin Pulang Cepat `056–062`, Cuti *[Hybrid]* `063–071`, Sakit `072–079` — 54 TCs)<br>3. **Import** (4 sub-menu: `154–169` — 16 TCs)<br>4. **Management Group A** (9 sub-menu: User `170–191` [+filter lengkap & pagination], Pendaftaran User `192–200` [+foto preview & Jumlah Cuti], Pendaftaran Sendiri `201–207` [+pagination], Divisi `208–215`, Unit `216–224` [+Atur Virtual ID], Unit Setting `225–229`, Posisi `230–237`, Client Upliner `238–246`, Absen Point `247–254` — 85 TCs)<br>5. **Integrasi Lintas Modul E2E — Bagian Deny** (`311–316`: 6 TCs, termasuk E2E Jumlah Cuti)<br>6. **API Group A** (`SIT_ADM_API_001–030`: Auth Security 10, Users CRUD 12, Attendance/Activity/Corrections GET 8) | **173 TCs** | **30 TCs** | **203 TCs** (53.3%) |
| **👤 Tester Aji** | 1. **Dashboard** (`Dashboard-Dashboard` `013–018` & `Dashboard-Pending` *[Hybrid]* `019–025`: 13 TCs)<br>2. **Laporan Group B + Download** (11 sub-menu: Lembur *[Hybrid]* `080–088`, Validasi Lembur `089–093`, Shifting `094–100`, Meeting `101–115`, Perjalanan Dinas *[Hybrid]* `116–124`, Koreksi *[Hybrid]* `125–133`, Izin Off *[Hybrid]* `134–141`, Download Absen `142–143`, Download Absen V2 `144–146`, Sisa Cuti `147–148`, Daily Report `149–153` — 74 TCs)<br>3. **Management Group B** (9 sub-menu: Jabatan `255–260`, Shifting `261–267`, Jadwal `268–274`, Kalender `275–280`, Aturan Cuti `281–286`, Day Off `287–288`, Monthly Schedule `289–296`, User Monitoring `297–302`, Overtime Reason `303–310` — 56 TCs)<br>4. **Integrasi Lintas Modul E2E — Bagian Aji** (`317–321`: 5 TCs)<br>5. **API Group B** (`SIT_ADM_API_031–060`: Reports/SLA 4, Approvals Workflow 10, Master Data CRUD 16) | **148 TCs** | **30 TCs** | **178 TCs** (46.7%) |
| **TOTAL ADMIN** | **Seluruh Modul Kontigu Admin + Pagination 30 Menu Tabel + 11 E2E Lintas Modul** | **321 TCs** | **60 TCs** | **381 TCs** |

---

### 3. Struktur Direktori Target Proyek Admin (`final-project-admin/`)

```text
final-project-admin/
├── pom.xml                                  # Java 21, Selenium 4.52.0, REST Assured 6.3.0, TestNG 7.12.0, ExtentReports 5.1.2
├── .gitignore
├── .gitattributes
├── .editorconfig
├── README.md
├── reference.md
├── plan.md
├── tasklist.md
├── WebKaryawanPrompt.md                     # Deliverable Fase 1: Panduan & Prompt proyek Karyawan
├── Project SQA Batch 31.xlsx
├── docs/
│   └── SIT_Scenario_Magang_Hadir.xlsx       # Single Source of Truth Matriks SIT v3 (576 TCs: 381 Admin + 195 Karyawan)
├── reports/
│   ├── ExtentReport.html
│   └── screenshots/
└── src/
    ├── main/java/com/juaracoding/magang/
    │   ├── drivers/DriverFactory.java
    │   ├── pages/
    │   │   ├── BasePage.java
    │   │   └── admin/
    │   │       ├── deny/                    # AdminLoginPage, AdminReportsGroupAPage, AdminLeavePage, AdminImportPage, AdminManagementGroupAPage
    │   │       └── aji/                     # AdminDashboardPage, AdminReportsGroupBPage, AdminApprovalPage, AdminDownloadPage, AdminManagementGroupBPage
    │   ├── reports/{ExtentManager,ExtentTestManager}.java
    │   └── utils/{Constants,SecurityUtil,ScreenshotUtil}.java
    └── test/
        ├── java/com/juaracoding/magang/
        │   ├── BaseTest.java
        │   ├── listeners/TestListener.java
        │   ├── api/
        │   │   ├── BaseApiTest.java
        │   │   ├── helper/EmployeeApiHelper.java
        │   │   └── admin/{AdminGroupAApiTest,AdminGroupBApiTest}.java
        │   └── tests/admin/
        │       ├── deny/{AdminLoginTest,AdminReportsGroupATest,AdminImportTest,AdminManagementGroupATest}.java
        │       └── aji/{AdminDashboardTest,AdminReportsGroupBTest,AdminManagementGroupBTest}.java
        └── resources/
            ├── testng.xml
            ├── testng-deny.xml
            ├── testng-aji.xml
            └── testdata/
```

---

## Verification Plan
- **Fase 1**: Verifikasi jumlah baris dan pembagian tester pada `docs/SIT_Scenario_Magang_Hadir.xlsx` v3 (**576 TCs**: Deny Admin 203, Aji Admin 178, Deny User 99, Aji User 96) serta kelengkapan `plan.md`, `tasklist.md`, dan `WebKaryawanPrompt.md`.
- **Fase 2–7**: `mvn clean test-compile`, `mvn test -Dtest=AdminGroupAApiTest,AdminGroupBApiTest`, `mvn test -DsuiteXmlFile=src/test/resources/testng-deny.xml`, `mvn test -DsuiteXmlFile=src/test/resources/testng-aji.xml`, dan `mvn clean test`.

