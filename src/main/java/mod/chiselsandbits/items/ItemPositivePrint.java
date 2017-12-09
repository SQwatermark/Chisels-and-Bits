package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ActingPlayer;
import mod.chiselsandbits.helpers.ContinousChisels;
import mod.chiselsandbits.helpers.IContinuousInventory;
import mod.chiselsandbits.helpers.InfiniteBitStorage;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
import mod.chiselsandbits.integration.mcmultipart.MCMultipartProxy;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.localization.BitName;
import mod.chiselsandbits.localization.ChiselErrors;
import mod.chiselsandbits.localization.LocalStrings;
import mod.chiselsandbits.modes.PositivePatternMode;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketAccurateSneakPlace;
import mod.chiselsandbits.network.packets.PacketAccurateSneakPlace.IItemBlockAccurate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPositivePrint extends ItemNegativePrint implements IChiselModeItem, IItemBlockAccurate
{

	@Override
	@SideOnly( Side.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<String> tooltip,
			final ITooltipFlag advanced )
	{
		defaultAddInfo( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpPositivePrint, tooltip,
				ClientSide.instance.getKeyName( Minecraft.getMinecraft().gameSettings.keyBindUseItem ),
				ClientSide.instance.getKeyName( Minecraft.getMinecraft().gameSettings.keyBindUseItem ),
				ClientSide.instance.getModeKey() );

		if ( stack.hasTagCompound() )
		{
			if ( ClientSide.instance.holdingShift() )
			{
				if ( toolTipCache.needsUpdate( stack ) )
				{
					final VoxelBlob blob = ModUtil.getBlobFromStack( stack, null );
					toolTipCache.updateCachedValue( blob.listContents( new ArrayList<String>() ) );
				}

				tooltip.addAll( toolTipCache.getCached() );
			}
			else
			{
				tooltip.add( LocalStrings.ShiftDetails.getLocal() );
			}
		}
	}

	@Override
	protected NBTTagCompound getCompoundFromBlock(
			final World world,
			final BlockPos pos,
			final EntityPlayer player )
	{
		final IBlockState state = world.getBlockState( pos );
		final Block blkObj = state.getBlock();

		if ( !( blkObj instanceof BlockChiseled ) && BlockBitInfo.supportsBlock( state ) )
		{
			final NBTBlobConverter tmp = new NBTBlobConverter();

			tmp.fillWith( state );
			final NBTTagCompound comp = new NBTTagCompound();
			tmp.writeChisleData( comp, false );

			comp.setByte( ModUtil.NBT_SIDE, (byte) ModUtil.getPlaceFace( player ).ordinal() );
			return comp;
		}

		return super.getCompoundFromBlock( world, pos, player );
	}

	@Override
	protected boolean convertToStone()
	{
		return false;
	}

	@Override
	public EnumActionResult onItemUse(
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumHand hand,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final ItemStack stack = player.getHeldItem( hand );

		if ( PositivePatternMode.getMode( stack ) == PositivePatternMode.PLACEMENT )
		{
			if ( player.isSneaking() )
			{
				if ( !world.isRemote )
				{
					// Say it "worked", Don't do anything we'll get a better
					// packet.
					return EnumActionResult.SUCCESS;
				}
				else
				{
					// send accurate packet.
					final PacketAccurateSneakPlace pasp = new PacketAccurateSneakPlace();

					pasp.hand = hand;
					pasp.pos = pos;
					pasp.side = side;
					pasp.stack = stack;
					pasp.hitX = hitX;
					pasp.hitY = hitY;
					pasp.hitZ = hitZ;

					NetworkRouter.instance.sendToServer( pasp );
				}
			}
		}

		return doItemUse( stack, player, world, pos, hand, side, hitX, hitY, hitZ );
	}

	@Override
	public final EnumActionResult doItemUse(
			final ItemStack stack,
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumHand hand,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		if ( PositivePatternMode.getMode( stack ) == PositivePatternMode.PLACEMENT )
		{
			final ItemStack output = getPatternedItem( stack, false );
			if ( output != null )
			{
				final VoxelBlob pattern = ModUtil.getBlobFromStack( stack, player );
				final Map<Integer, Integer> stats = pattern.getBlockSums();

				if ( consumeEntirePattern( pattern, stats, pos, ActingPlayer.testingAs( player, hand ) ) && output.getItem() instanceof ItemBlockChiseled )
				{
					final ItemBlockChiseled ibc = (ItemBlockChiseled) output.getItem();
					final EnumActionResult res = ibc.doItemUse( output, player, world, pos, hand, side, hitX, hitY, hitZ );

					if ( res == EnumActionResult.SUCCESS )
					{
						consumeEntirePattern( pattern, stats, pos, ActingPlayer.actingAs( player, hand ) );
					}

					return res;
				}

				return EnumActionResult.FAIL;
			}
		}

		return super.onItemUse( player, world, pos, hand, side, hitX, hitY, hitZ );
	}

	private boolean consumeEntirePattern(
			final VoxelBlob pattern,
			final Map<Integer, Integer> stats,
			final BlockPos pos,
			final ActingPlayer player )
	{
		final List<BagInventory> bags = ModUtil.getBags( player );
		InfiniteBitStorage infiniteStorage = new InfiniteBitStorage();

		boolean missing = false;

		for ( final Entry<Integer, Integer> type : stats.entrySet() )
		{
			final int inPattern = type.getKey();

			if ( type.getKey() == 0 )
			{
				continue;
			}

			ContinousChisels cc = new ContinousChisels( player, pos, EnumFacing.UP );
			ItemStackSlot bit = ModUtil.findBit( player, pos, inPattern );
			int stillNeeded = type.getValue() - ModUtil.consumeBagBit( bags, inPattern, type.getValue() );
			stillNeeded = stillNeeded - infiniteStorage.attempToConsume( inPattern, stillNeeded );
			if ( stillNeeded != 0 )
			{
				for ( int x = stillNeeded; x > 0 && bit.isValid(); --x )
				{
					bit.consume();
					stillNeeded--;
					bit = ModUtil.findBit( player, pos, inPattern );
				}

				// start smashing blocks.
				while ( stillNeeded != 0 && infiniteStorage.chiselBlock( inPattern, player, cc ) )
				{
					stillNeeded = stillNeeded - infiniteStorage.attempToConsume( inPattern, stillNeeded );
				}

				if ( stillNeeded != 0 )
				{
					player.report( ChiselErrors.NO_BITS, new BitName( inPattern ) );
					missing = true;
				}
			}
		}

		if ( missing )
		{
			player.displayError();
			return false;
		}

		infiniteStorage.give( player );
		return true;
	}

	@Override
	protected void applyPrint(
			final ItemStack stack,
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final VoxelBlob vb,
			final VoxelBlob pattern,
			final EntityPlayer who,
			final EnumHand hand )
	{
		// snag a tool...
		final ActingPlayer player = ActingPlayer.actingAs( who, hand );
		final IContinuousInventory chisels = new ContinousChisels( player, pos, side );
		InfiniteBitStorage infiniteStorage = new InfiniteBitStorage();

		ItemStack spawnedItem = null;

		final VoxelBlob filled = new VoxelBlob();
		MCMultipartProxy.proxyMCMultiPart.addFiller( world, pos, filled );

		final List<BagInventory> bags = ModUtil.getBags( player );

		final PositivePatternMode chiselMode = PositivePatternMode.getMode( stack );
		final boolean chisel_bits = chiselMode == PositivePatternMode.IMPOSE || chiselMode == PositivePatternMode.REPLACE;
		final boolean chisel_to_air = chiselMode == PositivePatternMode.REPLACE;

		for ( int y = 0; y < vb.detail; y++ )
		{
			for ( int z = 0; z < vb.detail; z++ )
			{
				for ( int x = 0; x < vb.detail; x++ )
				{
					int inPlace = vb.get( x, y, z );
					final int inPattern = pattern.get( x, y, z );
					if ( inPlace != inPattern )
					{
						if ( inPlace != 0 && chisel_bits && chisels.isValid() )
						{
							if ( chisel_to_air || inPattern != 0 )
							{
								ItemChisel.chiselBlock( chisels, player, vb, world, pos, side, x, y, z, infiniteStorage );

								if ( spawnedItem != null )
								{
									inPlace = 0;
								}
							}
						}

						if ( inPlace == 0 && inPattern != 0 && filled.get( x, y, z ) == 0 )
						{
							if ( infiniteStorage.dec( inPattern ) )
							{
								vb.set( x, y, z, inPattern );
							}
							else if ( ModUtil.consumeBagBit( bags, inPattern, 1 ) == 1 )
							{
								vb.set( x, y, z, inPattern );
							}
							else
							{
								final ItemStackSlot bit = ModUtil.findBit( player, pos, inPattern );
								if ( bit.isValid() )
								{
									vb.set( x, y, z, inPattern );

									if ( !player.isCreative() )
									{
										bit.consume();
									}
								}
								else if ( infiniteStorage.chiselBlock( inPattern, player, chisels ) )
								{
									infiniteStorage.dec( 1 );
									vb.set( x, y, z, inPattern );
								}
							}
						}
					}
				}
			}
		}

		player.displayError();
		infiniteStorage.give( player );
	}

	@Override
	public String getHighlightTip(
			final ItemStack item,
			final String displayName )
	{
		if ( ChiselsAndBits.getConfig().itemNameModeDisplay )
		{
			return displayName + " - " + PositivePatternMode.getMode( item ).string.getLocal();
		}

		return displayName;
	}

}
