# Just-In-Time (JIT) User Provisioning Guide

## Overview

SecureCAS Commercial Edition includes a powerful Just-In-Time (JIT) user provisioning feature that automatically creates and manages user accounts upon successful authentication through external identity providers such as LDAP, Active Directory, or OIDC.

## Key Features

- **Automatic User Creation**: Users are automatically provisioned upon first login
- **Attribute Synchronization**: User attributes are synchronized from external sources
- **Rule-Based Role Assignment**: Automatically assign roles and groups based on configurable rules
- **Audit Trail**: Comprehensive logging of all provisioning events
- **RESTful API**: Manage provisioning rules and users through REST endpoints
- **Dashboard Integration**: Monitor and manage provisioned users through the admin dashboard

## Configuration

### Enable JIT Provisioning

Add the following configuration to your `application.yml`:

```yaml
securecas:
  commercial:
    jit:
      enabled: true
      auto-deactivate-enabled: true
      inactive-days-threshold: 90
      sync-attributes-on-login: true
      default-roles:
        - ROLE_USER
      attribute-mappings:
        username: uid
        email: mail
        first-name: givenName
        last-name: sn
        display-name: displayName
        phone-number: telephoneNumber
        department: department
        employee-id: employeeNumber
        groups: memberOf
```

### Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `enabled` | Enable/disable JIT provisioning | `true` |
| `auto-deactivate-enabled` | Automatically deactivate inactive users | `false` |
| `inactive-days-threshold` | Days of inactivity before deactivation | `90` |
| `sync-attributes-on-login` | Update user attributes on each login | `true` |
| `default-roles` | Default roles for new users | `[ROLE_USER]` |
| `attribute-mappings` | Map external attributes to internal fields | See example |

## Provisioning Rules

### Rule Types

1. **ALWAYS**: Rule always applies
2. **ATTRIBUTE_EQUALS**: Check if attribute equals specific value
3. **ATTRIBUTE_CONTAINS**: Check if attribute contains substring
4. **ATTRIBUTE_MATCHES**: Match attribute against regex pattern
5. **ATTRIBUTE_EXISTS**: Check if attribute exists
6. **MEMBEROF_GROUP**: Check group membership
7. **EMAIL_DOMAIN**: Check email domain

### Creating Rules via API

```bash
curl -X POST https://your-cas-server/api/v1/provisioning/rules \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "IT Department Rule",
    "description": "Assign IT roles to IT department members",
    "enabled": true,
    "order": 1,
    "sourceType": "LDAP",
    "conditionType": "ATTRIBUTE_EQUALS",
    "conditionAttribute": "department",
    "conditionValue": "IT",
    "assignedRoles": ["ROLE_USER", "ROLE_IT"],
    "assignedGroups": ["IT_STAFF"]
  }'
```

### Example Rules

#### 1. Email Domain Rule
```json
{
  "name": "Company Email Rule",
  "sourceType": "OIDC",
  "conditionType": "EMAIL_DOMAIN",
  "conditionValue": "company.com",
  "assignedRoles": ["ROLE_EMPLOYEE"],
  "assignedGroups": ["INTERNAL_USERS"]
}
```

#### 2. Active Directory Group Rule
```json
{
  "name": "Admin Group Rule",
  "sourceType": "ACTIVE_DIRECTORY",
  "conditionType": "MEMBEROF_GROUP",
  "conditionValue": "CN=Admins,OU=Groups,DC=company,DC=com",
  "assignedRoles": ["ROLE_ADMIN"],
  "assignedGroups": ["ADMINISTRATORS"]
}
```

#### 3. Department-Based Rule
```json
{
  "name": "Finance Department Rule",
  "sourceType": "LDAP",
  "conditionType": "ATTRIBUTE_EQUALS",
  "conditionAttribute": "department",
  "conditionValue": "Finance",
  "assignedRoles": ["ROLE_FINANCE"],
  "assignedGroups": ["FINANCE_USERS"]
}
```

## API Endpoints

### User Management

#### Get Provisioned Users
```
GET /api/v1/provisioning/users?source=LDAP&active=true&page=0&size=20
```

