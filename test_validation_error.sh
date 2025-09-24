#!/bin/bash

# Test script to verify validation error handling
# This should now return a simple "Validation failed" message instead of detailed field errors

echo "Testing validation error handling..."
echo ""

# Test with invalid partner creation data (missing required fields)
curl -X POST http://localhost:8080/api/v1/partners \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "companyName": "",
    "contactPerson": "",
    "email": "invalid-email"
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo ""
echo "Expected response: Simple 'Validation failed' message without detailed field errors"
