FROM adoptopenjdk/openjdk8-openj9:ubi

WORKDIR /app

COPY ./target/*.jar /app/est-api-autoec.jar

CMD java -Duser.timezone=America/Sao_Paulo -jar /app/est-api-autoec.jar