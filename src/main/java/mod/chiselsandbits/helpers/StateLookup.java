package mod.chiselsandbits.helpers;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class StateLookup
{

	public static class CachedStateLookup extends StateLookup
	{

		private final BlockState[] states;

		public CachedStateLookup()
		{
			final ArrayList<BlockState> list = new ArrayList<>();

			for ( final Block blk : ForgeRegistries.BLOCKS)
			{
				for ( final BlockState state : blk.getStateDefinition().getPossibleStates() )
				{
					final int id = ModUtil.getStateId( state );

					list.ensureCapacity( id );
					while ( list.size() <= id )
					{
						list.add( null );
					}

					list.set( id, state );
				}
			}

			states = list.toArray( new BlockState[list.size()] );
		}

		@Override
		public BlockState getStateById(
				final int blockStateID )
		{
			return blockStateID >= 0 && blockStateID < states.length ? states[blockStateID] == null ? Blocks.AIR.defaultBlockState() : states[blockStateID] : Blocks.AIR.defaultBlockState();
		}

	}

	public int getStateId(
			final BlockState state )
	{
		return Block.getId( state );
	}

	public BlockState getStateById(
			final int blockStateID )
	{
		return Block.stateById( blockStateID );
	}

}
