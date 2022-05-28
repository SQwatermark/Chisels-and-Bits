package mod.chiselsandbits.data.tags;

import com.google.common.collect.Lists;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForcedChiselableTagGenerator extends AbstractChiselableTagGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new ForcedChiselableTagGenerator(event.getGenerator()));
    }

    private ForcedChiselableTagGenerator(final DataGenerator generator)
    {
        super(generator,
          Mode.FORCED,
          Lists.newArrayList(Blocks.GRASS_BLOCK));
    }
}
