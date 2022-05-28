package mod.chiselsandbits.data.recipe;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class IronChiselRecipeGenerator extends AbstractChiselRecipeGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new IronChiselRecipeGenerator(event.getGenerator()));
    }

    private IronChiselRecipeGenerator(
      final DataGenerator generator)
    {
        super(generator, ModItems.ITEM_CHISEL_IRON.get(), Tags.Items.INGOTS_IRON);
    }
}
