package mod.chiselsandbits.registry;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.crafting.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers
{

    private static final DeferredRegister<RecipeSerializer<?>> REGISTRAR = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ChiselsAndBits.MODID);

    public static final RegistryObject<SimpleRecipeSerializer<ChiselCrafting>> CHISEL_CRAFTING = REGISTRAR.register("chisel_crafting", () -> new SimpleRecipeSerializer<>(ChiselCrafting::new));
    public static final RegistryObject<SimpleRecipeSerializer<ChiselBlockCrafting>> CHISEL_BLOCK_CRAFTING = REGISTRAR.register("chisel_block_crafting", () -> new SimpleRecipeSerializer<>(ChiselBlockCrafting::new));
    public static final RegistryObject<SimpleRecipeSerializer<StackableCrafting>> STACKABLE_CRAFTING = REGISTRAR.register("stackable_crafting", () -> new SimpleRecipeSerializer<>(StackableCrafting::new));
    public static final RegistryObject<SimpleRecipeSerializer<NegativeInversionCrafting>> NEGATIVE_INVERSION_CRAFTING = REGISTRAR.register("negative_inversion_crafting", () -> new SimpleRecipeSerializer<>(NegativeInversionCrafting::new));
    public static final RegistryObject<SimpleRecipeSerializer<MirrorTransferCrafting>> MIRROR_TRANSFER_CRAFTING = REGISTRAR.register("mirror_transfer_crafting", () -> new SimpleRecipeSerializer<>(MirrorTransferCrafting::new));

    public static void onModConstruction() {
        REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
