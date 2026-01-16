# Healthcare Management System - Backend Microservices

## Description

A cloud-ready healthcare platform that helps kidney disease patients and doctors manage appointments, medical records, and health monitoring. Patients can book appointments, track their health metrics, and chat with doctors in real-time. Doctors can manage schedules, create medical records, and use AI-powered tools for diagnosis support.

Built using Spring Boot microservices architecture with JWT authentication, RESTful APIs, real-time communication, and AI integration for disease prediction and intelligent chatbot consultation.

## Key Features

- **User Authentication & Authorization** - JWT-based authentication with role-based access control (Patient, Doctor, Admin)
- **Appointment Management** - Complete booking, scheduling, and cancellation workflow with email notifications
- **Medical Records System** - Digital health records with prescriptions, diagnoses, and health metrics tracking
- **Real-time Communication** - WebSocket-based chat between doctors and patients; community posts and comments
- **AI-Powered Prediction** - Machine learning model for kidney disease stage prediction with 99% accuracy
- **RAG Chatbot** - 24/7 medical Q&A using Retrieval-Augmented Generation with verified medical documents
- **Payment Processing** - Online payment integration for consultations and appointments
- **Admin Dashboard** - User management, content moderation, and system analytics

## Tech Stack

**Backend (Java)**
- Java 17, Spring Boot 3.4.2
- Spring Cloud (Netflix Eureka, Spring Cloud Gateway, OpenFeign)
- Spring Security + JWT
- Spring Data JPA, Spring WebSocket

**Databases**
- PostgreSQL 16 (User, HealthRecord, Scheduling, Payment, Notification)
- MongoDB Atlas (Chat messages, Posts, Comments)

**Messaging & Caching**
- RabbitMQ (Event-driven communication)
- Redis 7 (Session caching)

**AI/ML Service (Python)**
- FastAPI, scikit-learn, LightGBM
- LangChain, FAISS, Google Gemini 2.0 Flash (RAG Chatbot)

**DevOps & Cloud**
- Docker, Docker Compose
- Cloudinary (Media storage)
- Spring Mail (Notifications)

## Architecture Overview

The system follows **microservices architecture** with 8 independent services:

- **EurekaServer** (8761) - Service discovery and registration
- **GatewayService** (8080) - API Gateway with JWT authentication filter; routes all client requests
- **UserService** (8081) - User authentication, profiles, doctor/patient management
- **HealthRecordService** (8082) - Medical records, prescriptions, health metrics
- **SchedulingService** (8084) - Appointments, doctor schedules, AI prediction results
- **CommunicationService** (8085) - WebSocket chat, posts, comments
- **NotificationService** (8083) - Email and in-app notifications
- **PaymentService** (8087) - Payment processing
- **AdminService** (8088) - System administration
- **AIService** (8086) - CKD prediction, RAG chatbot

**Key Patterns:**
- API Gateway for centralized routing and authentication
- Service discovery with Eureka for dynamic service registration
- Event-driven communication using RabbitMQ for async operations
- Database per service for data isolation
- Redis caching for performance optimization

## My Responsibilities

As a backend developer on this project, I was responsible for:

**Microservices Development**
- Designed and implemented multiple Spring Boot microservices with RESTful APIs and WebSocket
- Configured service discovery with Netflix Eureka and API Gateway routing
- Implemented JWT authentication filter at Gateway level with role-based authorization
- Applied **Global Exception Handler** pattern for centralized error handling across all services
- Designed **API Response Wrapper** pattern for consistent response structure (success, error, validation messages)

**Database Design & Implementation**
- Designed normalized database schemas across 5 PostgreSQL databases
- Implemented JPA entities with one-to-many and many-to-many relationships
- Integrated MongoDB for flexible document storage in CommunicationService

**Authentication & Security**
- Built JWT-based authentication system with access and refresh tokens
- Implemented role-based access control (RBAC) for Patient, Doctor, and Admin roles
- Integrated Spring Security with custom authentication filters

**Inter-Service Communication**
- Implemented synchronous communication using OpenFeign REST clients
- Built asynchronous messaging with RabbitMQ for notification events
- Configured Redis caching for session management and performance optimization

**AI/ML Service Development**
- Built AI Service using FastAPI for CKD prediction and RAG chatbot
- Trained and deployed machine learning models (scikit-learn, LightGBM) for disease prediction
- Implemented RAG (Retrieval-Augmented Generation) chatbot using LangChain, FAISS vector store, and Google Gemini
- Integrated Python AI service with Java microservices through REST APIs and Eureka service registry

**Real-time Features**
- Implemented WebSocket chat for real-time doctor-patient communication
- Integrated STOMP protocol for message routing

**Third-Party Integrations**
- Integrated Cloudinary for media file management
- Configured Spring Mail for email notifications
- Connected Python AI service with Java microservices ecosystem

**DevOps**
- Containerized all services using Docker with multi-stage builds
- Created Docker Compose configuration for local development environment
- Managed environment-specific configurations (dev, docker profiles)

