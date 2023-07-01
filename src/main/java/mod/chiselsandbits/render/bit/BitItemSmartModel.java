package mod.chiselsandbits.render.bit;

import java.util.HashMap;
import java.util.Objects;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.events.TickHandler;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.client.model.baked.BaseSmartModel;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.render.ModelCombined;
import mod.chiselsandbits.render.chiseledblock.ChiselRenderType;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.EmptyModel;

// TODO 这是什么
public class BitItemSmartModel extends BaseSmartModel implements ICacheClearable {
    static private final HashMap<Integer, BakedModel> modelCache = new HashMap<>();
    static private final HashMap<Integer, BakedModel> largeModelCache = new HashMap<>();

    static private final NonNullList<ItemStack> alternativeStacks = NonNullList.create();

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
                final VoxelBlob blob = new VoxelBlob();
                blob.fill(stateID);
                final BakedModel a = new ChiseledBlockBakedModel(stateID, ChiselRenderType.SOLID, blob, DefaultVertexFormat.BLOCK);
                final BakedModel b = new ChiseledBlockBakedModel(stateID, ChiselRenderType.SOLID_FLUID, blob, DefaultVertexFormat.BLOCK);
                final BakedModel c = new ChiseledBlockBakedModel(stateID, ChiselRenderType.CUTOUT_MIPPED, blob, DefaultVertexFormat.BLOCK);
                final BakedModel d = new ChiseledBlockBakedModel(stateID, ChiselRenderType.CUTOUT, blob, DefaultVertexFormat.BLOCK);
                final BakedModel e = new ChiseledBlockBakedModel(stateID, ChiselRenderType.TRANSLUCENT, blob, DefaultVertexFormat.BLOCK);
                out = new ModelCombined(a, b, c, d, e);
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
