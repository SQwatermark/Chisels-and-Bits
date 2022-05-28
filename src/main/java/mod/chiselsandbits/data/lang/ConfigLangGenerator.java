package mod.chiselsandbits.data.lang;

import com.google.gson.JsonObject;
import mod.chiselsandbits.config.AbstractConfiguration;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigLangGenerator implements DataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ConfigLangGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private ConfigLangGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final HashCache cache) throws IOException
    {
        final List<String> langKeys = new ArrayList<>(AbstractConfiguration.LANG_KEYS);
        Collections.sort(langKeys);
        final JsonObject returnValue = new JsonObject();

        for (String langKey : langKeys)
        {
            returnValue.addProperty(langKey, "");
        }

        final Path configLangFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.CONFIG_LANG_DIR);
        final Path langPath = configLangFolder.resolve("config.json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, returnValue, langPath);
    }

    @Override
    public String getName()
    {
        return "Chiseled config lang generator";
    }
}
