#!/usr/bin/env bash
set -euo pipefail

BOX3D_VERSION=0.1.0
BOX3D_SHA256=df232c7618c0d0d3927b798044559ee56eabadeb9d8ff9dc526d4b384d7b415d
PREFIX="build/${PLATFORM}"
ARCHIVE="${PREFIX}/box3d-${BOX3D_VERSION}.tar.gz"
SOURCE="${PREFIX}/box3d-${BOX3D_VERSION}"

mkdir -p "${PREFIX}"
if [[ ! -f "${ARCHIVE}" ]]; then
    curl --fail --location --silent --show-error \
        "https://github.com/erincatto/box3d/archive/refs/tags/v${BOX3D_VERSION}.tar.gz" \
        --output "${ARCHIVE}"
fi
if [[ "${OSTYPE}" == darwin* ]]; then
    echo "${BOX3D_SHA256}  ${ARCHIVE}" | shasum -a 256 -c
else
    echo "${BOX3D_SHA256}  ${ARCHIVE}" | sha256sum -c
fi

if [[ ! -d "${SOURCE}" ]]; then
    tar -xzf "${ARCHIVE}" -C "${PREFIX}"
fi

case "${PLATFORM}" in
    linux-*)
        CONFIGURE_PRESET=linux-release
        BUILD_PRESET=linux-release
        ;;
    macosx-*)
        CONFIGURE_PRESET=macos
        BUILD_PRESET=macos-release
        ;;
    windows-*)
        CONFIGURE_PRESET=windows
        BUILD_PRESET=windows-release
        ;;
    *)
        echo "Unsupported platform: ${PLATFORM}" >&2
        exit 1
        ;;
esac

INSTALL_PREFIX="$(pwd)/${PREFIX}"
(
    cd "${SOURCE}"
    cmake --preset "${CONFIGURE_PRESET}" \
        -DCMAKE_INSTALL_PREFIX="${INSTALL_PREFIX}" \
        -DBUILD_SHARED_LIBS=OFF \
        -DBOX3D_SAMPLES=OFF \
        -DBOX3D_BENCHMARKS=OFF \
        -DBOX3D_DOCS=OFF \
        -DBOX3D_UNIT_TESTS=OFF \
        -DBOX3D_VALIDATE=OFF
    cmake --build --preset "${BUILD_PRESET}" --parallel
    cmake --install build --config Release
)
