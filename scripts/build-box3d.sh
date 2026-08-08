#!/usr/bin/env bash
set -euo pipefail

PREFIX="build/${BOX3D_TARGET_PLATFORM}"
SOURCE="${PREFIX}/box3d-git"
BOX3D_COMMIT="${BOX3D_COMMIT:-main}"

mkdir -p "${PREFIX}"
if [[ ! -d "${SOURCE}" ]]; then
    git init --quiet "${SOURCE}"
    git -C "${SOURCE}" remote add origin https://github.com/erincatto/box3d.git
    git -C "${SOURCE}" fetch --quiet --depth 1 origin "${BOX3D_COMMIT}"
    git -C "${SOURCE}" checkout --quiet --detach FETCH_HEAD
fi

case "${BOX3D_TARGET_PLATFORM}" in
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
        echo "Unsupported platform: ${BOX3D_TARGET_PLATFORM}" >&2
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

cp src/main/c/box3d4j.h "${PREFIX}/include/box3d4j.h"
