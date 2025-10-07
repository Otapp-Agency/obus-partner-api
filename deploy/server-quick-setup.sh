#!/bin/bash
# OBUS Partner API - Server Quick Setup Script
# Run this on: forge@otapp-pgo-staging
# Usage: bash <(curl -s URL) OR copy-paste directly

set -e

echo "🚀 OBUS Partner API - Quick Server Setup"
echo "========================================="
echo ""

# Configuration
BASE_DIR="${HOME}/obus-partners.otapp.live"
PROFILE="${1:-prod}"

echo "📁 Creating directory structure..."
mkdir -p "${BASE_DIR}"/{logs,nginx/{logs,ssl},deploy}
cd "${BASE_DIR}"

echo "✅ Directories created at: ${BASE_DIR}"
echo ""

echo "📝 Creating environment file template..."
cat > .env.${PROFILE} << 'ENVFILE'
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/obus_partner_db
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
SERVER_PORT=8080
SERVER_COMPRESSION_ENABLED=true
JWT_SECRET=CHANGE_THIS_TO_LONG_RANDOM_SECRET
API_KEY_ENCRYPTION_SECRET=CHANGE_THIS_TO_LONG_RANDOM_SECRET
CORS_ALLOWED_ORIGINS=https://obus-partners.otapp.live
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_OBUSPARTNERS=INFO
LOGGING_FILE_NAME=/app/logs/application.log
ENVFILE

chmod 600 .env.${PROFILE}
echo "✅ Created .env.${PROFILE} (secured with 600 permissions)"
echo ""

echo "🐳 Checking Docker installation..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Installing..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker ${USER}
    rm get-docker.sh
    echo "✅ Docker installed"
    echo "⚠️  Please log out and log back in for Docker group changes"
else
    echo "✅ Docker already installed"
fi

if ! command -v docker compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose not available"
    echo "   Please install Docker Compose plugin"
else
    echo "✅ Docker Compose available"
fi
echo ""

echo "✅ Setup completed!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 NEXT STEPS:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1️⃣  COPY FILES from your local machine:"
echo "   Run this on your LOCAL MACHINE:"
echo ""
echo "   cd D:\\My_Works\\OTAPP\\BUS\\obus-partner-api"
echo "   scp docker-compose.yml forge@otapp-pgo-staging:~/obus-partners.otapp.live/"
echo "   scp Dockerfile forge@otapp-pgo-staging:~/obus-partners.otapp.live/"
echo "   scp .dockerignore forge@otapp-pgo-staging:~/obus-partners.otapp.live/"
echo "   scp nginx/nginx.conf forge@otapp-pgo-staging:~/obus-partners.otapp.live/nginx/"
echo "   scp -r deploy/* forge@otapp-pgo-staging:~/obus-partners.otapp.live/deploy/"
echo ""
echo "2️⃣  CONFIGURE ENVIRONMENT:"
echo "   nano ${BASE_DIR}/.env.${PROFILE}"
echo "   (Update database credentials, JWT secret, etc.)"
echo ""
echo "3️⃣  SETUP SSL CERTIFICATE:"
echo "   chmod +x ${BASE_DIR}/deploy/setup-ssl.sh"
echo "   sudo ${BASE_DIR}/deploy/setup-ssl.sh obus-partners.otapp.live admin@otapp.live"
echo ""
echo "4️⃣  LOGIN TO GITHUB CONTAINER REGISTRY:"
echo "   export GHCR_USERNAME=your-github-username"
echo "   export GHCR_TOKEN=ghp_your_token"
echo "   echo \$GHCR_TOKEN | docker login ghcr.io -u \$GHCR_USERNAME --password-stdin"
echo ""
echo "5️⃣  DEPLOY APPLICATION:"
echo "   chmod +x ${BASE_DIR}/deploy/deploy.sh"
echo "   ${BASE_DIR}/deploy/deploy.sh ${PROFILE}"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📁 Current directory structure:"
tree -L 2 "${BASE_DIR}" 2>/dev/null || ls -la "${BASE_DIR}"
echo ""
echo "🎉 Server is ready for deployment!"

