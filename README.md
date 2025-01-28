# Room Finder System
Version 1.0
[![GitHub Streak](https://streak-stats.demolab.com/?user=sumanbisunkhe)](https://git.io/streak-stats)
## Table of Contents
1. [Introduction](#1-introduction)
2. [Backend Structure](#2-backend-structure)
3. [Frontend Structure](#3-frontend-structure)
4. [Application Properties](#4-application-properties)
5. [Getting Started](#5-getting-started)

## 1. Introduction
Room Finder System is a web application built using Spring Boot and React. The project follows a modular architecture with clear separation of concerns.

## 2. Backend Structure
```
room-finder-system/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── .gitattributes
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/roomfinder/
    │   │   ├── config/
    │   │   │   └── SecurityConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── BookingController.java
    │   │   │   ├── MessageController.java
    │   │   │   ├── RoomController.java
    │   │   │   └── UserController.java
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── BookingRequest.java
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── MessageRequest.java
    │   │   │   │   ├── PasswordChangeRequest.java
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── RoomRequest.java
    │   │   │   │   ├── UpdateProfileRequest.java
    │   │   │   │   └── ValidateUsersRequest.java
    │   │   │   └── response/
    │   │   │       ├── ApiResponse.java
    │   │   │       ├── JwtResponse.java
    │   │   │       └── MessageResponse.java
    │   │   ├── entity/
    │   │   │   ├── Booking.java
    │   │   │   ├── Message.java
    │   │   │   ├── Room.java
    │   │   │   └── User.java
    │   │   ├── enums/
    │   │   │   ├── BookingStatus.java
    │   │   │   └── UserRole.java
    │   │   ├── exceptions/
    │   │   │   ├── BookingNotFoundException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── InvalidBookingException.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── RoomNotFoundException.java
    │   │   │   ├── UnauthorizedAccessException.java
    │   │   │   ├── UserNotFoundException.java
    │   │   │   └── ValidationException.java
    │   │   ├── repository/
    │   │   │   ├── BookingRepository.java
    │   │   │   ├── MessageRepository.java
    │   │   │   ├── RoomRepository.java
    │   │   │   └── UserRepository.java
    │   │   ├── security/
    │   │   │   ├── CustomUserDetails.java
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── JwtRequestFilter.java
    │   │   │   └── JwtUtil.java
    │   │   ├── service/
    │   │   │   ├── impl/
    │   │   │   │   ├── BookingServiceImpl.java
    │   │   │   │   ├── MessageServiceImpl.java
    │   │   │   │   ├── RoomServiceImpl.java
    │   │   │   │   └── UserServiceImpl.java
    │   │   │   ├── BookingService.java
    │   │   │   ├── MessageService.java
    │   │   │   ├── RoomService.java
    │   │   │   └── UserService.java
    │   │   └── RoomfinderBackendApplication.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/roomfinder/
```

### Backend Components Description
- **Config**: Security configuration and application settings
- **Controller**: REST API endpoints for handling HTTP requests
- **DTO**: Request/Response objects for data transfer
- **Entity**: JPA entities for database mapping
- **Enums**: Status and role enumerations
- **Exceptions**: Custom exception handlers
- **Repository**: Database operation interfaces
- **Security**: JWT authentication implementation
- **Service**: Business logic layer

## 3. Frontend Structure

```
    room-finder-frontend/
    ├── .env
    ├── .gitignore
    ├── index.html
    ├── package.json
    ├── README.md
    ├── vite.config.js
    └── src/
    ├── assets/
    │   ├── images/
    │   └── icons/
    ├── config/
    │   ├── axios.config.js
    │   ├── routes.config.js
    │   └── constants.js
    ├── components/
    │   ├── auth/
    │   │   ├── LoginForm.jsx
    │   │   ├── RegisterForm.jsx
    │   │   └── PasswordResetForm.jsx
    │   ├── booking/
    │   │   ├── BookingForm.jsx
    │   │   ├── BookingList.jsx
    │   │   └── BookingCard.jsx
    │   ├── common/
    │   │   ├── Button.jsx
    │   │   ├── Input.jsx
    │   │   ├── Modal.jsx
    │   │   ├── Spinner.jsx
    │   │   └── Toast.jsx
    │   ├── message/
    │   │   ├── MessageList.jsx
    │   │   ├── MessageItem.jsx
    │   │   └── MessageForm.jsx
    │   ├── room/
    │   │   ├── RoomList.jsx
    │   │   ├── RoomCard.jsx
    │   │   ├── RoomForm.jsx
    │   │   └── RoomDetails.jsx
    │   └── user/
    │       ├── UserProfile.jsx
    │       └── UserSettings.jsx
    ├── dto/
    │   ├── request/
    │   │   ├── BookingRequest.js
    │   │   ├── LoginRequest.js
    │   │   ├── MessageRequest.js
    │   │   ├── PasswordChangeRequest.js
    │   │   ├── RegisterRequest.js
    │   │   ├── RoomRequest.js
    │   │   └── UpdateProfileRequest.js
    │   └── response/
    │       ├── ApiResponse.js
    │       ├── JwtResponse.js
    │       └── MessageResponse.js
    ├── enums/
    │   ├── BookingStatus.js
    │   └── UserRole.js
    ├── hooks/
    │   ├── useAuth.js
    │   ├── useBooking.js
    │   ├── useMessage.js
    │   ├── useRoom.js
    │   └── useUser.js
    ├── layouts/
    │   ├── AuthLayout.jsx
    │   ├── DashboardLayout.jsx
    │   └── MainLayout.jsx
    ├── pages/
    │   ├── auth/
    │   │   ├── LoginPage.jsx
    │   │   ├── RegisterPage.jsx
    │   │   └── ResetPasswordPage.jsx
    │   ├── booking/
    │   │   ├── BookingListPage.jsx
    │   │   └── BookingDetailsPage.jsx
    │   ├── error/
    │   │   ├── NotFoundPage.jsx
    │   │   └── ErrorPage.jsx
    │   ├── message/
    │   │   ├── MessageListPage.jsx
    │   │   └── ConversationPage.jsx
    │   ├── room/
    │   │   ├── RoomListPage.jsx
    │   │   ├── RoomDetailsPage.jsx
    │   │   └── AddRoomPage.jsx
    │   └── user/
    │       ├── ProfilePage.jsx
    │       └── SettingsPage.jsx
    ├── services/
    │   ├── api/
    │   │   ├── authApi.js
    │   │   ├── bookingApi.js
    │   │   ├── messageApi.js
    │   │   ├── roomApi.js
    │   │   └── userApi.js
    │   └── http/
    │       └── axios.js
    ├── store/
    │   ├── slices/
    │   │   ├── authSlice.js
    │   │   ├── bookingSlice.js
    │   │   ├── messageSlice.js
    │   │   ├── roomSlice.js
    │   │   └── userSlice.js
    │   └── store.js
    ├── styles/
    │   ├── components/
    │   │   ├── auth.css
    │   │   ├── booking.css
    │   │   ├── message.css
    │   │   ├── room.css
    │   │   └── user.css
    │   ├── index.css
    │   └── variables.css
    ├── utils/
    │   ├── formatters.js
    │   ├── validators.js
    │   └── helpers.js
    ├── App.jsx
    └── main.jsx
```


## 4. Application Properties

### Development Environment (application-dev.properties)



### Production Environment (application-prod.properties)


## 5. Getting Started

### Prerequisites
- Java 11+
- Node.js 14+
- PostgreSQL 12+
- Maven 3.6+

