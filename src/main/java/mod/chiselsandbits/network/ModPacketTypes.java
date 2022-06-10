package mod.chiselsandbits.network;

import java.util.HashMap;
import java.util.function.BiConsumer;

import mod.chiselsandbits.network.packets.PacketAccurateSneakPlace;
import mod.chiselsandbits.network.packets.PacketChisel;
import mod.chiselsandbits.network.packets.PacketRotateVoxelBlob;
import mod.chiselsandbits.network.packets.PacketSetChiselMode;
import mod.chiselsandbits.network.packets.PacketSetColor;
import mod.chiselsandbits.network.packets.PacketSuppressInteraction;
import mod.chiselsandbits.network.packets.PacketUndo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum ModPacketTypes
{
	CHISEL((channel, integer) -> {
        channel.registerMessage(integer, PacketChisel.class, PacketChisel::new);
    }),
	SET_CHISEL_MODE( (channel, integer) -> {
        channel.registerMessage(integer, PacketSetChiselMode.class, PacketSetChiselMode::new);
    } ),
	ROTATE_VOXEL_BLOB( ((channel, integer) -> {
	    channel.registerMessage(integer, PacketRotateVoxelBlob.class, PacketRotateVoxelBlob::new);
    })),
	UNDO( ((channel, integer) -> {
        channel.registerMessage(integer, PacketUndo.class, PacketUndo::new);
    }) ),
	SUPRESS_INTERACTION( ((channel, integer) -> {
        channel.registerMessage(integer, PacketSuppressInteraction.class, PacketSuppressInteraction::new);
    }) ),
	SET_COLOR( ((channel, integer) -> {
        channel.registerMessage(integer, PacketSetColor.class, PacketSetColor::new);
    }) ),
	ACCURATE_PLACEMENT( ((channel, integer) -> {
        channel.registerMessage(integer, PacketAccurateSneakPlace.class, PacketAccurateSneakPlace::new);
    }) );

	private static final Logger LOGGER = LogManager.getLogger(ModPacketTypes.class);

	private final BiConsumer<NetworkChannel, Integer> registrationHandler;

	private static HashMap<Class<? extends ModPacket>, Integer> fromClassToId = new HashMap<Class<? extends ModPacket>, Integer>();
	private static HashMap<Integer, Class<? extends ModPacket>> fromIdToClass = new HashMap<Integer, Class<? extends ModPacket>>();

    ModPacketTypes(final BiConsumer<NetworkChannel, Integer> registrationHandler) {this.registrationHandler = registrationHandler;}

    public static void init(NetworkChannel channel)
	{
	    int idx = 0;

		for ( final ModPacketTypes p : ModPacketTypes.values() )
		{
		    p.registrationHandler.accept(channel, ++idx);
		}
	}

	public static int getID(
			final Class<? extends ModPacket> clz )
	{
		return fromClassToId.get( clz );
	}

	public static ModPacket constructByID(
			final int id ) throws InstantiationException, IllegalAccessException
	{
		return fromIdToClass.get( id ).newInstance();
	}

}
