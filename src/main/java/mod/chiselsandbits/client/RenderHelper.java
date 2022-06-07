package mod.chiselsandbits.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import mod.chiselsandbits.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

public class RenderHelper
{

    public static Random RENDER_RANDOM = new Random();

    public static void drawSelectionBoundingBoxIfExists(final PoseStack matrixStack, final AABB bb, final BlockPos blockPos,
                                                        final Player player, final float partialTicks, final boolean NormalBoundingBox) {
        drawSelectionBoundingBoxIfExistsWithColor(matrixStack, bb, blockPos, player, partialTicks, NormalBoundingBox,
                0, 0, 0, 102, 32);
    }

    public static void drawSelectionBoundingBoxIfExistsWithColor(
      final PoseStack matrixStack,
      final AABB bb,
      final BlockPos blockPos,
      final Player player,
      final float partialTicks,
      final boolean NormalBoundingBox,
      final int red,
      final int green,
      final int blue,
      final int alpha,
      final int seeThruAlpha)
    {
        if (bb != null)
        {

            if (!NormalBoundingBox)
            {
                RenderHelper.renderBoundingBox(matrixStack,
                        bb.expandTowards(0.002D, 0.002D, 0.002D)
                                .move(blockPos.getX(), blockPos.getY(), blockPos.getZ()), red, green, blue, alpha);
            }

            RenderHelper.renderBoundingBox(matrixStack,
                    bb.expandTowards(0.002D, 0.002D, 0.002D)
                            .move(blockPos.getX(), blockPos.getY(), blockPos.getZ()), red, green, blue, seeThruAlpha);

        }
    }

    public static void drawLineWithColor(
      final PoseStack matrixStack,
      final Vec3 a,
      final Vec3 b,
      final BlockPos blockPos,
      final Player player,
      final float partialTicks,
      final boolean NormalBoundingBox,
      final int red,
      final int green,
      final int blue,
      final int alpha,
      final int seeThruAlpha) {
        if (a != null && b != null)
        {

            final Vec3 a2 = a.add(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            final Vec3 b2 = b.add(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if (!NormalBoundingBox)
            {
                RenderHelper.renderLine(matrixStack, a2, b2, red, green, blue, alpha);
            }

            RenderHelper.renderLine(matrixStack, a2, b2, red, green, blue, seeThruAlpha);

        }
    }

    public static void renderQuads(
      final PoseStack matrixStack,
      final int alpha,
      final BufferBuilder renderer,
      final List<BakedQuad> quads,
      final Level worldObj,
      final BlockPos blockPos,
      int combinedLightIn,
      int combinedOverlayIn)
    {
        int i = 0;
        for (final int j = quads.size(); i < j; ++i)
        {
            final BakedQuad bakedquad = quads.get(i);
            final int color = bakedquad.getTintIndex() == -1 ? alpha | 0xffffff : getTint(alpha, bakedquad.getTintIndex(), worldObj, blockPos);

            float cb = color & 0xFF;
            float cg = (color >>> 8) & 0xFF;
            float cr = (color >>> 16) & 0xFF;
            float ca = (color >>> 24) & 0xFF;

            renderer.putBulkData(matrixStack.last(), bakedquad, cb, cg, cr, ca, combinedLightIn, combinedOverlayIn, false);
        }
    }

    // Custom replacement of 1.9.4 -> 1.10's method that changed.
    public static void renderBoundingBox(final PoseStack matrixStack, final AABB boundingBox, final int red, final int green, final int blue, final int alpha) {
        LevelRenderer.renderLineBox(matrixStack, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES), boundingBox, (float) red / 255, (float) green / 255, (float) blue / 255, (float) alpha / 255);
    }

    public static void renderLine(
      final PoseStack matrixStack,
      final Vec3 a,
      final Vec3 b,
      final int red,
      final int green,
      final int blue,
      final int alpha)
    {
        final VertexConsumer vertexConsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
        Matrix4f matrix4f = matrixStack.last().pose();
        Matrix3f matrix3f = matrixStack.last().normal();
        float dx = (float) (a.x-b.x);
        float dy = (float) (a.y-b.y);
        float dz = (float) (a.z-b.z);
        vertexConsumer.vertex(matrix4f, (float) a.x, (float) a.y, (float) a.z).color(red, green, blue, alpha).normal(matrix3f, dx, dy, dz).endVertex();
        vertexConsumer.vertex(matrix4f, (float) b.x, (float) b.y, (float) b.z).color(red, green, blue, alpha).normal(matrix3f, dx, dy, dz).endVertex();
    }

    public static int getTint(
      final int alpha,
      final int tintIndex,
      final Level worldObj,
      final BlockPos blockPos)
    {
        return alpha | Minecraft.getInstance().getBlockColors().getColor(ModBlocks.getChiseledDefaultState(), worldObj, blockPos, tintIndex);
    }

    public static void renderModel(
      final PoseStack matrixStack,
      final BakedModel model,
      final Level worldObj,
      final BlockPos blockPos,
      final int alpha,
      final int combinedLightmap,
      final int combinedOverlay)
    {
        RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        for (final Direction enumfacing : Direction.values())
        {
            renderQuads(matrixStack, alpha, buffer, model.getQuads(null, enumfacing, RENDER_RANDOM), worldObj, blockPos, combinedLightmap, combinedOverlay);
        }

        renderQuads(matrixStack, alpha, buffer, model.getQuads(null, null, RENDER_RANDOM), worldObj, blockPos, combinedLightmap, combinedOverlay);
        tessellator.end();
    }

    public static void renderGhostModel(
      final PoseStack matrixStack,
      final BakedModel baked,
      final Level worldObj,
      final BlockPos blockPos,
      final boolean isUnplaceable,
      final int combinedLightmap,
      final int combinedOverlay)
    {
        final int alpha = isUnplaceable ? 0x22000000 : 0xaa000000;
        Minecraft.getInstance().getTextureManager().bindForSetup(InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.colorMask(false, false, false, false);

        RenderHelper.renderModel(matrixStack, baked, worldObj, blockPos, alpha, combinedLightmap, combinedOverlay);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderHelper.renderModel(matrixStack, baked, worldObj, blockPos, alpha, combinedLightmap, combinedOverlay);

        RenderSystem.disableBlend();
    }
}
