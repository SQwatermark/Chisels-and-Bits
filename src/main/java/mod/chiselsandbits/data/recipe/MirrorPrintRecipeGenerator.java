package mod.chiselsandbits.data.recipe;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemMirrorPrint;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MirrorPrintRecipeGenerator extends AbstractPrintRecipeGenerator<ItemMirrorPrint>
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new MirrorPrintRecipeGenerator(event.getGenerator()));
    }

    private MirrorPrintRecipeGenerator(
      final DataGenerator generator
    )
    {
        super(generator,
          ModItems.ITEM_MIRROR_PRINT.get(),
          ModItems.ITEM_MIRROR_PRINT_WRITTEN.get(),
          Tags.Items.DUSTS_GLOWSTONE
          );
    }
}
