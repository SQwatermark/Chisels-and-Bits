package mod.chiselsandbits.chiseledblock;

import com.google.common.collect.Lists;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import java.util.List;

/**
 * 不应使用，因为Block一经初始化便造成了一些奇怪的影响
 */
class ReflectionHelperBlock extends Block
{
	public String MethodName;

	private void markMethod()
	{
		MethodName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();
	}

	protected ReflectionHelperBlock()
	{
		super( BlockBehaviour.Properties.of(Material.AIR) );
	}

    @Override
    public VoxelShape getOcclusionShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @Override
    public VoxelShape getBlockSupportShape(final BlockState state, final BlockGetter reader, final BlockPos pos)
    {
        markMethod();
        return null;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        markMethod();
        return null;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context)
    {
        markMethod();
        return null;
    }

    @Override
    public float getDestroyProgress(final BlockState state, final Player player, final BlockGetter worldIn, final BlockPos pos)
    {
        markMethod();
        return 0;
    }

    @Override
    public float getExplosionResistance()
    {
        markMethod();
        return 0;
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootContext.Builder builder)
    {
        markMethod();
        return Lists.newArrayList();
    }
}