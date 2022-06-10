package mod.chiselsandbits.events;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityItemPickupEventHandler
{

    @SubscribeEvent
    public static void pickupItems(EntityItemPickupEvent event) {

        boolean modified = false;

        final ItemEntity entityItem = event.getItem();
        if (entityItem != null)
        {
            final ItemStack is = entityItem.getItem();
            final Player player = event.getPlayer();
            if (is.getItem() instanceof ItemChiseledBit)
            {
                final int originalSize = ModUtil.getStackSize( is );

                if ( ModUtil.getStackSize( is ) > is.getMaxStackSize() && entityItem.isAlive() )
                {
                    final ItemStack singleStack = is.copy();
                    ModUtil.setStackSize( singleStack, singleStack.getMaxStackSize() );

                    if (!player.getInventory().add(singleStack))
                    {
                        ModUtil.adjustStackSize( is, -( singleStack.getMaxStackSize() - ModUtil.getStackSize( is ) ) );
                    }

                    modified = updateEntity( player, entityItem, is, originalSize ) || modified;
                }
                else
                {
                    return;
                }
            }
        }

        if ( modified )
        {
            event.setCanceled( true );
        }
    }

    private static boolean updateEntity(
      final Player player,
      final ItemEntity ei,
      ItemStack is,
      final int originalSize )
    {
        if ( is == null )
        {
            ei.remove(Entity.RemovalReason.DISCARDED);
            return true;
        }
        else
        {
            final int changed = ModUtil.getStackSize( is ) - ModUtil.getStackSize( ei.getItem() );
            ei.setItem( is );
            return changed != 0;
        }
    }
}
