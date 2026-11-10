#!/usr/bin/env bash
set -e

echo "=== 🚀 Starting HR & Payroll System Automated Deployment ==="

# 1. Pull latest changes from GitHub
echo "📥 1. Pulling latest code from GitHub..."
git pull origin main

# 2. Rebuild and start Docker containers
echo "🔨 2. Rebuilding and restarting Docker containers..."
docker compose up -d --build

# 3. Clean up dangling/unused images
echo "🧹 3. Cleaning up unused Docker images..."
docker image prune -f

# 4. Display service status
echo "📊 4. Checking container status..."
docker compose ps

echo "=== ✅ HR & Payroll System Backend Deployment Finished Successfully! ==="
