#!/bin/bash

set -e

echo "Running Maven tests and integration tests..."
./mvnw clean verify

echo "Building the Docker image..."
docker build -t temperature-service .

echo "Running the Docker container on port 8080..."

# Adjust the volume mount path and ensure the environment variable points to the correct file location
docker run -v "$(pwd)/example_file.csv:/app/example_file.csv" \
  -e TEMPERATURE_CSV_FILE=file:/app/example_file.csv \
  -d -p 8080:8080 temperature-service

echo "Waiting for the container to start..."
sleep 5

docker ps | grep temperature-service

echo "Container is running successfully!"
