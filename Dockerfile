FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -U

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar bot.jar
EXPOSE 8080
CMD ["java", "-jar", "bot.jar"]
