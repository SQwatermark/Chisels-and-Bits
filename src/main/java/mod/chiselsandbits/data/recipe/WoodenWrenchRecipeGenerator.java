package mod.chiselsandbits.data.recipe;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WoodenWrenchRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new WoodenWrenchRecipeGenerator(event.getGenerator()));
    }

    private WoodenWrenchRecipeGenerator(final DataGenerator generator)
    {
        super(generator, ModItems.ITEM_WRENCH.get());
    }

    @Override
    protected void generate() throws IOException
    {
        addShapedRecipe(
          " w ",
          "ws ",
          "  w",
          "s",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.RODS_WOODEN.location().toString(), true)),
          "w",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.PLANKS.location().toString(), true))
          );
    }
}
