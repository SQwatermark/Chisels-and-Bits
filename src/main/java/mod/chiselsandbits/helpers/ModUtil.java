package mod.chiselsandbits.helpers;

import mod.chiselsandbits.chiseledblock.BlockEntityChiseledBlock;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.data.IntegerBox;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.StateLookup.CachedStateLookup;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.items.ItemNegativePrint;
import mod.chiselsandbits.items.ItemPositivePrint;
import mod.chiselsandbits.render.helpers.SimpleInstanceCache;
import mod.chiselsandbits.utils.SingleBlockBlockReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.registries.GameData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public class ModUtil {

    @Nonnull
    public static final String NBT_SIDE = "side";

    @Nonnull
    public static final String NBT_BLOCKENTITYTAG = "BlockEntityTag";

    private final static Random RAND = new Random();
    private final static float DEG_TO_RAD = 0.017453292f;

    private static final SimpleInstanceCache<CompoundTag, VoxelBlob> STACK_VOXEL_BLOB_SIMPLE_INSTANCE_CACHE = new SimpleInstanceCache<>(new CompoundTag(), null);

    static public Direction getPlaceFace(final LivingEntity placer) {
        return Direction.orderedByNearest(placer)[0].getOpposite();
    }

    static public Pair<Vec3, Vec3> getPlayerRay(Player playerIn) {

        double reachDistance = 5.0d;

        // TODO 为什么？
        final double x = playerIn.xo + (playerIn.getX() - playerIn.xo);
        final double y = playerIn.yo + (playerIn.getY() - playerIn.yo) + playerIn.getEyeHeight();
        final double z = playerIn.zo + (playerIn.getZ() - playerIn.zo);

        final float playerPitch = playerIn.xRotO + (playerIn.getXRot() - playerIn.xRotO);
        final float playerYaw = playerIn.yRotO + (playerIn.getYRot() - playerIn.yRotO);

        final float yawRayX = Mth.sin(-playerYaw * DEG_TO_RAD - (float) Math.PI);
        final float yawRayZ = Mth.cos(-playerYaw * DEG_TO_RAD - (float) Math.PI);

        final float pitchMultiplier = -Mth.cos(-playerPitch * DEG_TO_RAD);
        final float eyeRayY = Mth.sin(-playerPitch * DEG_TO_RAD);
        final float eyeRayX = yawRayX * pitchMultiplier;
        final float eyeRayZ = yawRayZ * pitchMultiplier;

        if (playerIn instanceof ServerPlayer) {
            reachDistance = playerIn.getAttributeValue(ForgeMod.BLOCK_REACH.get());
        }

        final Vec3 from = new Vec3(x, y, z);
        final Vec3 to = from.add(eyeRayX * reachDistance, eyeRayY * reachDistance, eyeRayZ * reachDistance);

        return Pair.of(from, to);
    }

    static public IItemInInventory findBit(
            final ActingPlayer who,
            final BlockPos pos,
            final int StateID) {
        final ItemStack inHand = who.getCurrentEquippedItem();
        final Container inv = who.getInventory();
        final boolean canEdit = who.canPlayerManipulate(pos, Direction.UP, inHand, true);

        if (getStackSize(inHand) > 0 && inHand.getItem() instanceof ItemChiseledBit && ItemChiseledBit.getStackState(inHand) == StateID) {
            return new ItemStackSlot(inv, who.getCurrentItem(), inHand, who, canEdit);
        }

        for (int x = 0; x < inv.getContainerSize(); x++) {
            final ItemStack is = inv.getItem(x);
            if (getStackSize(is) > 0 && is.getItem() instanceof ItemChiseledBit && ItemChiseledBit.sameBit(is, StateID)) {
                return new ItemStackSlot(inv, x, is, who, canEdit);
            }
        }

        return new ItemStackSlot(inv, -1, ModUtil.getEmptyStack(), who, canEdit);
    }

    public static @Nonnull ItemStack copy(
            final ItemStack st) {
        if (st == null) {
            return ModUtil.getEmptyStack();
        }

        return nonNull(st.copy());
    }

    public static @Nonnull ItemStack nonNull(
            final ItemStack st) {
        if (st == null) {
            return ModUtil.getEmptyStack();
        }

        return st;
    }

    public static boolean isHoldingPattern(
            final Player player) {
        final ItemStack inHand = player.getMainHandItem();

        if (inHand != null && inHand.getItem() instanceof ItemPositivePrint) {
            return true;
        }

        if (inHand != null && inHand.getItem() instanceof ItemNegativePrint) {
            return true;
        }

        return false;
    }

    public static boolean isHoldingChiseledBlock(
            final Player player) {
        final ItemStack inHand = player.getMainHandItem();

        if (inHand != null && inHand.getItem() instanceof ItemBlockChiseled) {
            return true;
        }

        return false;
    }

    public static int getRotationIndex(
            final Direction face) {
        return face.get2DDataValue();
    }

    public static int getRotations(
            final LivingEntity placer,
            final Direction oldYaw) {
        final Direction newFace = ModUtil.getPlaceFace(placer);

        int rotations = getRotationIndex(newFace) - getRotationIndex(oldYaw);

        // work out the rotation math...
        while (rotations < 0) {
            rotations = 4 + rotations;
        }
        while (rotations > 4) {
            rotations = rotations - 4;
        }

        return 4 - rotations;
    }

    public static BlockPos getPartialOffset(
            final Direction side,
            final BlockPos partial,
            final IntegerBox modelBounds) {
        int offset_x = modelBounds.minX;
        int offset_y = modelBounds.minY;
        int offset_z = modelBounds.minZ;

        final int partial_x = partial.getX();
        final int partial_y = partial.getY();
        final int partial_z = partial.getZ();

        int middle_x = (modelBounds.maxX - modelBounds.minX) / -2;
        int middle_y = (modelBounds.maxY - modelBounds.minY) / -2;
        int middle_z = (modelBounds.maxZ - modelBounds.minZ) / -2;

        switch (side) {
            case DOWN:
                offset_y = modelBounds.maxY;
                middle_y = 0;
                break;
            case EAST:
                offset_x = modelBounds.minX;
                middle_x = 0;
                break;
            case NORTH:
                offset_z = modelBounds.maxZ;
                middle_z = 0;
                break;
            case SOUTH:
                offset_z = modelBounds.minZ;
                middle_z = 0;
                break;
            case UP:
                offset_y = modelBounds.minY;
                middle_y = 0;
                break;
            case WEST:
                offset_x = modelBounds.maxX;
                middle_x = 0;
                break;
            default:
                throw new NullPointerException();
        }

        final int t_x = -offset_x + middle_x + partial_x;
        final int t_y = -offset_y + middle_y + partial_y;
        final int t_z = -offset_z + middle_z + partial_z;

        return new BlockPos(t_x, t_y, t_z);
    }

    @SafeVarargs
    static public <T> T firstNonNull(
            final T... options) {
        for (final T i : options) {
            if (i != null) {
                return i;
            }
        }

        throw new NullPointerException("Unable to find a non null item.");
    }

    public static BlockEntity getTileEntitySafely(
            final @Nonnull BlockGetter world,
            final @Nonnull BlockPos pos) {

        // stupid...
        if (world instanceof Level) {
            return ((Level) world).getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
        }

        // yep... stupid.
        else {
            return world.getBlockEntity(pos);
        }
    }

    public static BlockEntityChiseledBlock getChiseledTileEntity(
            @Nonnull final BlockGetter world,
            @Nonnull final BlockPos pos) {
        final BlockEntity te = getTileEntitySafely(world, pos);
        if (te instanceof BlockEntityChiseledBlock) {
            return (BlockEntityChiseledBlock) te;
        }

        return null;
    }

    public static BlockEntityChiseledBlock getChiseledTileEntity(
            @Nonnull final Level world,
            @Nonnull final BlockPos pos,
            final boolean create) {
        if (world.hasChunkAt(pos)) {
            final BlockEntity te = world.getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
            if (te instanceof BlockEntityChiseledBlock) {
                return (BlockEntityChiseledBlock) te;
            }

            return null;
        }
        return null;
    }

    public static void removeChiseledBlock(
            @Nonnull final Level world,
            @Nonnull final BlockPos pos) {
        final BlockEntity te = world.getBlockEntity(pos);
        final BlockState oldState = world.getBlockState(pos);

        if (te instanceof BlockEntityChiseledBlock) {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()); // no physical matter left...
            return;
        }

        world.setBlocksDirty(pos, oldState, Blocks.AIR.defaultBlockState());
    }

    public static boolean containsAtLeastOneOf(
            final Container inv,
            final ItemStack is) {
        boolean seen = false;
        for (int x = 0; x < inv.getContainerSize(); x++) {
            final ItemStack which = inv.getItem(x);

            if (which != null && which.getItem() == is.getItem() && ItemChiseledBit.sameBit(which, ItemChiseledBit.getStackState(is))) {
                if (!seen) {
                    seen = true;
                }
            }
        }
        return seen;
    }

    public static VoxelBlob getBlobFromStack(
            final ItemStack stack,
            final LivingEntity rotationPlayer) {
        if (stack.hasTag()) {
            VoxelBlob blob;
            CompoundTag cData = getSubCompound(stack, NBT_BLOCKENTITYTAG, false);

            if (STACK_VOXEL_BLOB_SIMPLE_INSTANCE_CACHE.needsUpdate(cData)) {
                final NBTBlobConverter tmp = new NBTBlobConverter();

                if (cData.size() == 0) {
                    cData = stack.getTag();
                }

                tmp.readChisleData(cData, VoxelBlob.VERSION_ANY);
                blob = tmp.getBlob();
                STACK_VOXEL_BLOB_SIMPLE_INSTANCE_CACHE.updateCachedValue(new VoxelBlob(blob));
            } else {
                blob = new VoxelBlob(STACK_VOXEL_BLOB_SIMPLE_INSTANCE_CACHE.getCached());
            }

            if (rotationPlayer != null) {
                int xrotations = ModUtil.getRotations(rotationPlayer, ModUtil.getSide(stack));
                while (xrotations-- > 0) {
                    blob = blob.spin(Direction.Axis.Y);
                }
            }

            return blob;
        }

        return new VoxelBlob();
    }

    public static void sendUpdate(
            @Nullable final Level worldObj,
            @Nonnull final BlockPos pos) {
        if (worldObj == null)
            return;

        final BlockState state = worldObj.getBlockState(pos);
        worldObj.sendBlockUpdated(pos, state, state, 0);
    }

    private static Item getItem(@NotNull final BlockState blockState) {
        final Block block = blockState.getBlock();
        if (block.equals(Blocks.LAVA)) {
            return Items.LAVA_BUCKET;
        } else if (block instanceof CropBlock) {
            final ItemStack stack = ((CropBlock) block).getCloneItemStack(null, null, blockState);
            if (stack != null) {
                return stack.getItem();
            }

            return Items.WHEAT_SEEDS;
        }
        // oh no...
        else if (block instanceof FarmBlock || block instanceof DirtPathBlock) {
            return getItemFromBlock(Blocks.DIRT);
        } else if (block instanceof FireBlock) {
            return Items.FLINT_AND_STEEL;
        } else if (block instanceof FlowerPotBlock) {
            return Items.FLOWER_POT;
        } else if (block == Blocks.BAMBOO_SAPLING) {
            return Items.BAMBOO;
        } else {
            return getItemFromBlock(block);
        }
    }

    private static Item getItemFromBlock(final Block block) {
        return GameData.getBlockItemMap().get(block);
    }

    /**
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack fromt the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(@NotNull final BlockState blockState) {
        if (blockState.getBlock() instanceof IFluidBlock) {
            return FluidUtil.getFilledBucket(new FluidStack(((IFluidBlock) blockState.getBlock()).getFluid(), 1000));
        }
        final Item item = getItem(blockState);
        if (item != Items.AIR && item != null) {
            return new ItemStack(item, 1);
        }

        return new ItemStack(blockState.getBlock(), 1);
    }

    @Nullable
    public static VoxelBlob rotate(
            final VoxelBlob blob,
            final Direction.Axis axis,
            final Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_90:
                return blob.spin(axis).spin(axis).spin(axis);
            case CLOCKWISE_180:
                return blob.spin(axis).spin(axis);
            case COUNTERCLOCKWISE_90:
                return blob.spin(axis);
            case NONE:
            default:
                break;

        }
        return null;
    }

    public static boolean isNormalCube(
            final BlockState blockType,
            final BlockGetter reader,
            final BlockPos pos) {
        return blockType.isRedstoneConductor(reader, pos);
    }

    public static boolean isNormalCube(
            final BlockState blockState
    ) {
        return isNormalCube(blockState, new SingleBlockBlockReader(blockState, blockState.getBlock()), BlockPos.ZERO);
    }

    public static Direction getSide(
            final ItemStack stack) {
        if (stack != null) {
            final CompoundTag blueprintTag = stack.getTag();

            int byteValue = Direction.NORTH.ordinal();

            if (blueprintTag == null) {
                return Direction.NORTH;
            }

            if (blueprintTag.contains(NBT_SIDE)) {
                byteValue = blueprintTag.getByte(NBT_SIDE);
            }

            if (blueprintTag.contains(NBT_BLOCKENTITYTAG)) {
                final CompoundTag c = blueprintTag.getCompound(NBT_BLOCKENTITYTAG);
                if (c.contains(NBT_SIDE)) {
                    byteValue = c.getByte(NBT_SIDE);
                }
            }

            Direction side = Direction.NORTH;

            if (byteValue >= 0 && byteValue < Direction.values().length) {
                side = Direction.values()[byteValue];
            }

            if (side == Direction.DOWN || side == Direction.UP) {
                side = Direction.NORTH;
            }

            return side;
        }

        return Direction.NORTH;
    }

    public static void setSide(
            final ItemStack stack,
            final Direction side) {
        if (stack != null) {
            CompoundTag blueprintTag = stack.getTag();

            if (blueprintTag == null) {
                blueprintTag = new CompoundTag();
            }
            if (blueprintTag.contains(NBT_BLOCKENTITYTAG)) {
                blueprintTag.getCompound(NBT_BLOCKENTITYTAG).putByte(NBT_SIDE, (byte) +side.ordinal());
            }

            blueprintTag.putInt(NBT_SIDE, +side.ordinal());

            stack.setTag(blueprintTag);
        }
    }

    private static StateLookup IDRelay = new StateLookup();

    public static BlockState getStateById(
            final int blockStateID) {
        return IDRelay.getStateById(blockStateID);
    }

    public static int getStateId(
            final BlockState state) {
        return Math.max(0, IDRelay.getStateId(state));
    }

    public static void cacheFastStates() {
//        if (!ChiselsAndBits.getConfig().getServer().lowMemoryMode.get()) { TODO
//            // cache id -> state table as an array for faster rendering lookups.
//
//        }
        IDRelay = new CachedStateLookup();
    }

    public static int getStackSize(
            final ItemStack stack) {
        return stack == null ? 0 : stack.getCount();
    }

    public static void setStackSize(
            final @Nonnull ItemStack stack,
            final int stackSize) {
        stack.setCount(stackSize);
    }

    public static void adjustStackSize(
            final @Nonnull ItemStack is,
            final int sizeDelta) {
        setStackSize(is, getStackSize(is) + sizeDelta);
    }

    public static CompoundTag getSubCompound(
            final ItemStack stack,
            final String tag,
            final boolean create) {
        if (create) {
            if (!stack.getOrCreateTag().contains(tag))
                Objects.requireNonNull(stack.getTag()).put(tag, new CompoundTag());
        }

        return stack.getOrCreateTag().getCompound(tag);
    }

    public static @Nonnull ItemStack getEmptyStack() {
        return ItemStack.EMPTY;
    }

    public static boolean notEmpty(
            final ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty();
    }

    public static boolean isEmpty(
            final ItemStack itemStack) {
        return itemStack == null || itemStack.isEmpty();
    }

    public static @Nonnull CompoundTag getTagCompound(
            final ItemStack ei) {
        return ei.getOrCreateTag();
    }

    @SuppressWarnings("deprecation")
    public static BlockState getStateFromItem(
            final ItemStack is) {
        try {
            if (!ModUtil.isEmpty(is) && is.getItem() instanceof BlockItem) {
                final BlockItem iblk = (BlockItem) is.getItem();
                final BlockState state = iblk.getBlock().defaultBlockState();

                return state;
            }
        } catch (final Throwable t) {
            // : (
        }

        return Blocks.AIR.defaultBlockState();
    }

    public static void damageItem(
            @Nonnull final ItemStack is,
            @Nonnull final RandomSource r) {
        if (is.isDamageableItem()) {
            if (is.hurt(1, r, null)) {
                is.shrink(1);
                is.setDamageValue(0);
            }
        }
    }

    @Nonnull
    public static ItemStack makeStack(
            final Item item) {
        return makeStack(item, 1);
    }

    @Nonnull
    public static ItemStack makeStack(
            final Item item,
            final int stackSize) {
        return new ItemStack(item, stackSize);
    }

    public static boolean isEmpty(final Item item) {
        return item == Items.AIR;
    }
}
