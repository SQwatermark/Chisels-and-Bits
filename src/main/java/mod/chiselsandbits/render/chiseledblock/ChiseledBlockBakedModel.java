package mod.chiselsandbits.render.chiseledblock;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.VisibleFace;
import mod.chiselsandbits.client.culling.ICullTest;
import mod.chiselsandbits.client.model.baked.BaseBakedBlockModel;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.helpers.ModelQuadLayer;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

/**
 * 雕刻方块的模型
 */
public class ChiseledBlockBakedModel extends BaseBakedBlockModel {
    private static final RandomSource RANDOM = RandomSource.create();

    public static final float PIXELS_PER_BLOCK = 16.0f;
    private final static int[][] faceVertMap = new int[6][4];
    private final static float[][][] quadMapping = new float[6][4][6];

    private static final Direction[] X_Faces = new Direction[]{Direction.EAST, Direction.WEST};
    private static final Direction[] Y_Faces = new Direction[]{Direction.UP, Direction.DOWN};
    private static final Direction[] Z_Faces = new Direction[]{Direction.SOUTH, Direction.NORTH};

    // Analyze FaceBakery / makeBakedQuad and prepare static data for face gen.
    static {
        final Vector3f to = new Vector3f(0, 0, 0);
        final Vector3f from = new Vector3f(16, 16, 16);

        for (final Direction myFace : Direction.values()) {
            final FaceBakery faceBakery = new FaceBakery();

            final BlockElementRotation bpr = null;
            final BlockModelRotation mr = BlockModelRotation.X0_Y0;

            final float[] defUVs = new float[]{0, 0, 1, 1};
            final BlockFaceUV uv = new BlockFaceUV(defUVs, 0);
            final BlockElementFace bpf = new BlockElementFace(myFace, 0, "", uv);

            final TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("missingno"));
            final BakedQuad q = faceBakery.bakeQuad(to, from, bpf, texture, myFace, mr, bpr, true, new ResourceLocation(ChiselsAndBits.MODID, "chiseled_block"));

            final int[] vertData = q.getVertices();

            int a = 0;
            int b = 2;

            switch (myFace) {
                case NORTH, SOUTH -> {
                    a = 0;
                    b = 1;
                }
                case EAST, WEST -> {
                    a = 1;
                    b = 2;
                }
                default -> {
                }
            }

            final int p = vertData.length / 4;
            for (int vertNum = 0; vertNum < 4; vertNum++) {
                final float A = Float.intBitsToFloat(vertData[vertNum * p + a]);
                final float B = Float.intBitsToFloat(vertData[vertNum * p + b]);

                for (int o = 0; o < 3; o++) {
                    final float v = Float.intBitsToFloat(vertData[vertNum * p + o]);
                    final float scaler = 1.0f / 16.0f; // pos start in the 0-16
                    quadMapping[myFace.ordinal()][vertNum][o * 2] = v * scaler;
                    quadMapping[myFace.ordinal()][vertNum][o * 2 + 1] = (1.0f - v) * scaler;
                }

                if (ModelUtil.isZero(A) && ModelUtil.isZero(B)) {
                    faceVertMap[myFace.get3DDataValue()][vertNum] = 0;
                } else if (ModelUtil.isZero(A) && ModelUtil.isOne(B)) {
                    faceVertMap[myFace.get3DDataValue()][vertNum] = 3;
                } else if (ModelUtil.isOne(A) && ModelUtil.isZero(B)) {
                    faceVertMap[myFace.get3DDataValue()][vertNum] = 1;
                } else {
                    faceVertMap[myFace.get3DDataValue()][vertNum] = 2;
                }
            }
        }
    }

    private ChiselRenderType myLayer;
    private TextureAtlasSprite sprite;

    // keep memory requirements low by using arrays.
    private BakedQuad[] up;
    private BakedQuad[] down;
    private BakedQuad[] north;
    private BakedQuad[] south;
    private BakedQuad[] east;
    private BakedQuad[] west;
    private BakedQuad[] generic;

    public List<BakedQuad> getList(final Direction side) {
        if (side != null) {
            switch (side) {
                case DOWN:
                    return asList(down);
                case EAST:
                    return asList(east);
                case NORTH:
                    return asList(north);
                case SOUTH:
                    return asList(south);
                case UP:
                    return asList(up);
                case WEST:
                    return asList(west);
                default:
            }
        }

        return asList(generic);
    }

    private static List<BakedQuad> asList(final BakedQuad[] array) {
        if (array == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(array);
    }

    private ChiseledBlockBakedModel() {
    }


    // TODO 用一个ChiseledBlockBakedModel的数组表示一个合并的模型
    // 此处开始根据体素数据构建模型面片
    public ChiseledBlockBakedModel(int stateId, ChiselRenderType layer, VoxelBlob data, VertexFormat format) {
        myLayer = layer;
        BlockState state = ModUtil.getStateById(stateId);

        BakedModel originalModel = null;

        if (state != null) {
            originalModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
        }

        if (originalModel != null && data != null) {
            if (layer.filter(data)) {
                ChiseledModelBuilder builder = new ChiseledModelBuilder();
                generateFaces(builder, data, RANDOM);

                // convert from builder to final storage.
                up = builder.getSide(Direction.UP);
                down = builder.getSide(Direction.DOWN);
                east = builder.getSide(Direction.EAST);
                west = builder.getSide(Direction.WEST);
                north = builder.getSide(Direction.NORTH);
                south = builder.getSide(Direction.SOUTH);
                generic = builder.getSide(null);
            }
        }
    }

    public static ChiseledBlockBakedModel breakingParticleModel(final ChiselRenderType layer, final Integer blockStateID, final RandomSource random) {
        return ModelUtil.getBreakingModel(layer, blockStateID, random);
    }

    public boolean isEmpty() {
        boolean trulyEmpty = getList(null).isEmpty();

        for (final Direction e : Direction.values()) {
            trulyEmpty = trulyEmpty && getList(e).isEmpty();
        }

        return trulyEmpty;
    }

    private IFaceBuilder getBuilder(VertexFormat format) {
        if (ChiseledBlockSmartModel.ForgePipelineDisabled()) {
            format = DefaultVertexFormat.BLOCK;
        }

        return new ChiselsAndBitsBakedQuad.Builder(format);
    }

    /**
     * 重中之重的根据体素构建片面
     * @param builder
     * @param blob
     * @param random
     */
    private void generateFaces(ChiseledModelBuilder builder, VoxelBlob blob, RandomSource random) {
        // 缓存面片信息
        ArrayList<ArrayList<FaceRegion>> rset = new ArrayList<>();
        // 一个用于记录返回值的对象，节省反复new返回值的开销
        VisibleFace visFace = new VisibleFace();
        // 将体素信息转换为面片信息
        processXFaces(blob, visFace, rset);
        processYFaces(blob, visFace, rset);
        processZFaces(blob, visFace, rset);

        // re-usable float[]'s to minimize garbage cleanup.
        int[] to = new int[3];
        int[] from = new int[3];
        float[] uvs = new float[8];
        float[] pos = new float[3];

        // TODO FaceBuilder很可能需要完全重写，参考FaceBakery
        // TODO 步骤：构建顶点数据：FaceBakery.fillVertex
        //           光照：QuadTransformers.applyingLightmap(data.blockLight(), data.skyLight()).processInPlace(quad);
        //           颜色：QuadTransformers.applyingColor(data.color()).processInPlace(quad);
        // single reusable face builder.
        IFaceBuilder darkBuilder = getBuilder(DefaultVertexFormat.BLOCK);
        IFaceBuilder litBuilder = darkBuilder; // TODO 为什么是一样的？

        for (ArrayList<FaceRegion> src : rset) {
            // 尽可能合并同一平面上的面 TODO 为什么不在processXFaces的时候就合并？
            mergeFaces(src);

            for (FaceRegion region : src) {
                Direction direction = region.direction;

                // keep integers up until the last moment... ( note I tested
                // snapping the floats after this stage, it made no
                // difference. )
                // 之前处理面的时候，FaceRegion的最大最小值都是边缘小块的中心，现在将其修正为真正的边缘，返回值存储至to和from
                offsetVec(to, region.getMaxX(), region.getMaxY(), region.getMaxZ(), direction, 1);
                offsetVec(from, region.getMinX(), region.getMinY(), region.getMinZ(), direction, -1);
                // 获取相应方块ID在相应方向的相应渲染类型的面
                ModelQuadLayer[] mpc = ModelUtil.getCachedFace(region.blockStateID, random, direction, myLayer.layer);

                if (mpc != null) {
                    for (ModelQuadLayer pc : mpc) {
                        // 将ModelQuadLayer和FaceRegion转换为BakedQuad
                        IFaceBuilder faceBuilder = pc.light > 0 ? litBuilder : darkBuilder;
                        VertexFormat builderFormat = faceBuilder.getFormat();

                        faceBuilder.begin();
                        faceBuilder.setFace(direction, pc.tint);

                        float maxLightmap = 32.0f / 0xffff;
                        getFaceUvs(uvs, direction, from, to, pc.uvs);

                        // build it.
                        for (int vertNum = 0; vertNum < 4; vertNum++) {
                            for (int elementIndex = 0; elementIndex < builderFormat.getElements().size(); elementIndex++) {
                                VertexFormatElement element = builderFormat.getElements().get(elementIndex);
                                switch (element.getUsage()) {
                                    case POSITION:
                                        getVertexPos(pos, direction, vertNum, to, from);
                                        faceBuilder.put(elementIndex, pos[0], pos[1], pos[2]);
                                        break;

                                    case COLOR:
                                        int cb = pc.color;
                                        faceBuilder.put(elementIndex, byteToFloat(cb >> 16), byteToFloat(cb >> 8), byteToFloat(cb), NotZero(byteToFloat(cb >> 24)));
                                        break;

                                    case NORMAL:
                                        // this fixes a bug with Forge AO?? and
                                        // solid blocks... I have no idea why...
                                        float normalShift = 0.999f;
                                        faceBuilder.put(elementIndex, normalShift * direction.getStepX(), normalShift * direction.getStepY(), normalShift * direction.getStepZ());
                                        break;

                                    case UV:
                                        if (element.getIndex() == 2) {
                                            float v = maxLightmap * Math.max(0, Math.min(15, pc.light));
                                            faceBuilder.put(elementIndex, v, v);
                                        } else {
                                            float u = uvs[faceVertMap[direction.get3DDataValue()][vertNum] * 2 + 0];
                                            float v = uvs[faceVertMap[direction.get3DDataValue()][vertNum] * 2 + 1];
                                            faceBuilder.put(elementIndex, pc.sprite.getU(u), pc.sprite.getV(v));
                                        }
                                        break;

                                    default:
                                        faceBuilder.put(elementIndex);
                                        break;
                                }
                            }
                        }

                        if (region.isEdge) {
                            builder.getList(direction).add(faceBuilder.create(pc.sprite));
                        } else {
                            builder.getList(null).add(faceBuilder.create(pc.sprite));
                        }
                    }
                }
            }
        }
    }

    private float NotZero(float byteToFloat) {
        if (byteToFloat < 0.00001f) {
            return 1;
        }
        return byteToFloat;
    }

    private float byteToFloat(final int i) {
        return (i & 0xff) / 255.0f;
    }

    private void mergeFaces(final ArrayList<FaceRegion> src) {
        boolean restart;

        do {
            restart = false;

            final int size = src.size();
            final int sizeMinusOne = size - 1;

            restart:
            for (int x = 0; x < sizeMinusOne; ++x) {
                final FaceRegion faceA = src.get(x);

                for (int y = x + 1; y < size; ++y) {
                    final FaceRegion faceB = src.get(y);

                    if (faceA.extend(faceB)) {
                        src.set(y, src.get(sizeMinusOne));
                        src.remove(sizeMinusOne);

                        restart = true;
                        break restart;
                    }
                }
            }
        }
        while (restart);
    }

    /**
     * 根据二进制信息获取X方向的所有面片区域
     * @param blob 体素二进制信息
     * @param visFace 工具对象，用于传参
     * @param rset 返回值，X方向的所有面片
     */
    private void processXFaces(VoxelBlob blob, VisibleFace visFace, ArrayList<ArrayList<FaceRegion>> rset) {
        ArrayList<FaceRegion> regions = null;
        ICullTest test = myLayer.getTest();

        // 东侧和西侧
        for (Direction direction : X_Faces) {
            // blob.detail === 16
            // 一层一层遍历
            for (int x = 0; x < blob.detail; x++) {
                if (regions == null) {
                    regions = new ArrayList<>(16);
                }

                // 遍历整个面
                for (int z = 0; z < blob.detail; z++) {
                    FaceRegion currentFace = null;

                    for (int y = 0; y < blob.detail; y++) {
                        FaceRegion region = getRegion(blob, direction, x, y, z, visFace, test);
                        // 如果面不可见，返回值就是null
                        if (region == null) {
                            currentFace = null;
                            continue;
                        }

                        if (currentFace != null) {
                            // 合并
                            if (currentFace.extend(region)) {
                                continue;
                            }
                        }

                        currentFace = region;
                        regions.add(region);
                    }
                }

                if (!regions.isEmpty()) {
                    rset.add(regions);
                    regions = null;
                }
            }
        }
    }

    private void processYFaces(final VoxelBlob blob, final VisibleFace visFace, final ArrayList<ArrayList<FaceRegion>> rset) {
        ArrayList<FaceRegion> regions = null;
        final ICullTest test = myLayer.getTest();

        for (final Direction myFace : Y_Faces) {
            for (int y = 0; y < blob.detail; y++) {
                if (regions == null) {
                    regions = new ArrayList<>(16);
                }

                for (int z = 0; z < blob.detail; z++) {
                    FaceRegion currentFace = null;

                    for (int x = 0; x < blob.detail; x++) {
                        final FaceRegion region = getRegion(blob, myFace, x, y, z, visFace, test);

                        if (region == null) {
                            currentFace = null;
                            continue;
                        }

                        if (currentFace != null) {
                            if (currentFace.extend(region)) {
                                continue;
                            }
                        }

                        currentFace = region;
                        regions.add(region);
                    }
                }

                if (!regions.isEmpty()) {
                    rset.add(regions);
                    regions = null;
                }
            }
        }
    }

    private void processZFaces(final VoxelBlob blob, final VisibleFace visFace, final ArrayList<ArrayList<FaceRegion>> rset) {
        ArrayList<FaceRegion> regions = null;
        final ICullTest test = myLayer.getTest();

        for (final Direction myFace : Z_Faces) {
            for (int z = 0; z < blob.detail; z++) {
                if (regions == null) {
                    regions = new ArrayList<>(16);
                }

                for (int y = 0; y < blob.detail; y++) {
                    FaceRegion currentFace = null;

                    for (int x = 0; x < blob.detail; x++) {
                        final FaceRegion region = getRegion(blob, myFace, x, y, z, visFace, test);

                        if (region == null) {
                            currentFace = null;
                            continue;
                        }

                        if (currentFace != null) {
                            if (currentFace.extend(region)) {
                                continue;
                            }
                        }

                        currentFace = region;
                        regions.add(region);
                    }
                }

                if (!regions.isEmpty()) {
                    rset.add(regions);
                    regions = null;
                }
            }
        }
    }

    private FaceRegion getRegion(VoxelBlob blob, Direction direction, int x, int y, int z, VisibleFace visFace, ICullTest test) {
        // 判断该坐标的小方块在这个方向上是否可见，获取stateId，以及是否在边缘，返回值在visFace中
        blob.visibleFace(direction, x, y, z, visFace, test);

        if (visFace.visibleFace) {
            // 坐标范围是0-32，表示面的中心点在什么位置
            return new FaceRegion(direction,
                    x * 2 + 1 + direction.getStepX(),
                    y * 2 + 1 + direction.getStepY(),
                    z * 2 + 1 + direction.getStepZ(),
                    visFace.state,
                    visFace.isEdge);
        }

        return null;
    }

    // generate final pos from static data.
    private void getVertexPos(final float[] pos, final Direction side, final int vertNum, final int[] to, final int[] from) {
        final float[] interpos = quadMapping[side.ordinal()][vertNum];

        pos[0] = to[0] * interpos[0] + from[0] * interpos[1];
        pos[1] = to[1] * interpos[2] + from[1] * interpos[3];
        pos[2] = to[2] * interpos[4] + from[2] * interpos[5];
    }

    private void getFaceUvs(final float[] uvs, final Direction face, final int[] from, final int[] to, final float[] quadsUV) {
        float to_u = 0;
        float to_v = 0;
        float from_u = 0;
        float from_v = 0;

        switch (face) {
            case UP, DOWN -> {
                to_u = to[0] / 16.0f;
                to_v = to[2] / 16.0f;
                from_u = from[0] / 16.0f;
                from_v = from[2] / 16.0f;
            }
            case SOUTH, NORTH -> {
                to_u = to[0] / 16.0f;
                to_v = to[1] / 16.0f;
                from_u = from[0] / 16.0f;
                from_v = from[1] / 16.0f;
            }
            case EAST, WEST -> {
                to_u = to[1] / 16.0f;
                to_v = to[2] / 16.0f;
                from_u = from[1] / 16.0f;
                from_v = from[2] / 16.0f;
            }
            default -> {
            }
        }

        uvs[0] = 16.0f * u(quadsUV, to_u, to_v); // 0
        uvs[1] = 16.0f * v(quadsUV, to_u, to_v); // 1

        uvs[2] = 16.0f * u(quadsUV, from_u, to_v); // 2
        uvs[3] = 16.0f * v(quadsUV, from_u, to_v); // 3

        uvs[4] = 16.0f * u(quadsUV, from_u, from_v); // 2
        uvs[5] = 16.0f * v(quadsUV, from_u, from_v); // 3

        uvs[6] = 16.0f * u(quadsUV, to_u, from_v); // 0
        uvs[7] = 16.0f * v(quadsUV, to_u, from_v); // 1
    }

    float u(final float[] src, final float inU, final float inV) {
        final float inv = 1.0f - inU;
        final float u1 = src[0] * inU + inv * src[2];
        final float u2 = src[4] * inU + inv * src[6];
        return u1 * inV + (1.0f - inV) * u2;
    }

    float v(final float[] src, final float inU, final float inV) {
        final float inv = 1.0f - inU;
        final float v1 = src[1] * inU + inv * src[3];
        final float v2 = src[5] * inU + inv * src[7];
        return v1 * inV + (1.0f - inV) * v2;
    }

    private static void offsetVec(final int[] result, final int toX, final int toY, final int toZ, final Direction f, final int d) {

        int leftX = 0;
        final int leftY = 0;
        int leftZ = 0;

        final int upX = 0;
        int upY = 0;
        int upZ = 0;

        switch (f) {
            case DOWN, UP -> {
                leftX = 1;
                upZ = 1;
            }
            case EAST, WEST -> {
                leftZ = 1;
                upY = 1;
            }
            case NORTH, SOUTH -> {
                leftX = 1;
                upY = 1;
            }
            default -> {
            }
        }

        result[0] = (toX + leftX * d + upX * d) / 2;
        result[1] = (toY + leftY * d + upY * d) / 2;
        result[2] = (toZ + leftZ * d + upZ * d) / 2;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        return getList(side);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return getList(side);
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return sprite != null ? sprite : ClientSide.instance.getMissingIcon();
    }

    public int faceCount() {
        int count = getList(null).size();

        for (final Direction f : Direction.values()) {
            count += getList(f).size();
        }

        return count;
    }

    public static ChiseledBlockBakedModel createFromTexture(TextureAtlasSprite findTexture, ChiselRenderType layer) {
        ChiseledBlockBakedModel out = new ChiseledBlockBakedModel();
        out.sprite = findTexture;
        out.myLayer = layer;
        return out;
    }
}
