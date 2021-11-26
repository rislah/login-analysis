FROM gradle:7-jdk17 AS build

COPY . /app
WORKDIR /app
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:17-jdk

ENV JMX_PORT=8080

WORKDIR /app

COPY --from=build "/app/build/libs/*-all.jar" ./logindetection.jar
COPY --from=build "/app/files/jmx_prometheus_javaagent.jar" ./jmx.jar
COPY --from=build "/app/files/jmx_prometheus_exporter.yml" ./exporter.yml

EXPOSE ${JMX_PORT}

ENTRYPOINT java -javaagent:./jmx.jar=${JMX_PORT:-8080}:exporter.yml -jar logindetection.jar