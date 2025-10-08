# 🎯 Quick Reference Card

## 📋 Deployment in 5 Steps

```
┌──────────────────────────────────────────────────────┐
│  STEP 1: Update Config (Local)           [2 min]    │
└──────────────────────────────────────────────────────┘
Edit docker-compose.yml line 5:
  image: ghcr.io/${GITHUB_REPOSITORY:-YOUR-USERNAME/obus-partner-api}:latest

git add . && git commit -m "deployment setup" && git push origin deploy

┌──────────────────────────────────────────────────────┐
│  STEP 2: Copy Files to Server            [2 min]    │
└──────────────────────────────────────────────────────┘
./copy-to-server.sh forge@obus-partners.otapp.live

┌──────────────────────────────────────────────────────┐
│  STEP 3: Server Setup (SSH)              [15 min]   │
└──────────────────────────────────────────────────────┘
ssh forge@obus-partners.otapp.live
cd ~/obus-partners.otapp.live
./deploy/initial-server-setup.sh
nano .env.staging  # Edit with your values
sudo ./deploy/setup-ssl.sh
echo TOKEN | docker login ghcr.io -u USERNAME --password-stdin
export GITHUB_REPOSITORY="USERNAME/obus-partner-api"

┌──────────────────────────────────────────────────────┐
│  STEP 4: Deploy                           [5 min]    │
└──────────────────────────────────────────────────────┘
./deploy/quick-deploy.sh staging

┌──────────────────────────────────────────────────────┐
│  STEP 5: Verify                           [2 min]    │
└──────────────────────────────────────────────────────┘
https://obus-partners.otapp.live/actuator/health
```

---

## 🔑 Key Commands

### Server Commands
```bash
# Deploy
./deploy/quick-deploy.sh staging

# Logs
docker compose logs -f app

# Status
docker compose ps

# Restart
docker compose restart app

# Health
curl http://localhost:8080/actuator/health
```

### Local Commands
```bash
# Copy files
./copy-to-server.sh forge@obus-partners.otapp.live

# Auto-deploy
git push origin deploy

# SSH
ssh forge@obus-partners.otapp.live
```

---

## 📚 Documentation

| Read This | When |
|-----------|------|
| **START_HERE.md** | **First time setup** |
| DEPLOYMENT_GUIDE.md | Need details |
| DEPLOYMENT_CHECKLIST.md | Track progress |
| DEPLOYMENT_SUMMARY.md | Quick overview |
| VISUAL_DEPLOYMENT_FLOW.md | See diagrams |

---

## 🔐 Secrets You Need

1. **GitHub Personal Access Token**
   - GitHub → Settings → Developer settings → Tokens
   - Permission: `read:packages`

2. **Database Credentials**
   - URL, username, password

3. **Security Secrets**
   ```bash
   # Generate with:
   openssl rand -base64 32
   ```
   - JWT_SECRET
   - API_KEY_ENCRYPTION_SECRET

4. **SSH Private Key**
   ```bash
   # View with:
   cat ~/.ssh/id_rsa
   ```

---

## 🌐 URLs

```
Application:  https://obus-partners.otapp.live
Health:       /actuator/health
API Docs:     /swagger-ui.html
```

---

## 🚨 Emergency Commands

```bash
# Can't start
docker compose logs app
docker compose down && docker compose up -d

# Can't pull
echo TOKEN | docker login ghcr.io -u USER --password-stdin

# Check env
docker compose exec app env | grep SPRING

# Restart all
docker compose restart

# Full reset
docker compose down
docker compose pull
docker compose up -d
```

---

## ✅ Checklist

- [ ] Updated docker-compose.yml with GitHub username
- [ ] Pushed code to GitHub
- [ ] Copied files to server
- [ ] Ran initial-server-setup.sh
- [ ] Edited .env.staging
- [ ] Setup SSL
- [ ] Logged in to GHCR
- [ ] Deployed with quick-deploy.sh
- [ ] Verified health check

---

## 📞 Support

**Application URL:** https://obus-partners.otapp.live  
**Server:** forge@obus-partners.otapp.live  
**Project Dir:** ~/obus-partners.otapp.live  

---

**For complete instructions, see START_HERE.md**

