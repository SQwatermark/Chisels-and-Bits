package mod.chiselsandbits.render.helpers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;

public class ModelUVReader implements VertexConsumer {

    final float minU;
    final float maxUMinusMin;

    final float minV;
    final float maxVMinusMin;

    public final float[] quadUVs = new float[]{0, 0, 0, 1, 1, 0, 1, 1};

    int uCoord, vCoord;

    public ModelUVReader(TextureAtlasSprite texture, int uFaceCoord, int vFaceCoord) {
        minU = texture.getU0();
        maxUMinusMin = texture.getU1() - minU;

        minV = texture.getV0();
        maxVMinusMin = texture.getV1() - minV;

        uCoord = uFaceCoord;
        vCoord = vFaceCoord;
    }

    private final float[] pos = new float[3];
    private final float[] uv = new float[2];
    public int corners;

    @Override
    public @NotNull VertexConsumer vertex(double pX, double pY, double pZ) {
        pos[0] = (float) pX;
        pos[1] = (float) pY;
        pos[2] = (float) pZ;
        return this;
    }

    @Override
    public @NotNull VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
        return this;
    }

    @Override
    public @NotNull VertexConsumer uv(float pU, float pV) {
        uv[0] = pU;
        uv[1] = pV;
        return this;
    }

    @Override
    public @NotNull VertexConsumer overlayCoords(int pU, int pV) {
        return this;
    }

    @Override
    public @NotNull VertexConsumer uv2(int pU, int pV) {
        return this;
    }

    @Override
    public @NotNull VertexConsumer normal(float pX, float pY, float pZ) {
        return this;
    }

    @Override
    public void endVertex() {
        if (ModelUtil.isZero(pos[uCoord]) && ModelUtil.isZero(pos[vCoord])) {
            corners = corners | 0x1;
            quadUVs[0] = (uv[0] - minU) / maxUMinusMin;
            quadUVs[1] = (uv[1] - minV) / maxVMinusMin;
        } else if (ModelUtil.isZero(pos[uCoord]) && ModelUtil.isOne(pos[vCoord])) {
            corners = corners | 0x2;
            quadUVs[4] = (uv[0] - minU) / maxUMinusMin;
            quadUVs[5] = (uv[1] - minV) / maxVMinusMin;
        } else if (ModelUtil.isOne(pos[uCoord]) && ModelUtil.isZero(pos[vCoord])) {
            corners = corners | 0x4;
            quadUVs[2] = (uv[0] - minU) / maxUMinusMin;
            quadUVs[3] = (uv[1] - minV) / maxVMinusMin;
        } else if (ModelUtil.isOne(pos[uCoord]) && ModelUtil.isOne(pos[vCoord])) {
            corners = corners | 0x8;
            quadUVs[6] = (uv[0] - minU) / maxUMinusMin;
            quadUVs[7] = (uv[1] - minV) / maxVMinusMin;
        }
    }

    @Override
    public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {

    }

    @Override
    public void unsetDefaultColor() {

    }
}