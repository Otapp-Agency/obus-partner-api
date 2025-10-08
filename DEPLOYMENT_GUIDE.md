# 🚀 OBUS Partner API - Complete Deployment Guide

## 📋 Deployment Strategy Overview

```
Local Machine              GitHub                Server
    │                         │                    │
    ├─────────────────────────┤                    │
    │   1. Push Code          │                    │
    │                         │                    │
    │                    ┌────▼────┐              │
    │                    │  Build  │              │
    │                    │  Image  │              │
    │                    └────┬────┘              │
    │                         │                    │
    │                    ┌────▼────┐              │
    │                    │  Push   │              │
    │                    │  GHCR   │              │
    │                    └────┬────┘              │
    │                         │                    │
    │                         ├────────────────────┤
    │                         │  2. Deploy         │
    │                         │                    │
    │                         │               ┌────▼────┐
    │                         │               │  Pull   │
    │                         │               │  Image  │
    │                         │               └────┬────┘
    │                         │                    │
    │                         │               ┌────▼────┐
    │                         │               │  Start  │
    │                         │               │ Docker  │
    │                         │               └─────────┘
```

---

## ✅ Prerequisites Checklist

### On Your Local Machine:
- [ ] Git installed
- [ ] Docker installed (optional, for local testing)
- [ ] SSH access to server
- [ ] GitHub account with repository access

### On Server (forge@otapp-pgo-staging):
- [ ] Docker installed
- [ ] Docker Compose installed
- [ ] Domain pointing to server (obus-partners.otapp.live)
- [ ] Ports 80, 443 open in firewall

---

## 🎯 STEP-BY-STEP DEPLOYMENT

### **PHASE 1: Prepare GitHub & Automated Build** (One-time setup)

#### Step 1.1: Configure GitHub Repository Settings

**Action:** Add GitHub Secrets for server deployment

1. Go to your GitHub repository
2. Navigate to: **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"** and add:

```
Name: SERVER_HOST
Value: obus-partners.otapp.live
```

```
Name: SERVER_USER
Value: forge
```

```
Name: SSH_PRIVATE_KEY
Value: [Paste your private SSH key - the one that can access forge@server]
```

**How to get your SSH key:**
```bash
# On your local machine
cat ~/.ssh/id_rsa
# Copy the ENTIRE output including
# -----BEGIN OPENSSH PRIVATE KEY-----
# and
# -----END OPENSSH PRIVATE KEY-----
```

#### Step 1.2: Update Repository Name in docker-compose.yml

**Action:** Edit line 5 of `docker-compose.yml` if needed

Current value:
```yaml
image: ghcr.io/${GITHUB_REPOSITORY:-obuspartners/obus-partner-api}:latest
```

Change `obuspartners/obus-partner-api` to match your **GitHub username/repo-name**

For example, if your GitHub is `myusername/obus-partner-api`:
```yaml
image: ghcr.io/${GITHUB_REPOSITORY:-myusername/obus-partner-api}:latest
```

#### Step 1.3: Commit and Push GitHub Actions Workflow

```bash
git add .github/workflows/deploy.yml docker-compose.yml
git commit -m "feat: add automated deployment with GitHub Actions"
git push origin deploy
```

✅ **Result:** GitHub Actions will automatically:
- Build Docker image with Java 21
- Push to GitHub Container Registry (GHCR)
- Deploy to your server

---

### **PHASE 2: Prepare Server Environment** (One-time setup)

#### Step 2.1: Copy Required Files to Server

**From your local machine:**

```bash
# Copy deployment files
scp -r deploy nginx docker-compose.yml Dockerfile forge@obus-partners.otapp.live:~/obus-partners.otapp.live/

# Copy application source (needed for pom.xml)
scp pom.xml forge@obus-partners.otapp.live:~/obus-partners.otapp.live/
```

#### Step 2.2: SSH to Server and Setup Directories

