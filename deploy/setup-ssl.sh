#!/bin/bash
set -e

DOMAIN="${1:-obus-partners.otapp.live}"
EMAIL="${2:-admin@otapp.live}"

echo "🔒 Setting up SSL certificate for $DOMAIN"

# Install certbot if not installed
if ! command -v certbot &> /dev/null; then
    echo "📦 Installing certbot..."
    sudo apt update
    sudo apt install -y certbot
fi

# Stop nginx if running
if docker ps | grep -q obus-nginx; then
    echo "🛑 Stopping nginx container..."
    docker stop obus-nginx
fi

# Get certificate
echo "📜 Requesting SSL certificate..."
sudo certbot certonly \
    --standalone \
    --non-interactive \
    --agree-tos \
    --email $EMAIL \
    -d $DOMAIN

# Create nginx ssl directory
mkdir -p ~/obus-partners.otapp.live/nginx/ssl

# Copy certificates
echo "📋 Copying certificates..."
sudo cp /etc/letsencrypt/live/$DOMAIN/fullchain.pem ~/obus-partners.otapp.live/nginx/ssl/
sudo cp /etc/letsencrypt/live/$DOMAIN/privkey.pem ~/obus-partners.otapp.live/nginx/ssl/

# Set permissions
sudo chown -R $USER:$USER ~/obus-partners.otapp.live/nginx/ssl/
chmod 600 ~/obus-partners.otapp.live/nginx/ssl/privkey.pem

echo "✅ SSL certificate setup completed!"
echo ""
echo "📝 Certificate will expire on: $(sudo certbot certificates | grep 'Expiry Date' | head -1)"
echo ""
echo "🔄 To renew certificates, run: sudo certbot renew"
echo "   (Or set up auto-renewal with cron)"

