package mod.chiselsandbits.chiseledblock;

import net.minecraft.world.level.material.Material;

class SubMaterial
{

    public static Material create(Material other) {
        return new Material(
          other.getColor(),
          other.isLiquid(),
          true,
          true,
          false,
          other.isFlammable(),
          other.isReplaceable(),
          other.getPushReaction()
        );
    }
}