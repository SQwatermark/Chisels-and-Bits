package mod.chiselsandbits.data.model;

import com.ldtteam.datagenerators.models.item.ItemModelJson;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockBitItemModelGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BlockBitItemModelGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private BlockBitItemModelGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final HashCache cache) throws IOException
    {
        actOnItemWithEmptyGenerated(cache, ModItems.ITEM_BLOCK_BIT.get());
    }

    @Override
    public String getName()
    {
        return "Chisel block item model generator";
    }

    public void actOnItemWithEmptyGenerated(final HashCache cache, final Item item) throws IOException
    {
        final ItemModelJson json = new ItemModelJson();
        json.setParent("item/generated");

        saveItemJson(cache, json, Objects.requireNonNull(item.getRegistryName()).getPath());
    }

    private void saveItemJson(final HashCache cache, final ItemModelJson json, final String name) throws IOException
    {
        final Path itemModelFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.ITEM_MODEL_DIR);
        final Path itemModelPath = itemModelFolder.resolve(name + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, json.serialize(), itemModelPath);
    }
}
