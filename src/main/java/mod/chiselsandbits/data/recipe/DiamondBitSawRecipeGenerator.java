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
public class DiamondBitSawRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new DiamondBitSawRecipeGenerator(event.getGenerator()));
    }

    private DiamondBitSawRecipeGenerator(final DataGenerator generator) {
        super(generator, ModItems.ITEM_BIT_SAW_DIAMOND.get());
    }

    @Override
    protected void generate() throws IOException
    {
        addShapedRecipe(
          "sss",
          "sdd",
          "   ",
          "s",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.RODS_WOODEN.location().toString(), true)),
          "d",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(Tags.Items.GEMS_DIAMOND.location().toString(), true))
        );
    }
}
