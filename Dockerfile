FROM eclipse-temurin:17

LABEL maintainer="nguyenphucphat111999@gmail.com"

WORKDIR /app

COPY target/novel-aggregator-0.0.1-SNAPSHOT.jar /app/novel-aggregator-docker.jar

ENTRYPOINT ["java", "-jar", "novel-aggregator-docker.jar"]