# Etapa 1: compila el proyecto con Maven (sin tests, la base de datos no está disponible en el build)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

# Etapa 2: imagen liviana solo con el JRE y el jar generado
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
