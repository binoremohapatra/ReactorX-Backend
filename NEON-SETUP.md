# Neon Database Setup Guide for ReactorX Backend

## Why Neon?
- **Serverless PostgreSQL**: No database management required
- **Auto-scaling**: Handles traffic automatically
- **Free Tier**: Generous free plan for development
- **Fast Connection**: Optimized for cloud applications
- **SSL Support**: Secure connections by default

## Step 1: Create Neon Account

1. Go to [https://neon.tech](https://neon.tech)
2. Sign up for a free account
3. Create a new project

## Step 2: Get Connection Details

1. In your Neon dashboard, go to your project
2. Click on **Connection Details**
3. Copy the connection string (looks like):
   ```
   postgresql://username:password@ep-xxx.us-east-2.aws.neon.tech/dbname?sslmode=require
   ```

## Step 3: Set Environment Variables

### For Local Development (PowerShell):
```powershell
$env:DATABASE_URL = "jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/dbname?sslmode=require"
$env:DATABASE_USERNAME = "your_username"
$env:DATABASE_PASSWORD = "your_password"
$env:JWT_SECRET = "your_512_bit_base64_secret"
$env:SPRING_PROFILES_ACTIVE = "neon"
```

### For Docker (.env file):
```bash
DATABASE_URL=jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/dbname?sslmode=require
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
JWT_SECRET=your_512_bit_base64_secret
SPRING_PROFILES_ACTIVE=neon
```

### For Render Deployment:
1. In Render dashboard, go to your service
2. Add Environment Variables:
   - `DATABASE_URL`: Your Neon connection string
   - `DATABASE_USERNAME`: Your Neon username
   - `DATABASE_PASSWORD`: Your Neon password
   - `SPRING_PROFILES_ACTIVE`: `neon`

## Step 4: Test the Connection

### Option 1: Using PowerShell Test Script
```powershell
# Set your Neon credentials first
$env:DATABASE_URL = "jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/dbname?sslmode=require"
$env:DATABASE_USERNAME = "your_username"
$env:DATABASE_PASSWORD = "your_password"

# Run the test
powershell -ExecutionPolicy Bypass -File test-neon.ps1
```

### Option 2: Manual Test
```powershell
$env:SPRING_PROFILES_ACTIVE="neon"
mvn spring-boot:run
```

Then test endpoints:
- Health: http://localhost:8080/api/health
- Categories: http://localhost:8080/api/categories
- Products: http://localhost:8080/api/products

## Step 5: Docker Deployment with Neon

```bash
# Set environment variables
export DATABASE_URL="jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/dbname?sslmode=require"
export DATABASE_USERNAME="your_username"
export DATABASE_PASSWORD="your_password"
export SPRING_PROFILES_ACTIVE="neon"

# Run with Docker
docker-compose up -d
```

## Benefits of Neon vs H2/Local PostgreSQL

| Feature | Neon | H2 | Local PostgreSQL |
|---------|------|----|------------------|
| **Setup** | 2 minutes | Instant | 30+ minutes |
| **Scalability** | Auto-scaling | Limited | Manual |
| **Backups** | Automatic | None | Manual |
| **SSL** | Built-in | No | Manual setup |
| **Free Tier** | 3GB | Unlimited | VPS costs |
| **Production Ready** | Yes | No | Yes |
| **Connection Speed** | Fast | Fastest | Local only |

## Troubleshooting

### Connection Issues
1. **Check SSL**: Ensure `sslmode=require` in connection string
2. **Firewall**: Neon allows all connections by default
3. **Credentials**: Verify username and password from Neon dashboard

### Performance Issues
1. **Connection Pool**: Already configured (15 connections max)
2. **Batch Size**: Set to 20 for optimal performance
3. **SSL Overhead**: Minimal for most applications

### Common Errors
- **SSL Error**: Add `?sslmode=require` to connection string
- **Timeout**: Increase connection timeout in settings
- **Auth Failed**: Double-check username/password

## Production Best Practices

1. **Use Neon Branches**: Create separate branches for dev/staging/prod
2. **Monitor Usage**: Check Neon dashboard for usage metrics
3. **Connection Pooling**: Already configured in application-neon.properties
4. **Backup Strategy**: Neon handles automatic backups
5. **Environment Variables**: Never commit credentials to git

## Migration from Local PostgreSQL

If you have existing data:
1. Export from local PostgreSQL
2. Import to Neon using `pg_dump` and `psql`
3. Update connection string
4. Test application

Your ReactorX backend is now ready for production with Neon!
