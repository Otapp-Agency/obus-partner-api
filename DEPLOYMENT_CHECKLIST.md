# ✅ OBUS Partner API - Deployment Checklist

Print this and check off as you go!

---

## 📋 PRE-DEPLOYMENT (On Local Machine)

### Repository Setup
- [ ] Updated `docker-compose.yml` line 5 with my GitHub username
- [ ] Committed and pushed to GitHub (`git push origin deploy`)

### GitHub Configuration (For Auto-Deploy)
- [ ] Added `SERVER_HOST` secret in GitHub
- [ ] Added `SERVER_USER` secret in GitHub  
- [ ] Added `SSH_PRIVATE_KEY` secret in GitHub
- [ ] GitHub Actions workflow is present (`.github/workflows/deploy.yml`)

### Credentials Ready
- [ ] I have my GitHub Personal Access Token (with `read:packages`)
- [ ] I can SSH to `forge@obus-partners.otapp.live`
- [ ] I have database credentials ready
- [ ] I have generated JWT secret (using `openssl rand -base64 32`)
- [ ] I have generated API key encryption secret

---

## 📤 FILE TRANSFER (Local → Server)

### Copy Files to Server
- [ ] Ran `./copy-to-server.sh` successfully
  - OR manually copied: deploy/, nginx/, docker-compose.yml, Dockerfile, pom.xml

### Verify Files on Server
```bash
ssh forge@obus-partners.otapp.live
ls -la ~/obus-partners.otapp.live/
```

- [ ] `deploy/` directory exists
- [ ] `nginx/` directory exists  
- [ ] `docker-compose.yml` exists
- [ ] `Dockerfile` exists
- [ ] Scripts are executable (`deploy/*.sh`)

---

## 🔧 SERVER SETUP (On Server)

### Initial Setup
```bash
cd ~/obus-partners.otapp.live
./deploy/initial-server-setup.sh
```

- [ ] Ran initial setup script
- [ ] All directories created (logs, nginx/logs, nginx/ssl)
- [ ] Scripts are executable

### Docker Installation
- [ ] Docker is installed (`docker --version`)
- [ ] Docker Compose is installed (`docker compose version`)
- [ ] Docker service is running (`docker ps`)

### Environment Configuration
- [ ] Created `.env.staging` from template
- [ ] Updated `SPRING_DATASOURCE_URL` with real database
- [ ] Updated `SPRING_DATASOURCE_USERNAME`
- [ ] Updated `SPRING_DATASOURCE_PASSWORD`
- [ ] Updated `JWT_SECRET` with generated value
- [ ] Updated `API_KEY_ENCRYPTION_SECRET` with generated value
- [ ] Saved the file

### SSL Certificate
```bash
sudo ./deploy/setup-ssl.sh
```

- [ ] Certbot is installed
- [ ] SSL certificate generated for `obus-partners.otapp.live`
- [ ] Certificates copied to `nginx/ssl/fullchain.pem`
- [ ] Certificates copied to `nginx/ssl/privkey.pem`
- [ ] Certificate permissions are correct (owned by forge)

### GitHub Container Registry
```bash
echo TOKEN | docker login ghcr.io -u USERNAME --password-stdin
```

- [ ] Generated GitHub Personal Access Token
- [ ] Logged in to GHCR successfully
- [ ] Can see "Login Succeeded" message

### Environment Variables
```bash
export GITHUB_REPOSITORY="username/obus-partner-api"
export PROFILE=staging
```

- [ ] Set `GITHUB_REPOSITORY` variable
- [ ] Set `PROFILE` variable
- [ ] Added to `~/.bashrc` for persistence
- [ ] Verified with `echo $GITHUB_REPOSITORY`

---

## 🚀 DEPLOYMENT (On Server)

### Deploy Application
```bash
cd ~/obus-partners.otapp.live
./deploy/quick-deploy.sh staging
```

- [ ] Pulled Docker images successfully
- [ ] Containers started (app, redis, nginx)
- [ ] No errors in deployment script output

### Verify Deployment
```bash
docker compose ps
```

