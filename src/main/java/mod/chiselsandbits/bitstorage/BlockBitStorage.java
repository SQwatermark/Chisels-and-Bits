package mod.chiselsandbits.bitstorage;

import com.google.common.collect.Lists;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.ExceptionNoTileEntity;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockBitStorage extends Block implements EntityBlock
{

    private static final Property<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public BlockBitStorage(BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new BlockEntityBitStorage(p_153215_, p_153216_);
    }

    public BlockEntityBitStorage getTileEntity(
      final BlockEntity te) throws ExceptionNoTileEntity
    {
        if (te instanceof BlockEntityBitStorage)
        {
            return (BlockEntityBitStorage) te;
        }
        throw new ExceptionNoTileEntity();
    }

    public BlockEntityBitStorage getTileEntity(
      final BlockGetter world,
      final BlockPos pos) throws ExceptionNoTileEntity
    {
        return getTileEntity(world.getBlockEntity(pos));
    }

    @Override
    public InteractionResult use(
      final BlockState state, final Level worldIn, final BlockPos pos, final Player player, final InteractionHand handIn, final BlockHitResult hit)
    {
        try
        {
            final BlockEntityBitStorage tank = getTileEntity(worldIn, pos);
            final ItemStack current = ModUtil.nonNull(player.getInventory().getSelected());

            if (!ModUtil.isEmpty(current))
            {
                final IFluidHandler wrappedTank = tank;
                if (FluidUtil.interactWithFluidHandler(player, handIn, wrappedTank))
                {
                    return InteractionResult.SUCCESS;
                }

                if (tank.addHeldBits(current, player))
                {
                    return InteractionResult.SUCCESS;
                }
            }
            else
            {
                if (tank.addAllPossibleBits(player))
                {
                    return InteractionResult.SUCCESS;
                }
            }

            if (tank.extractBits(player, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, pos))
            {
                return InteractionResult.SUCCESS;
            }
        }
        catch (final ExceptionNoTileEntity e)
        {
            Log.noTileError(e);
        }

        return InteractionResult.FAIL;
    }

    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos)
    {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return true;
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootContext.Builder builder)
    {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) == null)
        {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(getTankDrop((BlockEntityBitStorage) builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)));
    }

    public ItemStack getTankDrop(final BlockEntityBitStorage bitTank)
    {
        final ItemStack tankStack = new ItemStack(this);
        tankStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
          .ifPresent(s -> s
                            .fill(
                              bitTank
                                .getCapability(
                                  CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                                )
                                .map(
                                  t -> t
                                         .drain(
                                           Integer.MAX_VALUE,
                                           IFluidHandler.FluidAction.EXECUTE
                                         )
                                ).orElse(FluidStack.EMPTY),
                              IFluidHandler.FluidAction.EXECUTE
                            )
          );
        return tankStack;
    }
}
