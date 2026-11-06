FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system rag && useradd --system --gid rag rag
COPY --from=build /app/target/suntinus-rag-lab-*.jar app.jar
USER rag
EXPOSE 8110
ENTRYPOINT ["java", "-jar", "app.jar"]
