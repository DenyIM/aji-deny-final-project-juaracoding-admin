# 📌 Tasklist Pelaksanaan: Automation Testing Admin `magang.dikahadir.com` (381 Admin TCs + Blueprint Proyek Karyawan)

Roadmap pengujian otomatisasi berbasis **Java 21**, **Selenium WebDriver `4.52.0`** (default `headless = true`), **REST Assured `6.3.0`**, **TestNG `7.12.0`**, dan **ExtentReports `5.1.2`** dengan struktur standar industri mengacu pada `reference.md`.

Khusus repositori ini difokuskan pada **Portal Admin HRD** (**381 Test Cases**: **321 Web UI** + **60 REST Assured API**), sedangkan seluruh panduan, struktur, pembagian tester, endpoint API, dan *master prompt* untuk repositori **Web Karyawan** (`195 TCs`: `140 Web UI` + `55 REST Assured API`) telah disiapkan melalui dokumen [WebKaryawanPrompt.md](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/WebKaryawanPrompt.md) dan matriks [docs/SIT_Scenario_Magang_Hadir.xlsx](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/docs/SIT_Scenario_Magang_Hadir.xlsx).

---

## 🧭 Ikhtisar Status 7 Fase & Alokasi Beban Kerja

| Fase | Deskripsi Fase | Output Deliverables | Penanggung Jawab | Status |
| :---: | :--- | :--- | :---: | :---: |
| **Fase 1** | Discovery Menu & API Riil, Audit & Refactor SIT v3 (5 Perbaikan Tester Aji + 10 Revisi Live Chunk + Pagination Menyeluruh), & Update `WebKaryawanPrompt.md` | `plan.md`, `tasklist.md`, `docs/SIT_Scenario_Magang_Hadir.xlsx` (576 TCs total), `WebKaryawanPrompt.md` | Shared Foundation | ✅ Selesai (Completed) |
| **Fase 2** | Maven Setup (`pom.xml` Selenium `4.52.0`) & Struktur Package Terisolasi (`deny` & `aji`) | `pom.xml`, `.gitignore`, `.editorconfig`, `src/main/...`, `src/test/...`, `testdata/` | Shared Foundation | ✅ Selesai (Completed) |
| **Fase 3** | Core Thread-Safe Engine, Keamanan Base64, & Hybrid API Seeding Helper | `SecurityUtil`, `Constants`, `DriverFactory` (`headless=true`), `BasePage`, `BaseTest`, `BaseApiTest`, `EmployeeApiHelper`, `ExtentManager`, `TestListener` | Shared Foundation | ✅ Selesai (Completed) |
| **Fase 4** | Page Object Model (POM) Admin Implementation | POM Deny (5+ Classes) & POM Aji (5+ Classes) | Deny & Aji | 🔄 In Progress (4.1 Login & 4.6 Dashboard merged) |
| **Fase 5** | Automated Tests Implementation (Web UI + REST Assured API + Hybrid API+UI + E2E Lintas Modul) | `tests/admin/deny/*` (173 UI), `tests/admin/aji/*` (148 UI), `AdminGroupAApiTest` (30 API), `AdminGroupBApiTest` (30 API) | Deny (203 TCs) & Aji (178 TCs) | 🔄 In Progress (5.1 Login & 5.7 Dashboard merged) |
| **Fase 6** | TestNG Multi-Suite Runner (`testng`, `testng-deny`, `testng-aji`) & Verifikasi Headless | `testng.xml`, `testng-deny.xml`, `testng-aji.xml` | Deny & Aji | ⏳ Pending |
| **Fase 7** | Validasi ExtentReports Dashboard, Sinkronisasi Status SIT v3, README & Walkthrough | `reports/ExtentReport.html`, `README.md`, `walkthrough.md` | Shared Deliverable | ⏳ Pending |

---

## ⚖️ Matriks Pembagian Tugas Adil (Proyek Admin — Total 381 TCs)

