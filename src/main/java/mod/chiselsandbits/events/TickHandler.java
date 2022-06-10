package mod.chiselsandbits.events;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickHandler {

    private static long clientTicks = 0;

    @SubscribeEvent
    public static void onTickClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            clientTicks++;
        }
    }

    public static long getClientTicks()
    {
        return clientTicks;
    }
}
