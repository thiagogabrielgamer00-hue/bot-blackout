# Estágio 1: Baixa o Maven com Java 11 para compilar na nuvem
FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /app

# CORREÇÃO DEFINITIVA: Intercepta e redireciona APENAS as chamadas do id "dv8tion" para o Maven Central, sem quebrar os plugins nativos
RUN mkdir -p /root/.m2 && echo '<settings><mirrors><mirror><id>central-mirror</id><mirrorOf>dv8tion</mirrorOf><url>https://apache.org</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# Copia todos os seus códigos originais para dentro do contêiner
COPY . .

# Executa o comando de compilação na nuvem
RUN mvn clean package -DskipTests -U

# Estágio 2: Cria uma imagem leve contendo apenas o Java 11 para rodar o bot
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

# Puxa o arquivo executável criado pelo build para a pasta de execução
COPY --from=build /app/app.jar bot.jar

# Informa ao Render a porta padrão que o seu bot usa
EXPOSE 8080

# Comando definitivo que liga o bot da Blackout Community
CMD ["java", "-jar", "bot.jar"]
