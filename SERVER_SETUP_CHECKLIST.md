# 🚀 Server Setup Checklist

## Prerequisites
✅ Docker image built and pushed to GHCR
✅ Deployment files copied to server

## Step-by-Step Server Configuration

### 1️⃣ SSH to Server
```bash
ssh forge@otapp-pgo-staging
cd ~/obus-partners.otapp.live
```

---

### 2️⃣ Create Environment Configuration (.env.prod)

```bash
# Create .env.prod file
cat > .env.prod << 'EOF'
SPRING_PROFILES_ACTIVE=prod

# Database Configuration - UPDATE THESE!
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/obus_partner_db
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_secure_password
SPRING_JPA_HIBERNATE_DDL_AUTO=validate

# Redis Configuration
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379

# Application Configuration
SERVER_PORT=8080
SERVER_COMPRESSION_ENABLED=true

# Security - GENERATE NEW SECRETS!
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
API_KEY_ENCRYPTION_SECRET=$(openssl rand -base64 64 | tr -d '\n')

# CORS
CORS_ALLOWED_ORIGINS=https://obus-partners.otapp.live

# Actuator
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_OBUSPARTNERS=INFO
LOGGING_FILE_NAME=/app/logs/application.log
EOF

# Secure the file
chmod 600 .env.prod

# Edit with your actual database credentials
nano .env.prod
```

**Required Updates in .env.prod:**
- ✏️ `SPRING_DATASOURCE_URL` - Your PostgreSQL database URL
- ✏️ `SPRING_DATASOURCE_USERNAME` - Your database username
- ✏️ `SPRING_DATASOURCE_PASSWORD` - Your database password
- ✏️ Update any other environment-specific values

---

### 3️⃣ Login to GitHub Container Registry (GHCR)

```bash
# Set your GitHub credentials
export GHCR_USERNAME=your-github-username
export GHCR_TOKEN=ghp_your_personal_access_token

# Login to GHCR
echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USERNAME --password-stdin
```

**To create a GitHub Personal Access Token:**
1. Go to: https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Select scopes: `read:packages`
4. Copy the token (starts with `ghp_`)

**Make credentials persistent** (optional):
```bash
# Add to ~/.bashrc
echo 'export GHCR_USERNAME=your-github-username' >> ~/.bashrc
echo 'export GHCR_TOKEN=ghp_your_token' >> ~/.bashrc
source ~/.bashrc
```

---

### 4️⃣ Setup SSL Certificate (For HTTPS)

```bash
# Install certbot
sudo apt update
sudo apt install -y certbot

# Stop nginx if running
docker compose stop nginx 2>/dev/null || true

# Get SSL certificate
sudo certbot certonly \
    --standalone \
    --non-interactive \
    --agree-tos \
    --email admin@otapp.live \
    -d obus-partners.otapp.live

# Copy certificates to nginx directory
sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/fullchain.pem ~/obus-partners.otapp.live/nginx/ssl/
sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/privkey.pem ~/obus-partners.otapp.live/nginx/ssl/

# Set ownership
sudo chown -R forge:forge ~/obus-partners.otapp.live/nginx/ssl/
chmod 600 ~/obus-partners.otapp.live/nginx/ssl/privkey.pem
```

---

### 5️⃣ Update Docker Compose Image URL

```bash
cd ~/obus-partners.otapp.live

# Edit docker-compose.yml
nano docker-compose.yml
```

**Update line 5-6 to:**
```yaml
services:
  app:
    image: ghcr.io/otapp-agency/obus-partner-api:deploy  # or :latest for main branch
```

---

### 6️⃣ Pull and Start Services

```bash
cd ~/obus-partners.otapp.live

# Pull latest images from GHCR
docker compose pull

# Start all services
docker compose up -d

# Check status
docker compose ps
```

---

### 7️⃣ Verify Deployment

```bash
# Check container status
docker compose ps

# View logs
docker compose logs -f app

# Check health (inside container)
docker compose exec app curl -s http://localhost:8080/actuator/health

# Check health from outside (if nginx is running)
curl http://localhost/actuator/health
# or
curl https://obus-partners.otapp.live/actuator/health
```

