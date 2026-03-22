# ---- Build stage ----
FROM clojure:lein AS builder

WORKDIR /app

# Cache dependencies before copying source
COPY project.clj .
RUN lein deps

# Copy source and build an uberjar
COPY src src
RUN lein uberjar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/uberjar/app.jar app.jar

EXPOSE 8890
EXPOSE 8891

CMD ["java", "-jar", "app.jar"]
