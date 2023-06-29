package mod.chiselsandbits.render.helpers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

import static net.minecraftforge.client.model.IQuadTransformer.*;

public class ModelUVReader extends BaseModelReader {

    private final Map<VertexFormatElement, Integer> ELEMENT_OFFSETS = Util.make(new IdentityHashMap<>(), map -> {
        int i = 0;
        for (var element : DefaultVertexFormat.BLOCK.getElements())
            map.put(element, DefaultVertexFormat.BLOCK.getOffset(i++) / 4); // Int offset
    });

    final float minU;
    final float maxUMinusMin;

    final float minV;
    final float maxVMinusMin;

    public final float[] quadUVs = new float[]{0, 0, 0, 1, 1, 0, 1, 1};

    int uCoord, vCoord;

    public ModelUVReader(
            final TextureAtlasSprite texture,
            final int uFaceCoord,
            final int vFaceCoord) {
        minU = texture.getU0();
        maxUMinusMin = texture.getU1() - minU;

        minV = texture.getV0();
        maxVMinusMin = texture.getV1() - minV;

        uCoord = uFaceCoord;
        vCoord = vFaceCoord;
    }

    private float pos[];
    private float uv[];
    public int corners;

	@Override
	public void misc(@NotNull VertexFormatElement element, int @NotNull ... rawData) {
        final VertexFormat format = getVertexFormat();
        // TODO 找出int和float转换的秘密
        if (element.getUsage() == VertexFormatElement.Usage.UV && element.getIndex() == 0) {
            uv = Arrays.copyOf(rawData, rawData.length);
        } else if (element.getUsage() == VertexFormatElement.Usage.POSITION) {
            pos = Arrays.copyOf(rawData, rawData.length);
        }

        if (element == format.getElements().size() - 1) {
            if (ModelUtil.isZero(pos[uCoord]) && ModelUtil.isZero(pos[vCoord])) {
                corners = corners | 0x1;
                quadUVs[0] = (uv[0] - minU) / maxUMinusMin;
                quadUVs[1] = (uv[1] - minV) / maxVMinusMin;
            } else if (ModelUtil.isZero(pos[uCoord]) && ModelUtil.isOne(pos[vCoord])) {
                corners = corners | 0x2;
                quadUVs[4] = (uv[0] - minU) / maxUMinusMin;
                quadUVs[5] = (uv[1] - minV) / maxVMinusMin;
            } else if (ModelUtil.isOne(pos[uCoord]) && ModelUtil.isZero(pos[vCoord])) {
                corners = corners | 0x4;
                quadUVs[2] = (uv[0] - minU) / maxUMinusMin;
                quadUVs[3] = (uv[1] - minV) / maxVMinusMin;
            } else if (ModelUtil.isOne(pos[uCoord]) && ModelUtil.isOne(pos[vCoord])) {
                corners = corners | 0x8;
                quadUVs[6] = (uv[0] - minU) / maxUMinusMin;
                quadUVs[7] = (uv[1] - minV) / maxVMinusMin;
            }
        }
	}

}