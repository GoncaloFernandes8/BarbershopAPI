# ===== Build =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace/app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package -T 1C

# ===== Run =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar
EXPOSE 8000
ENV PORT=8000
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
