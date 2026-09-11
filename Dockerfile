# ===== Etapa de build =====
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

# Copia primeiro só o necessário pro cache de dependências
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Copia o código-fonte e builda o jar (sem rodar os testes)
COPY src ./src
RUN ./gradlew clean bootJar --no-daemon -x test

# ===== Etapa final (imagem enxuta, usuário não-root) =====
FROM eclipse-temurin:25-jre
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
