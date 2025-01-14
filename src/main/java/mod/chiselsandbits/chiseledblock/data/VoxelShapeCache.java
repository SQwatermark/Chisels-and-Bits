package mod.chiselsandbits.chiseledblock.data;

import mod.chiselsandbits.api.BoxType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

public final class VoxelShapeCache {

    private static final VoxelShapeCache INSTANCE = new VoxelShapeCache();

    public static VoxelShapeCache getInstance() {
        return INSTANCE;
    }

    private final SimpleMaxSizedCache<VoxelShapeCache.CacheKey, VoxelShape> cache = new SimpleMaxSizedCache<>(ChiselsAndBits.getConfig().getCommon().collisionBoxCacheSize.get());

    private VoxelShapeCache() {
    }

    public VoxelShape get(VoxelBlob blob, BoxType type) {
        final CacheKey key = new CacheKey(type, (BitSet) blob.getNoneAir().clone());

        VoxelShape shape = cache.get(key);
        if (shape == null) {
            shape = calculateNewVoxelShape(blob, type);
            cache.put(key, shape);
        }

        return shape;
    }

    private VoxelShape calculateNewVoxelShape(final VoxelBlob data, final BoxType type) {
        return VoxelShapeCalculator.calculate(data, type).optimize();
    }

    private record CacheKey(BoxType type, BitSet noneAirMap) {

        @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof final CacheKey cacheKey)) {
                    return false;
                }
            return type == cacheKey.type &&
                        noneAirMap.equals(cacheKey.noneAirMap);
            }

    }
}
