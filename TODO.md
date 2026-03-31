# GuildFlow — TODO

> Feature tracker organized by user role and module.
> Follow the **Feature Development Workflow** in `CLAUDE.md` for every new task.

---

## 🛡️ Admin Dashboard

### Classes

- [x] **Class list** — browse all classes with search and filtering
- [x] **Class detail — Roster tab** — view enrolled students with progress circles; add or remove students via the enrollment panel
- [x] **Class detail — Assignments tab** — assign homework templates to the class; configure frequency, date range, and target students
- [x] **Homework form preview** — eye icon on each assignment row opens a read-only modal showing the exact student-facing entry form, with a live summary panel at the bottom
- [x] **Back button** — consistent top-left back navigation on all detail, form, and sub-pages

### Users

- [x] **User list page** — unified table showing all users across all roles; columns: name, email, role badge, status (active/inactive); paginated
- [x] **Role filter** — dropdown to filter the list by role (ALL / ADMIN / MENTOR / STUDENT / PARENT)
- [x] **Name & email search** — live search input that filters users by full name or email address
- [x] **Admin password reset** — each user row has a "Reset Password" action that opens a modal; admin enters and confirms a new password; backend enforces ADMIN-only access on the endpoint

### Homework Library

- [x] **Rename module** — "Goal/Quest" renamed to "Homework" across the UI in all three languages (TR/EN/DE)
- [x] **Simplified template form** — create a homework template with just a title, description, and task list; all scheduling fields removed from templates
- [x] **Clean library view** — template cards show title, task count, and resource badges; Edit and Delete only (no Assign button)
- [ ] **Task picker from Resource Library** — when building a template, tasks must be selected from the resource library via a search-and-pick flow; the resource's title and target value auto-fill the task

### Resource Library

- [x] **Dynamic categories** — resource categories are admin-managed (create, edit, delete) instead of hardcoded
- [x] **Tracking types** — each resource has a tracking type (LINEAR / BINARY), total capacity, and daily limit
- [x] **Mandatory capacity & daily limit** — total capacity and daily limit are now required fields when creating or editing a resource
- [ ] In Resource managemnet modul return panel button is not working
- [ ] The View of Resource , i mean for for CRUD is ugly. We need to align the css with other of applications.

### Meetings

_(Meeting management is functional — no pending improvements)_

### Events

- [ ] **Participant list** — view the RSVP list for a specific event (currently no way to see who has joined)

### Rooms

- [x] **Mentor room booking fix** — mentors were unable to book rooms due to `toISOString()` sending UTC with `Z` suffix that Jackson could not deserialize into `LocalDateTime`; fixed by sending local datetime string

---

## 👨‍🏫 Mentor Dashboard

### My Classes

_(Mentors share the same class detail view as admins — all Classes improvements above apply here too)_

### Student Reports

- [x] **Student report module** — mentor/admin selects a student from the sidebar to view all assigned homework; tasks grouped by resource category into In Progress / Finished sections; tasks can be individually Approved or Revoked
- [x] **Sidebar filter** — filter student list by name (live search) and education level dropdown
- [x] **Category progress chart** — bar chart showing daily aggregated totals by resource category (Book, Podcast, etc.); quick-select range buttons (1W / 1M / 6M / 1Y / All) plus manual date range inputs; defaults to showing all history
- [x] **Attendance statistics** — SVG donut chart showing present/absent/late/excused breakdown with attendance percentage; total sessions planned vs recorded; visible in student report for all roles (admin, mentor, student, parent)

### Meetings

_(Meeting scheduling and recurrence is functional — no pending improvements)_

---

## 🎓 Student Dashboard

### Homework Tracking

- [x] **Homework list** — cards showing each assigned homework with date range, frequency badge, task count, and overall progress; click a card to open the detail view
- [x] **Daily entry with date picker** — date input clamped to the homework's start/end range; loads NUMBER and CHECKBOX task inputs for the selected day
- [x] **Save-lock per day** — saving a day locks it; locked days show read-only values and a "Day Saved" pill; no re-editing after save
- [x] **Permanent CHECKBOX done** — CHECKBOX tasks marked done and saved are permanently completed across all dates; shown as "Done" regardless of selected day
- [x] **Progress summary** — cumulative progress bar per NUMBER task; overall progress bar per homework card, colour-coded by completion percentage
- [x] **Block future date entry** — date picker capped to today; students cannot enter data for future dates
- [x] **Daily limit enforcement** — homework entry screen shows per-task daily limit hint; input highlighted and Save button blocked when value exceeds the resource's daily limit

