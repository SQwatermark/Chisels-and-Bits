package mod.chiselsandbits.render.chiseledblock;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import mod.chiselsandbits.chiseledblock.BlockEntityChiseledBlock;
import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateInstance;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.client.model.baked.BaseSmartModel;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.render.ModelCombined;
import mod.chiselsandbits.render.cache.CacheMap;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

/**
 * 雕刻方块的模型
 * TODO 它或许应该是个用于存取ChiseledBlockBakedModel的工具类，不需要继承smartModel
 */
public class ChiseledBlockSmartModel extends BaseSmartModel implements ICacheClearable {

    private static final SimpleMaxSizedCache<ModelCacheKey, ChiseledBlockBakedModel> MODEL_CACHE = new SimpleMaxSizedCache<>(ChiselsAndBits.getConfig().getClient().modelCacheSize.get());
    private static final CacheMap<ItemStack, BakedModel> ITEM_TO_MODEL_CACHE = new CacheMap<>();
    private static final CacheMap<VoxelBlobStateInstance, Integer> SIDE_CACHE = new CacheMap<>();

    public static final BitSet FLUID_RENDER_TYPES = new BitSet(RenderType.chunkBufferLayers().size());

    public static int getSides(final BlockEntityChiseledBlock te) {
        final VoxelBlobStateReference ref = te.getBlobStateReference();
        Integer out;

        if (ref == null) {
            return 0;
        }

        synchronized (SIDE_CACHE) {
            out = SIDE_CACHE.get(ref.getInstance());
            if (out == null) {
                final VoxelBlob blob = ref.getVoxelBlob();

                // ignore non-solid, and fluids.
                blob.filter(RenderType.solid());
                blob.filterFluids(false);

                out = blob.getSideFlags(0, VoxelBlob.dim_minus_one, VoxelBlob.dim2);
                SIDE_CACHE.put(ref.getInstance(), out);
            }
        }

        return out;
    }

    /**
     * 获取雕刻方块实体的特定渲染类型的模型
     * @param blockEntity
     * @param layer
     * @return
     */
    public static ChiseledBlockBakedModel getOrCreateBakedModel(BlockEntityChiseledBlock blockEntity, ChiselRenderType layer) {
        VoxelBlobStateReference data = blockEntity.getBlobStateReference();
        Integer stateId = blockEntity.getPrimaryBlockStateId();
        VoxelBlob vBlob = (data != null) ? data.getVoxelBlob() : null;
        return getOrCreateBakedModel(stateId, vBlob, layer, getModelFormat(), Objects.requireNonNull(blockEntity.getLevel()).random);
    }

    /**
     * 获取雕刻物品在特定渲染层的模型
     * @param stack
     * @param layer
     * @return
     */
    public static ChiseledBlockBakedModel getOrCreateBakedModel(ItemStack stack, ChiselRenderType layer) {
        Integer stateId = 0;
        return getOrCreateBakedModel(stateId, ModUtil.getBlobFromStack(stack, null), layer, getModelFormat(), RandomSource.create());
    }

    private static VertexFormat getModelFormat() {
        return DefaultVertexFormat.BLOCK;
    }

    public static boolean ForgePipelineDisabled() {
        return !ForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get() || ChiselsAndBits.getConfig().getClient().disableCustomVertexFormats.get();
    }

    // TODO 入参的format有用吗？
    public static ChiseledBlockBakedModel getOrCreateBakedModel(Integer stateId, VoxelBlob data, ChiselRenderType layer, VertexFormat format, RandomSource random) {
        if (data == null) {
            return new ChiseledBlockBakedModel(stateId, layer, null, format);
        }

        ChiseledBlockBakedModel out = null;

        if (format == getModelFormat()) {
            out = MODEL_CACHE.get(new ModelCacheKey(data, layer));
        }

        if (out == null) {
            out = new ChiseledBlockBakedModel(stateId, layer, data, format);
            if (out.isEmpty()) {
                // TODO 这是什么
                out = ChiseledBlockBakedModel.breakingParticleModel(layer, stateId, random);
            }
            if (format == getModelFormat()) {
                MODEL_CACHE.put(new ModelCacheKey(data, layer), out);
            }
        } else {
            return out;
        }

        return out;
    }

