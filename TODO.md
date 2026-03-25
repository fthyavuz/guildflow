# GuildFlow — TODO

> Generated from full codebase audit on 2026-03-19.
> Tasks are grouped by priority. Check off items as they are completed.

---

## 🔴 High Priority — Bugs, Security Issues, Broken Things

### Security

- [x] **Move JWT secret out of `application.properties`** — use an environment variable (`APP_JWT_SECRET`) and reference it via `${APP_JWT_SECRET}`. The current value is committed to version control.
  - `backend/src/main/resources/application.properties`
  - `backend/src/main/java/.../security/JwtTokenProvider.java`

- [x] **Guard `DataSeeder` behind a Spring profile or property** — currently seeds 16 test accounts (including `admin@guildflow.com / admin123`) on every startup regardless of environment. Add `@ConditionalOnProperty(name = "app.seed-data.enabled", havingValue = "true")` or restrict to `@Profile("dev")`.
  - `backend/src/main/java/.../config/DataSeeder.java`

- [x] **Implement the password change endpoint** — `PUT /api/auth/password` returns `200 OK` with a success message but is a complete no-op (`// TODO` comment). Users believe their password changed when it hasn't.
  - `backend/src/main/java/.../controller/AuthController.java:44-50`

- [x] **Fix CORS misconfiguration** — `setAllowedHeaders(List.of("*"))` combined with `setAllowCredentials(true)` violates the CORS security spec. Replace `"*"` with an explicit header whitelist (`Authorization`, `Content-Type`, `Accept`).
  - `backend/src/main/java/.../security/SecurityConfig.java:75-86`

- [x] **Externalize CORS allowed origin** — `http://localhost:4200` is hardcoded. Read it from a property (`app.cors.allowed-origins`) so it can be set per environment.
  - `backend/src/main/java/.../security/SecurityConfig.java:77`

- [x] **Fix JWT auth filter null-safety** — if a valid JWT references a deleted user, `userRepository.findByEmail().orElse(null)` returns `null` and authentication silently proceeds with no principal. Use `.orElseThrow()` or explicitly clear the security context.
  - `backend/src/main/java/.../security/JwtAuthenticationFilter.java:40-44`

- [x] **Add token refresh mutex in the Angular interceptor** — when multiple requests simultaneously receive 401, each independently calls `refreshToken()`, causing N parallel refresh attempts and token rotation desync. Implement a shared refresh observable with `BehaviorSubject` + `switchMap`.
  - `frontend/src/app/core/interceptors/auth.interceptor.ts:24-53`

### Bugs

- [x] **Fix missing `submitReview` / `submitProgress` methods in `GoalService`** — `GoalReviewController` and `ProgressController` called methods that didn't exist, preventing backend compilation. Added stub implementations with TODO comments pending `GoalReview` / `GoalProgress` entity creation.
  - `backend/src/main/java/.../service/GoalService.java`

- [x] **Fix dashboard calling non-existent `getUpcomingEvents()`** — `DashboardComponent` called `eventService.getUpcomingEvents()` which doesn't exist on `EventService`. Replaced with `getEvents()`.
  - `frontend/src/app/features/dashboard/dashboard.component.ts`

- [x] **Fix PostgreSQL "could not determine data type" on events query** — JPQL `IS NULL OR` pattern with null bind parameters causes PostgreSQL to fail when it can't infer the parameter type. Replaced the static JPQL query with a dynamic `JpaSpecificationExecutor` + `Specification<Event>` that only adds predicates for non-null values.
  - `backend/src/main/java/.../repository/EventRepository.java`
  - `backend/src/main/java/.../service/EventService.java`

- [x] **Fix N+1 query in `GoalService.getStudentGoalsWithProgress()`** — each task triggers 2 individual DB queries (one load + one progress fetch). For 10 goals × 3 tasks = 60+ queries per call. Use `@EntityGraph` or a `JOIN FETCH` query.
  - `backend/src/main/java/.../service/GoalService.java:232-281`

- [x] **Fix compounding N+1 in `ClassService.getClassProgressSummary()`** — calls `getStudentGoalsWithProgress()` inside a per-student loop. For a class of 10 students this results in ~81 DB queries for a single API call.
  - `backend/src/main/java/.../service/ClassService.java:172-198`

- [x] **Fix race condition in class enrollment** — two concurrent calls to `addStudentToClass()` can both pass the duplicate-check and insert two active enrollment rows. Add a unique DB constraint on `(class_id, student_id, active)` or use pessimistic locking.
  - `backend/src/main/java/.../service/ClassService.java:122-148`

