# Stage 1: Build the application
FROM gradle:8.5-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Build the server fat JAR
RUN ./gradlew :server:buildFatJar --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-jammy
EXPOSE 8080
RUN mkdir /app

# Copy the generated JAR from the server module's build directory
COPY --from=build /home/gradle/src/server/build/libs/*-all.jar /app/server.jar

ENTRYPOINT ["java", "-jar", "/app/server.jar"]