```bash
# SSH to server
ssh forge@obus-partners.otapp.live

# Navigate to project directory
cd ~/obus-partners.otapp.live

# Create required directories
mkdir -p logs
mkdir -p nginx/logs
mkdir -p nginx/ssl

# Make scripts executable
chmod +x deploy/*.sh
```

#### Step 2.3: Create Environment Configuration

```bash
# Still on server
cd ~/obus-partners.otapp.live

# Copy template to create staging environment file
cp deploy/env.staging.template .env.staging

# Edit with your actual values
nano .env.staging
```

**📝 Edit these critical values in `.env.staging`:**

```bash
# Database - CHANGE THESE!
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_DB_HOST:5432/YOUR_DB_NAME
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# Security - GENERATE NEW SECRETS!
JWT_SECRET=your-random-jwt-secret-here-use-long-string
API_KEY_ENCRYPTION_SECRET=your-random-encryption-secret-here

# Redis (keep as is if using docker redis)
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

**💡 To generate strong secrets:**
```bash
# Run these commands to generate random secrets
openssl rand -base64 32  # For JWT_SECRET
openssl rand -base64 32  # For API_KEY_ENCRYPTION_SECRET
```

Save the file (Ctrl+X, Y, Enter)

#### Step 2.4: Setup SSL Certificate

```bash
# Still on server
cd ~/obus-partners.otapp.live

# Run SSL setup script
sudo ./deploy/setup-ssl.sh
```

**📝 The script will:**
- Install certbot
- Generate SSL certificate for obus-partners.otapp.live
- Copy certificates to nginx/ssl directory
- Setup auto-renewal

**If the script doesn't exist or fails, manual SSL setup:**
```bash
# Install certbot
sudo apt update
sudo apt install certbot -y

# Stop nginx if running
docker compose stop nginx 2>/dev/null || true

# Get certificate
sudo certbot certonly --standalone \
  -d obus-partners.otapp.live \
  --agree-tos \
  -m your-email@example.com \
  --non-interactive

# Copy certificates
sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/fullchain.pem ~/obus-partners.otapp.live/nginx/ssl/
sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/privkey.pem ~/obus-partners.otapp.live/nginx/ssl/

