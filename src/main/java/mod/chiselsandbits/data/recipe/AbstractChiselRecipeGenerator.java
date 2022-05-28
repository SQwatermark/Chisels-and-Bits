package mod.chiselsandbits.data.recipe;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import mod.chiselsandbits.items.ItemChisel;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.Tags;

import java.io.IOException;

public abstract class AbstractChiselRecipeGenerator extends AbstractRecipeGenerator
{
    private final TagKey<?> rodTag;
    private final TagKey<?> ingredientTag;

    protected AbstractChiselRecipeGenerator(final DataGenerator generator, final ItemChisel result, final TagKey<?> ingredientTag)
    {
        super(generator, result);
        this.ingredientTag = ingredientTag;
        this.rodTag = Tags.Items.RODS_WOODEN;
    }

    protected AbstractChiselRecipeGenerator(
      final DataGenerator generator,
      final ItemChisel result,
      final TagKey<?> rodTag,
      final TagKey<?> ingredientTag)
    {
        super(generator, result);
        this.rodTag = rodTag;
        this.ingredientTag = ingredientTag;
    }


    @Override
    protected final void generate() throws IOException
    {
        addShapedRecipe(
          "st ",
          "   ",
          "   ",
          "s",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(rodTag.location().toString(), true)), // TODO getName.toString
          "t",
          new RecipeIngredientKeyJson(new RecipeIngredientJson(ingredientTag.location().toString(), true))
        );
    }
}
