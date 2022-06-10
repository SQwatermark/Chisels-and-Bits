package mod.chiselsandbits.registry;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModContainerTypes {

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, ChiselsAndBits.MODID);

    public static void onModConstruction() {
        MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
