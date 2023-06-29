package mod.chiselsandbits.events;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemMagnifyingGlass;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvent {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (Minecraft.getInstance().player != null && ChiselsAndBits.getConfig().getCommon().enableHelp.get())
            if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof ItemMagnifyingGlass || Minecraft.getInstance().player.getOffhandItem().getItem() instanceof ItemMagnifyingGlass)
                if (event.getItemStack().getItem() instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    BlockState blockState = block.defaultBlockState();
                    BlockBitInfo.SupportsAnalysisResult result = BlockBitInfo.doSupportAnalysis(blockState);

                    event.getToolTip().add(
                        Component.literal(
                          result.supported() ?
                            ChatFormatting.GREEN + result.supportedReason().getLocal() + ChatFormatting.RESET :
                                  ChatFormatting.RED + result.unsupportedReason().getLocal() + ChatFormatting.RESET
                        )
                    );
                }
    }
}
