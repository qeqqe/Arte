FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

RUN apk add --no-cache protobuf protobuf-dev bash

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B || true

COPY src ./src

RUN chmod +x mvnw && \
    (while true; do find target/protoc-plugins -type f 2>/dev/null | xargs chmod +x 2>/dev/null; sleep 0.1; done) & \
    BG=$! && \
    ./mvnw clean package -DskipTests -B && \
    kill $BG 2>/dev/null || true

RUN ls -la /app/target/*.jar && \
    test -f /app/target/*.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8082 50051
ENTRYPOINT ["java", "-jar", "app.jar"]
