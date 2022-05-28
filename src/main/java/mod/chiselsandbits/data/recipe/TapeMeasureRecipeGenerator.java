package mod.chiselsandbits.data.recipe;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TapeMeasureRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new TapeMeasureRecipeGenerator(event.getGenerator()));
    }

    private TapeMeasureRecipeGenerator(final DataGenerator generator)
    {
        super(generator, ModItems.ITEM_TAPE_MEASURE.get());
    }

    @Override
    protected void generate() throws IOException
    {
        addShapedRecipe(
          "  s",
          "isy",
          "ii ",
          "i",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.INGOTS_IRON.location().toString(), true)),
          "s",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.STRING.location().toString(), true)),
          "y",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.DYES_YELLOW.location().toString(), true))
          );
    }
}
