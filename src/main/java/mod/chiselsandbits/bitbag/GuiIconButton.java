package mod.chiselsandbits.bitbag;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.MinecraftForge;

public class GuiIconButton extends Button
{
	TextureAtlasSprite icon;

	public GuiIconButton(final int x, final int y, final TextureAtlasSprite icon, Button.OnPress pressedAction, Button.OnTooltip tooltip) {
		super( x, y, 18, 18, new TextComponent(""), pressedAction, tooltip);
		this.icon = icon;
	}

    @Override
    public void renderButton(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        blit(matrixStack, x + 1, y + 1, 0, 16,16, icon);
    }
}
