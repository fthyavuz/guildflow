# GuildFlow — CLAUDE.md

## Project Overview

**GuildFlow** is a full-stack educational mentoring and student management platform. It facilitates mentor-student relationships, goal tracking, class management, meeting scheduling, and student evaluations. The app supports four user roles (ADMIN, MENTOR, STUDENT, PARENT) and three languages (Turkish, English, German).

---

## Tech Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Backend | Spring Boot | 3.4.3 | REST API framework |
| | Java | 21 | Language |
| | Spring Security | (managed) | Auth & authorization |
| | Spring Data JPA | (managed) | ORM/database abstraction |
| | JJWT | 0.12.6 | JWT token handling |
| | Flyway | (managed) | Database migrations |
| | Lombok | (managed) | Boilerplate reduction |
| | PostgreSQL | 16 | Relational database |
| Frontend | Angular | 19.2.0 | SPA framework (standalone components) |
| | TypeScript | 5.7.2 | Language |
| | RxJS | 7.8.0 | Reactive programming |
| | ngx-translate | 17.0.0 | i18n (TR/EN/DE) |
| | date-fns | 4.1.0 | Date utilities |
| Infra | Docker Compose | — | PostgreSQL container |

---

## Folder Structure

```
guildflow/
├── docker-compose.yml                        # PostgreSQL service (port 5432)
├── .gitignore
│
├── backend/                                  # Spring Boot (Maven)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/guildflow/backend/
│       │   ├── GuildflowBackendApplication.java   # Main entry point
│       │   ├── config/
│       │   │   └── DataSeeder.java                # Seed data on startup
│       │   ├── controller/                        # REST controllers (13 files)
│       │   │   ├── AuthController.java
│       │   │   ├── ClassController.java
│       │   │   ├── EventController.java
│       │   │   ├── GoalController.java
│       │   │   ├── MeetingController.java
│       │   │   ├── RoomController.java
│       │   │   ├── SourceController.java
│       │   │   └── UserController.java
│       │   ├── dto/                               # Request/Response DTOs (30+ files)
│       │   │   ├── LoginRequest.java / AuthResponse.java
│       │   │   ├── CreateUserRequest.java
│       │   │   ├── CreateClassRequest.java / ClassResponse.java
│       │   │   └── (goal, event, meeting, room, source DTOs)
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java    # Centralized error handling
│       │   ├── model/                             # JPA entities (18 files)
│       │   │   ├── User.java
│       │   │   ├── MentorClass.java
│       │   │   ├── Goal.java / GoalTask.java / GoalStudent.java
│       │   │   ├── Meeting.java / Attendance.java
│       │   │   ├── Event.java / Room.java
│       │   │   ├── StudentEvaluation.java
│       │   │   └── (relationship/bridge models)
│       │   ├── model/enums/                       # Domain enumerations
│       │   │   ├── Role.java                      # ADMIN, MENTOR, STUDENT, PARENT
│       │   │   ├── LanguagePreference.java        # TR, EN, DE
│       │   │   ├── EducationLevel.java            # PRIMARY, SECONDARY, HIGH_SCHOOL, UNIVERSITY
│       │   │   ├── TaskType.java                  # CHECKBOX, NUMBER
│       │   │   ├── AttendanceStatus.java          # PRESENT, ABSENT, EXCUSED, LATE
│       │   │   ├── EvaluationPeriod.java          # WEEKLY, MONTHLY, QUARTERLY
│       │   │   └── SourceType.java
│       │   ├── repository/                        # Spring Data JPA repositories (9+ files)
│       │   │   ├── UserRepository.java
│       │   │   ├── ClassStudentRepository.java
│       │   │   ├── AttendanceRepository.java
│       │   │   └── (others per domain)
│       │   ├── security/                          # JWT & Spring Security
│       │   │   ├── SecurityConfig.java            # Security config, CORS, public routes
│       │   │   ├── JwtTokenProvider.java          # Token generation & validation
│       │   │   ├── JwtAuthenticationFilter.java   # Per-request token filter
│       │   │   └── JwtAuthenticationEntryPoint.java
│       │   └── service/                           # Business logic (9 files)
│       │       ├── UserService.java
│       │       ├── ClassService.java
│       │       ├── GoalService.java
│       │       ├── EventService.java
│       │       ├── MeetingService.java
│       │       └── (room, source, evaluation services)
│       └── resources/
│           ├── application.properties             # Server, DB, JWT settings
│           └── db/migration/                      # Flyway migrations
│               ├── V1__create_users_table.sql
│               ├── V2__create_classes_tables.sql
│               ├── V3__create_goals_and_tasks_tables.sql
│               ├── V4__create_meetings_and_attendance_tables.sql
│               └── V5__create_student_evaluations_table.sql
│
└── frontend/                                 # Angular 19 (standalone)
    ├── package.json
    ├── tsconfig.json
    └── src/
        ├── main.ts                           # Angular bootstrap
        └── app/
            ├── app.config.ts                 # Providers (HttpClient, Router, i18n)
            ├── app.routes.ts                 # Top-level route definitions
            ├── app.component.*               # Root shell component
            ├── core/
            │   ├── guards/
            │   │   ├── auth.guard.ts         # Redirects unauthenticated users
            │   │   └── role.guard.ts         # Role-based route protection
            │   ├── interceptors/
            │   │   └── auth.interceptor.ts   # Attaches JWT; handles 401 refresh
            │   ├── models/                   # TypeScript interfaces
            │   │   ├── auth.model.ts
            │   │   ├── class.model.ts
            │   │   ├── student.model.ts
            │   │   ├── meeting.model.ts
            │   │   ├── event.model.ts
            │   │   └── (others)
            │   ├── pipes/
            │   │   └── event.pipe.ts
            │   └── services/                 # HTTP service layer (9 files)
            │       ├── auth.service.ts       # Login, logout, token refresh, currentUser$
            │       ├── class.service.ts
            │       ├── goal.service.ts
            │       ├── meeting.service.ts
            │       ├── event.service.ts
            │       ├── room.service.ts
            │       ├── user.service.ts
            │       ├── language.service.ts   # ngx-translate wrapper
            │       └── theme.service.ts      # Light/dark theme
            └── features/                     # Lazy-loaded feature components
                ├── auth/login/
                ├── dashboard/
                ├── classes/
                │   ├── class-list/
                │   ├── class-detail/
                │   ├── class-form/
                │   └── student-profile/
                ├── users/
                │   ├── mentor-list/
                │   ├── student-list/
                │   └── user-form/
                ├── goals/
                │   ├── goal-form/
                │   ├── goal-library/
                │   ├── goal-assignment/
                │   └── goal-tracking.component.ts
                ├── meetings/
                │   ├── meeting-list/
                │   └── meeting-form/
                ├── events/
                │   ├── event-list/
                │   ├── event-detail/
                │   └── event-form/
                ├── rooms/room-management/
                └── sources/source-list/
```

