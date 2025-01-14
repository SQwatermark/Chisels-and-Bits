package mod.chiselsandbits.crafting;

import javax.annotation.Nonnull;

import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class StackableCrafting extends CustomRecipe {

    public StackableCrafting(ResourceLocation name) {
        super(name, CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(
            final CraftingContainer craftingInv,
            final Level worldIn) {
        ItemStack target = null;

        for (int x = 0; x < craftingInv.getContainerSize(); x++) {
            final ItemStack f = craftingInv.getItem(x);
            if (ModUtil.isEmpty(f)) {
                continue;
            }

            if (target == null) {
                target = f;
            } else {
                return false;
            }
        }

        if (target == null || !target.hasTag() || !(target.getItem() instanceof ItemBlockChiseled)) {
            return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingInv, RegistryAccess p_267165_) {
        ItemStack target = null;

        for (int x = 0; x < craftingInv.getContainerSize(); x++) {
            final ItemStack f = craftingInv.getItem(x);
            if (ModUtil.isEmpty(f)) {
                continue;
            }

            if (target == null) {
                target = f;
            } else {
                return ModUtil.getEmptyStack();
            }
        }

        if (target == null || !target.hasTag() || !(target.getItem() instanceof ItemBlockChiseled)) {
            return ModUtil.getEmptyStack();
        }

        return getSortedVersion(target);
    }

    private ItemStack getSortedVersion(
            final @Nonnull ItemStack stack) {
        final NBTBlobConverter tmp = new NBTBlobConverter();
        tmp.readChisleData(ModUtil.getSubCompound(stack, ModUtil.NBT_BLOCKENTITYTAG, false), VoxelBlob.VERSION_ANY);

        VoxelBlob bestBlob = tmp.getBlob();
        byte[] bestValue = bestBlob.toLegacyByteArray();

        VoxelBlob lastBlob = bestBlob;
        for (int x = 0; x < 34; x++) {
            lastBlob = lastBlob.spin(Axis.Y);
            final byte[] aValue = lastBlob.toLegacyByteArray();

            if (arrayCompare(bestValue, aValue)) {
                bestBlob = lastBlob;
                bestValue = aValue;
            }
        }

        tmp.setBlob(bestBlob);
        return tmp.getItemStack(false);
    }

    private boolean arrayCompare(
            final byte[] bestValue,
            final byte[] aValue) {
        if (aValue.length < bestValue.length) {
            return true;
        }

        if (aValue.length > bestValue.length) {
            return false;
        }

        for (int x = 0; x < aValue.length; x++) {
            if (aValue[x] < bestValue[x]) {
                return true;
            }

            if (aValue[x] > bestValue[x]) {
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean canCraftInDimensions(
            final int width,
            final int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267025_) {
        return ModUtil.getEmptyStack(); // nope
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(
            final CraftingContainer inv) {
        final NonNullList<ItemStack> aitemstack = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            final ItemStack itemstack = ModUtil.nonNull(inv.getItem(i));
            aitemstack.set(i, itemstack.getCraftingRemainingItem());
        }

        return aitemstack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.STACKABLE_CRAFTING.get();
    }
}
