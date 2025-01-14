package mod.chiselsandbits.chiseledblock.data;

import java.util.List;

import net.minecraft.world.phys.AABB;

public class BitOcclusionIterator extends BitCollisionIterator {

    private final double epsilon = 0;

    private float physicalStartX = 0.0f;
    private boolean lastSetting = false;

    final List<AABB> o;

    public BitOcclusionIterator(
            final List<AABB> out) {
        o = out;
    }

    @Override
    protected void yPlus() {
        addCurrentBox(One16thf);
        super.yPlus();
    }

    @Override
    protected void zPlus() {
        addCurrentBox(One16thf);
        super.zPlus();
    }

    @Override
    protected void done() {
        addCurrentBox(One16thf);
    }

    protected void addCurrentBox(double addition) {
        if (lastSetting == true) {
            addBox(addition);
            lastSetting = false;
        }
    }

    private void addBox(
            final double addition) {
        final double xFullMinusEpsilon = 1.0 - epsilon;
        final AABB newBox = new AABB(
                physicalStartX < epsilon ? physicalStartX : physicalStartX + epsilon,
                y == 0 ? physicalY : physicalY + epsilon,
                z == 0 ? physicalZ : physicalZ + epsilon,
                physicalX + addition > xFullMinusEpsilon ? physicalX + addition : physicalX + addition - epsilon,
                y == 15 ? physicalYp1 : physicalYp1 - epsilon,
                z == 15 ? physicalZp1 : physicalZp1 - epsilon);

        if (!o.isEmpty()) {
            int offset = o.size() - 1;
            AABB lastBox = o.get(offset);

            if (isBelow(newBox, lastBox)) {
                AABB combined = lastBox.minmax(newBox);
                o.remove(offset);

                if (!o.isEmpty()) {
                    offset = o.size() - 1;
                    lastBox = o.get(offset);
                    if (!o.isEmpty() && isNextTo(combined, lastBox)) {
                        combined = lastBox.minmax(combined);
                        o.remove(offset);
                    }

                }

                o.add(combined);
                return;
            }

        }

        o.add(newBox);
    }

    private boolean isNextTo(
            final AABB newBox,
            final AABB lastBox) {
        final boolean sameX = newBox.minX == lastBox.minX && newBox.maxX == lastBox.maxX;
        final boolean sameY = newBox.minY == lastBox.minY && newBox.maxY == lastBox.maxY;
        final double touchingZ = newBox.minZ - lastBox.maxZ;
        final double epsilonGap = epsilon * 2.1;
        return sameX && sameY && touchingZ < epsilonGap;
    }

    private boolean isBelow(
            final AABB newBox,
            final AABB lastBox) {
        final boolean sameX = newBox.minX == lastBox.minX && newBox.maxX == lastBox.maxX;
        final boolean sameZ = newBox.minZ == lastBox.minZ && newBox.maxZ == lastBox.maxZ;
        final double touchingY = newBox.minY - lastBox.maxY;
        return sameX && sameZ && touchingY < 0.001;
    }

    public void add() {
        if (!lastSetting) {
            physicalStartX = physicalX;
            lastSetting = true;
        }
    }

    public void drop() {
        addCurrentBox(0);
    }

}
