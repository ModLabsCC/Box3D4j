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
if command -v sha256sum >/dev/null 2>&1; then
    echo "${BOX3D_SHA256}  ${ARCHIVE}" | sha256sum -c
else
    echo "${BOX3D_SHA256}  ${ARCHIVE}" | shasum -a 256 -c
fi

if [[ ! -d "${SOURCE}" ]]; then
    tar -xzf "${ARCHIVE}" -C "${PREFIX}"
fi

cmake -S "${SOURCE}" -B "${PREFIX}/cmake" \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_INSTALL_PREFIX="$(pwd)/${PREFIX}" \
    -DBUILD_SHARED_LIBS=OFF \
    -DBOX3D_SAMPLES=OFF \
    -DBOX3D_BENCHMARKS=OFF \
    -DBOX3D_DOCS=OFF \
    -DBOX3D_UNIT_TESTS=OFF \
    -DBOX3D_VALIDATE=OFF
cmake --build "${PREFIX}/cmake" --config Release --parallel
cmake --install "${PREFIX}/cmake" --config Release
