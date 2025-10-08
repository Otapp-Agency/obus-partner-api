# 🚀 START HERE - OBUS Partner API Deployment

## 📋 What We're Doing

Deploy your Java 21 Spring Boot application to `obus-partners.otapp.live` using:
- ✅ **Docker** - Containerized application
- ✅ **GitHub Container Registry (GHCR)** - Store Docker images
- ✅ **Nginx** - Reverse proxy with SSL
- ✅ **GitHub Actions** - Automated deployment

---

## 🎯 Quick Strategy Summary

```
┌─────────────┐        ┌──────────────┐        ┌─────────────┐
│   Local     │        │   GitHub     │        │   Server    │
│  Computer   │───────▶│   (GHCR)     │───────▶│  (Deploy)   │
└─────────────┘        └──────────────┘        └─────────────┘
     Push code           Build & Store           Pull & Run
```

---

## ⚡ QUICK START (Choose Your Path)

### Path A: Automated Deployment (Recommended) ⭐

**Time: 30 minutes**

1. **Configure GitHub** (5 min)
2. **Push code** (1 min) 
3. **Setup server** (20 min)
4. **Done!** Future deploys = just push code

### Path B: Manual Deployment

**Time: 45 minutes**

1. **Build locally** (10 min)
2. **Push to GHCR** (5 min)
3. **Setup server** (20 min)
4. **Deploy** (10 min)

---

## 📝 STEP-BY-STEP INSTRUCTIONS

### STEP 1: Prepare Your Local Repository (5 minutes)

#### 1.1 Update GitHub repository name in docker-compose.yml

Open `docker-compose.yml` and find line 5:

```yaml
image: ghcr.io/${GITHUB_REPOSITORY:-obuspartners/obus-partner-api}:latest
```

Change `obuspartners/obus-partner-api` to **your GitHub username/repo**:

```yaml
image: ghcr.io/${GITHUB_REPOSITORY:-YOUR-USERNAME/obus-partner-api}:latest
```

**Example:** If your GitHub is `johndoe/my-obus-api`:
```yaml
image: ghcr.io/${GITHUB_REPOSITORY:-johndoe/my-obus-api}:latest
```

#### 1.2 Commit and push the deployment files

```bash
git add .
git commit -m "feat: add deployment configuration"
git push origin deploy
```

---

### STEP 2: Configure GitHub (5 minutes) - FOR AUTOMATED DEPLOYMENT ONLY

If you want automated deployment on every push:

#### 2.1 Go to your GitHub repository

Navigate to: **Settings** → **Secrets and variables** → **Actions**

#### 2.2 Add these 3 secrets:

Click **"New repository secret"** for each:

**Secret 1:**
```
Name: SERVER_HOST
Value: obus-partners.otapp.live
```

**Secret 2:**
```
Name: SERVER_USER  
Value: forge
```

**Secret 3:**
```
Name: SSH_PRIVATE_KEY
Value: [Your SSH private key]
```

**To get your SSH private key:**

**On Windows (Git Bash):**
```bash
cat ~/.ssh/id_rsa
```

**On Linux/Mac:**
```bash
cat ~/.ssh/id_rsa
```

Copy the **ENTIRE** output including:
```
-----BEGIN OPENSSH PRIVATE KEY-----
...everything...
-----END OPENSSH PRIVATE KEY-----
```

✅ **Done!** GitHub Actions will now auto-deploy when you push.

---

### STEP 3: Copy Files to Server (5 minutes)

#### 3.1 Run the copy script

**On your local machine:**

```bash
chmod +x copy-to-server.sh
./copy-to-server.sh forge@obus-partners.otapp.live
```

**Or manually copy:**

```bash
scp -r deploy nginx docker-compose.yml Dockerfile pom.xml forge@obus-partners.otapp.live:~/obus-partners.otapp.live/
```

---

### STEP 4: Setup Server Environment (20 minutes)

#### 4.1 SSH to server

```bash
ssh forge@obus-partners.otapp.live
```

#### 4.2 Run initial setup script

```bash
cd ~/obus-partners.otapp.live
./deploy/initial-server-setup.sh
```

**This will:**
- ✅ Create required directories
- ✅ Make scripts executable  
- ✅ Create environment file templates
- ✅ Check Docker installation
- ✅ Check SSL setup

#### 4.3 Edit environment file

```bash
nano .env.staging
```

**REQUIRED: Update these values:**

```bash
# Database - CHANGE THESE!
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_DB_HOST:5432/YOUR_DB_NAME
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_secure_password

# Security - GENERATE NEW SECRETS!
JWT_SECRET=paste-random-string-here
API_KEY_ENCRYPTION_SECRET=paste-random-string-here
```

**Generate secure secrets:**
```bash
openssl rand -base64 32
```

Run this twice and use each output for JWT_SECRET and API_KEY_ENCRYPTION_SECRET.

**Save:** `Ctrl+X`, then `Y`, then `Enter`

#### 4.4 Setup SSL Certificate

```bash
sudo ./deploy/setup-ssl.sh
```

**If the script doesn't exist or fails, manual setup:**

```bash
# Install certbot
sudo apt update && sudo apt install certbot -y

# Stop nginx temporarily
docker compose stop nginx 2>/dev/null || true

# Get certificate (replace YOUR-EMAIL)
sudo certbot certonly --standalone \
  -d obus-partners.otapp.live \
  --agree-tos \
  -m YOUR-EMAIL@example.com \
  --non-interactive

# Copy certificates
sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/fullchain.pem ~/obus-partners.otapp.live/nginx/ssl/
sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/privkey.pem ~/obus-partners.otapp.live/nginx/ssl/
sudo chown forge:forge ~/obus-partners.otapp.live/nginx/ssl/*.pem
```

