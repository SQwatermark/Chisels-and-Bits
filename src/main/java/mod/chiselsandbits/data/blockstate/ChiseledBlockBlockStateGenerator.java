package mod.chiselsandbits.data.blockstate;

import com.google.common.collect.Maps;
import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.MaterialType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiseledBlockBlockStateGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiseledBlockBlockStateGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ChiseledBlockBlockStateGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final HashCache cache) throws IOException
    {
        for (MaterialType materialType : ModBlocks.VALID_CHISEL_MATERIALS)
        {
            final RegistryObject<BlockChiseled> blockChiseledRegistryObject = ModBlocks.getMaterialToBlockConversions().get(materialType.getType());
            BlockChiseled blockChiseled = blockChiseledRegistryObject.get();
            actOnBlock(cache, blockChiseled, materialType);
        }
    }

    @Override
    public String getName()
    {
        return "Chiseled block blockstate generator";
    }

    public void actOnBlock(final HashCache cache, final Block block, final MaterialType type) throws IOException
    {
        final Map<String, BlockstateVariantJson> variants = Maps.newHashMap();

        block.getStateDefinition().getProperties().stream().forEach(property -> {
            property.getPossibleValues().forEach(value -> {
                final String variantKey = String.format("%s=%s", property.getName(), value);
                String modelFile = Constants.DataGenerator.CHISELED_BLOCK_MODEL.toString();
                final BlockstateModelJson model = new BlockstateModelJson(modelFile, 0, 0);
                variants.put(variantKey, new BlockstateVariantJson(model));
            });
        });

        final BlockstateJson blockstateJson = new BlockstateJson(variants);
        final Path blockstateFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve("chiseled" + type.getName() + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, blockstateJson.serialize(), blockstatePath);
    }
}