- [x] **Add null-check for `goal.getMentorClass()`** — `goal.getMentorClass().getMentor()` will throw `NullPointerException` if `mentorClass` is null (which the JPA mapping allows).
  - `backend/src/main/java/.../service/GoalService.java:314-320`

- [x] **Validate that attendance students are enrolled in the class** — `markAttendance()` accepts any `studentId` without checking class membership. A mentor can record attendance for students from other classes.
  - `backend/src/main/java/.../service/MeetingService.java:116-143`

- [x] **Fix Goal entity vs. Flyway schema inconsistency** — `V3` migration defines `class_id BIGINT NOT NULL`, but the `Goal` Java entity has `@JoinColumn(name = "class_id")` without `nullable = false`. Align them. *(Resolved by V9 migration which already dropped the NOT NULL constraint on `class_id` to support templates — DB and entity are consistent.)*
  - `backend/src/main/java/.../model/Goal.java`
  - `backend/src/main/resources/db/migration/V3__create_goals_and_tasks_tables.sql`

### Performance

- [x] **Add pagination to all list endpoints** — every `GET` list endpoint returns an unbounded `List<T>`. Replace with `Page<T>` and accept `Pageable` parameters. Affected: `/users`, `/classes`, `/goals`, `/meetings`, `/events`, `/rooms`, `/sources`.
  - `backend/src/main/java/.../controller/` (all controllers)

- [x] **Add missing database indexes** — the following are absent from the migration files and cause full table scans:
  - `goal_tasks(goal_id)`
  - `goal_students(student_id)`
  - `class_students(student_id, active)` — composite
  - `task_progress(task_id, student_id)` — composite
  - Added as `V11__add_missing_indexes.sql`

- [x] **Replace lazy-load loops with `JOIN FETCH` / `@EntityGraph`** — `Goal`, `MentorClass`, `Meeting` entities all use `FetchType.LAZY` but are accessed in loops without batch loading, causing one query per entity access.
  - `backend/src/main/java/.../model/Goal.java`
  - `backend/src/main/java/.../service/GoalService.java`

### Angular

- [x] **Fix subscription leaks in `GoalTrackingComponent`** — `quickLog()` and `submitProgress()` call `.subscribe()` without cleanup. Implement `OnDestroy` with `takeUntil(destroy$)` or use `takeUntilDestroyed()`.
  - `frontend/src/app/features/goals/goal-tracking.component.ts`

- [x] **Fix subscription leaks in `ClassDetailComponent`** — `saveEnrollments()`, `addStudent()`, and `removeStudent()` all call `.subscribe()` without unsubscribing.
  - `frontend/src/app/features/classes/class-detail/class-detail.component.ts`

- [x] **Move all hardcoded API base URLs to `environment.ts`** — 9+ service files hardcode `http://localhost:8080/api/...`. Create `environment.ts` / `environment.prod.ts` and reference `environment.apiBaseUrl` in every service.
  - `frontend/src/app/core/services/*.service.ts` (all services)

---

## 🟡 Medium Priority — Refactoring & Missing Best Practices

### Error Handling

- [x] **Create a custom exception hierarchy** — replace the 55+ `new RuntimeException(...)` calls with typed exceptions (`EntityNotFoundException`, `ForbiddenException`, `ConflictException`). Update `GlobalExceptionHandler` to map each to the correct HTTP status (404, 403, 409, 422).
  - `backend/src/main/java/.../exception/GlobalExceptionHandler.java`
  - All `*Service.java` files

- [x] **Fix incorrect HTTP status codes** — services throw `RuntimeException` for all error cases, causing every business error to return HTTP 500. Map: not found → 404, unauthorized → 403, duplicate → 409, validation → 422.
  - All controllers and `GlobalExceptionHandler.java`

- [x] **Add user-facing error messages in Angular components** — errors are currently logged to `console.error()` or shown via raw `alert()`. Replace with a consistent UI notification pattern (toast, inline error, etc.).
  - `frontend/src/app/features/` (all feature components)

- [x] **Distinguish error types in auth interceptor `catchError`** — currently any error during token refresh triggers logout. Network timeouts and server errors should not log the user out; only an explicit 401 on the refresh request should.
  - `frontend/src/app/core/interceptors/auth.interceptor.ts:37-41`

### Code Quality

