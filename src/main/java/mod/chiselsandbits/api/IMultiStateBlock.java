package mod.chiselsandbits.api;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

/**
 * Implemented by C&B Blocks, can be used to request a material that represents
 * the largest quantity of a C&B block.
 */
public interface IMultiStateBlock {
    BlockState getPrimaryState(
            BlockGetter world,
            BlockPos pos);
}
