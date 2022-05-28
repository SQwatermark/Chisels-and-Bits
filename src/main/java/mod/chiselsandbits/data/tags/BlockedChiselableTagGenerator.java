package mod.chiselsandbits.data.tags;

import com.google.common.collect.Lists;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockedChiselableTagGenerator extends AbstractChiselableTagGenerator
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new BlockedChiselableTagGenerator(event.getGenerator()));
    }

    private BlockedChiselableTagGenerator(final DataGenerator generator)
    {
        super(generator, Mode.BLOCKED, Lists.newArrayList());
    }
}
