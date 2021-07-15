FROM gradle:7.1.1-jdk11 AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle build --debug

FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim

COPY build/libs/*-all.jar batch-logger.jar

EXPOSE 8080

CMD java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar batch-logger.jar
