#!/bin/bash

echo "🔐 Testing Root Admin Authentication..."

# Test root admin login
echo "1. Logging in as root admin..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rootadmin",
    "password": "Root@2024!Sec"
  }')

echo "Response:"
echo $RESPONSE | jq '.'

# Extract token
TOKEN=$(echo $RESPONSE | jq -r '.token')

if [ "$TOKEN" != "null" ] && [ "$TOKEN" != "" ]; then
    echo -e "\n✅ Root admin login successful!"
    echo "Token: $TOKEN"
    
    echo -e "\n2. Testing protected endpoint with admin token..."
    curl -s -X GET http://localhost:8080/api/v1/partners \
      -H "Authorization: Bearer $TOKEN" | jq '.'
else
    echo -e "\n❌ Root admin login failed!"
    echo "Make sure the application is running and the root user was created."
fi

echo -e "\n✅ Root admin test completed!"
