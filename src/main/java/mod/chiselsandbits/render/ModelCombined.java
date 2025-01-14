package mod.chiselsandbits.render;

import mod.chiselsandbits.client.model.baked.BaseBakedBlockModel;
import mod.chiselsandbits.core.ClientSide;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelCombined extends BaseBakedBlockModel {

    BakedModel[] merged;

    List<BakedQuad>[] face;
    List<BakedQuad> generic;

    boolean isSideLit;

    @SuppressWarnings("unchecked")
    public ModelCombined(BakedModel... args) {
        RandomSource random = RandomSource.create();
        face = new ArrayList[Direction.values().length];

        generic = new ArrayList<>();
        for (final Direction f : Direction.values()) {
            face[f.ordinal()] = new ArrayList<>();
        }

        merged = args;

        for (final BakedModel m : merged) {
            generic.addAll(m.getQuads(null, null, random));
            for (final Direction f : Direction.values()) {
                face[f.ordinal()].addAll(m.getQuads(null, f, random));
            }
        }

        isSideLit = Arrays.stream(args).anyMatch(BakedModel::usesBlockLight);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        for (final BakedModel a : merged) {
            return a.getParticleIcon();
        }

        return ClientSide.instance.getMissingIcon();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        if (side != null) {
            return face[side.ordinal()];
        }
        return generic;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public boolean usesBlockLight() {
        return isSideLit;
    }
}
