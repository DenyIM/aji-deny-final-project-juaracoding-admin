# 📌 Tasklist Pelaksanaan: Automation Testing Admin `magang.dikahadir.com` (255 Admin TCs + Blueprint Proyek Karyawan)

Roadmap pengujian otomatisasi berbasis **Java 21**, **Selenium WebDriver `4.52.0`** (default `headless = true`), **REST Assured `6.3.0`**, **TestNG `7.12.0`**, dan **ExtentReports `5.1.2`** dengan struktur standar industri mengacu pada `reference.md`.

Khusus repositori ini difokuskan pada **Portal Admin HRD** (**255 Test Cases**: **195 Web UI** + **60 REST Assured API**), sedangkan seluruh panduan, struktur, pembagian tester, endpoint API, dan *master prompt* untuk repositori **Web Karyawan** telah disiapkan pada Fase 1 melalui dokumen `WebKaryawanPrompt.md`.

---

## 🧭 Ikhtisar Status 7 Fase & Alokasi Beban Kerja

| Fase | Deskripsi Fase | Output Deliverables | Penanggung Jawab | Status |
| :---: | :--- | :--- | :---: | :---: |
| **Fase 1** | Discovery Menu & API Riil, Update Matriks SIT (2 Tester Adil + 30 Skenario Management Baru), & Pembuatan `WebKaryawanPrompt.md` | `plan.md`, `tasklist.md`, `SIT_Scenario_Magang_Hadir.xlsx` (430 TCs total), `WebKaryawanPrompt.md` | Shared Foundation | ✅ Selesai (Completed) |
| **Fase 2** | Maven Setup (`pom.xml` Selenium `4.52.0`) & Struktur Package Terisolasi (`deny` & `aji`) | `pom.xml`, `.gitignore`, `.editorconfig`, `src/main/...`, `src/test/...`, `testdata/` | Shared Foundation | ✅ Selesai (Completed) |
| **Fase 3** | Core Thread-Safe Engine, Keamanan Base64, & Hybrid API Seeding Helper | `SecurityUtil`, `Constants`, `DriverFactory` (`headless=true`), `BasePage`, `BaseTest`, `BaseApiTest`, `EmployeeApiHelper`, `ExtentManager`, `TestListener` | Shared Foundation | ✅ Selesai (Completed) |
| **Fase 4** | Page Object Model (POM) Admin Implementation | POM Deny (5 Classes) & POM Aji (5 Classes) | Deny & Aji | ⏳ Pending |
| **Fase 5** | Automated Tests Implementation (Web UI + REST Assured API + Hybrid API+UI) | `tests/admin/deny/*` (96 UI), `tests/admin/aji/*` (99 UI), `AdminGroupAApiTest` (30 API), `AdminGroupBApiTest` (30 API) | Deny (126 TCs) & Aji (129 TCs) | ⏳ Pending |
| **Fase 6** | TestNG Multi-Suite Runner (`testng`, `testng-deny`, `testng-aji`) & Verifikasi Headless | `testng.xml`, `testng-deny.xml`, `testng-aji.xml` | Deny & Aji | ⏳ Pending |
| **Fase 7** | Validasi ExtentReports Dashboard, Sinkronisasi Status SIT, README & Walkthrough | `reports/ExtentReport.html`, `README.md`, `walkthrough.md` | Shared Deliverable | ⏳ Pending |

---

## ⚖️ Matriks Pembagian Tugas Adil (Proyek Admin — Total 255 TCs)

| Aspek Kolaborasi | 👤 Tester Deny (126 TCs — 49.4%) | 👤 Tester Aji (129 TCs — 50.6%) |
| :--- | :--- | :--- |
| **Fitur Web UI (Selenium)** | **96 TCs**:<br>1. `Autentikasi-Login` (12 TCs: `001–012`)<br>2. `Laporan Group A` (36 TCs: Semua, Kehadiran, Izin, Izin Terlambat, Izin Pulang Cepat, Cuti *[Hybrid]*, Sakit — `026–061`)<br>3. `Import` (12 TCs: 4 sub-menu — `113–124`)<br>4. `Management Group A` (36 TCs: User, Pendaftaran User, Pendaftaran Sendiri, Divisi, Unit, Unit Setting, Posisi, Client Upliner, Absen Point — `125–144` & `166–181`) | **99 TCs**:<br>1. `Dashboard` (13 TCs: Dashboard & Pending *[Hybrid]* — `013–025`)<br>2. `Laporan Group B + Download` (51 TCs: Lembur *[Hybrid]*, Validasi Lembur, Shifting, Meeting, Perjalanan Dinas *[Hybrid]*, Koreksi *[Hybrid]*, Izin Off *[Hybrid]*, Download Absen, Download Absen V2, Sisa Cuti, Daily Report — `062–112`)<br>3. `Management Group B` (35 TCs: Jabatan, Shifting, Jadwal, Kalender, Aturan Cuti, Day Off, Monthly Schedule, User Monitoring, Overtime Reason — `145–165` & `182–195`) |
| **Fitur API (REST Assured)** | **30 TCs** (`SIT_ADM_API_001–030`):<br>Auth & JWT Security (10), Users CRUD (12), Attendance/Activity/Corrections GET (8) | **30 TCs** (`SIT_ADM_API_031–060`):<br>Leaves/Overtimes GET & SLA (4), Approvals Workflow (10), Master Data CRUD (16) |
| **Scope Package POM** | `com.juaracoding.magang.pages.admin.deny.*` | `com.juaracoding.magang.pages.admin.aji.*` |
| **Scope Package Test** | `tests.admin.deny.*` & `AdminGroupAApiTest` | `tests.admin.aji.*` & `AdminGroupBApiTest` |
| **Git Branch (Opsi B)** | `feature/deny-admin-login`, `feature/deny-admin-reports-a`, `feature/deny-admin-import`, `feature/deny-admin-mgmt-a`, `feature/deny-admin-api` | `feature/aji-admin-dashboard`, `feature/aji-admin-reports-b`, `feature/aji-admin-mgmt-b`, `feature/aji-admin-api` |
| **Suite Runner XML** | `mvn test -DsuiteXmlFile=src/test/resources/testng-deny.xml` | `mvn test -DsuiteXmlFile=src/test/resources/testng-aji.xml` |

