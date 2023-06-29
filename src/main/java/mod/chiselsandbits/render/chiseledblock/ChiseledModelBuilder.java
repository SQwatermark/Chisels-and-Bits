package mod.chiselsandbits.render.chiseledblock;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class ChiseledModelBuilder {

    private final List<BakedQuad> up = new ArrayList<>();
    private final List<BakedQuad> down = new ArrayList<>();
    private final List<BakedQuad> north = new ArrayList<>();
    private final List<BakedQuad> south = new ArrayList<>();
    private final List<BakedQuad> east = new ArrayList<>();
    private final List<BakedQuad> west = new ArrayList<>();
    private final List<BakedQuad> generic = new ArrayList<>();

    public List<BakedQuad> getList(Direction side) {
        if (side != null) {
            switch (side) {
                case DOWN:
                    return down;
                case EAST:
                    return east;
                case NORTH:
                    return north;
                case SOUTH:
                    return south;
                case UP:
                    return up;
                case WEST:
                    return west;
                default:
            }
        }

        return generic;
    }

    public BakedQuad[] getSide(Direction side) {
        List<BakedQuad> out = getList(side);

        if (out.isEmpty()) {
            return null;
        }

        return out.toArray(new BakedQuad[0]);
    }
}
