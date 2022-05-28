package mod.chiselsandbits.client.model.baked;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrides;

public abstract class BaseBakedBlockModel extends BaseBakedPerspectiveModel implements BakedModel
{

	@Override
	final public boolean useAmbientOcclusion()
	{
		return true;
	}

	@Override
	final public boolean isGui3d()
	{
		return true;
	}

	@Override
	final public boolean isCustomRenderer()
	{
		return false;
	}

	@Override
	final public ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

	@Override
	public ItemOverrides getOverrides()
	{
		return ItemOverrides.EMPTY;
	}

}
