FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM amazoncorretto:22-alpine
RUN apk add --no-cache ffmpeg

EXPOSE 8080:8080
VOLUME /app/images

ENV TZ=Europe/Berlin

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/ktor-docker-sample.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","ktor-docker-sample.jar"]