#### Get User Details
```
GET /api/v1/provisioning/users/{username}
```

#### Deactivate User
```
POST /api/v1/provisioning/users/{username}/deactivate
```

#### Reactivate User
```
POST /api/v1/provisioning/users/{username}/activate
```

### Rule Management

#### List Provisioning Rules
```
GET /api/v1/provisioning/rules?source=LDAP&enabled=true
```

#### Create Rule
```
POST /api/v1/provisioning/rules
```

#### Update Rule
```
PUT /api/v1/provisioning/rules/{id}
```

#### Delete Rule
```
DELETE /api/v1/provisioning/rules/{id}
```

#### Enable/Disable Rule
```
POST /api/v1/provisioning/rules/{id}/enable
POST /api/v1/provisioning/rules/{id}/disable
```

### Statistics

#### Get Provisioning Statistics
```
GET /api/v1/provisioning/stats?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
```

## Database Schema

### Provisioned Users Table
```sql
CREATE TABLE cas_provisioned_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    external_id VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(255),
    phone_number VARCHAR(50),
    department VARCHAR(100),
    employee_id VARCHAR(50),
    provision_source VARCHAR(50) NOT NULL,
    provision_timestamp TIMESTAMP NOT NULL,
    last_login_timestamp TIMESTAMP,
    last_updated_timestamp TIMESTAMP,
    active BOOLEAN DEFAULT true,
    auto_provisioned BOOLEAN DEFAULT true
);
```

### User Roles Table
```sql
CREATE TABLE cas_user_roles (
    user_id BIGINT REFERENCES cas_provisioned_users(id),
    role VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, role)
);
```

### Provisioning Rules Table
```sql
CREATE TABLE cas_provisioning_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN DEFAULT true,
    rule_order INTEGER DEFAULT 0,
    source_type VARCHAR(50) NOT NULL,
    condition_type VARCHAR(50) NOT NULL,
    condition_attribute VARCHAR(100),
    condition_operator VARCHAR(20),
    condition_value VARCHAR(255)
);
```

## Monitoring and Troubleshooting

### Viewing Provisioning Logs

Provisioning events are logged at the INFO level:

```bash
grep "JIT provisioning" /var/log/cas/cas.log
```

### Common Issues

#### 1. User Not Being Provisioned

Check:
- JIT provisioning is enabled
- User successfully authenticated
- No errors in logs
- Database connectivity

#### 2. Wrong Roles Assigned

Check:
- Rule conditions are correct
- Rule order (rules are evaluated in order)
- Attribute names match external source

#### 3. Attributes Not Syncing

Check:
- Attribute mappings configuration
- External source returns expected attributes
- `sync-attributes-on-login` is enabled

### Performance Considerations

1. **Database Indexes**: Ensure proper indexes on frequently queried columns
2. **Rule Optimization**: Order rules by frequency of matches
3. **Caching**: User data is cached to reduce database queries
4. **Batch Operations**: Use bulk APIs for mass updates

## Security Considerations

1. **Access Control**: Provisioning APIs require admin role
2. **Audit Trail**: All provisioning events are logged
3. **Data Validation**: Input validation prevents injection attacks
4. **Encryption**: Sensitive attributes are encrypted at rest

## Integration Examples

### LDAP Integration
```yaml
cas:
  authn:
    ldap:
      - ldap-url: ldap://ldap.company.com:389
        base-dn: dc=company,dc=com
        principal-attribute-list:
          - uid
          - mail
          - givenName
          - sn
          - department
          - memberOf
```

### OIDC Integration
```yaml
cas:
  authn:
    oidc:
      claims: sub,email,name,given_name,family_name,department
```

## Best Practices

1. **Start Simple**: Begin with basic rules and add complexity as needed
2. **Test Rules**: Use the test endpoint before applying to production
3. **Monitor Usage**: Regularly review provisioned users and access patterns
4. **Clean Up**: Enable auto-deactivation for inactive users
5. **Document Rules**: Maintain documentation of provisioning logic

---

For additional support, contact support@securecas.com