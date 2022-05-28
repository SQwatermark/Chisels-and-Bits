package mod.chiselsandbits.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class EventBlockBitModification extends Event
{

	private final Level        w;
	private final BlockPos     pos;
	private final Player player;
	private final InteractionHand         hand;
	private final ItemStack    stackUsed;
	private final boolean      placement;

	public EventBlockBitModification(
			final Level w,
			final BlockPos pos,
			final Player player,
			final InteractionHand hand,
			final ItemStack stackUsed,
			final boolean placement )
	{

		this.w = w;
		this.pos = pos;
		this.player = player;
		this.hand = hand;
		this.stackUsed = stackUsed;
		this.placement = placement;
	}

	public Level getWorld()
	{
		return w;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public Player getPlayer()
	{
		return player;
	}

	public InteractionHand getHand()
	{
		return hand;
	}

	public ItemStack getItemUsed()
	{
		return stackUsed;
	}

	public boolean isPlacing()
	{
		return placement;
	}

}
