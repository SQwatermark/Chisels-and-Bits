package mod.chiselsandbits.printer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChiselPrinterScreen extends AbstractContainerScreen<ChiselPrinterContainer>
{

    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(Constants.MOD_ID, "textures/gui/container/chisel_printer.png");

    public ChiselPrinterScreen(final ChiselPrinterContainer screenContainer, final Inventory inv, final Component titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init()
    {
        this.imageWidth = 176;
        this.imageHeight = 166;

        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(@NotNull final PoseStack matrixStack, final float partialTicks, final int x, final int y)
    {
        renderBackground(matrixStack);

        //noinspection deprecation Required.
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        this.blit(matrixStack, this.leftPos, this.topPos,0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.getToolStack().isEmpty())
            return;

        // TODO
        this.itemRenderer.renderAndDecorateItem(Objects.requireNonNull(this.minecraft.player), this.menu.getToolStack(), this.leftPos + 81, this.topPos + 47, 0);

        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int scaledProgress = this.menu.getChiselProgressionScaled();
        matrixStack.pushPose();
        matrixStack.translate(0,0,400);
        this.blit(matrixStack, this.leftPos + 73 + 10 + scaledProgress, this.topPos + 49, this.imageWidth + scaledProgress, 0, 16 - scaledProgress, 16);
        matrixStack.popPose();
    }
}
