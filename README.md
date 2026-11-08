# Box3D4j

Java bindings for [Erin Catto's Box3D](https://github.com/erincatto/box3d).

Box3D4j generates bindings for the latest Box3D `main` branch and exposes the complete public C17 API, including worlds, bodies, shapes, joints, collision queries, events, character movement, recording, and replay.

The Java API stays close to the native Box3D API. Function and type names are kept unchanged where possible.

## Features

- Bindings for the complete public Box3D C API
- Tracks the latest Box3D `main` branch
- Automatic publishing for new upstream commits
- JavaCPP-based JNI bindings
- Bundled native libraries
- Java 8 runtime compatibility
- Compatible with Forge 1.16.5
- Linux x86-64
- Windows x86-64
- macOS x86-64
- macOS ARM64

## API

Functions are available from:

```text
cc.modlabs.box3d.global.Box3D
```

Structs, IDs, enums, and callback types are available directly under:

```text
cc.modlabs.box3d
```

Names intentionally match the native Box3D API.

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

JavaCPP loads the bundled native library automatically when the API is first used.

Published artifacts target Java 8, so they can also be used by older JVM projects such as Forge 1.16.5 mods.

## Performance

Existing getters remain available. For hot loops that synchronize many bodies, the optional
`b3Body_GetTransformsPacked` helper reads a batch in one JNI call without allocating native
result structs. It writes seven doubles per body: `px, py, pz, qx, qy, qz, qw`.
The quaternion uses vector components followed by the scalar component.

```java
import cc.modlabs.box3d.b3BodyId;
import java.nio.DoubleBuffer;
import org.bytedeco.javacpp.DoublePointer;

import static cc.modlabs.box3d.global.Box3D.*;

// Set up once: copy your existing body IDs into a contiguous native array.
try (b3BodyId ids = new b3BodyId(bodyCount);
     DoublePointer storage = new DoublePointer(7L * bodyCount)) {
    for (int i = 0; i < bodyCount; i++) {
        b3BodyId id = bodies[i];
        ids.position(i).index1(id.index1()).world0(id.world0()).generation(id.generation());
    }
    ids.position(0);
    DoubleBuffer transforms = storage.asBuffer();

    // Repeat after each simulation step; reuse ids, storage, and transforms.
    b3Body_GetTransformsPacked(ids, transforms, bodyCount);
    double firstBodyX = transforms.get(0);
    double firstBodyQuaternionW = transforms.get(6);
}
```

Provide `bodyCount` valid IDs and space for at least `7 * bodyCount` doubles from the
current buffer position. Positions are honored without being advanced. This low-level
native helper does not check buffer bounds or ID validity. Refresh the ID array when
bodies are destroyed or replaced, and do not step or mutate the worlds during the read.
The output is a caller-owned snapshot and remains valid after the next step.
Use a direct buffer or `DoublePointer` for this path; the generated `double[]` overload
may copy data. Packing into doubles does not change the native engine's precision mode.

For scenes where relatively few bodies move, the existing `b3World_GetBodyMoveEvents`
helper exposes the native move-event array without allocating a result struct. Its data
is transient; consume it before modifying or stepping the world again.

To read those events without per-event field access through JNI, use
`b3World_CopyBodyMoveEventsPacked`. It copies only the current step's move events into
reusable buffers in one JNI call, without allocating native result structs:

```java
// Allocate once; use LongPointer, DoublePointer, and BytePointer with capacities
// eventCapacity, 7L * eventCapacity, and eventCapacity, respectively.
LongBuffer movedIds = idStorage.asBuffer();
DoubleBuffer movedTransforms = transformStorage.asBuffer();
ByteBuffer fellAsleep = sleepStorage.asBuffer();

int required = b3World_CopyBodyMoveEventsPacked(
        world, movedIds, movedTransforms, fellAsleep, eventCapacity);
// Only Math.min(required, eventCapacity) entries were written.
// If required > eventCapacity, grow the buffers and retry before changing the world.
```

The method returns the **total** event count, even when the output is too small. Passing
capacity zero with null outputs queries the required size without copying. Positive
capacity requires writable buffers with at least `capacity` longs, `7 * capacity` doubles,
and `capacity` bytes remaining. Native code trusts these bounds. Buffer positions are
honored without being advanced. Re-reading does not consume events, and copied data
remains valid after the world changes. Call once after each step; events represent that
step, not all changes since the last read.

Each ID uses Box3D's `b3StoreBodyId` encoding; compute a matching Java key once when
registering a body:

```java
long key = ((long) id.index1() << 32)
        | ((id.world0() & 0xffffL) << 16)
        | (id.generation() & 0xffffL);
```

Transforms use the same seven-double layout as `b3Body_GetTransformsPacked`. Sleep flags
are `0` or `1`; a `1` reports the step where the body went to sleep. Keep that body's
last transform when later steps omit it. The original event API also exposes user data;
the packed helper uses body IDs for application lookup.

Measure `b3World_Step` separately from reading transforms and application work. Batching
reduces binding overhead; it does not accelerate the solver itself or establish the cause
of a performance difference against a Java engine. Native libraries are compiled during
the build, not at application startup.

## Dependency

Artifacts are published to the public [ModLabs repository](https://repo.modlabs.cc/explorer).

No credentials are required.

### Gradle Kotlin DSL

```kotlin
repositories {
    maven("https://repo-api.modlabs.cc/repo/maven/maven-public/")
}

dependencies {
    implementation("cc.modlabs:box3d4j:git-3362633-1.0.6")
}
```

### Gradle Groovy DSL

```groovy
repositories {
    maven { url "https://repo-api.modlabs.cc/repo/maven/maven-public/" }
}

dependencies {
    implementation "cc.modlabs:box3d4j:git-3362633-1.0.6"
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>modlabs-maven-public</id>
        <url>https://repo-api.modlabs.cc/repo/maven/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>cc.modlabs</groupId>
        <artifactId>box3d4j</artifactId>
        <version>git-3362633-1.0.6</version>
    </dependency>
</dependencies>
```

For double-precision world positions, append `-double` to the version. Use only one precision variant on the classpath.

## Versioning

Builds from Box3D `main` use the upstream Box3D commit in the version:

```text
git-<Box3D short commit>-1.0.3
```

For example:

```text
git-3fc20f5-1.0.3
```

CI checks Box3D `main` daily and publishes a new artifact when the upstream commit changes.

Tagged builds use the tag version instead. For example:

```text
v0.1.0-1.0.3
```

publishes:

```text
0.1.0-1.0.3
```

This makes it possible to tell which Box3D revision a Box3D4j artifact was generated from.

## Build

Requirements:

- JDK 17
- CMake 3.22 or newer
- Git
- A C/C++ compiler

Build with:

```shell
./gradlew clean test jar
# For double precision:
./gradlew clean test jar -PdoublePrecision=true
```

The build:

1. Clones the latest Box3D `main`
2. Builds Box3D statically
3. Generates Java and JNI bindings from the public Box3D headers
4. Builds the JNI library
5. Packages the native library into the JAR

A system installation of Box3D is not used.

### Selecting a platform

The native target can be selected with `javacppPlatform`.

Supported values:

```text
linux-x86_64
macosx-arm64
macosx-x86_64
windows-x86_64
```

For example:

```shell
./gradlew jar -PjavacppPlatform=linux-x86_64
```

Native code must be built on the matching operating system.

## Cross-platform bundle

GitHub Actions builds and tests Box3D4j on:

- Linux x86-64
- Windows x86-64
- macOS x86-64
- macOS ARM64

The platform-specific outputs are combined into:

```text
box3d4j-all.jar
```

The combined artifact is published as:

```text
cc.modlabs:box3d4j:<version>
```

It contains:

- Box3D JNI libraries for all supported platforms
- The matching JavaCPP native runtimes
- JavaCPP Java classes
- Generated Box3D4j classes

Consumers only need the published Box3D4j artifact on the classpath.

A JAR built locally contains native binaries for the selected local target. The cross-platform `-all.jar` is assembled by CI because Windows and macOS binaries need to be built on their respective operating systems.

## Upstream Box3D

Box3D4j is generated from the public headers of:

[https://github.com/erincatto/box3d](https://github.com/erincatto/box3d)

The project follows Box3D `main` rather than only tracking tagged releases.

Because Box3D is still evolving, upstream API changes can result in corresponding changes to the Java API.

## License

Box3D4j is licensed under the [MIT License](LICENSE).

The bundled [Box3D](https://github.com/erincatto/box3d) code is also MIT licensed. Generated bindings retain the upstream SPDX notices and license information:

[Box3D license](src/main/resources/META-INF/LICENSE-box3d.txt)

The bundled [JavaCPP](https://github.com/bytedeco/javacpp) runtime is redistributed under the Apache License 2.0:

- [JavaCPP license](src/main/resources/META-INF/LICENSE-javacpp.txt)
- [JavaCPP notice](src/main/resources/META-INF/NOTICE-javacpp.txt)
