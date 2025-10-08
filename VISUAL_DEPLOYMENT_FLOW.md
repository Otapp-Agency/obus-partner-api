# 🎨 Visual Deployment Flow

## 📊 Complete Deployment Process

```
╔════════════════════════════════════════════════════════════════════════════╗
║                         DEPLOYMENT WORKFLOW                                 ║
╚════════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 1: PREPARE LOCAL (Your Computer)                          [5 minutes] │
└─────────────────────────────────────────────────────────────────────────────┘

    📝 Edit docker-compose.yml
    │  Line 5: Change to YOUR GitHub username
    │
    ├── ✅ Commit changes
    │   │
    │   └── $ git add .
    │       $ git commit -m "feat: deployment setup"
    │       $ git push origin deploy
    │
    └── 🔐 GitHub Secrets (Settings → Secrets → Actions)
        ├── SERVER_HOST = obus-partners.otapp.live
        ├── SERVER_USER = forge
        └── SSH_PRIVATE_KEY = (from cat ~/.ssh/id_rsa)

                            ⬇️

┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 2: COPY FILES TO SERVER                                   [2 minutes] │
└─────────────────────────────────────────────────────────────────────────────┘

    💻 Local Machine
    │
    ├── $ ./copy-to-server.sh forge@obus-partners.otapp.live
    │
    └── Files copied:
        ├── deploy/
        ├── nginx/
        ├── docker-compose.yml
        ├── Dockerfile
        └── Documentation

                            ⬇️

┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 3: SETUP SERVER (One-Time)                               [20 minutes] │
└─────────────────────────────────────────────────────────────────────────────┘

    🔌 SSH to Server
    │
    ├── $ ssh forge@obus-partners.otapp.live
    │   $ cd ~/obus-partners.otapp.live
    │
    ├── 🔧 Initial Setup
    │   │
    │   └── $ ./deploy/initial-server-setup.sh
    │       ├── ✅ Creates directories
    │       ├── ✅ Makes scripts executable
    │       ├── ✅ Creates .env templates
    │       └── ✅ Checks Docker installation
    │
    ├── 📝 Configure Environment
    │   │
    │   └── $ nano .env.staging
    │       ├── Database URL
    │       ├── Database credentials
    │       ├── JWT_SECRET (openssl rand -base64 32)
    │       └── API_KEY_ENCRYPTION_SECRET (openssl rand -base64 32)
    │
    ├── 🔒 Setup SSL
    │   │
    │   └── $ sudo ./deploy/setup-ssl.sh
    │       ├── Installs certbot
    │       ├── Gets certificate for obus-partners.otapp.live
    │       └── Copies to nginx/ssl/
    │
    ├── 🐙 Login to GHCR
    │   │
    │   └── $ echo TOKEN | docker login ghcr.io -u USERNAME --password-stdin
    │       └── ✅ Login Succeeded
    │
    └── 🔧 Set Environment Variables
        │
        └── $ export GITHUB_REPOSITORY="username/obus-partner-api"
            $ export PROFILE=staging
            $ echo 'export GITHUB_REPOSITORY="username/obus-partner-api"' >> ~/.bashrc

                            ⬇️

┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 4: DEPLOY APPLICATION                                     [5 minutes] │
└─────────────────────────────────────────────────────────────────────────────┘

    🚀 Deploy
    │
    └── $ ./deploy/quick-deploy.sh staging
        │
        ├── 📥 Pulling latest images from GHCR
        │
        ├── 🛑 Stopping old containers
        │
        ├── 🚀 Starting new containers
        │   ├── obus-partner-api (App)
        │   ├── obus-redis (Cache)
        │   └── obus-nginx (Proxy)
        │
        ├── ⏳ Waiting for startup
        │
        └── ✅ Health check passed!

                            ⬇️

┌─────────────────────────────────────────────────────────────────────────────┐
│ PHASE 5: VERIFY DEPLOYMENT                                      [2 minutes] │
└─────────────────────────────────────────────────────────────────────────────┘

    ✅ Check Status
    │
    ├── $ docker compose ps
    │   └── All containers: Up (healthy)
    │
    ├── $ curl http://localhost:8080/actuator/health
    │   └── {"status":"UP"}
    │
    └── 🌐 Browser Test
        ├── https://obus-partners.otapp.live/actuator/health ✅
        ├── https://obus-partners.otapp.live/swagger-ui.html ✅
        └── No SSL warnings ✅

                            ⬇️

┌─────────────────────────────────────────────────────────────────────────────┐
│ 🎉 DEPLOYMENT COMPLETE!                                                     │
└─────────────────────────────────────────────────────────────────────────────┘

    Your application is now live at:
    🌐 https://obus-partners.otapp.live

```

