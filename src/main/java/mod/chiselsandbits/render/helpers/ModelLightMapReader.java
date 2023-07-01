package mod.chiselsandbits.render.helpers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.NotNull;

public class ModelLightMapReader implements VertexConsumer {
    public int lv = 0;
    final float maxLightmap = 32.0f / 0xffff;
    private VertexFormat format = DefaultVertexFormat.BLOCK;
    boolean hasLightMap = false;

    public ModelLightMapReader() {
    }

//    public void setVertexFormat(
//            VertexFormat format) {
//        hasLightMap = false;
//
//        int eCount = format.getVertexSize();
//        for (int x = 0; x < eCount; x++) {
//            VertexFormatElement e = format.getElements().get(x);
//            if (e.getUsage() == VertexFormatElement.Usage.UV && e.getIndex() == 1 && e.getType() == VertexFormatElement.Type.SHORT) {
//                hasLightMap = true;
//            }
//        }
//
//        this.format = format;
//    }

    @Override
    public @NotNull VertexConsumer vertex(double pX, double pY, double pZ) {
        return this;
    }

    @Override
    public @NotNull VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
        return this;
    }

    @Override
    public @NotNull VertexConsumer uv(float pU, float pV) {
        return this;
    }

    @Override
    public @NotNull VertexConsumer overlayCoords(int pU, int pV) {
        return this;
    }

    @Override
    public @NotNull VertexConsumer uv2(int pU, int pV) {
        lv = Math.max(pU, lv);
        lv = Math.max(pV, lv);
        return this;
    }

    @Override
    public @NotNull VertexConsumer normal(float pX, float pY, float pZ) {
        return this;
    }

    @Override
    public void endVertex() {

    }

    @Override
    public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {

    }

    @Override
    public void unsetDefaultColor() {

    }
}