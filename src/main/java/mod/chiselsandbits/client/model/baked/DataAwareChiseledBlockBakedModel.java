package mod.chiselsandbits.client.model.baked;

import mod.chiselsandbits.chiseledblock.BlockEntityChiseledBlock;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.render.ModelCombined;
import mod.chiselsandbits.render.NullBakedModel;
import mod.chiselsandbits.render.chiseledblock.ChiselRenderType;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBakedModel;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * 雕刻模型烘焙的入口
 */
public class DataAwareChiseledBlockBakedModel implements BakedModel {
    private final ModelProperty<BakedModel> MODEL_PROP = new ModelProperty<>();

    private final ItemOverrides overrides = new ItemOverrides() {
        @Override
        public @NotNull BakedModel resolve(@NotNull BakedModel pModel, @NotNull ItemStack stack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
//            ChiseledBlockBakedModel a = ChiseledBlockSmartModel.getOrCreateBakedModel(
//                    stack,
//                    ChiselRenderType.fromLayer(MinecraftForgeClient.getRenderType(), false));
//            ChiseledBlockBakedModel b = ChiseledBlockSmartModel.getOrCreateBakedModel(
//                    stack,
//                    ChiselRenderType.fromLayer(MinecraftForgeClient.getRenderType(), true));
//
//            if (a.isEmpty()) {
//                return b;
//            } else if (b.isEmpty()) {
//                return a;
//            } else {
//                return new ModelCombined(a, b);
//            }
            return EmptyModel.BAKED;
        }
    };

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.STONE.defaultBlockState());
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        BakedModel bakedModel = null;
        if (data.has(MODEL_PROP)) {
            bakedModel = data.get(MODEL_PROP);
        }
        if (bakedModel == null) {
            bakedModel = NullBakedModel.INSTANCE;
        }
        return bakedModel.getQuads(state, side, rand, data, renderType);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    /**
     * 从方块信息中获取ModelData
     * <p>
     * 这里直接把烘焙或者缓存的方块模型放进ModelData中
     * @param modelData 似乎是方块实体的getModelData()的返回值，没有方块实体则是ModelData.EMPTY
     */
    @NotNull
    @Override
    public ModelData getModelData(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
/*        final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel(
          (TileEntityBlockChiseled) Objects.requireNonNull(world.getTileEntity(pos)),
          ChiselRenderType.fromLayer(
            MinecraftForgeClient.getRenderLayer(),
            false
          )
        );*/

        // 有雕刻方块却没有方块实体，为什么会出现这种情况？
        if (world.getBlockEntity(pos) == null) {
            return modelData;
        }

        // 未能理解
        // This seems silly, but it proves to be faster in practice.
        VoxelBlobStateReference data = modelData.get(BlockEntityChiseledBlock.MP_VBSR);
        Integer stateID = modelData.get(BlockEntityChiseledBlock.MP_PBSI);
        stateID = stateID == null ? 0 : stateID;

        // TODO 此方法中返回模型含有的所有renderType和相应的模型quad信息
        final RenderType layer = net.minecraftforge.client.MinecraftForgeClient.getRenderType();

        if (layer == null) {
            final ChiseledBlockBakedModel[] models = new ChiseledBlockBakedModel[ChiselRenderType.values().length];
            int o = 0;

            // 获取每个渲染类型缓存的模型
            for (final ChiselRenderType l : ChiselRenderType.values()) {
                models[o++] = ChiseledBlockSmartModel.getOrCreateBakedModel(
                        (BlockEntityChiseledBlock) Objects.requireNonNull(world.getBlockEntity(pos)),
                        l);
            }

            return ModelData.builder().with(MODEL_PROP, new ModelCombined(models)).build();
        }

        BakedModel baked;
        if (RenderType.chunkBufferLayers().contains(layer) && ChiseledBlockSmartModel.FLUID_RENDER_TYPES.get(RenderType.chunkBufferLayers().indexOf(layer))) {
            // 分别获取流体和方块的在该渲染层的缓存的模型
            ChiseledBlockBakedModel a = ChiseledBlockSmartModel.getOrCreateBakedModel(
                    (BlockEntityChiseledBlock) Objects.requireNonNull(world.getBlockEntity(pos)),
                    ChiselRenderType.fromLayer(layer, false));
            ChiseledBlockBakedModel b = ChiseledBlockSmartModel.getOrCreateBakedModel(
                    (BlockEntityChiseledBlock) Objects.requireNonNull(world.getBlockEntity(pos)),
                    ChiselRenderType.fromLayer(layer, true));

            if (a.isEmpty()) {
                baked = b;
            } else if (b.isEmpty()) {
                baked = a;
            } else {
                baked = new ModelCombined(a, b);
            }
        } else {
            baked = ChiseledBlockSmartModel.getOrCreateBakedModel(
                    (BlockEntityChiseledBlock) Objects.requireNonNull(world.getBlockEntity(pos)),
                    ChiselRenderType.fromLayer(layer, false));
        }

        return ModelData.builder().with(MODEL_PROP, baked).build();
    }

}
