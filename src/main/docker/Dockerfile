FROM ghcr.io/graalvm/graalvm-community:21.0.2-ol9-20240116@sha256:6e46c711c90bdbc24e23ccdb6e3fba837d660e68dd4ffeb87abbd3d08e115653 AS builder

WORKDIR /code
COPY . .

RUN chmod +x ./gradlew && ./gradlew :nativeCompile

FROM ubuntu:noble-20241011@sha256:99c35190e22d294cdace2783ac55effc69d32896daaa265f0bbedbcde4fbe3e5 AS cve
COPY --from=builder /code/build/libs/rtp-activator-*.jar .

FROM ubuntu:noble-20241011@sha256:99c35190e22d294cdace2783ac55effc69d32896daaa265f0bbedbcde4fbe3e5 AS runtime

WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --from=builder --chown=1001:root --chmod=0755 /code/build/native/nativeCompile/rtp-activator /work/application

RUN useradd --uid 10000 runner
USER 10000

EXPOSE 8080

ENTRYPOINT ["./application"]
