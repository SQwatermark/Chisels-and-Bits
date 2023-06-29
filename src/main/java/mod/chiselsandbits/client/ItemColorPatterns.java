package mod.chiselsandbits.client;

import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemColorPatterns implements ItemColor
{

	@Override
	public int getColor(
			final ItemStack stack,
			final int tint )
	{
		if ( ClientSide.instance.holdingShift() )
		{
			final BlockState state = ModUtil.getStateById( tint >> BlockColorChiseled.TINT_BITS );
			final Block blk = state.getBlock();
			final Item i = Item.byBlock( blk );
			int tintValue = tint & BlockColorChiseled.TINT_MASK;

			return ModelUtil.getItemStackColor( new ItemStack( i, 1 ), tintValue );

		}

		return 0xffffffff;
	}

}
