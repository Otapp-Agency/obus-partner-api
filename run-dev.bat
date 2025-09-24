@echo off
echo Starting OBUS Partner API in Development Mode...
echo Hot reload is enabled - changes will automatically restart the application
echo.

REM Set development profile
set SPRING_PROFILES_ACTIVE=dev

REM Run the application with Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

pause