| Aspek Kolaborasi | 👤 Tester Deny (203 TCs — 53.3%) | 👤 Tester Aji (178 TCs — 46.7%) |
| :--- | :--- | :--- |
| **Fitur Web UI (Selenium)** | **173 TCs**:<br>1. `Autentikasi-Login` (12 TCs: `001–012`, *preserved on branch `feature/deny-admin-login`*)<br>2. `Laporan Group A` (54 TCs paginated: Semua `026–034`, Kehadiran `035–041`, Izin `042–048`, Izin Terlambat `049–055`, Izin Pulang Cepat `056–062`, Cuti *[Hybrid]* `063–071`, Sakit `072–079`)<br>3. `Import` (16 TCs: 4 sub-menu — `154–169`)<br>4. `Management Group A` (85 TCs: User `170–191` [+filter lengkap & pagination], Pendaftaran User `192–200` [+foto preview & Jumlah Cuti], Pendaftaran Sendiri `201–207` [+pagination], Divisi `208–215`, Unit `216–224` [+Atur Virtual ID], Unit Setting `225–229`, Posisi `230–237`, Client Upliner `238–246`, Absen Point `247–254`)<br>5. `Integrasi Lintas Modul (Hybrid API+UI)` — Bagian Deny (6 TCs: `311–316`) | **148 TCs**:<br>1. `Dashboard` (13 TCs: Dashboard `013–018` & Pending *[Hybrid]* `019–025`)<br>2. `Laporan Group B + Download` (74 TCs: Lembur *[Hybrid]* `080–088`, Validasi Lembur `089–093`, Shifting `094–100`, Meeting `101–115`, Perjalanan Dinas *[Hybrid]* `116–124`, Koreksi *[Hybrid]* `125–133`, Izin Off *[Hybrid]* `134–141`, Download Absen `142–143`, Download Absen V2 `144–146`, Sisa Cuti `147–148`, Daily Report `149–153`)<br>3. `Management Group B` (56 TCs: Jabatan `255–260`, Shifting `261–267`, Jadwal `268–274`, Kalender `275–280`, Aturan Cuti `281–286`, Day Off `287–288`, Monthly Schedule `289–296`, User Monitoring `297–302`, Overtime Reason `303–310`)<br>4. `Integrasi Lintas Modul (Hybrid API+UI)` — Bagian Aji (5 TCs: `317–321`) |
| **Fitur API (REST Assured)** | **30 TCs** (`SIT_ADM_API_001–030`):<br>Auth & JWT Security (10), Users CRUD (12), Attendance/Activity/Corrections GET (8) | **30 TCs** (`SIT_ADM_API_031–060`):<br>Leaves/Overtimes GET & SLA (4), Approvals PATCH Workflow (10), Master Data CRUD (16) |
| **Scope Package POM** | `com.juaracoding.magang.pages.admin.deny.*` | `com.juaracoding.magang.pages.admin.aji.*` |
| **Scope Package Test** | `tests.admin.deny.*` & `AdminGroupAApiTest` | `tests.admin.aji.*` & `AdminGroupBApiTest` |
| **Git Branch (Opsi B)** | `feature/deny-admin-login`, `feature/deny-admin-reports-a`, `feature/deny-admin-import`, `feature/deny-admin-mgmt-a`, `feature/deny-admin-e2e`, `feature/deny-admin-api` | `feature/aji-admin-dashboard`, `feature/aji-admin-reports-b`, `feature/aji-admin-mgmt-b`, `feature/aji-admin-e2e`, `feature/aji-admin-api` |
| **Suite Runner XML** | `mvn test -DsuiteXmlFile=src/test/resources/testng-deny.xml` | `mvn test -DsuiteXmlFile=src/test/resources/testng-aji.xml` |

---

## 📋 Rincian Tugas per Fase

### 🔹 Fase 1: Discovery, Audit SIT v3 (576 TCs) & Handoff Prompt Karyawan
- [x] **1.1. Discovery Menu Admin & Endpoint API Riil**
  - [x] Inspeksi seluruh rute Next.js (`_buildManifest.js`) dan *chunk* JS seluruh 18 sub-menu `Management-*`, 14 menu `Laporan-*` bertabel (`TablePagination`), serta rute User (`/apps/absent/reports/share_report`, `/absen/forgot-password`, `/apps/absent/change-password`, alias `leave-client` & `overtime-client`, dan 6 list `InfiniteScroll`).
  - [x] Identifikasi backend API riil (`https://api-dev.dikahadir.com/hadir-api-magang`), endpoint `POST /authentication`, serta endpoint Hybrid Employee (`/activity-correction`, `/off-permit`, `/business-trips`, `/overtime`, `/late-permit`, `/early-leave`, `/auth-management`, `/share-report`, `/upload`).
  - [x] Dokumentasikan API tools yang dipakai dan direkomendasikan (`Node.js fetch` / Chrome DevTools Network + Postman/Bruno + REST Assured `6.3.0`).
