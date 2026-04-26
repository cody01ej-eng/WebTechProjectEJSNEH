# Deployment

## Environments
- Local
- Dev
- Staging
- Production

---

## Target Azure Topology

- Frontend: Azure Static Web Apps
- Backend: Azure App Service
- Database: Azure Database for MySQL
- Email: SMTP-compatible provider

---

## CI/CD

- `.github/workflows/ci.yml` now runs backend Maven verify, frontend Vitest, frontend Vite build, and Playwright browser tests
- `.github/workflows/deploy.yml` now packages the backend jar for Azure App Service and deploys the built frontend to Azure Static Web Apps
- Deployment jobs are parameterized through GitHub repository variables and secrets so the same workflow can be reused across environments

---

## GitHub Variables And Secrets

### Repository Variables

- `AZURE_BACKEND_WEBAPP_NAME`
- `VITE_API_BASE_URL`

### Repository Secrets

- `AZURE_BACKEND_WEBAPP_PUBLISH_PROFILE`
- `AZURE_STATIC_WEB_APPS_API_TOKEN`

`VITE_API_BASE_URL` should point at the deployed backend API origin, for example `https://your-backend-app.azurewebsites.net/api`.

---

## Backend App Service Settings

Use [backend/.env.example](../backend/.env.example) as the baseline. The key production values are:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `ALLOWED_ORIGINS=https://your-frontend-app.azurestaticapps.net`
- `REQUIRE_HTTPS=true`
- `SESSION_COOKIE_SAME_SITE=none`
- `SESSION_COOKIE_SECURE=true`
- `APP_EMAIL_BASE_URL=https://your-frontend-app.azurestaticapps.net`
- `APP_EMAIL_ENABLED`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

`SESSION_COOKIE_SAME_SITE=none` is required when the frontend and backend are hosted on different Azure domains and the browser needs to send the session cookie on cross-origin API requests.

The backend now exposes a public health endpoint at `/api/actuator/health`, which can be used with Azure App Service Health Check.

---

## Frontend Static Web App Settings

Use [frontend/.env.example](../frontend/.env.example) as the baseline.

- `VITE_API_BASE_URL=https://your-backend-app.azurewebsites.net/api`

The checked-in [frontend/staticwebapp.config.json](../frontend/staticwebapp.config.json) handles Vue history-mode route fallback so direct loads of `/admin`, `/student`, or `/instructor` resolve back to `index.html`.

---

## Runtime Configuration Checklist

### Required Email Settings

- `APP_EMAIL_ENABLED`
- `APP_EMAIL_BASE_URL`
- `APP_EMAIL_FROM_ADDRESS`
- `APP_EMAIL_FROM_NAME`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

### Gmail Example

- `MAIL_HOST=smtp.gmail.com`
- `MAIL_PORT=587`
- `MAIL_USERNAME=<gmail address>`
- `MAIL_PASSWORD=<gmail app password>`

---

## Deployment Notes

- Keep bootstrap-admin disabled outside local development
- Keep App Service HTTPS-only enabled
- Configure the frontend origin in `ALLOWED_ORIGINS` before testing authenticated browser flows
- Use the App Service publish profile secret for backend deployments and the Static Web Apps token for frontend deployments