---

## 📋 Rincian Tugas per Fase

### 🔹 Fase 1: Discovery, Update SIT Adil (430 TCs) & Handoff Prompt Karyawan
- [x] **1.1. Discovery Menu Admin & Endpoint API Riil**
  - [x] Inspeksi seluruh rute Next.js (`_buildManifest.js`) dan *chunk* JS seluruh 18 sub-menu `Management-*`.
  - [x] Identifikasi backend API riil (`https://api-dev.dikahadir.com/hadir-api-magang`), endpoint `POST /authentication`, serta endpoint Hybrid Employee (`/activity-correction`, `/off-permit`, `/business-trips`, `/overtime`, `/late-permit`, `/early-leave`).
  - [x] Dokumentasikan API tools yang dipakai dan direkomendasikan (`Node.js fetch` / Chrome DevTools Network + Postman/Bruno + REST Assured `6.3.0`).
- [x] **1.2. Pembaruan Matriks SIT Tanpa Mengubah Skenario Existing (`SIT_Scenario_Magang_Hadir.xlsx`)**
  - [x] Pertahankan 100% seluruh 400 skenario existing tanpa mengubah isi skenario.
  - [x] Tambahkan **30 skenario baru modul Management (`SIT_ADM_UI_166` s/d `SIT_ADM_UI_195`)**.
  - [x] Distribusikan ulang kolom `Assigned SDET` secara adil untuk proyek Admin (**Deny 126 TCs** | **Aji 129 TCs**) dan proyek Karyawan (**Deny 87 TCs** | **Aji 88 TCs**).
- [x] **1.3. Penyusunan Dokumen Perencanaan & Prompt Proyek Karyawan**
  - [x] Buat [plan.md](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/plan.md) khusus Admin.
  - [x] Buat [tasklist.md](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/tasklist.md) multi-fase.
  - [x] Buat [WebKaryawanPrompt.md](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/WebKaryawanPrompt.md) berisi seluruh alur, struktur, tools, Base64 security, Hybrid pattern, endpoint riil, pembagian adil Deny/Aji, dan template prompt siap pakai untuk Agent AI di repositori proyek Karyawan.

---

### 🔹 Fase 2: Inisialisasi Proyek Maven & Struktur Package
- [x] **2.1. Buat `pom.xml` (Java 21)**
  - [x] Konfigurasi `selenium-java` (`4.52.0`), `rest-assured-bom` (`6.3.0`), `testng` (`7.12.0`), `extentreports` (`5.1.2`), `poi-ooxml` (`5.2.5`), `jackson-databind` (`2.17.2`), `maven-surefire-plugin` (`3.5.0`, default `headless=true`).
- [x] **2.2. Buat Struktur Direktori & File Konfigurasi**
  - [x] Buat `.gitignore`, `.gitattributes`, `.editorconfig`.
  - [x] Buat direktori `pages/admin/{deny,aji}`, `tests/admin/{deny,aji}`, `api/{helper,admin}`, `src/test/resources/testdata/`.

---

### 🔹 Fase 3: Core Engine, Keamanan Base64 & Hybrid API+UI Helper
- [x] **3.1. Keamanan Kredensial Base64**
  - [x] Implementasi `SecurityUtil.java` (`encode` / `decode` berbasis `java.util.Base64`).
  - [x] Implementasi `Constants.java` tanpa password plaintext (`ADMIN_PASSWORD_ENCODED`, `HYBRID_EMPLOYEE_PASSWORD_ENCODED`, serta konstanta URL & endpoint riil).
- [x] **3.2. Thread-Safe WebDriver & Base POM**
  - [x] Implementasi `DriverFactory.java` (`ThreadLocal<WebDriver>`, Chrome default `headless = true`).
  - [x] Implementasi `BasePage.java` dan `BaseTest.java`.
