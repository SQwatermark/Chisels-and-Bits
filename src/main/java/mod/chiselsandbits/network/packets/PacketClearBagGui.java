package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.bitbag.BagInventoryMenu;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketClearBagGui extends ModPacket
{
	private ItemStack stack = null;

	public PacketClearBagGui(final FriendlyByteBuf buffer)
	{
	    readPayload(buffer);
	}

	public PacketClearBagGui(
			final ItemStack inHandItem )
	{
		stack = inHandItem;
	}

	@Override
	public void server(
      final ServerPlayer player )
	{
		if ( player.containerMenu instanceof BagInventoryMenu)
		{
			( (BagInventoryMenu) player.containerMenu ).clear( stack );
		}
	}

	@Override
	public void getPayload(
			final FriendlyByteBuf buffer )
	{
		buffer.writeItem( stack );
		// no data...
	}

	@Override
	public void readPayload(
			final FriendlyByteBuf buffer )
	{
        stack = buffer.readItem();
    }

}
