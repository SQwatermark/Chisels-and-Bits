package mod.chiselsandbits.render.chiseledblock;

import net.minecraft.core.Direction;

class FaceRegion {
    public FaceRegion(Direction myFace, int centerX, int centerY, int centerZ, int blockStateID, boolean isEdgeFace) {
        direction = myFace;
        this.blockStateID = blockStateID;
        isEdge = isEdgeFace;
        minX = centerX;
        minY = centerY;
        minZ = centerZ;
        maxX = centerX;
        maxY = centerY;
        maxZ = centerZ;
    }

    final public Direction direction;
    final int blockStateID;
    final boolean isEdge;

    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public boolean extend(FaceRegion currentFace) {
        if (currentFace.blockStateID != blockStateID) {
            return false;
        }

        switch (direction) {
            case DOWN:
            case UP: {
                final boolean a = maxX == currentFace.minX - 2 && maxZ == currentFace.maxZ && minZ == currentFace.minZ;
                final boolean b = minX == currentFace.maxX + 2 && maxZ == currentFace.maxZ && minZ == currentFace.minZ;
                final boolean c = maxZ == currentFace.minZ - 2 && maxX == currentFace.maxX && minX == currentFace.minX;
                final boolean d = minZ == currentFace.maxZ + 2 && maxX == currentFace.maxX && minX == currentFace.minX;

                if (a || b || c || d) {
                    minX = Math.min(currentFace.minX, minX);
                    minY = Math.min(currentFace.minY, minY);
                    minZ = Math.min(currentFace.minZ, minZ);
                    maxX = Math.max(currentFace.maxX, maxX);
                    maxY = Math.max(currentFace.maxY, maxY);
                    maxZ = Math.max(currentFace.maxZ, maxZ);
                    return true;
                }

                return false;
            }

            case WEST:
            case EAST: {
                final boolean a = maxY == currentFace.minY - 2 && maxZ == currentFace.maxZ && minZ == currentFace.minZ;
                final boolean b = minY == currentFace.maxY + 2 && maxZ == currentFace.maxZ && minZ == currentFace.minZ;
                final boolean c = maxZ == currentFace.minZ - 2 && maxY == currentFace.maxY && minY == currentFace.minY;
                final boolean d = minZ == currentFace.maxZ + 2 && maxY == currentFace.maxY && minY == currentFace.minY;

                if (a || b || c || d) {
                    minX = Math.min(currentFace.minX, minX);
                    minY = Math.min(currentFace.minY, minY);
                    minZ = Math.min(currentFace.minZ, minZ);
                    maxX = Math.max(currentFace.maxX, maxX);
                    maxY = Math.max(currentFace.maxY, maxY);
                    maxZ = Math.max(currentFace.maxZ, maxZ);
                    return true;
                }

                return false;
            }

            case NORTH:
            case SOUTH: {
                final boolean a = maxY == currentFace.minY - 2 && maxX == currentFace.maxX && minX == currentFace.minX;
                final boolean b = minY == currentFace.maxY + 2 && maxX == currentFace.maxX && minX == currentFace.minX;
                final boolean c = maxX == currentFace.minX - 2 && maxY == currentFace.maxY && minY == currentFace.minY;
                final boolean d = minX == currentFace.maxX + 2 && maxY == currentFace.maxY && minY == currentFace.minY;

                if (a || b || c || d) {
                    minX = Math.min(currentFace.minX, minX);
                    minY = Math.min(currentFace.minY, minY);
                    minZ = Math.min(currentFace.minZ, minZ);
                    maxX = Math.max(currentFace.maxX, maxX);
                    maxY = Math.max(currentFace.maxY, maxY);
                    maxZ = Math.max(currentFace.maxZ, maxZ);
                    return true;
                }

                return false;
            }

            default:
                return false;
        }
    }
}