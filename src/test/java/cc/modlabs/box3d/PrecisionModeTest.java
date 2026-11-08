package cc.modlabs.box3d;

import static cc.modlabs.box3d.global.Box3D.b3IsDoublePrecision;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PrecisionModeTest {
    @Test
    void nativePrecisionMatchesRequestedVariant() {
        assertEquals(Boolean.getBoolean("box3d.doublePrecision"), b3IsDoublePrecision());
    }
}
