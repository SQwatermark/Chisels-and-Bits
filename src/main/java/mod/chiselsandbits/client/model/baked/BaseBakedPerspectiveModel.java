package mod.chiselsandbits.client.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
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

	private static final Transformation ground;
	private static final Transformation gui;
	private static final Transformation fixed;
	private static final Transformation firstPerson_righthand;
	private static final Transformation firstPerson_lefthand;
	private static final Transformation thirdPerson_righthand;
	private static final Transformation thirdPerson_lefthand;

	static {
		gui = getMatrix( 0, 0, 0, 30, 225, 0, 0.625f );
		ground = getMatrix( 0, 3 / 16.0f, 0, 0, 0, 0, 0.25f );
		fixed = getMatrix( 0, 0, 0, 0, 0, 0, 0.5f );
		thirdPerson_lefthand = thirdPerson_righthand = getMatrix( 0, 2.5f / 16.0f, 0, 75, 45, 0, 0.375f );
		firstPerson_righthand = firstPerson_lefthand = getMatrix( 0, 0, 0, 0, 45, 0, 0.40f );
	}

	private static Transformation getMatrix(float transX, float transY, float transZ, float rotX, float rotY, float rotZ, float scaleXYZ) {
		final Vector3f translation = new Vector3f( transX, transY, transZ );
		final Vector3f scale = new Vector3f( scaleXYZ, scaleXYZ, scaleXYZ );

		final Quaternionf rotation = TransformationHelper.quatFromXYZ(rotX, rotY, rotZ, true);
		return new Transformation(translation, rotation, scale, null);
	}

    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        switch ( transformType ) {
            case FIRST_PERSON_LEFT_HAND:
                poseStack.pushTransformation(firstPerson_lefthand);
                return this;
            case FIRST_PERSON_RIGHT_HAND:
                poseStack.pushTransformation(firstPerson_righthand);
                return this;
            case THIRD_PERSON_LEFT_HAND:
                poseStack.pushTransformation(thirdPerson_lefthand);
                return this;
            case THIRD_PERSON_RIGHT_HAND:
                poseStack.pushTransformation(thirdPerson_righthand);
            case FIXED:
                poseStack.pushTransformation(fixed);
                return this;
            case GROUND:
                poseStack.pushTransformation(ground);
                return this;
            case GUI:
                poseStack.pushTransformation(gui);
                return this;
            default:
        }
        poseStack.pushTransformation(fixed);
        return this;
    }
}
