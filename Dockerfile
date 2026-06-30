FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre

RUN apt-get update && \
    apt-get install -y wget && \
    rm -rf /var/lib/apt/lists/*

RUN addgroup --system saldium && \
    adduser --system --ingroup saldium saldium

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

USER saldium

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]