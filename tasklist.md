# 📌 Tasklist Pelaksanaan: Automation Testing Admin `magang.dikahadir.com` (451 Admin TCs + Blueprint Proyek Karyawan)

Roadmap pengujian otomatisasi berbasis **Java 21**, **Selenium WebDriver `4.52.0`** (default `headless = true`), **REST Assured `6.3.0`**, **TestNG `7.12.0`**, dan **ExtentReports `5.1.2`** dengan struktur standar industri mengacu pada `reference.md`.

Khusus repositori ini difokuskan pada **Portal Admin HRD** (**451 Test Cases**: **321 Web UI** + **130 REST Assured API**), sedangkan seluruh panduan, struktur, pembagian tester, endpoint API, dan *master prompt* untuk repositori **Web Karyawan** (`250 TCs`: `140 Web UI` + `110 REST Assured API`) telah disiapkan melalui dokumen [WebKaryawanPrompt.md](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/WebKaryawanPrompt.md) dan matriks [docs/SIT_Scenario_Magang_Hadir.xlsx](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/docs/SIT_Scenario_Magang_Hadir.xlsx) (**v4 — 701 TCs + kolom `Manual Check`**).

---

## 🧭 Ikhtisar Status 7 Fase & Alokasi Beban Kerja

| Fase | Deskripsi Fase | Output Deliverables | Penanggung Jawab | Status |
| :---: | :--- | :--- | :---: | :---: |
| **Fase 1** | Discovery Menu & API Riil, Audit & Ekspansi SIT v4 (+125 API TCs & Kolom `Manual Check`), & Update `WebKaryawanPrompt.md` | `plan.md`, `tasklist.md`, `docs/SIT_Scenario_Magang_Hadir.xlsx` (701 TCs total), `WebKaryawanPrompt.md` | Shared Foundation | ✅ Selesai (Completed) |
| **Fase 2** | Maven Setup (`pom.xml` Selenium `4.52.0`) & Struktur Package Terisolasi (`deny` & `aji`) | `pom.xml`, `.gitignore`, `.editorconfig`, `src/main/...`, `src/test/...`, `testdata/` | Shared Foundation | ✅ Selesai (Completed) |
| **Fase 3** | Core Thread-Safe Engine, Keamanan Base64, & Hybrid API Seeding Helper | `SecurityUtil`, `Constants`, `DriverFactory` (`headless=true`), `BasePage`, `BaseTest`, `BaseApiTest`, `EmployeeApiHelper`, `ExtentManager`, `TestListener` | Shared Foundation | ✅ Selesai (Completed) |
| **Fase 4** | Page Object Model (POM) Admin Implementation | POM Deny (6/6 Classes selesai) & POM Aji (1/5 Classes selesai; sisa 4 Classes) | Deny & Aji | 🔄 In Progress (POM Deny 100% selesai; Aji 4.6 Dashboard merged, sisa `4.7–4.10`) |
| **Fase 5** | Automated Tests Implementation (Web UI + REST Assured API + Hybrid API+UI + E2E Lintas Modul) | `tests/admin/deny/*` (173/173 UI selesai), `tests/admin/aji/*` (13/148 UI merged), `AdminGroupAApiTest` (0/65 API), `AdminGroupBApiTest` (0/65 API) | Deny (238 TCs) & Aji (213 TCs) | 🔄 In Progress (`186/451` TCs selesai — Deny sisa `65 API`; Aji sisa `135 UI + 65 API`) |
| **Fase 6** | TestNG Multi-Suite Runner (`testng`, `testng-smoke`, `testng-regression`, `testng-positive`, `testng-negative`, `testng-deny`, `testng-aji`) & Verifikasi Headless | `testng.xml`, `testng-smoke.xml`, `testng-regression.xml`, `testng-positive.xml`, `testng-negative.xml` | Deny & Aji | 🔄 In Progress (5 XML Suites Active for 186 TCs; sisa `testng-deny.xml` & `testng-aji.xml`) |
| **Fase 7** | Validasi ExtentReports Dashboard, Sinkronisasi Status SIT v4, README & Walkthrough | `reports/ExtentReport.html`, `README.md`, `walkthrough.md` | Shared Deliverable | ⏳ Pending |

---

## 📌 Ringkasan Sisa Pekerjaan (Per 26 Nov 2026 — `186 / 451` Admin TCs Selesai)

- **👤 Sisa Tester Deny (`65 / 238 TCs` tersisa — seluruh 173 UI TCs sudah selesai)**:
  1. **REST Assured API (`5.6`)**: `AdminGroupAApiTest.java` (`SIT_ADM_API_001–030` & `061–095`: **65 TCs** — Auth & JWT Security, Users CRUD, Attendance/Corrections GET, Bulk Import Excel, Binary Export/Reports, Upload Security, Sync/Summary/Defect/RBAC).
  2. **Runner**: Buat `src/test/resources/testng-deny.xml` (**238 TCs**).
