package mod.chiselsandbits.data.recipe;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemPositivePrint;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PositivePrintRecipeGenerator extends AbstractPrintRecipeGenerator<ItemPositivePrint>
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new PositivePrintRecipeGenerator(event.getGenerator()));
    }

    private PositivePrintRecipeGenerator(
      final DataGenerator generator
    )
    {
        super(generator,
          ModItems.ITEM_POSITIVE_PRINT.get(),
          ModItems.ITEM_POSITIVE_PRINT_WRITTEN.get(),
          Tags.Items.GEMS_LAPIS
          );
    }
}
