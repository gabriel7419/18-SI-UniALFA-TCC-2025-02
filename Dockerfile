# usa a imagem base do Amazon Corretto 21 - openjdk foi descontinuado
FROM amazoncorretto:21

# Instala pacotes de fontes para o JasperReports
RUN yum install -y fontconfig dejavu-sans-fonts && yum clean all

# define o argumento para o arquivo jar
ARG JAR_FILE=target/*.jar

# copia o arquivo jar para o container
COPY ${JAR_FILE} app.jar

# define o comando para iniciar a aplicacao
ENTRYPOINT ["java","-jar","/app.jar"]
