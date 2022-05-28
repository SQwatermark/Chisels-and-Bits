package mod.chiselsandbits.registry;

import com.google.common.collect.Maps;
import mod.chiselsandbits.bitstorage.BlockBitStorage;
import mod.chiselsandbits.bitstorage.ItemBlockBitStorage;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.MaterialType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.printer.ChiselPrinterBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static mod.chiselsandbits.registry.ModItemGroups.CHISELS_AND_BITS;

public final class ModBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ChiselsAndBits.MODID);
    private static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ChiselsAndBits.MODID);

    public static final Map<Material, RegistryObject<BlockChiseled>> MATERIAL_TO_BLOCK_CONVERSIONS = Maps.newHashMap();
    public static final Map<Material, RegistryObject<ItemBlockChiseled>>  MATERIAL_TO_ITEM_CONVERSIONS = Maps.newHashMap();

    public static final RegistryObject<BlockBitStorage> BIT_STORAGE_BLOCK = BLOCKS.register("bit_storage",
            () -> new BlockBitStorage(BlockBehaviour.Properties.of(Material.METAL)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .dynamicShape()
                    .noOcclusion()
                    .isValidSpawn((p_test_1_, p_test_2_, p_test_3_, p_test_4_) -> false)
                    .isRedstoneConductor((p_test_1_, p_test_2_, p_test_3_) -> false)
                    .isSuffocating((p_test_1_, p_test_2_, p_test_3_) -> false)
                    .isViewBlocking((p_test_1_, p_test_2_, p_test_3_) -> false)
            )
    );

    public static final RegistryObject<BlockItem> BIT_STORAGE_BLOCK_ITEM = BLOCK_ITEMS.register("bit_storage",
            () -> new ItemBlockBitStorage(BIT_STORAGE_BLOCK.get(), new Item.Properties()
                    .tab(CHISELS_AND_BITS)
            )
    );
//                .setISTER(() -> ItemStackSpecialRendererBitStorage::new)));

    public static final RegistryObject<ChiselPrinterBlock> CHISEL_PRINTER_BLOCK = BLOCKS.register("chisel_printer",
            () -> new ChiselPrinterBlock(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(1.5f, 6f)
//      .harvestLevel(1)
//      .harvestTool(ToolType.PICKAXE)
                    .noOcclusion()
                    .isRedstoneConductor((p_test_1_, p_test_2_, p_test_3_) -> false)
                    .isViewBlocking((p_test_1_, p_test_2_, p_test_3_) -> false)
    ));

    public static final RegistryObject<BlockItem> CHISEL_PRINTER_ITEM = BLOCK_ITEMS.register("chisel_printer",
            () -> new BlockItem(ModBlocks.CHISEL_PRINTER_BLOCK.get(), new Item.Properties().tab(CHISELS_AND_BITS))
    );

    public static final MaterialType[] VALID_CHISEL_MATERIALS = new MaterialType[] {
        new MaterialType( "wood", Material.WOOD ),
        new MaterialType( "rock", Material.STONE ),
        new MaterialType( "iron", Material.METAL ),
        new MaterialType( "cloth", Material.CLOTH_DECORATION ),
        new MaterialType( "ice", Material.ICE ),
        new MaterialType( "packed_ice", Material.ICE_SOLID ),
        new MaterialType( "clay", Material.CLAY ),
        new MaterialType( "glass", Material.GLASS ),
        new MaterialType( "sand", Material.SAND ),
        new MaterialType( "ground", Material.DIRT ),
        new MaterialType( "grass", Material.DIRT ),
        new MaterialType( "snow", Material.SNOW ),
        new MaterialType( "fluid", Material.WATER ),
        new MaterialType( "leaves", Material.LEAVES ),
    };

    public static void onModConstruction() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        Arrays.stream(VALID_CHISEL_MATERIALS).forEach(materialType -> {
            MATERIAL_TO_BLOCK_CONVERSIONS.put(
              materialType.getType(),
              BLOCKS.register("chiseled" + materialType.getName(), () ->
                      new BlockChiseled("chiseled_" + materialType.getName(), BlockBehaviour.Properties
                      .of(materialType.getType())
                      .strength(1.5f, 6f)
                      .isViewBlocking((p_test_1_, p_test_2_, p_test_3_) -> false)
                      .isRedstoneConductor((p_test_1_, p_test_2_, p_test_3_) -> false)
                      .noOcclusion()))
            );
            MATERIAL_TO_ITEM_CONVERSIONS.put(
              materialType.getType(),
              BLOCK_ITEMS.register("chiseled" + materialType.getName(), () ->
                      new ItemBlockChiseled(MATERIAL_TO_BLOCK_CONVERSIONS.get(materialType.getType()).get(), new Item.Properties()))
            );
          }
        );
    }

    public static Map<Material, RegistryObject<ItemBlockChiseled>> getMaterialToItemConversions() {
        return MATERIAL_TO_ITEM_CONVERSIONS;
    }

    public static Map<Material, RegistryObject<BlockChiseled>> getMaterialToBlockConversions() {
        return MATERIAL_TO_BLOCK_CONVERSIONS;
    }

    public static MaterialType[] getValidChiselMaterials()
    {
        return VALID_CHISEL_MATERIALS;
    }

    @Nullable
    public static BlockState getChiseledDefaultState() {
        final Iterator<RegistryObject<BlockChiseled>> blockIterator = getMaterialToBlockConversions().values().iterator();
        if (blockIterator.hasNext()) {
            return blockIterator.next().get().defaultBlockState();
        }
        return null;
    }

    public static BlockChiseled convertGivenStateToChiseledBlock(final BlockState state) {
        final Fluid f = BlockBitInfo.getFluidFromBlock( state.getBlock() );
        return convertGivenMaterialToChiseledBlock(f != null ? Material.WATER : state.getMaterial());
    }

    public static BlockChiseled convertGivenMaterialToChiseledBlock(final Material material) {
        final RegistryObject<BlockChiseled> materialBlock = getMaterialToBlockConversions().get( material );
        return materialBlock != null ? materialBlock.get() : convertGivenMaterialToChiseledBlock(Material.STONE);
    }

    public static RegistryObject<BlockChiseled> convertGivenStateToChiseledRegistryBlock(final BlockState state) {
        final Fluid f = BlockBitInfo.getFluidFromBlock( state.getBlock() );
        return convertGivenMaterialToChiseledRegistryBlock(f != null ? Material.WATER : state.getMaterial());
    }

    public static RegistryObject<BlockChiseled> convertGivenMaterialToChiseledRegistryBlock(final Material material) {
        final RegistryObject<BlockChiseled> materialBlock = getMaterialToBlockConversions().get( material );
        return materialBlock != null ? materialBlock : convertGivenMaterialToChiseledRegistryBlock(Material.STONE);
    }

    public static boolean convertMaterialTo(final Material source, final Material target) {
        final RegistryObject<BlockChiseled> sourceRegisteredObject = convertGivenMaterialToChiseledRegistryBlock(source);
        return getMaterialToBlockConversions().put(target, sourceRegisteredObject) != null;
    }
}
