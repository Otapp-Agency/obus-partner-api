#!/bin/bash

# Start Redis using Docker Compose
echo "Starting Redis with Docker Compose..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker first."
    exit 1
fi

# Start Redis
docker-compose -f docker-compose.redis.yml up -d

# Wait for Redis to be ready
echo "Waiting for Redis to be ready..."
sleep 5

# Check if Redis is running
if docker-compose -f docker-compose.redis.yml ps redis | grep -q "Up"; then
    echo "✅ Redis is running successfully!"
    echo "Redis is available at: localhost:6379"
    echo ""
    echo "🌐 RedisInsight UI is available at: http://localhost:8001"
    echo ""
    echo "To connect to Redis CLI:"
    echo "docker exec -it obus-redis redis-cli"
    echo ""
    echo "To stop Redis and UI:"
    echo "docker-compose -f docker-compose.redis.yml down"
else
    echo "❌ Failed to start Redis"
    exit 1
fi