- [x] **3.3. REST Assured Engine, Hybrid Helper & Reporting**
  - [x] Implementasi `BaseApiTest.java` (`RestAssured.useRelaxedHTTPSValidation()`, `POST /authentication`, Bearer token helper).
  - [x] Implementasi `EmployeeApiHelper.java` untuk `@Before` *seeding* pengajuan Karyawan (`off-permit`, `activity-correction`, `overtime`, `business-trips`) serta query/fallback data Cuti.
  - [x] Implementasi `ScreenshotUtil.java`, `ExtentManager.java`, `ExtentTestManager.java`, `TestListener.java`.

---

### 🔹 Fase 4: Implementasi Admin Page Object Model (POM)
#### 👤 Bagian Tester Deny (`pages/admin/deny/`):
- [ ] **4.1.** `AdminLoginPage.java` (`/authentication/login`)
- [ ] **4.2.** `AdminReportsGroupAPage.java` (`Semua`, `Kehadiran`, `Izin`, `Izin Terlambat`, `Izin Pulang Cepat`, `Sakit`)
- [ ] **4.3.** `AdminLeavePage.java` (`Laporan-Cuti`: Cancel Cuti & verifikasi Defect Edit)
- [ ] **4.4.** `AdminImportPage.java` (4 sub-menu `Import-*`)
- [ ] **4.5.** `AdminManagementGroupAPage.java` (9 sub-menu: User, Pendaftaran User, Pendaftaran Sendiri, Divisi, Unit + Type-Shift, Unit Setting, Posisi, Client Upliner + BCA, Absen Point)

#### 👤 Bagian Tester Aji (`pages/admin/aji/`):
- [ ] **4.6.** `AdminDashboardPage.java` (`Dashboard-Dashboard` & `Dashboard-Pending`)
- [ ] **4.7.** `AdminReportsGroupBPage.java` (`Lembur`, `Validasi Lembur`, `Shifting`, `Meeting` + `Participants`)
- [ ] **4.8.** `AdminApprovalPage.java` (`Perjalanan Dinas`, `Koreksi`, `Izin Off`)
- [ ] **4.9.** `AdminDownloadPage.java` (`Download Absen`, `Download Absen V2`, `Sisa Cuti`, `Daily Report`)
- [ ] **4.10.** `AdminManagementGroupBPage.java` (9 sub-menu: Jabatan, Shifting, Jadwal, Kalender + Detail, Aturan Cuti, Day Off, Monthly Schedule, User Monitoring, Overtime Reason + Detail)

---

### 🔹 Fase 5: Implementasi Automated Test Suites (UI, API & Hybrid)
#### 👤 Bagian Tester Deny (126 TCs = 96 UI + 30 API):
- [ ] **5.1.** `AdminLoginTest.java` (`SIT_ADM_UI_001–012`: 12 TCs)
- [ ] **5.2.** `AdminReportsGroupATest.java` (`SIT_ADM_UI_026–061`: 36 TCs, termasuk Hybrid `Laporan-Cuti`)
- [ ] **5.3.** `AdminImportTest.java` (`SIT_ADM_UI_113–124`: 12 TCs)
- [ ] **5.4.** `AdminManagementGroupATest.java` (`SIT_ADM_UI_125–144` & `166–181`: 36 TCs)
- [ ] **5.5.** `AdminGroupAApiTest.java` (`SIT_ADM_API_001–030`: 30 TCs)

#### 👤 Bagian Tester Aji (129 TCs = 99 UI + 30 API):
- [ ] **5.6.** `AdminDashboardTest.java` (`SIT_ADM_UI_013–025`: 13 TCs, termasuk Hybrid `Dashboard-Pending`)
- [ ] **5.7.** `AdminReportsGroupBTest.java` (`SIT_ADM_UI_062–112`: 51 TCs, termasuk Hybrid Approve/Reject Koreksi, Izin Off, Perjalanan Dinas, Lembur)
- [ ] **5.8.** `AdminManagementGroupBTest.java` (`SIT_ADM_UI_145–165` & `182–195`: 35 TCs)
- [ ] **5.9.** `AdminGroupBApiTest.java` (`SIT_ADM_API_031–060`: 30 TCs)

---

### 🔹 Fase 6: Konfigurasi Suite Runner & Verifikasi Eksekusi
- [ ] **6.1.** Buat `src/test/resources/testng.xml` (Master 255 TCs, `headless=true`)
- [ ] **6.2.** Buat `src/test/resources/testng-deny.xml` (126 TCs) & `testng-aji.xml` (129 TCs)
- [ ] **6.3.** Jalankan verifikasi `mvn test -DsuiteXmlFile=src/test/resources/testng-deny.xml`, `testng-aji.xml`, dan `mvn clean test`

---

### 🔹 Fase 7: Validasi ExtentReports, Sinkronisasi Status SIT & Finalisasi
- [ ] **7.1.** Validasi `reports/ExtentReport.html` (filter `Tester Deny`, `Tester Aji`, `Selenium Web UI`, `REST Assured API`, `Hybrid API + UI`, Base64 screenshots)
- [ ] **7.2.** Sinkronisasi status hasil eksekusi aktual pada `SIT_Scenario_Magang_Hadir.xlsx`
- [ ] **7.3.** Lengkapi `README.md` dan `walkthrough.md`

