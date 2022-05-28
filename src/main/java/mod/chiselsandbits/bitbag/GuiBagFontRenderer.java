package mod.chiselsandbits.bitbag;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.math.Matrix4f;

public class GuiBagFontRenderer extends Font
{
	Font talkto;

	int offsetX, offsetY;
	float scale;

	public GuiBagFontRenderer(
			final Font src,
			final int bagStackSize )
	{
		super(src.fonts);
		talkto = src;

		if ( bagStackSize < 100 )
		{
			scale = 1f;
		}
		else if ( bagStackSize >= 100 )
		{
			scale = 0.75f;
			offsetX = 3;
			offsetY = 2;
		}
	}

	@Override
	public int width(
			String text )
	{
		text = convertText( text );
		return talkto.width( text );
	}

    @Override
    public int drawInternal(final String text, float x, float y, final int color, final Matrix4f matrix, final boolean dropShadow, final boolean p_228078_7_)
    {
        final PoseStack stack = new PoseStack();
        final Matrix4f original = new Matrix4f(matrix);

        try {
            stack.last().pose().multiply(matrix);
            stack.scale( scale, scale, scale );

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return super.drawInternal(text, x, y, color, stack.last().pose(), dropShadow, p_228078_7_);
        }
        finally
        {
            matrix.load(original);
        }
    }

    @Override
    public int drawInBatch(
      final String text,
      float x,
      float y,
      final int color,
      final boolean dropShadow,
      final Matrix4f matrix,
      final MultiBufferSource buffer,
      final boolean transparentIn,
      final int colorBackgroundIn,
      final int packedLight)
    {
        final PoseStack stack = new PoseStack();
        final Matrix4f original = new Matrix4f(matrix);

        try {
            stack.last().pose().multiply(matrix);
            stack.scale( scale, scale, scale );

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return super.drawInBatch(text, x, y, color, dropShadow, stack.last().pose(), buffer, transparentIn, colorBackgroundIn, packedLight);
        }
        finally
        {
            matrix.load(original);
        }
    }

    @Override
    public int draw(PoseStack matrixStack, String text, float x, float y, int color)
    {
        try
        {
            text = convertText( text );
            matrixStack.pushPose();
            matrixStack.scale( scale, scale, scale );

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return talkto.draw(matrixStack, text, x, y, color );
        }
        finally
        {
            matrixStack.popPose();
        }
    }

    @Override
    public int drawShadow(PoseStack matrixStack, String text, float x, float y, int color)
    {
        try
        {
            text = convertText( text );
            matrixStack.pushPose();
            matrixStack.scale( scale, scale, scale );

            x /= scale;
            y /= scale;
            x += offsetX;
            y += offsetY;

            return talkto.drawShadow(matrixStack, text, x, y, color );
        }
        finally
        {
            matrixStack.popPose();
        }
    }

    private String convertText(
			final String text )
	{
		try
		{
			final int value = Integer.parseInt( text );

			if ( value >= 1000 )
			{
				return value / 1000 + "k";
			}

			return text;
		}
		catch ( final NumberFormatException e )
		{
			return text;
		}
	}
}
