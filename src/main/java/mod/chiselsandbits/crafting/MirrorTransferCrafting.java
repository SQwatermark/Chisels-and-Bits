package mod.chiselsandbits.crafting;

import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemMirrorPrint;
import mod.chiselsandbits.items.ItemNegativePrint;
import mod.chiselsandbits.items.ItemPositivePrint;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.registry.ModRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class MirrorTransferCrafting extends CustomRecipe {

    public MirrorTransferCrafting(ResourceLocation name) {
        super(name, CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(
            final CraftingContainer craftingInv,
            final Level worldIn) {
        return analzyeCraftingInventory(craftingInv, true) != null;
    }

    public ItemStack analzyeCraftingInventory(
            final CraftingContainer craftingInv,
            final boolean generatePattern) {
        ItemStack targetA = null;
        ItemStack targetB = null;

        boolean isNegative = false;

        for (int x = 0; x < craftingInv.getContainerSize(); x++) {
            final ItemStack f = craftingInv.getItem(x);
            if (f == null) {
                continue;
            }

            if (f.getItem() instanceof ItemMirrorPrint) {
                if (ModItems.ITEM_MIRROR_PRINT.get().isWritten(f)) {
                    if (targetA != null) {
                        return null;
                    }

                    targetA = f;
                } else {
                    return null;
                }
            } else if (f.getItem() instanceof ItemNegativePrint) {
                if (!ModItems.ITEM_NEGATIVE_PRINT.get().isWritten(f)) {
                    if (targetB != null) {
                        return null;
                    }

                    isNegative = true;
                    targetB = f;
                } else {
                    return null;
                }
            } else if (f.getItem() instanceof ItemPositivePrint) {
                if (!ModItems.ITEM_POSITIVE_PRINT.get().isWritten(f)) {
                    if (targetB != null) {
                        return null;
                    }

                    isNegative = false;
                    targetB = f;
                } else {
                    return null;
                }
            } else if (!ModUtil.isEmpty(f)) {
                return null;
            }
        }

        if (targetA != null && targetB != null) {
            if (generatePattern) {
                return targetA;
            }

            final NBTBlobConverter tmp = new NBTBlobConverter();
            tmp.readChisleData(targetA.getTag(), VoxelBlob.VERSION_ANY);

            final VoxelBlob bestBlob = tmp.getBlob();

            if (isNegative) {
                bestBlob.binaryReplacement(0, ModUtil.getStateId(Blocks.STONE.defaultBlockState()));
            }

            tmp.setBlob(bestBlob);

            final CompoundTag comp = ModUtil.getTagCompound(targetA).copy();
            tmp.writeChiselData(comp, false);

            final ItemStack outputPattern = new ItemStack(targetB.getItem());
            outputPattern.setTag(comp);

            return outputPattern;
        }

        return null;
    }


    @Override
    public ItemStack assemble(CraftingContainer craftingInv, RegistryAccess p_267165_) {
        return analzyeCraftingInventory(craftingInv, false);
    }


    @Override
    public boolean canCraftInDimensions(
            final int width,
            final int height) {
        return width > 1 || height > 1;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267025_) {
        return ModUtil.getEmptyStack(); // nope
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(
            final CraftingContainer craftingInv) {
        final NonNullList<ItemStack> aitemstack = NonNullList.withSize(craftingInv.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            final ItemStack itemstack = craftingInv.getItem(i);
            if (itemstack.getItem() == ModItems.ITEM_MIRROR_PRINT_WRITTEN.get() && itemstack.hasTag()) {
                ModUtil.adjustStackSize(itemstack, 1);
            }
        }

        return aitemstack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.MIRROR_TRANSFER_CRAFTING.get();
    }
}
