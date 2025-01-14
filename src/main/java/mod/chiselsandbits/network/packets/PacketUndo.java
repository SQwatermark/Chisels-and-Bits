package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.chiseledblock.data.BitIterator;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.client.UndoTracker;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.api.BitAccess;
import mod.chiselsandbits.helpers.*;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class PacketUndo extends ModPacket
{

	private BlockPos pos;
	private VoxelBlobStateReference before;
	private VoxelBlobStateReference after;

	public PacketUndo(FriendlyByteBuf buffer)
	{
	    readPayload(buffer);
	}

	public PacketUndo(
			final BlockPos pos,
			final VoxelBlobStateReference before,
			final VoxelBlobStateReference after )
	{
		this.pos = pos;
		this.before = before;
		this.after = after;
	}

	@Override
	public void server(
			final ServerPlayer player )
	{
		preformAction( ActingPlayer.actingAs( player, InteractionHand.MAIN_HAND ), true );
	}

	@Override
	public void getPayload(
			final FriendlyByteBuf buffer )
	{
		buffer.writeBlockPos( pos );

		final byte[] bef = before.getByteArray();
		buffer.writeInt( bef.length );
		buffer.writeBytes( bef );

		final byte[] aft = after.getByteArray();
		buffer.writeInt( aft.length );
		buffer.writeBytes( aft );
	}

	@Override
	public void readPayload(
			final FriendlyByteBuf buffer )
	{
		pos = buffer.readBlockPos();

		final int lena = buffer.readInt();
		final byte[] ta = new byte[lena];
		buffer.readBytes( ta );

		final int lenb = buffer.readInt();
		final byte[] tb = new byte[lenb];
		buffer.readBytes( tb );

		before = new VoxelBlobStateReference( ta, 0 );
		after = new VoxelBlobStateReference( tb, 0 );
	}

	public boolean preformAction(
			final ActingPlayer player,
			final boolean spawnItemsAndCommitWorldChanges )
	{
		if ( inRange( player, pos ) )
		{
			return apply( player, spawnItemsAndCommitWorldChanges );
		}

		return false;
	}

	private boolean apply(
			final ActingPlayer player,
			final boolean spawnItemsAndCommitWorldChanges )
	{
		try
		{
			final Direction side = Direction.UP;

			final Level world = player.getWorld();
			final BitAccess ba = (BitAccess) ChiselsAndBits.getApi().getBitAccess( world, pos );

			final VoxelBlob bBefore = before.getVoxelBlob();
			final VoxelBlob bAfter = after.getVoxelBlob();

			final VoxelBlob target = ba.getNativeBlob();

			if ( target.equals( bBefore ) )
			{
				// if something horrible goes wrong in a single block change we
				// can roll it back, but it shouldn't happen since its already
				// been approved as possible.
				final InventoryBackup backup = new InventoryBackup( player.getInventory() );

				boolean successful = true;

				final IContinuousInventory selected = new ContinousChisels( player, pos, side );
				ItemStack spawnedItem = null;

				final List<ItemEntity> spawnlist = new ArrayList<ItemEntity>();

				final BitIterator bi = new BitIterator();
				while ( bi.hasNext() )
				{
					final int inBefore = bi.getNext( bBefore );
					final int inAfter = bi.getNext( bAfter );

					if ( inBefore != inAfter )
					{
						if ( inAfter == 0 )
						{
							if ( selected.isValid() )
							{
								spawnedItem = ItemChisel.chiselBlock( selected, player, target, world, pos, side, bi.x, bi.y, bi.z, spawnedItem, spawnlist );
							}
							else
							{
								successful = false;
								break;
							}
						}
						else if ( inAfter != 0 )
						{
							if ( inBefore != 0 )
							{
								if ( selected.isValid() )
								{
									spawnedItem = ItemChisel.chiselBlock( selected, player, target, world, pos, side, bi.x, bi.y, bi.z, spawnedItem, spawnlist );
								}
								else
								{
									successful = false;
									break;
								}
							}

							final IItemInInventory bit = ModUtil.findBit( player, pos, inAfter );
							if ( bit.isValid() )
							{
								if ( !player.isCreative() )
								{
									if ( !bit.consume() )
									{
										successful = false;
										break;
									}
								}

								bi.setNext( target, inAfter );
							}
							else
							{
								successful = false;
								break;
							}
						}
					}
				}

				if ( successful )
				{
					if ( spawnItemsAndCommitWorldChanges )
					{
						ba.commitChanges( true );
						BitInventoryFeeder feeder = new BitInventoryFeeder( player.getPlayer(), player.getWorld() );
						for ( final ItemEntity ei : spawnlist )
						{
							feeder.addItem(ei);
						}
					}

					return true;
				}
				else
				{
					backup.rollback();
					UndoTracker.getInstance().addError( player, "mod.chiselsandbits.result.missing_bits" );
					return false;
				}
			}
		}
		catch ( final CannotBeChiseled e )
		{
			// error message below.
		}

		UndoTracker.getInstance().addError( player, "mod.chiselsandbits.result.has_changed" );
		return false;

	}

	private boolean inRange(
			final ActingPlayer player,
			final BlockPos pos )
	{
		if ( player.isReal() )
		{
			return true;
		}

		double reach = 6;
		if ( player.isCreative() )
		{
			reach = 32;
		}

		if ( player.getPlayer().distanceToSqr( pos.getX(), pos.getY(), pos.getZ() ) < reach * reach )
		{
			return true;
		}

		UndoTracker.getInstance().addError( player, "mod.chiselsandbits.result.out_of_range" );
		return false;
	}

}
