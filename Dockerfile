FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM build AS test
# Keeps Maven + full source so `mvn test` (and Testcontainers, from stage 2
# onward) can run inside this stage.

FROM tomcat:10-jdk21 AS server
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /app/target/hr-system.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
