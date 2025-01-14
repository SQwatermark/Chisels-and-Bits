package mod.chiselsandbits.chiseledblock;

import mod.chiselsandbits.api.*;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.client.UndoTracker;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.core.api.BitAccess;
import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IChiseledTileContainer;
import mod.chiselsandbits.registry.ModBlockEntityTypes;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import mod.chiselsandbits.utils.SingleBlockBlockReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class BlockEntityChiseledBlock extends BlockEntity implements IChiseledTileContainer, IChiseledBlockTileEntity {

    public static final ModelProperty<VoxelBlobStateReference> MP_VBSR = new ModelProperty<>();
    public static final ModelProperty<Integer> MP_PBSI = new ModelProperty<>();

    public BlockEntityChiseledBlock() {
        this(BlockPos.ZERO, ModBlocks.getChiseledDefaultState());
    }

    public BlockEntityChiseledBlock(BlockPos pos, BlockState state) {
        this(ModBlockEntityTypes.CHISELED.get(), pos, state);
    }

    public BlockEntityChiseledBlock(final BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn == null ? ModBlockEntityTypes.CHISELED.get() : tileEntityTypeIn, pos, state);
    }

    public IChiseledTileContainer occlusionState;

    boolean isNormalCube = false;
    int sideState = 0;
    int lightLevel = -1;

    private BlockState state;
    private VoxelBlobStateReference blobStateReference;
    private int primaryBlockStateId;

    private static final ThreadLocal<Integer> LOCAL_LIGHT_LEVEL = new ThreadLocal<>();

    public VoxelBlobStateReference getBlobStateReference() {
        return blobStateReference;
    }

    private void setBlobStateReference(final VoxelBlobStateReference blobStateReference) {
        if (this.blobStateReference == null || !this.blobStateReference.equals(blobStateReference))
            this.blobStateReference = blobStateReference;
    }

    public int getPrimaryBlockStateId() {
        return primaryBlockStateId;
    }

    public void setPrimaryBlockStateId(final int primaryBlockStateId) {
        this.primaryBlockStateId = primaryBlockStateId;

        setLightFromBlock(
                ModUtil.getStateById(primaryBlockStateId)
        );
    }

    public IChiseledTileContainer getTileContainer() {
        if (occlusionState != null) {
            return occlusionState;
        }

        return this;
    }

    @Override
    public boolean isBlobOccluded(
            final VoxelBlob blob) {
        return false;
    }

    @Override
    public void saveData() {
        super.setChanged();
    }

    @Override
    public void sendUpdate() {
        ModUtil.sendUpdate(Objects.requireNonNull(getLevel()), worldPosition);
    }

    @Nonnull
    protected BlockState getState() {
        if (state == null) {
            state = ModBlocks.getChiseledDefaultState();
        }

        return Objects.requireNonNull(state);
    }

    public BlockState getBlockState(
            final Block alternative) {
        final int stateID = getPrimaryBlockStateId();

        final BlockState state = ModUtil.getStateById(stateID);
        if (state != null) {
            return state;
        }

        return alternative.defaultBlockState();
    }

    public void setState(
            final BlockState blockState,
            final VoxelBlobStateReference newRef) {
        final VoxelBlobStateReference originalRef = getBlobStateReference();

        this.state = blockState;

        if (newRef != null && !newRef.equals(originalRef)) {
            final EventBlockBitPostModification bmm = new EventBlockBitPostModification(Objects.requireNonNull(getLevel()), getBlockPos());
            MinecraftForge.EVENT_BUS.post(bmm);
            setBlobStateReference(newRef);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        final CompoundTag compound = new CompoundTag();
        writeChiselData(compound);

        if (compound.size() == 0) {
            return null;
        }

        return new ClientboundBlockEntityDataPacket(worldPosition, ModBlockEntityTypes.CHISELED.get(), compound);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        final CompoundTag compound = new CompoundTag();

        compound.putInt("x", worldPosition.getX());
        compound.putInt("y", worldPosition.getY());
        compound.putInt("z", worldPosition.getZ());

        writeChiselData(compound);

        return compound;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        readChiselData(tag);
    }

    @Override
    public void onDataPacket(
            final Connection net,
            final ClientboundBlockEntityDataPacket pkt) {
        final VoxelBlobStateReference current = getBlobStateReference();
        final int oldLight = lightLevel;
        final boolean changed = readChiselData(pkt.getTag());

        if (level != null && changed) {
            level.setBlocksDirty(worldPosition, level.getBlockState(worldPosition), Blocks.AIR.defaultBlockState());

            // fixes lighting on placement when tile packet arrives.
            if (oldLight != lightLevel) {
                level.getLightEngine().checkBlock(worldPosition);
            }
        }

        if (level.isClientSide()) {
            UndoTracker.getInstance().onNetworkUpdate(current, getBlobStateReference());
        }
    }

    public boolean readChiselData(CompoundTag tag) {
        final NBTBlobConverter converter = new NBTBlobConverter(false, this);
        return converter.readChisleData(tag, VoxelBlob.VERSION_CROSSWORLD);
    }

    public void writeChiselData(CompoundTag tag) {
        new NBTBlobConverter(false, this).writeChiselData(tag, true);
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
        writeChiselData(p_187471_);
    }

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
        readChiselData(p_155245_);
    }

    @NotNull
    @Override
    public CompoundTag writeTileEntityToTag(
            @NotNull final CompoundTag tag,
            final boolean crossWorld) {
        super.saveAdditional(tag);
        new NBTBlobConverter(false, this).writeChiselData(tag, crossWorld);
        tag.putBoolean("cw", crossWorld);
        return tag;
    }

