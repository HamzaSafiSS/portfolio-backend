# Portfolio Contact Backend

A secure, stateless Spring Boot REST backend for developer portfolio contact forms. Built with Java 17, Spring Web, Jakarta Bean Validation, and Brevo (Sendinblue) SMTP for transactional email routing.

---

## 🛠 Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 4.0.0 (Spring Web MVC)
- **Email:** Spring Boot Starter Mail (JavaMailSender / MimeMessageHelper via Brevo SMTP)
- **Validation:** Jakarta Bean Validation (Hibernate Validator)
- **Monitoring:** Spring Boot Actuator (Health probe endpoints)
- **Utilities:** Lombok, Jackson

---

## 🔒 Security Architecture

1. **Environment-Driven Secrets:** All SMTP credentials, sender identities, recipient addresses, and allowed CORS origins are injected via environment variables. Zero hardcoded secrets in source control.
2. **CORS Enforcement:** Strictly restricts API access to origins configured in `CORS_ALLOWED_ORIGINS`. Disallows wildcard origins in production and disables credentials (`allowCredentials=false`).
3. **Email Header Injection Defense:** Validates input strings with strict regex patterns (`^[^\\r\\n]*$`) and defensively strips CR/LF characters (`\r`, `\n`) before constructing email headers.
4. **HTML Injection Protection:** Safely encodes all visitor-supplied fields (`HtmlUtils.htmlEscape`) before rendering within HTML email templates.
5. **In-Memory Rate Limiting:** Sliding-window IP rate limiter protects against spam and automated abuse (default: 5 requests per 60 seconds per IP, returning HTTP 429).
6. **Honeypot Trap:** Optional hidden form field (`honeypot`) silently traps automated spam bots, returning HTTP 400 without triggering email dispatch.
7. **Safe Error Handling:** Centralized `GlobalExceptionHandler` ensures stack traces, database metadata, and internal server details are never leaked to clients.

---

## 📋 Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher
- **Maven:** 3.8+ (or use the included Maven wrapper `./mvnw`)
- **Brevo Account:** A free [Brevo (Sendinblue)](https://www.brevo.com/) account with a verified sender address and master SMTP key.

---

## ⚙️ Environment Variables

Create a `.env` file in the `backend/` directory by copying `.env.example`:

```bash
cp .env.example .env
```

| Variable | Description | Example / Default |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`dev` or `prod`) | `dev` |
| `SERVER_PORT` / `PORT` | HTTP server port | `8080` |
| `SMTP_HOST` | Brevo SMTP host | `smtp-relay.brevo.com` |
| `SMTP_PORT` | Brevo SMTP port (Use 465 for SSL on Render / Cloud) | `465` |
| `SMTP_USERNAME` | Brevo SMTP login email | `your-brevo-login@domain.com` |
| `SMTP_PASSWORD` | Brevo master SMTP key | `xsmtpsib-...` |
| `MAIL_FROM` | Verified sender email in Brevo | `contact@yourdomain.com` |
| `CONTACT_RECEIVER_EMAIL` | Destination email to receive inquiries | `personal@domain.com` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins | `http://localhost:5173,https://yourdomain.com` |
| `RATE_LIMIT_MAX_REQUESTS` | Allowed submissions per time window per IP | `5` |
| `RATE_LIMIT_WINDOW_SECONDS`| Duration of the rate-limit window in seconds | `60` |

---

## 🚀 Running Locally

### 1. Build and Run via Maven Wrapper

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8080`.

### 2. Verify Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

---

## 📡 API Reference

### Submit Contact Form

- **Endpoint:** `POST /api/v1/contact` *(also accessible via `/api/contact`)*
- **Headers:** `Content-Type: application/json`

#### Request Payload

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "subject": "Project Inquiry",
  "message": "I would like to discuss a software project with you."
}
```

#### Successful Response (HTTP 200 OK)

```json
{
  "success": true,
  "message": "Your message has been sent successfully."
}
```

#### Error Responses

- **HTTP 400 Bad Request:** Field validation failure or honeypot triggered:
  ```json
  {
    "success": false,
    "message": "Please provide a valid email address"
  }
  ```
- **HTTP 429 Too Many Requests:** Submission rate limit exceeded:
  ```json
  {
    "success": false,
    "message": "Too many requests. Please try again later."
  }
  ```
- **HTTP 500 Internal Server Error:** Email delivery failure or unhandled exception:
  ```json
  {
    "success": false,
    "message": "Failed to send your message. Please try again later."
  }
  ```

---

## 🧪 Testing

Run the automated test suite using Maven:

```bash
# Run all tests
./mvnw test

# Run only ContactController tests
./mvnw test -Dtest=ContactControllerTest
```

Manual test via cURL:

```bash
curl -X POST http://localhost:8080/api/v1/contact \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tester",
    "email": "tester@example.com",
    "subject": "Testing Contact API",
    "message": "This is a test message to verify the contact backend."
  }'
```

---

## 🌐 Production Deployment

### Option A: Standalone Linux Server (Ubuntu / Debian Systemd)

1. **Build the production JAR:**
   ```bash
   ./mvnw clean package -DskipTests
   ```
   The executable JAR is generated in `target/backend-0.0.1-SNAPSHOT.jar`.

2. **Copy JAR to your server:**
   ```bash
   scp target/backend-0.0.1-SNAPSHOT.jar user@your-server-ip:/opt/portfolio-backend/app.jar
   ```

3. **Create environment file on the server:**
   ```bash
   sudo nano /etc/portfolio-backend.env
   ```
   Add all required variables with `SPRING_PROFILES_ACTIVE=prod`.

4. **Create a Systemd Service:**
   ```ini
   # /etc/systemd/system/portfolio-backend.service
   [Unit]
   Description=Portfolio Contact Backend
   After=network.target

   [Service]
   User=www-data
   EnvironmentFile=/etc/portfolio-backend.env
   ExecStart=/usr/bin/java -jar /opt/portfolio-backend/app.jar
   SuccessExitStatus=143
   Restart=always
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```

5. **Enable and Start:**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable portfolio-backend
   sudo systemctl start portfolio-backend
   ```

---

### Option B: Cloud Hosting (Render, Railway, Fly.io, DigitalOcean)

1. **Repository:** Push your repository to GitHub or GitLab.
2. **Build Command:** `./mvnw clean package -DskipTests`
3. **Start Command:** `java -Dspring.profiles.active=prod -jar target/backend-0.0.1-SNAPSHOT.jar`
4. **Environment Variables:** Set all environment variables in your cloud provider's dashboard:
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `SMTP_HOST` = `smtp-relay.brevo.com`
   - `SMTP_PORT` = `587`
   - `SMTP_USERNAME` = `your-brevo-login@domain.com`
   - `SMTP_PASSWORD` = `your-brevo-smtp-key`
   - `MAIL_FROM` = `verified-sender@yourdomain.com`
   - `CONTACT_RECEIVER_EMAIL` = `your-personal-email@domain.com`
   - `CORS_ALLOWED_ORIGINS` = `https://your-portfolio-domain.com`
5. **Health Check Path:** Set the cloud provider's health check path to `/actuator/health`.

