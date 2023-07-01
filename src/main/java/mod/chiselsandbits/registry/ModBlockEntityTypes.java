package mod.chiselsandbits.registry;

import mod.chiselsandbits.chiseledblock.BlockEntityChiseledBlock;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntityTypes {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ChiselsAndBits.MODID);

    public static RegistryObject<BlockEntityType<BlockEntityChiseledBlock>> CHISELED =
            BLOCK_ENTITIES.register("chiseled",
                    () -> BlockEntityType.Builder.of(BlockEntityChiseledBlock::new,
                                    ModBlocks.CHISELED_BLOCK.get())
                            .build(null)); // null ok

    public static void onModConstruction() {
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
