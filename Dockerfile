# usa a imagem base do openjdk 21
FROM openjdk:21-jdk-slim

# Instala pacotes de fontes para o JasperReports
RUN apt-get update && apt-get install -y \
    fontconfig \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# define o argumento para o arquivo jar
ARG JAR_FILE=target/*.jar

# copia o arquivo jar para o container
COPY ${JAR_FILE} app.jar

# define o comando para iniciar a aplicacao
ENTRYPOINT ["java","-jar","/app.jar"]
