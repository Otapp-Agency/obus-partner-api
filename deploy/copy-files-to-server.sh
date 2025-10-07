#!/bin/bash
set -e

# Configuration
SERVER_USER="forge"
SERVER_HOST="otapp-pgo-staging"
REMOTE_DIR="~/obus-partners.otapp.live"

echo "📤 Copying deployment files to server"
echo "======================================"
echo "Server: $SERVER_USER@$SERVER_HOST"
echo "Remote directory: $REMOTE_DIR"
echo ""

# Check if server is reachable
echo "🔍 Checking server connectivity..."
if ! ssh -o ConnectTimeout=5 "$SERVER_USER@$SERVER_HOST" "echo 'Connected'" 2>/dev/null; then
    echo "❌ Error: Cannot connect to server"
    echo "   Make sure you can SSH to: $SERVER_USER@$SERVER_HOST"
    exit 1
fi

echo "✅ Server is reachable"
echo ""

# Create remote directory structure
echo "📁 Creating remote directory structure..."
ssh "$SERVER_USER@$SERVER_HOST" "mkdir -p $REMOTE_DIR/{logs,nginx/{logs,ssl},deploy}"

echo "✅ Remote directories created"
echo ""

# Copy deployment files
echo "📤 Copying files..."

# Copy docker-compose.yml
echo "   → docker-compose.yml"
scp docker-compose.yml "$SERVER_USER@$SERVER_HOST:$REMOTE_DIR/"

# Copy Dockerfile
echo "   → Dockerfile"
scp Dockerfile "$SERVER_USER@$SERVER_HOST:$REMOTE_DIR/"

# Copy .dockerignore
echo "   → .dockerignore"
scp .dockerignore "$SERVER_USER@$SERVER_HOST:$REMOTE_DIR/"

# Copy nginx configuration
echo "   → nginx/nginx.conf"
scp nginx/nginx.conf "$SERVER_USER@$SERVER_HOST:$REMOTE_DIR/nginx/"

# Copy deploy directory
echo "   → deploy/"
scp -r deploy/* "$SERVER_USER@$SERVER_HOST:$REMOTE_DIR/deploy/"

# Make scripts executable on server
echo ""
echo "🔐 Setting permissions on server..."
ssh "$SERVER_USER@$SERVER_HOST" "chmod +x $REMOTE_DIR/deploy/*.sh"

echo ""
echo "✅ All files copied successfully!"
echo ""
echo "📋 Files on server:"
ssh "$SERVER_USER@$SERVER_HOST" "ls -lah $REMOTE_DIR"

echo ""
echo "📝 Next steps (run on server):"
echo "   ssh $SERVER_USER@$SERVER_HOST"
echo "   cd $REMOTE_DIR"
echo "   ./deploy/setup-environment.sh"

