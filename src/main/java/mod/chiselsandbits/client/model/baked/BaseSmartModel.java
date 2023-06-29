package mod.chiselsandbits.client.model.baked;

import mod.chiselsandbits.core.ClientSide;
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
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public abstract class BaseSmartModel implements BakedModel
{

	private final ItemOverrides overrides;

	private static class OverrideHelper extends ItemOverrides {
		final BaseSmartModel parent;

		public OverrideHelper(final BaseSmartModel p) {
			super();
			parent = p;
		}

        @Nullable
        @Override
        public BakedModel resolve(final BakedModel p_239290_1_, final ItemStack p_239290_2_, @Nullable final ClientLevel p_239290_3_, @Nullable final LivingEntity p_239290_4_, int p_173469_) {
            return parent.resolve( p_239290_1_, p_239290_2_, p_239290_3_, p_239290_4_ );
        }
	}

	public BaseSmartModel()
	{
		overrides = new OverrideHelper(this);
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return false;
	}

	@NotNull
	@Override
	public TextureAtlasSprite getParticleIcon() {
		final TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.STONE.defaultBlockState());

		if (sprite == null) {
			return ClientSide.instance.getMissingIcon();
		}

		return sprite;
	}

	@NotNull
	@Override
	public ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
		return BakedModel.super.getQuads(state, side, rand, data, renderType);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
		final BakedModel model = handleBlockState( state, rand );
		return model.getQuads( state, side, rand );
	}

	public BakedModel handleBlockState(
			final BlockState state,
			final RandomSource rand )
	{
		return NullBakedModel.instance;
	}

	public BakedModel handleBlockState(
	  final BlockState state,
      final RandomSource random,
      final ModelData modelData
    )
    {
        return NullBakedModel.instance;
    }

	@Override
	public ItemOverrides getOverrides()
	{
		return overrides;
	}

	public BakedModel resolve(
			final BakedModel originalModel,
			final ItemStack stack,
			final Level world,
			final LivingEntity entity )
	{
		return originalModel;
	}

}
