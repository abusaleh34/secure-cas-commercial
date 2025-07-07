# SecureCAS Commercial Edition

[![License](https://img.shields.io/badge/License-Commercial-red.svg)](LICENSE.md)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](docker/Dockerfile)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net/)
[![CAS](https://img.shields.io/badge/CAS-7.0.0-green.svg)](https://apereo.github.io/cas/)

Enterprise-grade Single Sign-On (SSO) solution built on Apereo CAS, enhanced with commercial features for Saudi Arabian enterprises.

## 🚀 Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd secure-cas-commercial

# Configure environment
cp .env.example .env
# Edit .env with your settings

# Start with Docker Compose
docker-compose up -d

# Access CAS at https://localhost:8443/cas
# Admin Dashboard at https://localhost:8443/admin
```

## 📋 Features

### Core Features (Open Source)
- ✅ Single Sign-On (SSO)
- ✅ Multi-protocol support (CAS, SAML 2.0, OAuth 2.0, OIDC)
- ✅ LDAP/Active Directory integration
- ✅ Service management

### Commercial Features (Proprietary)
- 🎯 Advanced admin dashboard with analytics
- 🔐 Multi-Factor Authentication (SMS/OTP/Email)
- 📊 Enterprise reporting and audit logs
- 🚀 Just-In-Time (JIT) user provisioning
- 🔄 High Availability clustering
- 🔌 Oracle EBS integration
- 🛡️ NCA compliance features

## 🏗️ Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│  Web Browser    │────▶│  Nginx Proxy    │────▶│   CAS Server    │
│                 │     │  (SSL/Load Bal) │     │  (Spring Boot)  │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
                                                          │
                              ┌───────────────────────────┴───────────────────────────┐
                              │                                                       │
                     ┌────────▼────────┐     ┌─────────────────┐     ┌──────────────▼─────┐
                     │                 │     │                 │     │                    │
                     │   PostgreSQL    │     │     Redis       │     │    LDAP/AD        │
                     │   (Database)    │     │  (Sessions)     │     │  (Directory)      │
                     │                 │     │                 │     │                    │
                     └─────────────────┘     └─────────────────┘     └────────────────────┘
```

## 📦 Project Structure

```
secure-cas-commercial/
├── cas-server/                 # CAS WAR overlay
│   ├── src/
│   └── pom.xml
├── cas-commercial-modules/     # Proprietary modules
│   ├── src/
│   │   ├── main/java/
│   │   │   ├── dashboard/     # Admin dashboard
│   │   │   ├── mfa/          # Multi-factor auth
│   │   │   ├── reporting/    # Analytics & reports
│   │   │   └── integration/  # Oracle EBS
│   │   └── test/
│   └── pom.xml
├── docker/                    # Docker configuration
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── nginx/
├── docs/                      # Documentation
├── scripts/                   # Utility scripts
└── tests/                     # Integration tests
```

## 🔧 Configuration

### Environment Variables

Key environment variables (see `.env.example` for full list):

| Variable | Description | Default |
|----------|-------------|---------|
| `CAS_SERVER_HOST` | Hostname for CAS server | `localhost` |
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://postgres:5432/cas` |
| `LDAP_URL` | LDAP server URL | `ldap://ldap:389` |
| `REDIS_HOST` | Redis server host | `redis` |

### Service Registration

Register applications via:
1. Admin Dashboard UI
2. REST API
3. JSON files in `/etc/cas/services/`

## 🚀 Deployment

### Docker Deployment (Recommended)

```bash
# Production deployment
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Scale for high availability
docker-compose up -d --scale cas=3
```

### Manual Deployment

See [Installation Guide](docs/INSTALLATION.md) for detailed instructions.

## 🔒 Security

- TLS 1.3 encryption
- OWASP security headers
- Rate limiting
- Session management
- Audit logging
- NCA compliance

## 📊 Monitoring

- Health endpoint: `/cas/actuator/health`
- Metrics endpoint: `/cas/actuator/prometheus`
- Admin dashboard: `/admin`

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run with test profile
docker-compose --profile test up
```

## 📚 Documentation

- [Installation Guide](docs/INSTALLATION.md)
- [Configuration Guide](docs/CONFIGURATION.md)
- [API Documentation](docs/API.md)
- [Security Guide](docs/SECURITY.md)

## 🤝 Support

### Commercial Support
- 📧 Email: support@securecas.com
- 📞 Phone: +966-XX-XXXX-XXXX
- 🌐 Portal: https://support.securecas.com

### License
This is commercial software. See [LICENSE.md](LICENSE.md) for details.

---

© 2024 SecureCAS Corporation. All rights reserved.