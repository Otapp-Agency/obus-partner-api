#!/bin/bash

echo "🚀 Testing Partner API with Response Wrapper..."

BASE_URL="http://localhost:8080/api/v1"

# First, login to get token
echo "1. Logging in as root admin..."
LOGIN_RESPONSE=$(curl -s -X POST ${BASE_URL}/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rootadmin",
    "password": "Root@2024!Sec"
  }')

echo "Login Response:"
echo $LOGIN_RESPONSE | jq '.'

# Extract token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')

if [ "$TOKEN" != "null" ] && [ "$TOKEN" != "" ]; then
    echo -e "\n✅ Login successful!"
    
    # Test getting all partners
    echo -e "\n2. Testing GET all partners..."
    curl -s -X GET ${BASE_URL}/partners \
      -H "Authorization: Bearer $TOKEN" | jq '.'
    
    # Test getting partners with pagination
    echo -e "\n3. Testing GET partners with pagination..."
    curl -s -X GET "${BASE_URL}/partners?page=0&size=5&sortBy=id&sortDir=asc" \
      -H "Authorization: Bearer $TOKEN" | jq '.'
    
    # Test getting partner statistics
    echo -e "\n4. Testing GET partner statistics..."
    curl -s -X GET ${BASE_URL}/partners/statistics \
      -H "Authorization: Bearer $TOKEN" | jq '.'
    
    # Test creating a new partner
    echo -e "\n5. Testing POST create partner..."
    CREATE_RESPONSE=$(curl -s -X POST ${BASE_URL}/partners \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "businessName": "Test Partner Company",
        "legalName": "Test Partner Company Ltd",
        "email": "testpartner@example.com",
        "phoneNumber": "+1234567890",
        "address": "123 Test Street",
        "city": "Test City",
        "state": "Test State",
        "country": "Test Country",
        "postalCode": "12345",
        "businessRegistrationNumber": "BR123456789",
        "taxIdentificationNumber": "TIN123456789",
        "partnerType": "BUSINESS",
        "partnerTier": "BRONZE",
        "commissionRate": 5.0
      }')
    
    echo "Create Partner Response:"
    echo $CREATE_RESPONSE | jq '.'
    
    # Extract partner ID if creation was successful
    PARTNER_ID=$(echo $CREATE_RESPONSE | jq -r '.data.id')
    
    if [ "$PARTNER_ID" != "null" ] && [ "$PARTNER_ID" != "" ]; then
        echo -e "\n✅ Partner created successfully with ID: $PARTNER_ID"
        
        # Test getting partner by ID
        echo -e "\n6. Testing GET partner by ID..."
        curl -s -X GET ${BASE_URL}/partners/$PARTNER_ID \
          -H "Authorization: Bearer $TOKEN" | jq '.'
        
        # Test updating partner
        echo -e "\n7. Testing PUT update partner..."
        curl -s -X PUT ${BASE_URL}/partners/$PARTNER_ID \
          -H "Authorization: Bearer $TOKEN" \
          -H "Content-Type: application/json" \
          -d '{
            "businessName": "Updated Test Partner Company",
            "commissionRate": 7.5
          }' | jq '.'
        
        # Test activating partner
        echo -e "\n8. Testing PUT activate partner..."
        curl -s -X PUT ${BASE_URL}/partners/$PARTNER_ID/activate \
          -H "Authorization: Bearer $TOKEN" | jq '.'
        
        # Test verifying partner
        echo -e "\n9. Testing PUT verify partner..."
        curl -s -X PUT ${BASE_URL}/partners/$PARTNER_ID/verify \
          -H "Authorization: Bearer $TOKEN" | jq '.'
        
        # Test soft delete partner
        echo -e "\n10. Testing PUT soft delete partner..."
        curl -s -X PUT ${BASE_URL}/partners/$PARTNER_ID/soft-delete \
          -H "Authorization: Bearer $TOKEN" | jq '.'
        
        # Test hard delete partner
        echo -e "\n11. Testing DELETE partner..."
        curl -s -X DELETE ${BASE_URL}/partners/$PARTNER_ID \
          -H "Authorization: Bearer $TOKEN" | jq '.'
    else
        echo -e "\n❌ Partner creation failed!"
    fi
    
else
    echo -e "\n❌ Login failed!"
fi

echo -e "\n✅ Partner API test completed!"
