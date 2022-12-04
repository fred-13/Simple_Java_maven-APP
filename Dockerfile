#
# Build stage
#
FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /usr/src/app
COPY . ./
RUN mvn clean package

#
# Package stage
#
FROM openjdk:11-jre-slim
WORKDIR /usr/src/app
COPY --from=build /usr/src/app/target/my-app-v0.0.1.jar ./app.jar
CMD ["java","-jar", "./app.jar"]
