package mod.chiselsandbits.render.helpers;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ModelQuadLayer {

    public float[] uvs;
    public TextureAtlasSprite sprite;
    public int light;
    public int color;
    public int tint;

    public static class ModelQuadLayerBuilder {
        public final ModelQuadLayer cache = new ModelQuadLayer();
        public final ModelLightMapReader lightMapReader;
        public ModelUVReader uvReader;

        public ModelQuadLayerBuilder(
                final TextureAtlasSprite sprite,
                final int uCoord,
                final int vCoord) {
            cache.sprite = sprite;
            lightMapReader = new ModelLightMapReader();
            uvReader = new ModelUVReader(sprite, uCoord, vCoord);
        }

        public ModelQuadLayer build(
                final int stateid,
                final int color,
                final int lightValue) {
            cache.light = Math.max(lightValue, lightMapReader.lv);
            cache.uvs = uvReader.quadUVs;
            cache.color = cache.tint != -1 ? color : 0xffffffff;

            if (0x00 <= cache.tint && cache.tint <= 0xff) {
                cache.color = 0xffffffff;
                cache.tint = (stateid << 8) | cache.tint;
            } else {
                cache.tint = -1;
            }

            return cache;
        }
    }

    ;

}
