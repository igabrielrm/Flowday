# ============================================
# DOCKERFILE - Event Organizer UCE (Render / Docker)
# ============================================

# --- Etapa 1: compilar la SPA web ---
# bookworm-slim evita fallos conocidos de npm ci en Node 22/Alpine
# y es compatible con dependencias nativas (p. ej. sharp vía Capacitor).
FROM node:22-bookworm-slim AS frontend-build
WORKDIR /app/frontend

COPY frontend/package.json frontend/package-lock.json ./
# --ignore-scripts: el build web no necesita postinstall nativos (sharp/Capacitor assets).
RUN npm ci --no-audit --no-fund --ignore-scripts

COPY frontend ./
RUN npm run build:web

# --- Etapa 2: compilar Spring Boot con la SPA actualizada ---
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
COPY --from=frontend-build /app/src/main/resources/static/app ./src/main/resources/static/app
RUN mvn clean package -DskipTests -B

# --- Etapa 3: ejecutar ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/servidorproyecto-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
