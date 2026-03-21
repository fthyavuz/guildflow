#!/bin/bash

# --- Start Docker if not running ---
if docker info > /dev/null 2>&1; then
  echo "[DOCKER] Already running."
else
  echo "[DOCKER] Starting Docker Desktop..."
  cmd.exe /c start "" "C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe"

  echo "[DOCKER] Waiting for Docker daemon to be ready..."
  until docker info > /dev/null 2>&1; do
    sleep 2
  done
  echo "[DOCKER] Docker is ready."
fi

# --- Start database container ---
echo "[DOCKER] Starting database container..."
docker-compose up -d
echo "[DOCKER] Database started."

# --- Open backend in a new Windows terminal window ---
echo "[BACKEND] Starting Spring Boot in new window..."
cmd.exe /c start "GuildFlow Backend" cmd.exe /k "cd /d C:\Develop\guildflow\backend && mvn spring-boot:run"

# --- Open frontend in a new Windows terminal window ---
echo "[FRONTEND] Starting Angular in new window..."
cmd.exe /c start "GuildFlow Frontend" cmd.exe /k "cd /d C:\Develop\guildflow\frontend && ng serve"

echo ""
echo "[DEV] All services started."
echo "[DEV] Backend: http://localhost:8080"
echo "[DEV] Frontend: http://localhost:4200"
echo "[DEV] Close the opened terminal windows to stop backend/frontend."
