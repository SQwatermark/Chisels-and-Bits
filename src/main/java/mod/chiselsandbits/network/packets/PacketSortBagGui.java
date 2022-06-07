package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.bitbag.BagInventoryMenu;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

public class PacketSortBagGui extends ModPacket
{
    public PacketSortBagGui(FriendlyByteBuf buffer) {
        readPayload(buffer);
    }

	public PacketSortBagGui()
	{
	}

	@Override
	public void server(
			final ServerPlayer player )
	{
		if ( player.containerMenu instanceof BagInventoryMenu)
		{
			( (BagInventoryMenu) player.containerMenu ).sort();
		}
	}

	@Override
	public void getPayload(
			FriendlyByteBuf buffer )
	{
	}

	@Override
	public void readPayload(
			FriendlyByteBuf buffer )
	{
	}

}
