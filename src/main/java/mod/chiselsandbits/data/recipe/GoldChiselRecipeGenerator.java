package mod.chiselsandbits.data.recipe;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GoldChiselRecipeGenerator extends AbstractChiselRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new GoldChiselRecipeGenerator(event.getGenerator()));
    }

    private GoldChiselRecipeGenerator(
      final DataGenerator generator)
    {
        super(generator, ModItems.ITEM_CHISEL_GOLD.get(), Tags.Items.INGOTS_GOLD);
    }
}
