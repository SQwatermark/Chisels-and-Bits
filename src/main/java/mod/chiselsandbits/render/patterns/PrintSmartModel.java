package mod.chiselsandbits.render.patterns;

import java.util.WeakHashMap;

import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.client.model.baked.BaseSmartModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PrintSmartModel extends BaseSmartModel {

    WeakHashMap<ItemStack, PrintBaked> cache = new WeakHashMap<>();

    final IPatternItem item;
    final String name;

    public PrintSmartModel(
            final String name,
            final IPatternItem item) {
        this.name = name;
        this.item = item;
    }

    @Override
    public BakedModel resolve(final BakedModel originalModel, final ItemStack stack, final Level world, final LivingEntity entity) {
        if (ClientSide.instance.holdingShift()) {
            PrintBaked npb = cache.get(stack);

            if (npb == null) {
                cache.put(stack, npb = new PrintBaked(name, item, stack));
            }

            return npb;
        }

        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager().getModel(new ModelResourceLocation("chiselsandbits", name + "_written", "inventory"));
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
