package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.bitbag.BagContainer;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;

public class PacketBagGui extends ModPacket
{
	private int slotNumber = -1;
	private int mouseButton = -1;
	private boolean duplicateButton = false;
	private boolean holdingShift = false;

    public PacketBagGui(FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public PacketBagGui(final int slotNumber, final int mouseButton, final boolean duplicateButton, final boolean holdingShift)
    {
        this.slotNumber = slotNumber;
        this.mouseButton = mouseButton;
        this.duplicateButton = duplicateButton;
        this.holdingShift = holdingShift;
    }

    @Override
	public void server(
			final ServerPlayer player )
	{
		doAction( player );
	}

	public void doAction(
			final Player player )
	{
		final AbstractContainerMenu c = player.containerMenu;
		if ( c instanceof BagContainer )
		{
			final BagContainer bc = (BagContainer) c;
			bc.handleCustomSlotAction( slotNumber, mouseButton, duplicateButton, holdingShift );
		}
	}

	@Override
	public void getPayload(
			final FriendlyByteBuf buffer )
	{
		buffer.writeInt( slotNumber );
		buffer.writeInt( mouseButton );
		buffer.writeBoolean( duplicateButton );
		buffer.writeBoolean( holdingShift );
	}

	@Override
	public void readPayload(
			final FriendlyByteBuf buffer )
	{
		slotNumber = buffer.readInt();
		mouseButton = buffer.readInt();
		duplicateButton = buffer.readBoolean();
		holdingShift = buffer.readBoolean();
	}

}
