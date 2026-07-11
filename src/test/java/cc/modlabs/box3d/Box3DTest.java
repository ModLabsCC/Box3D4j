package cc.modlabs.box3d;

import static cc.modlabs.box3d.global.Box3D.b3CreateWorld;
import static cc.modlabs.box3d.global.Box3D.b3DefaultWorldDef;
import static cc.modlabs.box3d.global.Box3D.b3DestroyWorld;
import static cc.modlabs.box3d.global.Box3D.b3World_IsValid;
import static cc.modlabs.box3d.global.Box3D.b3World_Step;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
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