- [x] **1.2. Audit & Refactoring Matriks SIT v3 (`docs/SIT_Scenario_Magang_Hadir.xlsx` — 576 TCs)**
  - [x] Pertahankan `SIT_ADM_UI_001–012` (`Autentikasi-Login`) utuh agar implementasi branch `feature/deny-admin-login` tidak terdampak.
  - [x] Terapkan 5 perbaikan Tester Aji: urutan modul kontigu (`0` blok terpecah), pindah perilaku bug dari `Expected` ke kolom `Actual Result / Defect Notes` (`Status = Ready (Known Defect)`), kolom `Severity` + `Priority`, **22 Skenario Integrasi Lintas Modul E2E** (11 Admin + 11 User berbasis Hybrid API+UI), dan pemecahan skenario *compound* menjadi atomik.
  - [x] Terapkan 10 revisi *live chunk*: lengkapi 10 filter `Management-User` (`Job Level`, `Tipe Karyawan`, `Posisi Kerja`, `Lokasi Kerja`, `Status`, Search `Nama`/`NIK`, Kombinasi & Reset), koreksi menu aksi `Management-User` (hapus asumsi `Shift` & `Atur Virtual ID`), tambah modal `Atur Virtual ID` di `Management-Unit`, tambah **Foto Preview** + verifikasi **`Jumlah Cuti` (`Known Defect`)** di `Management-Pendaftaran User`, serta lengkapi **`Rows per page` (`5/10/25`) & `Next/Prev/First/Last`** pada seluruh 30 menu tabel Admin + `InfiniteScroll` pada `Daily Report` & 6 list User.
  - [x] Tambah **4 skenario User `Profil-Download Report`** (`SIT_USR_UI_126–129` untuk Tester Deny) serta perbaiki form `Reset Password` (+OTP) dan `Ubah Password` (tanpa field Password Lama) + 115 endpoint API ke FeathersJS riil.
  - [x] Distribusikan kolom `Assigned SDET` secara seimbang: Proyek Admin **381 TCs** (**Deny 203** | **Aji 178**) dan Proyek Karyawan **195 TCs** (**Deny 99** | **Aji 96**) — Grand Total **576 TCs** (**Deny 302 [52.4%]** | **Aji 274 [47.6%]**).
- [x] **1.3. Penyusunan Dokumen Perencanaan & Prompt Proyek Karyawan**
  - [x] Sinkronkan [plan.md](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/plan.md) khusus Admin ke SIT v3.
  - [x] Sinkronkan [tasklist.md](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/tasklist.md) multi-fase ke SIT v3.
  - [x] Sinkronkan [WebKaryawanPrompt.md](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/WebKaryawanPrompt.md) dengan SIT v3 (195 TCs, `Download Report`, `Reset Password` OTP, `Ubah Password`, `InfiniteScroll` 6 list, `11` E2E User Hybrid termasuk kuota cuti pendaftaran, dan acuan tunggal `docs/SIT_Scenario_Magang_Hadir.xlsx`).

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
- [x] **4.1.** `AdminLoginPage.java` (`/authentication/login`) — *✅ Selesai & merged ke `main`*
- [ ] **4.2.** `AdminReportsGroupAPage.java` (`Semua`, `Kehadiran`, `Izin`, `Izin Terlambat`, `Izin Pulang Cepat`, `Sakit`)
- [ ] **4.3.** `AdminLeavePage.java` (`Laporan-Cuti`: Cancel Cuti & verifikasi Defect Edit)
- [ ] **4.4.** `AdminImportPage.java` (4 sub-menu `Import-*`)
- [ ] **4.5.** `AdminManagementGroupAPage.java` (9 sub-menu: User [+filter lengkap & pagination], Pendaftaran User [+foto preview & Jumlah Cuti], Pendaftaran Sendiri [+pagination], Divisi, Unit + Type-Shift + `Atur Virtual ID`, Unit Setting, Posisi, Client Upliner + BCA, Absen Point)

