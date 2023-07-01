package mod.chiselsandbits.render.bit;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.client.model.baked.BaseSmartModel;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.events.TickHandler;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.render.ModelCombined;
import mod.chiselsandbits.render.chiseledblock.ChiselRenderType;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.EmptyModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

// TODO 这是什么
public class BitItemSmartModel extends BaseSmartModel implements ICacheClearable {
    static private final HashMap<Integer, BakedModel> modelCache = new HashMap<>();
    static private final HashMap<Integer, BakedModel> largeModelCache = new HashMap<>();

    static private final NonNullList<ItemStack> alternativeStacks = NonNullList.create();

    RandomSource random = RandomSource.create();

    private BakedModel getCachedModel(int stateID, boolean large) {
        if (stateID == 0) {
            //We are running an empty bit, for display purposes.
            //Lets loop:
            if (alternativeStacks.isEmpty()) {
                return EmptyModel.BAKED;
//                ModItems.ITEM_BLOCK_BIT.get().fillItemCategory(Objects.requireNonNull(ModItems.ITEM_BLOCK_BIT.get().getItemCategory()), alternativeStacks); TODO
            }

            final int alternativeIndex = (int) ((Math.floor(TickHandler.getClientTicks() / 20d)) % alternativeStacks.size());

            stateID = ItemChiseledBit.getStackState(alternativeStacks.get(alternativeIndex));
        }

        final HashMap<Integer, BakedModel> target = large ? largeModelCache : modelCache;
        BakedModel out = target.get(stateID);

        if (out == null) {
            if (large) {
                // TODO 应该有很大优化空间
                List<BakedModel> list = new ObjectArrayList<>();
                final VoxelBlob blob = new VoxelBlob();
                for (RenderType layer : RenderType.chunkBufferLayers()) {
                    blob.fill(stateID); // TODO 找出为什么它读取一次就变成0了
                    list.add(ChiseledBlockSmartModel.getOrCreateBakedModel(stateID, blob,
                            ChiselRenderType.fromLayer(layer, false), DefaultVertexFormat.BLOCK, random));
                    blob.fill(stateID);
                    list.add(ChiseledBlockSmartModel.getOrCreateBakedModel(stateID, blob,
                            ChiselRenderType.fromLayer(layer, true), DefaultVertexFormat.BLOCK, random));
                }
                out = new ModelCombined(list.toArray(new BakedModel[0]));
            } else {
                out = new BitItemBaked(stateID);
            }

            target.put(stateID, out);
        }

        return out;
    }

    public BakedModel resolve(
            final BakedModel originalModel,
            final ItemStack stack,
            final Level world,
            final LivingEntity entity) {
        return getCachedModel(ItemChiseledBit.getStackState(stack), ClientSide.instance.holdingShift());
    }

    @Override
    public void clearCache() {
        modelCache.clear();
        largeModelCache.clear();
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }
}
