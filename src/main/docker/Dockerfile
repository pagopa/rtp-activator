FROM ghcr.io/graalvm/graalvm-community:17.0.7-ol9 AS builder
WORKDIR /code
COPY . .

RUN chmod +x ./gradlew && ./gradlew bootJar

FROM ubuntu:noble-20241118.1@sha256:80dd3c3b9c6cecb9f1667e9290b3bc61b78c2678c02cbdae5f0fea92cc6734ab AS cve
COPY --from=builder /code/build/libs/rtp-activator-*.jar .

FROM openjdk:17-jre-slim AS runtime

WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --from=builder --chown=1001:root /code/build/libs/rtp-activator-*.jar /work/application.jar

RUN useradd --uid 10000 runner
USER 10000

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "./application.jar"]