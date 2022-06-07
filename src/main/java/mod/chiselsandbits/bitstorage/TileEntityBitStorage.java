package mod.chiselsandbits.bitstorage;

import javax.annotation.Nonnull;

import mod.chiselsandbits.api.IBitBag;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.registry.ModBlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TileEntityBitStorage extends BlockEntity implements IItemHandler, IFluidHandler
{

    public LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> this);
    public LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> this);

	public static final int MAX_CONTENTS = 4096;

	// best conversion...
	// 125mb = 512bits
	public static final int MB_PER_BIT_CONVERSION = 125;
	public static final int BITS_PER_MB_CONVERSION = 512;

	private BlockState state = null;
	private Fluid myFluid = null;
	private int   bits    = 0;

	private int oldLV = -1;

    public TileEntityBitStorage() {
        this(BlockPos.ZERO, ModBlocks.BIT_STORAGE_BLOCK.get().defaultBlockState());
    }

    public TileEntityBitStorage(BlockPos pos, BlockState state)
    {
        super(ModBlockEntityTypes.BIT_STORAGE.get(), pos, state);
    }

    @Override
	public void onDataPacket(
			final Connection net,
			final ClientboundBlockEntityDataPacket pkt )
	{
		load( pkt.getTag() );
	}

	@Override
	public CompoundTag getUpdateTag()
	{
		final CompoundTag nbttagcompound = new CompoundTag();
        saveAdditional(nbttagcompound);
		return nbttagcompound;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		final CompoundTag t = new CompoundTag();
        saveAdditional(t);
		return new ClientboundBlockEntityDataPacket( getBlockPos(), ModBlockEntityTypes.BIT_STORAGE.get(), t );
	}

    @Override
    public void load(CompoundTag p_155245_) {
        super.load( p_155245_);
        final String fluid = p_155245_.getString( "fluid" );

        if (fluid.equals( "" ))
        {
            final int rawState = p_155245_.getInt("blockstate");
            if (rawState != -1)
            {
                this.state = ModUtil.getStateById(rawState);
            }
            else
            {
                this.state = null;
            }
        }
        else
        {
            myFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluid));
        }

        bits = p_155245_.getInt( "bits" );
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
        p_187471_.putString( "fluid", myFluid == null ? "" : Objects.requireNonNull(myFluid.getRegistryName()).toString() );
        p_187471_.putInt("blockstate", myFluid != null || state == null ? -1 : ModUtil.getStateId(state));
        p_187471_.putInt( "bits", bits );
    }

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> LazyOptional<T> getCapability(
			final Capability<T> capability,
			final Direction facing )
	{
		if ( capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return itemHandler.cast();
		}

		if ( capability == net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
		{
		    return fluidHandler.cast();
		}

		return super.getCapability( capability, facing );
	}

	@Override
	public int getSlots()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(
			final int slot )
	{
		if ( bits > 0 && slot == 0 && (myFluid != null || state != null) )
		{
		    if (myFluid != null)
		    {
                return getFluidBitStack( myFluid, bits );
            }
		    else
            {
                return getBlockBitStack( state, bits);
            }
		}

		return ModUtil.getEmptyStack();
	}

	public @Nonnull ItemStack getFluidBitStack(
			final Fluid liquid,
			final int amount )
	{
		if (liquid == null)
		{
			return ModUtil.getEmptyStack();
		}

        return ItemChiseledBit.createStack( ModUtil.getStateId( liquid.defaultFluidState().createLegacyBlock() ), amount, false );
	}

    public @Nonnull ItemStack getBlockBitStack(
      final BlockState blockState,
      final int amount )
    {
        if ( blockState == null )
        {
            return ModUtil.getEmptyStack();
        }

        return ItemChiseledBit.createStack( ModUtil.getStateId( blockState), amount, false );
    }


    @Override
	public @Nonnull ItemStack insertItem(
			final int slot,
			final ItemStack stack,
			final boolean simulate )
	{
		if ( !ModUtil.isEmpty( stack ) && stack.getItem() instanceof ItemChiseledBit )
		{
			final int state = ItemChiseledBit.getStackState( stack );
			final BlockState blk = ModUtil.getStateById( state );

            final ItemStack fluidInsertion = attemptFluidBitStackInsertion(stack, simulate, blk);
            if (fluidInsertion != stack)
            {
                return fluidInsertion;
            }

            return attemptSolidBitStackInsertion(stack, simulate, blk);
        } else if (!ModUtil.isEmpty(stack) && BlockBitInfo.canChisel(stack) && myFluid == null) {
		    final BlockState stackState = ModUtil.getStateFromItem(stack);
		    if (stackState.getBlock() != Blocks.AIR) {
                if (this.state == null)
                {
                    this.state = stackState;
                    this.bits = 4096;
                } else if (ModUtil.getStateId(this.state) == ModUtil.getStateId(stackState))
                {

                }
            }
        }
		return stack;
	}

    @NotNull
    private ItemStack attemptFluidBitStackInsertion(final ItemStack stack, final boolean simulate, final BlockState blk)
    {
        Fluid f = null;
        for ( final Fluid fl : ForgeRegistries.FLUIDS )
        {
            if ( fl.defaultFluidState().createLegacyBlock().getBlock() == blk.getBlock() )
            {
                f = fl;
                break;
            }
        }

        if ( f == null )
        {
            return stack;
        }

        final ItemStack bitItem = getFluidBitStack( myFluid, bits );
        final boolean canInsert = ModUtil.isEmpty( bitItem ) || ItemStack.tagMatches( bitItem, stack) && bitItem.getItem() == stack.getItem() || state == null;

        if ( canInsert )
        {
            final int merged = bits + ModUtil.getStackSize(stack);
            final int amount = Math.min( merged, MAX_CONTENTS );

            if ( !simulate)
            {
                final Fluid oldFluid = myFluid;
                final BlockState oldState = state;
                final int oldBits = bits;

                myFluid = f;
                state = null;
                bits = amount;

                if ( bits != oldBits || myFluid != oldFluid || oldState != null)
                {
                    saveAndUpdate();
                }
            }

            if ( amount < merged )
            {
                final ItemStack out = ModUtil.copy(stack);
                ModUtil.setStackSize( out, merged - amount );
                return out;
            }

            return ModUtil.getEmptyStack();
        }
        return stack;
    }

    @NotNull
    private ItemStack attemptSolidBitStackInsertion(final ItemStack stack, final boolean simulate, final BlockState blk)
    {
        Fluid f = null;
        for ( final Fluid fl : ForgeRegistries.FLUIDS )
        {
            if ( fl.defaultFluidState().createLegacyBlock().getBlock() == blk.getBlock() )
            {
                f = fl;
                break;
            }
        }

        if ( f != null )
        {
            return stack;
        }

        final ItemStack bitItem = getBlockBitStack( blk, bits );
        final boolean canInsert = ModUtil.isEmpty( bitItem ) || ItemStack.tagMatches( bitItem, stack) && bitItem.getItem() == stack.getItem();

        if ( canInsert )
        {
            final int merged = bits + ModUtil.getStackSize(stack);
            final int amount = Math.min( merged, MAX_CONTENTS );

            if ( !simulate)
            {
                final Fluid oldFluid = myFluid;
                final BlockState oldBlockState = this.state;
                final int oldBits = bits;

                myFluid = null;
                state = blk;
                bits = amount;

                if ( bits != oldBits || state != oldBlockState || oldFluid != null )
                {
                    saveAndUpdate();
                }
            }

            if ( amount < merged )
            {
                final ItemStack out = ModUtil.copy(stack);
                ModUtil.setStackSize( out, merged - amount );
                return out;
            }

            return ModUtil.getEmptyStack();
        }
        return stack;
    }

    private void saveAndUpdate()
	{
	    if (level == null || getLevel() == null)
	        return;

		setChanged();
		ModUtil.sendUpdate( level, getBlockPos() );

		final int lv = getLightValue();
		if ( oldLV != lv )
		{
			getLevel().getLightEngine().checkBlock( getBlockPos() );
			oldLV = lv;
		}
	}

	/**
	 * Dosn't limit to stack size...
	 *
	 * @param slot
	 * @param amount
	 * @param simulate
	 * @return
	 */
	public @Nonnull ItemStack extractBits(
			final int slot,
			final int amount,
			final boolean simulate )
	{
		final ItemStack contents = getStackInSlot( slot );

		if ( !contents.isEmpty() && amount > 0)
		{
			// how many to extract?
			ModUtil.setStackSize( contents, Math.min( amount, ModUtil.getStackSize( contents ) ) );

			// modulate?
			if ( !simulate )
			{
				final int oldBits = bits;

				bits -= ModUtil.getStackSize( contents );
				if ( bits <= 0 )
				{
				    bits = 0;
				    state = null;
					myFluid = null;
				}

				if ( bits != oldBits )
				{
					saveAndUpdate();
				}
			}

			return contents;
		}

		return ModUtil.getEmptyStack();
	}

	@Override
	public ItemStack extractItem(
			final int slot,
			final int amount,
			final boolean simulate )
	{
		return extractBits( slot, Math.min( amount, ModItems.ITEM_BLOCK_BIT.get().getItemStackLimit(ItemStack.EMPTY) ), simulate );
	}

	public FluidStack getAccessableFluid()
	{
	    if (myFluid == null && state != null)
	        return FluidStack.EMPTY;

		int mb = ( bits - bits % BITS_PER_MB_CONVERSION ) / BITS_PER_MB_CONVERSION;
		mb *= MB_PER_BIT_CONVERSION;

		if ( mb > 0 && myFluid != null )
		{
			return new FluidStack( myFluid, mb );
		}

		return FluidStack.EMPTY;
	}

	FluidStack getBitsAsFluidStack()
	{
        if (myFluid == null && state != null)
            return FluidStack.EMPTY;

		if ( bits > 0 && myFluid != null )
		{
			return new FluidStack( myFluid, bits );
		}

		return null;
	}

	public int getLightValue()
	{
	    final BlockState workingState = myFluid == null ? state : myFluid.defaultFluidState().createLegacyBlock();
		if (workingState == null)
        {
            return 0;
        }

        return DeprecationHelper.getLightValue( workingState );
	}

	boolean extractBits(
			final Player playerIn,
			final double hitX,
			final double hitY,
			final double hitZ,
			final BlockPos pos )
	{
		if ( !playerIn.isShiftKeyDown() )
		{
			final ItemStack is = extractItem( 0, 64, false );
			if ( !is.isEmpty() )
			{
				ChiselsAndBits.getApi().giveBitToPlayer( playerIn, is, new Vec3( hitX + pos.getX(), hitY + pos.getY(), hitZ + pos.getZ() ) );
			}
			return true;
		}

		return false;
	}

	boolean addAllPossibleBits(
			final Player playerIn )
	{
		if ( playerIn.isShiftKeyDown() )
		{
			boolean change = false;
			for ( int x = 0; x < playerIn.getInventory().getContainerSize(); x++ )
			{
				final ItemStack stackInSlot = ModUtil.nonNull( playerIn.getInventory().getItem( x ) );
				if ( ChiselsAndBits.getApi().getItemType( stackInSlot ) == ItemType.CHISLED_BIT )
				{
					playerIn.getInventory().setItem( x, insertItem( 0, stackInSlot, false ) );
					change = true;
				}

				if ( ChiselsAndBits.getApi().getItemType( stackInSlot ) == ItemType.BIT_BAG )
				{
					final IBitBag bag = ChiselsAndBits.getApi().getBitbag( stackInSlot );

					if ( bag == null )
					{
						continue;
					}

					for ( int y = 0; y < bag.getSlots(); ++y )
					{
						bag.insertItem( y, insertItem( 0, bag.extractItem( y, bag.getSlotLimit( y ), false ), false ), false );
						change = true;
					}
				}
			}

			if ( change )
			{
				playerIn.getInventory().setChanged();
			}

			return change;
		}

		return false;
	}

	boolean addHeldBits(
			final @Nonnull ItemStack current,
			final Player playerIn )
	{
		if ( playerIn.isShiftKeyDown() || this.bits == 0 )
		{
			if ( ChiselsAndBits.getApi().getItemType( current ) == ItemType.CHISLED_BIT || BlockBitInfo.canChisel(current) )
			{
			    final ItemStack resultStack = insertItem( 0, current, false );
                if (!playerIn.isCreative()) {
                    playerIn.getInventory().setItem( playerIn.getInventory().selected, resultStack );
                    playerIn.getInventory().setChanged();
                }
				return true;
			}
		}

		return false;
	}

    @Override
    public int getTanks()
    {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(final int tank)
    {
        return getAccessableFluid();
    }

    @Override
    public int getTankCapacity(final int tank)
    {
        return MAX_CONTENTS;
    }

    @Override
    public boolean isFluidValid(final int tank, @NotNull final FluidStack stack)
    {
        if (getAccessableFluid().isEmpty() && state == null)
            return true;

        if (state != null)
            return false;

        return Objects.equals(getAccessableFluid().getFluid().getRegistryName(), stack.getFluid().getRegistryName());
    }

    @Override
    public int fill(final FluidStack resource, final FluidAction action)
    {
        if ( resource == null || state != null )
        {
            return 0;
        }

        final int possibleAmount = resource.getAmount() - resource.getAmount() % TileEntityBitStorage.MB_PER_BIT_CONVERSION;

        if ( possibleAmount > 0 )
        {
            final int bitCount = possibleAmount * TileEntityBitStorage.BITS_PER_MB_CONVERSION / TileEntityBitStorage.MB_PER_BIT_CONVERSION;
            final ItemStack bitItems = getFluidBitStack( resource.getFluid(), bitCount );
            final ItemStack leftOver = insertItem( 0, bitItems, action.simulate() );

            if ( ModUtil.isEmpty( leftOver ) )
            {
                return possibleAmount;
            }

            int mbUsedUp = ModUtil.getStackSize( leftOver );

            // round up...
            mbUsedUp *= TileEntityBitStorage.MB_PER_BIT_CONVERSION;
            mbUsedUp += TileEntityBitStorage.BITS_PER_MB_CONVERSION - 1;
            mbUsedUp /= TileEntityBitStorage.BITS_PER_MB_CONVERSION;

            return resource.getAmount() - mbUsedUp;
        }

        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(final FluidStack resource, final FluidAction action)
    {
        if ( resource == null || state != null )
        {
            return FluidStack.EMPTY;
        }

        final FluidStack a = getAccessableFluid();

        if ( a != null && resource.containsFluid( a ) ) // right type of fluid.
        {
            final int aboutHowMuch = resource.getAmount();

            final int mbThatCanBeRemoved = Math.min( a.getAmount(), aboutHowMuch - aboutHowMuch % TileEntityBitStorage.MB_PER_BIT_CONVERSION );
            if ( mbThatCanBeRemoved > 0 )
            {
                a.setAmount(mbThatCanBeRemoved);

                if ( action.execute() )
                {
                    final int bitCount = mbThatCanBeRemoved * TileEntityBitStorage.BITS_PER_MB_CONVERSION / TileEntityBitStorage.MB_PER_BIT_CONVERSION;
                    extractBits( 0, bitCount, false );
                }

                return a;
            }
        }

        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(final int maxDrain, final FluidAction action)
    {
        if ( maxDrain <= 0 || state != null )
        {
            return FluidStack.EMPTY;
        }

        final FluidStack a = getAccessableFluid();

        if ( a != null ) // right type of fluid.
        {
            final int aboutHowMuch = maxDrain;

            final int mbThatCanBeRemoved = Math.min( a.getAmount(), aboutHowMuch - aboutHowMuch % TileEntityBitStorage.MB_PER_BIT_CONVERSION );
            if ( mbThatCanBeRemoved > 0 )
            {
                a.setAmount(mbThatCanBeRemoved);

                if ( action.execute() )
                {
                    final int bitCount = mbThatCanBeRemoved * TileEntityBitStorage.BITS_PER_MB_CONVERSION / TileEntityBitStorage.MB_PER_BIT_CONVERSION;
                    extractBits( 0, bitCount, false );
                }

                return a;
            }
        }

        return FluidStack.EMPTY;
    }

    @Override
	public int getSlotLimit(
			final int slot )
	{
		return TileEntityBitStorage.BITS_PER_MB_CONVERSION;
	}

    @Override
    public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
    {
        return !ModUtil.isEmpty( stack ) && (stack.getItem() instanceof ItemChiseledBit || BlockBitInfo.canChisel(stack));
    }

    public BlockState getState()
    {
        return state;
    }

    public Fluid getMyFluid()
    {
        return myFluid;
    }

    public int getBits()
    {
        return bits;
    }
}