- [x] **Extract duplicated access-control checks into a shared utility** — the same user-state validation and mentor-ownership check pattern appears copy-pasted in `ClassService`, `GoalService`, `MeetingService`, and `RoomService`. Extract to a `SecurityUtils` component or Spring AOP aspect.
  - Created `backend/src/main/java/.../util/SecurityUtils.java` with `validateUserState()` and `requireAdminOrOwner()`

- [x] **Break up `GoalService` (god class)** — it handles goal CRUD, template management, assignment, progress tracking, and student lookups. Split into at least `GoalTemplateService`, `GoalProgressService`, and `GoalAssignmentService`.
  - `backend/src/main/java/.../service/GoalService.java` — now handles only goal types + goal CRUD
  - `backend/src/main/java/.../service/GoalTemplateService.java` — template listing + assignment
  - `backend/src/main/java/.../service/GoalProgressService.java` — student progress + mentor reviews

- [x] **Remove redundant null-check in `EvaluationService`** — `if (student == null ...)` appears after `.orElseThrow()`, making the check dead code that misleads readers.
  - `backend/src/main/java/.../service/EvaluationService.java:29`

- [x] **Replace manual cascade deletes with JPA cascade** — `EventService.deleteEvent()` manually deletes participants and assignments before deleting the event. If the entity already has `cascade = CascadeType.ALL`, this double-deletes. Align cascade config with deletion logic.
  - Added `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` to `Event.java` for participants and assignments
  - Simplified `EventService.deleteEvent()` to a single `eventRepository.delete(event)`

- [x] **Make meeting recurrence duration configurable** — the `for (int i = 0; i < 13; i++)` loop hardcodes exactly 13 recurring instances. Accept a `recurrenceCount` field in the request DTO.
  - Added `recurrenceCount` (default 13, max 52) to `MeetingRequest.java`

### DTO Validation

- [ ] **Add validation to `EventRequest`** — `startTime` and `endTime` have no `@NotNull` annotations. Add a custom cross-field validator to enforce `startTime < endTime`.

- [ ] **Add validation to `GoalRequest`** — task target values can be null or negative. Add `@NotNull` and `@Min(1)` to numeric task fields.

- [ ] **Add validation to `RoomBookingRequest`** — start/end time ordering is only checked in the service layer. Move it to a DTO-level validator.

- [ ] **Restrict `active` field in `UserResponse`** — the `active` soft-delete flag is returned to all roles, enabling enumeration of disabled accounts. Return it only to ADMIN.

### Configuration & Infrastructure

- [x] **Add Spring profiles for dev/prod** — create `application-dev.properties` and `application-prod.properties`. DB credentials, CORS origins, seed-data flag, and JWT secrets should differ by environment.

- [ ] **Add rate limiting on the login endpoint** — `POST /api/auth/login` has no brute-force protection. Integrate Spring's `RateLimiter` or Bucket4j to limit to ~5 attempts per minute per IP.

- [ ] **Add Swagger/OpenAPI documentation** — add `springdoc-openapi-starter-webmvc-ui` dependency and annotate controllers with `@Operation` and DTOs with `@Schema`. Expose docs at `/swagger-ui.html`.

- [ ] **Add structured request/response logging** — add a `HandlerInterceptor` or AOP aspect that logs user ID, HTTP method, endpoint, and response time for every API call. Use MDC for correlation IDs.

### Frontend

- [ ] **Add `ChangeDetectionStrategy.OnPush` to all components** — all feature components use the default change detection strategy, causing the entire tree to re-check on every event.

- [ ] **Add a global Angular `ErrorHandler`** — create a class that `implements ErrorHandler` and centralizes error reporting instead of scattered `console.error()` calls.

- [ ] **Add loading and empty states to all list views** — list components show no feedback while data is loading and no message when results are empty.

---

## 🟢 Low Priority — Nice-to-Have Improvements & Cleanup

### Testing

- [ ] **Write unit tests for `GoalService`** — cover `createGoal`, `assignGoalTemplate`, `submitProgress`, and access-control rules. Use Mockito to mock repositories.

- [ ] **Write unit tests for `ClassService`** — cover `addStudentToClass` (including duplicate/concurrent enrollment edge cases) and `getClassProgressSummary`.

- [ ] **Write integration tests for authentication flow** — cover login, token refresh, and logout using `@SpringBootTest` + `MockMvc`. Verify that invalid tokens return 401 and expired tokens trigger refresh.

- [ ] **Write unit tests for `JwtTokenProvider`** — verify token generation, parsing, expiry detection, and tampered-token rejection.

