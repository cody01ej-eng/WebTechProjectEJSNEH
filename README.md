# Project Pulse

Project Pulse is a docs-first full-stack application for managing weekly activity reports (WAR) and peer evaluations in TCU senior design courses.

## Current State

The repository now includes Phase 1 foundation work and a substantial Phase 2 core-domain implementation:

- backend auth/user, project, and requirement-traceability modules following the domain-oriented package structure
- Flyway schema for sections, teams, invitations, users, WAR activity, peer evaluations, and traceability links
- session-based authentication plus role-guarded backend endpoints for admin, student, and instructor users
- SMTP-backed invitation delivery plus deep-linked invitation-registration and role-based workspaces for admin, student, instructor, and requirements-driven traceability
- baseline CI automation, local Maven verification support, and source-of-truth requirements/docs kept in view
- deployment-oriented Azure workflow assets, environment templates, and health-check/session settings for separate frontend/backend hosting

## Product Goal

The current submission and grading process depends on Google Sheets, Excel spreadsheets, and manual LMS uploads. Project Pulse consolidates that workflow so:

- students submit WARs and peer evaluations in one place
- instructors review reports and generated results in one place
- admins manage sections, teams, and assignments in one place

## Project Structure

```text
project-pulse-ai/
|-- backend/          Spring Boot foundation and domain module packages
|-- frontend/         Vue 3 + Vite foundation and feature workspace
|-- requirements/     Vision and scope, use cases, glossary
|-- docs/             Architecture, coding standards, API guidelines, plans
`-- .github/          CI workflow definitions
```

## Getting Started

### Frontend

```bash
cd frontend
npm install
npm run test:run
npm run dev
npm run test:e2e
```

### Backend

```bash
cd backend
set SPRING_PROFILES_ACTIVE=local
mvn spring-boot:run
```

The backend expects MySQL connection values through `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
Local development should run with the `local` profile. That profile enables the bootstrap admin account, allows localhost frontend origins, and disables HTTPS-only cookie requirements for workstation use.
Example environment templates now live in [backend/.env.example](backend/.env.example) and [frontend/.env.example](frontend/.env.example).

### Invitation Email Delivery

Invitation delivery now uses SMTP so the admin invite flows can send real registration emails. The backend keeps email delivery disabled by default until SMTP settings are provided.

For local Gmail-backed testing, set:

```bash
set APP_EMAIL_ENABLED=true
set APP_EMAIL_BASE_URL=http://localhost:5173
set APP_EMAIL_FROM_ADDRESS=your-gmail-address@gmail.com
set APP_EMAIL_FROM_NAME=Project Pulse Team
set MAIL_HOST=smtp.gmail.com
set MAIL_PORT=587
set MAIL_USERNAME=your-gmail-address@gmail.com
set MAIL_PASSWORD=your-app-password
```

Google accounts generally require an app password for SMTP access. Once configured, admin invitation emails contain deep links that open `/login` with the correct registration mode and invitation token already filled in.

## Implemented Phase 2 Scope

- Auth / User: invite students, invite instructors, register student accounts, register instructor accounts, sign in/out with session auth, load current session, search users, edit accounts, deactivate instructors, reactivate instructors
- Project: create and update rubrics/sections/teams, configure active weeks, assign students, assign instructors, remove students from teams, remove instructors from teams
- Reporting: create/update/delete WAR activities, submit peer evaluations, load team WAR reports, load student WAR reports, load section peer-evaluation reports, load student self reports, load instructor student reports
- Requirement / Traceability: create requirement references and implementation links
- Validation and Security Hardening: friendlier request-validation messages, client-side auth form checks, and controller-level security regression coverage
- Session Security Hardening: session-backed CSRF protection, stricter session/cookie defaults, configurable CORS origins, and local-only bootstrap-admin defaults
- Invitation Delivery: SMTP-backed student and instructor invitation emails with unique deep-linked registration URLs and per-invitation failure tracking
- Frontend UX: public login/invitation-registration portal plus protected admin, student, instructor, and requirements dashboards built on top of the implemented domain APIs, including guided role workspaces for admin setup focus, student peer-evaluation scoring, and instructor reporting windows
- Backend Security Coverage: auth/session endpoints plus guided student workspace and report endpoints now have expanded controller/security regression coverage
- Backend Persistence Coverage: H2-backed service integration tests now verify invitation lifecycle persistence, reporting aggregates, and student workspace context assembly against real saved entities
- Deployment Readiness: Azure deployment workflows, App Service health endpoint exposure, cross-origin session cookie configuration, frontend SPA fallback config, and checked-in environment templates now support production-style hosting more directly

## Verification Notes

- Frontend production build passes with the current Vue 3 + Vite workspace
- Frontend `npm run test:run` passes with Vitest component/store coverage for auth invitation handling, session state, and admin/student/instructor workspace flows
- Frontend `npm run test:e2e` passes with Playwright browser coverage for login-to-dashboard routing, admin section-to-invitation flow, and the student guided peer-evaluation journey
- Backend `mvn verify` passes locally using the repository-scoped Maven installation in `.tools/apache-maven-3.9.15`
- Current verification covers 13 passing frontend component tests, 3 passing Playwright end-to-end tests, and 48 passing backend tests, and produces the packaged Spring Boot jar in `backend/target/`

## Documentation

- [Architecture](docs/architecture.md)
- [Tech Stack](docs/tech-stack.md)
- [API Guidelines](docs/api-guidelines.md)
- [Coding Standards](docs/coding-standards.md)
- [Development Plan](docs/development-plan.md)
- [Team Workflow](docs/team-workflow.md)
- [Testing Strategy](docs/testing-strategy.md)
- [Deployment](docs/deployment.md)
