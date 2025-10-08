#!/bin/bash
# Script to copy deployment files to server
# Usage: ./copy-to-server.sh [user@host]

set -e

# Color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

SERVER=${1:-forge@obus-partners.otapp.live}
REMOTE_DIR="~/obus-partners.otapp.live"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║      Copy Deployment Files to Server                      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Target Server:${NC} $SERVER"
echo -e "${YELLOW}Remote Dir:${NC}    $REMOTE_DIR"
echo ""

# Check if we can connect
echo -e "${BLUE}🔍 Testing SSH connection...${NC}"
if ssh -o ConnectTimeout=5 -o BatchMode=yes $SERVER exit 2>/dev/null; then
    echo -e "${GREEN}✅ SSH connection successful${NC}"
else
    echo -e "${RED}❌ Cannot connect to server${NC}"
    echo -e "${YELLOW}   Make sure you can SSH to: $SERVER${NC}"
    exit 1
fi
echo ""

# Create remote directory if it doesn't exist
echo -e "${BLUE}📁 Creating remote directory...${NC}"
ssh $SERVER "mkdir -p $REMOTE_DIR/deploy $REMOTE_DIR/nginx"
echo -e "${GREEN}✅ Directory created${NC}"
echo ""

# Copy files
echo -e "${BLUE}📤 Copying files to server...${NC}"
echo ""

echo -e "${YELLOW}→${NC} Copying docker-compose.yml..."
scp docker-compose.yml $SERVER:$REMOTE_DIR/

echo -e "${YELLOW}→${NC} Copying Dockerfile..."
scp Dockerfile $SERVER:$REMOTE_DIR/

echo -e "${YELLOW}→${NC} Copying pom.xml..."
scp pom.xml $SERVER:$REMOTE_DIR/

echo -e "${YELLOW}→${NC} Copying deployment scripts..."
scp deploy/*.sh $SERVER:$REMOTE_DIR/deploy/
scp deploy/*.template $SERVER:$REMOTE_DIR/deploy/

echo -e "${YELLOW}→${NC} Copying nginx configuration..."
scp nginx/nginx.conf $SERVER:$REMOTE_DIR/nginx/

echo -e "${YELLOW}→${NC} Copying documentation..."
scp DEPLOYMENT_GUIDE.md $SERVER:$REMOTE_DIR/ 2>/dev/null || echo "  (DEPLOYMENT_GUIDE.md not found, skipping)"
scp README.md $SERVER:$REMOTE_DIR/ 2>/dev/null || echo "  (README.md not found, skipping)"

echo ""
echo -e "${GREEN}✅ All files copied successfully!${NC}"
echo ""

# Make scripts executable
echo -e "${BLUE}🔧 Making scripts executable on server...${NC}"
ssh $SERVER "chmod +x $REMOTE_DIR/deploy/*.sh"
echo -e "${GREEN}✅ Scripts are now executable${NC}"
echo ""

echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║         ✅ Files Copied Successfully!                      ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}Next Steps:${NC}"
echo ""
echo -e "${YELLOW}1.${NC} SSH to the server:"
echo -e "   ${BLUE}ssh $SERVER${NC}"
echo ""
echo -e "${YELLOW}2.${NC} Run initial setup:"
echo -e "   ${BLUE}cd $REMOTE_DIR${NC}"
echo -e "   ${BLUE}./deploy/initial-server-setup.sh${NC}"
echo ""
echo -e "${YELLOW}3.${NC} Edit environment files:"
echo -e "   ${BLUE}nano .env.staging${NC}"
echo ""
echo -e "${YELLOW}4.${NC} Setup SSL:"
echo -e "   ${BLUE}sudo ./deploy/setup-ssl.sh${NC}"
echo ""
echo -e "${YELLOW}5.${NC} Deploy:"
echo -e "   ${BLUE}./deploy/quick-deploy.sh staging${NC}"
echo ""
echo -e "${GREEN}📚 For detailed instructions, see DEPLOYMENT_GUIDE.md${NC}"
echo ""

