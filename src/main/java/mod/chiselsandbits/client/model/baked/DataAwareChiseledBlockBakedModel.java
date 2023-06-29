package mod.chiselsandbits.client.model.baked;

import mod.chiselsandbits.chiseledblock.BlockEntityChiseledBlock;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.render.ModelCombined;
import mod.chiselsandbits.render.NullBakedModel;
import mod.chiselsandbits.render.chiseledblock.ChiselRenderType;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBakedModel;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class DataAwareChiseledBlockBakedModel extends BaseSmartModel {
    private final ModelProperty<BakedModel> MODEL_PROP = new ModelProperty<>();

    @Override
    public boolean usesBlockLight()
    {
        return true;
    }

    @Override
    public BakedModel handleBlockState(final BlockState state, final Random random, final ModelData modelData)
    {
        if (!modelData.has(MODEL_PROP))
            return NullBakedModel.instance;

        return modelData.get(MODEL_PROP);
    }

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

        if (world.getBlockEntity(pos) == null) {
            return ModelData.builder().build();
        }

        // This seems silly, but it proves to be faster in practice.
        VoxelBlobStateReference data = modelData.get(BlockEntityChiseledBlock.MP_VBSR);
        Integer blockP = modelData.get(BlockEntityChiseledBlock.MP_PBSI);
        blockP = blockP == null ? 0 : blockP;

        final RenderType layer = net.minecraftforge.client.MinecraftForgeClient.getRenderType();

        if (layer == null)
        {
            final ChiseledBlockBakedModel[] models = new ChiseledBlockBakedModel[ChiselRenderType.values().length];
            int o = 0;

            for (final ChiselRenderType l : ChiselRenderType.values())
            {
                models[o++] = ChiseledBlockSmartModel.getCachedModel(
                  (BlockEntityChiseledBlock) Objects.requireNonNull(world.getBlockEntity(pos)),
                  l);
            }

            return ModelData.builder().with(MODEL_PROP, new ModelCombined(models)).build();
        }

        BakedModel baked;
        if (RenderType.chunkBufferLayers().contains(layer) && ChiseledBlockSmartModel.FLUID_RENDER_TYPES.get(RenderType.chunkBufferLayers().indexOf(layer)))
        {
            final ChiseledBlockBakedModel a = ChiseledBlockSmartModel.getCachedModel(
              (BlockEntityChiseledBlock) Objects.requireNonNull(world.getBlockEntity(pos)),
              ChiselRenderType.fromLayer(layer, false));
            final ChiseledBlockBakedModel b = ChiseledBlockSmartModel.getCachedModel(
              (BlockEntityChiseledBlock) Objects.requireNonNull(world.getBlockEntity(pos)),
              ChiselRenderType.fromLayer(layer, true));

            if (a.isEmpty())
            {
                baked = b;
            }
            else if (b.isEmpty())
            {
                baked = a;
            }
            else
            {
                baked = new ModelCombined(a, b);
            }
        }
        else
        {
            baked = ChiseledBlockSmartModel.getCachedModel(
              (BlockEntityChiseledBlock) Objects.requireNonNull(world.getBlockEntity(pos)),
              ChiselRenderType.fromLayer(layer, false));
        }

        return ModelData.builder().with(MODEL_PROP, baked).build();
    }

    @Override
    public BakedModel resolve(
      final BakedModel originalModel, final ItemStack stack, final Level world, final LivingEntity entity)
    {
        final ChiseledBlockBakedModel a = ChiseledBlockSmartModel.getCachedModel(
          stack,
          ChiselRenderType.fromLayer(MinecraftForgeClient.getRenderType(), false));
        final ChiseledBlockBakedModel b = ChiseledBlockSmartModel.getCachedModel(
          stack,
          ChiselRenderType.fromLayer(MinecraftForgeClient.getRenderType(), true));

        if (a.isEmpty())
        {
            return b;
        }
        else if (b.isEmpty())
        {
            return a;
        }
        else
        {
            return new ModelCombined(a, b);
        }
    }
}
