package mod.chiselsandbits.data.recipe;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemNegativePrint;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NegativePrintRecipeGenerator extends AbstractPrintRecipeGenerator<ItemNegativePrint>
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new NegativePrintRecipeGenerator(event.getGenerator()));
    }

    private NegativePrintRecipeGenerator(
      final DataGenerator generator
    )
    {
        super(generator,
          ModItems.ITEM_NEGATIVE_PRINT.get(),
          ModItems.ITEM_NEGATIVE_PRINT_WRITTEN.get(),
          Tags.Items.DUSTS_REDSTONE
          );
    }
}
