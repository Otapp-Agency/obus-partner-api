@echo off
REM Start Redis using Docker Compose
echo Starting Redis with Docker Compose...

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Docker is not running. Please start Docker first.
    pause
    exit /b 1
)

REM Start Redis
docker-compose -f docker-compose.redis.yml up -d

REM Wait for Redis to be ready
echo Waiting for Redis to be ready...
timeout /t 5 /nobreak >nul

REM Check if Redis is running
docker-compose -f docker-compose.redis.yml ps redis | findstr "Up" >nul
if %errorlevel% equ 0 (
    echo ✅ Redis is running successfully!
    echo Redis is available at: localhost:6379
    echo.
    echo 🌐 RedisInsight UI is available at: http://localhost:8001
    echo.
    echo To connect to Redis CLI:
    echo docker exec -it obus-redis redis-cli
    echo.
    echo To stop Redis and UI:
    echo docker-compose -f docker-compose.redis.yml down
) else (
    echo ❌ Failed to start Redis
    pause
    exit /b 1
)

pause
