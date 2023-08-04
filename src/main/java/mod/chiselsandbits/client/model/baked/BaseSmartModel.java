package mod.chiselsandbits.client.model.baked;

import mod.chiselsandbits.render.NullBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BaseSmartModel implements BakedModel {

    private final ItemOverrides overrides;

    private static class OverrideHelper extends ItemOverrides {
        final BaseSmartModel parent;

        public OverrideHelper(final BaseSmartModel p) {
            super();
            parent = p;
        }

        // TODO 这里的seed是什么
        @Nullable
        @Override
        public BakedModel resolve(@NotNull BakedModel pModel, @NotNull ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
            return parent.resolve(pModel, pStack, pLevel, pEntity);
        }
    }

    public BaseSmartModel() {
        overrides = new OverrideHelper(this);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.STONE.defaultBlockState());
    }

    @NotNull
    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        final BakedModel model = handleBlockState(state, rand, data, renderType);
        return model.getQuads(state, side, rand, data, renderType);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    public BakedModel handleBlockState(BlockState state, RandomSource random, ModelData modelData, RenderType renderType) {
        return NullBakedModel.INSTANCE;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return overrides;
    }

    public BakedModel resolve(BakedModel originalModel, ItemStack stack, Level world, LivingEntity entity) {
        return originalModel;
    }

}
