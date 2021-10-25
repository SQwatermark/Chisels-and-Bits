package mod.chiselsandbits.client.chiseling.preview.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.client.chiseling.preview.render.IChiselContextPreviewRenderer;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.render.ModRenderTypes;
import mod.chiselsandbits.voxelshape.VoxelShapeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static mod.chiselsandbits.api.util.StateEntryPredicates.ALL;
import static mod.chiselsandbits.api.util.StateEntryPredicates.NOT_AIR;

public class ConfigurableColoredVoxelShapeChiselContextPreviewRenderer implements IChiselContextPreviewRenderer
{
    static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "default");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void renderExistingContextsBoundingBox(
      final MatrixStack matrixStack, final IChiselingContext currentContextSnapshot)
    {
        if (!currentContextSnapshot.getMutator().isPresent())
            return;

        Vector3d vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double xView = vector3d.x();
        double yView = vector3d.y();
        double zView = vector3d.z();

        final BlockPos inWorldStartPos = new BlockPos(currentContextSnapshot.getMutator().get().getInWorldStartPoint());
        final VoxelShape boundingShape = VoxelShapeManager.getInstance()
          .get(currentContextSnapshot.getMutator().get(),
            areaAccessor -> {
                final Predicate<IStateEntryInfo> contextPredicate = currentContextSnapshot.getStateFilter()
                  .map(factory -> factory.apply(areaAccessor))
                  .orElse(currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING ? NOT_AIR : ALL);

                return new InternalContextFilter(contextPredicate);
            },
            false);

        final List<? extends Float> color = currentContextSnapshot.getModeOfOperandus() == ChiselingOperation.CHISELING ?
                                 Configuration.getInstance().getClient().previewChiselingColor.get() :
                                 Configuration.getInstance().getClient().previewPlacementColor.get();

        RenderSystem.disableDepthTest();
        WorldRenderer.renderShape(
          matrixStack,
          Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ModRenderTypes.MEASUREMENT_LINES.get()),
          boundingShape,
          inWorldStartPos.getX() - xView, inWorldStartPos.getY() - yView, inWorldStartPos.getZ() - zView,
          getColorValue(color, 0, 0f),
          getColorValue(color, 1, 0f),
          getColorValue(color, 2, 0f),
          getColorValue(color, 3, 1f)
        );
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(ModRenderTypes.MEASUREMENT_LINES.get());
        RenderSystem.enableDepthTest();
    }

    private static float getColorValue(final List<? extends Float> values, final int index, final float defaultValue) {
        if (values.size() <= index || index < 0)
            return defaultValue;

        final Float value = values.get(index);
        if (0 <= value && value <= 1f)
            return value;

        return defaultValue;
    }

    private static final class InternalContextFilter implements Predicate<IStateEntryInfo>
    {
        private final Predicate<IStateEntryInfo> placingContextPredicate;

        private InternalContextFilter(final Predicate<IStateEntryInfo> placingContextPredicate) {this.placingContextPredicate = placingContextPredicate;}

        @Override
        public boolean test(final IStateEntryInfo s)
        {
            return (s.getState().isAir() || IEligibilityManager.getInstance().canBeChiseled(s.getState())) && placingContextPredicate.test(s);
        }

        @Override
        public int hashCode()
        {
            return placingContextPredicate != null ? placingContextPredicate.hashCode() : 0;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof InternalContextFilter))
            {
                return false;
            }

            final InternalContextFilter that = (InternalContextFilter) o;

            return Objects.equals(placingContextPredicate, that.placingContextPredicate);
        }
    }
}