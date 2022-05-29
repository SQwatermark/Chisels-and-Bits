package mod.chiselsandbits.bitbag;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.network.packets.PacketBagGui;
import mod.chiselsandbits.network.packets.PacketClearBagGui;
import mod.chiselsandbits.network.packets.PacketSortBagGui;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.GuiUtils;

import java.util.Arrays;
import java.util.List;

public class BagGui extends AbstractContainerScreen<BagContainer>
{

	private static final ResourceLocation BAG_GUI_TEXTURE = new ResourceLocation( ChiselsAndBits.MODID, "textures/gui/container/bitbag.png" );

    private static GuiBagFontRenderer specialFontRenderer = null;
	private GuiIconButton trashBtn;

    private Slot hoveredBitSlot = null;

	public BagGui(
			final BagContainer container,
            final Inventory playerInventory,
            final Component title
    )
	{
		super(container, playerInventory, title);
		imageHeight = 239;
	}

    @Override
    protected void init()
    {
        super.init();
        trashBtn = addWidget(new GuiIconButton(leftPos - 18, topPos + 0, ClientSide.trashIcon, p_onPress_1_ -> {
            if (requireConfirm)
            {
                dontThrow = true;
                if (isValidBitItem())
                {
                    requireConfirm = false;
                }
            }
            else
            {
                requireConfirm = true;
                // server side!
                ChiselsAndBits.getNetworkChannel().sendToServer(new PacketClearBagGui(getInHandItem()));
                dontThrow = false;
            }
        }, (button, poseStack, p_onTooltip_3_, p_onTooltip_4_) -> {
            if ( isValidBitItem() )
            {
                final String msgNotConfirm = ModUtil.notEmpty( getInHandItem() ) ? LocalStrings.TrashItem.getLocal( getInHandItem().getHoverName().getString() ) : LocalStrings.Trash.getLocal();
                final String msgConfirm = ModUtil.notEmpty( getInHandItem() ) ? LocalStrings.ReallyTrashItem.getLocal( getInHandItem().getHoverName().getString() ) : LocalStrings.ReallyTrash.getLocal();


                final List<Component> text = Arrays.asList( new Component[] { new TextComponent(requireConfirm ? msgNotConfirm : msgConfirm) } );
//                GuiUtils.drawHoveringText(poseStack, text, p_onTooltip_3_, p_onTooltip_4_, width, height, -1, Minecraft.getInstance().font );
            }
            else
            {
                final List<Component> text = Arrays.asList( new Component[] { new TextComponent(LocalStrings.TrashInvalidItem.getLocal( getInHandItem().getHoverName().getString() )) } );
//                GuiUtils.drawHoveringText(poseStack, text, p_onTooltip_3_, p_onTooltip_4_, width, height, -1, Minecraft.getInstance().font );
            }
        }));

        final GuiIconButton sortBtn = addWidget(new GuiIconButton(leftPos - 18, topPos + 18, ClientSide.sortIcon, new Button.OnPress()
        {
            @Override
            public void onPress(final Button p_onPress_1_)
            {
                ChiselsAndBits.getNetworkChannel().sendToServer(new PacketSortBagGui());
            }
        },
          (p_onTooltip_1_, p_onTooltip_2_, p_onTooltip_3_, p_onTooltip_4_) -> {
              final List<Component> text = Arrays.asList(new Component[] {new TextComponent(LocalStrings.Sort.getLocal())});
//              GuiUtils.drawHoveringText(p_onTooltip_2_, text, p_onTooltip_3_, p_onTooltip_4_, width, height, -1, Minecraft.getInstance().font);
          }));
    }

	BagContainer getBagContainer()
	{
		return (BagContainer) menu;
	}

