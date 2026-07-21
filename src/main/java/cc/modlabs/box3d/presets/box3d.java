package cc.modlabs.box3d.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.Info;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
        target = "cc.modlabs.box3d",
        global = "cc.modlabs.box3d.global.Box3D",
        value = {
            @Platform(define = "NDEBUG", include = {
                    "<box3d/base.h>",
                    "<box3d/math_functions.h>",
                    "<box3d/constants.h>",
                    "<box3d/id.h>",
                    "<box3d/collision.h>",
                    "<box3d/types.h>",
                    "<box3d/box3d.h>",
                    "<box3d4j.h>"
            }, link = "box3d")
        })
public class box3d implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        infoMap.put(new Info("B3_API").cppText("#define B3_API"));
        infoMap.put(new Info("B3_INLINE").cppText("#define B3_INLINE"));
        infoMap.put(new Info("b3InternalAssert").skip());
        infoMap.put(new Info("BOX3D_EXPORT", "B3_BREAKPOINT", "B3_FORCE_INLINE", "B3_ID_INLINE",
                "B3_DEFAULT_CATEGORY_BITS", "B3_DEFAULT_MASK_BITS").skip());
    }
}