- [ ] `obus-partner-api` container is Up (healthy)
- [ ] `obus-redis` container is Up (healthy)
- [ ] `obus-nginx` container is Up

### Check Application Logs
```bash
docker compose logs app
```

- [ ] No ERROR messages in logs
- [ ] Application started successfully
- [ ] Can see "Started ObusPartnersApiApplication"

---

## ✅ TESTING (Verification)

### Local Health Check (from server)
```bash
curl http://localhost:8080/actuator/health
```

- [ ] Returns `{"status":"UP"}`
- [ ] Response is quick (< 2 seconds)

### Public Health Check (from browser or local machine)
```
https://obus-partners.otapp.live/actuator/health
```

- [ ] Can access via HTTPS (SSL working)
- [ ] Returns `{"status":"UP"}`
- [ ] No certificate warnings

### API Documentation
```
https://obus-partners.otapp.live/swagger-ui.html
```

- [ ] Swagger UI loads
- [ ] Can see API endpoints
- [ ] No 502/503 errors

### Test Key Endpoints
- [ ] Can access main application URL
- [ ] Login endpoint works (if applicable)
- [ ] Database connection successful
- [ ] Redis connection successful

---

## 📊 POST-DEPLOYMENT

### Monitoring Setup
- [ ] Bookmarked health check URL
- [ ] Know how to view logs: `docker compose logs -f app`
- [ ] Know how to check status: `docker compose ps`

### Documentation
- [ ] Saved database credentials securely
- [ ] Saved JWT secrets securely
- [ ] Saved GitHub token securely
- [ ] Documented deployment date and version

### SSL Certificate Renewal
- [ ] Verified certbot auto-renewal is configured
- [ ] Tested renewal: `sudo certbot renew --dry-run`

### Backup Plan
- [ ] Know how to rollback: `docker compose pull && docker compose up -d`
- [ ] Have database backup procedure
- [ ] Can access Redis data if needed

---

## 🎉 DEPLOYMENT COMPLETE!

### Final Verification Checklist
- [ ] Application accessible at: https://obus-partners.otapp.live
- [ ] Health check passing
- [ ] SSL certificate valid (no warnings)
- [ ] All containers running and healthy
- [ ] No errors in application logs
- [ ] Can test API endpoints successfully

### URLs to Save
```
Application:  https://obus-partners.otapp.live
Health Check: https://obus-partners.otapp.live/actuator/health
API Docs:     https://obus-partners.otapp.live/swagger-ui.html
```

### Next Steps
- [ ] Test all major API endpoints
- [ ] Run integration tests (if available)
- [ ] Monitor logs for first hour
- [ ] Notify team of successful deployment
- [ ] Update documentation with production URLs

---

## 🔄 FOR FUTURE DEPLOYMENTS

### Automated (if GitHub Actions configured)
- [ ] Just push to deploy branch: `git push origin deploy`
- [ ] Monitor GitHub Actions for build status
- [ ] Verify deployment on server

### Manual
- [ ] SSH to server: `ssh forge@obus-partners.otapp.live`
- [ ] Navigate: `cd ~/obus-partners.otapp.live`
- [ ] Deploy: `./deploy/quick-deploy.sh staging`
- [ ] Verify: Check health endpoint

---

## 📞 Emergency Contacts & Commands

### Quick Commands
```bash
# View logs
docker compose logs -f app

# Restart
docker compose restart app

# Full restart
docker compose down && docker compose up -d

# Check status
docker compose ps

# Health check
curl http://localhost:8080/actuator/health
```

### If Something Goes Wrong
1. Check logs: `docker compose logs app`
2. Check container status: `docker compose ps`
3. Restart application: `docker compose restart app`
4. Check environment: `docker compose exec app env | grep SPRING`
5. Full restart: `docker compose down && docker compose up -d`

---

**Deployment Date:** _____________

**Deployed By:** _____________

**Version/Commit:** _____________

**Notes:**
_____________________________________________
_____________________________________________
_____________________________________________

---

✅ **Congratulations! Your deployment is complete!**

Keep this checklist for future reference and for deploying to production.

