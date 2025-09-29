@echo off
REM OBUS Partners API - Kafka Development Setup Script for Windows
REM This script sets up Kafka for local development

echo 🚀 Setting up Kafka for OBUS Partners API Development...

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not running. Please start Docker and try again.
    pause
    exit /b 1
)

REM Check if Docker Compose is available
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose is not installed. Please install Docker Compose and try again.
    pause
    exit /b 1
)

echo ✅ Docker and Docker Compose are available

REM Start Kafka services
echo 📦 Starting Kafka services...
docker-compose -f docker-compose.kafka.yml up -d

REM Wait for services to be ready
echo ⏳ Waiting for services to be ready...
timeout /t 30 /nobreak >nul

REM Check if Kafka is ready
echo 🔍 Checking Kafka health...
set kafka_ready=false
for /l %%i in (1,1,30) do (
    docker exec obus-kafka kafka-topics --bootstrap-server localhost:9092 --list >nul 2>&1
    if !errorlevel! equ 0 (
        set kafka_ready=true
        goto :kafka_ready
    )
    echo Waiting for Kafka... (%%i/30)
    timeout /t 2 /nobreak >nul
)

:kafka_ready
if "%kafka_ready%"=="true" (
    echo ✅ Kafka is ready!
    
    echo ✅ Topics will be auto-created by the Spring Boot application!
    
    REM List topics
    echo 📋 Available topics:
    docker exec obus-kafka kafka-topics --bootstrap-server localhost:9092 --list
    
    echo.
    echo 🎉 Kafka development environment is ready!
    echo.
    echo 📊 Services:
    echo   - Kafka: localhost:9092
    echo   - Kafka UI: http://localhost:8083
    echo   - Schema Registry: http://localhost:8084
    echo.
    echo 🔧 Next steps:
    echo   1. Start your Spring Boot application with: mvnw.cmd spring-boot:run
    echo   2. The application will automatically create all required topics
    echo   3. Open Kafka UI to monitor topics and messages
    echo   4. Begin implementing event producers and consumers
    echo.
    echo 🛑 To stop Kafka services: docker-compose -f docker-compose.kafka.yml down
    
) else (
    echo ❌ Kafka failed to start properly. Please check the logs:
    echo docker-compose -f docker-compose.kafka.yml logs
    pause
    exit /b 1
)

pause
