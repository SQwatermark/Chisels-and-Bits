package mod.chiselsandbits.client.model.baked;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import org.jetbrains.annotations.NotNull;

public abstract class BaseBakedBlockModel extends BaseBakedPerspectiveModel {

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

	@NotNull
	@Override
	final public ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

	@NotNull
	@Override
	public ItemOverrides getOverrides()
	{
		return ItemOverrides.EMPTY;
	}

}
