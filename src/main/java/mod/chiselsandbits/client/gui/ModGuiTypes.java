package mod.chiselsandbits.client.gui;

import mod.chiselsandbits.bitbag.BagInventoryMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import java.lang.reflect.Constructor;

@SuppressWarnings( "unused" )
public enum ModGuiTypes
{

	BitBag( BagInventoryMenu.class );

    public final Constructor<?> container_construtor;
	public final Constructor<?> gui_construtor;

	ModGuiTypes(
			final Class<? extends AbstractContainerMenu> c)
	{
        Class<? extends AbstractContainerMenu> container;
        try
		{
			container = c;
			container_construtor = container.getConstructor( Player.class, Level.class, int.class, int.class, int.class );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( e );
		}

		// by default...
		Class<?> g = null;
		Constructor<?> g_construtor = null;

		// attempt to get gui class/constructor...
		try
		{
			g = (Class<?>) container.getMethod( "getGuiClass" ).invoke( null );
			g_construtor = g.getConstructor( Player.class, Level.class, int.class, int.class, int.class );
		}
		catch ( final Exception e )
		{
			// Only throw error if this is a client...
			if (EffectiveSide.get().isClient())
			{
				throw new RuntimeException( e );
			}

		}

        final Class<?> gui = g;
		gui_construtor = g_construtor;

	}
}
