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
public class GoldBitSawRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new GoldBitSawRecipeGenerator(event.getGenerator()));
    }

    private GoldBitSawRecipeGenerator(final DataGenerator generator) {
        super(generator, ModItems.ITEM_BIT_SAW_GOLD.get());
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
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.INGOTS_GOLD.location().toString(), true))
        );
    }
}
