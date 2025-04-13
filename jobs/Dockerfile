FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app

# Copy the entire project
COPY . .

# Build the project
RUN mvn clean package -DskipTests -pl jobs -am

FROM amazoncorretto:21-alpine

WORKDIR /app
COPY --from=build /app/jobs/target/*.jar jobs.jar

ENTRYPOINT ["java", "-jar", "jobs.jar"] 