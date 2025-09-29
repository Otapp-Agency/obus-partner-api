#!/bin/bash

# OBUS Partners API - Kafka Development Setup Script
# This script sets up Kafka for local development

echo "🚀 Setting up Kafka for OBUS Partners API Development..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

echo "✅ Docker and Docker Compose are available"

# Start Kafka services
echo "📦 Starting Kafka services..."
docker-compose -f docker-compose.kafka.yml up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check if Kafka is ready
echo "🔍 Checking Kafka health..."
kafka_ready=false
for i in {1..30}; do
    if docker exec obus-kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
        kafka_ready=true
        break
    fi
    echo "Waiting for Kafka... ($i/30)"
    sleep 2
done

if [ "$kafka_ready" = true ]; then
    echo "✅ Kafka is ready!"
    
    echo "✅ Topics will be auto-created by the Spring Boot application!"
    
    # List topics
    echo "📋 Available topics:"
    docker exec obus-kafka kafka-topics --bootstrap-server localhost:9092 --list
    
    echo ""
    echo "🎉 Kafka development environment is ready!"
    echo ""
    echo "📊 Services:"
    echo "  - Kafka: localhost:9092"
    echo "  - Kafka UI: http://localhost:8083"
    echo "  - Schema Registry: http://localhost:8084"
    echo ""
    echo "🔧 Next steps:"
    echo "  1. Start your Spring Boot application with: ./mvnw spring-boot:run"
    echo "  2. The application will automatically create all required topics"
    echo "  3. Open Kafka UI to monitor topics and messages"
    echo "  4. Begin implementing event producers and consumers"
    echo ""
    echo "🛑 To stop Kafka services: docker-compose -f docker-compose.kafka.yml down"
    
else
    echo "❌ Kafka failed to start properly. Please check the logs:"
    echo "docker-compose -f docker-compose.kafka.yml logs"
    exit 1
fi
