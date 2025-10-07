# Server Configuration

## Server Details
- **SSH Host**: `forge@otapp-pgo-staging`
- **Domain**: `obus-partners.otapp.live`
- **Deployment Directory**: `~/obus-partners.otapp.live`

## Quick Commands

### Connect to Server
```bash
ssh forge@otapp-pgo-staging
```

### Copy Files to Server
```bash
./deploy/copy-files-to-server.sh
```

### Complete Server Setup
```bash
# SSH to server first
ssh forge@otapp-pgo-staging

# Then run setup
cd ~/obus-partners.otapp.live
./deploy/complete-server-setup.sh prod admin@otapp.live
```

### Deploy Application
```bash
ssh forge@otapp-pgo-staging
cd ~/obus-partners.otapp.live
./deploy/deploy.sh prod
```

## GitHub Secrets Configuration

Add these to your GitHub repository secrets:

| Secret Name | Value |
|------------|-------|
| `SERVER_HOST` | `otapp-pgo-staging` |
| `SERVER_USER` | `forge` |
| `SSH_PRIVATE_KEY` | Your SSH private key |

## SSL Certificate Setup

The SSL certificate is for domain: `obus-partners.otapp.live`

```bash
# On server
./deploy/setup-ssl.sh obus-partners.otapp.live admin@otapp.live
```

## Notes

- Server hostname: `otapp-pgo-staging`
- Public domain: `obus-partners.otapp.live`  
- Make sure DNS points `obus-partners.otapp.live` → `otapp-pgo-staging`

