package mod.chiselsandbits.render.chiseledblock;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import mod.chiselsandbits.render.cache.FormatInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.concurrent.ConcurrentHashMap;

public class ChiselsAndBitsBakedQuad extends BakedQuad {

    public static final ConcurrentHashMap<VertexFormat, FormatInfo> formatData = new ConcurrentHashMap<>();

    private final int[] raw;

    private static int[] packData(
            VertexFormat format,
            float[][][] unpackedData) {
        FormatInfo fi = formatData.get(format);

        if (fi == null) {
            fi = new FormatInfo(format);
            formatData.put(format, fi);
        }

        return fi.pack(unpackedData);
    }

//	@Override
//	public void pipe(
//			final IVertexConsumer consumer )
//	{
//		final int[] eMap = LightUtil.mapFormats( consumer.getVertexFormat(), DefaultVertexFormat.BLOCK );
//
//		consumer.setTexture( sprite );
//		consumer.setQuadTint( getTintIndex() );
//		consumer.setQuadOrientation( getDirection() );
//		consumer.setApplyDiffuseLighting( true );
//
//		for ( int v = 0; v < 4; v++ )
//		{
//			for ( int e = 0; e < consumer.getVertexFormat().getElements().size(); e++ )
//			{
//				if ( eMap[e] != consumer.getVertexFormat().getElements().size() )
//				{
//					consumer.put( e, getRawPart( v, eMap[e] ) );
//				}
//				else
//				{
//					consumer.put( e );
//				}
//			}
//		}
//	}

//    private float[] getRawPart(int v, int i) {
//        return formatData.get(DefaultVertexFormat.BLOCK).unpack(raw, v, i);
//    }

    public ChiselsAndBitsBakedQuad(
            final int[] raw,
            final int[] packedData,
            final int tint,
            final Direction orientation,
            final TextureAtlasSprite sprite) {
        super(packedData, tint, orientation, sprite, true);
        this.raw = raw;
    }

    public static class Colored extends ChiselsAndBitsBakedQuad {
        public Colored(
                final int[] raw,
                final int[] packedData,
                final int tint,
                final Direction orientation,
                final TextureAtlasSprite sprite) {
            super(raw, packedData, tint, orientation, sprite);
        }
    }

    public static class Builder implements IFaceBuilder {
        private float[][][] unpackedData;
        private int tint = -1;
        private Direction orientation;

        private int vertices = 0;
        private int elements = 0;

        private final VertexFormat format;

        public Builder(VertexFormat format) {
            this.format = format;
        }

        public VertexFormat getVertexFormat() {
            return format;
        }

        public void setQuadTint(int tint) {
            this.tint = tint;
        }

        public void setQuadOrientation(Direction orientation) {
            this.orientation = orientation;
        }

        @Override
        public void put(int element, float... data) {
            for (int i = 0; i < 4; i++) {
                if (i < data.length) {
                    unpackedData[vertices][element][i] = data[i];
                } else {
                    unpackedData[vertices][element][i] = 0;
                }
            }

            elements++;

            if (elements == getVertexFormat().getElements().size()) {
                vertices++;
                elements = 0;
            }
        }

        @Override
        public void begin() {
            if (format != getVertexFormat()) {
                throw new RuntimeException("Bad format, can only be CNB.");
            }

            unpackedData = new float[4][getVertexFormat().getElements().size()][4];
            tint = -1;
            orientation = null;

            vertices = 0;
            elements = 0;
        }

        @Override
        public BakedQuad create(TextureAtlasSprite sprite) {

            int[] unLighted = packData(DefaultVertexFormat.BLOCK, unpackedData);

            int[] packed = new int[DefaultVertexFormat.BLOCK.getVertexSize()];

            for (int v = 0; v < 4; v++) {
                for (int e = 0; e < DefaultVertexFormat.BLOCK.getElements().size(); e++) {
                    float[] rawPart = formatData.get(DefaultVertexFormat.BLOCK).unpack(unLighted, v, e);
                    // TODO 亮度从哪弄的？
                    pack(rawPart, packed, DefaultVertexFormat.BLOCK, v, e);
                }
            }

            final boolean isColored = false;
            if (isColored) {
                return new Colored(unLighted, packed, tint, orientation, sprite);
            }

            return new ChiselsAndBitsBakedQuad(unLighted, packed, tint, orientation, sprite);
        }

        public static void pack(float[] from, int[] to, VertexFormat formatTo, int v, int e)
        {
            VertexFormatElement element = formatTo.getElements().get(e);
            int vertexStart = v * formatTo.getVertexSize() + formatTo.getOffset(e);
            int count = element.getElementCount();
            VertexFormatElement.Type type = element.getType();
            int size = type.getSize();
            int mask = (256 << (8 * (size - 1))) - 1;
            for(int i = 0; i < 4; i++)
            {
                if(i < count)
                {
                    int pos = vertexStart + size * i;
                    int index = pos >> 2;
                    int offset = pos & 3;
                    int bits = 0;
                    float f = i < from.length ? from[i] : 0;
                    if(type == VertexFormatElement.Type.FLOAT)
                    {
                        bits = Float.floatToRawIntBits(f);
                    }
                    else if(
                            type == VertexFormatElement.Type.UBYTE ||
                                    type == VertexFormatElement.Type.USHORT ||
                                    type == VertexFormatElement.Type.UINT
                    )
                    {
                        bits = Math.round(f * mask);
                    }
                    else
                    {
                        bits = Math.round(f * (mask >> 1));
                    }
                    to[index] &= ~(mask << (offset * 8));
                    to[index] |= (((bits & mask) << (offset * 8)));
                    // TODO handle overflow into to[index + 1]
                }
            }
        }

        @Override
        public void setFace(
                final Direction myFace,
                final int tintIndex) {
            setQuadOrientation(myFace);
            setQuadTint(tintIndex);
        }

//        @Override
//        public void setApplyDiffuseLighting(
//                final boolean diffuse) {
//        }

//        @Override
//        public void setTexture(
//                final TextureAtlasSprite texture) {
//        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }
}
