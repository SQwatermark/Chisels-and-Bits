package mod.chiselsandbits.client.model;

import mod.chiselsandbits.client.model.baked.DataAwareChiseledBlockBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

/**
 * 用于为雕刻方块使用特殊的模型烘焙方法
 */
public class ChiseledBlockModel implements IUnbakedGeometry<ChiseledBlockModel> {

    /**
     * 资源重载时会调用此方法
     */
    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new DataAwareChiseledBlockBakedModel();
    }
}
