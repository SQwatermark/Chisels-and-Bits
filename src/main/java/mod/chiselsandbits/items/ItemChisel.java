package mod.chiselsandbits.items;

import com.google.common.base.Stopwatch;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.*;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.network.packets.PacketChisel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.minecraft.world.item.Tiers.*;

/**
 * 凿子
 */
public class ItemChisel extends DiggerItem implements IItemScrollWheel, IChiselModeItem {

	private static final float one_16th = 1.0f / 16.0f;

	public ItemChisel(Tier material, Item.Properties properties) {
		super(0.1F, -2.8F, material, BlockTags.MINEABLE_WITH_AXE, setupDamageStack(material, properties));
	}

	private static Item.Properties setupDamageStack(Tier material, Item.Properties properties) {
        long uses = 1;
        if (DIAMOND.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().diamondChiselUses.get();
        }
        else if (GOLD.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().goldChiselUses.get();
        }
        else if (IRON.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().ironChiselUses.get();
        }
        else if (STONE.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().stoneChiselUses.get();
        }
        else if (NETHERITE.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().netheriteChiselUses.get();
        }

        return properties.durability(ChiselsAndBits.getConfig().getServer().damageTools.get() ? (int) Math.max( 0, uses ) : 0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        ChiselsAndBits.getConfig().getCommon().helpText(LocalStrings.HelpChisel, tooltip,
				ClientSide.instance.getKeyName(Minecraft.getInstance().options.keyAttack),
				ClientSide.instance.getModeKey());
    }

	private static Stopwatch timer;

	public static void resetDelay() {
		timer = null;
	}

	/**
	 * alter digging behavior to chisel, uses packets to enable server to stay in-sync.
	 * 将挖掘行为转换为雕刻，使用发包实现服务器同步
	 */
	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
		return ItemChisel.fromBreakToChisel(ChiselMode.castMode(ChiselModeManager.getChiselMode(player, ChiselToolType.CHISEL, InteractionHand.MAIN_HAND)), itemstack, pos, player, InteractionHand.MAIN_HAND);
	}

	public static boolean fromBreakToChisel(ChiselMode mode, ItemStack itemstack, @Nonnull BlockPos pos, Player player, InteractionHand hand) {
		final BlockState state = player.getCommandSenderWorld().getBlockState(pos);
		if (BlockBitInfo.canChisel(state)) {
			// 防止按住雕刻键时连续雕刻
			if (itemstack != null && (timer == null || timer.elapsed(TimeUnit.MILLISECONDS) > 150)) {
				timer = Stopwatch.createStarted();
				if (mode == ChiselMode.DRAWN_REGION) {
					final Pair<Vec3, Vec3> PlayerRay = ModUtil.getPlayerRay(player);
					final Vec3 ray_from = PlayerRay.getLeft();
					final Vec3 ray_to = PlayerRay.getRight();

					final ClipContext context = new ClipContext(ray_from, ray_to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player);

					final BlockHitResult rayTraceResult = player.level.clip(context);
					final BitLocation loc = new BitLocation( rayTraceResult, BitOperation.CHISEL );
					ClientSide.instance.pointAt(ChiselToolType.CHISEL, loc, hand);
					return true;
				}

				if (!player.level.isClientSide) {
					return true;
				}

				final Pair<Vec3, Vec3> PlayerRay = ModUtil.getPlayerRay(player);
				final Vec3 ray_from = PlayerRay.getLeft();
				final Vec3 ray_to = PlayerRay.getRight();
                final ClipContext context = new ClipContext(ray_from, ray_to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player);

                BlockHitResult mop = player.level.clip(context);
				if (mop.getType() != HitResult.Type.MISS) {
				    if ((Minecraft.getInstance().hitResult != null ? Minecraft.getInstance().hitResult.getType() : HitResult.Type.MISS) == HitResult.Type.BLOCK) {
                        BlockHitResult minecraftResult = (BlockHitResult) Minecraft.getInstance().hitResult;
                        if (!minecraftResult.getBlockPos().immutable().equals(mop.getBlockPos().immutable())) {
                            mop = minecraftResult;
                        }
                    }
					useChisel( mode, player, player.level, mop, hand );
				}
			}
			return true;
		}

		if (player.getCommandSenderWorld().isClientSide) {
            return ClientSide.instance.getStartPos() != null;
		}

		return false;
	}

