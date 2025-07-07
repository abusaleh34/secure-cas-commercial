#!/bin/bash

# SecureCAS Commercial Edition - Deployment Script

set -e

echo "SecureCAS Commercial Edition Deployment Script"
echo "=============================================="

# Check if Docker and Docker Compose are installed
if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "Error: Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Creating .env file from template..."
    cp .env.example .env
    echo "Please edit .env file with your configuration before running this script again."
    exit 1
fi

# Build the project
echo "Building SecureCAS..."
mvn clean package -DskipTests

# Pull latest images
echo "Pulling Docker images..."
docker-compose pull

# Start services
echo "Starting SecureCAS services..."
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to start..."
sleep 30

# Check health
echo "Checking service health..."
docker-compose ps

# Show logs
echo ""
echo "To view logs, run: docker-compose logs -f"
echo "Access CAS at: https://localhost:8443/cas"
echo "Access Admin Dashboard at: https://localhost:8443/admin"
echo ""
echo "Deployment complete!"