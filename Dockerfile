# Estágio 1: Baixa o Maven com Java 11 para compilar na nuvem
FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /app

# Cria uma configuração de espelhamento para redirecionar o site quebrado para um mirror ativo
RUN mkdir -p /root/.m2 && echo '<settings><mirrors><mirror><id>central-mirror</id><mirrorOf>dv8tion</mirrorOf><url>https://apache.org</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# Copia todos os seus códigos originais para dentro do contêiner
COPY . .

# Executa o comando de compilação usando a rota de espelhamento limpa
RUN mvn clean package -DskipTests -U

# Estágio 2: Cria uma imagem leve contendo apenas o Java 11 para rodar o bot
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

# Puxa o arquivo compilado criado pelo pom.xml (app.jar) para o ambiente de execução
COPY --from=build /app/app.jar bot.jar

# Informa ao Render a porta padrão que o bot usa
EXPOSE 8080

# Comando definitivo que liga o bot da Blackout Community
CMD ["java", "-jar", "bot.jar"]
