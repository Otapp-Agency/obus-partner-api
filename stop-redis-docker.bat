@echo off
REM Stop Redis Docker container
echo Stopping Redis...

docker-compose -f docker-compose.redis.yml down

echo ✅ Redis stopped successfully!
pause
