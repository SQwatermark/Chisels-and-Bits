package mod.chiselsandbits.registry;

import mod.chiselsandbits.blueprints.ItemBlueprint;
import mod.chiselsandbits.config.ModConfig;
import mod.chiselsandbits.debug.ItemApiDebug;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemBitSaw;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.items.ItemMirrorPrint;
import mod.chiselsandbits.items.ItemNegativePrint;
import mod.chiselsandbits.items.ItemPositivePrint;
import mod.chiselsandbits.items.ItemTapeMeasure;
import mod.chiselsandbits.items.ItemWrench;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ModItems extends ModRegistry
{

	final public ItemChisel itemChiselStone;
	final public ItemChisel itemChiselIron;
	final public ItemChisel itemChiselGold;
	final public ItemChisel itemChiselDiamond;

	final public ItemChiseledBit itemBlockBit;
	final public ItemMirrorPrint itemMirrorprint;
	final public ItemPositivePrint itemPositiveprint;
	final public ItemNegativePrint itemNegativeprint;

	final public ItemBlueprint itemBlueprint;

	final public ItemBitBag itemBitBag;
	final public ItemWrench itemWrench;
	final public ItemBitSaw itemBitSawDiamond;
	final public ItemTapeMeasure itemTapeMeasure;

	public ModItems(
			final ModConfig config )
	{
		// register items...
		itemChiselStone = registerItem( config.enableStoneChisel, new ItemChisel( ToolMaterial.STONE ), "chisel_stone" );
		itemChiselIron = registerItem( config.enableIronChisel, new ItemChisel( ToolMaterial.IRON ), "chisel_iron" );
		itemChiselGold = registerItem( config.enableGoldChisel, new ItemChisel( ToolMaterial.GOLD ), "chisel_gold" );
		itemChiselDiamond = registerItem( config.enableDiamondChisel, new ItemChisel( ToolMaterial.DIAMOND ), "chisel_diamond" );
		itemPositiveprint = registerItem( config.enablePositivePrint, new ItemPositivePrint(), "positiveprint" );
		itemNegativeprint = registerItem( config.enableNegativePrint, new ItemNegativePrint(), "negativeprint" );
		itemMirrorprint = registerItem( config.enableMirrorPrint, new ItemMirrorPrint(), "mirrorprint" );
		itemBitBag = registerItem( config.enableBitBag, new ItemBitBag(), "bit_bag" );
		itemWrench = registerItem( config.enableWoodenWrench, new ItemWrench(), "wrench_wood" );
		itemBitSawDiamond = registerItem( config.enableBitSaw, new ItemBitSaw(), "bitsaw_diamond" );
		itemBlockBit = registerItem( config.enableChisledBits, new ItemChiseledBit(), "block_bit" );
		itemBlueprint = registerItem( config.enableBlueprints, new ItemBlueprint(), "blueprint" );
		itemTapeMeasure = registerItem( config.enableTapeMeasure, new ItemTapeMeasure(), "tape_measure" );
		registerItem( config.enableAPITestingItem, new ItemApiDebug(), "debug" );
	}

	public void addRecipes()
	{
		// tools..
		ShapedOreRecipe( itemChiselDiamond, "TS", 'T', "gemDiamond", 'S', "stickWood" );
		ShapedOreRecipe( itemChiselGold, "TS", 'T', "ingotGold", 'S', "stickWood" );
		ShapedOreRecipe( itemChiselIron, "TS", 'T', "ingotIron", 'S', "stickWood" );
		ShapedOreRecipe( itemChiselStone, "TS", 'T', "cobblestone", 'S', "stickWood" );
		ShapedOreRecipe( itemBitSawDiamond, "SSS", "STT", 'T', "gemDiamond", 'S', "stickWood" );
		ShapedOreRecipe( itemWrench, " W ", "WS ", "  S", 'W', "plankWood", 'S', "stickWood" );
		ShapedOreRecipe( itemTapeMeasure, "  S", "ISY", "II ", 'I', "ingotIron", 'S', "string", 'Y', "dyeYellow" );

		// create prints...
		ShapelessOreRecipe( itemPositiveprint, Items.WATER_BUCKET, Items.PAPER, "gemLapis" );
		ShapelessOreRecipe( itemNegativeprint, Items.WATER_BUCKET, Items.PAPER, "dustRedstone" );
		ShapelessOreRecipe( itemMirrorprint, Items.WATER_BUCKET, Items.PAPER, "dustGlowstone" );

		// blue print.
		ShapelessOreRecipe( itemBlueprint, itemPositiveprint, itemPositiveprint, itemPositiveprint );

		// clean patterns...
		ShapelessOreRecipe( itemPositiveprint, new ItemStack( itemPositiveprint, 1, OreDictionary.WILDCARD_VALUE ) );
		ShapelessOreRecipe( itemNegativeprint, new ItemStack( itemNegativeprint, 1, OreDictionary.WILDCARD_VALUE ) );
		ShapelessOreRecipe( itemMirrorprint, new ItemStack( itemMirrorprint, 1, OreDictionary.WILDCARD_VALUE ) );

		// make a bit bag..
		ShapedOreRecipe( itemBitBag, "WWW", "WbW", "WWW", 'W', new ItemStack( Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE ), 'b', new ItemStack( itemBlockBit, 1, OreDictionary.WILDCARD_VALUE ) );
	}

}