#### 4.5 Login to GitHub Container Registry

You need a **GitHub Personal Access Token** with `read:packages` permission.

**Create token:**
1. GitHub → **Settings** (your profile, top right)
2. **Developer settings** (bottom left)
3. **Personal access tokens** → **Tokens (classic)**
4. **Generate new token (classic)**
5. Check: `read:packages` (and `write:packages` if building locally)
6. Copy the token (**save it somewhere safe!**)

**Login on server:**
```bash
echo YOUR_TOKEN_HERE | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

**Example:**
```bash
echo ghp_abc123xyz789 | docker login ghcr.io -u johndoe --password-stdin
```

#### 4.6 Set environment variables

```bash
# Set your GitHub repository
export GITHUB_REPOSITORY="YOUR-USERNAME/obus-partner-api"
export PROFILE=staging

# Make them permanent
echo 'export GITHUB_REPOSITORY="YOUR-USERNAME/obus-partner-api"' >> ~/.bashrc
echo 'export PROFILE=staging' >> ~/.bashrc
```

**Example:**
```bash
export GITHUB_REPOSITORY="johndoe/obus-partner-api"
```

---

### STEP 5: Deploy! (5 minutes)

#### 5.1 Deploy the application

```bash
cd ~/obus-partners.otapp.live
./deploy/quick-deploy.sh staging
```

#### 5.2 Check if it's running

```bash
# Check containers
docker compose ps

# View logs
docker compose logs -f app
```

**Press `Ctrl+C` to exit logs**

#### 5.3 Test the deployment

**From server:**
```bash
curl http://localhost:8080/actuator/health
```

**From your browser:**
```
https://obus-partners.otapp.live/actuator/health
```

**Expected response:**
```json
{"status":"UP"}
```

---

## ✅ Success! What's Next?

### Your Application is Live at:
- 🌐 **Main URL:** https://obus-partners.otapp.live
- ❤️ **Health Check:** https://obus-partners.otapp.live/actuator/health
- 📚 **API Docs:** https://obus-partners.otapp.live/swagger-ui.html

### For Future Deployments:

**If you set up GitHub Actions (Path A):**
```bash
# Just push your code!
git add .
git commit -m "your changes"
git push origin deploy
```

GitHub will automatically build and deploy! 🎉

**If you're deploying manually (Path B):**
```bash
# On server
ssh forge@obus-partners.otapp.live
cd ~/obus-partners.otapp.live
./deploy/quick-deploy.sh staging
```

---

## 📊 Useful Commands (On Server)

```bash
# View live logs
docker compose logs -f app

# Check status
docker compose ps

# Restart application
docker compose restart app

# Check health
curl http://localhost:8080/actuator/health

# Update and redeploy
docker compose pull
docker compose up -d --force-recreate

# Shell into container
docker compose exec app sh
```

---

## 🚨 Troubleshooting

### Problem: Can't pull Docker image

```bash
# Make sure you're logged in
echo YOUR_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin

# Check environment variable
echo $GITHUB_REPOSITORY

# Try pulling manually
docker pull ghcr.io/${GITHUB_REPOSITORY}:latest
```

### Problem: Container won't start

```bash
# Check logs
docker compose logs app

# Check configuration
docker compose config

# Restart
docker compose down
docker compose up -d
```

### Problem: Health check fails

```bash
# Check application logs
docker compose logs --tail=100 app

# Check database connection
docker compose exec app env | grep SPRING_DATASOURCE

# Check Redis
docker compose exec redis redis-cli ping
```

### Problem: 502 Bad Gateway from Nginx

```bash
# Check if app is running
docker compose ps

# Restart nginx
docker compose restart nginx

# Check nginx logs
docker compose logs nginx
```

---

## 📚 More Information

- **Detailed Guide:** See `DEPLOYMENT_GUIDE.md`
- **Deployment Scripts:** See `deploy/README.md`
- **Quick Deploy:** `./deploy/quick-deploy.sh staging`

---

## 📞 Quick Reference

### Files You Modified:
- ✅ `.github/workflows/deploy.yml` - GitHub Actions
- ✅ `docker-compose.yml` - Docker orchestration
- ✅ `deploy/initial-server-setup.sh` - Server setup
- ✅ `deploy/quick-deploy.sh` - Quick deployment
- ✅ `copy-to-server.sh` - Copy files to server

### Server Files You Need to Create:
- `.env.staging` - Environment variables for staging
- `.env.prod` - Environment variables for production
- `nginx/ssl/fullchain.pem` - SSL certificate
- `nginx/ssl/privkey.pem` - SSL private key

---

## 🎉 You're All Set!

Your OBUS Partner API is ready to deploy!

**Summary:**
1. ✅ Update docker-compose.yml with your GitHub username
2. ✅ Add GitHub secrets (for auto-deploy)
3. ✅ Copy files to server
4. ✅ Run setup script on server
5. ✅ Edit .env.staging with your values
6. ✅ Setup SSL certificate
7. ✅ Login to GHCR
8. ✅ Deploy!

**Any questions? Check DEPLOYMENT_GUIDE.md for detailed information.**

---

**Made with ❤️ for OTAPP**

