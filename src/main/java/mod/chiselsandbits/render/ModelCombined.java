package mod.chiselsandbits.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mod.chiselsandbits.client.model.baked.BaseBakedBlockModel;
import mod.chiselsandbits.core.ClientSide;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelCombined extends BaseBakedBlockModel
{

    private static final Random COMBINED_RANDOM_MODEL = new Random();

	BakedModel[] merged;

	List<BakedQuad>[] face;
	List<BakedQuad>   generic;

	boolean isSideLit;

	@SuppressWarnings( "unchecked" )
	public ModelCombined(
			final BakedModel... args )
	{
		face = new ArrayList[Direction.values().length];

		generic = new ArrayList<>();
		for ( final Direction f : Direction.values() )
		{
			face[f.ordinal()] = new ArrayList<>();
		}

		merged = args;

		for ( final BakedModel m : merged )
		{
			generic.addAll( m.getQuads( null, null, COMBINED_RANDOM_MODEL, EmptyModelData.INSTANCE ) );
			for ( final Direction f : Direction.values() )
			{
				face[f.ordinal()].addAll( m.getQuads( null, f, COMBINED_RANDOM_MODEL, EmptyModelData.INSTANCE ) );
			}
		}

		isSideLit = Arrays.stream(args).anyMatch(BakedModel::usesBlockLight);
	}

	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		for ( final BakedModel a : merged )
		{
			return a.getParticleIcon();
		}

		return ClientSide.instance.getMissingIcon();
	}

    @NotNull
    @Override
    public List<BakedQuad> getQuads(
      @Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand, @NotNull final IModelData extraData)
    {
        if ( side != null )
        {
            return face[side.ordinal()];
        }

        return generic;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final Random rand)
    {
        if ( side != null )
        {
            return face[side.ordinal()];
        }

        return generic;
    }

    @Override
    public boolean usesBlockLight()
    {
        return isSideLit;
    }
}
