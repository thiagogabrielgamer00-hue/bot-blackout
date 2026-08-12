# Usa uma imagem oficial estável do Maven com Java 17 para compilar
FROM maven:3.8.8-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Usa uma imagem oficial estável e leve do Java 17 para rodar o bot
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/JujutsuBot-1.0.jar app.jar

# Abre a porta exigida pelo plano gratuito do Render
EXPOSE 8080

# Inicia o bot de Jujutsu
CMD ["java", "-jar", "app.jar"]
