# Room Finder System
## Project Structure Documentation
Version 1.0

## Table of Contents
1. [Introduction](#1-introduction)
2. [Backend Structure](#2-backend-structure)
3. [Frontend Structure](#3-frontend-structure)
4. [Application Properties](#4-application-properties)
5. [Initial Setup Guide](#5-initial-setup-guide)

## 1. Introduction
Room Finder System is a web application built using Spring Boot and React. The project follows a modular architecture with clear separation of concerns.

## 2. Backend Structure

### Core Application
```
roomfinder-backend/
└── src/
    └── main/
        └── java/
            └── com/
                └── roomfinder/
                    └── RoomFinderApplication.java  # Main application class
```

### Configuration Layer
```
config/
├── SecurityConfig.java        # Spring Security configuration
├── JwtConfig.java            # JWT token configuration
├── CorsConfig.java           # CORS policy configuration
├── SwaggerConfig.java        # API documentation config
└── ModelMapperConfig.java    # Object mapping configuration
```

### Web Layer
```
controller/
├── AuthController.java       # Authentication endpoints
├── UserController.java       # User management endpoints
├── RoomController.java       # Room management endpoints
├── BookingController.java    # Booking management endpoints
└── MessageController.java    # Messaging system endpoints
```

### Data Transfer Objects
```
dto/
├── request/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── RoomRequest.java
│   └── BookingRequest.java
└── response/
    ├── JwtResponse.java
    ├── RoomResponse.java
    ├── BookingResponse.java
    └── MessageResponse.java
```

### Domain Layer
```
entity/
├── User.java                # User entity
├── Room.java                # Room entity
├── Booking.java             # Booking entity
├── Message.java             # Message entity
└── RoomImage.java          # Room images entity
```

### Enumerations
```
enums/
├── UserRole.java           # User roles (ADMIN, LANDLORD, SEEKER)
└── BookingStatus.java      # Booking statuses
```

### Exception Handling
```
exception/
├── GlobalExceptionHandler.java     # Global exception handling
├── ResourceNotFoundException.java   # Resource not found exception
└── UnauthorizedException.java      # Authorization exception
```

### Data Access Layer
```
repository/
├── UserRepository.java
├── RoomRepository.java
├── BookingRepository.java
└── MessageRepository.java
```

### Security Layer
```
security/
├── JwtTokenProvider.java           # JWT token generation/validation
├── JwtAuthenticationFilter.java    # JWT authentication filter
└── UserDetailsServiceImpl.java     # Custom user details service
```

### Service Layer
```
service/
├── impl/
│   ├── UserServiceImpl.java
│   ├── RoomServiceImpl.java
│   ├── BookingServiceImpl.java
│   └── MessageServiceImpl.java
├── UserService.java
├── RoomService.java
├── BookingService.java
└── MessageService.java
```

### Utility Classes
```
util/
├── Constants.java          # Application constants
└── ValidationUtil.java     # Validation utility methods
```

## 3. Frontend Structure

### Public Assets
```
public/
├── index.html             # Main HTML file
├── favicon.ico            # Website favicon
└── manifest.json          # PWA manifest
```

### Components
```
src/components/
├── auth/                  # Authentication components
├── layout/                # Layout components
├── room/                  # Room-related components
├── booking/               # Booking components
├── user/                  # User profile components
├── admin/                 # Admin dashboard components
├── common/                # Reusable components
└── message/               # Messaging components
```

### Application Logic
```
src/
├── context/              # React Context providers
├── hooks/                # Custom React hooks
├── services/             # API service layers
├── utils/                # Utility functions
├── assets/               # Static assets
└── routes/               # Routing configuration
```

## 4. Application Properties

### Development Environment (application-dev.properties)
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api/v1

# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/roomfinder_dev
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=your_jwt_secret_key_here
jwt.expiration=86400000

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true

# Logging Configuration
logging.level.root=INFO
logging.level.com.roomfinder=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Cors Configuration
cors.allowed-origins=http://localhost:3000
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.exposed-headers=Authorization
cors.allow-credentials=true
cors.max-age=3600
```

### Production Environment (application-prod.properties)
```properties
# Server Configuration
server.port=${PORT:8080}
server.servlet.context-path=/api/v1

# PostgreSQL Database Configuration
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true

# Logging Configuration
logging.level.root=ERROR
logging.level.com.roomfinder=INFO

# Cors Configuration
cors.allowed-origins=${CORS_ALLOWED_ORIGINS}
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.exposed-headers=Authorization
cors.allow-credentials=true
cors.max-age=3600
```


The application will be accessible at:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080/api/v1
