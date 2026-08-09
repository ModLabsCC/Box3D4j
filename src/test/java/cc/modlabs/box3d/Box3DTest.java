package cc.modlabs.box3d;

import static cc.modlabs.box3d.global.Box3D.b3CreateWorld;
import static cc.modlabs.box3d.global.Box3D.b3CreateBody;
import static cc.modlabs.box3d.global.Box3D.b3DefaultBodyDef;
import static cc.modlabs.box3d.global.Box3D.b3DefaultWorldDef;
import static cc.modlabs.box3d.global.Box3D.b3DestroyWorld;
import static cc.modlabs.box3d.global.Box3D.b3_dynamicBody;
import static cc.modlabs.box3d.global.Box3D.b3World_GetBodyMoveEvents;
import static cc.modlabs.box3d.global.Box3D.b3World_IsValid;
import static cc.modlabs.box3d.global.Box3D.b3World_Step;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Arrays;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;
import org.junit.jupiter.api.Test;

class Box3DTest {
    @Test
    void exposesBox3DVersion() throws Exception {
        Class<?> api = Class.forName("cc.modlabs.box3d.global.Box3D");
        Object version = api.getMethod("b3GetVersion").invoke(null);

        assertEquals(0, version.getClass().getMethod("major").invoke(version));
        assertEquals(1, version.getClass().getMethod("minor").invoke(version));
        assertEquals(0, version.getClass().getMethod("revision").invoke(version));
    }

    @Test
    void doesNotExposeInternalAssertionHandler() throws Exception {
        Class<?> api = Class.forName("cc.modlabs.box3d.global.Box3D");

        assertTrue(Arrays.stream(api.getDeclaredMethods())
                .noneMatch(method -> method.getName().equals("b3InternalAssert")));
    }

    @Test
    void doesNotExposeUndefinedUpstreamFunction() throws Exception {
        Class<?> api = Class.forName("cc.modlabs.box3d.global.Box3D");

        assertTrue(Arrays.stream(api.getDeclaredMethods())
                .noneMatch(method -> method.getName().equals("b3World_DumpShapeBounds")));
    }

    @Test
    void exposesAllocationFreeBodyMoveEvents() throws Exception {
        Class<?> api = Class.forName("cc.modlabs.box3d.global.Box3D");

        assertNotNull(api.getMethod("b3World_GetBodyMoveEvents", b3WorldId.class, IntPointer.class));

        b3WorldId world = b3CreateWorld(b3DefaultWorldDef());
        try (IntPointer moveCount = new IntPointer(1)) {
            b3BodyDef bodyDefinition = b3DefaultBodyDef();
            bodyDefinition.type(b3_dynamicBody);
            b3CreateBody(world, bodyDefinition);
            b3World_Step(world, 1.0f / 60.0f, 4);

            long before = Pointer.totalBytes();
            long maximum = before;
            long address = 0;
            for (int i = 0; i < 300; i++) {
                address = b3World_GetBodyMoveEvents(world, moveCount);
                maximum = Math.max(maximum, Pointer.totalBytes());
            }

            assertTrue(moveCount.get(0) > 0);
            assertTrue(address != 0);
            assertEquals(0, maximum - before);
        } finally {
            b3DestroyWorld(world);
        }
    }

    @Test
    void producesJava8Bytecode() throws Exception {
        try (InputStream resource = getClass().getClassLoader()
                .getResourceAsStream("cc/modlabs/box3d/global/Box3D.class")) {
            assertNotNull(resource);
            DataInputStream classFile = new DataInputStream(resource);
            assertEquals(0xCAFEBABE, classFile.readInt());
            classFile.readUnsignedShort();
            assertEquals(52, classFile.readUnsignedShort());
        }
    }

    @Test
    void createsAndStepsAWorld() {
        b3WorldDef definition = b3DefaultWorldDef();
        b3WorldId world = b3CreateWorld(definition);
        try {
            assertTrue(b3World_IsValid(world));
            b3World_Step(world, 1.0f / 60.0f, 4);
        } finally {
            b3DestroyWorld(world);
        }
    }
}
