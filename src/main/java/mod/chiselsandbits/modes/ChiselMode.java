package mod.chiselsandbits.modes;

import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;

/**
 * 雕刻工具的模式
 */
public enum ChiselMode implements IToolMode {
	SINGLE( LocalStrings.ChiselModeSingle ),
	SNAP2( LocalStrings.ChiselModeSnap2 ),
	SNAP4( LocalStrings.ChiselModeSnap4 ),
	SNAP8( LocalStrings.ChiselModeSnap8 ),
	LINE( LocalStrings.ChiselModeLine ),
	PLANE( LocalStrings.ChiselModePlane ),
	CONNECTED_PLANE( LocalStrings.ChiselModeConnectedPlane ),
	CUBE_SMALL( LocalStrings.ChiselModeCubeSmall ),
	CUBE_MEDIUM( LocalStrings.ChiselModeCubeMedium ),
	CUBE_LARGE( LocalStrings.ChiselModeCubeLarge ),
	SAME_MATERIAL( LocalStrings.ChiselModeSameMaterial ),
	DRAWN_REGION( LocalStrings.ChiselModeDrawnRegion ), // 绘制区域
	CONNECTED_MATERIAL( LocalStrings.ChiselModeConnectedMaterial );

	public final LocalStrings string;

	public boolean isDisabled = false;

	public Object binding;

	ChiselMode(LocalStrings str) {
		string = str;
	}

	public static ChiselMode getMode(ItemStack stack) {
		if (stack != null) {
			try {
				final CompoundTag nbt = stack.getTag();
				if ( nbt != null && nbt.contains( "mode" ) ) {
					return valueOf( nbt.getString( "mode" ) );
				}
			} catch ( final IllegalArgumentException iae ) {
				// nope!
			} catch ( final Exception e ) {
				Log.logError( "Unable to determine mode.", e );
			}
		}

		return SINGLE;
	}

	@Override
	public void setMode(ItemStack stack) {
		if (stack != null) {
			stack.addTagElement( "mode", StringTag.valueOf(name()));
		}
	}

	@Override
	public LocalStrings getName()
	{
		return string;
	}

	@Override
	public boolean isDisabled()
	{
		return isDisabled;
	}

	public static ChiselMode castMode(IToolMode chiselMode) {
		if (chiselMode instanceof ChiselMode) {
			return (ChiselMode) chiselMode;
		}
		return ChiselMode.SINGLE;
	}

}
