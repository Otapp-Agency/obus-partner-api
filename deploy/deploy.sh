#!/bin/bash
set -e

PROFILE=${1:-prod}
IMAGE_TAG=${2:-latest}

echo "🚀 Deploying OBUS Partner API"
echo "   Profile: $PROFILE"
echo "   Image Tag: $IMAGE_TAG"
echo "   Domain: obus-partners.otapp.live"
echo ""

# Navigate to deployment directory
cd ~/obus-partners.otapp.live

# Check if .env file exists
if [ ! -f ".env.$PROFILE" ]; then
    echo "❌ Error: .env.$PROFILE not found!"
    echo "   Please create it from .env.$PROFILE.example"
    exit 1
fi

# Login to GHCR (if credentials are set)
if [ -n "$GHCR_TOKEN" ] && [ -n "$GHCR_USERNAME" ]; then
    echo "🔐 Logging in to GHCR..."
    echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USERNAME --password-stdin
fi

# Pull latest images
echo "📥 Pulling latest images..."
PROFILE=$PROFILE docker compose pull

# Stop and remove old containers
echo "🛑 Stopping old containers..."
docker compose down

# Start new containers
echo "🚀 Starting new containers..."
PROFILE=$PROFILE docker compose up -d

# Wait for health check
echo "⏳ Waiting for application to be healthy..."
sleep 10

# Check health
HEALTH_STATUS=$(curl -s http://localhost:8080/actuator/health | grep -o '"status":"[^"]*"' | cut -d'"' -f4 || echo "UNKNOWN")

if [ "$HEALTH_STATUS" = "UP" ]; then
    echo "✅ Application is healthy!"
else
    echo "⚠️  Health check returned: $HEALTH_STATUS"
    echo "📋 Showing recent logs:"
    docker compose logs --tail=50 app
fi

# Cleanup old images
echo "🧹 Cleaning up old images..."
docker image prune -f

echo ""
echo "✅ Deployment completed!"
echo "   Application: https://obus-partners.otapp.live"
echo "   Health: https://obus-partners.otapp.live/actuator/health"
echo ""
echo "📋 View logs: docker compose logs -f app"
echo "📊 Check status: docker compose ps"

