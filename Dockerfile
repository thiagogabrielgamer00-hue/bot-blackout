# Estágio 1: Baixa o Maven com Java 11 para compilar o projeto na nuvem
FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /app

# Copia todos os seus códigos originais para dentro do contêiner
COPY . .

# Executa o comando de compilação do Maven forçando a atualização (-U) das dependências clássicas
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
