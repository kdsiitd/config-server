FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app

# Copy the entire project
COPY . .

# Build the project
RUN mvn clean package -DskipTests -pl app -am

FROM amazoncorretto:21-alpine

WORKDIR /app
COPY --from=build /app/app/target/*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"] 