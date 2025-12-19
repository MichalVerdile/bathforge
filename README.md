# BathForge

A full-stack web application for 3D bathroom design and product management. BathForge allows users to visualize and configure bathroom products in real-time using an interactive 3D configurator, while providing a comprehensive product management system with automated asset importing.

## Description

BathForge combines modern web technologies with 3D visualization to create an intuitive platform for bathroom design. The application automatically imports 3D assets and product images into a structured database, exposes a RESTful API for product management, and provides an interactive React-based user interface with real-time 3D rendering capabilities.

## Features

- **3D Product Visualization** - Interactive 3D viewer using Three.js for realistic product rendering
- **Automated Asset Import** - Scans and imports 3D models and images from the assets directory
- **Product Management** - Complete CRUD operations for products, categories, and colors
- **Advanced Filtering** - Search and filter products by category, price range, mounting type, and name
- **JWT Authentication** - Secure API endpoints with JSON Web Token authentication
- **AI-Powered Assistance** - OpenAI integration for intelligent product recommendations
- **Email Notifications** - SMTP integration for user communications
- **Responsive Design** - Mobile-friendly interface with modern UI components
- **Real-time Configuration** - Dynamic bathroom layout configuration with instant visual feedback

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security with JWT
- Gradle
- H2 Database (development)
- PostgreSQL (production)
- OpenAI API
- JavaMail

### Frontend
- React 18
- TypeScript 5
- Vite
- Three.js with React Three Fiber
- React Three Drei
- Axios
- React Router DOM
- React Icons

## Project Structure

```
bathforge/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bathforge/
│   │   │   │   ├── controller/      # REST API endpoints
│   │   │   │   ├── model/           # JPA entities
│   │   │   │   ├── repository/      # Data access layer
│   │   │   │   ├── service/         # Business logic
│   │   │   │   └── security/        # Authentication & authorization
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── static/          # Static assets
│   │   └── test/                    # Unit and integration tests
│   ├── build.gradle
│   └── gradlew
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── 3d/                  # Three.js components
│   │   │   ├── admin/               # Admin interface
│   │   │   ├── configurator/        # Product configurator
│   │   │   └── user/                # User interface
│   │   ├── controllers/             # API client controllers
│   │   ├── types/                   # TypeScript definitions
│   │   └── utils/                   # Utility functions
│   ├── public/
│   │   └── assets/                  # 3D models and images
│   ├── package.json
│   └── vite.config.ts
└── docs/                            # Documentation
```

## Installation

### Prerequisites

- Java 21 or higher
- Node.js 22.x
- Gradle (included via wrapper)
- Git

### Clone the Repository

```bash
git clone https://github.com/yourusername/bathforge.git
cd bathforge
```

### Backend Setup

```bash
cd backend
./gradlew build
```

On Windows:
```bash
gradlew.bat build
```

### Frontend Setup

```bash
cd frontend
npm install
```

## Configuration

### Environment Variables

Create a `.env` file or set the following environment variables for production deployment:

#### Database Configuration
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bathforge
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password
HIBERNATE_DDL_AUTO=update
```

#### OpenAI Configuration
```properties
OPENAI_API_KEY=your_openai_api_key
OPENAI_API_MODEL=gpt-4
OPENAI_API_TIMEOUT=150000
```

#### Email Configuration
```properties
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=your_smtp_username
SMTP_PASSWORD=your_smtp_password
SMTP_AUTH=true
SMTP_STARTTLS_ENABLE=true
```

#### H2 Console (Development Only)
```properties
H2_CONSOLE_ENABLED=true
```

### Asset Import Configuration

Configure automatic asset import in `application.properties`:

```properties
bathforge.auto-import.enabled=true
bathforge.auto-import.skip-if-products-exist=true
```

- `enabled=true` with `skip-if-products-exist=true` - Import only if database is empty (default)
- `enabled=true` with `skip-if-products-exist=false` - Always import on startup
- `enabled=false` - Disable automatic import

## Usage

### Running the Application

#### Start the Backend

```bash
cd backend
./gradlew bootRun
```

The backend API will be available at `http://localhost:8080`

#### Start the Frontend

```bash
cd frontend
npm run dev
```

The frontend application will be available at `http://localhost:5173`

### Development Mode

For H2 console access during development:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Categories Endpoints

```
GET    /api/categories           # Get all categories
GET    /api/categories/{id}      # Get category by ID
POST   /api/categories           # Create new category (admin)
PUT    /api/categories/{id}      # Update category (admin)
DELETE /api/categories/{id}      # Delete category (admin)
```

### Products Endpoints

```
GET    /api/products                        # Get all products
GET    /api/products/{id}                   # Get product by ID
GET    /api/products/category/{categoryId}  # Get products by category
POST   /api/products                        # Create product (admin)
PUT    /api/products/{id}                   # Update product (admin)
DELETE /api/products/{id}                   # Delete product (admin)
```

### Colors Endpoints

```
GET    /api/colors                            # Get all colors
GET    /api/colors/{id}                       # Get color by ID
GET    /api/colors/category/{categoryId}      # Get colors by category
POST   /api/colors                            # Create color (admin)
PUT    /api/colors/{id}                       # Update color (admin)
DELETE /api/colors/{id}                       # Delete color (admin)
```

### Authentication

All admin endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## Testing

### Backend Tests

Run all tests with coverage:

```bash
cd backend
./gradlew test jacocoTestReport
```

View coverage report:
```bash
open build/reports/jacoco/test/html/index.html
```

### Frontend Tests

Run tests:

```bash
cd frontend
npm test
```

Run tests with coverage:

```bash
npm test -- --coverage
```

## Deployment

### Building for Production

#### Backend

```bash
cd backend
./gradlew bootJar
```

The executable JAR will be created in `build/libs/`

#### Frontend

```bash
cd frontend
npm run build
```

The production build will be created in `build/`

### Deployment Options

The project includes `nixpacks.toml` configuration files for both frontend and backend, making it compatible with platforms like Railway, Render, or other Nixpacks-supporting hosts.

#### Environment Variables for Production

Ensure all required environment variables are set in your deployment platform:
- Database credentials
- OpenAI API key
- SMTP configuration
- Set `H2_CONSOLE_ENABLED=false` for security

## Contributing

Contributions are welcome. Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Create a Pull Request

### Code Style

- Backend: Follow standard Java conventions and Spring Boot best practices
- Frontend: Use TypeScript strict mode and ESLint rules
- Write unit tests for new features
- Ensure all tests pass before submitting PR

## License

This project is licensed under the MIT License.

## Authors
- Antonio Michal Verdile
- Mees Zonneveld

Developed as part of a semester project at ZHAW (Zurich University of Applied Sciences)

---

## Frontend Controllers (TypeScript)
- Extend **`BaseController`** for shared HTTP (get/post/put/delete)
- Central **error handling**, **JWT token** attachment via interceptors
- Strong typing for requests/responses for IntelliSense & safety

**Import examples**
```ts
import { systemController } from "./controllers";
const res = await systemController.testConnection();

import { systemController, bathroomController, authController } from "./controllers";
import { ApiResponse, Bathroom, CreateBathroomRequest } from "./controllers";
```

---

## Dev Tips
- Backend tests: `./gradlew test`
- Frontend tests: `npm test`
- Quick API check:
```bash
curl http://localhost:8080/api/products
```

---

## Production
- **Backend**: `./gradlew build` → jar in `backend/build/libs/`
- **Frontend**: `npm run build` → static build in `frontend/build/`
- Switch DB to PostgreSQL in `application.properties` for production.

---

## License
MIT
