package mod.chiselsandbits.crafting;

import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemNegativePrint;
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

public class NegativeInversionCrafting extends CustomRecipe {

    public NegativeInversionCrafting(ResourceLocation name) {
        super(name, CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingContainer craftingInv, Level worldIn) {
        return analzyeCraftingInventory(craftingInv, true) != null;
    }

    public ItemStack analzyeCraftingInventory(
            final CraftingContainer craftingInv,
            final boolean generatePattern) {
        ItemStack targetA = null;
        ItemStack targetB = null;

        for (int x = 0; x < craftingInv.getContainerSize(); x++) {
            final ItemStack f = craftingInv.getItem(x);
            if (f.isEmpty()) {
                continue;
            }

            if (f.getItem() instanceof ItemNegativePrint) {
                if (ModItems.ITEM_NEGATIVE_PRINT.get().isWritten(f)) {
                    if (targetA != null) {
                        return null;
                    }

                    targetA = f;
                } else {
                    if (targetB != null) {
                        return null;
                    }

                    targetB = f;
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
            bestBlob.binaryReplacement(ModUtil.getStateId(Blocks.STONE.defaultBlockState()), 0);

            tmp.setBlob(bestBlob);

            final CompoundTag comp = ModUtil.getTagCompound(targetA).copy();
            tmp.writeChiselData(comp, false);

            final ItemStack outputPattern = new ItemStack(targetA.getItem());
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
            if (itemstack != null && itemstack.getItem() == ModItems.ITEM_NEGATIVE_PRINT_WRITTEN.get() && itemstack.hasTag()) {
                ModUtil.adjustStackSize(itemstack, 1);
            }
        }

        return aitemstack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NEGATIVE_INVERSION_CRAFTING.get();
    }
}
