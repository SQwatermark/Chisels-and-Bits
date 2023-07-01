package mod.chiselsandbits.items;

import com.google.common.base.Stopwatch;
import mod.chiselsandbits.api.ReplacementStateHandler;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.BlockChiseled.ReplaceWithChiseledValue;
import mod.chiselsandbits.chiseledblock.BlockEntityChiseledBlock;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.events.TickHandler;
import mod.chiselsandbits.helpers.*;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.network.packets.PacketChisel;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * 碎屑
 */
public class ItemChiseledBit extends Item implements IItemScrollWheel, IChiselModeItem, ICacheClearable {

    public static boolean bitBagStackLimitHack;

    private ArrayList<ItemStack> bits;

    public ItemChiseledBit(Item.Properties properties) {
        super(properties);
        ChiselsAndBits.getInstance().addClearable(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(
            final ItemStack stack,
            final Level worldIn,
            final List<Component> tooltip,
            final TooltipFlag advanced) {
        super.appendHoverText(stack, worldIn, tooltip, advanced);
        ChiselsAndBits.getConfig().getCommon().helpText(LocalStrings.HelpBit, tooltip,
                ClientSide.instance.getKeyName(Minecraft.getInstance().options.keyAttack),
                ClientSide.instance.getKeyName(Minecraft.getInstance().options.keyUse),
                ClientSide.instance.getModeKey());

        final int stateId = ItemChiseledBit.getStackState(stack);
        if (stateId == 0) {
            tooltip.add(Component.literal(ChatFormatting.RED.toString() + ChatFormatting.ITALIC.toString() + LocalStrings.AnyHelpBit.getLocal() + ChatFormatting.RESET.toString()));
        }
    }

    @Override
    public Component getHighlightTip(final ItemStack item, final Component displayName) {
        return DistExecutor.unsafeRunForDist(() -> () -> {
                    if (ChiselsAndBits.getConfig().getClient().itemNameModeDisplay.get() && displayName instanceof MutableComponent) {
                        String extra = "";
                        if (getBitOperation(ClientSide.instance.getPlayer(), InteractionHand.MAIN_HAND, item) == BitOperation.REPLACE) {
                            extra = " - " + LocalStrings.BitOptionReplace.getLocal();
                        }

                        final MutableComponent comp = (MutableComponent) displayName;

                        return comp.append(" - ").append(Component.literal(ChiselModeManager.getChiselMode(ClientSide.instance.getPlayer(), ChiselToolType.BIT, InteractionHand.MAIN_HAND).getName().getLocal())).append(Component.literal(extra));
                    }

                    return displayName;
                },
                () -> () -> displayName);
    }

    /**
     * alter digging behavior to chisel, uses packets to enable server to stay in-sync.
     */
    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        return ItemChisel.fromBreakToChisel(ChiselMode.castMode(ChiselModeManager.getChiselMode(player, ChiselToolType.BIT, InteractionHand.MAIN_HAND)), itemstack, pos, player, InteractionHand.MAIN_HAND);
    }

    public static Component getBitStateName(BlockState state) {
        ItemStack target = null;
        Block blk = null;

        if (state == null) {
            return Component.literal("Null");
        }

        try {
            // for an unknown reason its possible to generate mod blocks without
            // proper state here...
            blk = state.getBlock();

            final Item item = Item.byBlock(blk);
            if (ModUtil.isEmpty(item)) {
                final Fluid f = BlockBitInfo.getFluidFromBlock(blk);
                if (f != null) {
                    return Component.translatable(f.getFluidType().getDescriptionId());
                }
            } else {
                target = new ItemStack(() -> Item.byBlock(state.getBlock()), 1);
            }
        } catch (final IllegalArgumentException e) {
            Log.logError("Unable to get Item Details for Bit.", e);
        }

        if (target == null || target.getItem() == null) {
            return null;
        }

        try {
            final Component myName = target.getHoverName();
            if (!(myName instanceof MutableComponent))
                return myName;

            final MutableComponent formattableName = (MutableComponent) myName;

            final Set<String> extra = new HashSet<>();
            if (blk != null && state != null) {
                for (final Property<?> p : state.getProperties()) {
                    if (p.getName().equals("axis") || p.getName().equals("facing")) {
                        extra.add(DeprecationHelper.translateToLocal("mod.chiselsandbits.pretty." + p.getName() + "-" + state.getValue(p).toString()));
                    }
                }
            }

            if (extra.isEmpty()) {
                return myName;
            }

            for (final String x : extra) {
                formattableName.append(" ").append(x);
            }

            return formattableName;
        } catch (final Exception e) {
            return Component.literal("Error");
        }
    }

    static private final NonNullList<ItemStack> alternativeStacks = NonNullList.create();

    public static Component getBitTypeName(
            final ItemStack stack) {
        int stateID = ItemChiseledBit.getStackState(stack);
        if (stateID == 0) {
            //We are running an empty bit, for display purposes.
            //Lets loop:
            if (alternativeStacks.isEmpty()) {
                return Component.literal("aaaaa");
//                ModItems.ITEM_BLOCK_BIT.get().fillItemCategory(Objects.requireNonNull(ModItems.ITEM_BLOCK_BIT.get().getItemCategory()), alternativeStacks); TODO
            }

            stateID = ItemChiseledBit.getStackState(alternativeStacks.get((int) (TickHandler.getClientTicks() % ((alternativeStacks.size() * 20L)) / 20L)));

            if (stateID == 0) {
                alternativeStacks.clear();
            }
        }

        return getBitStateName(ModUtil.getStateById(stateID));
    }

    @Override
    public Component getName(
            final ItemStack stack) {
        final Component typeName = getBitTypeName(stack);

        if (typeName == null) {
            return super.getName(stack);
        }

        final MutableComponent strComponent = Component.literal("");
        return strComponent.append(super.getName(stack))
                .append(" - ")
                .append(typeName);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return bitBagStackLimitHack ? ChiselsAndBits.getConfig().getServer().bagStackSize.get() : super.getMaxStackSize(stack);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            return InteractionResult.PASS;
        }

        final Pair<Vec3, Vec3> PlayerRay = ModUtil.getPlayerRay(context.getPlayer());
        final Vec3 ray_from = PlayerRay.getLeft();
        final Vec3 ray_to = PlayerRay.getRight();
        final ClipContext rtc = new ClipContext(ray_from, ray_to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, context.getPlayer());

        final HitResult mop = context.getLevel().clip(rtc);
        if (mop != null) {
            final BlockHitResult rayTraceResult = (BlockHitResult) mop;
            return onItemUseInternal(
                    context.getPlayer(),
                    context.getLevel(),
                    context.getClickedPos(),
                    context.getHand(),
                    rayTraceResult
            );
        }

        return InteractionResult.FAIL;
    }