# Fix permissions
sudo chown forge:forge ~/obus-partners.otapp.live/nginx/ssl/*.pem
```

---

### **PHASE 3: Deploy Application**

#### Step 3.1: Login to GitHub Container Registry (on server)

```bash
# On server
cd ~/obus-partners.otapp.live

# Login to GHCR (replace with YOUR GitHub username and token)
# You need a GitHub Personal Access Token with 'read:packages' permission
echo YOUR_GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

**💡 How to create GitHub Personal Access Token:**
1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token
3. Check: `read:packages` permission
4. Copy the token (it will be shown only once!)

#### Step 3.2: Set Environment Variable for Docker Compose

```bash
# On server
export GITHUB_REPOSITORY="your-github-username/obus-partner-api"
export PROFILE=staging

# You can add these to ~/.bashrc to make them permanent
echo 'export GITHUB_REPOSITORY="your-github-username/obus-partner-api"' >> ~/.bashrc
echo 'export PROFILE=staging' >> ~/.bashrc
```

#### Step 3.3: Deploy!

```bash
# On server
cd ~/obus-partners.otapp.live

# Pull latest images
PROFILE=staging docker compose pull

# Start all services
PROFILE=staging docker compose up -d

# Check status
docker compose ps

# View logs
docker compose logs -f app
```

**✅ You should see:**
```
NAME                IMAGE                                              STATUS
obus-partner-api    ghcr.io/username/obus-partner-api:latest          Up (healthy)
obus-redis          redis:7-alpine                                     Up (healthy)
obus-nginx          nginx:alpine                                       Up
```

#### Step 3.4: Verify Deployment

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Or from your browser:
https://obus-partners.otapp.live/actuator/health
```

**✅ Expected response:**
```json
{
  "status": "UP"
}
```

---

## 🔄 Future Deployments (After Initial Setup)

### Method 1: Automatic (Recommended)

Just push to the `deploy` branch:

```bash
# On your local machine
git add .
git commit -m "your changes"
git push origin deploy
```

GitHub Actions will automatically build and deploy! 🎉

### Method 2: Manual Deployment

If you need to deploy manually:

```bash
# On your local machine - build and push
docker build -t ghcr.io/username/obus-partner-api:latest .
echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USERNAME --password-stdin
docker push ghcr.io/username/obus-partner-api:latest

# On server - deploy
ssh forge@obus-partners.otapp.live
cd ~/obus-partners.otapp.live
PROFILE=staging docker compose pull
docker compose up -d --force-recreate
```

---

## 📊 Monitoring & Maintenance

### View Logs
```bash
# All services
docker compose logs -f

# Just application
docker compose logs -f app

# Last 100 lines
docker compose logs --tail=100 app
```

### Check Container Status
```bash
docker compose ps
docker stats
```

### Restart Services
```bash
# Restart all
docker compose restart

# Restart just app
docker compose restart app
```

### Update Application
```bash
cd ~/obus-partners.otapp.live
PROFILE=staging docker compose pull
docker compose up -d --force-recreate
```

### Check SSL Certificate
```bash
sudo certbot certificates
```

### Renew SSL Certificate (auto-renews, but manual if needed)
```bash
sudo certbot renew
sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/*.pem ~/obus-partners.otapp.live/nginx/ssl/
docker compose restart nginx
```

---

## 🚨 Troubleshooting

### Problem: Container won't start

```bash
# Check logs
docker compose logs app

# Check configuration
docker compose config

# Try recreating
docker compose down
docker compose up -d --force-recreate
```

### Problem: Can't pull image from GHCR

```bash
# Make sure you're logged in
echo YOUR_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin

# Make sure image name is correct
echo $GITHUB_REPOSITORY

# Try pulling manually
docker pull ghcr.io/${GITHUB_REPOSITORY}:latest
```

### Problem: SSL certificate issues

```bash
# Check if certificate exists
sudo ls -l /etc/letsencrypt/live/obus-partners.otapp.live/

# Re-run setup
sudo ./deploy/setup-ssl.sh

# Or get certificate manually
sudo certbot certonly --standalone -d obus-partners.otapp.live
```

### Problem: Application health check fails

```bash
# Check application logs
docker compose logs --tail=100 app

# Check if database is accessible
docker compose exec app env | grep SPRING_DATASOURCE

# Check Redis connection
docker compose exec redis redis-cli ping
```

### Problem: Nginx 502 Bad Gateway

```bash
# Check if app container is running
docker compose ps app

# Check app health
docker compose exec app wget -O- http://localhost:8080/actuator/health

# Restart nginx
docker compose restart nginx
```

---

## 🎯 Quick Reference Commands

```bash
# Deploy new version
cd ~/obus-partners.otapp.live && PROFILE=staging docker compose pull && docker compose up -d

# View logs
docker compose logs -f app

# Restart
docker compose restart app

# Check status
docker compose ps

# Shell into container
docker compose exec app sh

# Check health
curl https://obus-partners.otapp.live/actuator/health

# Cleanup old images
docker image prune -f
```

---

## 📞 Support

- **Health Check:** https://obus-partners.otapp.live/actuator/health
- **API Docs:** https://obus-partners.otapp.live/swagger-ui.html
- **Logs Location:** `~/obus-partners.otapp.live/logs/`

---

## ✅ Deployment Complete!

Your application should now be running at:
🌐 **https://obus-partners.otapp.live**

**Next Steps:**
1. Test the API endpoints
2. Monitor logs for any errors
3. Setup monitoring/alerts (optional)
4. Create backup strategy for Redis data (optional)

🎉 **Congratulations! Your OBUS Partner API is deployed!**

