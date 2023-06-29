package mod.chiselsandbits.helpers;

import mod.chiselsandbits.utils.LanguageHandler;
import mod.chiselsandbits.utils.SingleBlockBlockReader;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.DistExecutor;

public class DeprecationHelper
{

	public static int getLightValue(
			final BlockState state )
	{
		return state.getBlock().getLightEmission( state, new SingleBlockBlockReader(state, state.getBlock()), BlockPos.ZERO );
	}

	public static BlockState getStateFromItem(
			final ItemStack bitItemStack )
	{
		if ( bitItemStack != null && bitItemStack.getItem() instanceof BlockItem)
		{
			final BlockItem blkItem = (BlockItem) bitItemStack.getItem();
			return blkItem.getBlock().defaultBlockState();
		}

		return null;
	}

	public static String translateToLocal(
			final String string )
	{
	    return DistExecutor.unsafeRunForDist(
          () -> () -> {
              final String translated = Language.getInstance().getOrDefault(string);
              if (translated.equals(string))
                  return LanguageHandler.translateKey(string);

              return translated;
              },
          () -> () -> LanguageHandler.translateKey(string)
        );
	}

	public static String translateToLocal(
			final String string,
			final Object... args )
	{
        return String.format(translateToLocal(string), args);
	}

	public static SoundType getSoundType(
			BlockState block )
	{
		return block.getSoundType();
	}

    public static SoundType getSoundType(
      Block block )
    {
        return block.defaultBlockState().getSoundType();
    }
}
