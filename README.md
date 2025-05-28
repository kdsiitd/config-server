# Config Server

A comprehensive Spring Cloud Config Server implementation that provides centralized configuration management for distributed systems. This server stores configuration data in a MySQL database and provides RESTful APIs with extensive validation, error handling, and comprehensive Swagger documentation.

## 🚀 Features

- **Centralized Configuration Management**: Store and manage all application configurations in one place
- **JDBC-based Storage**: Reliable MySQL database backend with Liquibase migrations
- **RESTful API**: Comprehensive REST endpoints with full CRUD operations
- **Spring Cloud Config Compatible**: Returns data in Spring Cloud Config format
- **Comprehensive Validation**: Input validation with detailed error messages
- **Swagger Documentation**: Complete API documentation with examples
- **Batch Operations**: Create and update multiple configurations atomically
- **Profile & Label Support**: Environment-specific configurations with versioning
- **Extensive Test Coverage**: Unit, integration, and controller tests
- **Docker Support**: Easy containerized deployment
- **Production Ready**: Comprehensive error handling and logging

## 🏗️ Architecture

The project follows a clean, multi-module Maven architecture:

```
config-server/
├── core/           # Domain entities and repository interfaces
├── service/        # Business logic and service layer
├── jobs/           # Scheduled jobs and background tasks
└── app/            # REST controllers and main application
```

### Module Dependencies
- **app** → **service** → **core**
- Each module has clear responsibilities and boundaries
- Dependency injection managed by Spring Boot

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.9+** for building
- **MySQL 8.0+** for production database
- **Docker & Docker Compose** (optional, for containerized deployment)

## 🛠️ Building the Application

### Using Maven

```bash
# Clean and build all modules
mvn clean install

# Build without running tests
mvn clean install -DskipTests

# Build specific module
cd app && mvn clean package
```

### Using Docker

```bash
# Build Docker image
docker-compose build

# Build and start services
docker-compose up --build
```

## 🚀 Running the Application

### Local Development (Maven)

```bash
cd app
mvn spring-boot:run
```

### Production (Docker)

```bash
# Start all services (MySQL + Config Server)
docker-compose up -d

# View logs
docker-compose logs -f config-server

# Stop services
docker-compose down
```

### Environment Profiles

- **default**: Local development with H2 database
- **prod**: Production with MySQL database
- **test**: Testing with H2 in-memory database

## ⚙️ Configuration

### Application Configuration (`application.yml`)

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  
  # Database Configuration
  datasource:
    url: jdbc:mysql://localhost:3306/config_server?createDatabaseIfNotExist=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
  
  # Liquibase Configuration
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
  
  # Spring Cloud Config Server
  cloud:
    config:
      server:
        jdbc:
          enabled: true
          sql: "SELECT prop_value FROM config WHERE application=? AND profile=? AND label=? AND prop_key=?"

# Logging Configuration
logging:
  level:
    com.kds.config.server: INFO
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `password` |
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `3306` |
| `DB_NAME` | Database name | `config_server` |

## 📚 API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: http://localhost:8888/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8888/v3/api-docs

### API Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/configs/{app}/{profile}/{label}/{key}` | Get specific configuration |
| `GET` | `/api/v1/configs/{app}/{profile}/{label}` | Get all configs for app/profile/label |
| `GET` | `/api/v1/configs/{app}/{profile}` | Get all configs for app/profile |
| `POST` | `/api/v1/configs` | Create new configuration |
| `POST` | `/api/v1/configs/batch` | Create multiple configurations |
| `PUT` | `/api/v1/configs` | Update configuration |
| `PUT` | `/api/v1/configs/batch` | Update multiple configurations |
| `DELETE` | `/api/v1/configs/{app}/{profile}/{label}/{key}` | Delete configuration |

### Request/Response Examples

#### Create Configuration

```bash
curl -X POST http://localhost:8888/api/v1/configs \
  -H "Content-Type: application/json" \
  -d '{
    "application": "user-service",
    "profile": "prod",
    "label": "v1.0.0",
    "key": "database.url",
    "value": "jdbc:postgresql://prod-db:5432/userdb"
  }'
```

**Response:**
```json
{
  "status": "SUCCESS",
  "message": "Config Saved",
  "config": {
    "id": 1,
    "application": "user-service",
    "profile": "prod",
    "label": "v1.0.0",
    "propKey": "database.url",
    "propValue": "jdbc:postgresql://prod-db:5432/userdb",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

#### Get Configuration (Spring Cloud Config Format)

```bash
curl http://localhost:8888/api/v1/configs/user-service/prod/v1.0.0
```

**Response:**
```json
{
  "name": "user-service",
  "profiles": ["prod"],
  "label": "v1.0.0",
  "propertySources": [
    {
      "name": "user-service-prod-v1.0.0",
      "source": {
        "database.url": "jdbc:postgresql://prod-db:5432/userdb",
        "database.username": "produser",
        "cache.ttl": "3600"
      }
    }
  ]
}
```

#### Batch Operations

```bash
curl -X POST http://localhost:8888/api/v1/configs/batch \
  -H "Content-Type: application/json" \
  -d '{
    "configs": [
      {
        "application": "user-service",
        "profile": "prod",
        "label": "v1.0.0",
        "key": "database.url",
        "value": "jdbc:postgresql://prod-db:5432/userdb"
      },
      {
        "application": "user-service",
        "profile": "prod",
        "label": "v1.0.0",
        "key": "database.username",
        "value": "produser"
      }
    ]
  }'
