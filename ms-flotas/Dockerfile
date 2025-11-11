FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copiar POM padre
COPY pom.xml ./pom.xml

# Copiar shared libraries
COPY shared-libs ./shared-libs

# Copiar POMs de todos los módulos (sin código fuente)
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY ms-solicitudes/pom.xml ./ms-solicitudes/pom.xml
COPY ms-flotas/pom.xml ./ms-flotas/pom.xml
COPY ms-rutas/pom.xml ./ms-rutas/pom.xml
COPY ms-precios/pom.xml ./ms-precios/pom.xml
COPY ms-localizaciones/pom.xml ./ms-localizaciones/pom.xml
COPY tests/integration/pom.xml ./tests/integration/pom.xml

# Instalar shared libraries en el repositorio local de Maven
RUN mvn install -pl shared-libs/database-common,shared-libs/common-dto,shared-libs/security-common -am -DskipTests

# Copiar código fuente del microservicio
COPY ms-flotas/src ./ms-flotas/src

# Construir el microservicio
WORKDIR /build/ms-flotas
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/ms-flotas/target/*.jar app.jar
EXPOSE 8081
ENV TZ=UTC
ENTRYPOINT ["java", "-jar", "app.jar"]

