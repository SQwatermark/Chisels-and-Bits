package mod.chiselsandbits.registry;

import mod.chiselsandbits.bitbag.BagInventoryMenu;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.printer.ChiselPrinterContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModContainerTypes {

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, ChiselsAndBits.MODID);

    public static final RegistryObject<MenuType<BagInventoryMenu>> BAG_CONTAINER = MENUS.register("bag",
            () -> new MenuType<>(BagInventoryMenu::new));
    public static final RegistryObject<MenuType<ChiselPrinterContainer>> CHISEL_STATION_CONTAINER = MENUS.register("chisel_station",
            () -> new MenuType<>(ChiselPrinterContainer::new));

    public static void onModConstruction() {
        MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