---

## 👨‍👩‍👧 Parent Dashboard

_(No features implemented yet — planned for a future phase)_

---

## ⚙️ Performance / Security / Code Quality

> Internal improvements that do not map to a specific dashboard feature.

### Security

- [x] JWT secret moved out of version control — uses environment variable
- [x] Seed data guarded behind dev profile — test accounts don't load in production
- [x] Password change endpoint — was a silent no-op; now actually updates the password
- [x] CORS configuration fixed — explicit header whitelist, no wildcard with credentials
- [x] JWT filter null-safety — deleted users can no longer silently authenticate
- [x] Token refresh race condition — parallel 401s share a single refresh observable instead of firing N refreshes
- [ ] **Rate limiting on login** — no brute-force protection on the login endpoint

### Performance

- [x] Pagination on all list endpoints
- [x] Missing database indexes added
- [x] N+1 queries fixed in homework and class progress queries
- [x] Lazy-load loops replaced with JOIN FETCH / EntityGraph
- [ ] **Caching** — read-heavy, rarely-changed data (resource types, rooms, user profiles) should be cached
- [ ] **Redis for token storage** — refresh tokens stored only in the JWT payload cannot be invalidated before expiry; move to Redis with TTL

### Code Quality

- [x] Custom exception hierarchy — typed exceptions map to correct HTTP status codes (404, 403, 409, 422)
- [x] UI error notifications — toast messages replace raw alert() and console.error() across all components
- [x] Shared access-control utility — SecurityUtils used across all services instead of copy-pasted checks
- [x] GoalService split into GoalTemplateService, GoalProgressService, GoalAssignmentService
- [x] JPA cascade on event deletion — no more manual pre-delete loops
- [x] FK cascade on task deletion — task_progress and task_completion rows are deleted automatically when a GoalTask is removed (fixes FK constraint error when editing homework)
- [x] Meeting recurrence count configurable — was hardcoded to 13 instances
- [ ] **DTO validation** — EventRequest (start < end enforced), GoalRequest (task target ≥ 1), RoomBookingRequest (time ordering)
- [ ] **Restrict soft-delete flag** — the `active` field in UserResponse should be visible to ADMIN only
- [ ] **Loading & empty states** — list views show no feedback while loading and no message when the list is empty
- [ ] **Global error handler** — Angular errors currently scattered across components; needs a centralised ErrorHandler
- [ ] **OnPush change detection** — all components use default strategy; switching to OnPush reduces unnecessary re-renders

### Testing

- [ ] **Unit tests: GoalService** — createGoal, assignTemplate, submitProgress, access-control rules
- [ ] **Unit tests: ClassService** — enrollment (including concurrent edge case), progress summary
- [ ] **Unit tests: JwtTokenProvider** — token generation, parsing, expiry detection, tampering
- [ ] **Integration tests: auth flow** — login, refresh, logout; invalid and expired token behaviour
- [ ] **Angular component tests** — GoalTrackingComponent and ClassDetailComponent render logic and form submission

### Infrastructure

- [x] Spring dev/prod profiles — DB credentials, CORS, seed-data flag, and JWT secrets separated by environment
- [x] `.env.example` — documents all required environment variables for new developers
- [x] API base URL in environment.ts — no more hardcoded localhost URLs in services
- [ ] **Swagger / OpenAPI** — interactive API docs at `/swagger-ui.html`
- [ ] **Request/response logging** — log user ID, method, endpoint, and response time per API call
- [ ] **Dev workflow scripts** — Makefile or npm scripts for `start`, `stop`, `test` across both services
- [ ] **API versioning** — prefix all routes with `/api/v1/`
- [ ] **CHANGELOG.md** — track notable changes between versions

---

## Progress Summary

| Section              | Total  | Done   |
| -------------------- | ------ | ------ |
| 🛡️ Admin Dashboard   | 18     | 16     |
| 👨‍🏫 Mentor Dashboard  | 3      | 3      |
| 🎓 Student Dashboard | 7      | 7      |
| 👨‍👩‍👧 Parent Dashboard  | 0      | 0      |
| ⚙️ Security          | 7      | 6      |
| ⚙️ Performance       | 6      | 4      |
| ⚙️ Code Quality      | 12     | 7      |
| ⚙️ Testing           | 5      | 0      |
| ⚙️ Infrastructure    | 8      | 3      |
| **Total**            | **65** | **45** |