    @Override
    public void render(
      final PoseStack stack,
      final int mouseX,
      final int mouseY,
      final float partialTicks )
    {
        this.renderBackground(stack);
        drawDefaultBackground(stack, partialTicks, mouseX, mouseY);
        super.render(stack, mouseX, mouseY, partialTicks );
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(
      final PoseStack stack,
      final float partialTicks,
      final int mouseX,
      final int mouseY )
    {
        final int xOffset = ( width - imageWidth ) / 2;
        final int yOffset = ( height - imageHeight ) / 2;

        RenderSystem.setShaderColor( 1.0F, 1.0F, 1.0F, 1.0F );
        RenderSystem.setShaderTexture(0, BAG_GUI_TEXTURE);
        this.blit(stack, xOffset, yOffset, 0, 0, imageWidth, imageHeight );

        if ( specialFontRenderer == null )
        {
            specialFontRenderer = new GuiBagFontRenderer( font, ChiselsAndBits.getConfig().getServer().bagStackSize.get() );
        }

        hoveredBitSlot = null;
        for ( int slotIdx = 0; slotIdx < getBagContainer().customSlots.size(); ++slotIdx )
        {
            final Slot slot = getBagContainer().customSlots.get( slotIdx );

            final Font defaultFontRenderer = font;

            try
            {
                font = specialFontRenderer;
                stack.pushPose();
                stack.translate(leftPos, topPos, 0f);
                renderSlot(stack, slot);
                stack.pushPose();
            }
            finally
            {
                font = defaultFontRenderer;
            }

            if ( isHovering( slot, mouseX, mouseY ) && slot.isActive() )
            {
                final int xDisplayPos = this.leftPos + slot.x;
                final int yDisplayPos = this.topPos + slot.y;
                hoveredBitSlot = slot;

                RenderSystem.disableDepthTest();
                RenderSystem.colorMask( true, true, true, false );
                final int INNER_SLOT_SIZE = 16;
                fillGradient(stack, xDisplayPos, yDisplayPos, xDisplayPos + INNER_SLOT_SIZE, yDisplayPos + INNER_SLOT_SIZE, -2130706433, -2130706433 );
                RenderSystem.colorMask( true, true, true, true );
                RenderSystem.enableDepthTest();
            }
        }

        if ( !trashBtn.isMouseOver(mouseX, mouseY) )
        {
            requireConfirm = true;
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button)
    {
        // This is what vanilla does...
        final boolean duplicateButton = button == Minecraft.getInstance().options.keyPickItem.getKey().getValue() + 100;

        Slot slot = getSlotUnderMouse();
        if (slot == null)
            slot = hoveredBitSlot;
        if ( slot != null && slot.container instanceof TargetedInventory)
        {
            final PacketBagGui bagGuiPacket = new PacketBagGui(slot.index, button, duplicateButton, ClientSide.instance.holdingShift());
            bagGuiPacket.doAction( ClientSide.instance.getPlayer() );

            ChiselsAndBits.getNetworkChannel().sendToServer( bagGuiPacket );

            return true;
        }

        return super.mouseClicked( mouseX, mouseY, button );
    }


	private ItemStack getInHandItem()
	{
		return getBagContainer().thePlayer.getInventory().getSelected();
	}

	boolean requireConfirm = true;
	boolean dontThrow = false;

	private boolean isValidBitItem()
	{
		return ModUtil.isEmpty( getInHandItem() ) || getInHandItem().getItem() == ModItems.ITEM_BLOCK_BIT.get();
	}

    @Override
    protected void renderLabels(final PoseStack matrixStack, final int x, final int y)
    {
        font.drawShadow(matrixStack, Language.getInstance().getVisualOrder(ModItems.ITEM_BIT_BAG_DEFAULT.get().getName( ModUtil.getEmptyStack() )), 8, 6, 0x404040 );
        font.draw(matrixStack, I18n.get( "container.inventory" ), 8, imageHeight - 93, 0x404040 );
    }

    protected void drawDefaultBackground(final PoseStack matrixStack, final float partialTicks, final int x, final int y)
    {
    }
}
