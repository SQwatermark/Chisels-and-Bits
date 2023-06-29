package mod.chiselsandbits.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

public class SimpleRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final Function<ResourceLocation, T> constructor;

    public SimpleRecipeSerializer(Function<ResourceLocation, T> p_44399_) {
        this.constructor = p_44399_;
    }

    public T fromJson(ResourceLocation p_44404_, JsonObject p_44405_) {
        return this.constructor.apply(p_44404_);
    }

    public T fromNetwork(ResourceLocation p_44407_, FriendlyByteBuf p_44408_) {
        return this.constructor.apply(p_44407_);
    }

    public void toNetwork(FriendlyByteBuf p_44401_, T p_44402_) {
    }
}