package mod.chiselsandbits.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.MaterialType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselBlockItemModelGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiselBlockItemModelGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ChiselBlockItemModelGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final HashCache cache) throws IOException
    {
        for (MaterialType materialType : ModBlocks.VALID_CHISEL_MATERIALS)
        {
            final RegistryObject<BlockChiseled> blockChiseledRegistryObject = ModBlocks.getMaterialToBlockConversions().get(materialType.getType());
            BlockChiseled blockChiseled = blockChiseledRegistryObject.get();
            actOnBlockWithLoader(cache, blockChiseled, new ResourceLocation(Constants.MOD_ID, "chiseled_block"), materialType);
        }

    }

    @Override
    public String getName()
    {
        return "Chisel block item model generator";
    }

    public void actOnBlockWithParent(final HashCache cache, final Block block, final ResourceLocation parent) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent(parent.toString());

        saveBlockJson(cache, block, json, Objects.requireNonNull(block.getRegistryName()).getPath());
    }

    public void actOnBlockWithLoader(final HashCache cache, final Block block, final ResourceLocation loader, final MaterialType materialType) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent("item/generated");
        json.setLoader(loader.toString());

        saveBlockJson(cache, block, json, "chiseled" + materialType.getName());
    }

    private void saveBlockJson(final HashCache cache, final Block block, final ItemModelJson json, final String name) throws IOException
    {
        final Path itemModelFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.ITEM_MODEL_DIR);
        final Path itemModelPath = itemModelFolder.resolve(name + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), itemModelPath);
    }
}
