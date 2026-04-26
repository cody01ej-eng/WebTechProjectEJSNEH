# Development Plan

## Phase 1 - Foundation

- Initialize frontend and backend
- Setup database
- Setup CI/CD
- Establish domain module structure
- Define coding standards

### Status Snapshot

- Completed: backend Spring Boot scaffold with domain-oriented package structure
- Completed: frontend Vue 3 + Vite scaffold with feature-oriented workspace
- Completed: baseline CI workflow for frontend build and backend verification
- Completed: initial database migration structure and shared API/result handling
- Completed: local backend verification with Maven plus packaged Spring Boot artifact generation
- Completed: role-specific admin, student, and instructor frontend workspaces
- Completed: session-based authentication, bootstrap admin access, and role-aware route/endpoint protection
- Completed: SMTP-backed invitation delivery with unique registration links into the public auth flow
- Completed: frontend Vitest coverage for auth invitation handling, session-store behavior, and admin/student/instructor workspace flows
- Completed: Playwright end-to-end coverage for login-to-dashboard routing, admin guided invitation flow, and the student guided peer-evaluation journey
- Completed: student workspace polish using context-driven defaults for WAR and self-report loading plus guided teammate/rubric peer-evaluation scoring
- Completed: instructor workspace polish using selected student/team focus, shared report-window filters, and clearer snapshot/trend summaries
- Completed: admin workspace polish using selected section/team/user focus that cascades into setup, invitation, roster, and access-management actions
- Completed: expanded backend security/controller coverage for auth session endpoints, team/student report access rules, and the student workspace endpoint
- Completed: H2-backed service integration coverage for invitation lifecycle persistence, reporting aggregates, and student workspace context assembly
- Completed: Azure-oriented deployment wiring with environment templates, health-check exposure, SPA fallback config, and deploy workflow scaffolding
- Next: continue Phase 3 use-case depth with richer workflow presentation, deployment wiring, and broader report/workspace test coverage

---

## Phase 2 - Core Domains

Domains:

- Auth / User
- Project
- Requirement / Traceability

### Status Snapshot

- Completed: invitation and registration flows for students and instructors
- Completed: real SMTP-backed invitation delivery with deep-linked student and instructor registration URLs
- Completed: session login/logout, current-session loading, and role-guarded frontend routing
- Completed: clearer backend validation messages and client-side validation for the public auth flows
- Completed: session-backed CSRF protection, HTTPS-oriented session defaults, configurable CORS origins, and local-profile bootstrap-admin safeguards
- Completed: instructor search, account edit, deactivate, and reactivate workflows
- Completed: section, active week, rubric, team, student assignment, instructor assignment, and maintenance workflows
- Completed: team member removal safeguards for students and instructors
- Completed: WAR activity create, update, and delete services plus team/student reporting services
- Completed: peer-evaluation submission plus section/student/instructor reporting services
- Completed: requirement reference and traceability link management
- Completed: Phase 2 frontend workspaces mapped to the implemented domain APIs, including protected admin, student, instructor, and login/registration dashboards
- Completed: service-level backend tests for section setup, team membership rules, user activation flows, and peer-evaluation/reporting rules
- Completed: controller-level security tests for authentication, student-only WAR submission, self-report authorization paths, and student workspace endpoint access
- Completed: persistence-backed integration tests for invitation status transitions, team/section reporting aggregates, and student workspace defaults against seeded H2 data
- Completed: frontend Vitest setup with component/store tests for invitation-prefilled auth flows, session handling, and student/instructor report workflows
- Completed: local frontend production build, frontend Vitest runs, Playwright end-to-end runs, and backend Maven verify on the workstation, with 13 passing frontend component tests, 3 passing Playwright end-to-end tests, and 48 passing backend tests

---

## Team Ownership

- Member A: Auth / User
- Member B: Project
- Member C: Requirement

Each member owns:

- controller
- service
- repository
- domain
- dto

---

## Phase 3 - Use Case Implementation

Within each domain:

1. Continue tightening production deployment details and environment promotion workflow
2. Deepen the new role-specific views with richer task flows, validation hints, and report presentation
3. Add more backend tests for controller/service edge cases and negative-path validation
4. Continue requirement traceability updates as new use cases land
5. Prepare deployment-ready environment handling for Azure and MySQL

---

## Shared Responsibilities

- CI/CD
- Deployment
- Database schema
- API consistency

---

## Definition of Done

- Meets requirement specification
- Follows architecture rules
- Code reviewed
- Tested
- Integrated successfully
- Reflected in traceability and development planning docs