    public InteractionResult onItemUseInternal(
            final @Nonnull Player player,
            final @Nonnull Level world,
            final @Nonnull BlockPos usedBlock,
            final @Nonnull InteractionHand hand,
            final @Nonnull BlockHitResult rayTraceResult) {
        final ItemStack stack = player.getItemInHand(hand);

        if (!player.mayUseItemAt(usedBlock, rayTraceResult.getDirection(), stack)) {
            return InteractionResult.FAIL;
        }

        // forward interactions to tank...
        final BlockState usedState = world.getBlockState(usedBlock);

        if (world.isClientSide) {
            final IToolMode mode = ChiselModeManager.getChiselMode(player, ClientSide.instance.getHeldToolType(hand), hand);
            final BitLocation bitLocation = new BitLocation(rayTraceResult, getBitOperation(player, hand, stack));


            BlockState blkstate = world.getBlockState(bitLocation.blockPos);
            BlockEntityChiseledBlock tebc = ModUtil.getChiseledTileEntity(world, bitLocation.blockPos, true);
            ReplaceWithChiseledValue rv = null;
            if (tebc == null && (rv = BlockChiseled.replaceWithChiseled(world, bitLocation.blockPos, blkstate, ItemChiseledBit.getStackState(stack), true)).success) {
                blkstate = world.getBlockState(bitLocation.blockPos);
                tebc = rv.te;
            }

            if (tebc != null) {
                PacketChisel pc = null;
                if (mode == ChiselMode.DRAWN_REGION) {
                    if (world.isClientSide) {
                        ClientSide.instance.pointAt(getBitOperation(player, hand, stack).getToolType(), bitLocation, hand);
                    }
                    return InteractionResult.FAIL;
                } else {
                    pc = new PacketChisel(getBitOperation(player, hand, stack), bitLocation, rayTraceResult.getDirection(), ChiselMode.castMode(mode), hand);
                }

                final int result = pc.doAction(player);

                if (result > 0) {
                    ClientSide.instance.setLastTool(ChiselToolType.BIT);
                    ChiselsAndBits.getNetworkChannel().sendToServer(pc);
                }
            }
        }

        return InteractionResult.SUCCESS;

    }

//    @Override
//    public boolean canHarvestBlock(final ItemStack stack, final BlockState state)
//    {
//        return state.getBlock() instanceof BlockChiseled || super.canHarvestBlock( stack, state);
//    }

