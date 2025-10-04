# 🛁 BathForge

BathForge is a modern web application for bathroom design and planning. The project consists of a **Java Spring Boot backend** with **Gradle** build system and a **React TypeScript frontend**.

## Project Structure

```
bathforge/
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/bathforge/
│   │   ├── BathForgeApplication.java    # Main application class
│   │   ├── controller/                  # REST controllers
│   │   ├── service/                     # Business logic services
│   │   ├── model/                       # Entity models
│   │   └── repository/                  # Data access layer
│   ├── src/main/resources/
│   │   └── application.properties       # Configuration
│   ├── src/test/                        # Test files
│   ├── build.gradle                     # Gradle build configuration
│   ├── gradlew                          # Gradle wrapper (Unix)
│   └── gradlew.bat                      # Gradle wrapper (Windows)
├── frontend/                   # React TypeScript app
│   ├── public/                          # Static assets
│   ├── src/                             # React components
│   │   ├── App.tsx                      # Main App component
│   │   ├── index.tsx                    # Entry point
│   │   └── index.css                    # Styles
│   ├── package.json                     # NPM dependencies
│   └── tsconfig.json                    # TypeScript configuration
└── README.md                   # This file
```

## Prerequisites

Before running this project, make sure you have the following installed:

- **Java 17 or higher** - For the Spring Boot backend
- **Node.js 16 or higher** - For the React frontend
- **npm or yarn** - Package manager for frontend dependencies

## Getting Started

### 1. Backend Setup (Spring Boot + Gradle)

Navigate to the backend directory:
```bash
cd backend
```

#### On Windows:
```powershell
# Build the project
.\gradlew build

# Run the application
.\gradlew bootRun
```

#### On Unix/Linux/Mac:
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The backend will start on **http://localhost:8080**

**Available endpoints:**
- `GET /api/` - Welcome message
- `GET /api/health` - Health check
- `GET /h2-console` - H2 Database console (development only)

### 2. Frontend Setup (React + TypeScript)

Navigate to the frontend directory:
```bash
cd frontend
```

Install dependencies:
```bash
npm install
```

Start the development server:
```bash
npm start
```

The frontend will start on **http://localhost:3000** and automatically proxy API calls to the backend.

## Development

### Backend Development

- **Main Application**: `backend/src/main/java/com/bathforge/BathForgeApplication.java`
- **Controllers**: Add REST endpoints in `backend/src/main/java/com/bathforge/controller/`
- **Services**: Business logic in `backend/src/main/java/com/bathforge/service/`
- **Models**: Entity classes in `backend/src/main/java/com/bathforge/model/`
- **Configuration**: Modify `backend/src/main/resources/application.properties`

### Frontend Development

- **Main Component**: `frontend/src/App.tsx`
- **Styling**: `frontend/src/index.css`
- **API Calls**: Using Axios, configured with proxy to backend

### Database

The application is configured to use **H2 in-memory database** for development:
- **URL**: `jdbc:h2:mem:testdb`
- **Console**: http://localhost:8080/h2-console
- **Username**: `sa`
- **Password**: (empty)

For production, update `application.properties` to use PostgreSQL or another database.

## Building for Production

### Backend
```bash
cd backend
.\gradlew build  # Windows
./gradlew build  # Unix/Linux/Mac
```

The JAR file will be created in `backend/build/libs/`

### Frontend
```bash
cd frontend
npm run build
```

The production build will be created in `frontend/build/`

## Testing

### Backend Tests
```bash
cd backend
.\gradlew test  # Windows
./gradlew test  # Unix/Linux/Mac
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Contributing

1. Create a feature branch
2. Make your changes
3. Add tests for new functionality
4. Ensure all tests pass
5. Create a pull request

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Web** - REST API
- **Spring Data JPA** - Database access
- **Spring Security** - Authentication & authorization
- **H2 Database** - Development database
- **Gradle** - Build tool

### Frontend
- **React 18**
- **TypeScript**
- **Axios** - HTTP client
- **React Router** - Client-side routing
- **Create React App** - Development setup

## License

This project is licensed under the MIT License.