FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /app

# Bloqueia qualquer chamada para o domínio antigo e joga para o repositório estável central
RUN mkdir -p /root/.m2 && echo '<settings><mirrors><mirror><id>central-mirror</id><mirrorOf>*</mirrorOf><url>https://apache.org</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

COPY . .
RUN mvn clean package -DskipTests -U

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar bot.jar
EXPOSE 8080
CMD ["java", "-jar", "bot.jar"]
