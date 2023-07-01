package mod.chiselsandbits.registry;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModItemGroups {

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ChiselsAndBits.MODID);

    // TODO https://github.com/Creators-of-Create/Create/blob/mc1.20.1/dev/src/main/java/com/simibubi/create/AllCreativeModeTabs.java
    public static final RegistryObject<CreativeModeTab> CHISELS_AND_BITS = CREATIVE_TABS.register(ChiselsAndBits.MODID,
            () -> CreativeModeTab.builder()
                    .title(Component.literal(Constants.MOD_ID)) // TODO 翻译
                    .withSearchBar()
                    .icon(() -> new ItemStack(ModItems.ITEM_CHISEL_DIAMOND.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ITEM_CHISEL_DIAMOND.get());
                        output.accept(ModItems.ITEM_MIRROR_PRINT.get());
                        output.accept(ModItems.ITEM_MIRROR_PRINT_WRITTEN.get());
                        output.accept(ModItems.ITEM_POSITIVE_PRINT.get());
                        output.accept(ModItems.ITEM_POSITIVE_PRINT_WRITTEN.get());
                        output.accept(ModItems.ITEM_NEGATIVE_PRINT.get());
                        output.accept(ModItems.ITEM_NEGATIVE_PRINT_WRITTEN.get());
                        output.accept(ModItems.ITEM_WRENCH.get());
                        output.accept(ModItems.ITEM_TAPE_MEASURE.get());
                        output.accept(ModItems.ITEM_MAGNIFYING_GLASS.get());
                        // TODO
//                        output.accept(ModItems.ITEM_BLOCK_BIT.get());
                    })
                    .backgroundSuffix("item_search.png")
                    .build());

    public static void onModConstruction() {
        CREATIVE_TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
