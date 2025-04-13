# Config Server

A Spring Cloud Config Server implementation that provides centralized configuration management for distributed systems. This server stores configuration data in a MySQL database and provides RESTful APIs for clients to retrieve their configuration.

## Features

- Centralized configuration management
- JDBC-based configuration storage
- RESTful API for configuration retrieval
- Support for different profiles (dev, test, prod)
- Support for different labels (main, v1, etc.)
- Liquibase for database migration
- Docker support for easy deployment

## Architecture

The project is structured as a multi-module Maven project:

- **core**: Contains the entity classes and repository interfaces
- **service**: Contains the service layer implementation
- **jobs**: Contains scheduled jobs (if any)
- **app**: Contains the main application class and REST controllers

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Docker and Docker Compose (for containerized deployment)

## Building the Application

### Using Maven

```bash
mvn clean install
```

### Using Docker

```bash
docker-compose build
```

## Running the Application

### Using Maven

```bash
cd app
mvn spring-boot:run
```

### Using Docker

```bash
docker-compose up -d
```

## Configuration

The application is configured using `application.yml`. The main configuration options are:

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  datasource:
    url: jdbc:mysql://localhost:3306/config_server?createDatabaseIfNotExist=true
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
  cloud:
    config:
      server:
        jdbc:
          enabled: true
          sql: "SELECT prop_value FROM config WHERE application=? AND profile=? AND label=? AND prop_key=?"
```

## API Endpoints

### Get Configuration

```
GET /{application}/{profile}/{label}/{key}
```

Example:
```
GET /myapp/dev/main/database.url
```

### Get All Configurations for an Application and Profile

```
GET /{application}/{profile}
```

Example:
```
GET /myapp/dev
```

### Save Configuration

```
POST /config
```

Request body:
```json
{
  "application": "myapp",
  "profile": "dev",
  "label": "main",
  "key": "database.url",
  "value": "jdbc:mysql://localhost:3306/mydb"
}
```

### Delete Configuration

```
DELETE /{application}/{profile}/{label}/{key}
```

Example:
```
DELETE /myapp/dev/main/database.url
```

## Docker Deployment

The application can be deployed using Docker and Docker Compose. The `docker-compose.yml` file sets up both the MySQL database and the Config Server application.

### Starting the Services

```bash
docker-compose up -d
```

### Stopping the Services

```bash
docker-compose down
```

### Viewing Logs

```bash
docker-compose logs -f
```

## Database Schema

The application uses a single table called `config` with the following schema:

```sql
CREATE TABLE IF NOT EXISTS config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application VARCHAR(100) NOT NULL,
    profile VARCHAR(100) NOT NULL,
    label VARCHAR(100),
    prop_key VARCHAR(150) NOT NULL,
    prop_value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uk_config UNIQUE (application, profile, label, prop_key)
);
```

## Testing

The application includes unit tests for the repository, service, and API layers. To run the tests:

```bash
mvn test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 