# IronRainbow

Full-stack Clojure/ClojureScript web application with multi-module frontend architecture.

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

#### Start Development Server
Requires specifying frontend configuration:
```bash
# Start with single module
./start-dev.sh site

# Start with multiple modules
./start-dev.sh flex customizer
```

Available modules:
- `site` - Public website
- `flex` - Business management application
- `customizer` - Product customization interface
- `labs` - Development/experimental features

#### Backend Development
```bash
# Start Clojure REPL
clj -X:zero

# REPL connects on port 5555
```

#### Production Build
```bash
./start-prod.sh
```

### Railway Deployment

Railway builds using the root `Dockerfile` by default. The included `railway.toml` selects the development Dockerfile:

```toml
[build]
dockerfilePath = "Dockerfile.dev"
```

If configuring the service manually, set the `RAILWAY_DOCKERFILE_PATH` variable to `Dockerfile.dev`.

## Architecture

### Frontend Architecture

Multi-module system with independent frontend applications:

| Module | Path | Description |
|--------|------|-------------|
| Site | `/` | Public-facing website |
| Flex | `/app` | Business management application |
| Customizer | Custom routes | Product customization interface |
| Labs | Development | Experimental features |

Each module includes:
- Shadow-CLJS build configuration (`shadow-cljs.edn`)
- Webpack configuration for external JS dependencies
- Entry point in `app/frontend/`
- Backend routes in `app/z_*/backend/`

### Backend Architecture

| Component | Purpose |
|-----------|---------|
| Zero Framework | Custom web framework for server, routing, and state |
| Pathom3 | GraphQL-like data layer for frontend-backend communication |
| Mount | State management and dependency injection |
| PostgreSQL | Primary database with HCL-based schema management (Atlas) |
| Ring/Reitit | HTTP handling and routing |

### Project Structure

```
ironrainbow/
├── project/
│   ├── code/
│   │   ├── app/           # Main application modules
│   │   ├── features/      # Feature-specific code by domain
│   │   ├── zero/          # Core framework code
│   │   └── ui/            # Reusable UI components (Reagent/React)
│   └── resources/
│       └── public/        # Static assets and compiled JS
└── database-schema/       # Database definitions and migrations
```

### Data Flow

- **Frontend State**: Re-frame with modern React hooks (no Clojure atoms)
- **Data Fetching**: Pathom resolvers for GraphQL-like queries
- **Database Access**: PG2 connection pooling
- **File Storage**: MinIO integration for object storage

## Configuration

### Required Environment Variables

#### Core Settings
```bash
# Development mode flag
export IRONRAINBOW_DEV="true"

# Server port (default: 4000)
export IRONRAINBOW_PORT="4000"

# Domain for the application
export IRONRAINBOW_DOMAIN="yourdomain.com"

# JavaScript version for cache busting
export IRONRAINBOW_JS_VERSION="1.0.0"
```

#### Database Configuration
```bash
# Database connection URL for schema management
export IRONRAINBOW_DB_URL="postgresql://username:password@localhost:5432/dbname"
```

#### Authentication (Auth0)
```bash
# Customizer module
export IRONRAINBOW_AUTH0_CUSTOMIZER_CLIENT_ID="your_customizer_client_id"
export IRONRAINBOW_AUTH0_CUSTOMIZER_SECRET="your_customizer_secret"

# Flex module
export IRONRAINBOW_AUTH0_FLEX_CLIENT_ID="your_flex_client_id"
export IRONRAINBOW_AUTH0_FLEX_SECRET="your_flex_secret"

# Labs module
export IRONRAINBOW_AUTH0_LABS_CLIENT_ID="your_labs_client_id"
export IRONRAINBOW_AUTH0_LABS_SECRET="your_labs_secret"
```

#### Email Configuration
```bash
export IRONRAINBOW_EMAIL_HOST="mail.privateemail.com"
export IRONRAINBOW_EMAIL_PORT="465"
export IRONRAINBOW_EMAIL_SSL="true"
export IRONRAINBOW_EMAIL_USERNAME="your_email@domain.com"
export IRONRAINBOW_EMAIL_PASSWORD="your_email_password"
```

#### File Storage (MinIO)
```bash
export IRONRAINBOW_MINIO_URL="http://localhost:9000"
```


### Development Tools

- **Shadow-CLJS**: Frontend hot reloading and build tool
- **REPL**: Interactive development on port 5555
- **Live Reloading**: Automatic for both frontend and backend changes

## Testing

Run tests appropriate for your environment. The testing framework supports both unit and integration tests.

## Contributing

1. Follow the code style guidelines in the project
2. Ensure all tests pass before submitting changes
3. Keep functions small and focused (30 lines maximum)
4. Use descriptive commit messages

## License

[License information here]