---

## Architecture

### Authentication & Authorization

- **JWT**: Access tokens (24h) + Refresh tokens (7d), stored in `localStorage`
- `JwtAuthenticationFilter` validates tokens on every request
- `auth.interceptor.ts` attaches `Bearer <token>` to all HTTP requests; on 401 it triggers a refresh
- Auth state is a `BehaviorSubject<User | null>` in `AuthService` (`currentUser$`)
- Route protection: `auth.guard.ts` (login required), `role.guard.ts` (role required)
- Backend uses `@PreAuthorize("hasRole('ADMIN')")` / `hasAnyRole(...)` method-level security
- Authenticated user injected into controller methods via `@AuthenticationPrincipal User user`

### API Design

- Base URL: `http://localhost:8080/api/`
- RESTful resources: `/api/auth`, `/api/users`, `/api/classes`, `/api/goals`, `/api/meetings`, `/api/events`, `/api/rooms`, `/api/sources`
- Layered: Controller → Service → Repository
- Validation: Jakarta annotations (`@NotBlank`, `@NotNull`, `@Email`) on DTOs; `@Valid` in controller params
- Error handling: `GlobalExceptionHandler` maps exceptions to structured HTTP responses
- CORS: `SecurityConfig` allows `http://localhost:4200`

### Frontend Architecture

- **No NgModules** — all components are standalone (`standalone: true`)
- **Lazy loading**: all feature components loaded via `loadComponent` in `app.routes.ts`
- **State management**: BehaviorSubject in services (no NgRx/Redux)
- **Forms**: Angular Reactive Forms (`FormBuilder`, `FormGroup`, `Validators`)
- **HTTP**: `HttpClient` in services; functional interceptor in `app.config.ts`
- **i18n**: ngx-translate, JSON files in `src/assets/i18n/{tr,en,de}.json`; Turkish is default
- **Theming**: `ThemeService` manages `light`/`dark` class on `<body>`

### Database

