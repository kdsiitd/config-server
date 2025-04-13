#!/bin/bash

# Function to display help message
function show_help {
    echo "Config Server Runner"
    echo "Usage: ./run.sh [command]"
    echo ""
    echo "Commands:"
    echo "  build       Build the application using Maven"
    echo "  run         Run the application using Maven"
    echo "  test        Run the tests"
    echo "  docker-build Build the Docker image"
    echo "  docker-up   Start the Docker containers"
    echo "  docker-down Stop the Docker containers"
    echo "  docker-logs View the Docker container logs"
    echo "  help        Show this help message"
}

# Check if a command was provided
if [ $# -eq 0 ]; then
    show_help
    exit 1
fi

# Process commands
case "$1" in
    build)
        echo "Building the application..."
        mvn clean install
        ;;
    run)
        echo "Running the application..."
        cd app
        mvn spring-boot:run
        ;;
    test)
        echo "Running tests..."
        mvn test
        ;;
    docker-build)
        echo "Building Docker image..."
        docker-compose build
        ;;
    docker-up)
        echo "Starting Docker containers..."
        docker-compose up -d
        ;;
    docker-down)
        echo "Stopping Docker containers..."
        docker-compose down
        ;;
    docker-logs)
        echo "Viewing Docker logs..."
        docker-compose logs -f
        ;;
    help)
        show_help
        ;;
    *)
        echo "Unknown command: $1"
        show_help
        exit 1
        ;;
esac

exit 0 