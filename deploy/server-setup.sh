#!/bin/bash
set -e

echo "🔧 Setting up OBUS Partner API server environment"
echo "=================================================="
echo ""

# Check if running on server
if [ ! -d "/home/forge" ]; then
    echo "⚠️  Warning: This script should be run on the server (forge user)"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Define base directory
BASE_DIR="${HOME}/obus-partners.otapp.live"

echo "📁 Creating directory structure at: $BASE_DIR"

# Create all required directories
mkdir -p "$BASE_DIR"/{logs,nginx/{logs,ssl},deploy}

echo "✅ Directory structure created:"
tree -L 2 "$BASE_DIR" 2>/dev/null || ls -la "$BASE_DIR"

echo ""
echo "📋 Directory structure:"
echo "   $BASE_DIR/"
echo "   ├── logs/              (Application logs)"
echo "   ├── nginx/"
echo "   │   ├── logs/          (Nginx access/error logs)"
echo "   │   └── ssl/           (SSL certificates)"
echo "   └── deploy/            (Deployment scripts)"

# Set proper permissions
chmod -R 755 "$BASE_DIR"

echo ""
echo "✅ Server setup completed!"
echo ""
echo "📝 Next steps:"
echo "   1. Clone/copy deployment files to this directory"
echo "   2. Create .env.prod from template"
echo "   3. Setup SSL certificate"
echo "   4. Deploy application"