    @Override
    public Component getHighlightTip(ItemStack item, Component displayName) {
        if (EffectiveSide.get().isClient() && ChiselsAndBits.getConfig().getClient().itemNameModeDisplay.get() && displayName instanceof MutableComponent)
        {
            final MutableComponent formattableTextComponent = (MutableComponent) displayName;
            if ( ChiselsAndBits.getConfig().getClient().perChiselMode.get() || EffectiveSide.get().isServer())
            {
                return formattableTextComponent.append(" - ").append(ChiselMode.getMode( item ).string.getLocal());
            }
            else
            {
                return formattableTextComponent.append(" - ").append(ChiselModeManager.getChiselMode( ClientSide.instance.getPlayer(), ChiselToolType.CHISEL, InteractionHand.MAIN_HAND ).getName().getLocal());
            }
        }

        return displayName;
    }

	/**
	 * 右键轮换雕刻模式，按住shift键反向轮换
	 */
	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, @NotNull Player playerIn, @NotNull InteractionHand hand)
	{
		if (worldIn.isClientSide && ChiselsAndBits.getConfig().getClient().enableRightClickModeChange.get())
		{
			ItemStack itemStackIn = playerIn.getItemInHand(hand);
			final IToolMode mode = ChiselModeManager.getChiselMode(playerIn, ChiselToolType.CHISEL, hand);
			ChiselModeManager.scrollOption(ChiselToolType.CHISEL, mode, mode, playerIn.isShiftKeyDown() ? -1 : 1);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
		}
		return super.use(worldIn, playerIn, hand);
	}

	/**
	 * 右键轮换雕刻模式，按住shift键反向轮换
	 * TODO?
	 */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        if (context.getLevel().isClientSide && ChiselsAndBits.getConfig().getClient().enableRightClickModeChange.get())
        {
            this.use(context.getLevel(), Objects.requireNonNull(context.getPlayer()), context.getHand());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

	static void useChisel(ChiselMode mode, Player player, Level world, BlockHitResult rayTraceResult, InteractionHand hand)
	{
		final BitLocation location = new BitLocation(rayTraceResult, BitOperation.CHISEL );

		final PacketChisel pc = new PacketChisel( BitOperation.CHISEL, location, rayTraceResult.getDirection(), mode, hand );

		final int extractedState = pc.doAction( player );
		if ( extractedState != 0 )
		{
			ClientSide.breakSound( world, rayTraceResult.getBlockPos(), extractedState );

			ChiselsAndBits.getNetworkChannel().sendToServer(pc);
		}
	}

	/**
	 * Modifies VoxelData of TileEntityChiseled
	 *
	 * @param selected
	 *
	 * @param player
	 * @param vb
	 * @param world
	 * @param pos
	 * @param side
	 * @param x
	 * @param y
	 * @param z
	 * @param output
	 * @return
	 */
	static public ItemStack chiselBlock(
			final IContinuousInventory selected,
			final ActingPlayer player,
			final VoxelBlob vb,
			final Level world,
			final BlockPos pos,
			final Direction side,
			final int x,
			final int y,
			final int z,
			ItemStack output,
			final List<ItemEntity> spawnlist )
	{
		final boolean isCreative = player.isCreative();

		final int blk = vb.get( x, y, z );
		if ( blk == 0 )
		{
			return output;
		}

		if ( !canMine( selected, ModUtil.getStateById( blk ), player.getPlayer(), world, pos ) )
		{
			return output;
		}

		if ( !selected.useItem( blk ) )
		{
			return output;
		}

		if ( !world.isClientSide && !isCreative )
		{
			double hitX = x * one_16th;
			double hitY = y * one_16th;
			double hitZ = z * one_16th;

			final double offset = 0.5;
			hitX += side.getStepX() * offset;
			hitY += side.getStepY() * offset;
			hitZ += side.getStepZ() * offset;

			if ( output == null || !ItemChiseledBit.sameBit( output, blk ) || ModUtil.getStackSize( output ) == 64 )
			{
				output = ItemChiseledBit.createStack( blk, 1, true );

                spawnlist.add( new ItemEntity( world, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, output ) );
			}
			else
			{
				ModUtil.adjustStackSize( output, 1 );
			}
		}
		else
		{
			// return value...
			output = ItemChiseledBit.createStack( blk, 1, true );
		}

		vb.clear( x, y, z );
		return output;
	}

	private static boolean testingChisel = false;

	public static boolean canMine(
			final IContinuousInventory chiselInv,
			final BlockState state,
			final Player player,
			final Level world,
			final @Nonnull BlockPos pos )
	{
		final int targetState = ModUtil.getStateId( state );
		IItemInInventory chiselSlot = chiselInv.getItem( targetState );
		ItemStack chisel = chiselSlot.getStack();

		if ( player.isCreative() )
		{
			return world.mayInteract( player, pos );
		}

		if ( ModUtil.isEmpty( chisel ) )
		{
			return false;
		}

		if ( ChiselsAndBits.getConfig().getServer().enableChiselToolHarvestCheck.get() )
		{
			// this is the earily check.
			if ( state.getBlock() instanceof BlockChiseled )
			{
				return ( (BlockChiseled) state.getBlock() ).basicHarvestBlockTest( world, pos, player );
			}

			do
			{
				final Block blk = world.getBlockState( pos ).getBlock();
				BlockChiseled.setActingAs( state );
				testingChisel = true;
				chiselSlot.swapWithWeapon();
				final boolean canHarvest = world.getBlockState(pos).canHarvestBlock(world, pos, player);
				chiselSlot.swapWithWeapon();
				testingChisel = false;
				BlockChiseled.setActingAs( null );

				if ( canHarvest )
				{
					return true;
				}

				chiselInv.fail( targetState );

				chiselSlot = chiselInv.getItem( targetState );
				chisel = chiselSlot.getStack();
			}
			while ( !ModUtil.isEmpty( chisel ) );

			return false;
		}

		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(
			final BlockState blk )
	{
		Item it;

		final Tier tier = getTier();
        if (DIAMOND.equals(tier))
        {
            it = Items.DIAMOND_PICKAXE;
        }
        else if (GOLD.equals(tier))
        {
            it = Items.GOLDEN_PICKAXE;
        }
        else if (IRON.equals(tier))
        {
            it = Items.IRON_PICKAXE;
        }
        else if (WOOD.equals(tier))
        {
            it = Items.WOODEN_PICKAXE;
        }
        else
        {
            it = Items.STONE_PICKAXE;
        }

		return blk.getBlock() instanceof BlockChiseled || it.isCorrectToolForDrops( blk );
	}

//    @Override
//    public int getHarvestLevel(final ItemStack stack, final ToolType tool, @Nullable final Player player, @Nullable final BlockState blockState)
//    {
//        if ( testingChisel && stack.getItem() instanceof ItemChisel )
//        {
//            final String pattern = "(^|,)" + Pattern.quote( tool.getName() ) + "(,|$)";
//
//            final Pattern p = Pattern.compile( pattern );
//            final Matcher m = p.matcher( ChiselsAndBits.getConfig().getServer().enableChiselToolHarvestCheckTools.get() );
//
//            if ( m.find() )
//            {
//                final ItemChisel ic = (ItemChisel) stack.getItem();
//                return ic.getTier().getLevel();
//            }
//        }
//
//        return super.getHarvestLevel( stack, tool, player, blockState );
//    }

	@Override
	public void scroll(
			final Player player,
			final ItemStack stack,
			final int dwheel )
	{
		final IToolMode mode = ChiselModeManager.getChiselMode( player, ChiselToolType.CHISEL, InteractionHand.MAIN_HAND );
		ChiselModeManager.scrollOption( ChiselToolType.CHISEL, mode, mode, dwheel );
	}

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack)
    {
        return itemStack;
    }

    @Override
    public boolean hasContainerItem(final ItemStack stack)
    {
        return true;
    }
}
