#!/bin/bash
# Quick Deploy Script for OBUS Partner API
# Usage: ./quick-deploy.sh [staging|prod]

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROFILE=${1:-staging}

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         OBUS Partner API - Quick Deploy                   ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Profile:${NC} $PROFILE"
echo -e "${YELLOW}Domain:${NC} obus-partners.otapp.live"
echo ""

# Check if running on server
if [ ! -d ~/obus-partners.otapp.live ]; then
    echo -e "${RED}❌ Error: Must run on server in ~/obus-partners.otapp.live${NC}"
    exit 1
fi

cd ~/obus-partners.otapp.live

# Check if environment file exists
if [ ! -f ".env.$PROFILE" ]; then
    echo -e "${RED}❌ Error: .env.$PROFILE not found!${NC}"
    echo -e "${YELLOW}   Create it from: deploy/env.$PROFILE.template${NC}"
    exit 1
fi

# Check if logged in to GHCR
if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}❌ Error: Docker is not running!${NC}"
    exit 1
fi

echo -e "${BLUE}📥 Pulling latest images...${NC}"
export PROFILE=$PROFILE
docker compose pull

echo ""
echo -e "${BLUE}🛑 Stopping old containers...${NC}"
docker compose down

echo ""
echo -e "${BLUE}🚀 Starting new containers...${NC}"
docker compose up -d

echo ""
echo -e "${BLUE}⏳ Waiting for application to start...${NC}"
sleep 15

echo ""
echo -e "${BLUE}📊 Container Status:${NC}"
docker compose ps

echo ""
echo -e "${BLUE}🔍 Checking application health...${NC}"
sleep 5

# Check health
HEALTH_STATUS=$(curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -o '"status":"[^"]*"' | cut -d'"' -f4 || echo "UNAVAILABLE")

if [ "$HEALTH_STATUS" = "UP" ]; then
    echo -e "${GREEN}✅ Application is healthy!${NC}"
else
    echo -e "${YELLOW}⚠️  Health check returned: $HEALTH_STATUS${NC}"
    echo -e "${YELLOW}📋 Showing recent logs:${NC}"
    docker compose logs --tail=30 app
fi

echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║         ✅ Deployment Completed!                           ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}🌐 Application:${NC} https://obus-partners.otapp.live"
echo -e "${BLUE}❤️  Health:${NC}      https://obus-partners.otapp.live/actuator/health"
echo -e "${BLUE}📚 API Docs:${NC}    https://obus-partners.otapp.live/swagger-ui.html"
echo ""
echo -e "${BLUE}Useful Commands:${NC}"
echo -e "  ${YELLOW}View logs:${NC}        docker compose logs -f app"
echo -e "  ${YELLOW}Check status:${NC}     docker compose ps"
echo -e "  ${YELLOW}Restart:${NC}          docker compose restart app"
echo -e "  ${YELLOW}Shell access:${NC}     docker compose exec app sh"
echo ""

