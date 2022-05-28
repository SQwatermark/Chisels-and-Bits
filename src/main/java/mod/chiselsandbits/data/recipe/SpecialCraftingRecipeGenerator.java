package mod.chiselsandbits.data.recipe;

import com.google.gson.JsonObject;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModRecipeSerializers;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpecialCraftingRecipeGenerator implements DataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new SpecialCraftingRecipeGenerator(event.getGenerator()));
    }

    private final DataGenerator generator;

    private SpecialCraftingRecipeGenerator(final DataGenerator generator) {this.generator = generator;}

    @Override
    public void run(final HashCache cache) throws IOException
    {
        saveRecipe(cache, ModRecipeSerializers.BAG_DYEING.getId());
        saveRecipe(cache, ModRecipeSerializers.CHISEL_BLOCK_CRAFTING.getId());
        saveRecipe(cache, ModRecipeSerializers.BIT_SAW_CRAFTING.getId());
        saveRecipe(cache, ModRecipeSerializers.CHISEL_CRAFTING.getId());
        saveRecipe(cache, ModRecipeSerializers.MIRROR_TRANSFER_CRAFTING.getId());
        saveRecipe(cache, ModRecipeSerializers.NEGATIVE_INVERSION_CRAFTING.getId());
        saveRecipe(cache, ModRecipeSerializers.STACKABLE_CRAFTING.getId());
    }

    private void saveRecipe(final HashCache cache, final ResourceLocation location) throws IOException
    {
        final JsonObject object = new JsonObject();
        object.addProperty("type", location.toString());

        final Path recipeFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.RECIPES_DIR);
        final Path recipePath = recipeFolder.resolve(location.getPath() + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, object, recipePath);
    }

    @Override
    public String getName()
    {
        return "Special crafting recipe generator";
    }
}
