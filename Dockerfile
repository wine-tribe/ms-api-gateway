FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8091
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]