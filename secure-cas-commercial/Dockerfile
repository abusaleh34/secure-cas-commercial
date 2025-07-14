# For production build, ensure you have already built the JAR using:
# mvn -Pprod clean package -DskipTests
# This Dockerfile expects the JAR to be in target/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

# Create necessary directories
RUN mkdir -p /opt/secure-cas/config

# Copy pre-built JAR
COPY target/app.jar ./app.jar

# Copy configuration templates
COPY config/ ./config/

# Set environment variables
ENV JAVA_OPTS="-Xms1g -Xmx2g"

# Create non-root user
RUN groupadd -g 1001 cas || true && \
    useradd -u 1001 -g cas -m cas || true && \
    chown -R cas:cas /app /opt/secure-cas

USER cas

# Expose CAS port
EXPOSE 8842

# Entry point
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar --spring.config.location=/opt/secure-cas/config/EIKAuth.config --server.port=8842"]