## Database Design

**Main Entities:**
- **User** (abstract) → Patient, Doctor (inheritance mapping)
- **MedicalRecord** → Prescriptions, HealthMetrics (one-to-many)
- **Appointment** → DoctorSchedule, TimeSlot (relationships)
- **Message, Post, Comment** (MongoDB documents)
- **Notification, Payment** (transactional data)

**Key Relationships:**
- Patient has many MedicalRecords, Appointments, HealthMetrics
- Doctor has many Appointments, MedicalRecords created
- Appointment links Patient, Doctor, and MedicalRecord (one-to-one)

**ERD:** See `entity-class-diagram.puml` for complete visualization.

## API Documentation

All requests go through **API Gateway** at `http://localhost:8080`

**Authentication Endpoints:**
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - Login (returns JWT tokens)
- `GET /api/v1/auth/getMe` - Get current user profile

**Core Endpoints:**
- `/api/v1/users/**` - User management
- `/api/v1/doctors/**`, `/api/v1/patients/**` - Doctor/Patient profiles
- `/api/v1/appointments/**` - Appointment booking and management
- `/api/v1/medical-records/**` - Medical records CRUD
- `/api/v1/health-metrics/**` - Health data tracking
- `/api/v1/messages/**` - Chat messages
- `/api/v1/posts/**`, `/api/v1/comments/**` - Community features
- `/api/v1/notifications/**` - Notifications
- `/api/v1/admin/**` - Admin operations (Admin role only)

**AI Endpoints:**
- `POST /api/v1/analysis/ckd-prediction` - CKD stage prediction
- `POST /api/v1/chat/ask` - RAG chatbot Q&A

**WebSocket:**
- `ws://localhost:8080/ws/communication` - Real-time chat

All endpoints (except `/auth/login`, `/auth/register`) require JWT token in `Authorization: Bearer <token>` header.

## Authentication & Authorization

**JWT-based Authentication:**
1. User logs in via `/api/v1/auth/login`
2. Server returns Access Token (24 hours) and Refresh Token
3. Client includes token in requests: `Authorization: Bearer <token>`
4. API Gateway validates JWT before routing to services
5. Expired tokens return 401; use refresh token to get new access token

**Role-Based Access Control (RBAC):**
- **PATIENT** - Book appointments, view own records, chat with doctors, use AI chatbot
- **DOCTOR** - Manage schedules, create medical records, chat with patients, access AI tools
- **ADMIN** - Full user management, content moderation, system analytics

**Security:**
- Passwords encrypted with BCrypt
- Stateless sessions (no server-side storage)
- JWT tokens cached in Redis for fast validation

## How to Run the Project

**Prerequisites:**
- Java 17+, Maven 3.8+
- Docker & Docker Compose
- Python 3.10 (for AI Service)
- Cloudinary account, Google Gemini API key

**Quick Start with Docker Compose:**
```cmd
docker-compose up -d
```

**Manual Setup:**
1. Configure environment variables (see `.env` example in project)
2. Start infrastructure: `docker-compose up -d postgres-db rabbitmq redis`
3. Start services in order:
   - EurekaServer (8761)
   - GatewayService (8080)
   - All microservices (UserService, HealthRecordService, etc.)
   - AIService: `cd AIService && python main.py`
4. Verify at Eureka Dashboard: http://localhost:8761
5. Access API Gateway: http://localhost:8080

## What I Learned

This project demonstrates my backend development skills and understanding of enterprise software practices:

- **Microservices Architecture** - Designed and implemented 8 independent services with API Gateway and service discovery patterns
- **Spring Boot & Spring Cloud** - Built production-ready REST APIs with Spring Security, Spring Data JPA, and inter-service communication using OpenFeign
- **Database Design** - Designed normalized schemas with proper entity relationships (one-to-many, many-to-many) across PostgreSQL and MongoDB
- **Security & Authentication** - Implemented JWT authentication with role-based access control and stateless session management
- **Software Design Patterns** - Applied Global Exception Handler pattern for centralized error handling and API Response Wrapper pattern for consistent response structure across all services
- **AI/ML Development** - Built and deployed machine learning models using Python, FastAPI, and integrated with Java microservices ecosystem
- **Asynchronous Communication** - Used RabbitMQ for event-driven architecture and Redis for caching to improve performance
- **Real-time Features** - Integrated WebSocket for real-time chat functionality between users
- **API Integration** - Connected Java microservices with Python AI service and third-party APIs (Cloudinary, email services)
- **DevOps Practices** - Containerized all services with Docker and configured multi-container orchestration with Docker Compose

This project was developed to demonstrate my backend engineering skills with Java Spring Boot in a real-world healthcare domain.

The detailed project documentation is stored in the following Google Drive folder: https://drive.google.com/drive/folders/1D7vcMEsy38kssL-JhwqyOjmv60fDNYwP?usp=sharing
   