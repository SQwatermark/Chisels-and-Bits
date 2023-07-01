package mod.chiselsandbits.client.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.common.util.TransformationHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

/**
 * 仅仅规定了模型在各种观察模式下的显示
 */
public abstract class BaseBakedPerspectiveModel implements BakedModel {

    protected RandomSource RANDOM = RandomSource.create();

    private static final ItemTransform ground;
    private static final ItemTransform gui;
    private static final ItemTransform fixed;
    private static final ItemTransform firstPerson_righthand;
    private static final ItemTransform firstPerson_lefthand;
    private static final ItemTransform thirdPerson_righthand;
    private static final ItemTransform thirdPerson_lefthand;

    static {
        gui = getMatrix(0, 0, 0, 30, 225, 0, 0.625f);
        ground = getMatrix(0, 3 / 16.0f, 0, 0, 0, 0, 0.25f);
        fixed = getMatrix(0, 0, 0, 0, 0, 0, 0.5f);
        thirdPerson_lefthand = thirdPerson_righthand = getMatrix(0, 2.5f / 16.0f, 0, 75, 45, 0, 0.375f);
        firstPerson_righthand = firstPerson_lefthand = getMatrix(0, 0, 0, 0, 45, 0, 0.40f);
    }

    private static ItemTransform getMatrix(float transX, float transY, float transZ, float rotX, float rotY, float rotZ, float scaleXYZ) {
        Vector3f rotation = new Vector3f(rotX, rotY, rotZ);
        Vector3f translation = new Vector3f(transX, transY, transZ);
        Vector3f scale = new Vector3f(scaleXYZ, scaleXYZ, scaleXYZ);
        return new ItemTransform(rotation, translation, scale, new Vector3f());
    }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        switch (transformType) {
            case FIRST_PERSON_LEFT_HAND:
                firstPerson_lefthand.apply(applyLeftHandTransform, poseStack);
                return this;
            case FIRST_PERSON_RIGHT_HAND:
                firstPerson_righthand.apply(applyLeftHandTransform, poseStack);
                return this;
            case THIRD_PERSON_LEFT_HAND:
                thirdPerson_lefthand.apply(applyLeftHandTransform, poseStack);
                return this;
            case THIRD_PERSON_RIGHT_HAND:
                thirdPerson_righthand.apply(applyLeftHandTransform, poseStack);
            case FIXED:
                fixed.apply(applyLeftHandTransform, poseStack);
                return this;
            case GROUND:
                ground.apply(applyLeftHandTransform, poseStack);
                return this;
            case GUI:
                gui.apply(applyLeftHandTransform, poseStack);
                return this;
            default:
        }
        fixed.apply(applyLeftHandTransform, poseStack);
        return this;
    }
}
