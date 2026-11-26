# 📋 Implementation Plan: Enterprise Admin Automation Framework (`magang.dikahadir.com`)

## Goal Description
Membangun **Enterprise Web UI & API Test Automation Framework** khusus untuk **portal Admin HRD** pada platform **[magang.dikahadir.com](https://magang.dikahadir.com)** dengan mengacu pada struktur proyek standar industri di [reference.md](file:///c:/Users/Asus/Documents/JuaraCoding/Latihan/proyek/day31/reference.md). Kerangka kerja ini menggunakan **Java 21**, **Selenium WebDriver `4.52.0`** (default `headless = true`), **REST Assured `6.3.0`**, **TestNG `7.12.0`**, dan **ExtentReports `5.1.2`** (Dark Theme + Base64 failure screenshot).

Rencana ini mencakup:
1. **Keamanan Kredensial Base64 (`SecurityUtil`)**: Menyimpan seluruh password ter-encode Base64 di `Constants.java` dan men-decode di memori runtime agar aman dipublikasikan ke GitHub.
2. **Audit & Refactoring Matriks SIT v4 (`701 TCs` + Kolom `Manual Check`)**: Pengelompokan modul kontigu (`0` blok terpecah), pemisahan perilaku defect dari `Expected Result` ke kolom `Actual Result / Defect Notes` (`Ready (Known Defect)`), penambahan kolom `Severity`, `Priority`, **`Test Suite` (`Smoke`, `Regression`, `Positif`, `Negatif`)**, serta kolom **`Manual Check`** (`☐ Belum` / `☑ Sudah` dropdown + `COUNTIF` tracker), pemecahan skenario *compound* menjadi atomik (sambil mempertahankan `SIT_ADM_UI_001–012` utuh), filter lengkap `Management-User`, modal `Atur Virtual ID` di `Management-Unit`, upload foto *preview* & verifikasi `Jumlah Cuti` (`Known Defect`) di `Pendaftaran User`, **11 skenario Integrasi Lintas Modul Admin (`311–321`) + 11 User (`130–140`)**, **4 User `Download Report` (`126–129`)**, serta cakupan **Pagination `Rows per page` (`5/10/25`) + `Next/Prev/First/Last`** di seluruh 30 menu tabel Admin + `InfiniteScroll` di `Daily Report` & 6 list User.
3. **Inspeksi & Ekspansi API Riil (REST Assured — `240 API TCs`)**: Menggunakan endpoint backend FeathersJS terverifikasi (`https://api-dev.dikahadir.com/hadir-api-magang`, `POST /authentication`) beserta ekspansi v4 menjadi **`240 API TCs`** (**130 Admin API** `001–130` + **110 User API** `001–110`), mencakup Bulk Import Excel, Binary Export/Reports, Multipart `/upload` security, 14 Master Data CRUD, Approval Workflows, serta audit keamanan RBAC/IDOR/Mass-Assignment/Missing-Auth (`/calendar` & `/export-leave`).
4. **Arsitektur Hybrid API + UI (`EmployeeApiHelper`)**: Menggunakan REST Assured di tahap `@BeforeMethod` / `@BeforeClass` untuk membuat data pengajuan sebagai Karyawan (`hadirsqa1@gmail.com`), lalu Selenium menguji validasi **Approve / Reject / Cancel** dan **Integrasi Lintas Modul E2E** di UI Admin secara deterministik. (Untuk `Laporan-Cuti`, tahap `@Before` menggunakan strategi query data cuti aktif yang tersedia / fallback UI helper).
5. **Re-alokasi Seimbang SIT v4 untuk 2 Tester (Deny & Aji)**: Membagi pengerjaan fitur secara seimbang antara **Tester Deny** dan **Tester Aji** pada SIT v4 di proyek Admin (**238 : 213**, atau **52.8% : 47.2%**) maupun proyek Karyawan (**127 : 123**, atau **50.8% : 49.2%**).
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
> **2. Pembagian Seimbang SIT v4 untuk 2 Tester (Total 701 TCs — 52.1% : 47.9%)**
> - **Proyek Admin (Total 451 TCs = 321 Web UI + 130 API)**:
>   - **Tester Deny**: **238 TCs** (**173 Web UI** + **65 API**)
>   - **Tester Aji**: **213 TCs** (**148 Web UI** + **65 API**)
> - **Proyek Karyawan (Total 250 TCs = 140 Web UI + 110 API — `WebKaryawanPrompt.md`)**:
>   - **Tester Deny**: **127 TCs** (**72 Web UI** + **55 API**)
>   - **Tester Aji**: **123 TCs** (**68 Web UI** + **55 API**)
> - **Total Gabungan 2 Proyek (701 TCs)**: **Tester Deny 365 TCs (52.1%)** | **Tester Aji 336 TCs (47.9%)**.

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

    subgraph Tester Deny Scope - 238 TCs
        D_UI1[AdminLoginPage & AdminLoginTest - 12 UI]
        D_UI2[AdminReportsGroupAPage & AdminReportsGroupATest - 54 UI]
        D_UI3[AdminImportPage & AdminImportTest - 16 UI]
        D_UI4[AdminMgmtGroupAPage & AdminMgmtGroupATest - 85 UI]
        D_E2E[AdminCrossModuleDenyE2ETest - 6 E2E UI]
        D_API[AdminGroupAApiTest - 65 API]
    end

    subgraph Tester Aji Scope - 213 TCs
        A_UI1[AdminDashboardPage & AdminDashboardTest - 13 UI]
        A_UI2[AdminReportsGroupBPage & AdminReportsGroupBTest - 74 UI]
        A_UI3[AdminMgmtGroupBPage & AdminMgmtGroupBTest - 56 UI]
        A_E2E[AdminCrossModuleAjiE2ETest - 5 E2E UI]
        A_API[AdminGroupBApiTest - 65 API]
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

### 2. Pembagian Tugas Adil 2 Tester (Proyek Admin — 451 TCs)

| Tester | Modul / Fitur yang Ditangani | Web UI (Selenium) | REST Assured API | Total |
| :--- | :--- | :---: | :---: | :---: |
| **👤 Tester Deny** | 1. **Autentikasi-Login** (`001–012`: 12 TCs, *preserved*)<br>2. **Laporan Group A** (`026–079` — 54 TCs)<br>3. **Import** (`154–169` — 16 TCs)<br>4. **Management Group A** (`170–254` — 85 TCs)<br>5. **Integrasi Lintas Modul E2E — Bagian Deny** (`311–315, 321`: 6 TCs)<br>6. **API Group A** (`SIT_ADM_API_001–030, 061–095`: Auth Security, Users CRUD, Activity/Corrections GET, Bulk Import Excel [11], Binary Export/Reports [10], Upload Security [5], Sync/Summary/Defect Contract/RBAC [9]) | **173 TCs** | **65 TCs** | **238 TCs** (52.8%) |
| **👤 Tester Aji** | 1. **Dashboard** (`013–025`: 13 TCs)<br>2. **Laporan Group B + Download** (`080–153` — 74 TCs)<br>3. **Management Group B** (`255–310` — 56 TCs)<br>4. **Integrasi Lintas Modul E2E — Bagian Aji** (`316–320`: 5 TCs)<br>5. **API Group B** (`SIT_ADM_API_031–060, 096–130`: Reports/SLA, Approvals Workflow & State Transitions, Master Data CRUD [14 resources] & Calendar Auth Security) | **148 TCs** | **65 TCs** | **213 TCs** (47.2%) |
| **TOTAL ADMIN** | **Seluruh Modul Kontigu Admin + Pagination 30 Menu Tabel + 11 E2E Lintas Modul + Security & File APIs** | **321 TCs** | **130 TCs** | **451 TCs** |

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
│   └── SIT_Scenario_Magang_Hadir.xlsx       # Single Source of Truth Matriks SIT v4 (701 TCs: 451 Admin + 250 Karyawan)
├── reports/
│   ├── ExtentReport.html
│   └── screenshots/
└── src/
    ├── main/java/com/juaracoding/magang/
    │   ├── drivers/DriverFactory.java
    │   ├── pages/
    │   │   ├── BasePage.java
    │   │   └── admin/
    │   │       ├── deny/                    # AdminLoginPage, AdminReportsGroupAPage, AdminLeavePage, AdminImportPage, AdminManagementGroupAPage, AdminCrossModuleE2EPage
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
        │       ├── deny/{AdminLoginTest,AdminReportsGroupATest,AdminImportTest,AdminManagementGroupATest,AdminCrossModuleDenyE2ETest}.java
        │       └── aji/{AdminDashboardTest,AdminReportsGroupBTest,AdminManagementGroupBTest}.java
        └── resources/
            ├── testng.xml
            ├── testng-deny.xml
            ├── testng-aji.xml
            └── testdata/
```

---

## Verification Plan
- **Fase 1**: Verifikasi jumlah baris dan pembagian tester pada `docs/SIT_Scenario_Magang_Hadir.xlsx` v4 (**701 TCs**: Deny Admin 238, Aji Admin 213, Deny User 127, Aji User 123 + kolom `Manual Check`) serta kelengkapan `plan.md`, `tasklist.md`, dan `WebKaryawanPrompt.md`.
- **Fase 2–7**: `mvn clean test-compile`, `mvn test -Dtest=AdminGroupAApiTest,AdminGroupBApiTest`, `mvn test -DsuiteXmlFile=src/test/resources/testng-deny.xml`, `mvn test -DsuiteXmlFile=src/test/resources/testng-aji.xml`, dan `mvn clean test`.