//    @Override
//    public void mirror(
//      @NotNull final Mirror mirrorIn)
//    {
//        switch (mirrorIn)
//        {
//            case FRONT_BACK:
//                setBlob(getBlob().mirror(Axis.X), true);
//                break;
//            case LEFT_RIGHT:
//                setBlob(getBlob().mirror(Axis.Z), true);
//                break;
//            case NONE:
//            default:
//                break;
//        }
//    }
//
//    @Override
//    public void rotate(
//      @NotNull final Rotation rotationIn)
//    {
//        VoxelBlob blob = ModUtil.rotate(getBlob(), Axis.Y, rotationIn);
//        if (blob != null)
//        {
//            setBlob(blob, true);
//        }
//    }

    public void fillWith(
            final BlockState blockType) {
        final int ref = ModUtil.getStateId(blockType);

        sideState = 0xff;
        lightLevel = DeprecationHelper.getLightValue(blockType);
        isNormalCube = ModUtil.isNormalCube(blockType);

        BlockState defaultState = getState();

        // required for placing bits
        if (ref != 0) {
            setPrimaryBlockStateId(ref);
        }

        setState(defaultState, new VoxelBlobStateReference(ModUtil.getStateId(blockType), getPositionRandom(worldPosition)));

        getTileContainer().saveData();
    }

    public static long getPositionRandom(
            final BlockPos pos) {
        if (pos != null && EffectiveSide.get().isClient()) {
            return Mth.getSeed(pos);
        }

        return 0;
    }

    public VoxelBlob getBlob() {
        VoxelBlob vb;
        final VoxelBlobStateReference vbs = getBlobStateReference();

        if (vbs != null) {
            vb = vbs.getVoxelBlob();
        } else {
            vb = new VoxelBlob();
        }

        return vb;
    }

    public void setBlob(
            final VoxelBlob vb) {
        setBlob(vb, true);
    }

    public boolean updateBlob(
            final NBTBlobConverter converter,
            final boolean triggerUpdates) {
        final int oldLV = getLightValue();
        final boolean oldNC = isNormalCube();
        final int oldSides = sideState;

        final VoxelBlobStateReference originalRef = getBlobStateReference();

        VoxelBlobStateReference voxelRef;

        sideState = converter.getSideState();
        final int b = converter.getPrimaryBlockStateID();
        lightLevel = converter.getLightValue();
        isNormalCube = converter.isNormalCube();

        try {
            voxelRef = converter.getVoxelRef(VoxelBlob.VERSION_COMPACT_PALLETED, getPositionRandom(worldPosition));
        } catch (final Exception e) {
            Log.logError("Unable to read blob at " + getBlockPos(), e);
            voxelRef = new VoxelBlobStateReference(0, getPositionRandom(worldPosition));
        }

        setPrimaryBlockStateId(b);
        setBlobStateReference(voxelRef);
        setState(getState(), voxelRef);

        if (getLevel() != null && triggerUpdates) {
            if (oldLV != getLightValue() || oldNC != isNormalCube()) {
                getLevel().getLightEngine().checkBlock(worldPosition);

                // update block state to reflect lighting characteristics
                final BlockState state = getLevel().getBlockState(worldPosition);
                if (state.isRedstoneConductor(new SingleBlockBlockReader(state), BlockPos.ZERO) != isNormalCube && state.getBlock() instanceof BlockChiseled) {
                    getLevel().setBlockAndUpdate(worldPosition, state.setValue(BlockChiseled.FULL_BLOCK, isNormalCube));
                }
            }

            if (oldSides != sideState) {
                Objects.requireNonNull(level).updateNeighborsAt(worldPosition, level.getBlockState(worldPosition).getBlock());
            }
        }

        return voxelRef == null || !voxelRef.equals(originalRef);
    }

    public void setBlob(VoxelBlob vb, boolean triggerUpdates) {
        final int olv = getLightValue();
        final boolean oldNC = isNormalCube();

        final VoxelStats common = vb.getVoxelStats();
        final float light = common.blockLight;
        final boolean nc = common.isNormalBlock;
        final int lv = Math.max(0, Math.min(15, (int) (light * 15)));

        // are most of the bits in the center solid?
        final int sideFlags = vb.getSideFlags(5, 11, 4 * 4);

        if (getLevel() == null) {
            if (common.mostCommonState == 0) {
                common.mostCommonState = getPrimaryBlockStateId();
            }

            sideState = sideFlags;
            lightLevel = lv;
            isNormalCube = nc;

            setBlobStateReference(new VoxelBlobStateReference(vb.blobToBytes(VoxelBlob.VERSION_COMPACT_PALLETED), getPositionRandom(worldPosition)));
            setPrimaryBlockStateId(common.mostCommonState);
            setState(getState(), getBlobStateReference());
            return;
        }

        // 如果是完整方块
        if (common.isFullBlock) {
            setBlobStateReference(new VoxelBlobStateReference(common.mostCommonState, getPositionRandom(worldPosition)));
            setState(getState(), getBlobStateReference());
            // 将完整的雕刻方块变回普通方块
            final BlockState newState = ModUtil.getStateById(common.mostCommonState);
            if (ChiselsAndBits.getConfig().getServer().canRevertToBlock(newState)) {
                if (!MinecraftForge.EVENT_BUS.post(new EventFullBlockRestoration(Objects.requireNonNull(level), worldPosition, newState))) {
                    level.setBlock(worldPosition, newState, triggerUpdates ? 3 : 0);
                }
            }
        } else if (common.mostCommonState != 0) {
            sideState = sideFlags;
            lightLevel = lv;
            isNormalCube = nc;

            setBlobStateReference(new VoxelBlobStateReference(vb.blobToBytes(VoxelBlob.VERSION_COMPACT_PALLETED), getPositionRandom(worldPosition)));
            setPrimaryBlockStateId(common.mostCommonState);
            setState(getState(), getBlobStateReference());

            getTileContainer().saveData();
            getTileContainer().sendUpdate();

            // since its possible for bits to occlude parts.. update every time.
            final Block blk = Objects.requireNonNull(level).getBlockState(worldPosition).getBlock();
            // worldObj.notifyBlockOfStateChange( pos, blk, false );

            if (triggerUpdates) {
                level.updateNeighborsAt(worldPosition, blk);
            }
        } else {
            setBlobStateReference(new VoxelBlobStateReference(0, getPositionRandom(worldPosition)));
            setState(getState(), getBlobStateReference());

            ModUtil.removeChiseledBlock(Objects.requireNonNull(level), worldPosition);
        }

        if (olv != lv || oldNC != nc) {
            Objects.requireNonNull(level).getLightEngine().checkBlock(worldPosition);

            // update block state to reflect lighting characteristics
            final BlockState state = level.getBlockState(worldPosition);
            if (state.isRedstoneConductor(new SingleBlockBlockReader(state), BlockPos.ZERO) != isNormalCube && state.getBlock() instanceof BlockChiseled) {
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockChiseled.FULL_BLOCK, isNormalCube));
            }
        }
    }

    static private class ItemStackGeneratedCache {
        public ItemStackGeneratedCache(
                final ItemStack itemstack,
                final VoxelBlobStateReference blobStateReference,
                final int rotations2) {
            out = itemstack == null ? null : itemstack.copy();
            ref = blobStateReference;
            rotations = rotations2;
        }

        final ItemStack out;
        final VoxelBlobStateReference ref;
        final int rotations;

        public ItemStack getItemStack() {
            return out == null ? null : out.copy();
        }
    }

    /**
     * prevent mods that constantly ask for pick block from killing the client... ( looking at you waila )
     **/
    private ItemStackGeneratedCache pickCache = null;

    public ItemStack getItemStack(
            final Player player) {
        final ItemStackGeneratedCache cache = pickCache;

        if (player != null) {
            Direction placingFace = ModUtil.getPlaceFace(player);
            final int rotations = ModUtil.getRotationIndex(placingFace);

            if (cache != null && cache.rotations == rotations && cache.ref == getBlobStateReference() && cache.out != null) {
                return cache.getItemStack();
            }

            VoxelBlob vb = getBlob();

            int countDown = rotations;
            while (countDown > 0) {
                countDown--;
                placingFace = placingFace.getCounterClockWise();
                vb = vb.spin(Axis.Y);
            }

            final BitAccess ba = new BitAccess(null, null, vb, VoxelBlob.NULL_BLOB);
            final ItemStack itemstack = ba.getBitsAsItem(placingFace, ItemType.CHISLED_BLOCK, false);

            pickCache = new ItemStackGeneratedCache(itemstack, getBlobStateReference(), rotations);
            return itemstack;
        } else {
            if (cache != null && cache.rotations == 0 && cache.ref == getBlobStateReference()) {
                return cache.getItemStack();
            }

            final BitAccess ba = new BitAccess(null, null, getBlob(), VoxelBlob.NULL_BLOB);
            final ItemStack itemstack = ba.getBitsAsItem(null, ItemType.CHISLED_BLOCK, false);

            pickCache = new ItemStackGeneratedCache(itemstack, getBlobStateReference(), 0);
            return itemstack;
        }
    }

    public boolean isNormalCube() {
        return isNormalCube;
    }

    public boolean isSideSolid(
            final Direction side) {
        return (sideState & 1 << side.ordinal()) != 0;
    }

    public boolean isSideOpaque(
            final Direction side) {
        if (this.getLevel() != null && this.getLevel().isClientSide) {
            return isInnerSideOpaque(side);
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isInnerSideOpaque(
            final Direction side) {
        final int sideFlags = ChiseledBlockSmartModel.getSides(this);
        return (sideFlags & 1 << side.ordinal()) != 0;
    }

    public void completeEditOperation(
            final VoxelBlob vb) {
        final VoxelBlobStateReference before = getBlobStateReference();
        setBlob(vb);
        final VoxelBlobStateReference after = getBlobStateReference();

        if (level != null) {
            level.setBlocksDirty(worldPosition, level.getBlockState(worldPosition), Blocks.AIR.defaultBlockState());
        }

        UndoTracker.getInstance().add(getLevel(), getBlockPos(), before, after);
    }

    //TODO: Figure this out.
    public void rotateBlock() {
        final VoxelBlob occluded = new VoxelBlob();

        VoxelBlob postRotation = getBlob();
        int maxRotations = 4;
        while (--maxRotations > 0) {
            postRotation = postRotation.spin(Axis.Y);

            if (occluded.canMerge(postRotation)) {
                setBlob(postRotation);
                return;
            }
        }
    }

    public boolean canMerge(
            final VoxelBlob voxelBlob) {
        final VoxelBlob vb = getBlob();
        final IChiseledTileContainer occ = getTileContainer();

        return vb.canMerge(voxelBlob) && !occ.isBlobOccluded(voxelBlob);
    }

    @NotNull
    @Override
    public Collection<AABB> getBoxes(
            @NotNull final BoxType type) {
        final VoxelBlobStateReference ref = getBlobStateReference();

        if (ref != null) {
            return ref.getBoxes(type);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        final BlockPos p = getBlockPos();
        return new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1);
    }

    public void setNormalCube(
            final boolean b) {
        isNormalCube = b;
    }

    public static void setLightFromBlock(
            final BlockState defaultState) {
        if (defaultState == null) {
            LOCAL_LIGHT_LEVEL.remove();
        } else {
            LOCAL_LIGHT_LEVEL.set(DeprecationHelper.getLightValue(defaultState));
        }
    }

    public int getLightValue() {
        // first time requested, pull from local, or default to 0
        if (lightLevel < 0) {
            final Integer tmp = LOCAL_LIGHT_LEVEL.get();
            lightLevel = tmp == null ? 0 : tmp;
        }

        return lightLevel;
    }

    @NotNull
    @Override
    public IBitAccess getBitAccess() {
        VoxelBlob mask = VoxelBlob.NULL_BLOB;

        if (level != null) {
            mask = new VoxelBlob();
        }

        return new BitAccess(level, worldPosition, getBlob(), mask);
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(MP_PBSI, getPrimaryBlockStateId())
                .with(MP_VBSR, getBlobStateReference())
                .build();
    }
}