    /**
     * 获取quad时调用的，看看逻辑是什么
     */
    @Override
    public BakedModel handleBlockState(BlockState state, RandomSource rand, ModelData modelData, RenderType renderType) {
        if (state == null) {
            return EmptyModel.BAKED;
        }

        // 似曾相识的结构，但这里有用到
        // This seems silly, but it proves to be faster in practice.
        VoxelBlobStateReference data = modelData.get(BlockEntityChiseledBlock.MP_VBSR);
        VoxelBlob blob = data == null ? null : data.getVoxelBlob();
        Integer blockP = modelData.get(BlockEntityChiseledBlock.MP_PBSI);
        blockP = blockP == null ? 0 : blockP;

        // 从缓存中获取烘焙的模型
        if (renderType == null) {
            ChiseledBlockBakedModel[] models = new ChiseledBlockBakedModel[ChiselRenderType.values().length];
            int o = 0;
            for (ChiselRenderType l : ChiselRenderType.values()) {
                models[o++] = getOrCreateBakedModel(blockP, blob, l, getModelFormat(), rand);
            }
            return new ModelCombined(models);
        }

        BakedModel baked;
        if (RenderType.chunkBufferLayers().contains(renderType) && FLUID_RENDER_TYPES.get(RenderType.chunkBufferLayers().indexOf(renderType))) {
            final ChiseledBlockBakedModel a = getOrCreateBakedModel(blockP, blob, ChiselRenderType.fromLayer(renderType, false), getModelFormat(), rand);
            final ChiseledBlockBakedModel b = getOrCreateBakedModel(blockP, blob, ChiselRenderType.fromLayer(renderType, true), getModelFormat(), rand);

            if (a.isEmpty()) {
                baked = b;
            } else if (b.isEmpty()) {
                baked = a;
            } else {
                baked = new ModelCombined(a, b);
            }
        } else {
            baked = getOrCreateBakedModel(blockP, blob, ChiselRenderType.fromLayer(renderType, false), getModelFormat(), rand);
        }

        return baked;
    }

    @Override
    public BakedModel resolve(final BakedModel originalModel, final ItemStack stack, final Level world, final LivingEntity entity) {
        BakedModel mdl = ITEM_TO_MODEL_CACHE.get(stack);

        if (mdl != null) {
            return mdl;
        }

        CompoundTag c = stack.getTag();
        if (c == null) {
            return this;
        }

        c = c.getCompound(ModUtil.NBT_BLOCKENTITYTAG);

        final byte[] data = c.getByteArray(NBTBlobConverter.NBT_LEGACY_VOXEL);
        byte[] vdata = c.getByteArray(NBTBlobConverter.NBT_VERSIONED_VOXEL);
        final Integer blockP = c.getInt(NBTBlobConverter.NBT_PRIMARY_STATE);

        if (vdata.length == 0 && data.length > 0) {
            final VoxelBlob xx = new VoxelBlob();

            try {
                xx.fromLegacyByteArray(data);
            } catch (final IOException e) {
                // :_(
            }

            vdata = xx.blobToBytes(VoxelBlob.VERSION_COMPACT_PALLETED);
        }

        final BakedModel[] models = new BakedModel[ChiselRenderType.values().length];
        for (final ChiselRenderType l : ChiselRenderType.values()) {
            models[l.ordinal()] = getOrCreateBakedModel(blockP, new VoxelBlobStateReference(vdata, 0L).getVoxelBlob(), l, DefaultVertexFormat.BLOCK, RandomSource.create());
        }

        mdl = new ModelCombined(models);

        ITEM_TO_MODEL_CACHE.put(stack, mdl);

        return mdl;
    }

    @Override
    public void clearCache() {
        SIDE_CACHE.clear();
        MODEL_CACHE.clear();
        ITEM_TO_MODEL_CACHE.clear();
        FLUID_RENDER_TYPES.clear();

        final List<RenderType> blockRenderTypes = RenderType.chunkBufferLayers();
        for (int i = 0; i < blockRenderTypes.size(); i++) {
            RenderType renderType = blockRenderTypes.get(i);
            for (Fluid fluid : ForgeRegistries.FLUIDS) {
                RenderType renderLayer = ItemBlockRenderTypes.getRenderLayer(fluid.defaultFluidState());
                if (renderLayer == renderType) {
                    FLUID_RENDER_TYPES.set(i);
                    break;
                }
            }
        }
    }

    public static void onConfigurationReload(final ModConfigEvent.Reloading event) {
        MODEL_CACHE.changeMaxSize(ChiselsAndBits.getConfig().getClient().modelCacheSize.get());
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    private record ModelCacheKey(VoxelBlob blob, ChiselRenderType type) {

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof final ModelCacheKey that)) {
                return false;
            }
            return Objects.equals(blob, that.blob) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blob, type);
        }
    }
}
