# SecureCAS Commercial Edition

Enterprise-grade Single Sign-On (SSO) solution based on Apereo CAS, designed specifically for Saudi Arabian enterprises.

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [System Requirements](#system-requirements)
4. [Quick Start](#quick-start)
5. [Installation Guide](#installation-guide)
6. [Configuration](#configuration)
7. [Security & Compliance](#security--compliance)
8. [Support](#support)

## Overview

SecureCAS Commercial Edition is a comprehensive authentication and Single Sign-On solution that extends the open-source Apereo CAS with enterprise-grade features, enhanced security, and seamless integration capabilities tailored for Saudi Arabian business requirements.

## Features

### Core Features (Open Source)
- **Single Sign-On (SSO)**: Centralized authentication across multiple applications
- **Protocol Support**: CAS, SAML 2.0, OAuth 2.0, OpenID Connect
- **LDAP/Active Directory Integration**: Native support for enterprise directories
- **Service Management**: Dynamic service registration and management

### Commercial Features (Proprietary)
- **Advanced Admin Dashboard**: Real-time analytics and monitoring
- **Multi-Factor Authentication**: SMS, TOTP, and email-based MFA
- **Enterprise Reporting**: Comprehensive audit logs and compliance reports
- **[Just-In-Time Provisioning](JIT-PROVISIONING.md)**: Automatic user account creation with rule-based role assignment
- **High Availability**: Built-in clustering and failover support
- **Oracle EBS Integration**: Native connector for Oracle E-Business Suite
- **NCA Compliance**: Meets Saudi National Cybersecurity Authority requirements

## System Requirements

### Minimum Requirements
- **CPU**: 4 cores
- **RAM**: 8 GB
- **Storage**: 50 GB SSD
- **OS**: Linux (RHEL 8+, Ubuntu 20.04+, CentOS 8+)
- **Docker**: Version 20.10+
- **Docker Compose**: Version 2.0+

### Recommended Requirements
- **CPU**: 8 cores
- **RAM**: 16 GB
- **Storage**: 100 GB SSD
- **OS**: RHEL 8+ or Ubuntu 22.04 LTS

## Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/secure-cas-commercial.git
   cd secure-cas-commercial
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start services**
   ```bash
   docker-compose up -d
   ```

4. **Access CAS**
   - CAS Login: https://localhost:8443/cas
   - Admin Dashboard: https://localhost:8443/admin

## Installation Guide

### Prerequisites

1. **Install Docker and Docker Compose**
   ```bash
   # For Ubuntu/Debian
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   
   # Install Docker Compose
   sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   sudo chmod +x /usr/local/bin/docker-compose
   ```

2. **Configure SSL Certificates**
   ```bash
   # For production, replace with your certificates
   mkdir -p ./docker/nginx/ssl
   openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
     -keyout ./docker/nginx/ssl/server.key \
     -out ./docker/nginx/ssl/server.crt
   ```

### Deployment Steps

1. **Database Configuration**
   Edit the `.env` file:
   ```env
   DB_URL=jdbc:postgresql://postgres:5432/cas
   DB_USER=cas
   DB_PASSWORD=your-secure-password
   ```

2. **LDAP Configuration**
   ```env
   LDAP_URL=ldap://your-ldap-server:389
   LDAP_BASE_DN=dc=yourcompany,dc=com
   LDAP_BIND_DN=cn=admin,dc=yourcompany,dc=com
   LDAP_BIND_PASSWORD=your-ldap-password
   ```

3. **Start the Application**
   ```bash
   docker-compose up -d
   ```

4. **Verify Installation**
   ```bash
   docker-compose ps
   docker-compose logs -f cas
   ```

## Configuration

### Application Properties

The main configuration file is located at `/etc/cas/config/application.yml` inside the container.

Key configuration sections:

#### Authentication
```yaml
cas:
  authn:
    ldap:
      - ldap-url: ${LDAP_URL}
        base-dn: ${LDAP_BASE_DN}
        search-filter: (uid={user})
```

#### Multi-Factor Authentication
```yaml
securecas:
  commercial:
    mfa:
      sms-enabled: true
      twilio-account-sid: ${TWILIO_ACCOUNT_SID}
      twilio-auth-token: ${TWILIO_AUTH_TOKEN}
```

#### Oracle EBS Integration
```yaml
securecas:
  commercial:
    integration:
      oracle-ebs:
        enabled: true
        endpoint: https://your-ebs-server/integration
        username: ${EBS_USERNAME}
        password: ${EBS_PASSWORD}
```

### Service Registration

Services can be registered through:
1. Admin Dashboard UI
2. REST API
3. JSON files in `/etc/cas/services/`

Example service definition:
```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "https://app.example.com/*",
  "name": "Example Application",
  "id": 1000,
  "evaluationOrder": 10,
  "accessStrategy": {
    "@class": "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled": true,
    "ssoEnabled": true
  }
}
```

## Security & Compliance

### NCA Compliance Features

1. **Data Protection**
   - AES-256 encryption for data at rest
   - TLS 1.3 for data in transit
   - Secure key management

2. **Access Control**
   - Role-based access control (RBAC)
   - Principle of least privilege
   - Regular access reviews

3. **Audit & Monitoring**
   - Comprehensive audit logging
   - Real-time security monitoring
   - Incident response procedures

4. **Compliance Reports**
   - Automated compliance reporting
   - Security assessment tools
   - Risk management dashboard

### Security Best Practices

1. **Regular Updates**
   ```bash
   docker-compose pull
   docker-compose up -d
   ```

2. **Backup Procedures**
   ```bash
   # Backup database
   docker exec cas-postgres pg_dump -U cas cas > backup.sql
   
   # Backup configuration
   tar -czf config-backup.tar.gz ./cas-config/
   ```

3. **Monitoring**
   - Use provided Prometheus metrics
   - Configure alerts for security events
   - Regular security audits

## Support

### Commercial Support

Annual subscription includes:
- 24/7 technical support
- Security updates and patches
- Feature updates
- Professional services

### Contact Information

- **Email**: support@securecas.com
- **Phone**: +966-XX-XXXX-XXXX
- **Portal**: https://support.securecas.com

### License

This software is licensed under a commercial license agreement. The core CAS components are licensed under Apache 2.0, while commercial modules are proprietary.

For licensing inquiries, contact: licensing@securecas.com

---

© 2024 SecureCAS Commercial Edition. All rights reserved.