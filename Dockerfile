# -------- build stage --------
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

# кеш зависимостей
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle* settings.gradle* gradle.properties* /app/
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

# сборка
COPY . .
RUN ./gradlew --no-daemon clean bootJar -x test

# -------- run stage --------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Важно: у Spring Boot jar обычно лежит в build/libs/
COPY --from=build /app/build/libs/*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]