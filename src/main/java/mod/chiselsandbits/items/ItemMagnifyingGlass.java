package mod.chiselsandbits.items;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class ItemMagnifyingGlass extends Item
{

	public ItemMagnifyingGlass(Properties properties)
	{
        super(properties.stacksTo(1));
	}

	@OnlyIn(Dist.CLIENT)
    @Override
    public Component getHighlightTip(final ItemStack item, final Component displayName)
    {
        if (Minecraft.getInstance().hitResult == null)
            return displayName;

        if (Minecraft.getInstance().hitResult.getType() != HitResult.Type.BLOCK)
            return displayName;

        final BlockHitResult rayTraceResult = (BlockHitResult) Minecraft.getInstance().hitResult;
        final BlockState state = Minecraft.getInstance().level.getBlockState(rayTraceResult.getBlockPos());
        final BlockBitInfo.SupportsAnalysisResult result = BlockBitInfo.doSupportAnalysis(state);
        return new TextComponent(
          result.isSupported() ?
            ChatFormatting.GREEN + result.getSupportedReason().getLocal() + ChatFormatting.RESET :
            ChatFormatting.RED + result.getUnsupportedReason().getLocal() + ChatFormatting.RESET
        );
    }

    @Override
	@OnlyIn( Dist.CLIENT )
	public void appendHoverText(
			final ItemStack stack,
			final Level worldIn,
			final List<Component> tooltip,
			final TooltipFlag advanced )
	{
		super.appendHoverText( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpMagnifyingGlass, tooltip );
	}
}
