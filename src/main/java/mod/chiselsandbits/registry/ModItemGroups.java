package mod.chiselsandbits.registry;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ModItemGroups {
    public static final CreativeModeTab CHISELS_AND_BITS = (new CreativeModeTab(ChiselsAndBits.MODID) {
        @Override
        public boolean hasSearchBar() {
            return true;
        }
        @NotNull
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.ITEM_CHISEL_DIAMOND.get());
        }
    }).setBackgroundSuffix("item_search.png");
}
