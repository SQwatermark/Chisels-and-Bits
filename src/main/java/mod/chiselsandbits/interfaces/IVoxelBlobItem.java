package mod.chiselsandbits.interfaces;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;

public interface IVoxelBlobItem {

    void rotate(ItemStack is, Direction.Axis axis, Rotation rotation);

}
