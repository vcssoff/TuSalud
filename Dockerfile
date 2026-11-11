# Dockerfile
# ==============================================================================
# Dockerfile Multi-Stage para TuSalud (OpenJDK 21 + Usuario No-Root)
# Compatible con Render, GitHub Container Registry (GHCR) y Kubernetes (Minikube)
# ==============================================================================

# Etapa 1: Compilación y empaquetado con Maven y JDK 21
FROM maven:3.9-eclipse-temurin-26-alpine AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Imagen ligera de ejecución (JRE 21 Alpine) con endurecimiento (Hardening)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalar curl para el HEALTHCHECK nativo
RUN apk add --no-cache curl && \
    addgroup -S tusaludgroup && \
    adduser -S tusaluduser -G tusaludgroup

# Copiar únicamente el Fat JAR compilado desde la etapa builder
COPY --from=builder /app/target/TuSalud-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar
RUN chown -R tusaluduser:tusaludgroup /app

# Ejecutar con usuario sin privilegios de root (Hardening de seguridad)
USER tusaluduser

EXPOSE 8080

# Verificación automática del endpoint /health (Cumple R12)
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
