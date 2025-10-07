#!/bin/bash
set -e

echo "⚙️  Setting up environment configuration"
echo "========================================"
echo ""

# Get profile (default: prod)
PROFILE=${1:-prod}

if [ "$PROFILE" != "prod" ] && [ "$PROFILE" != "staging" ]; then
    echo "❌ Error: Invalid profile. Use 'prod' or 'staging'"
    echo "   Usage: ./setup-environment.sh [prod|staging]"
    exit 1
fi

echo "📝 Setting up $PROFILE environment"
echo ""

# Check if template exists
if [ ! -f "deploy/env.$PROFILE.template" ]; then
    echo "❌ Error: Template file not found: deploy/env.$PROFILE.template"
    exit 1
fi

# Create .env file from template
ENV_FILE=".env.$PROFILE"

if [ -f "$ENV_FILE" ]; then
    echo "⚠️  $ENV_FILE already exists!"
    read -p "Overwrite? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Cancelled"
        exit 1
    fi
    cp "$ENV_FILE" "$ENV_FILE.backup.$(date +%Y%m%d_%H%M%S)"
    echo "📋 Backup created: $ENV_FILE.backup.*"
fi

# Copy template
cp "deploy/env.$PROFILE.template" "$ENV_FILE"
echo "✅ Created $ENV_FILE from template"
echo ""

# Prompt for required values
echo "📝 Please provide the following configuration values:"
echo "(Press Enter to keep template value)"
echo ""

# Database Configuration
read -p "Database URL [jdbc:postgresql://...]: " DB_URL
if [ ! -z "$DB_URL" ]; then
    sed -i "s|SPRING_DATASOURCE_URL=.*|SPRING_DATASOURCE_URL=$DB_URL|g" "$ENV_FILE"
fi

read -p "Database Username: " DB_USER
if [ ! -z "$DB_USER" ]; then
    sed -i "s|SPRING_DATASOURCE_USERNAME=.*|SPRING_DATASOURCE_USERNAME=$DB_USER|g" "$ENV_FILE"
fi

read -s -p "Database Password: " DB_PASS
echo
if [ ! -z "$DB_PASS" ]; then
    sed -i "s|SPRING_DATASOURCE_PASSWORD=.*|SPRING_DATASOURCE_PASSWORD=$DB_PASS|g" "$ENV_FILE"
fi

# JWT Secret
echo ""
read -s -p "JWT Secret (or press Enter to generate): " JWT_SECRET
echo
if [ -z "$JWT_SECRET" ]; then
    JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
    echo "✅ Generated random JWT secret"
fi
sed -i "s|JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|g" "$ENV_FILE"

# API Key Encryption Secret
read -s -p "API Key Encryption Secret (or press Enter to generate): " API_SECRET
echo
if [ -z "$API_SECRET" ]; then
    API_SECRET=$(openssl rand -base64 64 | tr -d '\n')
    echo "✅ Generated random API encryption secret"
fi
sed -i "s|API_KEY_ENCRYPTION_SECRET=.*|API_KEY_ENCRYPTION_SECRET=$API_SECRET|g" "$ENV_FILE"

# Set secure permissions
chmod 600 "$ENV_FILE"

echo ""
echo "✅ Environment file created: $ENV_FILE"
echo "🔒 Permissions set to 600 (read/write by owner only)"
echo ""
echo "📋 Review the file and update any additional settings:"
echo "   nano $ENV_FILE"
echo ""
echo "📝 Next steps:"
echo "   1. Review and update $ENV_FILE"
echo "   2. Setup SSL: ./deploy/setup-ssl.sh"
echo "   3. Deploy: ./deploy/deploy.sh $PROFILE"

