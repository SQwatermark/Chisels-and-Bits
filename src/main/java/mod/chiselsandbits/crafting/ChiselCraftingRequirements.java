package mod.chiselsandbits.crafting;

import mod.chiselsandbits.api.StateCount;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

class ChiselCraftingRequirements {
    private final VoxelBlob voxelBlob;
    final ItemStack pattern;

    private Boolean isValid = null;

    final ItemStack[] pile;
    private final ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();

    public ChiselCraftingRequirements(
            final Container inv,
            final ItemStack inPattern,
            final boolean copy) {
        pile = new ItemStack[inv.getContainerSize()];
        pattern = inPattern;

        for (int x = 0; x < inv.getContainerSize(); x++) {
            final ItemStack is = inv.getItem(x);
            pile[x] = is;

            if (!copy) {
                // if we are not copying.. then we remove it...
                inv.setItem(x, ModUtil.getEmptyStack());
            }

            if (is == null) {
                continue;
            }

            if (is.getItem() instanceof ItemChiseledBit) {
                stacks.add(copy ? is.copy() : is);
            }
        }

        voxelBlob = ModUtil.getBlobFromStack(inPattern, null);
    }

    public boolean isValid() {
        if (isValid != null) {
            return isValid;
        }

        final List<StateCount> count = voxelBlob.getStateCounts();

        isValid = true;
        for (final StateCount ref : count) {
            if (ref.stateId != 0) {

                for (final ItemStack is : stacks) {
                    if (ItemChiseledBit.getStackState(is) == ref.stateId && ModUtil.notEmpty(is)) {
                        final int original = ModUtil.getStackSize(is);
                        ModUtil.setStackSize(is, Math.max(0, ModUtil.getStackSize(is) - ref.quantity));
                        ref.quantity -= original - ModUtil.getStackSize(is);
                    }
                }

                if (ref.quantity > 0) {
                    isValid = false;
                    break;
                }
            }
        }
        return isValid;
    }
}