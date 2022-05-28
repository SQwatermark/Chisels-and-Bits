package mod.chiselsandbits.helpers;

import javax.annotation.Nonnull;

import mod.chiselsandbits.api.EventBlockBitModification;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Consumer;

public class ActingPlayer
{
	private final Container storage;

	// used to test permission and stuff...
	private final Player innerPlayer;
	private final boolean realPlayer; // are we a real player?
	private final InteractionHand hand;

	private ActingPlayer(
			final Player player,
			final boolean realPlayer,
			final InteractionHand hand )
	{
		innerPlayer = player;
		this.hand = hand;
		this.realPlayer = realPlayer;
		storage = realPlayer ? player.getInventory() : new PlayerCopiedInventory( player.getInventory() );
	}

	public Container getInventory()
	{
		return storage;
	}

	public int getCurrentItem()
	{
		return innerPlayer.getInventory().selected;
	}

	public boolean isCreative()
	{
		return innerPlayer.isCreative();
	}

	public ItemStack getCurrentEquippedItem()
	{
		return storage.getItem( getCurrentItem() );
	}

	// permission check cache.
	BlockPos lastPos = null;
	Boolean lastPlacement = null;
	ItemStack lastPermissionBit = null;
	Boolean permissionResult = null;

	public boolean canPlayerManipulate(
			final @Nonnull BlockPos pos,
			final @Nonnull Direction side,
			final @Nonnull ItemStack is,
			final boolean placement )
	{
		// only re-test if something changes.
		if ( permissionResult == null || lastPermissionBit != is || lastPos != pos || placement != lastPlacement )
		{
			lastPos = pos;
			lastPlacement = placement;
			lastPermissionBit = is;

			if ( innerPlayer.mayUseItemAt( pos, side, is ) && innerPlayer.getCommandSenderWorld().mayInteract( innerPlayer, pos ) )
			{
				final EventBlockBitModification event = new EventBlockBitModification( innerPlayer.getCommandSenderWorld(), pos, innerPlayer, hand, is, placement );
				permissionResult = !MinecraftForge.EVENT_BUS.post( event );
			}
			else
			{
				permissionResult = false;
			}
		}

		return permissionResult;
	}

	public void damageItem(
			final ItemStack stack,
			final int amount )
	{
		if ( realPlayer )
		{
			stack.hurtAndBreak(amount, innerPlayer, playerEntity -> {});
		}
		else
		{
			stack.setDamageValue( stack.getDamageValue() + amount );
		}
	}

	public void playerDestroyItem(
			final @Nonnull ItemStack stack,
			final InteractionHand hand )
	{
		if ( realPlayer )
		{
			net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem( innerPlayer, stack, hand );
		}
	}

	@Nonnull
	public static ActingPlayer actingAs(
			final Player player,
			final InteractionHand hand )
	{
		return new ActingPlayer( player, true, hand );
	}

	@Nonnull
	public static ActingPlayer testingAs(
			final Player player,
			final InteractionHand hand )
	{
		return new ActingPlayer( player, false, hand );
	}

	public Level getWorld()
	{
		return innerPlayer.getCommandSenderWorld();
	}

	/**
	 * only call this is you require a player, and only as a last resort.
	 */
	public Player getPlayer()
	{
		return innerPlayer;
	}

	public boolean isReal()
	{
		return realPlayer;
	}

	/**
	 * @return the hand
	 */
	public InteractionHand getHand()
	{
		return hand;
	}

}
