FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copiar POM padre y shared libraries
COPY pom.xml ./pom.xml
COPY shared-libs shared-libs

# Instalar el POM padre en el repositorio local
RUN mvn clean install -N -DskipTests

# Construir solo las shared libraries
RUN cd shared-libs/database-common && mvn clean install -DskipTests
RUN cd shared-libs/common-dto && mvn clean install -DskipTests
RUN cd shared-libs/security-common && mvn clean install -DskipTests

# Copiar el microservicio específico
COPY ms-precios ./ms-precios
WORKDIR /build/ms-precios
RUN mvn clean package -DskipTests

# Debug: Listar archivos generados
RUN ls -la target/

FROM eclipse-temurin:17-jre-alpine

# Instalar wget para health checks
RUN apk add --no-cache wget

WORKDIR /app
# Copiar el JAR principal (excluyendo los de tests)
COPY --from=build /build/ms-precios/target/*.jar app.jar
EXPOSE 8083
ENV TZ=UTC
ENTRYPOINT ["java", "-jar", "app.jar"]
