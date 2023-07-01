package mod.chiselsandbits.registry;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// TODO 流体储罐什么时候被我删了？
public final class ModBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ChiselsAndBits.MODID);
    private static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ChiselsAndBits.MODID);

    public static final RegistryObject<BlockChiseled> CHISELED_BLOCK = BLOCKS.register("chiseled_block", () ->
            new BlockChiseled("chiseled_block", BlockBehaviour.Properties
                    .of()
                    .forceSolidOn() // 防止被水冲走
                    .strength(-1.0F, 3600000.0F) // 基岩同款
                    .noOcclusion()));

    public static final RegistryObject<ItemBlockChiseled> CHISELED_BLOCK_ITEM = BLOCK_ITEMS.register("chiseled_block", () ->
            new ItemBlockChiseled(CHISELED_BLOCK.get(), new Item.Properties()));

    public static void onModConstruction() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static BlockState getChiseledDefaultState() {
        return CHISELED_BLOCK.get().defaultBlockState();
    }

    public static BlockChiseled convertGivenStateToChiseledBlock(final BlockState state) {
//        final Fluid f = BlockBitInfo.getFluidFromBlock( state.getBlock() );
//        return convertGivenMaterialToChiseledBlock(f != null ? Material.WATER : state.getMaterial());
        return CHISELED_BLOCK.get();
    }

}
