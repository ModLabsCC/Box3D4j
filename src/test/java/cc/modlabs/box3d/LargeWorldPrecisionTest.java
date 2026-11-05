package cc.modlabs.box3d;

import static cc.modlabs.box3d.global.Box3D.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LargeWorldPrecisionTest {
    @Test
    void preservesSubBlockPositionsAtBothWorldEdges() {
        assertTrue(b3IsDoublePrecision(), "Native library must use double-precision world positions");
        try (b3WorldDef worldDef = b3DefaultWorldDef();
             b3WorldId world = b3CreateWorld(worldDef);
             b3BodyDef bodyDef = b3DefaultBodyDef()) {
            try {
                for (double x : new double[] {30_000_000.125, -30_000_000.125}) {
                    bodyDef.type(b3_dynamicBody);
                    bodyDef._position().x(x).y(10).z(0);
                    try (b3BodyId body = b3CreateBody(world, bodyDef);
                         b3Pos position = b3Body_GetPosition(body)) {
                        assertEquals(x, position.x(), 0.00001);
                        b3DestroyBody(body);
                    }
                }
            } finally {
                b3DestroyWorld(world);
            }
        }
    }
}
