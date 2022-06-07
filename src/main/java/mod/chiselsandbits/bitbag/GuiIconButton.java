package mod.chiselsandbits.bitbag;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.MinecraftForge;

public class GuiIconButton extends Button
{
    private final ResourceLocation resourceLocation;

	public GuiIconButton(final int x, final int y, final ResourceLocation resourceLocation, Button.OnPress pressedAction, Button.OnTooltip tooltip) {
		super( x, y, 18, 18, new TextComponent(""), pressedAction, tooltip);
		this.resourceLocation = resourceLocation;
	}

    @Override
    public void renderButton(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.resourceLocation);

        blit(matrixStack, this.x, this.y, 0f, 0f, this.width, this.height, 18, 18);
        if (this.isHovered) {
            this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