```

## 🗄️ Database Schema

### Config Table

```sql
CREATE TABLE config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application VARCHAR(50) NOT NULL,
    profile VARCHAR(20) NOT NULL,
    label VARCHAR(100),
    prop_key VARCHAR(100) NOT NULL,
    prop_value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    
    CONSTRAINT uk_config UNIQUE (application, profile, label, prop_key)
);
```

### Indexes

- **Primary Key**: `id`
- **Unique Constraint**: `(application, profile, label, prop_key)`
- **Performance Indexes**: 
  - `idx_app_profile` on `(application, profile)`
  - `idx_app_profile_label` on `(application, profile, label)`

## 🧪 Testing

### Test Coverage

The application includes comprehensive test coverage:

- **Unit Tests**: Service layer, API layer, and repository tests
- **Integration Tests**: Full application stack testing
- **Controller Tests**: REST endpoint testing with MockMvc
- **Validation Tests**: Input validation and error handling

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
cd app && mvn test

# Run tests with coverage report
mvn test jacoco:report

# Run integration tests only
mvn test -Dtest="*IntegrationTest"

# Run specific test class
mvn test -Dtest="ConfigControllerTest"
```

### Test Profiles

- **test**: Uses H2 in-memory database
- **integration**: Full stack testing with test containers

## 🔧 Validation & Error Handling

### Input Validation

All API endpoints include comprehensive validation:

- **Application Name**: 1-50 chars, alphanumeric + hyphens/underscores
- **Profile**: 1-20 chars, alphanumeric + hyphens/underscores  
- **Label**: 0-100 chars, alphanumeric + dots/hyphens/underscores
- **Key**: 1-100 chars, alphanumeric + dots/underscores/hyphens
- **Value**: 1-500 chars, any content

### Error Responses

```json
{
  "status": "VALIDATION_ERROR",
  "message": "Application name cannot be blank"
}
```

### HTTP Status Codes

- `200 OK`: Successful retrieval/update
- `201 Created`: Successful creation
- `204 No Content`: Successful deletion
- `400 Bad Request`: Validation errors
- `404 Not Found`: Resource not found
- `409 Conflict`: Duplicate resource
- `500 Internal Server Error`: Server errors

## 🐳 Docker Deployment

### Docker Compose Configuration

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: config_server
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  config-server:
    build: .
    ports:
      - "8888:8888"
    environment:
      DB_HOST: mysql
      DB_USERNAME: root
      DB_PASSWORD: password
    depends_on:
      - mysql

volumes:
  mysql_data:
```

### Production Deployment

```bash
# Production deployment with external database
docker run -d \
  --name config-server \
  -p 8888:8888 \
  -e DB_HOST=prod-mysql.example.com \
  -e DB_USERNAME=config_user \
  -e DB_PASSWORD=secure_password \
  config-server:latest
```

## 📊 Monitoring & Health Checks

### Health Endpoints

- **Health Check**: `GET /actuator/health`
- **Application Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`

### Logging

Structured logging with configurable levels:

```yaml
logging:
  level:
    com.kds.config.server: INFO
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 🔒 Security Considerations

### Production Recommendations

1. **Database Security**:
   - Use strong passwords
   - Enable SSL connections
   - Restrict database access

2. **Application Security**:
   - Enable HTTPS
   - Implement authentication/authorization
   - Use environment variables for secrets

3. **Network Security**:
   - Use private networks
   - Implement firewall rules
   - Monitor access logs

## 🚀 Performance Optimization

### Database Optimization

- Connection pooling configured
- Proper indexing on query columns
- Query optimization for large datasets

### Application Optimization

- Lazy loading for JPA entities
- Caching for frequently accessed configs
- Batch operations for bulk updates

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java coding standards
- Write comprehensive tests
- Update documentation
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:

- **Documentation**: Check this README and Swagger UI
- **Issues**: Create GitHub issues for bugs/features
- **Email**: support@kds.com

## 🔄 Version History

### v1.0.0 (Current)
- Initial release with full CRUD operations
- Comprehensive Swagger documentation
- Extensive validation and error handling
- Complete test coverage
- Docker support
- Spring Cloud Config compatibility

---

**Built with ❤️ by the KDS Team** 