- [ ] **Add Angular component tests** — write `spec.ts` tests for `GoalTrackingComponent` and `ClassDetailComponent` covering render logic and form submission.

### Performance & Caching

- [ ] **Add `@Cacheable` to read-heavy, rarely-changed endpoints** — goal type list, room list, and user profile lookups are strong cache candidates. Use Spring's built-in cache abstraction (backed by Caffeine or Redis).

- [ ] **Add Redis for refresh token storage** — storing refresh tokens only in the JWT payload means they cannot be invalidated before expiry. Track them in Redis with TTL to support proper logout.

### Developer Experience

- [ ] **Add a `Makefile` or root-level `package.json` scripts** — simplify the dev workflow with `make start`, `make stop`, `make test` rather than requiring separate commands in `backend/` and `frontend/`.

- [x] **Add `.env.example` file** — document required environment variables (`APP_JWT_SECRET`, `DB_PASSWORD`, `CORS_ORIGINS`, `SEED_DATA_ENABLED`) so new developers know what to configure.

- [ ] **Add API versioning strategy** — prefix all routes with `/api/v1/` to allow non-breaking evolution of the API. Document the versioning policy in `CLAUDE.md`.

- [ ] **Add `DELETE` endpoint for goal types** — `GoalTypeController` is missing a delete operation.

- [ ] **Add `GET /{id}/participants` endpoint for events** — there is no endpoint to list RSVP participants for a specific event.

- [ ] **Remove `@SuppressWarnings("null")` from `DataSeeder`** — this annotation suppresses real null warnings rather than fixing them. Address the underlying nullability issues and remove the suppression.
  - `backend/src/main/java/.../config/DataSeeder.java`

- [ ] **Add `CHANGELOG.md`** — track changes between versions to support future contributors and release management.

---

## 🌐 UI / UX Improvements

- [x] **Rename "Goal/Quest" module to "Homework" across all three languages** — the goal module is student-facing and should use the more familiar "Homework" terminology. Update all translation keys and hardcoded strings in goal-form, goal-library, goal-assignment, and goal-tracking templates for EN, TR, and DE.
  - `frontend/src/app/public/i18n/en.json`, `tr.json`, `de.json`
  - `frontend/src/app/features/goals/**/*.html`
  - `frontend/src/app/features/dashboard/dashboard.component.html`

---

- [ ] **Phase A — Evolve Resource Library** — replace hardcoded `SourceType` enum with a dynamic `resource_categories` table (admin-configurable); add `totalCapacity`, `dailyLimit`, `trackingType` (LINEAR/BINARY) to sources; update backend entities/DTOs/services and frontend source-list UI with category management.
  - `backend/src/main/resources/db/migration/V13__evolve_resource_library.sql`
  - `backend/src/main/java/.../model/Source.java`, `ResourceCategory.java`
  - `backend/src/main/java/.../controller/SourceController.java`, new `ResourceCategoryController.java`
  - `frontend/src/app/core/models/source.model.ts`
  - `frontend/src/app/features/sources/source-list/`

---

## 🛠️ Admin Panel

> Feature tasks for building out and polishing the admin-facing UI and its supporting backend.

### General

- [x] **Fix back button — presence, behavior, and placement** — several pages either have no back button at all, or have one that does not work correctly (e.g. navigates to the wrong page or does nothing). Additionally, existing back buttons are placed in the middle of the page instead of the top-left corner where users expect them. Audit every feature page and apply the following rules consistently:
  - Every detail, form, and sub-page must have a back button in the **top-left** of the page header.
  - The button must navigate to the **logical parent page** (e.g. class detail → class list, student profile → class detail).
  - Use Angular's `Location.back()` only when the previous history entry is guaranteed to be the correct parent; otherwise navigate explicitly with `Router.navigate([...])` to avoid broken behaviour on direct URL access or external links.
  - Affected pages (audit required): `class-detail`, `class-form`, `student-profile`, `goal-form`, `goal-assignment`, `meeting-form`, `event-detail`, `event-form`, `user-form`, `source-list`, `room-management`.
  - `frontend/src/app/features/**/*.component.{ts,html}`

---

## Progress Summary

| Priority | Total | Done |
|---|---|---|
| 🔴 High | 22 | 22 |
| 🟡 Medium | 18 | 10 |
| 🟢 Low | 13 | 1 |
| 🛠️ Admin Panel | 1 | 1 |
| **Total** | **54** | **34** |
