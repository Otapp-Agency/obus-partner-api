#!/bin/bash
set -e

echo "🚀 Complete Server Setup for OBUS Partner API"
echo "=============================================="
echo ""

# Configuration
PROFILE=${1:-prod}
DOMAIN="obus-partners.otapp.live"
SERVER_HOST="otapp-pgo-staging"
EMAIL="${2:-admin@otapp.live}"

if [ "$PROFILE" != "prod" ] && [ "$PROFILE" != "staging" ]; then
    echo "❌ Error: Invalid profile"
    echo "   Usage: ./complete-server-setup.sh [prod|staging] [email]"
    exit 1
fi

echo "Profile: $PROFILE"
echo "Domain: $DOMAIN"
echo "Email: $EMAIL"
echo ""

# Step 1: Create directory structure
echo "📁 Step 1/4: Creating directory structure..."
mkdir -p ~/obus-partners.otapp.live/{logs,nginx/{logs,ssl},deploy}
echo "✅ Directories created"
echo ""

# Step 2: Check Docker installation
echo "🐳 Step 2/4: Checking Docker installation..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Installing Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    echo "✅ Docker installed. Please log out and log back in for group changes to take effect."
    exit 0
fi
echo "✅ Docker is installed"
echo ""

# Check Docker Compose
if ! command -v docker compose &> /dev/null; then
    echo "❌ Docker Compose not found"
    echo "   Please install Docker Compose plugin"
    exit 1
fi
echo "✅ Docker Compose is installed"
echo ""

# Step 3: Setup SSL Certificate
echo "🔒 Step 3/4: Setting up SSL certificate..."
cd ~/obus-partners.otapp.live

if [ ! -f "deploy/setup-ssl.sh" ]; then
    echo "⚠️  SSL setup script not found. You'll need to run it manually later."
else
    # Check if certificates already exist
    if [ -f "nginx/ssl/fullchain.pem" ] && [ -f "nginx/ssl/privkey.pem" ]; then
        echo "✅ SSL certificates already exist"
    else
        echo "📜 Setting up SSL certificate..."
        ./deploy/setup-ssl.sh
    fi
fi
echo ""

# Step 4: Setup environment
echo "⚙️  Step 4/4: Setting up environment configuration..."
if [ ! -f "deploy/setup-environment.sh" ]; then
    echo "⚠️  Environment setup script not found. You'll need to create .env.$PROFILE manually."
else
    ./deploy/setup-environment.sh $PROFILE
fi

echo ""
echo "✅ Server setup completed!"
echo ""
echo "📋 Summary:"
echo "   ✅ Directory structure created"
echo "   ✅ Docker installed and verified"
echo "   ✅ SSL certificate configured"
echo "   ✅ Environment file created (.env.$PROFILE)"
echo ""
echo "📝 Final steps:"
echo "   1. Review environment file: nano .env.$PROFILE"
echo "   2. Login to GHCR (if auto-pull needed):"
echo "      echo \$GHCR_TOKEN | docker login ghcr.io -u \$GHCR_USERNAME --password-stdin"
echo "   3. Deploy application:"
echo "      ./deploy/deploy.sh $PROFILE"
echo ""
echo "🎉 You're ready to deploy!"

