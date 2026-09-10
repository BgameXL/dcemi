# Headless client for the DCEMI.
# This image contains no game files. For local iteration the
# host's Gradle cache and the project are bind-mounted at their real paths (seedocker-compose.yml)
# so `./gradlew runClient` reuses everything already on the host.

FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
        xvfb xauth \
        libgl1-mesa-dri libglx-mesa0 libgl1 libglu1-mesa \
        libx11-6 libxext6 libxrender1 libxrandr2 libxcursor1 libxi6 \
        libxxf86vm1 libxtst6 libxkbcommon0 \
        libopenal1 \
        ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN printf 'pcm.!default { type null }\nctl.!default { type null }\n' > /etc/asound.conf

COPY docker/entrypoint.sh /usr/local/bin/dcemi-entrypoint
RUN chmod +x /usr/local/bin/dcemi-entrypoint

# DCEMI command socket
EXPOSE 25599

ENTRYPOINT ["/usr/local/bin/dcemi-entrypoint"]
