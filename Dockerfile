# ============================================
# DOCKERFILE - Event Organizer UCE
# ============================================

# Usar imagen base de Java 21
FROM openjdk:21-jdk-slim

# Información del mantenedor
LABEL maintainer="Event Organizer UCE Team"
LABEL version="1.0.0"
LABEL description="Asistente de gestión académica para la UCE"

# Crear directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el JAR generado desde la máquina local al contenedor
COPY target/servidorproyecto-0.0.1-SNAPSHOT.jar app.jar

# Copiar archivo de propiedades de producción
COPY src/main/resources/application-prod.properties application-prod.properties

# Exponer el puerto donde corre la aplicación
EXPOSE 8080

# Variables de entorno para la base de datos
ENV DB_URL=jdbc:postgresql://postgres:5432/eventorganizer_uce
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=labcom,2015
ENV SPRING_PROFILES_ACTIVE=prod

# Comando de inicio de la aplicación
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]