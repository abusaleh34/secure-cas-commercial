FROM eclipse-temurin:17-jdk-alpine AS builder

# Install build dependencies
RUN apk add --no-cache maven git

# Set working directory
WORKDIR /build

# Copy project files
COPY . .

# Build the project
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install required packages
RUN apk add --no-cache \
    bash \
    curl \
    openssl \
    ca-certificates \
    && rm -rf /var/cache/apk/*

# Create cas user
RUN addgroup -g 1000 cas && \
    adduser -D -u 1000 -G cas cas

# Create directories
RUN mkdir -p /cas-overlay \
    /etc/cas/config \
    /etc/cas/services \
    /etc/cas/saml \
    /var/log/cas \
    /var/cas/reports \
    && chown -R cas:cas /cas-overlay /etc/cas /var/log/cas /var/cas

# Copy built artifacts
COPY --from=builder /build/cas-server/target/cas.war /cas-overlay/
COPY --from=builder /build/docker/entrypoint.sh /cas-overlay/
COPY --from=builder /build/docker/config/* /etc/cas/config/

# Make entrypoint executable
RUN chmod +x /cas-overlay/entrypoint.sh

# Switch to cas user
USER cas

# Expose ports
EXPOSE 8080 8443

# Set environment variables
ENV JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC" \
    CAS_CONFIG_DIR="/etc/cas/config" \
    LOGGING_CONFIG="file:/etc/cas/config/log4j2.xml"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f https://localhost:8443/cas/actuator/health || exit 1

# Entry point
ENTRYPOINT ["/cas-overlay/entrypoint.sh"]
CMD ["run"]