- PostgreSQL 16 via Docker (`docker-compose.yml`)
- Flyway versioned migrations (V1–V5); never edit existing migrations
- Soft deletes via `active BOOLEAN` column (not physical DELETE)
- Bridge tables for many-to-many: `class_students`, `goal_students`
- `created_at` / `updated_at` timestamps on all tables
- Indexes on foreign keys and frequently filtered columns

---

## Coding Conventions

### Backend (Java)

- **Package root**: `com.guildflow.backend`
- **Naming**:
  - Controllers: `*Controller` — one per domain
  - Services: `*Service` — one per domain
  - Repositories: `*Repository extends JpaRepository<Entity, Long>`
  - DTOs: `Create*Request`, `Update*Request`, `*Response`
  - Entities: plain class names matching domain (`User`, `MentorClass`, `Meeting`)
- **Lombok**: `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@NoArgsConstructor`, `@AllArgsConstructor` on DTOs and entities
- **Transactions**: `@Transactional` on all service methods that write
- **Return types**: `ResponseEntity<T>` — 200 OK, 201 Created, 204 No Content, 400 Bad Request, 403 Forbidden
- **Security annotations**: always use `@PreAuthorize` at the controller method level
- Enums live in `model/enums/` and are stored as strings in the DB (`@Enumerated(EnumType.STRING)`)

### Frontend (Angular/TypeScript)

- **Component files**: `<name>.component.ts` + `<name>.component.html` + `<name>.component.css`
- **Services**: `inject()` function inside component class body (not constructor injection)
- **All components** have `standalone: true`, explicit `imports: []`, `changeDetection: ChangeDetectionStrategy.OnPush` where applicable
- **Reactive patterns**: RxJS Observables; `.pipe(tap, switchMap, catchError)` chains
- **No subscription leaks**: use `async` pipe in templates or `takeUntilDestroyed()`
- **Models**: TypeScript `interface` (not `class`) in `core/models/`; one file per domain
- **API base**: defined as a constant `private readonly apiUrl = 'http://localhost:8080/api/...'` inside each service
- **Routing**: functional guards (`CanActivateFn`); lazy `loadComponent` only — no `loadChildren`

---

## Running the Project

### Prerequisites

- Java 21, Maven
- Node.js 18+, npm
- Docker Desktop

### Start Database

```bash
docker-compose up -d
```

### Start Backend

```bash
cd backend
mvn spring-boot:run
# Runs on http://localhost:8080
```

### Start Frontend

```bash
cd frontend
npm install
ng serve
# Runs on http://localhost:4200
```

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `backend/src/main/resources/application.properties` | Server port, DB URL, JWT secret/expiry |
| `backend/src/main/java/.../security/SecurityConfig.java` | CORS, public endpoints, filter chain |
| `backend/src/main/java/.../security/JwtTokenProvider.java` | Token generation, parsing, validation |
| `backend/src/main/resources/db/migration/V*.sql` | Flyway DB schema (source of truth) |
| `frontend/src/app/app.config.ts` | Angular providers: HTTP, router, i18n, interceptors |
| `frontend/src/app/app.routes.ts` | All client-side routes |
| `frontend/src/app/core/services/auth.service.ts` | Auth state, login/logout, token refresh |
| `frontend/src/app/core/interceptors/auth.interceptor.ts` | Auto-attach JWT; 401 → refresh flow |
| `docker-compose.yml` | PostgreSQL 16-alpine container config |

---

## Task Tracking

All tasks are tracked in [`TODO.md`](./TODO.md) at the project root.

**Rules:**
- Check `TODO.md` at the start of every session to understand current priorities.
- Mark tasks as done with `[x]` when completed — never delete completed tasks.
- Add any newly discovered tasks to `TODO.md` in the appropriate priority section.
- Update the progress summary table at the bottom of `TODO.md` when tasks are completed.

---

### Feature Development Workflow

Every new feature or improvement must follow this exact workflow:

1. **Add to TODO.md** — under the relevant module name, add a checklist entry and a clear description explaining what the task does and why.
2. **Create a git branch** — `git checkout -b feature/<short-name>` from `main`.
3. **Implement** — write all code changes needed.
4. **Verify build** — confirm both backend (`mvn spring-boot:run`) and frontend (`ng serve`) compile without errors.
5. **Ask the user** — present the completed feature and ask: *"Feature is ready. Please review and type YES to merge or NO to revise."*
6. **On YES** — mark the task `[x]` in TODO.md, merge the branch into `main`, and delete the feature branch.
7. **On NO** — stay on the branch, address the feedback, then repeat from step 4.