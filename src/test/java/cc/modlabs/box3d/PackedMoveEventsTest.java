package cc.modlabs.box3d;

import static cc.modlabs.box3d.global.Box3D.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.LongBuffer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.LongPointer;
import org.bytedeco.javacpp.Pointer;
import org.junit.jupiter.api.Test;

class PackedMoveEventsTest {
    @Test
    void copiesOnlyMovedBodiesWithIdsTransformsAndSleepFlags() {
        try (b3WorldDef worldDef = b3DefaultWorldDef();
             b3BodyDef bodyDef = b3DefaultBodyDef();
             LongPointer idStorage = new LongPointer(4);
             DoublePointer transformStorage = new DoublePointer(16);
             BytePointer sleepStorage = new BytePointer(4)) {
            b3WorldId world = b3CreateWorld(worldDef);
            try {
                bodyDef.type(b3_dynamicBody).gravityScale(0);
                bodyDef._position().x(1.25).y(-2.5).z(3.75);
                try (b3BodyId first = b3CreateBody(world, bodyDef)) {
                    bodyDef._position().x(-4).y(5).z(-6);
                    bodyDef.rotation().v().z(1);
                    bodyDef.rotation().s(0);
                    try (b3BodyId second = b3CreateBody(world, bodyDef)) {
                        bodyDef.isAwake(false);
                        try (b3BodyId ignored = b3CreateBody(world, bodyDef)) {
                            b3World_Step(world, 1.0f / 60.0f, 4);
                            LongBuffer ids = idStorage.asBuffer();
                            DoubleBuffer transforms = transformStorage.asBuffer();
                            ByteBuffer sleeping = sleepStorage.asBuffer();
                            ids.put(0, 123).put(3, 456).position(1);
                            transforms.put(0, 123).put(15, 456).position(1);
                            sleeping.put(0, (byte) 12).put(3, (byte) 34).position(1);
                            assertEquals(2, b3World_CopyBodyMoveEventsPacked(world, (LongBuffer) null, null, null, 0));
                            assertEquals(2, b3World_CopyBodyMoveEventsPacked(world, ids, transforms, sleeping, 2));
                            long firstKey = key(first);
                            long secondKey = key(second);
                            assertNotEquals(firstKey, secondKey);
                            assertTrue((ids.get(1) == firstKey && ids.get(2) == secondKey)
                                    || (ids.get(1) == secondKey && ids.get(2) == firstKey));
                            for (int i = 0; i < 2; i++) {
                                double[] expected = ids.get(i + 1) == firstKey
                                        ? new double[]{1.25, -2.5, 3.75, 0, 0, 0, 1}
                                        : new double[]{-4, 5, -6, 0, 0, 1, 0};
                                for (int j = 0; j < 7; j++) {
                                    assertEquals(expected[j], transforms.get(1 + 7 * i + j), 1e-6);
                                }
                                assertEquals(0, sleeping.get(i + 1));
                            }
                            assertEquals(1, ids.position());
                            assertEquals(1, transforms.position());
                            assertEquals(1, sleeping.position());
                            assertEquals(123, ids.get(0));
                            assertEquals(456, ids.get(3));
                            assertEquals(123, transforms.get(0));
                            assertEquals(456, transforms.get(15));
                            assertEquals(12, sleeping.get(0));
                            assertEquals(34, sleeping.get(3));

                            // Capacity limits writes, while the return value reports the full size.
                            ids.put(2, 999);
                            transforms.put(8, 999);
                            sleeping.put(2, (byte) 99);
                            assertEquals(2, b3World_CopyBodyMoveEventsPacked(world, ids, transforms, sleeping, 1));
                            assertEquals(999, ids.get(2));
                            assertEquals(999, transforms.get(8));
                            assertEquals(99, sleeping.get(2));
                            long before = Pointer.totalBytes();
                            for (int i = 0; i < 300; i++) {
                                assertEquals(2, b3World_CopyBodyMoveEventsPacked(world, ids, transforms, sleeping, 2));
                                assertEquals(before, Pointer.totalBytes());
                            }
                            boolean sawSleepTransition = false;
                            for (int i = 0; i < 120; i++) {
                                b3World_Step(world, 1.0f / 60.0f, 4);
                                int count = b3World_CopyBodyMoveEventsPacked(world, ids, transforms, sleeping, 2);
                                for (int j = 0; j < count; j++) {
                                    sawSleepTransition |= sleeping.get(1 + j) == 1;
                                }
                            }
                            assertTrue(sawSleepTransition);
                            assertEquals(0, b3World_CopyBodyMoveEventsPacked(world, (LongBuffer) null, null, null, 0));
                        }
                    }
                }
            } finally {
                b3DestroyWorld(world);
                world.close();
            }
        }
    }

    @Test
    void emptyWorldDoesNotWriteOutputs() {
        try (b3WorldDef definition = b3DefaultWorldDef()) {
            b3WorldId world = b3CreateWorld(definition);
            try {
                b3World_Step(world, 1.0f / 60.0f, 4);
                assertEquals(0, b3World_CopyBodyMoveEventsPacked(world, (LongBuffer) null, null, null, 0));
            } finally {
                b3DestroyWorld(world);
                world.close();
            }
        }
    }

    private static long key(b3BodyId id) {
        return ((long) id.index1() << 32) | ((id.world0() & 0xffffL) << 16)
                | (id.generation() & 0xffffL);
    }
}
