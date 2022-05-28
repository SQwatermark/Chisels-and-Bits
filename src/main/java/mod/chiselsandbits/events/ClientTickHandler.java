package mod.chiselsandbits.events;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemMagnifyingGlass;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTickHandler {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTickPlayerTick(final TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().player != null) {
            if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof ItemMagnifyingGlass
                    || Minecraft.getInstance().player.getOffhandItem().getItem() instanceof ItemMagnifyingGlass) {
                if (Minecraft.getInstance().gui != null) {
                    Minecraft.getInstance().gui.toolHighlightTimer = 40;
                }
            }
        }
    }
}
