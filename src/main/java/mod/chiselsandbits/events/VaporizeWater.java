package mod.chiselsandbits.events;

import mod.chiselsandbits.api.EventFullBlockRestoration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VaporizeWater
{

	@SubscribeEvent
	public void handle(EventFullBlockRestoration e) {
		if (e.getState().getBlock() == Blocks.WATER && e.getWorld().dimensionType().ultraWarm())
		{
            double i = e.getPos().getX();
            double j = e.getPos().getY();
            double k = e.getPos().getZ();
            e.getWorld().playLocalSound(i,j,k, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (e.getWorld().random.nextFloat() - e.getWorld().random.nextFloat()) * 0.8F, true);

            for(int l = 0; l < 8; ++l) {
                e.getWorld().addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(), 0.0D, 0.0D, 0.0D);
            }

			e.getWorld().setBlockAndUpdate( e.getPos(), Blocks.AIR.defaultBlockState() );
			e.setCanceled(true);
		}
	}

}
