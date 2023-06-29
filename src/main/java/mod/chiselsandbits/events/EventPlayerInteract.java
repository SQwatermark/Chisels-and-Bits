package mod.chiselsandbits.events;

import java.util.WeakHashMap;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Disable breaking blocks when using a chisel / bit, some items break too fast
 * for the other code to prevent which is where this comes in.
 *
 * This manages survival chisel actions, creative somehow skips this and calls
 * onBlockStartBreak on its own, but when in creative this is called on the
 * server... which still needs to be canceled, or it will break the block.
 *
 * The whole things, is very strange.
 */
public class EventPlayerInteract
{
	private static final WeakHashMap<Player, Boolean> serverSuppressEvent = new WeakHashMap<>();

	public static void setPlayerSuppressionState(Player player, boolean state) {
		if (state) {
			serverSuppressEvent.put( player, state );
		} else {
			serverSuppressEvent.remove(player);
		}
	}

	@SubscribeEvent
	public void interaction(LeftClickBlock event) {
		if (event.getEntity() != null && event.getUseItem() != Event.Result.DENY) {
			final ItemStack is = event.getItemStack();
			final boolean validEvent = event.getPos() != null && event.getLevel() != null;
			if ( is != null && ( is.getItem() instanceof ItemChisel || is.getItem() instanceof ItemChiseledBit ) && validEvent )
			{
				final BlockState state = event.getLevel().getBlockState( event.getPos() );
				if ( BlockBitInfo.canChisel( state ) )
				{
					if ( event.getLevel().isClientSide )
					{
						// this is called when the player is survival -
						// client side.
						is.getItem().onBlockStartBreak( is, event.getPos(), event.getEntity() );
					}

					// cancel interactions vs chiseable blocks, creative is
					// magic.
					event.setCanceled( true );
				}
			}
		}

		testInteractionSupression(event, event.getUseItem());
	}

	@SubscribeEvent
	public void interaction(RightClickBlock event) {
		testInteractionSupression(event, event.getUseItem());
	}

	private void testInteractionSupression(PlayerInteractEvent event, Event.Result useItem) {
		// client is dragging...
		if ( event.getLevel().isClientSide )
		{
			if ( ClientSide.instance.getStartPos() != null )
			{
				event.setCanceled( true );
			}
		}

		// server is supressed.
		if ( !event.getLevel().isClientSide && event.getEntity() != null && useItem != Event.Result.DENY )
		{
			if ( serverSuppressEvent.containsKey( event.getEntity() ) )
			{
				event.setCanceled( true );
			}
		}
	}
}
