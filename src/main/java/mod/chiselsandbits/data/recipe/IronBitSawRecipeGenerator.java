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
public class IronBitSawRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new IronBitSawRecipeGenerator(event.getGenerator()));
    }

    private IronBitSawRecipeGenerator(final DataGenerator generator) {
        super(generator, ModItems.ITEM_BIT_SAW_IRON.get());
    }

    @Override
    protected void generate() throws IOException
    {
        addShapedRecipe(
          "sss",
          "stt",
          "   ",
          "s",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.RODS_WOODEN.location().toString(), true)),
          "t",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.INGOTS_IRON.location().toString(), true))
        );
    }
}
