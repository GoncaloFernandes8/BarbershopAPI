# ===== Build =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace/app
COPY . .
RUN mvn -q -DskipTests package

# ===== Run =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
