package mod.chiselsandbits.render.chiseledblock;

import mod.chiselsandbits.helpers.IStateRef;
import net.minecraft.core.Direction;

public class ModelRenderState
{

	// less objects/garbage to clean up, and less memory usage.
	private IStateRef north, south, east, west, up, down;

	public IStateRef get(
			final Direction side )
	{
		switch ( side )
		{
			case DOWN:
				return down;
			case EAST:
				return east;
			case NORTH:
				return north;
			case SOUTH:
				return south;
			case UP:
				return up;
			case WEST:
				return west;
			default:
		}

		return null;
	}

	public void put(
			final Direction side,
			final IStateRef value )
	{
		switch (side) {
			case DOWN -> down = value;
			case EAST -> east = value;
			case NORTH -> north = value;
			case SOUTH -> south = value;
			case UP -> up = value;
			case WEST -> west = value;
			default -> {
			}
		}
	}

	public ModelRenderState(
			final ModelRenderState sides )
	{
		if ( sides != null )
		{
			north = sides.north;
			south = sides.south;
			east = sides.east;
			west = sides.west;
			up = sides.up;
			down = sides.down;
		}
	}

}
