# Room Finder System
## Comprehensive Software Documentation
Version 1.0

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Planning Phase](#2-planning-phase)
3. [Analysis Phase](#3-analysis-phase)
4. [Design Phase](#4-design-phase)
5. [Implementation Phase](#5-implementation-phase)
6. [Testing Strategy](#6-testing-strategy)
7. [Deployment Guidelines](#7-deployment-guidelines)

## 1. Project Overview

### 1.1 Purpose
The Room Finder System is a web-based platform designed to facilitate connections between landlords and individuals seeking rental properties. The system provides a comprehensive solution for room listings, property searches, and communication between stakeholders.

### 1.2 Team Structure
- **Team Lead**: Suman Bisunkhe
    - Responsibilities: Project oversight, full-stack development coordination
- **Frontend Developer**: Tulsi Sharma
    - Responsibilities: UI/UX implementation
- **Backend Developer**: Aayushma Rai
    - Responsibilities: Server-side development, API implementation

### 1.3 Timeline
- Analysis & Design: 2 weeks
- Implementation: 6 weeks
- Testing & Bug Fixes: 2 weeks
- Documentation & Presentation: 1 week

## 2. Planning Phase

### 2.1 Target Users
1. **Landlords**
    - Property owners/managers
    - Need to list and manage rental properties
    - Require communication tools with potential tenants

2. **Room Seekers**
    - Students and professionals
    - Need to search and filter available properties
    - Require easy communication with landlords

3. **Administrators**
    - Platform managers
    - Need tools for content moderation
    - Require system monitoring capabilities

### 2.2 Feature Scope

#### Core Features
1. User Management
    - Registration and authentication
    - Profile management
    - Role-based access control

2. Property Management
    - Room listing creation and management
    - Image upload functionality
    - Availability status tracking

3. Search System
    - Advanced filtering options
    - Location-based search
    - Price range filtering

4. Communication System
    - Direct messaging between users
    - Booking request management
    - Notification system

#### Optional Features
1. Map Integration
2. Real-time Chat
3. Rating System

### 2.3 Technology Stack
- **Frontend**: React.js with Bootstrap
- **Backend**: Spring Boot
- **Database**: PostgreSQL
- **Development Tools**:
    - IDE: VS Code, IntelliJ IDEA
    - Version Control: GitHub
    - Project Management: Trello
    - Database Management: pgAdmin

## 3. Analysis Phase

### 3.1 System Requirements

#### Functional Requirements
1. User Authentication
    - Secure registration process
    - Login/logout functionality
    - Password recovery system

2. Room Management
    - CRUD operations for room listings
    - Image management
    - Availability updates

3. Booking System
    - Booking request creation
    - Status tracking
    - Date management

4. Communication
    - Direct messaging
    - Read status tracking
    - Room-specific communications

#### Non-Functional Requirements
1. Performance
    - Page load time < 3 seconds
    - Response time < 1 second

2. Security
    - Encrypted data transmission
    - Secure password storage
    - Protected API endpoints

3. Scalability
    - Support for concurrent users
    - Expandable storage system

### 3.2 System Architecture
- Three-tier architecture
- RESTful API design
- Microservices approach for scalability

## 4. Design Phase

### 4.1 Logical Design

#### Database Schema
1. Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);
```

2. Rooms Table
```sql
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    landlord_id BIGINT REFERENCES users(id),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    size INTEGER,
    is_available BOOLEAN DEFAULT true,
    posted_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amenities JSONB
);
```

3. Bookings Table
```sql
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT REFERENCES rooms(id),
    seeker_id BIGINT REFERENCES users(id),
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    comments TEXT
);
```

#### Entity Relationships
- User -> Room (One-to-Many)
- Room -> Booking (One-to-Many)
- User -> Message (One-to-Many)

### 4.2 Physical Design

#### System Components
1. Frontend Components
    - Authentication Module
    - Room Management Module
    - Search Module
    - Messaging Module
    - Admin Dashboard

2. Backend Services
    - User Service
    - Room Service
    - Booking Service
    - Message Service
    - Search Service

#### API Design
1. Authentication APIs
    - POST /api/auth/register
    - POST /api/auth/login
    - GET /api/auth/profile

2. Room APIs
    - GET /api/rooms
    - POST /api/rooms
    - PUT /api/rooms/{id}
    - DELETE /api/rooms/{id}

3. Booking APIs
    - POST /api/bookings
    - GET /api/bookings
    - PUT /api/bookings/{id}

## 5. Implementation Phase

### 5.1 Development Environment Setup

#### Backend Setup
1. Spring Boot Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/roomfinder
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

2. Required Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

#### Frontend Setup
1. Project Initialization
```bash
npx create-react-app room-finder-frontend
cd room-finder-frontend
npm install react-router-dom axios bootstrap
```

2. Project Structure
```
src/
├── components/
│   ├── auth/
│   ├── rooms/
│   ├── bookings/
│   └── common/
├── services/
├── utils/
└── App.js
```

### 5.2 Implementation Guidelines

#### Code Organization
1. Backend Structure
    - Controllers
    - Services
    - Repositories
    - Models
    - Security Configuration

2. Frontend Structure
    - Components
    - Services
    - Utilities
    - Assets

#### Security Implementation
1. JWT Authentication
2. Role-Based Access Control
3. Input Validation
4. XSS Protection

## 6. Testing Strategy

### 6.1 Testing Levels
1. Unit Testing
    - Component testing
    - Service testing
    - Repository testing

2. Integration Testing
    - API endpoint testing
    - Database integration testing
    - Service integration testing

3. System Testing
    - End-to-end testing
    - Performance testing
    - Security testing

### 6.2 Testing Tools
- JUnit for backend testing
- Jest for frontend testing
- Postman for API testing
- JMeter for performance testing

## 7. Deployment Guidelines

### 7.1 Deployment Environment
- Frontend: Vercel/Netlify
- Backend: Railway.app/Render.com
- Database: Railway.app PostgreSQL

### 7.2 Configuration Management
1. Environment Variables
    - Database credentials
    - API endpoints
    - JWT secrets
    - Storage configuration

2. Deployment Checklist
    - Environment configuration
    - Database migration
    - Static asset optimization
    - Security headers
    - SSL certification

### 7.3 Monitoring and Maintenance
1. System Monitoring
    - Performance metrics
    - Error logging
    - User analytics

2. Maintenance Schedule
    - Regular backups
    - Security updates
    - Performance optimization
    - Feature updates