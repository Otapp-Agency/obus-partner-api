# OBUS Partner API - Deployment Guide

## Prerequisites

### On Your Development Machine:
- Java 21
- Maven 3.9+
- Docker
- GitHub account with GHCR access

### On Production Server:
- Docker & Docker Compose
- Nginx (or use Docker nginx)
- SSL certificate (Let's Encrypt)
- PostgreSQL database (if not using Docker)
- Redis (included in docker-compose)

## Deployment Strategies

### Strategy 1: Automatic Deployment (GitHub Actions) - Recommended

1. **Setup GitHub Secrets** (Repository Settings → Secrets and variables → Actions):
   ```
   SERVER_HOST=obus-partners.otapp.live
   SERVER_USER=forge
   SSH_PRIVATE_KEY=<your-private-key>
   ```

2. **Push to main or staging branch**:
   ```bash
   git push origin main
   # or
   git push origin staging
   ```

3. **GitHub Actions will automatically**:
   - Build the application with Java 21
   - Create Docker image
   - Push to GHCR
   - Deploy to server

### Strategy 2: Manual Deployment

1. **Build and push locally**:
   ```bash
   # Set credentials
   export GHCR_USERNAME=your-github-username
   export GHCR_TOKEN=your-personal-access-token
   
   # Run deployment script
   ./deploy-manual.sh
   ```

2. **Deploy on server**:
   ```bash
   ssh forge@obus-partners.otapp.live
   cd ~/obus-partners.otapp.live
   ./deploy/deploy.sh prod
   ```

## Initial Server Setup

### 1. Setup SSL Certificate
```bash
# On server
cd ~/obus-partners.otapp.live
./deploy/setup-ssl.sh
```

### 2. Configure Environment Variables
```bash
# Copy example files
cp .env.prod.example .env.prod
cp .env.staging.example .env.staging

# Edit with your values
nano .env.prod
```

### 3. Setup GHCR Authentication (Optional for auto-pull)
```bash
# On server, create ~/.bashrc or ~/.profile entry
export GHCR_USERNAME=your-github-username
export GHCR_TOKEN=your-personal-access-token

# Login once
echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USERNAME --password-stdin
```

### 4. Create required directories
```bash
mkdir -p ~/obus-partners.otapp.live/logs
mkdir -p ~/obus-partners.otapp.live/nginx/logs
mkdir -p ~/obus-partners.otapp.live/nginx/ssl
```

## Deployment Commands

### Deploy Production
```bash
./deploy/deploy.sh prod
```

### Deploy Staging
```bash
./deploy/deploy.sh staging
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

### Check Status
```bash
docker compose ps
```

### Restart Services
```bash
# Restart all
docker compose restart

# Restart just app
docker compose restart app
```

### Update and Redeploy
```bash
docker compose pull
docker compose up -d --force-recreate
```

## Health Checks

- **Application Health**: https://obus-partners.otapp.live/actuator/health
- **Application Info**: https://obus-partners.otapp.live/actuator/info
- **API Documentation**: https://obus-partners.otapp.live/swagger-ui.html

## Monitoring

### Check Container Stats
```bash
docker stats
```

### Check Logs
```bash
# Application logs
tail -f ~/obus-partners.otapp.live/logs/application.log

# Nginx access logs
tail -f ~/obus-partners.otapp.live/nginx/logs/access.log

# Nginx error logs
tail -f ~/obus-partners.otapp.live/nginx/logs/error.log
```

## Troubleshooting

### Container won't start
```bash
# Check logs
docker compose logs app

# Check configuration
docker compose config

# Rebuild and restart
docker compose up -d --build --force-recreate
```

### SSL Certificate Issues
```bash
# Check certificate
sudo certbot certificates

# Renew certificate
sudo certbot renew

# Re-run setup
./deploy/setup-ssl.sh
```

### Database Connection Issues
```bash
# Check environment variables
docker compose exec app env | grep SPRING_DATASOURCE

# Test database connection
docker compose exec app wget --spider http://localhost:8080/actuator/health
```

### Redis Connection Issues
```bash
# Check Redis
docker compose exec redis redis-cli ping

# Restart Redis
docker compose restart redis
```

## Rollback

### Rollback to Previous Version
```bash
# Pull specific version
docker pull ghcr.io/your-org/obus-partner-api:<previous-tag>

# Update docker-compose.yml to use specific tag
# Then redeploy
docker compose up -d --force-recreate
```

## Maintenance

### SSL Certificate Renewal (Auto-renewal with cron)
```bash
# Add to crontab
crontab -e

# Add this line (runs daily at 2 AM)
0 2 * * * sudo certbot renew --quiet && sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/*.pem ~/obus-partners.otapp.live/nginx/ssl/ && docker compose restart nginx
```

### Cleanup Old Docker Images
```bash
docker image prune -a -f
```

### Backup Redis Data
```bash
docker compose exec redis redis-cli BGSAVE
docker cp obus-redis:/data/dump.rdb ./backup-$(date +%Y%m%d).rdb
```

## Performance Tuning

### Java 21 JVM Options (already configured)
- Z Garbage Collector (ZGC) with Generational mode
- Container-aware memory settings
- 75% max RAM usage
- String deduplication

### Adjust Memory Limits
Edit `docker-compose.yml`:
```yaml
deploy:
  resources:
    limits:
      memory: 4G  # Adjust as needed
    reservations:
      memory: 2G
```

## Security Checklist

- [ ] SSL certificate configured and auto-renewing
- [ ] Environment variables secured (not in git)
- [ ] Database credentials rotated regularly
- [ ] API keys encrypted
- [ ] CORS properly configured
- [ ] Security headers enabled (in nginx)
- [ ] Non-root user in Docker container
- [ ] Firewall configured (only 80, 443 open)

## Support

For issues or questions:
- Check logs: `docker compose logs -f`
- Health check: https://obus-partners.otapp.live/actuator/health
- Contact: admin@otapp.live

