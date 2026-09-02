#!/bin/sh
# Build script for Light Squares Attestable Builds (see lightsquares.toml).
# Runs inside the docker/Dockerfile container with cwd = /workspace.
set -eu

# NDK pinned in the version catalog; not baked into the image (see docker/Dockerfile)
ndk_version=$(sed -n 's/^ndk = "\(.*\)"$/\1/p' gradle/libs.versions.toml)
sdkmanager --install "ndk;${ndk_version}"

./gradlew --no-daemon --console=plain assembleRelease

# The files listed under [build].artifacts in lightsquares.toml
ls -l lib/build/outputs/aar/argon2kt-release.aar \
      app/build/outputs/apk/release/app-release.apk
