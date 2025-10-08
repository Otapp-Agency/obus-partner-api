#!/bin/bash
# Initial Server Setup for OBUS Partner API
# Run this ONCE on the server after copying files

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║      OBUS Partner API - Initial Server Setup              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check if running on server
if [ ! -d ~/obus-partners.otapp.live ]; then
    echo -e "${RED}❌ Error: This script should run on server in ~/obus-partners.otapp.live${NC}"
    exit 1
fi

cd ~/obus-partners.otapp.live

# Step 1: Create directories
echo -e "${BLUE}📁 Step 1: Creating directories...${NC}"
mkdir -p logs
mkdir -p nginx/logs
mkdir -p nginx/ssl
echo -e "${GREEN}✅ Directories created${NC}"
echo ""

# Step 2: Make scripts executable
echo -e "${BLUE}🔧 Step 2: Making scripts executable...${NC}"
chmod +x deploy/*.sh
echo -e "${GREEN}✅ Scripts are now executable${NC}"
echo ""

# Step 3: Check environment files
echo -e "${BLUE}📝 Step 3: Checking environment files...${NC}"
if [ ! -f ".env.staging" ]; then
    if [ -f "deploy/env.staging.template" ]; then
        cp deploy/env.staging.template .env.staging
        echo -e "${YELLOW}⚠️  Created .env.staging from template${NC}"
        echo -e "${YELLOW}   You MUST edit this file with your actual values!${NC}"
    else
        echo -e "${RED}❌ Template file not found: deploy/env.staging.template${NC}"
    fi
else
    echo -e "${GREEN}✅ .env.staging exists${NC}"
fi

if [ ! -f ".env.prod" ]; then
    if [ -f "deploy/env.prod.template" ]; then
        cp deploy/env.prod.template .env.prod
        echo -e "${YELLOW}⚠️  Created .env.prod from template${NC}"
        echo -e "${YELLOW}   You MUST edit this file with your actual values!${NC}"
    else
        echo -e "${YELLOW}⚠️  Template file not found: deploy/env.prod.template${NC}"
    fi
else
    echo -e "${GREEN}✅ .env.prod exists${NC}"
fi
echo ""

# Step 4: Check Docker
echo -e "${BLUE}🐳 Step 4: Checking Docker installation...${NC}"
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    echo -e "${GREEN}✅ Docker installed: $DOCKER_VERSION${NC}"
    
    if command -v docker compose &> /dev/null; then
        COMPOSE_VERSION=$(docker compose version)
        echo -e "${GREEN}✅ Docker Compose installed: $COMPOSE_VERSION${NC}"
    else
        echo -e "${RED}❌ Docker Compose not found!${NC}"
        echo -e "${YELLOW}   Install with: sudo apt install docker-compose-plugin${NC}"
    fi
else
    echo -e "${RED}❌ Docker not found!${NC}"
    echo -e "${YELLOW}   Install Docker first${NC}"
    exit 1
fi
echo ""

# Step 5: Check if certbot is installed
echo -e "${BLUE}🔒 Step 5: Checking SSL setup...${NC}"
if command -v certbot &> /dev/null; then
    echo -e "${GREEN}✅ Certbot is installed${NC}"
    
    # Check if certificate exists
    if sudo test -d /etc/letsencrypt/live/obus-partners.otapp.live; then
        echo -e "${GREEN}✅ SSL certificate exists for obus-partners.otapp.live${NC}"
        
        # Check if certificates are copied to nginx
        if [ -f "nginx/ssl/fullchain.pem" ] && [ -f "nginx/ssl/privkey.pem" ]; then
            echo -e "${GREEN}✅ SSL certificates copied to nginx/ssl${NC}"
        else
            echo -e "${YELLOW}⚠️  Copying certificates to nginx/ssl...${NC}"
            sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/fullchain.pem nginx/ssl/ 2>/dev/null || true
            sudo cp /etc/letsencrypt/live/obus-partners.otapp.live/privkey.pem nginx/ssl/ 2>/dev/null || true
            sudo chown $USER:$USER nginx/ssl/*.pem 2>/dev/null || true
            echo -e "${GREEN}✅ Certificates copied${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  SSL certificate not found${NC}"
        echo -e "${YELLOW}   Run: sudo ./deploy/setup-ssl.sh${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  Certbot not installed${NC}"
    echo -e "${YELLOW}   Install with: sudo apt update && sudo apt install certbot -y${NC}"
fi
echo ""

# Step 6: Check GitHub Container Registry login
echo -e "${BLUE}🔐 Step 6: Checking GHCR authentication...${NC}"
if docker info 2>/dev/null | grep -q "ghcr.io"; then
    echo -e "${GREEN}✅ Logged in to GitHub Container Registry${NC}"
else
    echo -e "${YELLOW}⚠️  Not logged in to GHCR${NC}"
    echo -e "${YELLOW}   Login with:${NC}"
    echo -e "${YELLOW}   echo YOUR_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin${NC}"
fi
echo ""

# Step 7: Set environment variables
echo -e "${BLUE}🔧 Step 7: Setting up environment variables...${NC}"
if ! grep -q "GITHUB_REPOSITORY" ~/.bashrc 2>/dev/null; then
    echo ""
    echo -e "${YELLOW}📝 Enter your GitHub repository (e.g., username/obus-partner-api):${NC}"
    read -p "Repository: " REPO
    
    if [ ! -z "$REPO" ]; then
        echo "" >> ~/.bashrc
        echo "# OBUS Partner API" >> ~/.bashrc
        echo "export GITHUB_REPOSITORY=\"$REPO\"" >> ~/.bashrc
        echo "export PROFILE=staging" >> ~/.bashrc
        echo -e "${GREEN}✅ Environment variables added to ~/.bashrc${NC}"
        export GITHUB_REPOSITORY="$REPO"
        export PROFILE=staging
    else
        echo -e "${YELLOW}⚠️  Skipped - you can add manually later${NC}"
    fi
else
    echo -e "${GREEN}✅ GITHUB_REPOSITORY already set in ~/.bashrc${NC}"
fi
echo ""

# Summary
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║         ✅ Initial Setup Complete!                         ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}Next Steps:${NC}"
echo ""
echo -e "${YELLOW}1.${NC} Edit environment files with your actual values:"
echo -e "   ${BLUE}nano .env.staging${NC}"
echo -e "   ${BLUE}nano .env.prod${NC}"
echo ""
echo -e "${YELLOW}2.${NC} Setup SSL certificate (if not done):"
echo -e "   ${BLUE}sudo ./deploy/setup-ssl.sh${NC}"
echo ""
echo -e "${YELLOW}3.${NC} Login to GitHub Container Registry:"
echo -e "   ${BLUE}echo YOUR_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin${NC}"
echo ""
echo -e "${YELLOW}4.${NC} Deploy the application:"
echo -e "   ${BLUE}./deploy/quick-deploy.sh staging${NC}"
echo ""
echo -e "${GREEN}For detailed guide, see: DEPLOYMENT_GUIDE.md${NC}"
echo ""

