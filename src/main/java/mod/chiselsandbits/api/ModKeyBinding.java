package mod.chiselsandbits.api;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ModKeyBinding {
    // Misc
    ROTATE_CCW,
    ROTATE_CW,
    UNDO,
    REDO,
    MODE_MENU,
    ADD_TO_CLIPBOARD,
    PICK_BIT,
    OFFGRID_PLACEMENT,

    // Chisel Modes
    SINGLE,
    SNAP2,
    SNAP4,
    SNAP8,
    LINE,
    PLANE,
    CONNECTED_PLANE,
    CUBE_SMALL,
    CUBE_MEDIUM,
    CUBE_LARGE,
    SAME_MATERIAL,
    DRAWN_REGION,
    CONNECTED_MATERIAL,

    // Positive Pattern Modes
    REPLACE,
    ADDITIVE,
    PLACEMENT,
    IMPOSE,

    // Tape Measure Modes
    BIT,
    BLOCK,
    DISTANCE;
}
