package mod.chiselsandbits.api;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * <p> 完整的雕刻方块变回普通方块
 * <p> 若事件取消，则雕刻方块不会变回完整方块
 */
@Cancelable
public class EventFullBlockRestoration extends Event {

    private final Level w;
    private final BlockPos pos;
    private final BlockState restoredState;

    public EventFullBlockRestoration(Level w, BlockPos pos, BlockState restoredState) {
        this.w = w;
        this.pos = pos;
        this.restoredState = restoredState;
    }

    public Level getLevel() {
        return w;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return restoredState;
    }

}
