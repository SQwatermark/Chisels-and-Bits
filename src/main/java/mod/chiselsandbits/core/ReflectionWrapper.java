package mod.chiselsandbits.core;

import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLLoader;

import java.lang.reflect.Field;
import java.util.Map;

// TODO 这是什么？
public class ReflectionWrapper
{

	final static public ReflectionWrapper instance = new ReflectionWrapper();

	private Field highlightingItemStack = null;
	private Field mapRegSprites = null;

	private Field findField(
			Class<?> clz,
			final String... methods ) throws Exception
	{
		do
		{
			if ( clz == null || clz == Object.class )
			{
				break;
			}

			for ( final String name : methods )
			{
				try
				{
					final Field f = clz.getDeclaredField( name );
					if ( f != null )
					{
						return f;
					}
				}
				catch ( final Exception e )
				{
					// :__(
				}
			}

			clz = clz.getSuperclass();
		}
		while ( true );

		throw new Exception( "Unable to find field " + methods[0] );
	}

	/**
	 * CLASS: net.minecraft.client.gui.GuiIngame
	 *
	 * SRG: lastToolHighlight
	 *
	 * NAME: highlightingItemStack
	 */
	@OnlyIn( Dist.CLIENT )
	public void setHighlightStack(
			final ItemStack is )
	{
		try
		{
			final Object o = Minecraft.getInstance().gui;

			if ( highlightingItemStack == null )
			{
				highlightingItemStack = findField( o.getClass(), "highlightingItemStack", "lastToolHighlight" );
			}

			highlightingItemStack.setAccessible( true );
			highlightingItemStack.set( o, is );
		}
		catch ( final Throwable t )
		{
			// unable to clear the selected stack.
			notifyDeveloper( t );
		}
	}

	@OnlyIn( Dist.CLIENT )
	public void clearHighlightedStack()
	{
		setHighlightStack( ModUtil.getEmptyStack() );
	}

	@OnlyIn( Dist.CLIENT )
	public void endHighlightedStack()
	{
		setHighlightStack( Minecraft.getInstance().player.getMainHandItem() );
	}

	/**
	 * CLASS: net.minecraft.client.renderer.texture.TextureMap
	 *
	 * SRG: field_110574_e
	 *
	 * NAME: mapRegisteredSprites
	 */
	@SuppressWarnings( "unchecked" )
	@OnlyIn( Dist.CLIENT )
	public Map<String, TextureAtlasSprite> getRegSprite(
			final TextureAtlas map )
	{
		try
		{
			if ( mapRegSprites == null )
			{
				mapRegSprites = findField( map.getClass(), "mapUploadedSprites", "texturesByName" );
			}

			mapRegSprites.setAccessible( true );
			return (Map<String, TextureAtlasSprite>) mapRegSprites.get( map );
		}
		catch ( final Throwable t )
		{
			// unable to clear the selected stack.
			notifyDeveloper( t );
		}

		return null;
	}

	private void notifyDeveloper(
			final Throwable t )
	{
		if ( deobfuscatedEnvironment() )
		{
			throw new RuntimeException( t );
		}
	}

	private boolean deobfuscatedEnvironment()
	{
		return !FMLLoader.isProduction();
	}

}
