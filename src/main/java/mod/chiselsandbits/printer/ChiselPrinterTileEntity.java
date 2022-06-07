package mod.chiselsandbits.printer;

import mod.chiselsandbits.bitstorage.TileEntityBitStorage;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChiselPrinterTileEntity extends BlockEntity implements MenuProvider, BlockEntityTicker<ChiselPrinterTileEntity> {
    private final LazyOptional<EmptyHandler> empty_handler = LazyOptional.of(NonNullLazy.of(EmptyHandler::new));
    private final LazyOptional<ItemStackHandler> tool_handler   = LazyOptional.of(NonNullLazy.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
        {
            return stack.getItem() instanceof ItemChisel;
        }

        @Override
        public int getSlotLimit(final int slot)
        {
            return 1;
        }
    }));
    private final LazyOptional<ItemStackHandler> pattern_handler   = LazyOptional.of(NonNullLazy.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
        {
            return stack.getItem() instanceof IPatternItem;
        }

        @Override
        public int getSlotLimit(final int slot)
        {
            return 1;
        }

        @Override
        protected void onContentsChanged(final int slot)
        {
            currentRealisedWorkingStack.setValue(ItemStack.EMPTY);
        }
    }));
    private final LazyOptional<ItemStackHandler> result_handler = LazyOptional.of(NonNullLazy.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
        {
            return true;
        }
    }));

    protected final ContainerData stationData = new ContainerData() {
        public int get(int index) {
            if (index == 0)
            {
                return ChiselPrinterTileEntity.this.progress;
            }
            return 0;
        }

        public void set(int index, int value) {
            if (index == 0)
            {
                ChiselPrinterTileEntity.this.progress = value;
            }
        }

        public int getCount() {
            return 1;
        }
    };

    private int progress = 0;
    private long lastTickTime = 0L;
    private final MutableObject<ItemStack> currentRealisedWorkingStack = new MutableObject<>(ItemStack.EMPTY);

    public ChiselPrinterTileEntity() {
        this(BlockPos.ZERO, ModBlocks.CHISEL_PRINTER_BLOCK.get().defaultBlockState());
    }

    public ChiselPrinterTileEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntityTypes.CHISEL_PRINTER.get(), pos, state);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull final Capability<T> cap, @Nullable final Direction side)
    {
        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return super.getCapability(cap, side);

        if (side != null)
        {
            switch(side){
                case DOWN:
                    return result_handler.cast();
                case UP:
                case NORTH:
                case SOUTH:
                case WEST:
                case EAST:
                    return tool_handler.cast();
            }
        }

        return empty_handler.cast();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return super.getCapability(cap);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = super.serializeNBT();

        tool_handler.ifPresent(h -> compound.put("tool", h.serializeNBT()));
        pattern_handler.ifPresent(h -> compound.put("pattern", h.serializeNBT()));
        result_handler.ifPresent(h -> compound.put("result", h.serializeNBT()));

        compound.putInt("progress", progress);

        return compound;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox();
    }

    @Override
    public void requestModelDataUpdate() {
        super.requestModelDataUpdate();
    }

    @NotNull
    @Override
    public IModelData getModelData() {
        return super.getModelData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);

        tool_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("tool")));
        pattern_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("pattern")));
        result_handler.ifPresent(h -> h.deserializeNBT(nbt.getCompound("result")));

        progress = nbt.getInt("progress");
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return serializeNBT();
    }

    public IItemHandlerModifiable getPatternHandler() {
        return pattern_handler.orElseThrow(() -> new IllegalStateException("Missing empty handler."));
    }

    public IItemHandlerModifiable getToolHandler() {
        return tool_handler.orElseThrow(() -> new IllegalStateException("Missing tool handler."));
    }

    public IItemHandlerModifiable getResultHandler() {
        return result_handler.orElseThrow(() -> new IllegalStateException("Missing result handler."));
    }

    public boolean hasPatternStack() {
        return !getPatternStack().isEmpty();
    }

    public boolean hasToolStack() {
        return !getToolStack().isEmpty();
    }

    public boolean hasRealisedStack() {
        return !getRealisedStack().isEmpty();
    }

    public boolean hasOutputStack() {
        return !getOutputStack().isEmpty();
    }

    public boolean canMergeOutputs() {
        if (!hasOutputStack())
            return true;

        if (!hasRealisedStack())
            return false;

        return ItemHandlerHelper.canItemStacksStack(getOutputStack(), getRealisedStack());
    }

    public boolean canWork() {
        return hasPatternStack() && hasToolStack() && canMergeOutputs();
    }

    public boolean couldWork() {
        return hasPatternStack() && hasToolStack();
    }

    public boolean hasMergeableInput() {
        if (!hasOutputStack())
            return true;

        return ItemHandlerHelper.canItemStacksStack(getOutputStack(), realisePattern(false));
    }

    public ItemStack getPatternStack() {
        return pattern_handler.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
    }

    public ItemStack getToolStack() {
        return tool_handler.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
    }

    public ItemStack getRealisedStack() {
        ItemStack realisedStack = currentRealisedWorkingStack.getValue();
        if (realisedStack.isEmpty()) {
            realisedStack = realisePattern(false);
            currentRealisedWorkingStack.setValue(realisedStack);
        }

        return realisedStack;
    }

    public ItemStack getOutputStack() {
        return result_handler.map(h -> h.getStackInSlot(0)).orElse(ItemStack.EMPTY);
    }

    private ItemStack realisePattern(final boolean consumeResources) {
        if (!hasPatternStack())
            return ItemStack.EMPTY;

        final ItemStack stack = getPatternStack();
        if (!(stack.getItem() instanceof IPatternItem))
            return ItemStack.EMPTY;

        final IPatternItem patternItem = (IPatternItem) stack.getItem();
        final ItemStack realisedPattern = patternItem.getPatternedItem(stack.copy(), true);
        if (realisedPattern == null || realisedPattern.isEmpty())
            return ItemStack.EMPTY;

        BlockState firstState = getPrimaryBlockState();
        BlockState secondState = getSecondaryBlockState();
        BlockState thirdState = getTertiaryBlockState();

        if (firstState == null)
            firstState = Blocks.AIR.defaultBlockState();

        if (secondState == null)
            secondState = Blocks.AIR.defaultBlockState();

        if (thirdState == null)
            thirdState = Blocks.AIR.defaultBlockState();

        if ((!BlockBitInfo.isSupported(firstState) && !firstState.isAir())
                || (!BlockBitInfo.isSupported(secondState) && !secondState.isAir())
                || (!BlockBitInfo.isSupported(thirdState) && !thirdState.isAir())
        )
            return ItemStack.EMPTY;

        final NBTBlobConverter c = new NBTBlobConverter();
        final CompoundTag tag = ModUtil.getSubCompound(realisedPattern, ModUtil.NBT_BLOCKENTITYTAG, false).copy();
        c.readChisleData(tag, VoxelBlob.VERSION_ANY);
        VoxelBlob blob = c.getBlob();

        final VoxelBlob.PartialFillResult fillResult = blob.clearAllBut(
          ModUtil.getStateId(firstState),
          ModUtil.getStateId(secondState),
          ModUtil.getStateId(thirdState)
        );

        if (fillResult.getFirstStateUsedCount() == 0 && fillResult.getSecondStateUsedCount() == 0 && fillResult.getThirdStateUsedCount() == 0)
            return ItemStack.EMPTY;

        if (fillResult.getFirstStateUsedCount() > getAvailablePrimaryBlockState() ||
                fillResult.getSecondStateUsedCount() > getAvailableSecondaryBlockState() ||
                fillResult.getThirdStateUsedCount() > getAvailableTertiaryBlockState())
            return ItemStack.EMPTY;

        if (consumeResources) {
            drainPrimaryStorage(fillResult.getFirstStateUsedCount());
            drainSecondaryStorage(fillResult.getSecondStateUsedCount());
            drainTertiaryStorage(fillResult.getThirdStateUsedCount());
        }

        c.setBlob(blob);

        final BlockState state = c.getPrimaryBlockState();
        final ItemStack itemstack = new ItemStack(ModBlocks.convertGivenStateToChiseledBlock(state), 1 );
        c.writeChisleData(tag, false);

        itemstack.addTagElement( ModUtil.NBT_BLOCKENTITYTAG, tag );
        return itemstack;
    }



    private void damageChisel() {
        if (getLevel() != null && !getLevel().isClientSide())
        {
            getToolStack().hurt(1, getLevel().getRandom(), null);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, @NotNull final Inventory playerInventory, @NotNull final Player playerEntity)
    {
        return new ChiselPrinterContainer(
          containerId,
          playerInventory,
          getPatternHandler(),
          getToolHandler(),
          getResultHandler(),
          stationData);
    }

    @Override
    public Component getDisplayName()
    {
        return LocalStrings.ChiselStationName.getLocalText();
    }

    public int getAvailablePrimaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiselPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise();

        return getStorageContents(targetedFacing);
    }

    public int getAvailableSecondaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiselPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise().getClockWise();

        return getStorageContents(targetedFacing);
    }

    public int getAvailableTertiaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiselPrinterBlock.FACING);
        final Direction targetedFacing = facing.getCounterClockWise();

        return getStorageContents(targetedFacing);
    }

    private int getStorageContents(final Direction targetedFacing)
    {
        final BlockEntity targetedTileEntity = this.getLevel().getBlockEntity(this.getBlockPos().relative(targetedFacing));
        if (targetedTileEntity instanceof TileEntityBitStorage)
        {
            final TileEntityBitStorage storage = (TileEntityBitStorage) targetedTileEntity;
            return storage.getBits();
        }

        return 0;
    }

    public BlockState getPrimaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiselPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise();

        return getStorage(targetedFacing);
    }

    public BlockState getSecondaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiselPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise().getClockWise();

        return getStorage(targetedFacing);
    }

    public BlockState getTertiaryBlockState() {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiselPrinterBlock.FACING);
        final Direction targetedFacing = facing.getCounterClockWise();

        return getStorage(targetedFacing);
    }

    private BlockState getStorage(final Direction targetedFacing)
    {
        final BlockEntity targetedTileEntity = this.getLevel().getBlockEntity(this.getBlockPos().relative(targetedFacing));
        if (targetedTileEntity instanceof TileEntityBitStorage)
        {
            final TileEntityBitStorage storage = (TileEntityBitStorage) targetedTileEntity;
            return storage.getState();
        }

        return Blocks.AIR.defaultBlockState();
    }

    public void drainPrimaryStorage(final int amount) {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiselPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise();

        drainStorage(amount, targetedFacing);
    }

    public void drainSecondaryStorage(final int amount) {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiselPrinterBlock.FACING);
        final Direction targetedFacing = facing.getClockWise().getClockWise();

        drainStorage(amount, targetedFacing);
    }

    public void drainTertiaryStorage(final int amount) {
        final Direction facing = Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()).getValue(ChiselPrinterBlock.FACING);
        final Direction targetedFacing = facing.getCounterClockWise();

        drainStorage(amount, targetedFacing);
    }

    private void drainStorage(final int amount, final Direction targetedFacing)
    {
        final BlockEntity targetedTileEntity = this.getLevel().getBlockEntity(this.getBlockPos().relative(targetedFacing));
        if (targetedTileEntity instanceof TileEntityBitStorage)
        {
            final TileEntityBitStorage storage = (TileEntityBitStorage) targetedTileEntity;
            storage.extractBits(0, amount, false);
        }
    }

    @Override
    public void tick(Level p_155253_, BlockPos p_155254_, BlockState p_155255_, ChiselPrinterTileEntity p_155256_) {
        if (getLevel() == null || lastTickTime == getLevel().getGameTime() || getLevel().isClientSide()) {
            return;
        }

        this.lastTickTime = getLevel().getGameTime();

        if (couldWork()) {
            if (canWork()) {
                progress++;
                if (progress >= 100) {
                    result_handler.ifPresent(h -> h.insertItem(0, realisePattern(true), false));
                    currentRealisedWorkingStack.setValue(ItemStack.EMPTY);
                    progress = 0;
                    damageChisel();
                }
                setChanged();
            }
        } else if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }
}