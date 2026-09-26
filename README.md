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

## Dependency

Artifacts are published to the public [ModLabs repository](https://repo.modlabs.cc/explorer).

No credentials are required.

### Gradle Kotlin DSL

```kotlin
repositories {
    maven("https://repo-api.modlabs.cc/repo/maven/maven-public/")
}

dependencies {
    implementation("cc.modlabs:box3d4j:git-5643cd8-1.0.2")
}
```

### Gradle Groovy DSL

```groovy
repositories {
    maven { url "https://repo-api.modlabs.cc/repo/maven/maven-public/" }
}

dependencies {
    implementation "cc.modlabs:box3d4j:git-5643cd8-1.0.2"
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
        <version>git-5643cd8-1.0.2</version>
    </dependency>
</dependencies>
```

## Versioning

Builds from Box3D `main` use the upstream Box3D commit in the version:

```text
git-<Box3D short commit>-1.0.2
```

For example:

```text
git-3fc20f5-1.0.2
```

CI checks Box3D `main` daily and publishes a new artifact when the upstream commit changes.

Tagged builds use the tag version instead. For example:

```text
v0.1.0-1.0.2
```

publishes:

```text
0.1.0-1.0.2
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
