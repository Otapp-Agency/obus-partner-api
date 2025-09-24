#!/bin/bash

echo "🔐 Testing Response Wrapper Implementation..."

BASE_URL="http://localhost:8080/api/v1"

# Test successful login
echo "1. Testing successful login with response wrapper..."
RESPONSE=$(curl -s -X POST ${BASE_URL}/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rootadmin",
    "password": "Root@2024!Sec"
  }')

echo "Login Response:"
echo $RESPONSE | jq '.'

# Extract token
TOKEN=$(echo $RESPONSE | jq -r '.data.token')

if [ "$TOKEN" != "null" ] && [ "$TOKEN" != "" ]; then
    echo -e "\n✅ Login successful with response wrapper!"
    
    # Test protected endpoint
    echo -e "\n2. Testing protected endpoint with response wrapper..."
    curl -s -X GET ${BASE_URL}/partners \
      -H "Authorization: Bearer $TOKEN" | jq '.'
else
    echo -e "\n❌ Login failed!"
fi

# Test registration with response wrapper
echo -e "\n3. Testing registration with response wrapper..."
curl -s -X POST ${BASE_URL}/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser123",
    "email": "testuser123@example.com",
    "password": "TestPass123!",
    "firstName": "Test",
    "lastName": "User"
  }' | jq '.'

echo -e "\n✅ Response wrapper test completed!"
