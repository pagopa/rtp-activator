FROM ghcr.io/graalvm/graalvm-community:17.0.7-ol9-20230406@sha256:4e3d7c2e7f2e8f1b6c5d4e3f2a1b8c9d7e6f5a4b3c2d1e0f9g8h7i6j5k4l3m2n AS builder
WORKDIR /code
COPY . .

RUN chmod +x ./gradlew && ./gradlew :nativeCompile

FROM ubuntu:noble-20241118.1@sha256:80dd3c3b9c6cecb9f1667e9290b3bc61b78c2678c02cbdae5f0fea92cc6734ab AS cve
COPY --from=builder /code/build/libs/rtp-activator-*.jar .

FROM ubuntu:noble-20241118.1@sha256:80dd3c3b9c6cecb9f1667e9290b3bc61b78c2678c02cbdae5f0fea92cc6734ab AS runtime

WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --from=builder --chown=1001:root --chmod=0755 /code/build/native/nativeCompile/rtp-activator /work/application

RUN useradd --uid 10000 runner
USER 10000

EXPOSE 8080

ENTRYPOINT ["./application"]