---

## 🔧 Configuration Files Checklist

### ✅ Files You Should Have on Server:

```
~/obus-partners.otapp.live/
├── docker-compose.yml        ✅ (from GitHub/scp)
├── Dockerfile                 ✅ (optional, for reference)
├── .env.prod                  ⚠️  CREATE THIS!
├── nginx/
│   ├── nginx.conf            ✅ (from GitHub/scp)
│   ├── ssl/
│   │   ├── fullchain.pem     ⚠️  SETUP SSL!
│   │   └── privkey.pem       ⚠️  SETUP SSL!
│   └── logs/                 ✅ (created automatically)
├── logs/                      ✅ (created automatically)
└── deploy/
    ├── deploy.sh             ✅ (from GitHub/scp)
    └── *.sh                  ✅ (other scripts)
```

---

## 🎯 Quick Start Commands (Copy-Paste)

**Run all at once:**

```bash
# 1. Navigate to deployment directory
cd ~/obus-partners.otapp.live

# 2. Check if .env.prod exists
if [ ! -f .env.prod ]; then
    echo "❌ .env.prod not found! Create it first."
    exit 1
fi

# 3. Login to GHCR (replace with your credentials)
export GHCR_USERNAME=your-github-username
export GHCR_TOKEN=ghp_your_token
echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USERNAME --password-stdin

# 4. Pull latest image
docker compose pull

# 5. Start services
docker compose up -d

# 6. Wait for startup
sleep 15

# 7. Check status
docker compose ps
docker compose logs --tail=50 app

# 8. Test health
curl http://localhost:8080/actuator/health
```

---

## 📋 Deployment Commands

### Start Services
```bash
cd ~/obus-partners.otapp.live
docker compose up -d
```

### Stop Services
```bash
docker compose down
```

### View Logs
```bash
# All services
docker compose logs -f

# Just application
docker compose logs -f app

# Last 100 lines
docker compose logs --tail=100 app
```

### Restart Services
```bash
docker compose restart
# or specific service
docker compose restart app
```

### Update Application
```bash
# Pull latest image
docker compose pull

# Recreate containers
docker compose up -d --force-recreate

# Or use deploy script
./deploy/deploy.sh prod
```

### Check Status
```bash
docker compose ps
docker stats --no-stream
```

---

## 🔍 Troubleshooting

### Container Won't Start
```bash
# Check logs
docker compose logs app

# Check environment variables
docker compose exec app env | grep SPRING

# Validate docker-compose.yml
docker compose config
```

### Database Connection Error
```bash
# Test database connection from server
psql -h your-db-host -U your_db_user -d obus_partner_db

# Check .env.prod values
cat .env.prod | grep DATASOURCE
```

### Can't Pull Image from GHCR
```bash
# Re-login
echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USERNAME --password-stdin

# Manually pull
docker pull ghcr.io/otapp-agency/obus-partner-api:deploy
```

### Port Already in Use
```bash
# Check what's using port 8080
sudo lsof -i :8080
sudo netstat -tulpn | grep 8080

# Kill process or change port in .env.prod
```

---

## 🌐 DNS Configuration

Make sure your DNS points to the server:

```
Domain: obus-partners.otapp.live
Type: A Record
Points to: [Your server IP]
TTL: 3600
```

---

## ✅ Final Verification Checklist

- [ ] `.env.prod` created with correct database credentials
- [ ] GHCR login successful
- [ ] Docker images pulled successfully
- [ ] Containers started (docker compose ps shows "Up")
- [ ] Application health check passes
- [ ] Nginx configured and running (if using)
- [ ] SSL certificate installed (if using HTTPS)
- [ ] Can access: http://localhost:8080/actuator/health
- [ ] Can access: https://obus-partners.otapp.live (if DNS configured)

---

## 🆘 Need Help?

**Check application status:**
```bash
curl http://localhost:8080/actuator/health
```

**View recent logs:**
```bash
docker compose logs --tail=100 app
```

**Restart everything:**
```bash
docker compose down && docker compose up -d
```

**Connect to container:**
```bash
docker compose exec app sh
```