- **👤 Sisa Tester Aji (`200 / 213 TCs` tersisa — baru `AdminDashboardPage/Test` 13 TCs yang merged)**:
  1. **POM (`4.7–4.10`)**: `AdminReportsGroupBPage.java`, `AdminApprovalPage.java`, `AdminDownloadPage.java`, `AdminManagementGroupBPage.java` (+ helper E2E Aji bila diperlukan).
  2. **Web UI & E2E Tests (`5.8–5.10`, 135 UI TCs)**: `AdminReportsGroupBTest.java` (`080–153`: **74 TCs**), `AdminManagementGroupBTest.java` (`255–310`: **56 TCs**), dan `AdminCrossModuleAjiE2ETest.java` (`316–320`: **5 TCs**).
  3. **REST Assured API (`5.11`, 65 API TCs)**: `AdminGroupBApiTest.java` (`SIT_ADM_API_031–060` & `096–130`: **65 TCs** — Reports/SLA, Approvals Workflow, Master Data CRUD 14 Resources & Calendar Auth Security).
  4. **Runner**: Buat `src/test/resources/testng-aji.xml` (**213 TCs**).
- **🤝 Sisa Shared Finalization (`Fase 6.3 & Fase 7`)**:
  - Eksekusi penuh `testng-deny.xml`, `testng-aji.xml`, dan `mvn clean test` (headless).
  - Validasi akhir `reports/ExtentReport.html`, sinkronisasi status ke `docs/SIT_Scenario_Magang_Hadir.xlsx`, dan finalisasi `README.md` / `walkthrough.md`.
  - *(Proyek terpisah)* Implementasi repositori **Web Karyawan** (`250 TCs`: `140 Web UI` + `110 API`) mengacu pada [WebKaryawanPrompt.md](file:///c:/Users/Asus/Documents/JuaraCoding/Final/final-project-admin/WebKaryawanPrompt.md).

---

## ⚖️ Matriks Pembagian Tugas Adil (Proyek Admin — Total 451 TCs)

| Aspek Kolaborasi | 👤 Tester Deny (238 TCs — 52.8%) | 👤 Tester Aji (213 TCs — 47.2%) |
| :--- | :--- | :--- |
| **Fitur Web UI (Selenium)** | **173 TCs**:<br>1. `Autentikasi-Login` (12 TCs: `001–012`, *merged*)<br>2. `Laporan Group A` (54 TCs: `026–079`, *merged*)<br>3. `Import` (16 TCs: `154–169`, *merged*)<br>4. `Management Group A` (85 TCs: `170–254`, *merged*)<br>5. `Integrasi Lintas Modul (Hybrid API+UI)` — Bagian Deny (6 TCs: `311–315, 321`, *merged*) | **148 TCs**:<br>1. `Dashboard` (13 TCs: `013–025`, *merged*)<br>2. `Laporan Group B + Download` (74 TCs: `080–153`)<br>3. `Management Group B` (56 TCs: `255–310`)<br>4. `Integrasi Lintas Modul (Hybrid API+UI)` — Bagian Aji (5 TCs: `316–320`) |
| **Fitur API (REST Assured)** | **65 TCs** (`SIT_ADM_API_001–030` & `061–095`):<br>Auth & JWT Security (10), Users CRUD (12), Attendance/Corrections GET (8), Bulk Import Excel (11), Binary Export/Reports (10), Upload Security (5), Sync/Summary/Defect/RBAC (9) | **65 TCs** (`SIT_ADM_API_031–060` & `096–130`):<br>Reports GET & SLA (4), Approvals & State Workflow (22), Master Data CRUD 14 Resources + Calendar Unauth Security Defect (39) |
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
- [x] **4.2.** `AdminReportsGroupAPage.java` (`Semua`, `Kehadiran`, `Izin`, `Izin Terlambat`, `Izin Pulang Cepat`, `Sakit`) — *✅ Selesai & merged ke `main`*
- [x] **4.3.** `AdminLeavePage.java` (`Laporan-Cuti`: Cancel Cuti & verifikasi Defect Edit) — *✅ Selesai & merged ke `main`*
- [x] **4.4.** `AdminImportPage.java` (4 sub-menu `Import-*`) — *✅ Selesai & merged ke `main`*
- [x] **4.5.** `AdminManagementGroupAPage.java` (9 sub-menu: User [+filter lengkap & pagination], Pendaftaran User [+foto preview & Jumlah Cuti], Pendaftaran Sendiri [+pagination], Divisi, Unit + Type-Shift + `Atur Virtual ID`, Unit Setting, Posisi, Client Upliner + BCA, Absen Point) — *✅ Selesai & merged ke `main`*
- [x] **4.6b.** `AdminCrossModuleE2EPage.java` (Integrasi Lintas Modul Employee Portal `/absen/login`, `/apps/absent/leave-client`, `/apps/absent/sick` + `/laporan/sisa-cuti` + user toggle helper) — *✅ Selesai (`feature/deny-admin-e2e`)*

#### 👤 Bagian Tester Aji (`pages/admin/aji/`):
- [x] **4.6.** `AdminDashboardPage.java` (`Dashboard-Dashboard` & `Dashboard-Pending`) — *✅ Selesai & merged ke `main`*
- [ ] **4.7.** `AdminReportsGroupBPage.java` (`Lembur`, `Validasi Lembur`, `Shifting`, `Meeting` + `Participants`)
- [ ] **4.8.** `AdminApprovalPage.java` (`Perjalanan Dinas`, `Koreksi`, `Izin Off`)
- [ ] **4.9.** `AdminDownloadPage.java` (`Download Absen`, `Download Absen V2`, `Sisa Cuti`, `Daily Report`)
- [ ] **4.10.** `AdminManagementGroupBPage.java` (9 sub-menu: Jabatan, Shifting, Jadwal, Kalender + Detail, Aturan Cuti, Day Off, Monthly Schedule, User Monitoring, Overtime Reason + Detail)

---

### 🔹 Fase 5: Implementasi Automated Test Suites (UI, API, Hybrid & E2E)
#### 👤 Bagian Tester Deny (238 TCs = 173 UI + 65 API):
- [x] **5.1.** `AdminLoginTest.java` (`SIT_ADM_UI_001–012`: 12 TCs — *✅ Selesai & merged ke `main`*)
- [x] **5.2.** `AdminReportsGroupATest.java` (`SIT_ADM_UI_026–079`: 54 TCs, termasuk Hybrid `Laporan-Cuti` & Pagination 7 menu — *✅ Selesai & merged ke `main` [49 PASS + 5 Strict QA Known Defects]*)
- [x] **5.3.** `AdminImportTest.java` (`SIT_ADM_UI_154–169`: 16 TCs — *✅ Selesai & merged ke `main` [15 PASS + 1 New Defect on SIT_ADM_UI_167]*)
- [x] **5.4.** `AdminManagementGroupATest.java` (`SIT_ADM_UI_170–254`: 85 TCs, termasuk filter & pagination User, Preview Foto/Jumlah Cuti Register, pagination Sendiri, `Atur Virtual ID` Unit — *✅ Selesai & merged ke `main` [82 PASS + 2 Known Defects (202, 241) + 1 New Defect on SIT_ADM_UI_197]*)
- [x] **5.5.** `AdminCrossModuleDenyE2ETest.java` (`SIT_ADM_UI_311–315, 321`: 6 TCs Integrasi Lintas Modul Deny, termasuk E2E Jumlah Cuti — *✅ Selesai & merged ke `main` [5 PASS + 1 Strict QA Known Defect (321)]*)
- [ ] **5.6.** `AdminGroupAApiTest.java` (`SIT_ADM_API_001–030` & `061–095`: 65 TCs)

#### 👤 Bagian Tester Aji (213 TCs = 148 UI + 65 API):
- [x] **5.7.** `AdminDashboardTest.java` (`SIT_ADM_UI_013–025`: 13 TCs, termasuk Hybrid `Dashboard-Pending`) — *✅ Selesai & merged ke `main`*
- [ ] **5.8.** `AdminReportsGroupBTest.java` (`SIT_ADM_UI_080–153`: 74 TCs, termasuk Hybrid Approve/Reject & Pagination 7 tabel + `InfiniteScroll` Daily Report)
- [ ] **5.9.** `AdminManagementGroupBTest.java` (`SIT_ADM_UI_255–310`: 56 TCs, termasuk Pagination 8 menu)
- [ ] **5.10.** `AdminCrossModuleAjiE2ETest.java` (`SIT_ADM_UI_316–320`: 5 TCs Integrasi Lintas Modul Aji)
- [ ] **5.11.** `AdminGroupBApiTest.java` (`SIT_ADM_API_031–060` & `096–130`: 65 TCs)

---

### 🔹 Fase 6: Konfigurasi Suite Runner & Verifikasi Eksekusi
- [x] **6.1.** Buat `src/test/resources/testng.xml` serta suite 4 kategori (`testng-smoke.xml`, `testng-regression.xml`, `testng-positive.xml`, `testng-negative.xml`)
- [ ] **6.2.** Buat `src/test/resources/testng-deny.xml` (238 TCs) & `testng-aji.xml` (213 TCs) setelah seluruh modul selesai
- [ ] **6.3.** Jalankan verifikasi `mvn test -DsuiteXmlFile=src/test/resources/testng-deny.xml`, `testng-aji.xml`, dan `mvn clean test`

---

### 🔹 Fase 7: Validasi ExtentReports, Sinkronisasi Status SIT & Finalisasi
- [ ] **7.1.** Validasi `reports/ExtentReport.html` (filter `Tester Deny`, `Tester Aji`, `Selenium Web UI`, `REST Assured API`, `Hybrid API + UI`, Base64 screenshots)
- [ ] **7.2.** Sinkronisasi status hasil eksekusi aktual pada `docs/SIT_Scenario_Magang_Hadir.xlsx`
- [ ] **7.3.** Lengkapi `README.md` dan `walkthrough.md`

