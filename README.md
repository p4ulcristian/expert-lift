# Expert Lift

Elevator service management system built with Clojure/ClojureScript for tracking maintenance work, generating worksheets, and managing technician assignments.

## Overview

Expert Lift is a web-based management system for elevator service companies. It enables technicians and administrators to create, manage, and track elevator maintenance and repair work through digital worksheets with PDF generation capabilities.

## Quick Start

### Prerequisites
- Clojure/ClojureScript development environment
- Node.js and npm
- PostgreSQL database

### Installation
```bash
# Install dependencies
npm install

# Set up database schema
cd database-schema
./apply-schema.sh
```

### Development
```bash
# Start development server
./start-dev.sh app

# The application will be available at http://localhost:3002
```

## System Features

### User Management
- **Employee Role**: Create and edit their own worksheets
- **Admin Role**: Manage addresses, edit any worksheet, configure system settings
- **Super Admin Role**: Full system administration

### Worksheet Management
- Digital worksheet creation with automatic serial numbering (`YYYY-MM-DD/###`)
- Work type classification: repair/maintenance/other
- Service type tracking: normal/night/weekend-holiday hours
- Material usage tracking with quantities
- Arrival and departure time logging
- Automatic work duration calculation (rounded up to nearest hour)

### PDF Generation
- Automatic PDF worksheet generation
- Customizable company logo and footer text
- Digital signature support
- Server-side PDF storage with download links
- PDF naming convention: `work_type + date + serial.pdf`

### Address & Customer Management
- Elevator location tracking with postal codes
- Customer information management
- Address-based worksheet assignment

## Database Schema

### Core Tables

**Users**
- Username, full name, encrypted password
- Role-based access control (employee/admin/superadmin)
- Address associations with search indexing

**Elevators**
- Elevator identification codes
- Location tracking by postal code
- Service history linkage

**Worksheets**
- Unique serial number generation
- Creation, arrival, and departure timestamps
- Work categorization and descriptions
- Material usage vectors with quantities
- PDF file path storage
- User and location associations

**Settings**
- Configurable company logo for worksheets
- Customizable footer text for PDF generation

## Architecture

### Frontend
- ClojureScript with Reagent/React
- Single-page application (SPA) architecture
- Shadow-CLJS for development and building
- Webpack for external dependencies

### Backend
- Clojure web server with Ring/Reitit routing
- ParQuery data layer for frontend-backend communication
- PostgreSQL with connection pooling
- PDF generation and file storage

### Development Stack
- **Frontend**: ClojureScript, Reagent, Re-frame
- **Backend**: Clojure, Ring, Reitit
- **Database**: PostgreSQL with HCL schema management
- **Build Tools**: Shadow-CLJS, Webpack
- **Development**: Hot reloading, REPL-driven development

## Configuration

### Required Environment Variables

```bash
# Development mode
export EXPERTLIFT_DEV="true"

# Server configuration
export EXPERTLIFT_PORT="3002"
export EXPERTLIFT_DOMAIN="your-domain.com"

# Database
export EXPERTLIFT_DB_URL="postgresql://username:password@localhost:5432/expert_lift"

# File storage
export EXPERTLIFT_MINIO_URL="http://localhost:9000"
```

## Project Structure

```
expert-lift/
├── project/
│   ├── code/
│   │   ├── app/               # Main application entry points
│   │   ├── features/          # Feature modules
│   │   │   └── app/          # Core app features
│   │   │       ├── homepage/  # Landing page
│   │   │       └── zero/      # Framework integration
│   │   ├── zero/             # Custom web framework
│   │   ├── ui/               # Reusable UI components
│   │   ├── parquery/         # Data layer
│   │   └── router/           # Routing system
│   └── resources/
│       └── public/           # Static assets and compiled JS
└── database-schema/          # Database migrations and schema
```

## Development Commands

```bash
# Start development server
./start-dev.sh app

# Start Clojure REPL (connects on port 7888)
clj -X:zero

# Build for production
./start-prod.sh
```

## Features in Development

- Digital worksheet creation and management
- PDF generation with company branding
- User role management and permissions
- Elevator and address management
- Time tracking and billing calculations
- Material usage reporting

## Contributing

1. Follow Clojure code style conventions
2. Keep functions small and focused (max 30 lines)
3. Use descriptive commit messages
4. Ensure all tests pass before submitting changes

## License

[License information to be added]