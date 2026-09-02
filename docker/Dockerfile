# Builder image for Light Squares Attestable Builds (see ../lightsquares.toml).
# Toolchain only: the sources are bind-mounted at /workspace by the platform,
# and this directory (docker/) is the build context, so never COPY sources.

# eclipse-temurin:21-jdk (Ubuntu), pinned by digest
FROM eclipse-temurin:21-jdk@sha256:85f00967bcc624fc19fa9c2cf124ea426a5363898e267141726f31f358c2e14b

ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${PATH}"

# Android command-line tools (sdkmanager), pinned by version and checksum
ARG CMDLINE_TOOLS_VERSION=16111833
ARG CMDLINE_TOOLS_SHA256=0877a1d048fe4a24efe2eff536ca4223f7adeb58648bb81909d33c446918cfa8

RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip \
 && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL -o /tmp/cmdline-tools.zip \
      "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" \
 && echo "${CMDLINE_TOOLS_SHA256}  /tmp/cmdline-tools.zip" | sha256sum -c - \
 && mkdir -p "${ANDROID_HOME}/cmdline-tools" \
 && unzip -q /tmp/cmdline-tools.zip -d "${ANDROID_HOME}/cmdline-tools" \
 && mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest" \
 && rm /tmp/cmdline-tools.zip

# SDK components: compileSdk from gradle/libs.versions.toml, build-tools and
# cmake as required by the AGP version in use. The NDK (2 GB) is installed by
# build.sh instead: a layer that large fails to commit on a medium enclave.
RUN yes | sdkmanager --licenses >/dev/null \
 && sdkmanager --install \
      "platforms;android-37.0" \
      "build-tools;36.0.0" \
      "cmake;3.22.1" \
      "platform-tools"

WORKDIR /workspace
CMD ["./build.sh"]
