package mod.chiselsandbits.client;

import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemColorChiseled implements ItemColor {

    @Override
    public int getColor(@NotNull ItemStack stack, int tint) {
        final BlockState state = ModUtil.getStateById(tint >> BlockColorChiseled.TINT_BITS);
        final Block blk = state.getBlock();
        final Item i = Item.byBlock(blk);
        int tintValue = tint & BlockColorChiseled.TINT_MASK;

        return ModelUtil.getItemStackColor(new ItemStack(i, 1), tintValue);

    }

}
