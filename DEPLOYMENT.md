# ReactorX Backend - Deployment Guide

## Overview
This guide covers deploying the ReactorX ecommerce backend to production environments.

## Environment Variables

### Required Environment Variables
- `DATABASE_URL` - PostgreSQL connection string
- `DATABASE_USERNAME` - Database username  
- `DATABASE_PASSWORD` - Database password
- `JWT_SECRET` - 512-bit Base64 encoded secret key

### Optional Environment Variables
- `PORT` - Server port (default: 8080)
- `JWT_EXPIRATION_MS` - JWT token expiration (default: 86400000)
- `ALLOW_REGISTRATION` - Enable user registration (default: true)
- `JPA_DDL_AUTO` - Hibernate DDL mode (default: update)
- `SHOW_SQL` - Show SQL logs (default: false)
- `SPRING_PROFILES_ACTIVE` - Spring profile (default: dev)

## Deployment Options

### 1. Docker Compose (Local Development)

```bash
# Copy environment template
cp .env.example .env

# Update .env with your values
nano .env

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f backend
```

### 2. Render Deployment

1. Connect your GitHub repository to Render
2. Use the provided `render.yaml` configuration
3. Render will automatically:
   - Build the Docker image
   - Set up PostgreSQL database
   - Configure environment variables
   - Deploy and monitor the application

### 3. Railway Deployment

1. Push code to GitHub
2. Connect repository to Railway
3. Set environment variables in Railway dashboard
4. Railway will build and deploy automatically

### 4. Manual Docker Deployment

```bash
# Build image
docker build -t reactorx-backend .

# Run container
docker run -d \
  --name reactorx-backend \
  -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://your-db:5432/reactorx_db" \
  -e DATABASE_USERNAME="your_username" \
  -e DATABASE_PASSWORD="your_password" \
  -e JWT_SECRET="your_512_bit_secret" \
  reactorx-backend
```

## Health Checks

The application exposes health endpoints:
- `/api/health` - Basic health check
- `/actuator/health` - Detailed health information (prod only)

## Performance Monitoring

### JVM Metrics
- Memory usage: 512MB - 2GB heap
- GC: G1 Garbage Collector
- Connection pool: HikariCP (20 max connections)

### Database Optimization
- Lazy loading for LOB fields
- Connection pooling configured
- Batch processing enabled

## Security Considerations

1. **JWT Secret**: Use a strong, randomly generated 512-bit Base64 secret
2. **Database**: Use strong passwords and SSL connections in production
3. **Environment**: Never commit secrets to version control
4. **Docker**: Running as non-root user for security

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify DATABASE_URL format
   - Check database credentials
   - Ensure database is accessible

2. **JWT Token Issues**
   - Verify JWT_SECRET is properly Base64 encoded
   - Check token expiration settings

3. **Memory Issues**
   - Monitor JVM memory usage
   - Adjust heap size if needed
   - Check for memory leaks

### Log Analysis

```bash
# Docker logs
docker-compose logs backend

# Application logs
docker-compose exec backend tail -f /proc/1/fd/1
```

## Scaling Considerations

### Horizontal Scaling
- Use load balancer for multiple instances
- Configure sticky sessions if needed
- Consider Redis for session storage

### Database Scaling
- Read replicas for read-heavy workloads
- Connection pooling optimization
- Database indexing strategy

## Backup Strategy

### Database Backups
```bash
# PostgreSQL backup
pg_dump -h localhost -U username reactorx_db > backup.sql

# Restore backup
psql -h localhost -U username reactorx_db < backup.sql
```

### Application Backup
- Version control for code
- Environment variable documentation
- Configuration backup

## Monitoring Alerts

Set up monitoring for:
- Application uptime
- Database connection pool
- Memory usage
- Response times
- Error rates

## Performance Benchmarks

Expected performance:
- Startup time: <30 seconds
- API response time: <200ms (average)
- Memory usage: 512MB-2GB
- Database connections: 5-20 concurrent

## Support

For deployment issues:
1. Check application logs
2. Verify environment variables
3. Test database connectivity
4. Review health check endpoints
