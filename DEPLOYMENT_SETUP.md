# 🚀 OBUS Partner API - Quick Deployment Guide

## ✅ Files Created

I've set up complete deployment configuration for your Java 21 application:

### Docker & Container Files
- ✅ `Dockerfile` - Optimized multi-stage build for Java 21
- ✅ `docker-compose.yml` - Complete stack (App + Redis + Nginx)
- ✅ `.dockerignore` - Optimize build context

### CI/CD
- ✅ `.github/workflows/deploy.yml` - GitHub Actions for auto-deployment

### Nginx Configuration
- ✅ `nginx/nginx.conf` - Production-ready with SSL, compression, security headers

### Deployment Scripts
- ✅ `deploy-manual.sh` - Manual build and push to GHCR
- ✅ `deploy/deploy.sh` - Server-side deployment script
- ✅ `deploy/setup-ssl.sh` - SSL certificate setup with Let's Encrypt

### Configuration Templates
- ✅ `deploy/env.prod.template` - Production environment variables
- ✅ `deploy/env.staging.template` - Staging environment variables
- ✅ `deploy/README.md` - Comprehensive deployment documentation

## 📋 Quick Start - Choose Your Method

### Method 1: Automatic Deployment (Recommended)

**1. Setup GitHub Secrets**
Go to: Repository → Settings → Secrets and variables → Actions

Add these secrets:
```
SERVER_HOST=obus-partners.otapp.live
SERVER_USER=forge
SSH_PRIVATE_KEY=<paste your private SSH key>
```

**2. Update docker-compose.yml**
Edit line 6 in `docker-compose.yml`:
```yaml
image: ghcr.io/YOUR-GITHUB-USERNAME/obus-partner-api:latest
```

**3. Push to trigger deployment**
```bash
git add .
git commit -m "feat: add deployment configuration"
git push origin main  # or staging
```

GitHub Actions will automatically build and deploy!

### Method 2: Manual Deployment

**1. Update deploy-manual.sh**
Edit lines 4-5:
```bash
GITHUB_ORG="your-github-username"  # Change this
REPO_NAME="obus-partner-api"
```

**2. Set GHCR credentials**
```bash
export GHCR_USERNAME=your-github-username
export GHCR_TOKEN=ghp_your_personal_access_token_here
```

**3. Build and push**
```bash
./deploy-manual.sh
```

**4. Deploy on server**
```bash
ssh forge@obus-partners.otapp.live
# Then on server:
cd ~/obus-partners.otapp.live
./deploy/deploy.sh prod
```

## 🔧 Server Setup (One-time)

### 1. Copy files to server
```bash
scp -r deploy docker-compose.yml forge@obus-partners.otapp.live:~/obus-partners.otapp.live/
```

### 2. SSH to server and setup
```bash
ssh forge@obus-partners.otapp.live

# Create directories
mkdir -p ~/obus-partners.otapp.live/{logs,nginx/{logs,ssl},deploy}

# Setup SSL certificate
cd ~/obus-partners.otapp.live
./deploy/setup-ssl.sh

# Create environment file
cp deploy/env.prod.template .env.prod
nano .env.prod  # Edit with your actual values

# Make scripts executable
chmod +x deploy/*.sh
```

### 3. Configure environment variables
Edit `.env.prod` with your actual values:
- Database credentials
- JWT secret
- API keys
- etc.

## 🎯 Deployment Commands

### Deploy to Production
```bash
./deploy/deploy.sh prod
```

### Deploy to Staging
```bash
./deploy/deploy.sh staging
```

### View Logs
```bash
docker compose logs -f app
```

### Check Status
```bash
docker compose ps
```

## 🔍 Health Checks

After deployment, verify:
- 🌐 **Website**: https://obus-partners.otapp.live
- ❤️ **Health**: https://obus-partners.otapp.live/actuator/health
- 📚 **API Docs**: https://obus-partners.otapp.live/swagger-ui.html

## 📝 Next Steps

1. **Update GitHub org/username** in:
   - `docker-compose.yml` (line 6)
   - `deploy-manual.sh` (line 4)

2. **Add GitHub Secrets** (if using auto-deployment):
   - `SERVER_HOST`
   - `SERVER_USER`
   - `SSH_PRIVATE_KEY`

3. **Configure environment** on server:
   - Copy `deploy/env.prod.template` to `.env.prod`
   - Update with your database, secrets, etc.

4. **Setup SSL certificate**:
   ```bash
   ./deploy/setup-ssl.sh
   ```

5. **Deploy**:
   ```bash
   # Either push to GitHub (auto-deploy)
   git push origin main
   
   # Or manual deploy
   ./deploy-manual.sh
   ```

## 🆘 Troubleshooting

### Image pull fails
```bash
# Login to GHCR on server
echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USERNAME --password-stdin
```

### Container won't start
```bash
docker compose logs app
docker compose config  # Validate configuration
```

### SSL issues
```bash
sudo certbot certificates  # Check certificate
./deploy/setup-ssl.sh  # Re-run setup
```

## 📚 Full Documentation

See `deploy/README.md` for complete documentation including:
- Monitoring
- Rollback procedures
- Maintenance tasks
- Performance tuning
- Security checklist

## 🎉 You're all set!

Your Java 21 application is ready to deploy to `obus-partners.otapp.live` with:
- ✅ GitHub Container Registry (GHCR)
- ✅ Docker Compose orchestration
- ✅ Nginx reverse proxy with SSL
- ✅ Redis caching
- ✅ Auto-deployment via GitHub Actions
- ✅ Health checks and monitoring
- ✅ Production-ready JVM optimizations (ZGC)