---

## 🔄 Future Deployments (After Initial Setup)

```
╔════════════════════════════════════════════════════════════════════════════╗
║                      FUTURE DEPLOYMENT FLOW                                 ║
╚════════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────────────────┐
│ METHOD A: AUTOMATIC (GitHub Actions) ⭐ RECOMMENDED             [2 minutes] │
└─────────────────────────────────────────────────────────────────────────────┘

    💻 Local Machine
    │
    ├── Make code changes
    │   $ git add .
    │   $ git commit -m "your changes"
    │   $ git push origin deploy
    │
    │                   ⬇️
    │
    ├── 🤖 GitHub Actions (Automatic)
    │   ├── ✅ Checkout code
    │   ├── ✅ Build Docker image
    │   ├── ✅ Push to GHCR
    │   └── ✅ Deploy to server
    │
    │                   ⬇️
    │
    └── 🎉 DONE! Application automatically updated

    Time: ~2 minutes (push + wait for build)
    Effort: Just push code!


┌─────────────────────────────────────────────────────────────────────────────┐
│ METHOD B: MANUAL DEPLOYMENT                                     [5 minutes] │
└─────────────────────────────────────────────────────────────────────────────┘

    🔌 SSH to Server
    │
    ├── $ ssh forge@obus-partners.otapp.live
    │   $ cd ~/obus-partners.otapp.live
    │
    └── $ ./deploy/quick-deploy.sh staging
        │
        ├── Pull latest image
        ├── Restart containers
        └── ✅ DONE!

    Time: ~5 minutes
    Effort: Run one command

```

---

## 🏗️ Architecture Diagram

```
╔════════════════════════════════════════════════════════════════════════════╗
║                      SYSTEM ARCHITECTURE                                    ║
╚════════════════════════════════════════════════════════════════════════════╝

┌──────────────────────────────────────────────────────────────────────────┐
│                                                                          │
│  👤 User (Browser/API Client)                                           │
│                                                                          │
└──────────────────────────────┬───────────────────────────────────────────┘
                               │
                               │ HTTPS (443)
                               │ SSL Certificate
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  🌐 NGINX Container (obus-nginx)                                        │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  • SSL Termination (Let's Encrypt)                               │  │
│  │  • Reverse Proxy                                                 │  │
│  │  • Gzip Compression                                              │  │
│  │  • Security Headers                                              │  │
│  │  • Rate Limiting                                                 │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  Port: 80 (HTTP → HTTPS redirect)                                      │
│  Port: 443 (HTTPS)                                                     │
│                                                                         │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 │ HTTP (8080)
                                 │ Internal Docker Network
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  ☕ Spring Boot Application (obus-partner-api)                          │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  • Java 21 (Eclipse Temurin)                                     │  │
│  │  • Spring Boot Framework                                         │  │
│  │  • ZGC Garbage Collector                                         │  │
│  │  • REST API Endpoints                                            │  │
│  │  • Business Logic                                                │  │
│  │  • Authentication (JWT)                                          │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  Port: 8080                                                             │
│  Health: /actuator/health                                               │
│  Docs: /swagger-ui.html                                                 │
│                                                                         │
└────────────────┬───────────────────────────────────┬────────────────────┘
                 │                                   │
                 │                                   │
                 │                                   │
        ┌────────▼────────┐                 ┌────────▼────────┐
        │                 │                 │                 │
        │  🗄️ Redis       │                 │  🗄️ PostgreSQL │
        │  Cache          │                 │  Database       │
        │                 │                 │                 │
        │  Container:     │                 │  External:      │
        │  obus-redis     │                 │  Your DB Server │
        │                 │                 │                 │
        │  Port: 6379     │                 │  Port: 5432     │
        │  Cache Layer    │                 │  Persistent     │
        │  Session Store  │                 │  Data           │
        │                 │                 │                 │
        └─────────────────┘                 └─────────────────┘

All containers connected via: obus-network (Docker bridge network)

```

