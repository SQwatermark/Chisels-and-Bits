package mod.chiselsandbits.bitbag;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.NullInventory;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.network.packets.PacketBagGuiStack;
import mod.chiselsandbits.registry.ModContainerTypes;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class BagInventoryMenu extends AbstractContainerMenu
{
	static final int OUTER_SLOT_SIZE = 18;

	final Player thePlayer;
	final TargetedInventory visibleInventory = new TargetedInventory();

	BagInventory bagInv;
	SlotReadonly bagSlot;

	final public List<Slot> customSlots = new ArrayList<>();
	final public List<ItemStack> customSlotsItems = new ArrayList<>();

	private void addCustomSlot(final SlotBit newSlot) {
		newSlot.index = customSlots.size();
		customSlots.add(newSlot);
		customSlotsItems.add(ModUtil.getEmptyStack());
	}

    public BagInventoryMenu(final int id, final Inventory playerInventory)
    {
        super(ModContainerTypes.BAG_CONTAINER.get(), id);
        thePlayer = playerInventory.player;

        final int playerInventoryOffset = ( 7 - 4 ) * OUTER_SLOT_SIZE;

        final ItemStack stack = thePlayer.getMainHandItem();
        setBag(stack);

        for ( int yOffset = 0; yOffset < 7; ++yOffset )
        {
            for ( int xOffset = 0; xOffset < 9; ++xOffset )
            {
                addCustomSlot( new SlotBit( visibleInventory, xOffset + yOffset * 9, 8 + xOffset * OUTER_SLOT_SIZE, 18 + yOffset * OUTER_SLOT_SIZE ) );
            }
        }

        for ( int xPlayerInventory = 0; xPlayerInventory < 3; ++xPlayerInventory )
        {
            for ( int yPlayerInventory = 0; yPlayerInventory < 9; ++yPlayerInventory )
            {
                addSlot( new Slot( thePlayer.getInventory(), yPlayerInventory + xPlayerInventory * 9 + 9, 8 + yPlayerInventory * OUTER_SLOT_SIZE, 104 + xPlayerInventory * OUTER_SLOT_SIZE + playerInventoryOffset ) );
            }
        }

        for ( int xToolbar = 0; xToolbar < 9; ++xToolbar )
        {
            if ( thePlayer.getInventory().selected == xToolbar )
            {
                addSlot( bagSlot = new SlotReadonly( thePlayer.getInventory(), xToolbar, 8 + xToolbar * OUTER_SLOT_SIZE, 162 + playerInventoryOffset ) );
            }
            else
            {
                addSlot( new Slot( thePlayer.getInventory(), xToolbar, 8 + xToolbar * OUTER_SLOT_SIZE, 162 + playerInventoryOffset ) );
            }
        }
    }


	private void setBag(final ItemStack bagItem) {

		final Container inv;

		if ( ModUtil.notEmpty( bagItem ) && bagItem.getItem() instanceof ItemBitBag ) {
			inv = bagInv = new BagInventory( bagItem );
		} else {
			bagInv = null;
			inv = new NullInventory( BagStorage.BAG_STORAGE_SLOTS );
		}

		visibleInventory.setInventory( inv );
	}

	@Override
	public boolean stillValid(final Player playerIn) {
		return bagInv != null && playerIn == thePlayer && hasBagInHand( thePlayer );
	}

	private boolean hasBagInHand(final Player player) {
		if ( bagInv.getItemStack() != player.getMainHandItem() )
		{
			setBag( player.getMainHandItem() );
		}

		return bagInv != null && bagInv.getItemStack().getItem() instanceof ItemBitBag;
	}

	@Override
	public ItemStack quickMoveStack(final Player playerIn, final int index) {
		return transferStack( index, true );
	}

	private ItemStack transferStack(final int index, final boolean normalToBag ) {
		ItemStack someReturnValue = ModUtil.getEmptyStack();
		boolean reverse = true;

		final TargetedTransferContainer helper = new TargetedTransferContainer();

		if ( !normalToBag )
		{
		    helper.slots.clear();
		    helper.slots.addAll(customSlots);
		}
		else
		{
		    helper.slots.clear();
		    helper.slots.addAll(slots);
			reverse = false;
		}

		final Slot slot = helper.slots.get( index );

		if ( slot != null && slot.hasItem() )
		{
			final ItemStack transferStack = slot.getItem();
			someReturnValue = transferStack.copy();

			int extraItems = 0;
			if ( ModUtil.getStackSize( transferStack ) > transferStack.getMaxStackSize() )
			{
				extraItems = ModUtil.getStackSize( transferStack ) - transferStack.getMaxStackSize();
				ModUtil.setStackSize( transferStack, transferStack.getMaxStackSize() );
			}

			if ( normalToBag )
			{
                helper.slots.clear();
                helper.slots.addAll(customSlots);
				ItemChiseledBit.bitBagStackLimitHack = true;
			}
			else
			{
                helper.slots.clear();
                helper.slots.addAll(slots);
			}

			try
			{
				if ( !helper.doMergeItemStack( transferStack, 0, helper.slots.size(), reverse ) )
				{
					return ModUtil.getEmptyStack();
				}
			}
			finally
			{
				// add the extra items back on...
				ModUtil.adjustStackSize( transferStack, extraItems );
				ItemChiseledBit.bitBagStackLimitHack = false;
			}

			if ( ModUtil.getStackSize( transferStack ) == 0 )
			{
				slot.set( ModUtil.getEmptyStack() );
			}
			else
			{
				slot.setChanged();
			}
		}

		return someReturnValue;
	}

	@OnlyIn(Dist.CLIENT)
	public static Object getGuiClass()
	{
		return BagInventoryScreen.class;
	}

	public void handleCustomSlotAction(
			final int slotNumber,
			final int mouseButton,
			final boolean duplicateButton,
			final boolean holdingShift )
	{
		final Slot slot = customSlots.get( slotNumber );
		final ItemStack held = thePlayer.getInventory().getSelected();
		final ItemStack slotStack = slot.getItem();

		if ( duplicateButton && thePlayer.isCreative() )
		{
			if ( slot.hasItem() && ModUtil.isEmpty( held ) )
			{
				final ItemStack is = slot.getItem().copy();
				ModUtil.setStackSize( is, is.getMaxStackSize() );
				thePlayer.getInventory().setPickedItem(is);
			}
		}
		else if ( holdingShift )
		{
			if ( slotStack != null )
			{
				transferStack( slotNumber, false );
			}
		}
		else if ( mouseButton == 0 && !duplicateButton )
		{
			if ( ModUtil.isEmpty( held ) && slot.hasItem() )
			{
				final ItemStack pulled = ModUtil.copy( slotStack );
				ModUtil.setStackSize( pulled, Math.min( pulled.getMaxStackSize(), ModUtil.getStackSize( pulled ) ) );

				final ItemStack newStackSlot = ModUtil.copy( slotStack );
				ModUtil.setStackSize( newStackSlot, ModUtil.getStackSize( pulled ) >= ModUtil.getStackSize( slotStack ) ? 0 : ModUtil.getStackSize( slotStack ) - ModUtil.getStackSize( pulled ) );

				slot.set( ModUtil.getStackSize( newStackSlot ) <= 0 ? ModUtil.getEmptyStack() : newStackSlot );
				thePlayer.getInventory().setPickedItem( pulled );
			}
			else if ( ModUtil.notEmpty( held ) && slot.hasItem() && slot.mayPlace( held ) )
			{
				if ( held.getItem() == slotStack.getItem() && held.getDamageValue() == slotStack.getDamageValue() && ItemStack.tagMatches( held, slotStack ) )
				{
					final ItemStack newStackSlot = ModUtil.copy( slotStack );
					ModUtil.adjustStackSize( newStackSlot, ModUtil.getStackSize( held ) );
					int held_stackSize = 0;

					if ( ModUtil.getStackSize( newStackSlot ) > slot.getMaxStackSize() )
					{
						held_stackSize = ModUtil.getStackSize( newStackSlot ) - slot.getMaxStackSize();
						ModUtil.adjustStackSize( newStackSlot, -held_stackSize );
					}

					slot.set( newStackSlot );
					ModUtil.setStackSize( held, held_stackSize );
					thePlayer.getInventory().setPickedItem( held );
				}
				else
				{
					if ( ModUtil.notEmpty( held ) && slot.hasItem() && ModUtil.getStackSize( slotStack ) <= slotStack.getMaxStackSize() )
					{
						slot.set( held );
						thePlayer.getInventory().setPickedItem( slotStack );
					}
				}
			}
			else if ( ModUtil.notEmpty( held ) && !slot.hasItem() && slot.mayPlace( held ) )
			{
				slot.set( held );
				thePlayer.getInventory().setPickedItem( ModUtil.getEmptyStack() );
			}
		}
		else if ( mouseButton == 1 && !duplicateButton )
		{
			if ( ModUtil.isEmpty( held ) && slot.hasItem() )
			{
				final ItemStack pulled = ModUtil.copy( slotStack );
				ModUtil.setStackSize( pulled, Math.max( 1, ( Math.min( pulled.getMaxStackSize(), ModUtil.getStackSize( pulled ) ) + 1 ) / 2 ) );

				final ItemStack newStackSlot = ModUtil.copy( slotStack );
				ModUtil.setStackSize( newStackSlot, ModUtil.getStackSize( pulled ) >= ModUtil.getStackSize( slotStack ) ? 0 : ModUtil.getStackSize( slotStack ) - ModUtil.getStackSize( pulled ) );

				slot.set( ModUtil.getStackSize( newStackSlot ) <= 0 ? ModUtil.getEmptyStack() : newStackSlot );
				thePlayer.getInventory().setPickedItem( pulled );
			}
			else if ( ModUtil.notEmpty( held ) && slot.hasItem() && slot.mayPlace( held ) )
			{
				if ( held.getItem() == slotStack.getItem() && held.getDamageValue() == slotStack.getDamageValue() && ItemStack.tagMatches( held, slotStack ) )
				{
					final ItemStack newStackSlot = ModUtil.copy( slotStack );
					ModUtil.adjustStackSize( newStackSlot, 1 );
					int held_quantity = ModUtil.getStackSize( held ) - 1;

					if ( ModUtil.getStackSize( newStackSlot ) > slot.getMaxStackSize() )
					{
						final int diff = ModUtil.getStackSize( newStackSlot ) - slot.getMaxStackSize();
						held_quantity += diff;
						ModUtil.adjustStackSize( newStackSlot, -diff );
					}

					slot.set( newStackSlot );
					ModUtil.setStackSize( held, held_quantity );
					thePlayer.getInventory().setPickedItem( ModUtil.notEmpty( held ) ? held : ModUtil.getEmptyStack() );
				}
			}
			else if ( ModUtil.notEmpty( held ) && !slot.hasItem() && slot.mayPlace( held ) )
			{
				final ItemStack newStackSlot = ModUtil.copy( held );
				ModUtil.setStackSize( newStackSlot, 1 );
				ModUtil.adjustStackSize( ModUtil.nonNull( held ), -1 );

				slot.set( newStackSlot );
				thePlayer.getInventory().setPickedItem( ModUtil.notEmpty( held ) ? held : ModUtil.getEmptyStack() );
			}
		}
	}

	@Override
	public void broadcastChanges()
	{
		super.broadcastChanges();

		for ( int slotIdx = 0; slotIdx < customSlots.size(); ++slotIdx )
		{
			final ItemStack realStack = customSlots.get( slotIdx ).getItem();
			ItemStack clientstack = customSlotsItems.get( slotIdx );

			if ( !ItemStack.matches( clientstack, realStack ) )
			{
				clientstack = ModUtil.isEmpty( realStack ) ? ModUtil.getEmptyStack() : realStack.copy();
				customSlotsItems.set( slotIdx, clientstack );

				for ( int crafterIndex = 0; crafterIndex < containerListeners.size(); ++crafterIndex )
				{
					final ContainerListener cl = containerListeners.get( crafterIndex );

					if ( cl instanceof ServerPlayer)
					{
						final PacketBagGuiStack pbgs = new PacketBagGuiStack(slotIdx, clientstack);
                        ChiselsAndBits.getNetworkChannel().sendToPlayer(pbgs, (ServerPlayer) cl);
					}
				}
			}
		}
	}

	public void clear(final ItemStack stack)
	{
		if ( ModUtil.notEmpty( stack ) && stack.getItem() == ModItems.ITEM_BLOCK_BIT.get() )
		{
			if ( bagInv.matches( stack, thePlayer.getInventory().getSelected() ) )
			{
				thePlayer.getInventory().setPickedItem( ModUtil.getEmptyStack() );
			}
		}

		bagInv.clear( stack );
		this.broadcastChanges(); // TODO
//		( (ServerPlayer) thePlayer ).refreshContainer( this );
	}

	public void sort()
	{
		bagInv.sort();
		this.broadcastChanges();
//		( (ServerPlayer) thePlayer ).refreshContainer( this );
	}

}
