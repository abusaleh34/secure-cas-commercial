#!/bin/bash
set -e

# Function to generate random keys if not provided
generate_key() {
    openssl rand -hex 32
}

# Generate webflow keys if not provided
if [ -z "$CAS_WEBFLOW_SIGNING_KEY" ]; then
    export CAS_WEBFLOW_SIGNING_KEY=$(generate_key)
    echo "Generated CAS_WEBFLOW_SIGNING_KEY"
fi

if [ -z "$CAS_WEBFLOW_ENCRYPTION_KEY" ]; then
    export CAS_WEBFLOW_ENCRYPTION_KEY=$(generate_key)
    echo "Generated CAS_WEBFLOW_ENCRYPTION_KEY"
fi

# Generate self-signed certificate if not exists
if [ ! -f "/etc/cas/keystore.jks" ]; then
    echo "Generating self-signed certificate..."
    keytool -genkeypair -alias cas \
        -keyalg RSA -keysize 2048 \
        -validity 365 \
        -keystore /etc/cas/keystore.jks \
        -storepass ${SSL_KEYSTORE_PASSWORD:-changeit} \
        -keypass ${SSL_KEY_PASSWORD:-changeit} \
        -dname "CN=${CAS_SERVER_HOST:-localhost}, OU=CAS, O=SecureCAS, L=City, S=State, C=SA"
fi

# Wait for dependencies
echo "Waiting for dependencies..."
while ! nc -z postgres 5432; do
    echo "Waiting for PostgreSQL..."
    sleep 2
done

while ! nc -z redis 6379; do
    echo "Waiting for Redis..."
    sleep 2
done

echo "Dependencies are ready!"

# Run CAS
case "$1" in
    run)
        echo "Starting CAS server..."
        exec java $JAVA_OPTS \
            -Dcas.config.dir=$CAS_CONFIG_DIR \
            -Dlogging.config=$LOGGING_CONFIG \
            -jar /cas-overlay/cas.war
        ;;
    shell)
        exec /bin/bash
        ;;
    *)
        exec "$@"
        ;;
esac