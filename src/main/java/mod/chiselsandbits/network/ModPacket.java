package mod.chiselsandbits.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

@SuppressWarnings( "rawtypes" )
public abstract class ModPacket
{

	ServerPlayer serverEntity = null;

	public void server(
			final ServerPlayer playerEntity )
	{
		throw new RuntimeException( getClass().getName() + " is not a server packet." );
	}

	public void client()
	{
		throw new RuntimeException( getClass().getName() + " is not a client packet." );
	}

	abstract public void getPayload(
			FriendlyByteBuf buffer );

	abstract public void readPayload(
			FriendlyByteBuf buffer );

	public void processPacket(
			final NetworkEvent.Context context,
            final Boolean onServer)
	{
		if (!onServer)
		{
			client();
		}
		else
		{
		    serverEntity = context.getSender();
			server( serverEntity );
		}
	}

}
