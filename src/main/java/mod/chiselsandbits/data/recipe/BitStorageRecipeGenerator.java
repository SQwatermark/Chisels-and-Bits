package mod.chiselsandbits.data.recipe;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BitStorageRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BitStorageRecipeGenerator(event.getGenerator()));
    }

    private BitStorageRecipeGenerator(final DataGenerator generator) {
        super(generator, ModBlocks.BIT_STORAGE_BLOCK_ITEM.get());
    }

    @Override
    protected void generate() throws IOException
    {
        addShapedRecipe(
          "igi",
          "glg",
          "ici",
          "g",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.GLASS.location().toString(), true)),
          "l",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.LOGS.location().toString(), true)),
          "i",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.INGOTS_IRON.location().toString(), true)),
          "c",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(ModTags.Items.CHISEL.location().toString(), true))
        );
    }
}
