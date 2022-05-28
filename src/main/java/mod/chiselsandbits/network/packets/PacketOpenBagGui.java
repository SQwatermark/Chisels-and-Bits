package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.bitbag.BagContainer;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public class PacketOpenBagGui extends ModPacket
{
    public PacketOpenBagGui(FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public PacketOpenBagGui()
    {
    }

    @Override
	public void server(
			final ServerPlayer player )
	{
	    player.openMenu(new SimpleMenuProvider(
          (id, playerInventory, playerEntity) -> new BagContainer(id, playerInventory),
          new TextComponent("Bitbag")
        ));
	}

	@Override
	public void getPayload(
			final FriendlyByteBuf buffer )
	{
		// no data...
	}

	@Override
	public void readPayload(
			final FriendlyByteBuf buffer )
	{
		// no data..
	}

}
