FROM eclipse-temurin:21-jdk-alpine
WORKDIR /ebut

COPY target/chunks-0.0.1-SNAPSHOT.jar ebut.jar

EXPOSE 8080

CMD ["java", "-jar", "ebut.jar"]