package mod.chiselsandbits.registry;

import mod.chiselsandbits.bitstorage.BlockEntityBitStorage;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.BlockEntityChiseledBlock;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.printer.ChiselPrinterTileEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntityTypes {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ChiselsAndBits.MODID);

    public static RegistryObject<BlockEntityType<BlockEntityChiseledBlock>> CHISELED =
            BLOCK_ENTITIES.register("chiseled",
                    () -> BlockEntityType.Builder.of(BlockEntityChiseledBlock::new,
                                    ModBlocks.getMaterialToBlockConversions().values().stream().map(RegistryObject::get).toArray(BlockChiseled[]::new))
                            .build(null));

    public static RegistryObject<BlockEntityType<BlockEntityBitStorage>> BIT_STORAGE =
            BLOCK_ENTITIES.register("bit_storage",
                    () -> BlockEntityType.Builder.of(BlockEntityBitStorage::new,
                                    ModBlocks.BIT_STORAGE_BLOCK.get())
                            .build(null)
    );

    public static RegistryObject<BlockEntityType<ChiselPrinterTileEntity>> CHISEL_PRINTER =
            BLOCK_ENTITIES.register("chisel_printer",
                    () -> BlockEntityType.Builder.of(ChiselPrinterTileEntity::new,
                                    ModBlocks.CHISEL_PRINTER_BLOCK.get())
                            .build(null)
    );

    public static void onModConstruction() {
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
