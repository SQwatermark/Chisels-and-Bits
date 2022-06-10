package mod.chiselsandbits.utils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@OnlyIn(Dist.CLIENT)
public final class TextureUtils
{

    public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException
    {
        BufferedImage bufferedimage;

        try
        {
            bufferedimage = ImageIO.read(imageStream);
        }
        finally
        {
            IOUtils.closeQuietly(imageStream);
        }

        return bufferedimage;
    }

}
