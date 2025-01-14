package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.events.EventPlayerInteract;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

public class PacketSuppressInteraction extends ModPacket
{

	private boolean newSetting = false;

	public PacketSuppressInteraction(final FriendlyByteBuf buffer) {
	    readPayload(buffer);
    }

    public PacketSuppressInteraction(final boolean newSetting)
    {
        this.newSetting = newSetting;
    }

    @Override
	public void server(ServerPlayer player)
	{
		EventPlayerInteract.setPlayerSuppressionState( player, newSetting );
	}

	@Override
	public void getPayload(
			final FriendlyByteBuf buffer )
	{
		buffer.writeBoolean( newSetting );
	}

	@Override
	public void readPayload(
			final FriendlyByteBuf buffer )
	{
		newSetting = buffer.readBoolean();
	}

}
