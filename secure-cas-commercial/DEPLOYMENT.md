# Secure CAS Commercial Deployment Guide

## Prerequisites
- Docker installed on the target server
- Access to GitHub Container Registry
- Oracle EBS instance
- Keycloak server with SEDC realm

## Step 1: Push Docker Image to Registry

```bash
# Login to GitHub Container Registry
docker login ghcr.io -u abusaleh34 -p YOUR_GITHUB_PAT

# Push the image
docker push ghcr.io/abusaleh34/secure-cas-commercial:1.0
```

## Step 2: Push Code to GitHub Repository

```bash
# Push to GitHub (authentication required)
git push -u origin main
```

## Step 3: Stop and Backup Legacy Connector

```bash
# Stop existing service
sudo systemctl stop authenion-eik || true

# Backup existing installation
sudo mv /u01/EBSAuth /u01/EBSAuth.bak_$(date +%F)
```

## Step 4: Deploy Secure CAS Container

```bash
# Run the container
docker run -d --name secure-cas \
  -p 8842:8842 \
  -v /opt/secure-cas/config:/app/config \
  --restart=always \
  ghcr.io/abusaleh34/secure-cas-commercial:1.0

# Verify container is running
docker ps | grep secure-cas
```

## Step 5: Update EBS Configuration

1. Login to EBS as SYSADMIN
2. Navigate to System Administrator > Security > SSWA Options
3. Update the following profiles:
   - **Application Authentication Agent**: `https://<cas-host>:8842/cas/ssologin`
   - **Apps Servlet Agent**: `https://<cas-host>:8842/cas/ssologin`

## Step 6: Update Keycloak Configuration

1. Login to Keycloak Admin Console
2. Navigate to SEDC realm
3. Select the EBS client
4. Add redirect URI: `https://<cas-host>:8842/cas/oidc/handler`
5. Update the client secret in `/opt/secure-cas/config/EIKAuth.config`

## Step 7: Run Tests

```bash
# Run Selenium tests
mvn -pl tests selenium:test

# Run API tests
newman run tests/postman/ebs-cas-collection.json \
  -e tests/postman/env.json

# Run load tests
jmeter -n -t tests/jmeter/login-load.jmx -l results.jtl \
  -e -o /app/reports/

# View report
ls -la /app/reports/report.html
```

## Step 8: Verify Deployment

1. Browse to `https://<ebs-host>/OA_HTML/`
2. You should be redirected to Keycloak login
3. After successful authentication, verify you're redirected back to EBS
4. Check for valid ICX cookie in browser developer tools

## Troubleshooting

### Check container logs
```bash
docker logs secure-cas
```

### Check configuration
```bash
docker exec secure-cas cat /app/config/EIKAuth.config
```

### Restart container
```bash
docker restart secure-cas
```

## Rollback Procedure

If issues occur:
```bash
# Stop new container
docker stop secure-cas
docker rm secure-cas

# Restore old installation
sudo mv /u01/EBSAuth.bak_$(date +%F) /u01/EBSAuth

# Restart old service
sudo systemctl start authenion-eik
```