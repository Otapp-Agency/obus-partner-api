#!/bin/bash
set -e

# Configuration
GITHUB_ORG="your-org"  # Change this to your GitHub org/username
REPO_NAME="obus-partner-api"
IMAGE_NAME="ghcr.io/$GITHUB_ORG/$REPO_NAME"

echo "🚀 Manual Deployment Script for OBUS Partner API (Java 21)"
echo "=================================================="
echo ""

# Check if GHCR credentials are set
if [ -z "$GHCR_TOKEN" ] || [ -z "$GHCR_USERNAME" ]; then
    echo "❌ Error: GHCR credentials not set!"
    echo "   Please set the following environment variables:"
    echo "   export GHCR_USERNAME=your-github-username"
    echo "   export GHCR_TOKEN=your-github-personal-access-token"
    exit 1
fi

# Build with Maven
echo "📦 Building application with Maven (Java 21)..."
./mvnw clean package -DskipTests

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t $IMAGE_NAME:latest .

# Login to GHCR
echo "🔐 Logging in to GitHub Container Registry..."
echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USERNAME --password-stdin

# Push to GHCR
echo "📤 Pushing image to GHCR..."
docker push $IMAGE_NAME:latest

echo ""
echo "✅ Image pushed successfully to: $IMAGE_NAME:latest"
echo ""
echo "📋 Next steps:"
echo "   1. SSH to your server: ssh forge@obus-partners.otapp.live"
echo "   2. Navigate to deployment directory: cd ~/obus-partners.otapp.live"
echo "   3. Pull and deploy: ./deploy/deploy.sh prod"
echo ""
echo "Or run this command directly:"
echo "   ssh forge@obus-partners.otapp.live 'cd ~/obus-partners.otapp.live && ./deploy/deploy.sh prod'"

