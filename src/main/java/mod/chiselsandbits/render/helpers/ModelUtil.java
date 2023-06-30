package mod.chiselsandbits.render.helpers;

import com.google.common.collect.Maps;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.render.chiseledblock.ChiselRenderType;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBakedModel;
import mod.chiselsandbits.render.helpers.ModelQuadLayer.ModelQuadLayerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.data.ModelData;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ModelUtil implements ICacheClearable {
    private static final HashMap<Pair<RenderType, Direction>, HashMap<Integer, String>> blockToTexture = new HashMap<>();
    private static final HashMap<Triple<Integer, RenderType, Direction>, ModelQuadLayer[]> cache = new HashMap<>();
    private static final HashMap<Pair<RenderType, Integer>, ChiseledBlockBakedModel> breakCache = new HashMap<>();

    @SuppressWarnings("unused")
    private static final ModelUtil instance = new ModelUtil();

    public static RandomSource MODEL_RANDOM = RandomSource.create();

    @Override
    public void clearCache() {
        blockToTexture.clear();
        cache.clear();
        breakCache.clear();
    }

    public static ModelQuadLayer[] getCachedFace(int stateID, RandomSource weight, Direction face, RenderType layer) {
        if (layer == null) {
            return null;
        }

        var cacheVal = Triple.of(stateID, layer, face);

        // 先从缓存中获取
        final ModelQuadLayer[] mpc = cache.get(cacheVal);
        if (mpc != null) {
            return mpc;
        }

        // TODO ?
//        final RenderType original = net.minecraftforge.client.MinecraftForgeClient.getRenderType();
//        try {
//            ForgeHooksClient.setRenderType(layer);
//            return getInnerCachedFace(cacheVal, stateID, weight, face, layer);
//        } finally {
//            // restore previous layer.
//            ForgeHooksClient.setRenderType(original);
//        }
        return getInnerCachedFace(cacheVal, stateID, weight, face, layer);
    }

    private static ModelQuadLayer[] getInnerCachedFace(Triple<Integer, RenderType, Direction> cacheVal,
            int stateID, RandomSource weight, Direction face, RenderType layer) {

        BlockState state = ModUtil.getStateById(stateID);

        int lightValue = ChiselsAndBits.getConfig().getClient().useGetLightValue.get() ? DeprecationHelper.getLightValue(state) : 0;

        Fluid fluid = BlockBitInfo.getFluidFromBlock(state.getBlock());
        if (fluid != null) {
            var clientFluid = IClientFluidTypeExtensions.of(fluid);
            for (Direction direction : Direction.values()) {
                ModelQuadLayer[] mp = new ModelQuadLayer[1];
                mp[0] = new ModelQuadLayer();
                mp[0].color = clientFluid.getTintColor();
                mp[0].light = lightValue;

                final float V = 0.5f;
                final float Uf = 1.0f;
                final float U = 0.5f;
                final float Vf = 1.0f;

                if (direction.getAxis() == Axis.Y) {
                    mp[0].sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(clientFluid.getStillTexture());
                    mp[0].uvs = new float[]{Uf, Vf, 0, Vf, Uf, 0, 0, 0};
                } else if (direction.getAxis() == Axis.X) {
                    mp[0].sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(clientFluid.getFlowingTexture());
                    mp[0].uvs = new float[]{U, 0, U, V, 0, 0, 0, V};
                } else {
                    mp[0].sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(clientFluid.getFlowingTexture());
                    mp[0].uvs = new float[]{U, 0, 0, 0, U, V, 0, V};
                }

                mp[0].tint = 0;

                cache.put(Triple.of(stateID, layer, direction), mp);
            }

            return cache.get(cacheVal);
        }

        HashMap<Direction, ArrayList<ModelQuadLayerBuilder>> tmp = new HashMap<>();
        int color = BlockBitInfo.getColorFor(state, 0);

        for (Direction direction : Direction.values()) {
            tmp.put(direction, new ArrayList<>());
        }

        // 获取方块的模型？
        BakedModel model = ModelUtil.solveModel(state, weight, Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state), layer);

        if (model != null) {
            for (Direction direction : Direction.values()) {
                processFaces(tmp.get(direction), ModelUtil.getModelQuads(model, state, direction, MODEL_RANDOM, layer), state);
            }
        }

        for (Direction direction : Direction.values()) {
            Triple<Integer, RenderType, Direction> k = Triple.of(stateID, layer, direction);
            ArrayList<ModelQuadLayerBuilder> x = tmp.get(direction);
            ModelQuadLayer[] mp = new ModelQuadLayer[x.size()];

            for (int z = 0; z < x.size(); z++) {
                mp[z] = x.get(z).build(stateID, color, lightValue);
            }

            cache.put(k, mp);
        }

        return cache.get(cacheVal);
    }

    // 获取一个普通模型的quads
    private static List<BakedQuad> getModelQuads(BakedModel model, BlockState state, Direction direction, RandomSource rand, RenderType renderType) {
        try {
            // try to get block model...
            return model.getQuads(state, direction, rand, ModelData.EMPTY, renderType);
        } catch (final Throwable ignored) {

        }

        try {
            // try to get item model?
            return model.getQuads(null, direction, rand, ModelData.EMPTY, renderType);
        } catch (final Throwable ignored) {

        }

        ItemStack itemStack = ModUtil.getItemStackFromBlockState(state); // TODO 意义何在？
        if (!ModUtil.isEmpty(itemStack)) {
            BakedModel secondModel = getOverrides(model)
                    .resolve(model, itemStack, Minecraft.getInstance().level, Minecraft.getInstance().player, 0);

            if (secondModel != null) {
                try {
                    return secondModel.getQuads(null, direction, rand, ModelData.EMPTY, renderType);
                } catch (final Throwable ignored) {

                }
            }
        }

        // try to not crash...
        return Collections.emptyList();
    }

    private static ItemOverrides getOverrides(BakedModel model) {
        if (model != null) {
            return model.getOverrides();
        }
        return ItemOverrides.EMPTY;
    }

    private static void processFaces(ArrayList<ModelQuadLayerBuilder> list, List<BakedQuad> quads, BlockState state) {
        for (BakedQuad q : quads) {
            Direction face = q.getDirection();
            try {
                TextureAtlasSprite sprite = q.getSprite();

                ModelQuadLayerBuilder b = null;
                for (ModelQuadLayerBuilder lx : list) {
                    if (lx.cache.sprite == sprite) {
                        b = lx;
                        break;
                    }
                }

                if (b == null) {
                    // top/bottom
                    int uCoord = 0;
                    int vCoord = 2;

                    switch (face) {
                        case NORTH, SOUTH -> {
                            uCoord = 0;
                            vCoord = 1;
                        }
                        case EAST, WEST -> {
                            uCoord = 1;
                            vCoord = 2;
                        }
                        default -> {
                        }
                    }

                    b = new ModelQuadLayerBuilder(sprite, uCoord, vCoord);
                    b.cache.tint = q.getTintIndex();
                    list.add(b);
                }

                q.pipe(b.uvReader);

//                if (ChiselsAndBits.getConfig().getClient().enableFaceLightmapExtraction.get()) {
//                    //TODO: Check if this works.
//                    b.lightMapReader.setVertexFormat(DefaultVertexFormat.BLOCK);
//                    q.pipe(b.lightMapReader);
//                }
            } catch (final Exception ignored) {

            }
        }
    }

    private ModelUtil() {
        ChiselsAndBits.getInstance().addClearable(this);
    }

    // TODO 这个方法的意图？把原本的普通方块模型进行某种转换？
    public static BakedModel solveModel(BlockState state, RandomSource weight, BakedModel originalModel, RenderType layer) {
        // 判断模型是否存在面？？？
        boolean hasFaces;
        try {
            hasFaces = hasFaces(originalModel, state, null, weight, layer);
            for (Direction direction : Direction.values()) {
                hasFaces = hasFaces || hasFaces(originalModel, state, direction, weight, layer);
            }
        } catch (Exception e) {
            // an exception was thrown... use the item model and hope...
            hasFaces = false;
        }

        if (!hasFaces) {
            // if the model itemStack empty then lets grab an item and try that...
            ItemStack itemStack = ModUtil.getItemStackFromBlockState(state); // TODO 和hasFaces里边的方法重了吧？
            if (!ModUtil.isEmpty(itemStack)) {
                BakedModel itemModel = Minecraft.getInstance().getItemRenderer().getModel(itemStack, Minecraft.getInstance().level, Minecraft.getInstance().player, 0);

                try {
                    hasFaces = hasFaces(originalModel, state, null, weight, layer);

                    for (final Direction f : Direction.values()) {
                        hasFaces = hasFaces || hasFaces(originalModel, state, f, weight, layer);
                    }
                } catch (final Exception e) {
                    // an exception was thrown.. use the item model and hope...
                    hasFaces = false;
                }

                if (hasFaces) {
                    return itemModel;
                } else {
                    return new SimpleGeneratedModel(findTexture(Block.getId(state), originalModel, Direction.UP, layer, weight));
                }
            }
        }

        return originalModel;
    }

    private static boolean hasFaces(BakedModel model, BlockState state, Direction direction, RandomSource weight, RenderType renderType) {
        // 模型是否有quads
        List<BakedQuad> quads = getModelQuads(model, state, direction, weight, renderType);
        if (quads == null || quads.isEmpty()) {
            return false;
        }
        // 这个方向上是否有纹理？
        TextureAtlasSprite texture = quads.get(0).getSprite();

//        ModelVertexRange mvr = new ModelVertexRange();
//
//        for (BakedQuad q : quads) {
//            q.pipe(mvr);
//        }

//        return mvr.getLargestRange() > 0 && !isMissing(texture);
        return !isMissing(texture);
    }

    private static boolean isMissing(TextureAtlasSprite texture) {
        return texture == null || texture == Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("missingno"));
    }

    public static TextureAtlasSprite findTexture(
            final int BlockRef,
            final BakedModel model,
            final Direction myFace,
            final RenderType layer,
            final RandomSource random) {
        // didn't work? ok lets try scanning for the texture in the
        if (blockToTexture.getOrDefault(Pair.of(layer, myFace), Maps.newHashMap()).containsKey(BlockRef)) {
            final String textureName = blockToTexture.get(Pair.of(layer, myFace)).get(BlockRef);
            return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(textureName));
        }

        TextureAtlasSprite texture = null;
        final BlockState state = ModUtil.getStateById(BlockRef);

        if (model != null) {
            try {
                texture = findTexture(texture, getModelQuads(model, state, myFace, random, layer), myFace);

                if (texture == null) {
                    for (final Direction side : Direction.values()) {
                        texture = findTexture(texture, getModelQuads(model, state, side, random, layer), side);
                    }

                    texture = findTexture(texture, getModelQuads(model, state, null, random, layer), null);
                }
            } catch (final Exception ignored) {
            }
        }

        // who knows if that worked.. now lets try to get a texture...
        if (isMissing(texture)) {
            try {
                if (model != null) {
                    texture = model.getParticleIcon();
                }
            } catch (final Exception ignored) {
            }
        }

        if (isMissing(texture)) {
            try {
                texture = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state);
            } catch (final Exception ignored) {
            }
        }

        if (texture == null) {
            texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("missingno"));
        }

        blockToTexture.remove(Pair.of(layer, myFace), null);
        blockToTexture.putIfAbsent(Pair.of(layer, myFace), Maps.newHashMap());
        blockToTexture.get(Pair.of(layer, myFace)).put(BlockRef, texture.getName().toString());
        return texture;
    }

    private static TextureAtlasSprite findTexture(TextureAtlasSprite texture, List<BakedQuad> faceQuads, Direction direction)
            throws IllegalArgumentException, NullPointerException {
        for (BakedQuad q : faceQuads) {
            if (q.getDirection() == direction) {
                texture = q.getSprite();
            }
        }

        return texture;
    }

    public static boolean isOne(
            final float v) {
        return Math.abs(v) < 0.01;
    }

    public static boolean isZero(
            final float v) {
        return Math.abs(v - 1.0f) < 0.01;
    }

    public static Integer getItemStackColor(
            final ItemStack target,
            final int tint) {
        // don't send air though to MC, some mods have registered their custom
        // color handlers for it and it can crash.

        if (ModUtil.isEmpty(target))
            return -1;

        return Minecraft.getInstance().getItemColors().getColor(target, tint);
    }

    public static ChiseledBlockBakedModel getBreakingModel(ChiselRenderType layer, Integer blockStateID, RandomSource random) {
        Pair<RenderType, Integer> key = Pair.of(layer.layer, blockStateID);
        ChiseledBlockBakedModel out = breakCache.get(key);

        if (out == null) {
            final BlockState state = ModUtil.getStateById(blockStateID);
            final BakedModel model = ModelUtil.solveModel(state, random, Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(ModUtil.getStateById(blockStateID)), layer.layer);

            if (model != null) {
                out = ChiseledBlockBakedModel.createFromTexture(ModelUtil.findTexture(blockStateID, model, Direction.UP, layer.layer, random), layer);
            } else {
                out = ChiseledBlockBakedModel.createFromTexture(null, null);
            }

            breakCache.put(key, out);
        }

        return out;
    }

}
