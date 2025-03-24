# Online Store Management System

## Project Description

This project is a RESTful API for managing an online store with order processing functionality. The system allows for user, product, and order management, ensuring secure authentication and authorization of users.

### Key Features:

- **User Management**: registration, authentication, information retrieval, deletion
- **Product Management**: adding, updating, listing, deletion (using Soft Delete)
- **Order Management**: creation, details retrieval, cancellation, order history viewing
- **Security**: API protection using JWT tokens
- **Caching**: Redis implementation for data caching
- **Containerization**: ready for deployment via Docker

## Tech Stack

- **Java 17**
- **Spring Boot 3.4.4**
- **Spring Security** (OAuth2 Resource Server, JWT)
- **Spring Data JPA**
- **PostgreSQL** (for data storage)
- **Redis** (for caching)
- **Flyway** (for database migrations)
- **Docker** (for containerization)
- **Maven** (for dependency management)

## Architecture

The project is built on a multi-layered architecture:

- **Controller layer**: handling HTTP requests and responses
- **Service layer**: business logic
- **Repository layer**: data access
- **Entity layer**: data models
- **DTO layer**: data transfer objects

## Implementation Features

- **Soft Delete for Products**: products are marked as deleted instead of being physically removed, preserving historical data
- **Immutable Orders**: order details (product names, prices) are stored at creation, ensuring data integrity even when products are deleted
- **Product List Caching**: using Redis to speed up access to frequently requested data
- **Secure Authentication**: utilizing JWT tokens for user authorization

## Launch Instructions

### Prerequisites

- JDK 17+
- Maven
- Docker and Docker Compose

### Option 1: Launch via Docker Compose (recommended)

1. Clone the repository:
```bash
git clone https://github.com/PavloDymohlo/onlineStore.git
cd onlineStore
```

2. Build the project:
```bash
./mvnw clean package
```

3. Launch the containers:
```bash
docker-compose up -d
```

4. The application will be available at: `http://localhost:8080`

### Option 2: Local Launch

1. Clone the repository:
```bash
git clone https://github.com/PavloDymohlo/onlineStore.git
cd onlineStore
```

2. Configure application.properties for connecting to local PostgreSQL and Redis

3. Start PostgreSQL and Redis

4. Build and run the project:
```bash
./mvnw spring-boot:run
```

5. The application will be available at: `http://localhost:8080`

## API Endpoints

### Users
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate a user
- `GET /api/users/profile/{email}` - Get user profile
- `DELETE /api/users/profile/{email}` - Delete a user

### Products
- `GET /api/products` - Get list of products
- `POST /api/products` - Add a new product (admin only)
- `PUT /api/products/{productName}` - Update a product (admin only)
- `DELETE /api/products/{productName}` - Delete a product (admin only)

### Orders
- `POST /api/orders` - Create a new order
- `GET /api/orders/{orderNumber}` - Get order details
- `POST /api/orders/{orderNumber}/cancel` - Cancel an order
- `GET /api/orders/user/{email}` - Get a list of user's orders

## Security

All API endpoints, except registration and authentication, require a JWT token in the `Authorization` header. The token is automatically issued during user registration and authentication.

## Monitoring

Application monitoring is implemented using Spring Boot Actuator. Endpoints are available at `/actuator`.