    @Override
    public boolean isCorrectToolForDrops(
            final BlockState blk) {
        return blk.getBlock() instanceof BlockChiseled || super.isCorrectToolForDrops(blk);
    }

    public static BitOperation getBitOperation(
            final Player player,
            final InteractionHand hand,
            final ItemStack stack) {
        return ReplacementStateHandler.getInstance().isReplacing() ? BitOperation.REPLACE : BitOperation.PLACE;
    }

    @Override
    public void clearCache() {
        bits = null;
    }

    // TODO
//    @Override
//    public void fillItemCategory(final CreativeModeTab tab, final NonNullList<ItemStack> items) {
//        if (!this.allowdedIn(tab)) // is this my creative tab?
//        {
//            return;
//        }
//
//        if (bits == null) {
//            bits = new ArrayList<>();
//
//            final NonNullList<ItemStack> List = NonNullList.create();
//            final BitSet used = new BitSet(4096);
//
//            for (final Object obj : ForgeRegistries.ITEMS) {
//                if (!(obj instanceof BlockItem)) {
//                    continue;
//                }
//
//                try {
//                    Item it = (Item) obj;
//                    final CreativeModeTab ctab = it.getItemCategory();
//
//                    if (ctab != null) {
//                        it.fillItemCategory(ctab, List);
//                    }
//
//                    for (final ItemStack out : List) {
//                        it = out.getItem();
//
//                        if (!(it instanceof BlockItem)) {
//                            continue;
//                        }
//
//                        final BlockState state = DeprecationHelper.getStateFromItem(out);
//                        if (state != null && BlockBitInfo.canChisel(state)) {
//                            used.set(ModUtil.getStateId(state));
//                            bits.add(ItemChiseledBit.createStack(ModUtil.getStateId(state), 1, false));
//                        }
//                    }
//
//                } catch (final Throwable t) {
//                    // a mod did something that isn't acceptable, let them crash
//                    // in their own code...
//                }
//
//                List.clear();
//            }
//
//            for (final Fluid o : ForgeRegistries.FLUIDS) {
//                if (!o.defaultFluidState().isSource()) {
//                    continue;
//                }
//
//                bits.add(ItemChiseledBit.createStack(Block.getId(o.defaultFluidState().createLegacyBlock()), 1, false));
//            }
//        }
//
//        items.addAll(bits);
//    }

    public static boolean sameBit(
            final ItemStack output,
            final int blk) {
        return output.hasTag() ? getStackState(output) == blk : false;
    }

    public static @Nonnull ItemStack createStack(
            final int id,
            final int count,
            final boolean RequireStack) {
        final ItemStack out = new ItemStack(ModItems.ITEM_BLOCK_BIT.get(), count);
        out.addTagElement("id", IntTag.valueOf(id));
        return out;
    }

    @Override
    public void scroll(
            final Player player,
            final ItemStack stack,
            final int dwheel) {
        final IToolMode mode = ChiselModeManager.getChiselMode(player, ChiselToolType.BIT, InteractionHand.MAIN_HAND);
        ChiselModeManager.scrollOption(ChiselToolType.BIT, mode, mode, dwheel);
    }

    public static int getStackState(
            final ItemStack inHand) {
        return inHand != null && inHand.hasTag() ? ModUtil.getTagCompound(inHand).getInt("id") : 0;
    }

    public static boolean placeBit(
            final IContinuousInventory bits,
            final ActingPlayer player,
            final VoxelBlob vb,
            final int x,
            final int y,
            final int z) {
        if (vb.get(x, y, z) == 0) {
            final IItemInInventory slot = bits.getItem(0);
            final int stateID = ItemChiseledBit.getStackState(slot.getStack());

            if (slot.isValid()) {
                if (!player.isCreative()) {
                    if (bits.useItem(stateID))
                        vb.set(x, y, z, stateID);
                } else
                    vb.set(x, y, z, stateID);
            }

            return true;
        }

        return false;
    }

    public static boolean hasBitSpace(
            final Player player,
            final int blk) {
        for (int x = 0; x < 36; x++) {
            final ItemStack is = player.getInventory().getItem(x);
            if ((ItemChiseledBit.sameBit(is, blk) && ModUtil.getStackSize(is) < is.getMaxStackSize()) || ModUtil.isEmpty(is)) {
                return true;
            }
        }
        return false;
    }

    private static Stopwatch timer;

}
