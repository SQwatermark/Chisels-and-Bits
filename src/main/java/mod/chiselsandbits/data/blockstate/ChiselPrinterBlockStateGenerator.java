package mod.chiselsandbits.data.blockstate;

import com.google.common.collect.Maps;
import com.ldtteam.datagenerators.blockstate.BlockstateJson;
import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.printer.ChiselPrinterBlock;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChiselPrinterBlockStateGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ChiselPrinterBlockStateGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ChiselPrinterBlockStateGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final HashCache cache) throws IOException
    {
        actOnBlock(cache, ModBlocks.CHISEL_PRINTER_BLOCK.get());
    }

    public void actOnBlock(final HashCache cache, final Block block) throws IOException
    {
        final Map<String, BlockstateVariantJson> variants = Maps.newHashMap();

        ChiselPrinterBlock.FACING.getPossibleValues().forEach(dir -> {
            final String variantKey = String.format("%s=%s", ChiselPrinterBlock.FACING.getName(), dir);
            String modelFile = Constants.DataGenerator.CHISEL_PRINTER_MODEL.toString();
            final BlockstateModelJson model = new BlockstateModelJson(modelFile, 0, (int) dir.getOpposite().toYRot());
            variants.put(variantKey, new BlockstateVariantJson(model));
        });

        final BlockstateJson blockstateJson = new BlockstateJson(variants);
        final Path blockstateFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve(block.getRegistryName().getPath() + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, blockstateJson.serialize(), blockstatePath);
    }

    @Override
    public String getName()
    {
        return "ChiselStation blockstate generator";
    }
}
