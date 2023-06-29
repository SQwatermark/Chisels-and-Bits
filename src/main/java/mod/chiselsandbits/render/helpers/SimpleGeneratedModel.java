package mod.chiselsandbits.render.helpers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Vector3f;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimpleGeneratedModel implements BakedModel {

    @SuppressWarnings("unchecked")
    final List<BakedQuad>[] face = new List[6];

    final TextureAtlasSprite texture;

    public SimpleGeneratedModel(
            final TextureAtlasSprite texture) {
        // create lists...
        face[0] = new ArrayList<>();
        face[1] = new ArrayList<>();
        face[2] = new ArrayList<>();
        face[3] = new ArrayList<>();
        face[4] = new ArrayList<>();
        face[5] = new ArrayList<>();

        this.texture = texture;

        final float[] afloat = new float[]{0, 0, 16, 16};
        final BlockFaceUV uv = new BlockFaceUV(afloat, 0);
        final FaceBakery faceBakery = new FaceBakery();

        final Vector3f to = new Vector3f(0.0f, 0.0f, 0.0f);
        final Vector3f from = new Vector3f(16.0f, 16.0f, 16.0f);

        final BlockElementRotation bpr = null;
        final BlockModelRotation mr = BlockModelRotation.X0_Y0;

        for (final Direction side : Direction.values()) {
            final BlockElementFace bpf = new BlockElementFace(side, 1, "", uv);

            Vector3f toB, fromB;

            switch (side) {
                case UP -> {
                    toB = new Vector3f(to.x(), from.y(), to.z());
                    fromB = new Vector3f(from.x(), from.y(), from.z());
                }
                case EAST -> {
                    toB = new Vector3f(from.x(), to.y(), to.z());
                    fromB = new Vector3f(from.x(), from.y(), from.z());
                }
                case NORTH -> {
                    toB = new Vector3f(to.x(), to.y(), to.z());
                    fromB = new Vector3f(from.x(), from.y(), to.z());
                }
                case SOUTH -> {
                    toB = new Vector3f(to.x(), to.y(), from.z());
                    fromB = new Vector3f(from.x(), from.y(), from.z());
                }
                case DOWN -> {
                    toB = new Vector3f(to.x(), to.y(), to.z());
                    fromB = new Vector3f(from.x(), to.y(), from.z());
                }
                case WEST -> {
                    toB = new Vector3f(to.x(), to.y(), to.z());
                    fromB = new Vector3f(to.x(), from.y(), from.z());
                }
                default -> throw new NullPointerException();
            }

            final BakedQuad g = faceBakery.bakeQuad(toB, fromB, bpf, texture, side, mr, bpr, false, new ResourceLocation(ChiselsAndBits.MODID, "simple"));
            face[side.ordinal()].add(finishFace(g, side, DefaultVertexFormat.BLOCK));
        }
    }

    private BakedQuad finishFace(
            final BakedQuad g,
            final Direction myFace,
            final VertexFormat format) {
        final int[] vertData = g.getVertices();
        final int wrapAt = vertData.length / 4;

        final BakedQuadBuilder b = new BakedQuadBuilder(g.getSprite());
        b.setQuadOrientation(myFace);
        b.setQuadTint(1);

        for (int vertNum = 0; vertNum < 4; vertNum++) {
            for (int elementIndex = 0; elementIndex < format.getElements().size(); elementIndex++) {
                final VertexFormatElement element = format.getElements().get(elementIndex);
                switch (element.getUsage()) {
                    case POSITION:
                        b.put(elementIndex, Float.intBitsToFloat(vertData[0 + wrapAt * vertNum]), Float.intBitsToFloat(vertData[1 + wrapAt * vertNum]), Float.intBitsToFloat(vertData[2 + wrapAt * vertNum]));
                        break;

                    case COLOR:
                        final float light = LightUtil.diffuseLight(myFace);
                        b.put(elementIndex, light, light, light, 1f);
                        break;

                    case NORMAL:
                        b.put(elementIndex, myFace.getStepX(), myFace.getStepY(), myFace.getStepZ());
                        break;

                    case UV:

                        if (element.getIndex() == 1) {
                            b.put(elementIndex, 0, 0);
                        } else {
                            final float u = Float.intBitsToFloat(vertData[4 + wrapAt * vertNum]);
                            final float v = Float.intBitsToFloat(vertData[5 + wrapAt * vertNum]);
                            b.put(elementIndex, u, v);
                        }

                        break;

                    default:
                        b.put(elementIndex);
                        break;
                }
            }
        }

        return b.build();
    }

    public List<BakedQuad>[] getFace() {
        return face;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        if (side == null) {
            return Collections.emptyList();
        }

        return face[side.ordinal()];
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return texture;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}