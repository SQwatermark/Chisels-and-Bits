package mod.chiselsandbits.data.recipe;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import com.ldtteam.datagenerators.recipes.RecipeResultJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedPatternJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedRecipeJson;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class AbstractRecipeGenerator implements DataProvider
{

    private final DataGenerator generator;
    private final IForgeRegistryEntry<?> result;

    private HashCache cache = null;

    protected AbstractRecipeGenerator(final DataGenerator generator, final Item result) {
        this.generator = generator;
        this.result = result;}
    protected AbstractRecipeGenerator(final DataGenerator generator, final Block result) {
        this.generator = generator;
        this.result = result;}

    @Override
    public final void run(final HashCache cache) throws IOException
    {
        this.cache = cache;
        generate();
    }

    protected abstract void generate() throws IOException;

    protected final void addShapedRecipe(
      final String upperPart,
      final String middlePart,
      final String lowerPart,
      final String keyOne,
      final RecipeIngredientKeyJson ingOne
    ) throws IOException
    {
        final ShapedRecipeJson json = createShaped(upperPart, middlePart, lowerPart);
        json.setKey(ImmutableMap.of(keyOne, ingOne));

        save(json);
    }

    protected final void addShapedRecipe(
      final String upperPart,
      final String middlePart,
      final String lowerPart,
      final String keyOne,
      final RecipeIngredientKeyJson ingOne,
      final String keyTwo,
      final RecipeIngredientKeyJson ingTwo
    ) throws IOException
    {
        final ShapedRecipeJson json = createShaped(upperPart, middlePart, lowerPart);
        json.setKey(ImmutableMap.of(keyOne, ingOne, keyTwo, ingTwo));

        save(json);
    }

    protected final void addShapedRecipe(
      final String upperPart,
      final String middlePart,
      final String lowerPart,
      final String keyOne,
      final RecipeIngredientKeyJson ingOne,
      final String keyTwo,
      final RecipeIngredientKeyJson ingTwo,
      final String keyThree,
      final RecipeIngredientKeyJson ingThree
    ) throws IOException
    {
        final ShapedRecipeJson json = createShaped(upperPart, middlePart, lowerPart);
        json.setKey(ImmutableMap.of(keyOne, ingOne, keyTwo, ingTwo, keyThree, ingThree));

        save(json);
    }

    protected final void addShapedRecipe(
      final String upperPart,
      final String middlePart,
      final String lowerPart,
      final String keyOne,
      final RecipeIngredientKeyJson ingOne,
      final String keyTwo,
      final RecipeIngredientKeyJson ingTwo,
      final String keyThree,
      final RecipeIngredientKeyJson ingThree,
      final String keyFour,
      final RecipeIngredientKeyJson ingFour
    ) throws IOException
    {
        final ShapedRecipeJson json = createShaped(upperPart, middlePart, lowerPart);
        json.setKey(ImmutableMap.of(keyOne, ingOne, keyTwo, ingTwo, keyThree, ingThree, keyFour, ingFour));

        save(json);
    }

    private ShapedRecipeJson createShaped(
      final String upperPart,
      final String middlePart,
      final String lowerPart
    ) {
        final ShapedRecipeJson json = new ShapedRecipeJson();
        json.setGroup(Constants.MOD_ID);
        json.setRecipeType(ShapedRecipeJson.getDefaultType());
        json.setPattern(new ShapedPatternJson(upperPart, middlePart, lowerPart));
        return json;
    }

    private void save(final ShapedRecipeJson shapedRecipeJson) throws IOException
    {
        shapedRecipeJson.setResult(new RecipeResultJson(1, Objects.requireNonNull(this.result.getRegistryName()).toString()));

        final Path recipeFolder = this.generator.getOutputFolder().resolve(Constants.DataGenerator.RECIPES_DIR);
        final Path recipePath = recipeFolder.resolve(Objects.requireNonNull(this.result.getRegistryName()).getPath() + ".json");

        DataProvider.save(Constants.DataGenerator.GSON, cache, shapedRecipeJson.serialize(), recipePath);
    }

    @Override
    public final String getName()
    {
        return Objects.requireNonNull(result.getRegistryName()).toString() + " recipe generator";
    }
}
