FROM gradle:8.14-jdk17-alpine AS build

WORKDIR /app

COPY build.gradle .
COPY settings.gradle .

RUN gradle dependencies --no-daemon || true

COPY src src

ARG SKIP_TESTS=false
RUN if [ "$SKIP_TESTS" = "true" ]; then \
      gradle bootJar -x test --no-daemon; \
    else \
      gradle bootJar --no-daemon; \
    fi

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -g 1000 app && adduser -u 1000 -G app -D app

COPY --from=build /app/build/libs/*.jar app.jar
RUN chown app:app app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
