FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache netcat-openbsd \
    && addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh && chown app:app /docker-entrypoint.sh app.jar
USER app
ENV SPRING_PROFILES_ACTIVE=postgres \
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/meridiana \
    SPRING_DATASOURCE_USERNAME=meridiana \
    SPRING_DATASOURCE_PASSWORD=meridiana \
    POSTGRES_HOST=postgres
EXPOSE 8080
ENTRYPOINT ["/docker-entrypoint.sh"]
