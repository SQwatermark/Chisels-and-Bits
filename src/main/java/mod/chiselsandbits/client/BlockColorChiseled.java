package mod.chiselsandbits.client;

import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockColorChiseled implements BlockColor {

    public static final int TINT_MASK = 0xff;
    public static final int TINT_BITS = 8;

    @Override
    public int getColor(@NotNull BlockState pState, @Nullable BlockAndTintGetter pLevel, @Nullable BlockPos pPos, int pTintIndex) {
        // 一点hack
        BlockState tstate = ModUtil.getStateById(pTintIndex >> TINT_BITS);
        int tintValue = pTintIndex & TINT_MASK;
        return Minecraft.getInstance().getBlockColors().getColor(tstate, pLevel, pPos, tintValue);
    }

}
