# 📋 Implementation Plan: Enterprise Admin Automation Framework (`magang.dikahadir.com`)

## Goal Description
Membangun **Enterprise Web UI & API Test Automation Framework** khusus untuk **portal Admin HRD** pada platform **[magang.dikahadir.com](https://magang.dikahadir.com)** dengan mengacu pada struktur proyek standar industri di [reference.md](file:///c:/Users/Asus/Documents/JuaraCoding/Latihan/proyek/day31/reference.md). Kerangka kerja ini menggunakan **Java 21**, **Selenium WebDriver `4.52.0`** (default `headless = true`), **REST Assured `6.3.0`**, **TestNG `7.12.0`**, dan **ExtentReports `5.1.2`** (Dark Theme + Base64 failure screenshot).

Rencana ini mencakup:
1. **Keamanan Kredensial Base64 (`SecurityUtil`)**: Menyimpan seluruh password ter-encode Base64 di `Constants.java` dan men-decode di memori runtime agar aman dipublikasikan ke GitHub.
2. **Eksplorasi Skenario Tambahan di Menu Management**: Menambahkan **30 skenario baru (`SIT_ADM_UI_166` s/d `SIT_ADM_UI_195`)** untuk aksi dan sub-halaman Management yang belum tercakup (Export/Delete/Sync User, Bulk Checklist Self-Registration, Upliner BCA & Edit/Delete Upliner, Logo/Footer/Type-Shift Unit, Tambah/Hapus Event Kalender, Edit/Import/Hapus Monthly Schedule, Detail Overtime Reason, Edit User Monitoring, dsb.).
3. **Inspeksi & Integrasi API Riil (REST Assured)**: Menggunakan endpoint backend FeathersJS terverifikasi (`https://api-dev.dikahadir.com/hadir-api-magang`, `POST /authentication`) beserta dokumentasi tools inspeksi API yang digunakan.
4. **Arsitektur Hybrid API + UI (`EmployeeApiHelper`)**: Menggunakan REST Assured di tahap `@BeforeMethod` / `@BeforeClass` untuk membuat data pengajuan sebagai Karyawan (`hadirsqa1@gmail.com`), lalu Selenium menguji validasi **Approve / Reject / Cancel** di UI Admin secara deterministik. (Untuk `Laporan-Cuti`, tahap `@Before` menggunakan strategi query data cuti aktif yang tersedia / fallback UI helper).
5. **Re-alokasi Adil SIT 2 Tester (Deny & Aji) Tanpa Mengubah Skenario**: Membagi pengerjaan fitur secara seimbang antara **Tester Deny** dan **Tester Aji** di proyek Admin ini (serta pola pembagian yang sama untuk proyek Karyawan), tanpa menghapus atau mengubah skenario existing.
6. **Tasklist Multi-Fase (`tasklist.md`) & Blueprint Proyek Karyawan (`WebKaryawanPrompt.md`)**: Kedua dokumen disiapkan di **Fase 1** agar selaras sejak awal antara repositori Admin dan repositori Karyawan.

---

## Ringkasan Keputusan & Temuan Utama

> [!IMPORTANT]
> **1. Penemuan API Riil untuk REST Assured & Hybrid API+UI**
> - Backend API asli aplikasi berjalan di **`https://api-dev.dikahadir.com/hadir-api-magang`** (framework **FeathersJS**), bukan `https://magang.dikahadir.com/api/...` (proxy Next.js yang saat dipanggil langsung mengembalikan `400 unable to verify the first certificate`).
> - Endpoint login asli adalah **`POST /authentication`** dengan payload `{email, password, strategy: "local"}` (mengembalikan **HTTP 201** dan `accessToken` JWT).
> - Endpoint resource asli antara lain: `/users`, `/activity`, `/activity-correction`, `/overtime`, `/off-permit`, `/late-permit`, `/early-leave`, `/business-trips`, `/project-leave`, `/job-departements`, `/division`, `/job-position`, `/location-point`, `/shifting`, `/schedule`, `/calendar`, `/unit-leave`, `/monthly-schedule`, `/job-level`, `/overtime-reason`, `/holidays`, `/meetings`.
> - `BaseApiTest` wajib mengaktifkan **`RestAssured.useRelaxedHTTPSValidation()`** agar sertifikat TLS `api-dev.dikahadir.com` dapat diakses dengan lancar.

> [!IMPORTANT]
> **2. Pembagian Adil SIT 2 Tester (Tanpa Mengubah Skenario Existing)**
> - **Proyek Admin (Total 255 TCs = 195 Web UI + 60 API)**:
>   - **Tester Deny**: **126 TCs** (**96 Web UI** + **30 API**)
>   - **Tester Aji**: **129 TCs** (**99 Web UI** + **30 API**)
> - **Proyek Karyawan (Total 175 TCs = 120 Web UI + 55 API — `WebKaryawanPrompt.md`)**:
>   - **Tester Deny**: **87 TCs** (**60 Web UI** + **27 API**)
>   - **Tester Aji**: **88 TCs** (**60 Web UI** + **28 API**)
> - **Total Gabungan 2 Proyek (430 TCs)**: **Tester Deny 213 TCs (49.5%)** | **Tester Aji 217 TCs (50.5%)**.

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

    subgraph Tester Deny Scope - 126 TCs
        D_UI1[AdminLoginPage & AdminLoginTest - 12 UI]
        D_UI2[AdminReportsGroupAPage & AdminReportsGroupATest - 36 UI]
        D_UI3[AdminImportPage & AdminImportTest - 12 UI]
        D_UI4[AdminMgmtGroupAPage & AdminMgmtGroupATest - 36 UI]
        D_API[AdminGroupAApiTest - 30 API]
    end

    subgraph Tester Aji Scope - 129 TCs
        A_UI1[AdminDashboardPage & AdminDashboardTest - 13 UI]
        A_UI2[AdminReportsGroupBPage & AdminReportsGroupBTest - 51 UI]
        A_UI3[AdminMgmtGroupBPage & AdminMgmtGroupBTest - 35 UI]
        A_API[AdminGroupBApiTest - 30 API]
    end

    subgraph Deliverables & Reporting
        EXT[reports/ExtentReport.html - Dark Theme + Base64 Screenshots]
        SIT[SIT_Scenario_Magang_Hadir.xlsx]
        TASK[tasklist.md - 7 Phases]
        PROMPT[WebKaryawanPrompt.md - Fase 1 Deliverable]
    end

    HYB -->|@Before Seed Koreksi/IzinOff/Dinas/Lembur| A_UI1 & A_UI2
    HYB -->|@Before Query/Seed Cuti| D_UI2
    D_UI1 & D_UI2 & D_UI3 & D_UI4 & D_API --> EXT
    A_UI1 & A_UI2 & A_UI3 & A_API --> EXT
```

---

### 1. API Discovery Tools & Verified Backend Reference

| Kebutuhan | Tool yang Dipakai / Direkomendasikan | Penjelasan Singkat |
| :--- | :--- | :--- |
| **Inspeksi Endpoint dari Frontend** | **Next.js Chunk Analyzer (`Node.js fetch`)** & **Chrome DevTools → Network Tab (Fetch/XHR)** | Digunakan untuk membaca rute halaman (`_buildManifest.js`), *axios `baseURL`*, payload request, dan format response langsung dari kode frontend aplikasi. |
| **Verifikasi & Eksplorasi Manual API** | **Postman** atau **Bruno** (open-source, Git-friendly) + fitur *Copy as cURL* dari Chrome DevTools | Direkomendasikan untuk mencoba request `POST /authentication`, menyalin Bearer Token, dan menyimpan collection endpoint untuk referensi tim QA. |
| **Automated API & Hybrid Testing** | **REST Assured `6.3.0`** + `json-path` + `json-schema-validator` | Dijalankan otomatis melalui Maven/TestNG, terintegrasi dengan `ExtentReports` dan berfungsi sebagai `@Before` *test-data seeder* untuk pengujian Hybrid API + UI. |

---

### 2. Pembagian Tugas Adil 2 Tester (Proyek Admin — 255 TCs)

| Tester | Modul / Fitur yang Ditangani | Web UI (Selenium) | REST Assured API | Total |
| :--- | :--- | :---: | :---: | :---: |
| **👤 Tester Deny** | 1. **Autentikasi-Login** (`001–012`: 12 TCs)<br>2. **Laporan Group A** (7 sub-menu: Semua, Kehadiran, Izin, Izin Terlambat, Izin Pulang Cepat, Cuti *[Hybrid]*, Sakit — `026–061`: 36 TCs)<br>3. **Import** (4 sub-menu: Import Absen, Status Aktif, Struktur User, Cuti — `113–124`: 12 TCs)<br>4. **Management Group A** (9 sub-menu: User, Pendaftaran User, Pendaftaran Sendiri, Divisi, Unit, Unit Setting, Posisi, Client Upliner, Absen Point — `125–144` & `166–181`: 36 TCs)<br>5. **API Group A** (`SIT_ADM_API_001–030`: Auth Security 10, Users CRUD 12, Attendance/Activity/Corrections GET 8) | **96 TCs** | **30 TCs** | **126 TCs** (~49.4%) |
| **👤 Tester Aji** | 1. **Dashboard** (`Dashboard-Dashboard` & `Dashboard-Pending` *[Hybrid]* — `013–025`: 13 TCs)<br>2. **Laporan Group B + Download** (11 sub-menu: Lembur *[Hybrid]*, Validasi Lembur, Shifting, Meeting, Perjalanan Dinas *[Hybrid]*, Koreksi *[Hybrid]*, Izin Off *[Hybrid]*, Download Absen, Download Absen V2, Sisa Cuti, Daily Report — `062–112`: 51 TCs)<br>3. **Management Group B** (9 sub-menu: Jabatan, Shifting, Jadwal, Kalender, Aturan Cuti, Day Off, Monthly Schedule, User Monitoring, Overtime Reason — `145–165` & `182–195`: 35 TCs)<br>4. **API Group B** (`SIT_ADM_API_031–060`: Reports/SLA 4, Approvals Workflow 10, Master Data CRUD 16) | **99 TCs** | **30 TCs** | **129 TCs** (~50.6%) |
| **TOTAL ADMIN** | **43 Menu/Sub-menu Admin (termasuk +30 skenario baru Management)** | **195 TCs** | **60 TCs** | **255 TCs** |

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
├── SIT_Scenario_Magang_Hadir.xlsx           # Matriks SIT terupdate (430 TCs: 255 Admin + 175 Karyawan)
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
- **Fase 1**: Verifikasi jumlah baris dan pembagian tester pada `SIT_Scenario_Magang_Hadir.xlsx` (**430 TCs**: Deny Admin 126, Aji Admin 129, Deny User 87, Aji User 88) serta kelengkapan `plan.md`, `tasklist.md`, dan `WebKaryawanPrompt.md`.
- **Fase 2–7**: `mvn clean test-compile`, `mvn test -Dtest=AdminGroupAApiTest,AdminGroupBApiTest`, `mvn test -DsuiteXmlFile=src/test/resources/testng-deny.xml`, `mvn test -DsuiteXmlFile=src/test/resources/testng-aji.xml`, dan `mvn clean test`.

