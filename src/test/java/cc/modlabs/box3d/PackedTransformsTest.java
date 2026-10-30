package cc.modlabs.box3d;

import static cc.modlabs.box3d.global.Box3D.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.DoubleBuffer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.Pointer;
import org.junit.jupiter.api.Test;

class PackedTransformsTest {
    @Test
    void readsBodiesInOrderIntoAReusablePositionedBuffer() {
        try (b3WorldDef worldDef = b3DefaultWorldDef();
             b3BodyDef bodyDef = b3DefaultBodyDef();
             b3BodyId ids = new b3BodyId(2);
             DoublePointer storage = new DoublePointer(16)) {
            b3WorldId world = b3CreateWorld(worldDef);
            try {
                bodyDef._position().x(1.25).y(-2.5).z(3.75);
                try (b3BodyId first = b3CreateBody(world, bodyDef)) {
                    ids.position(1).index1(first.index1()).world0(first.world0()).generation(first.generation());
                }
                bodyDef._position().x(-4).y(5).z(-6);
                bodyDef.rotation().v().z(1);
                bodyDef.rotation().s(0);
                try (b3BodyId second = b3CreateBody(world, bodyDef)) {
                    ids.position(0).index1(second.index1()).world0(second.world0()).generation(second.generation());
                }
                DoubleBuffer output = storage.asBuffer();
                output.put(0, 123).put(15, 456);
                output.position(1);
                b3Body_GetTransformsPacked(ids, output, 2);
                assertEquals(1, output.position());
                assertEquals(123, output.get(0));
                assertEquals(456, output.get(15));
                double[] expected = {-4, 5, -6, 0, 0, 1, 0, 1.25, -2.5, 3.75, 0, 0, 0, 1};
                for (int i = 0; i < expected.length; i++) {
                    assertEquals(expected[i], output.get(i + 1), 1e-6, "component " + i);
                }

                // Reading a subset must honor the native ID array position as well.
                b3Body_GetTransformsPacked(ids.position(1), output, 1);
                assertEquals(1.25, output.get(1));
                assertEquals(-2.5, output.get(2));
                assertEquals(3.75, output.get(3));
                assertEquals(1, output.get(7));

                long before = Pointer.totalBytes();
                for (int i = 0; i < 300; i++) {
                    b3Body_GetTransformsPacked(ids, output, 1);
                    assertEquals(before, Pointer.totalBytes());
                }
            } finally {
                b3DestroyWorld(world);
                world.close();
            }
        }
    }

    @Test
    void emptyBatchDoesNotTouchPointers() {
        b3Body_GetTransformsPacked(null, (DoubleBuffer) null, 0);
    }
}
