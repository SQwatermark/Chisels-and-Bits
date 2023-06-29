package mod.chiselsandbits.modes;

import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.world.item.ItemStack;

/**
 * 工具模式
 */
public interface IToolMode {

	void setMode(ItemStack ei);

	LocalStrings getName();

	String name();

	boolean isDisabled();

	int ordinal();

}
