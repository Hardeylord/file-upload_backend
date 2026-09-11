#FROM eclipse-temurin:21-jdk-alpine
#WORKDIR /ebut
#
#COPY target/chunks-0.0.1-SNAPSHOT.jar ebut.jar
#
#EXPOSE 8080
#
#CMD ["java", "-jar", "ebut.jar"]

FROM eclipse-temurin:21-jdk-alpine AS dependencies
RUN apk add --no-cache
WORKDIR /dwldDependency
COPY pom.xml /dwldDependency/
RUN mvn dependency:go-offline

FROM dependencies As buildstage
COPY src /dwldDependency/src/
RUN mvn clean package

FROM eclipse-temurin:21-jre-alpine As runtime
WORKDIR /ebut
COPY --from=dependencies /dwldDependency/target/*.jar ebut.jar

CMD ["java", "-jar", "ebut.jar"]