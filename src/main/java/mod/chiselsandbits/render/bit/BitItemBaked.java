package mod.chiselsandbits.render.bit;

import mod.chiselsandbits.client.model.baked.BaseBakedBlockModel;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.render.helpers.ModelQuadLayer;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitItemBaked extends BaseBakedBlockModel {

    public static final float PIXELS_PER_BLOCK = 16.0f;

    private static final float BIT_BEGIN = 6.0f;
    private static final float BIT_END = 10.0f;

    final ArrayList<BakedQuad> generic = new ArrayList<>(6);

    public BitItemBaked(int BlockRef) {
        RandomSource random = RandomSource.create();
        final FaceBakery faceBakery = new FaceBakery();

        final Vector3f to = new Vector3f(BIT_BEGIN, BIT_BEGIN, BIT_BEGIN);
        final Vector3f from = new Vector3f(BIT_END, BIT_END, BIT_END);

        final BlockElementRotation bpr = null;
        final BlockModelRotation mr = BlockModelRotation.X0_Y0;

        for (final Direction myFace : Direction.values()) {
            for (final RenderType layer : RenderType.chunkBufferLayers()) {
                final ModelQuadLayer[] layers = ModelUtil.getCachedFace(BlockRef, random, myFace, layer);

                if (layers == null || layers.length == 0) {
                    continue;
                }

                for (final ModelQuadLayer clayer : layers) {
                    final BlockFaceUV uv = new BlockFaceUV(getFaceUvs(myFace), 0);
                    final BlockElementFace bpf = new BlockElementFace(myFace, 0, "", uv);

                    Vector3f toB, fromB;

                    switch (myFace) {
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

                    generic.add(faceBakery.bakeQuad(toB, fromB, bpf, clayer.sprite, myFace, mr, bpr, false, new ResourceLocation(ChiselsAndBits.MODID, "bit")));
                }
            }
        }

        generic.trimToSize();
    }

    private float[] getFaceUvs(Direction face) {
        float[] afloat;

        final int from_x = 7;
        final int from_y = 7;
        final int from_z = 7;

        final int to_x = 8;
        final int to_y = 8;
        final int to_z = 8;

        afloat = switch (face) {
            case DOWN, UP -> new float[]{from_x, from_z, to_x, to_z};
            case NORTH, SOUTH -> new float[]{from_x, PIXELS_PER_BLOCK - to_y, to_x, PIXELS_PER_BLOCK - from_y};
            case WEST, EAST -> new float[]{from_z, PIXELS_PER_BLOCK - to_y, to_z, PIXELS_PER_BLOCK - from_y};
        };

        return afloat;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        if (side != null) {
            return Collections.emptyList();
        }
        return generic;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return ClientSide.instance.getMissingIcon();
    }

}
