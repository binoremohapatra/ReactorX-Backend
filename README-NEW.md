# ReactorX Backend - Production Ready E-commerce API

## Overview
A fast, stable, production-ready Spring Boot ecommerce backend that has been fully optimized, cleaned, and fixed for cloud deployment.

## Features
- **High Performance**: Optimized database queries, lazy loading, connection pooling
- **Production Ready**: Environment variable configuration, health checks, monitoring
- **Secure**: JWT authentication, proper error handling, security best practices
- **Scalable**: Docker containerized, cloud deployment ready
- **Maintainable**: Clean code structure, proper logging, comprehensive documentation

## Tech Stack
- **Java 17** with Spring Boot 3.2.0
- **PostgreSQL** database with HikariCP connection pooling
- **JWT** authentication and authorization
- **Docker** multi-stage builds for production
- **Maven** for dependency management
- **Lombok** for code reduction

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 15+
- Docker (optional)

### Local Development

1. **Clone the repository**
```bash
git clone <repository-url>
cd ReactorX-Backend-main
```

2. **Set up environment variables**
```bash
cp .env.example .env
# Edit .env with your database credentials
```

3. **Run with Maven**
```bash
mvn spring-boot:run
```

4. **Run with Docker**
```bash
docker-compose up -d
```

### Access Points
- **API Base URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/api/health
- **Actuator**: http://localhost:8080/actuator/health

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product details
- `GET /api/products/category/{slug}` - Get products by category
- `GET /api/products/search?term={query}` - Search products

### Categories
- `GET /api/categories` - Get all categories

### Shopping Cart
- `GET /api/cart` - Get user cart
- `POST /api/cart/add` - Add item to cart
- `PUT /api/cart/update` - Update cart item
- `DELETE /api/cart/remove/{id}` - Remove cart item

### Orders
- `GET /api/orders` - Get user orders
- `POST /api/checkout` - Create order
- `GET /api/orders/{id}` - Get order details

## Configuration

### Environment Variables
See `.env.example` for all available configuration options.

### Database Setup
The application automatically creates database schema on startup using Hibernate DDL.

### Performance Settings
- **Memory**: 512MB - 2GB heap
- **Connections**: Up to 20 database connections
- **Caching**: Method-level caching for products
- **Lazy Loading**: Optimized for large JSON fields

## Deployment

### Render (Recommended)
1. Connect repository to Render
2. Use provided `render.yaml` configuration
3. Auto-deploy on push

### Railway
1. Connect repository to Railway
2. Set environment variables
3. Auto-deploy on push

### Docker Production
```bash
docker build -t reactorx-backend .
docker run -d -p 8080:8080 --env-file .env reactorx-backend
```

## Performance Optimizations Applied

### Database Performance
- **Connection Pooling**: HikariCP with optimized settings
- **Lazy Loading**: All LOB fields use FetchType.LAZY
- **Batch Processing**: Enabled for bulk operations
- **Query Optimization**: Proper indexing and fetch strategies

### Application Performance
- **Lazy Initialization**: Faster startup times
- **Memory Management**: G1GC with optimized heap settings
- **Caching**: Method-level caching for frequently accessed data
- **Transaction Management**: Read-only transactions where appropriate

### Production Optimizations
- **Health Checks**: Comprehensive health monitoring
- **Error Handling**: Structured error responses with proper logging
- **Security**: Non-root Docker user, secure defaults
- **Monitoring**: Actuator endpoints for production monitoring

## Security Features

### Authentication & Authorization
- **JWT Tokens**: Secure token-based authentication
- **Password Encryption**: BCrypt hashing
- **CORS Configuration**: Proper cross-origin settings
- **Role-based Access**: Method-level security

### Production Security
- **Environment Variables**: No hardcoded secrets
- **Docker Security**: Non-root user execution
- **Input Validation**: Comprehensive DTO validation
- **SQL Injection Prevention**: JPA/Hibernate protection

## Monitoring & Logging

### Health Monitoring
- Application health endpoints
- Database connectivity checks
- Memory and performance metrics

### Structured Logging
- SLF4J with proper log levels
- Request/response logging
- Error tracking and alerting

## Troubleshooting

### Common Issues
1. **Database Connection**: Check DATABASE_URL format
2. **JWT Issues**: Verify JWT_SECRET is Base64 encoded
3. **Memory Issues**: Monitor heap usage and adjust JVM settings

### Debug Mode
Enable debug logging:
```bash
export SHOW_SQL=true
export APP_LOG_LEVEL=DEBUG
```

## Development

### Code Structure
```
src/main/java/com/reactorx/
  config/          - Configuration classes
  controller/      - REST endpoints
  dto/            - Data transfer objects
  entity/         - JPA entities
  exception/      - Exception handling
  repository/     - JPA repositories
  security/       - JWT and security
  service/        - Business logic
```

### Adding New Features
1. Create entity in `entity/` package
2. Add repository in `repository/` package
3. Implement service in `service/` package
4. Create controller in `controller/` package
5. Add DTOs in `dto/` package

## Contributing

### Code Standards
- Use Lombok annotations
- Follow Spring Boot conventions
- Add proper logging
- Write unit tests
- Update documentation

### Testing
```bash
mvn test
mvn test-compile
```

## License

This project is licensed under the MIT License.

## Support

For deployment and development support:
1. Check the deployment guide in `DEPLOYMENT.md`
2. Review application logs for errors
3. Verify environment variables
4. Test health check endpoints
