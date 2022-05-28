package mod.chiselsandbits.api;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class EventFullBlockRestoration extends Event
{

	private final Level      w;
	private final BlockPos   pos;
	private final BlockState restoredState;

	public EventFullBlockRestoration(
			final Level w,
			final BlockPos pos,
			final BlockState restoredState )
	{

		this.w = w;
		this.pos = pos;
		this.restoredState = restoredState;
	}

	public Level getWorld()
	{
		return w;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public BlockState getState()
	{
		return restoredState;
	}

}
