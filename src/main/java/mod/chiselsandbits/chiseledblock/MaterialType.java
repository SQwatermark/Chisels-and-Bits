package mod.chiselsandbits.chiseledblock;

import net.minecraft.world.level.material.Material;

public class MaterialType
{

	private final String name;
	private final Material type;

	public MaterialType(
			final String n,
			final Material t )
	{
		name = n;
		type = t;
	}

    public String getName()
    {
        return name;
    }

    public Material getType()
    {
        return type;
    }
}
