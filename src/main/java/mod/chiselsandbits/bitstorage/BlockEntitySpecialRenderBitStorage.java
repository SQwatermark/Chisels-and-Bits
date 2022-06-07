package mod.chiselsandbits.bitstorage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.chiseledblock.ChiselRenderType;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBakedModel;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import mod.chiselsandbits.utils.FluidCuboidHelper;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public class BlockEntitySpecialRenderBitStorage implements BlockEntityRenderer<BlockEntityBitStorage>, BlockEntityRendererProvider<BlockEntityBitStorage>
{

    private static final SimpleMaxSizedCache<CacheKey, VoxelBlob> STORAGE_CONTENTS_BLOB_CACHE = new SimpleMaxSizedCache<>(ChiselsAndBits.getConfig().getClient().bitStorageContentCacheSize.get());

    @Override
    public BlockEntityRenderer<BlockEntityBitStorage> create(Context p_173571_) {
        return this;
    }

    public BlockEntitySpecialRenderBitStorage()
    {
        super();
    }

    @Override
    public void render(
      final BlockEntityBitStorage te,
      final float partialTicks,
      final PoseStack matrixStackIn,
      final MultiBufferSource buffer,
      final int combinedLightIn,
      final int combinedOverlayIn)
    {
        if (te.getMyFluid() != null) {
            final FluidStack fluidStack = te.getBitsAsFluidStack();
            if (fluidStack != null)
            {
                RenderType.chunkBufferLayers().forEach(renderType -> {
                    if (!ItemBlockRenderTypes.canRenderInLayer(fluidStack.getFluid().defaultFluidState(), renderType))
                        return;

                    if (renderType == RenderType.translucent() && Minecraft.useShaderTransparency())
                        renderType = Sheets.translucentCullBlockSheet();

                    final VertexConsumer builder = buffer.getBuffer(renderType);

                    final float fullness = (float) fluidStack.getAmount() / (float) BlockEntityBitStorage.MAX_CONTENTS;

                    FluidCuboidHelper.renderScaledFluidCuboid(
                      fluidStack,
                      matrixStackIn,
                      builder,
                      combinedLightIn,
                      combinedOverlayIn,
                      1, 1, 1,
                      15, 15 * fullness, 15
                    );
                });
            }

            return;
        }

        final int bits = te.getBits();
        final BlockState state = te.getMyFluid() == null ? te.getState() : te.getMyFluid().defaultFluidState().createLegacyBlock();
        if (bits <= 0 || state == null)
            return;

        VoxelBlob innerModelBlob = STORAGE_CONTENTS_BLOB_CACHE.get(new CacheKey(ModUtil.getStateId(state), bits));
        if (innerModelBlob == null) {
            innerModelBlob = new VoxelBlob();
            innerModelBlob.fillAmountFromBottom(ModUtil.getStateId(state), bits);
            STORAGE_CONTENTS_BLOB_CACHE.put(new CacheKey(ModUtil.getStateId(state), bits), innerModelBlob);
        }

        matrixStackIn.pushPose();
        matrixStackIn.translate(2/16f, 2/16f, 2/16f);
        matrixStackIn.scale(12/16f, 12/16f, 12/16f);
        final VoxelBlob finalInnerModelBlob = innerModelBlob;
        RenderType.chunkBufferLayers().forEach(renderType -> {
            final ChiseledBlockBakedModel innerModel = ChiseledBlockSmartModel.getCachedModel(
              ModUtil.getStateId(state),
              finalInnerModelBlob,
              ChiselRenderType.fromLayer(renderType, te.getMyFluid() != null),
              DefaultVertexFormat.BLOCK,
              Objects.requireNonNull(te.getLevel()).getRandom()
            );

            if (!innerModel.isEmpty())
            {
                final float r = te.getMyFluid() == null ? 1f : ((te.getMyFluid().getAttributes().getColor() >> 16) & 0xff) / 255F;
                final float g = te.getMyFluid() == null ? 1f : ((te.getMyFluid().getAttributes().getColor() >> 8) & 0xff) / 255f;
                final float b = te.getMyFluid() == null ? 1f : ((te.getMyFluid().getAttributes().getColor()) & 0xff) / 255f;

                Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStackIn.last(), buffer.getBuffer(renderType), state, innerModel, r, g, b, combinedLightIn, combinedOverlayIn,
                  EmptyModelData.INSTANCE);
            }
        });
        matrixStackIn.popPose();
    }

    private static final class CacheKey {
        private final int blockStateId;
        private final int bitCount;

        private CacheKey(final int blockStateId, final int bitCount) {
            this.blockStateId = blockStateId;
            this.bitCount = bitCount;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof CacheKey))
            {
                return false;
            }
            final CacheKey cacheKey = (CacheKey) o;
            return blockStateId == cacheKey.blockStateId &&
                     bitCount == cacheKey.bitCount;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(blockStateId, bitCount);
        }
    }
}