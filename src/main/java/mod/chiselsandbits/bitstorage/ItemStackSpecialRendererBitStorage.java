package mod.chiselsandbits.bitstorage;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ItemStackSpecialRendererBitStorage extends BlockEntityWithoutLevelRenderer {

    public ItemStackSpecialRendererBitStorage(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(
            final ItemStack stack,
            final ItemTransforms.TransformType p_239207_2_,
            final PoseStack matrixStack,
            final MultiBufferSource buffer,
            final int combinedLight,
            final int combinedOverlay) {

        final BakedModel model = Minecraft.getInstance().getModelManager()
                .getModel(new ModelResourceLocation(ModBlocks.BIT_STORAGE_BLOCK.getId(), "facing=east"));

        Minecraft.getInstance().getBlockRenderer().getModelRenderer()
                .renderModel(matrixStack.last(), buffer.getBuffer(RenderType.translucent()), ModBlocks.BIT_STORAGE_BLOCK.get().defaultBlockState(),
                        model, 1f, 1f, 1f, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

        final BlockEntityBitStorage tileEntity = new BlockEntityBitStorage(BlockPos.ZERO, ModBlocks.BIT_STORAGE_BLOCK.get().defaultBlockState());
        tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                .ifPresent(t -> t.fill(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                        .map(s -> s.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE))
                                .orElse(FluidStack.EMPTY), IFluidHandler.FluidAction.EXECUTE));

        this.blockEntityRenderDispatcher.renderItem(tileEntity, matrixStack, buffer, combinedLight, combinedOverlay);
    }
}
