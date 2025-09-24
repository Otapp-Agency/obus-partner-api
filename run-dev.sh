#!/bin/bash

echo "Starting OBUS Partner API in Development Mode..."
echo "Hot reload is enabled - changes will automatically restart the application"
echo ""

# Set development profile
export SPRING_PROFILES_ACTIVE=dev

# Run the application with Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev
