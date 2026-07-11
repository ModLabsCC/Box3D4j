# Box3D4j

Generated Java bindings for [Erin Catto's Box3D](https://github.com/erincatto/box3d), pinned to
the initial `v0.1.0` release. The binding covers the complete public C17 API: worlds, bodies,
shapes, joints, collision queries, events, character movement, recording, and replay.

## API

Functions live in `cc.modlabs.box3d.global.Box3D`; structs and callback types live directly in
`cc.modlabs.box3d`. Names intentionally match the native API:

```java
import cc.modlabs.box3d.b3WorldDef;
import cc.modlabs.box3d.b3WorldId;

import static cc.modlabs.box3d.global.Box3D.*;

b3WorldDef definition = b3DefaultWorldDef();
b3WorldId world = b3CreateWorld(definition);
try {
    b3World_Step(world, 1.0f / 60.0f, 4);
} finally {
    b3DestroyWorld(world);
}
```

JavaCPP loads the bundled native library automatically when the API class is first used.

## Build

Requirements are JDK 17, CMake 3.22+, a C/C++ compiler, `curl`, and `tar`.

```shell
./gradlew clean test jar
```

The build downloads the pinned Box3D source archive, verifies its SHA-256 checksum, builds it
statically, generates Java and JNI bindings from all public headers, and embeds the JNI library
in the JAR. No system Box3D installation is used.

Set `-PjavacppPlatform=linux-x86_64`, `macosx-arm64`, `macosx-x86_64`, or
`windows-x86_64` when selecting a
platform explicitly. Native code must be built on the matching operating system.

## Cross-platform bundle

The GitHub Actions workflow builds and tests on Linux x86-64, macOS Apple Silicon, macOS Intel,
and Windows x86-64, then merges the results into `box3d4j-all.jar` and publishes it as
`cc.modlabs:box3d4j:<version>`. That single artifact carries all four Box3D JNI libraries, the
corresponding JavaCPP native runtimes, and the JavaCPP Java classes. It is the only JAR consumers
need on the classpath.

A JAR built locally contains the native binaries for the local target. The canonical `-all.jar`
is assembled by CI because macOS and Windows binaries must be compiled on their respective
operating systems.

Box3D is MIT licensed. The generated binding retains the upstream SPDX notices in its generated
source and binary metadata.
