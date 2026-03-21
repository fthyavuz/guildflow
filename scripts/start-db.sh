#!/bin/bash

# Check if Docker daemon is already running
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

echo "[DOCKER] Starting database container..."
docker-compose up -d
echo "[DOCKER] Database started."
