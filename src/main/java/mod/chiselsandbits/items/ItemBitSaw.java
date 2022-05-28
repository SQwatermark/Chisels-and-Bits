package mod.chiselsandbits.items;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static net.minecraft.world.item.Tiers.*;

public class ItemBitSaw extends Item
{

	public ItemBitSaw(Tier tier, Item.Properties properties)
	{
        super(setupDamageStack(tier, properties.stacksTo(1)));
	}


    private static Item.Properties setupDamageStack(Tier material, Item.Properties properties) {
        long uses = 1;
        if (DIAMOND.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().diamondSawUses.get();
        }
        else if (GOLD.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().goldSawUses.get();
        }
        else if (IRON.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().ironSawUses.get();
        }
        else if (STONE.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().stoneSawUses.get();
        }
        else if (NETHERITE.equals(material))
        {
            uses = ChiselsAndBits.getConfig().getServer().netheriteSawUses.get();
        }

        return properties.durability(ChiselsAndBits.getConfig().getServer().damageTools.get() ? (int) Math.max( 0, uses ) : 0);
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
		ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpBitSaw, tooltip );
	}

	@Override
	public ItemStack getContainerItem(
			final ItemStack itemStack )
	{
		if ( ChiselsAndBits.getConfig().getServer().damageTools.get() )
		{
			itemStack.setDamageValue( itemStack.getDamageValue() + 1 );
			if (itemStack.getDamageValue() == itemStack.getMaxDamage())
            {
                return ItemStack.EMPTY;
            }
		}

		return itemStack.copy();
	}

	@Override
	public boolean hasCraftingRemainingItem()
	{
		return true;
	}

}