---

## 📦 Docker Container Details

```
╔════════════════════════════════════════════════════════════════════════════╗
║                        CONTAINER OVERVIEW                                   ║
╚════════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────────────────┐
│ 📦 obus-partner-api (Application Container)                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│ Image:      ghcr.io/username/obus-partner-api:latest                        │
│ Base:       eclipse-temurin:21-jre-alpine                                   │
│ Port:       8080 → 8080                                                     │
│ Memory:     1GB min, 2GB max                                                │
│ Health:     /actuator/health                                                │
│ Restart:    unless-stopped                                                  │
│ Env File:   .env.staging                                                    │
│ Volumes:    ./logs:/app/logs                                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ 🗄️ obus-redis (Cache Container)                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ Image:      redis:7-alpine                                                  │
│ Port:       6379 → 6379                                                     │
│ Memory:     256MB max (with LRU eviction)                                   │
│ Health:     redis-cli ping                                                  │
│ Restart:    unless-stopped                                                  │
│ Volumes:    redis-data:/data (persistent)                                   │
│ Config:     AOF enabled, LRU eviction policy                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ 🌐 obus-nginx (Reverse Proxy)                                               │
├─────────────────────────────────────────────────────────────────────────────┤
│ Image:      nginx:alpine                                                    │
│ Ports:      80 → 80, 443 → 443                                              │
│ Health:     nginx -t                                                        │
│ Restart:    unless-stopped                                                  │
│ Volumes:    ./nginx/nginx.conf:/etc/nginx/nginx.conf                        │
│             ./nginx/ssl:/etc/nginx/ssl                                      │
│             ./nginx/logs:/var/log/nginx                                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Security Flow

```
╔════════════════════════════════════════════════════════════════════════════╗
║                         SECURITY LAYERS                                     ║
╚════════════════════════════════════════════════════════════════════════════╝

Request Flow:

1️⃣  Client Request
    └─→ https://obus-partners.otapp.live/api/endpoint

2️⃣  Nginx (Security Layer 1)
    ├─→ SSL/TLS Termination ✅
    ├─→ HTTPS Enforcement ✅
    ├─→ Security Headers ✅
    │   ├── X-Frame-Options: SAMEORIGIN
    │   ├── X-Content-Type-Options: nosniff
    │   ├── X-XSS-Protection: 1
    │   └── Strict-Transport-Security: max-age=31536000
    ├─→ Rate Limiting (if configured)
    └─→ Forward to App (HTTP)

3️⃣  Spring Boot App (Security Layer 2)
    ├─→ CORS Validation ✅
    ├─→ JWT Token Verification ✅
    ├─→ Authentication ✅
    ├─→ Authorization ✅
    └─→ Business Logic

4️⃣  Data Layer (Security Layer 3)
    ├─→ Encrypted API Keys ✅
    ├─→ Hashed Passwords ✅
    └─→ Secure DB Connection ✅

Response:
    └─→ Flows back through all layers with security headers
