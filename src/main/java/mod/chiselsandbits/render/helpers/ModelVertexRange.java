package mod.chiselsandbits.render.helpers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public class ModelVertexRange implements VertexConsumer {
    private float minSumX = 1;
    private float minSumY = 1;
    private float minSumZ = 1;
    private float maxSumX = 0;
    private float maxSumY = 0;
    private float maxSumZ = 0;
    int vertCount = 0;

    public float getLargestRange() {
        if (vertCount == 0) {
            return 0;
        }

        final float x = maxSumX - minSumX;
        final float y = maxSumY - minSumY;
        final float z = maxSumZ - minSumZ;
        return Math.max(x, Math.max(y, z));
    }

    @Override
    public @NotNull VertexConsumer vertex(double pX, double pY, double pZ) {
        if (vertCount == 0) {
            minSumX = (float) pX;
            minSumY = (float) pY;
            minSumZ = (float) pZ;
            maxSumX = (float) pX;
            maxSumY = (float) pY;
            maxSumZ = (float) pZ;
        } else {
            minSumX = Math.min((float) pX, minSumX);
            minSumY = Math.min((float) pY, minSumY);
            minSumZ = Math.min((float) pZ, minSumZ);
            maxSumX = Math.max((float) pX, maxSumX);
            maxSumY = Math.max((float) pY, maxSumY);
            maxSumZ = Math.max((float) pZ, maxSumZ);
        }

        ++vertCount;
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