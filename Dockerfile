# ==============================
# ETAPA 1 - BUILD
# ==============================

FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests -B


# ==============================
# ETAPA 2 - EXECUÇÃO
# ==============================

FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /app/target/bot.jar ./bot.jar

EXPOSE 8080

ENV PORT=8080

CMD ["java", "-jar", "bot.jar"]