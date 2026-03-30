FROM eclipse-temurin:25.0.2_10-jdk-ubi10-minimal
LABEL authors="daicamangdeplao"

# Set a working directory in the container
WORKDIR /app

# Copy the build artifact (JAR file) into the container
COPY build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Execute the command that runs the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