#### 👤 Bagian Tester Aji (`pages/admin/aji/`):
- [x] **4.6.** `AdminDashboardPage.java` (`Dashboard-Dashboard` & `Dashboard-Pending`) — *✅ Selesai & merged ke `main`*
- [ ] **4.7.** `AdminReportsGroupBPage.java` (`Lembur`, `Validasi Lembur`, `Shifting`, `Meeting` + `Participants`)
- [ ] **4.8.** `AdminApprovalPage.java` (`Perjalanan Dinas`, `Koreksi`, `Izin Off`)
- [ ] **4.9.** `AdminDownloadPage.java` (`Download Absen`, `Download Absen V2`, `Sisa Cuti`, `Daily Report`)
- [ ] **4.10.** `AdminManagementGroupBPage.java` (9 sub-menu: Jabatan, Shifting, Jadwal, Kalender + Detail, Aturan Cuti, Day Off, Monthly Schedule, User Monitoring, Overtime Reason + Detail)

---

### 🔹 Fase 5: Implementasi Automated Test Suites (UI, API, Hybrid & E2E)
#### 👤 Bagian Tester Deny (203 TCs = 173 UI + 30 API):
- [x] **5.1.** `AdminLoginTest.java` (`SIT_ADM_UI_001–012`: 12 TCs — *✅ Selesai & merged ke `main`*)
- [ ] **5.2.** `AdminReportsGroupATest.java` (`SIT_ADM_UI_026–079`: 54 TCs, termasuk Hybrid `Laporan-Cuti` & Pagination 7 menu)
- [ ] **5.3.** `AdminImportTest.java` (`SIT_ADM_UI_154–169`: 16 TCs)
- [ ] **5.4.** `AdminManagementGroupATest.java` (`SIT_ADM_UI_170–254`: 85 TCs, termasuk filter & pagination User, Preview Foto/Jumlah Cuti Register, pagination Sendiri, `Atur Virtual ID` Unit)
- [ ] **5.5.** `AdminCrossModuleDenyE2ETest.java` (`SIT_ADM_UI_311–316`: 6 TCs Integrasi Lintas Modul Deny, termasuk E2E Jumlah Cuti)
- [ ] **5.6.** `AdminGroupAApiTest.java` (`SIT_ADM_API_001–030`: 30 TCs)

#### 👤 Bagian Tester Aji (178 TCs = 148 UI + 30 API):
- [x] **5.7.** `AdminDashboardTest.java` (`SIT_ADM_UI_013–025`: 13 TCs, termasuk Hybrid `Dashboard-Pending`) — *✅ Selesai & merged ke `main`*
- [ ] **5.8.** `AdminReportsGroupBTest.java` (`SIT_ADM_UI_080–153`: 74 TCs, termasuk Hybrid Approve/Reject & Pagination 7 tabel + `InfiniteScroll` Daily Report)
- [ ] **5.9.** `AdminManagementGroupBTest.java` (`SIT_ADM_UI_255–310`: 56 TCs, termasuk Pagination 8 menu)
- [ ] **5.10.** `AdminCrossModuleAjiE2ETest.java` (`SIT_ADM_UI_317–321`: 5 TCs Integrasi Lintas Modul Aji)
- [ ] **5.11.** `AdminGroupBApiTest.java` (`SIT_ADM_API_031–060`: 30 TCs)

---

### 🔹 Fase 6: Konfigurasi Suite Runner & Verifikasi Eksekusi
- [ ] **6.1.** Buat `src/test/resources/testng.xml` (Master 381 TCs, `headless=true`)
- [ ] **6.2.** Buat `src/test/resources/testng-deny.xml` (203 TCs) & `testng-aji.xml` (178 TCs)
- [ ] **6.3.** Jalankan verifikasi `mvn test -DsuiteXmlFile=src/test/resources/testng-deny.xml`, `testng-aji.xml`, dan `mvn clean test`

---

### 🔹 Fase 7: Validasi ExtentReports, Sinkronisasi Status SIT & Finalisasi
- [ ] **7.1.** Validasi `reports/ExtentReport.html` (filter `Tester Deny`, `Tester Aji`, `Selenium Web UI`, `REST Assured API`, `Hybrid API + UI`, Base64 screenshots)
- [ ] **7.2.** Sinkronisasi status hasil eksekusi aktual pada `docs/SIT_Scenario_Magang_Hadir.xlsx`
- [ ] **7.3.** Lengkapi `README.md` dan `walkthrough.md`

