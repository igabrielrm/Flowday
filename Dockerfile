# ============================================
# DOCKERFILE - Flowday (Render / Docker)
# ============================================
# La SPA se construye en CI/local (`frontend` → `src/main/resources/static/app`)
# y se empaqueta aquí con Maven. Evita fallos de npm ci en el builder de Render.

# --- Etapa 1: compilar Spring Boot ---
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Etapa 2: ejecutar ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/flowday-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
