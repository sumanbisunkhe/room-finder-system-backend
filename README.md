# 🏠 RoomRadar 

A modern, secure, and feature-rich room rental management system built with Spring Boot and WebSocket technology.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)
![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)
![License](https://img.shields.io/badge/License-Proprietary-red.svg)

## 📑 Table of Contents
1. [Introduction](#-introduction)
2. [Features](#-features)
3. [Technology Stack](#-technology-stack)
4. [Prerequisites](#-prerequisites)
5. [Getting Started](#-getting-started)
6. [API Documentation](#-api-documentation)
7. [Security](#-security)
8. [WebSocket Integration](#-websocket-integration)
9. [Contributing](#-contributing)
10. [License](#-license)

## 🎯 Introduction
RoomRadar is a comprehensive web-based platform designed to bridge the gap between landlords and individuals searching for rental properties. The system provides a secure, real-time communication platform with features like room listings, bookings, and instant messaging.

## ✨ Features

### User Management
- Multi-role support (Admin, Landlord, Seeker)
- JWT-based authentication
- Profile management
- Account activation/deactivation
- Password management
- User statistics and analytics

### Room Management
- Create and manage room listings
- Multiple image upload support
- Advanced room search functionality
- Room availability toggle
- Recent listings view
- Detailed room information

### Booking System
- Create and manage bookings
- Booking status tracking
- Date-based availability
- Booking history

### Communication
- Real-time messaging using WebSocket
- Direct conversations between users
- Message read status
- Conversation history

### Data Management
- CSV import/export for rooms, users, and messages
- Data backup and restore
- Growth trend analytics

### Security Features
- JWT-based authentication
- Role-based access control
- Secure password handling
- CORS configuration
- HTTP-only cookies
- XSS protection

## 🛠 Technology Stack
- **Backend**: Spring Boot 3.x
- **Security**: Spring Security, JWT
- **Database**: PostgreSQL 12+
- **Real-time Communication**: WebSocket, STOMP
- **Build Tool**: Maven 3.6+
- **File Storage**: Local file system with configurable storage
- **API Documentation**: Swagger/OpenAPI

## 📋 Prerequisites
- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6+
- IDE (recommended: IntelliJ IDEA or Eclipse)

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/roomfinder-backend.git
cd roomfinder-backend
```

### 2. Configure Database
Create a PostgreSQL database and update `application.properties` with your database configuration:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/roomfinder
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring:boot run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/validate` - Validate JWT token

### User Endpoints
- `POST /api/users/register` - Register new user
- `GET /api/users/current` - Get current user
- `PUT /api/users/{id}/update` - Update user profile
- `PUT /api/users/{id}/change-password` - Change password
- `GET /api/users/stats` - Get user statistics

### Room Endpoints
- `POST /api/rooms` - Create new room
- `PUT /api/rooms/{id}` - Update room
- `DELETE /api/rooms/{id}` - Delete room
- `GET /api/rooms/search` - Search rooms
- `GET /api/rooms/recent/new-listings` - Get recent listings

### Booking Endpoints
- `POST /api/bookings` - Create booking
- `GET /api/bookings/{id}` - Get booking details
- `PUT /api/bookings/{id}/status` - Update booking status

### Message Endpoints
- `POST /api/messages` - Send message
- `PUT /api/messages/{messageId}/read` - Mark message as read
- `GET /api/messages/conversations` - Get user conversations

## 🔒 Security
The application implements comprehensive security measures:
- JWT-based authentication
- Role-based access control (ADMIN, LANDLORD, SEEKER)
- Secure password hashing using BCrypt
- CORS configuration for frontend integration
- HTTP-only cookies for JWT storage
- Protection against XSS and CSRF attacks

## 📡 WebSocket Integration
Real-time messaging is implemented using WebSocket:
- Endpoint: `/ws`
- STOMP broker relay configuration
- Secured WebSocket connections
- Message queues for user-specific messages

## 🤝 Contributing
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License
This software is proprietary and confidential. All rights reserved. Unauthorized use, modification, or distribution is strictly prohibited. See the [LICENSE](LICENSE) file for details.

