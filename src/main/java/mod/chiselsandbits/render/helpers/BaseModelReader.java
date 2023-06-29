package mod.chiselsandbits.render.helpers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class BaseModelReader {

    public VertexFormat getVertexFormat() {
        return DefaultVertexFormat.BLOCK;
    }

    public abstract void misc(VertexFormatElement element, int... rawData);

}
