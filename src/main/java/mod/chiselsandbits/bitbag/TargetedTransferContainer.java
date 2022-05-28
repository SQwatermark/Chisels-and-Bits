package mod.chiselsandbits.bitbag;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class TargetedTransferContainer extends AbstractContainerMenu
{

    protected TargetedTransferContainer()
    {
        super(null, 0);
    }

    @Override
	public boolean stillValid(
      final Player playerIn )
	{
		return true;
	}

	public boolean doMergeItemStack(
			final ItemStack stack,
			final int startIndex,
			final int endIndex,
			final boolean reverseDirection )
	{
		return moveItemStackTo( stack, startIndex, endIndex, reverseDirection );
	}

}
