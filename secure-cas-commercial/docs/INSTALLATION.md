# SecureCAS Commercial Edition - Installation Guide

## Table of Contents

1. [Pre-Installation](#pre-installation)
2. [Installation Methods](#installation-methods)
3. [Docker Installation](#docker-installation)
4. [Manual Installation](#manual-installation)
5. [Post-Installation](#post-installation)
6. [Troubleshooting](#troubleshooting)

## Pre-Installation

### System Requirements Verification

```bash
# Check system resources
free -h
df -h
nproc

# Check Docker version
docker --version
docker-compose --version

# Check network ports
sudo netstat -tulpn | grep -E ':(80|443|8080|8443|5432|6379)'
```

### Required Ports

| Port | Service | Description |
|------|---------|-------------|
| 80 | HTTP | Redirect to HTTPS |
| 443 | HTTPS | Main web interface |
| 8443 | CAS | CAS application port |
| 5432 | PostgreSQL | Database |
| 6379 | Redis | Session storage |
| 389 | LDAP | Directory service |

## Installation Methods

### Method 1: Docker (Recommended)

The Docker installation provides a complete, pre-configured environment.

### Method 2: Manual Installation

For organizations requiring custom deployment configurations.

## Docker Installation

### Step 1: Download SecureCAS

```bash
# Option 1: From release package
wget https://download.securecas.com/releases/securecas-commercial-latest.tar.gz
tar -xzf securecas-commercial-latest.tar.gz
cd securecas-commercial

# Option 2: From Git repository (requires access)
git clone https://github.com/securecas/commercial-edition.git
cd commercial-edition
```

### Step 2: Environment Configuration

```bash
# Copy example environment file
cp .env.example .env

# Generate secure passwords
openssl rand -base64 32 > .db_password
openssl rand -base64 32 > .redis_password
openssl rand -hex 32 > .signing_key
openssl rand -hex 32 > .encryption_key

# Edit .env file
nano .env
```

Example `.env` configuration:

```env
# General Settings
CAS_SERVER_HOST=sso.company.sa
ENVIRONMENT=production

# Database Configuration
DB_HOST=postgres
DB_PORT=5432
DB_NAME=cas
DB_USER=cas
DB_PASSWORD=$(cat .db_password)

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=$(cat .redis_password)

# LDAP Configuration
LDAP_URL=ldap://ldap.company.sa:389
LDAP_BASE_DN=dc=company,dc=sa
LDAP_BIND_DN=cn=cas,ou=services,dc=company,dc=sa
LDAP_BIND_PASSWORD=your-ldap-password
LDAP_SEARCH_FILTER=(sAMAccountName={user})

# Security Keys
CAS_WEBFLOW_SIGNING_KEY=$(cat .signing_key)
CAS_WEBFLOW_ENCRYPTION_KEY=$(cat .encryption_key)

# SSL Configuration
SSL_KEYSTORE_PASSWORD=changeit
SSL_KEY_PASSWORD=changeit

# Commercial Features
SECURECAS_LICENSE_KEY=your-license-key
SECURECAS_DASHBOARD_ENABLED=true
SECURECAS_MFA_ENABLED=true

# MFA Configuration (Optional)
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_FROM_NUMBER=+966XXXXXXXXX

# Oracle EBS Integration (Optional)
ORACLE_EBS_ENABLED=false
ORACLE_EBS_ENDPOINT=https://ebs.company.sa/integration
ORACLE_EBS_USERNAME=integration_user
ORACLE_EBS_PASSWORD=integration_password
```

### Step 3: SSL Certificate Setup

```bash
# For production - use your CA-signed certificates
mkdir -p ./ssl
cp /path/to/your/certificate.crt ./ssl/server.crt
cp /path/to/your/private.key ./ssl/server.key
cp /path/to/your/ca-bundle.crt ./ssl/ca-bundle.crt

# For testing - generate self-signed certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ./ssl/server.key \
  -out ./ssl/server.crt \
  -subj "/C=SA/ST=Riyadh/L=Riyadh/O=Company/CN=${CAS_SERVER_HOST}"
```

### Step 4: Deploy SecureCAS

```bash
# Start all services
docker-compose up -d

# Monitor startup logs
docker-compose logs -f

# Verify all services are running
docker-compose ps
```

### Step 5: Initialize Database

```bash
# The database is automatically initialized on first run
# To manually initialize or reset:
docker-compose exec cas bash -c "java -jar cas.war --init-db"
```

## Manual Installation

### Prerequisites

```bash
# Install Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Install Redis
sudo apt install redis-server

# Install Nginx
sudo apt install nginx
```

### Step 1: Database Setup

```sql
-- Create database and user
sudo -u postgres psql

CREATE DATABASE cas;
CREATE USER cas WITH ENCRYPTED PASSWORD 'your-password';
GRANT ALL PRIVILEGES ON DATABASE cas TO cas;
\q
```

### Step 2: Build CAS WAR

```bash
# Build the project
./mvnw clean package

# Copy WAR file
cp cas-server/target/cas.war /opt/cas/
```

### Step 3: Configure Tomcat

```bash
# Install Tomcat
wget https://dlcdn.apache.org/tomcat/tomcat-10/v10.1.17/bin/apache-tomcat-10.1.17.tar.gz
tar -xzf apache-tomcat-10.1.17.tar.gz
mv apache-tomcat-10.1.17 /opt/tomcat

# Deploy CAS
cp /opt/cas/cas.war /opt/tomcat/webapps/

# Configure Tomcat
echo 'JAVA_OPTS="-Xms2g -Xmx4g -Dcas.config.dir=/etc/cas/config"' > /opt/tomcat/bin/setenv.sh
chmod +x /opt/tomcat/bin/setenv.sh
```

### Step 4: Configure CAS

```bash
# Create configuration directory
mkdir -p /etc/cas/config

# Copy configuration files
cp cas-server/src/main/resources/* /etc/cas/config/
```

### Step 5: Start Services

```bash
# Start PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Start Redis
sudo systemctl start redis
sudo systemctl enable redis

# Start Tomcat
/opt/tomcat/bin/startup.sh

# Configure Nginx reverse proxy
sudo nano /etc/nginx/sites-available/cas
```

Nginx configuration:
```nginx
server {
    listen 443 ssl http2;
    server_name sso.company.sa;

    ssl_certificate /etc/ssl/certs/server.crt;
    ssl_certificate_key /etc/ssl/private/server.key;

    location / {
        proxy_pass https://localhost:8443;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Post-Installation

### 1. Verify Installation

```bash
# Check service health
curl -k https://localhost:8443/cas/actuator/health

# Test login page
curl -I https://localhost:8443/cas/login
```

### 2. Access Admin Dashboard

1. Navigate to https://your-server/admin
2. Login with default admin credentials:
   - Username: casadmin
   - Password: (check logs for initial password)

### 3. Change Default Passwords

```bash
# Change admin password via CLI
docker-compose exec cas bash -c "java -jar cas.war --reset-admin-password"

# Or through the admin dashboard
```

### 4. Configure First Service

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "https://your-first-app.company.sa/*",
  "name": "First Application",
  "id": 1001,
  "evaluationOrder": 10
}
```

### 5. Enable Monitoring

```bash
# Prometheus metrics available at:
https://your-server:8443/cas/actuator/prometheus

# Configure your monitoring system to scrape metrics
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Failed

```bash
# Check PostgreSQL status
docker-compose logs postgres

# Test connection
docker-compose exec postgres psql -U cas -d cas -c "SELECT 1"
```

#### 2. LDAP Connection Issues

```bash
# Test LDAP connectivity
docker-compose exec cas ldapsearch -x -H ldap://ldap:389 -D "cn=admin,dc=example,dc=com" -w password -b "dc=example,dc=com"
```

#### 3. SSL Certificate Problems

```bash
# Verify certificate
openssl x509 -in ./ssl/server.crt -text -noout

# Check certificate chain
openssl verify -CAfile ./ssl/ca-bundle.crt ./ssl/server.crt
```

#### 4. Memory Issues

```bash
# Increase Java heap size
echo 'JAVA_OPTS="-Xms4g -Xmx8g"' >> .env
docker-compose up -d cas
```

### Log Locations

- CAS Application: `/var/log/cas/cas.log`
- Audit Logs: `/var/log/cas/cas_audit.log`
- PostgreSQL: `/var/log/postgresql/`
- Nginx: `/var/log/nginx/`

### Getting Help

If you encounter issues:

1. Check the logs: `docker-compose logs -f`
2. Consult the documentation: https://docs.securecas.com
3. Contact support: support@securecas.com

---

© 2024 SecureCAS Commercial Edition. All rights reserved.