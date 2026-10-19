# Church Library App - Production Readiness Audit Framework
**Purpose:** A systematic checklist for auditing the Church Library App before public release.
**Last Updated:** October 2026
**Usage:** Copy the relevant section, modify `[AUDIT AREA]`, and use the area-specific questions.

---

## Generic Audit Prompt Template
I am now focusing exclusively on `[AUDIT AREA]`. This is a pre-release production-readiness audit.
⚠️ **DO NOT make any code changes yet.** First, perform a thorough audit and report ALL issues you find, even minor ones. We are very close to release.

---
## Generic Audit Framework

### 1. Core Functionality ✅
- Does this area do exactly what users expect it to do?
- Are there any edge cases where it fails silently?
- Are all buttons, toggles, and inputs fully functional?
- Does state persist correctly across app restarts, backgrounding, and process death?
- Does this feature work correctly when permissions (like Camera for scanning) are denied or revoked?

### 2. User Experience & Design 🎨
- **First Impression:** What does a new user see? Is it intuitive or confusing?
- **Empty States:** What is shown when there's no data (e.g., no books found)? Is it helpful and actionable?
- **Loading States:** Are there loading indicators where async operations or syncs occur?
- **Error States:** What happens when something goes wrong? Is the error message helpful?
- **Visual Polish:** Any clipped text, misaligned elements, inconsistent spacing, or jarring colors?
- **Accessibility:** Are touch targets ≥48dp? Is text contrast sufficient? Do icons have `contentDescription`?

### 3. Navigation & Flow 🧭
- Can users easily understand how to access and use this feature?
- Are there any dead ends or confusing navigation paths?
- Does the back button behave as expected?
- Is there unnecessary friction (too many taps, confirmations, or interruptions)?
- Does it integrate smoothly with the rest of the app's navigation?

### 4. Edge Cases & Robustness 🛡️
- What happens with very long text (e.g., long book titles, long borrower names)?
- What happens with unusual data (empty strings, special characters)?
- What happens on tablet screens vs mobile screens? Is it optimized for kiosk mode?
- What happens when quickly tapping buttons/submitting forms rapidly?
- What happens during process death and recreation?

### 5. Copy & Messaging 📝
- Is all text clear, grammatically correct, and free of typos?
- Is the tone appropriate for a church library (helpful, welcoming, clear)?
- Are there any placeholder strings (e.g., "TODO", "Lorem ipsum", untranslated "...")?
- Is technical jargon avoided? Would a non-technical volunteer understand everything?

### 6. Kiosk & Library Context 🏛️
- Is this area optimized for a shared tablet/kiosk environment?
- Would older members or those less tech-savvy understand how to use it easily?
- Does the screen automatically timeout/reset to the home screen if left abandoned?
- Does it handle offline modes transparently if the church Wi-Fi drops?

### 7. Pain Points & Frustrations 😤
- What would frustrate a user about this area?
- What is unintuitive or requires explanation?
- Is there anything that "almost works" but fails in a subtle way (like finding a book or typing a name)?

### 8. Polish & Delight ✨
- Does this feel like a premium, polished experience for the church?
- Are there opportunities for subtle animations or visual feedback (e.g., success checkmarks)?
- Would you be proud to demo this to the congregation?

---

## Deliverables
1. **Issue List:** ALL issues found, categorized as:
  - 🔴 **Critical:** Blocks release, causes crashes, data loss, or major UX failures
  - 🟡 **Minor:** Noticeable problems but not blockers
  - 🔵 **Polish:** Nice-to-haves that would improve quality
 
2. **File/Line References:** For each issue, note the file and line if applicable
3. **Recommendations:** Specific suggestions for improvement
4. **Verdict:** One of:
  - ✅ **Ready to Ship** - No issues or only polish items
  - ⚠️ **Needs Work** - Minor issues that should be addressed
  - ❌ **Not Ready** - Critical issues that block release

---

## Top Audit Areas (Prioritized)
Below are the most critical areas to audit, ordered by user impact. Each includes area-specific questions to add to the generic framework above.

### 🔥 Priority 1: Core User Journey (Borrow & Return)

**1. HOME SCREEN (Kiosk Idle State)**
*File: `feature/home/HomeScreen.kt`*
This is the default state of the kiosk. Must be obvious.
*SPECIFIC QUESTIONS:*
- Are the primary actions ("Borrow Books", "Return Books") prominent and unmissable?
- Does the screen layout scale well for horizontal and vertical tablet orientations?
- Is there a clear, unobtrusive indication of network/sync status?
- Is the hidden/subtle Admin entry point working but unlikely to be triggered accidentally?

