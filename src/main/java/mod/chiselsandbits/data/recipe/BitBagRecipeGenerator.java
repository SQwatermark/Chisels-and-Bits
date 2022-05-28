package mod.chiselsandbits.data.recipe;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BitBagRecipeGenerator extends AbstractRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BitBagRecipeGenerator(event.getGenerator()));
    }

    private BitBagRecipeGenerator(final DataGenerator generator)
    {
        super(generator, ModItems.ITEM_BIT_BAG_DEFAULT.get());
    }

    @Override
    protected void generate() throws IOException
    {
        addShapedRecipe(
          "www",
          "wbw",
          "www",
          "b",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(ModItems.ITEM_BLOCK_BIT.getId().toString(), false)),
          "w",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(ItemTags.WOOL.location().toString(), true))
        );
    }
}
