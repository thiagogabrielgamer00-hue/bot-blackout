# ==============================
# ETAPA 1 - BUILD
# ==============================

FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests


# ==============================
# ETAPA 2 - EXECUÇÃO
# ==============================

FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /app/target/bot.jar ./bot.jar

EXPOSE 8080

ENV PORT=8080

CMD ["java", "-jar", "bot.jar"]