```

---

## 📊 Monitoring & Health Checks

```
╔════════════════════════════════════════════════════════════════════════════╗
║                      HEALTH CHECK SYSTEM                                    ║
╚════════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────────────────┐
│ Application Health Check                                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│ Endpoint:   /actuator/health                                                │
│ Interval:   Every 30 seconds                                                │
│ Timeout:    10 seconds                                                      │
│ Retries:    3 attempts                                                      │
│ Start Wait: 60 seconds                                                      │
│                                                                             │
│ Checks:                                                                     │
│  ✅ Application is running                                                  │
│  ✅ Database connection                                                     │
│  ✅ Redis connection                                                        │
│  ✅ Disk space                                                              │
│  ✅ Memory usage                                                            │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ Redis Health Check                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│ Command:    redis-cli ping                                                  │
│ Interval:   Every 10 seconds                                                │
│ Timeout:    5 seconds                                                       │
│ Retries:    5 attempts                                                      │
└─────────────────────────────────────────────────────────────────────────────┘

Access Health Status:
  • Internal: http://localhost:8080/actuator/health
  • External: https://obus-partners.otapp.live/actuator/health
  • CLI:      curl https://obus-partners.otapp.live/actuator/health
  • Docker:   docker compose ps
```

---

## 🎯 Deployment Decision Tree

```
Are you deploying for the first time?
│
├─ YES ─→ Follow PHASE 1-4 in main diagram
│         (Setup everything from scratch)
│
└─ NO ──→ Already deployed? Just updating?
          │
          ├─ Automatic deployment configured?
          │  │
          │  ├─ YES ─→ Just push code!
          │  │         $ git push origin deploy
          │  │         GitHub Actions handles it
          │  │
          │  └─ NO ──→ Manual deployment:
          │            SSH to server
          │            $ ./deploy/quick-deploy.sh staging
          │
          └─ Need to change configuration?
             (Database, secrets, etc.)
             │
             └─→ 1. Edit .env.staging on server
                 2. Restart: docker compose restart app
```

---

## 🔄 Continuous Deployment Pipeline

```
╔════════════════════════════════════════════════════════════════════════════╗
║                    GITHUB ACTIONS PIPELINE                                  ║
╚════════════════════════════════════════════════════════════════════════════╝

Trigger: Push to 'deploy', 'main', or 'staging' branch

┌─────────────────────────────────────────────────────────────────────────────┐
│ JOB 1: Build and Push                                          [~5 minutes] │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1️⃣  Checkout code                                                          │
│      └─→ actions/checkout@v4                                               │
│                                                                             │
│  2️⃣  Set up Docker Buildx                                                   │
│      └─→ actions/setup-buildx-action@v3                                    │
│                                                                             │
│  3️⃣  Login to GHCR                                                          │
│      └─→ docker/login-action@v3                                            │
│          └─→ Using GITHUB_TOKEN                                            │
│                                                                             │
│  4️⃣  Extract metadata                                                       │
│      └─→ docker/metadata-action@v5                                         │
│          └─→ Generate tags: latest, branch-sha                             │
│                                                                             │
│  5️⃣  Build and Push Image                                                   │
│      └─→ docker/build-push-action@v5                                       │
│          ├─→ Multi-stage build                                             │
│          ├─→ Layer caching                                                 │
│          └─→ Push to ghcr.io                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                               ⬇️
┌─────────────────────────────────────────────────────────────────────────────┐
│ JOB 2: Deploy to Server                                        [~2 minutes] │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1️⃣  SSH to Server                                                          │
│      └─→ appleboy/ssh-action@v1.0.3                                        │
│          ├─→ Using SSH_PRIVATE_KEY secret                                  │
│          └─→ Connect to SERVER_HOST as SERVER_USER                         │
│                                                                             │
│  2️⃣  Execute Deployment Script                                              │
│      └─→ cd ~/obus-partners.otapp.live                                     │
│          ├─→ Login to GHCR                                                 │
│          ├─→ Pull latest image                                             │
│          ├─→ Stop old containers                                           │
│          ├─→ Start new containers                                          │
│          └─→ Verify health                                                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                               ⬇️
                          ✅ DEPLOYED!
```

---

**🎯 Ready to start? Open START_HERE.md and follow the steps!**