**2. SELECT BOOKS SCREEN (Borrow Flow)**
*File: `feature/borrow/SelectBooksScreen.kt`*
*SPECIFIC QUESTIONS:*
- Is the book catalog easy to scroll and search?
- Are search results fast and accurate?
- Is book availability explicitly shown (preventing borrowing of already loaned books)?
- Can users easily add/remove books from their current checkout cart?
- Is there clear visual feedback when a book is added to the cart?

**3. BORROWER INFO SCREEN**
*File: `feature/borrow/BorrowerInfoScreen.kt`*
*SPECIFIC QUESTIONS:*
- Is the keyboard experience smooth and auto-focusing on the right fields?
- Are required fields (Name, Contact Info) clearly marked and enforced?
- Is data validation working (e.g., catching too short names)?
- Does the "Complete Borrow" button clearly communicate the final action?
- **Kiosk consideration:** Does this screen timeout and clear data if someone walks away mid-typing?

**4. BORROW SUCCESS SCREEN**
*File: `feature/borrow/BorrowSuccessScreen.kt`*
*SPECIFIC QUESTIONS:*
- Is the success message prominent and reassuring?
- Does it explicitly state the return due date for the borrowed items?
- Does the screen automatically navigate back to the Home Screen after a few seconds?

**5. RETURN BOOKS SCREEN**
*File: `feature/returns/ReturnBooksScreen.kt`*
*SPECIFIC QUESTIONS:*
- Is it easy for users to find their active loans (search by name or book title)?
- Is the return confirmation clear to prevent accidental returns?
- What happens if a book is already marked returned?
- Does it handle partial returns gracefully (returning 1 out of 3 borrowed books)?

### 🟠 Priority 2: Admin & Security

**6. PIN SCREEN**
*File: `feature/admin/PinScreen.kt`*
*SPECIFIC QUESTIONS:*
- Are PIN entries obfuscated (e.g., shown as dots)?
- Does it securely reject incorrect PINs with clear visual feedback?
- Is there a clear way to cancel and return to the Home Screen?

**7. ADMIN DASHBOARD SCREEN**
*File: `feature/admin/AdminDashboardScreen.kt`*
*SPECIFIC QUESTIONS:*
- Are all administrative actions (Manage Books, View Loans, Sync, Backup) accessible?
- Are overdue loans highlighted clearly?
- Can the admin easily trigger a manual data sync, and is the success/failure state clear?
- Can admin securely sign out/return to kiosk mode?

### 🟡 Priority 3: Background & Sync Services

**8. TABLET SYNC & OFFLINE MANAGER**
*Files: `core/sync/TabletSyncService.kt`, `core/sync/OfflineSyncManager.kt`*
*SPECIFIC QUESTIONS:*
- Do local database modifications securely queue for sync when offline?
- Does the app handle transitioning from offline to online smoothly without user interruption?
- Are `SyncReceivers` triggering appropriately on network state changes?
- Are conflicts resolved properly if a book state changes remotely and locally?

**9. BACKUP MANAGER**
*File: `core/backup/BackupManager.kt`*
*SPECIFIC QUESTIONS:*
- Are automatic backups scheduled (via WorkManager/AlarmManager) and executing?
- Is there a way for admins to export or trigger manual backups?
- Are backups saved in a secure, reliable location?

### 🟢 Priority 4: Data Integrity & Settings

**10. DATABASE & REPOSITORY OPERATIONS**
*Files: `core/database/*`, `core/repository/LibraryRepository.kt`*
*SPECIFIC QUESTIONS:*
- Are books marked "unavailable" synchronously when a loan is recorded to prevent double-booking?
- Are database queries for complex views (e.g., `BookWithAvailability`, `LoanWithBook`) optimized to prevent UI lag on large libraries?
- Does `DatabaseSnapshot` capture state accurately for backups?

**11. DATASTORE PREFERENCES**
*Files: `core/datastore/PinDataStore.kt`, `core/datastore/SettingsDataStore.kt`*
*SPECIFIC QUESTIONS:*
- Do the Admin PIN and settings persist reliably across app restarts?
- Is data encrypted or securely handled where necessary?

---

### Quick Start Checklist
Use this for a rapid audit of the most critical areas:
- [ ] **Home Screen:** Is the idle state welcoming and easy to understand?
- [ ] **Borrow Flow:** Can a user select a book, enter details, and finish successfully?
- [ ] **Return Flow:** Can a user easily locate their loan and return it?
- [ ] **Admin Access:** Does the PIN screen effectively block unauthorized access?
- [ ] **Offline Mode:** Does the app still allow browsing and borrowing if Wi-Fi is disabled?
- [ ] **Kiosk Timeout:** Do input screens reset automatically if abandoned?

### Recommended Audit Order
- **Day 1 - Core User Flow:** Areas 1-5 (Home → Borrow → Return)
- **Day 2 - Admin & Security:** Areas 6-7 (PIN → Admin Dashboard)
- **Day 3 - Data & Sync:** Areas 8-11 (Sync, Backups, DB, DataStore)
