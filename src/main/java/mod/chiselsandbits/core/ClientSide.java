package mod.chiselsandbits.core;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.*;
import mod.chiselsandbits.chiseledblock.*;
import mod.chiselsandbits.chiseledblock.data.*;
import mod.chiselsandbits.chiseledblock.iterators.ChiselIterator;
import mod.chiselsandbits.chiseledblock.iterators.ChiselTypeIterator;
import mod.chiselsandbits.client.*;
import mod.chiselsandbits.client.gui.ChiselsAndBitsMenu;
import mod.chiselsandbits.client.gui.SpriteIconPositioning;
import mod.chiselsandbits.helpers.*;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.interfaces.IPatternItem;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.modes.PositivePatternMode;
import mod.chiselsandbits.modes.TapeMeasureModes;
import mod.chiselsandbits.network.packets.PacketChisel;
import mod.chiselsandbits.network.packets.PacketRotateVoxelBlob;
import mod.chiselsandbits.network.packets.PacketSetColor;
import mod.chiselsandbits.network.packets.PacketSuppressInteraction;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSide {

    private static final Random RANDOM = new Random();
    public static final ClientSide instance = new ClientSide();

    ReadyState readyState = ReadyState.PENDING_PRE;

    private final HashMap<IToolMode, SpriteIconPositioning> chiselModeIcons = new HashMap<>();
    private KeyMapping rotateCCW;
    private KeyMapping rotateCW;
    private KeyMapping undo;
    private KeyMapping redo;
    private KeyMapping modeMenu;
    private KeyMapping addToClipboard;
    private KeyMapping pickBit;
    private KeyMapping offgridPlacement;
    private Stopwatch rotateTimer;

    final public TapeMeasures tapeMeasures = new TapeMeasures();

    public void preinit() {
        readyState = readyState.updateState(ReadyState.TRIGGER_PRE);

        MinecraftForge.EVENT_BUS.register(instance);

    }

    public void init() {
        readyState = readyState.updateState(ReadyState.TRIGGER_INIT);

        // TODO
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHISELED_BLOCK.get(), (Predicate<RenderType>) input -> RenderType.chunkBufferLayers().contains(input));
    }

    /* ============================== 快捷键相关 ============================== */

    public KeyMapping getKeyBinding(ModKeyBinding modKeyBinding) {
        return switch (modKeyBinding) {
            case ROTATE_CCW -> rotateCCW;
            case ROTATE_CW -> rotateCW;
            case UNDO -> undo;
            case REDO -> redo;
            case ADD_TO_CLIPBOARD -> addToClipboard;
            case PICK_BIT -> pickBit;
            case OFFGRID_PLACEMENT -> ClientSide.getOffGridPlacementKey();
            default -> modeMenu;
        };
    }

    @SubscribeEvent
    public static void registerBindingsStatic(RegisterKeyMappingsEvent event) {
        instance.registerBindings(event); // TODO 怪
    }


    public void registerBindings(RegisterKeyMappingsEvent event) {
        for (ChiselMode mode : ChiselMode.values()) {
            mode.binding = registerBind(event, mode.string.toString(), InputConstants.UNKNOWN, "itemGroup.chiselsandbits", ModConflictContext.HOLDING_CHISEL);
        }
        for (PositivePatternMode mode : PositivePatternMode.values()) {
            mode.binding = registerBind(event, mode.string.toString(), InputConstants.UNKNOWN, "itemGroup.chiselsandbits", ModConflictContext.HOLDING_POSTIVEPATTERN);
        }
        for (TapeMeasureModes mode : TapeMeasureModes.values()) {
            mode.binding = registerBind(event, mode.string.toString(), InputConstants.UNKNOWN, "itemGroup.chiselsandbits", ModConflictContext.HOLDING_TAPEMEASURE);
        }
        modeMenu = registerBind(event, "mod.chiselsandbits.other.mode", InputConstants.UNKNOWN, "itemGroup.chiselsandbits", ModConflictContext.HOLDING_MENUITEM);
        rotateCCW = registerBind(event, "mod.chiselsandbits.other.rotate.ccw", InputConstants.UNKNOWN, "itemGroup.chiselsandbits", ModConflictContext.HOLDING_ROTATEABLE);
        rotateCW = registerBind(event, "mod.chiselsandbits.other.rotate.cw", InputConstants.UNKNOWN, "itemGroup.chiselsandbits", ModConflictContext.HOLDING_ROTATEABLE);
        pickBit = registerBind(event, "mod.chiselsandbits.other.pickbit", InputConstants.UNKNOWN, "itemGroup.chiselsandbits", ModConflictContext.HOLDING_ROTATEABLE);
        offgridPlacement = registerBind(event, "mod.chiselsandbits.other.offgrid", InputConstants.UNKNOWN, "itemGroup.chiselsandbits", ModConflictContext.HOLDING_OFFGRID);
        undo = registerBind(event, "mod.chiselsandbits.other.undo", InputConstants.UNKNOWN, "itemGroup.chiselsandbits", KeyConflictContext.IN_GAME);
        redo = registerBind(event, "mod.chiselsandbits.other.redo", InputConstants.UNKNOWN, "itemGroup.chiselsandbits", KeyConflictContext.IN_GAME);
        addToClipboard = registerBind(event, "mod.chiselsandbits.other.add_to_clipboard", InputConstants.UNKNOWN, "itemGroup.chiselsandbits", KeyConflictContext.IN_GAME);
    }

    private KeyMapping registerBind(RegisterKeyMappingsEvent event, String bindingName, InputConstants.Key defaultKey, String groupName, IKeyConflictContext context) {
        final KeyMapping kb = new KeyMapping(bindingName, context, defaultKey, groupName);
        event.register(kb);
        return kb;
    }

    public void postinit(ChiselsAndBits mod) {
        readyState = readyState.updateState(ReadyState.TRIGGER_POST); // TODO
    }

    @SubscribeEvent
    public static void registerColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register(new BlockColorChiseled(), ModBlocks.CHISELED_BLOCK.get());
    }

    @SubscribeEvent
    public static void registerColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register(new ItemColorBits(), ModItems.ITEM_BLOCK_BIT.get());
        event.register(new ItemColorPatterns(), ModItems.ITEM_POSITIVE_PRINT.get());
        event.register(new ItemColorPatterns(), ModItems.ITEM_POSITIVE_PRINT_WRITTEN.get());
        event.register(new ItemColorPatterns(), ModItems.ITEM_NEGATIVE_PRINT.get());
        event.register(new ItemColorPatterns(), ModItems.ITEM_NEGATIVE_PRINT_WRITTEN.get());
        event.register(new ItemColorPatterns(), ModItems.ITEM_MIRROR_PRINT.get());
        event.register(new ItemColorPatterns(), ModItems.ITEM_MIRROR_PRINT_WRITTEN.get());
        event.register(new ItemColorChiseled(), ModBlocks.CHISELED_BLOCK_ITEM.get());
    }

    public static TextureAtlasSprite undoIcon;
    public static TextureAtlasSprite redoIcon;
    public static TextureAtlasSprite trashIcon;

    public static TextureAtlasSprite sortIcon;

    public static TextureAtlasSprite swapIcon;
    public static TextureAtlasSprite placeIcon;

    public static TextureAtlasSprite roll_x;
    public static TextureAtlasSprite roll_z;
    public static TextureAtlasSprite white;

    public SpriteIconPositioning getIconForMode(final IToolMode mode) {
        return chiselModeIcons.get(mode);
    }

    public void setIconForMode(final IToolMode mode, final SpriteIconPositioning positioning) {
        chiselModeIcons.put(mode, positioning);
    }

    @SubscribeEvent
    public void onRenderGUI(RenderGuiEvent.Post event) {
        final ChiselToolType tool = getHeldToolType(lastHand);
        if (tool != null && tool.hasMenu()) {
            final boolean wasVisible = ChiselsAndBitsMenu.instance.isVisible();

            if (!modeMenu.isUnbound() && modeMenu.isDown()) {
                ChiselsAndBitsMenu.instance.actionUsed = false;
                if (ChiselsAndBitsMenu.instance.raiseVisibility())
                    ChiselsAndBitsMenu.instance.getMinecraft().mouseHandler.releaseMouse();
            } else {
                if (!ChiselsAndBitsMenu.instance.actionUsed) {
                    if (ChiselsAndBitsMenu.instance.switchTo != null) {
                        ClientSide.instance.playRadialMenu();
                        ChiselModeManager.changeChiselMode(tool, ChiselModeManager.getChiselMode(getPlayer(), tool, InteractionHand.MAIN_HAND), ChiselsAndBitsMenu.instance.switchTo);
                    }

                    if (ChiselsAndBitsMenu.instance.doAction != null) {
                        ClientSide.instance.playRadialMenu();
                        switch (ChiselsAndBitsMenu.instance.doAction) {
                            case ROLL_X -> {
                                PacketRotateVoxelBlob pri = new PacketRotateVoxelBlob(Axis.X, Rotation.CLOCKWISE_90);
                                ChiselsAndBits.getNetworkChannel().sendToServer(pri);
                            }
                            case ROLL_Z -> {
                                PacketRotateVoxelBlob pri2 = new PacketRotateVoxelBlob(Axis.Z, Rotation.CLOCKWISE_90);
                                ChiselsAndBits.getNetworkChannel().sendToServer(pri2);
                            }
                            case REPLACE_TOGGLE -> {
                                ReplacementStateHandler.getInstance().setReplacing(!ReplacementStateHandler.getInstance().isReplacing());
                                ReflectionWrapper.instance.clearHighlightedStack();
                            }
                            case UNDO -> UndoTracker.getInstance().undo();
                            case REDO -> UndoTracker.getInstance().redo();
                            case BLACK, BLUE, BROWN, CYAN, GRAY, GREEN, LIGHT_BLUE, LIME, MAGENTA, ORANGE, PINK, PURPLE, RED, LIGHT_GRAY, WHITE, YELLOW -> {
                                final PacketSetColor setColor = new PacketSetColor(DyeColor.valueOf(ChiselsAndBitsMenu.instance.doAction.name()),
                                        getHeldToolType(InteractionHand.MAIN_HAND),
                                        ChiselsAndBits.getConfig().getClient().chatModeNotification.get());
                                ChiselsAndBits.getNetworkChannel().sendToServer(setColor);
                                ReflectionWrapper.instance.clearHighlightedStack();
                            }
                        }
                    }
                }

                ChiselsAndBitsMenu.instance.actionUsed = true;
                ChiselsAndBitsMenu.instance.decreaseVisibility();
            }

            if (ChiselsAndBitsMenu.instance.isVisible()) {
                final Window window = event.getWindow();
                ChiselsAndBitsMenu.instance.init(Minecraft.getInstance(), window.getGuiScaledWidth(), window.getGuiScaledHeight());
                ChiselsAndBitsMenu.instance.configure(window.getGuiScaledWidth(), window.getGuiScaledHeight());

                if (!wasVisible) {
                    ChiselsAndBitsMenu.instance.getMinecraft().screen = ChiselsAndBitsMenu.instance;
                    ChiselsAndBitsMenu.instance.getMinecraft().mouseHandler.releaseMouse();
                }

                if (ChiselsAndBitsMenu.instance.getMinecraft().mouseHandler.isMouseGrabbed()) {
                    KeyMapping.releaseAll();
                }
/*
                final int k1 = (int) (Minecraft.getInstance().mouseHelper.getMouseX() * window.getScaledWidth() / window.getWidth());
                final int l1 = (int) (window.getScaledHeight() - Minecraft.getInstance().mouseHelper.getMouseY() * window.getScaledHeight() / window.getHeight() - 1);

                net.minecraftforge.client.ForgeHooksClient.drawScreen(ChiselsAndBitsMenu.instance, event.getMatrixStack(), k1, l1, event.getPartialTicks());*/
            } else {
                if (wasVisible) {
                    ChiselsAndBitsMenu.instance.getMinecraft().mouseHandler.grabMouse();
                }
            }
        }

        if (!undo.isUnbound() && undo.consumeClick()) {
            UndoTracker.getInstance().undo();
        }

        if (!redo.isUnbound() && redo.consumeClick()) {
            UndoTracker.getInstance().redo();
        }

        if (!addToClipboard.isUnbound() && addToClipboard.consumeClick()) {
            final Minecraft mc = Minecraft.getInstance();
            if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK && mc.hitResult instanceof BlockHitResult) {
                final BlockHitResult rayTraceResult = (BlockHitResult) mc.hitResult;

                try {
                    final IBitAccess access = ChiselsAndBits.getApi().getBitAccess(mc.level, rayTraceResult.getBlockPos());
                    final ItemStack is = access.getBitsAsItem(null, ItemType.CHISLED_BLOCK, false);

//                    CreativeClipboardTab.addItem(is);
                } catch (final CannotBeChiseled e) {
                    // nope.
                }
            }
        }

        if (!pickBit.isUnbound() && pickBit.consumeClick()) {
            final Minecraft mc = Minecraft.getInstance();
            if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK && mc.hitResult instanceof BlockHitResult) {
                BlockHitResult rayTraceResult = (BlockHitResult) mc.hitResult;

                try {
                    final BitLocation bl = new BitLocation(rayTraceResult, BitOperation.CHISEL);
                    final IBitAccess access = ChiselsAndBits.getApi().getBitAccess(mc.level, bl.getBlockPos());
                    final IBitBrush brush = access.getBitAt(bl.getBitX(), bl.getBitY(), bl.getBitZ());
                    final ItemStack is = brush.getItemStack(1);
                    doPick(is);
                } catch (final CannotBeChiseled e) {
                    // nope.
                }
            }
        }

//        if (type == ElementType.HOTBAR && ChiselsAndBits.getConfig().getClient().enableToolbarIcons.get())
//        {
//            final Minecraft mc = Minecraft.getInstance();
//            final Window window = event.getWindow();
//
//            if (!mc.player.isSpectator())
//            {
//                final Gui sc = mc.gui;
//
//                for (int slot = 0; slot < 9; ++slot)
//                {
//                    final ItemStack stack = mc.player.getInventory().items.get(slot);
//                    if (stack.getItem() instanceof ItemChisel)
//                    {
//                        final ChiselToolType toolType = getToolTypeForItem(stack);
//                        IToolMode mode = toolType.getMode(stack);
//
//                        if (!ChiselsAndBits.getConfig().getClient().perChiselMode.get() && tool == ChiselToolType.CHISEL)
//                        {
//                            mode = ChiselModeManager.getChiselMode(mc.player, ChiselToolType.CHISEL, lastHand);
//                        }
//
//                        final int x = window.getGuiScaledWidth() / 2 - 90 + slot * 20 + 2;
//                        final int y = window.getGuiScaledHeight() - 16 - 3;
//
//                        RenderSystem.setShaderColor(1, 1, 1, 1.0f);
//                        Minecraft.getInstance().getTextureManager().bindForSetup(InventoryMenu.BLOCK_ATLAS);
//                        final TextureAtlasSprite sprite = chiselModeIcons.get(mode) == null ? getMissingIcon() : chiselModeIcons.get(mode).sprite;
//
//                        RenderSystem.enableBlend();
//                        sc.blit(event.getMatrixStack(), x + 1, y + 1, 0, 8, 8, sprite);
//                        RenderSystem.disableBlend();
//                    }
//                }
//            }
//        }
    }

    RandomSource soundRandomSource = RandomSource.create();

    public void playRadialMenu() {
        final double volume = ChiselsAndBits.getConfig().getClient().radialMenuVolume.get();
        if (volume >= 0.0001f) {
            final SoundInstance psr = new SimpleSoundInstance(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, (float) volume, 1.0f, soundRandomSource, getPlayer().blockPosition());
            Minecraft.getInstance().getSoundManager().play(psr);
        }
    }

    private boolean doPick(@Nonnull ItemStack result) {
        Player player = getPlayer();

        for (int x = 0; x < 9; x++) {
            ItemStack stack = player.getInventory().getItem(x);
            if (ItemStack.isSameItemSameTags(stack, result)) {
                player.getInventory().selected = x;
                return true;
            }
        }

        if (!player.isCreative()) {
            return false;
        }

        int slot = player.getInventory().getFreeSlot();
        if (slot < 0 || slot >= 9) {
            slot = player.getInventory().selected;
        }

        // update inventory..
        player.getInventory().setItem(slot, result);
        player.getInventory().selected = slot;

        // update server...
        final int j = player.inventoryMenu.slots.size() - 9 + player.getInventory().selected;
        Minecraft.getInstance().gameMode.handleCreativeModeItemAdd(player.getInventory().getItem(player.getInventory().selected), j);
        return true;
    }

    public ChiselToolType getHeldToolType(
            final InteractionHand Hand) {
        final Player player = getPlayer();

        if (player == null) {
            return null;
        }

        final ItemStack is = player.getItemInHand(Hand);
        return getToolTypeForItem(is);
    }

    private ChiselToolType getToolTypeForItem(ItemStack is) {

        if (is == null) {
            return null;
        }

        Item item = is.getItem();

        if (item instanceof ItemChisel) {
            return ChiselToolType.CHISEL;
        }

        if (item instanceof ItemChiseledBit) {
            return ChiselToolType.BIT;
        }

        if (item instanceof ItemBlockChiseled) {
            return ChiselToolType.CHISELED_BLOCK;
        }

        if (item == ModItems.ITEM_TAPE_MEASURE.get()) {
            return ChiselToolType.TAPEMEASURE;
        }

        if (item == ModItems.ITEM_POSITIVE_PRINT.get()) {
            return ChiselToolType.POSITIVEPATTERN;
        }

        if (item == ModItems.ITEM_POSITIVE_PRINT_WRITTEN.get()) {
            return ChiselToolType.POSITIVEPATTERN;
        }

        if (item == ModItems.ITEM_NEGATIVE_PRINT.get()) {
            return ChiselToolType.NEGATIVEPATTERN;
        }

        if (item == ModItems.ITEM_NEGATIVE_PRINT_WRITTEN.get()) {
            return ChiselToolType.NEGATIVEPATTERN;
        }

        if (item == ModItems.ITEM_MIRROR_PRINT.get()) {
            return ChiselToolType.MIRRORPATTERN;
        }

        if (item == ModItems.ITEM_MIRROR_PRINT_WRITTEN.get()) {
            return ChiselToolType.MIRRORPATTERN;
        }

        return null;
    }

    @SubscribeEvent
    public void drawingInteractionPrevention(final RightClickBlock pie) {
        if (pie.getLevel() != null && pie.getLevel().isClientSide) {
            final ChiselToolType tool = getHeldToolType(pie.getHand());
            final IToolMode chMode = ChiselModeManager.getChiselMode(getPlayer(), tool, pie.getHand());

            final BitLocation other = getStartPos();
            if ((chMode == ChiselMode.DRAWN_REGION || tool == ChiselToolType.TAPEMEASURE) && other != null) {
                // this handles the client side, but the server side will fire separately.
                pie.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void interaction(final TickEvent.PlayerTickEvent event) {
        if (event.type != TickEvent.Type.CLIENT || event.phase != TickEvent.Phase.END) { // TODO 这样判断是否正确
            return;
        }

        Player player = Minecraft.getInstance().player;

        if (player != null && !player.getInventory().getSelected().isEmpty()) {
            lastTool = getToolTypeForItem(player.getInventory().getSelected());
        }

        // used to prevent hyper chisels... it's actually far worse than you might think...
        // 用于防止一下凿太多
        if (event.phase == TickEvent.Phase.START && !Minecraft.getInstance().options.keyAttack.isUnbound() && !Minecraft.getInstance().options.keyAttack.isDown()) {
            ItemChisel.resetDelay();
        }

        // 处理快捷键
        if (!getToolKey().isUnbound() && !getToolKey().isDown() && lastTool == ChiselToolType.CHISEL) {
            if (ticksSinceRelease >= 4) {
                if (drawStart != null) {
                    drawStart = null;
                    lastHand = InteractionHand.MAIN_HAND;
                }

                ticksSinceRelease = 0;
            } else {
                ticksSinceRelease++;
            }
        } else {
            ticksSinceRelease = 0;
        }

        if (!rotateCCW.isUnbound() && rotateCCW.isDown()) {
            if (rotateTimer == null || rotateTimer.elapsed(TimeUnit.MILLISECONDS) > 200) {
                rotateTimer = Stopwatch.createStarted();
                final PacketRotateVoxelBlob p = new PacketRotateVoxelBlob(Axis.Y, Rotation.COUNTERCLOCKWISE_90);
                ChiselsAndBits.getNetworkChannel().sendToServer(p);
            }
        }

        if (!rotateCW.isUnbound() && rotateCW.isDown()) {
            if (rotateTimer == null || rotateTimer.elapsed(TimeUnit.MILLISECONDS) > 200) {
                rotateTimer = Stopwatch.createStarted();
                final PacketRotateVoxelBlob p = new PacketRotateVoxelBlob(Axis.Y, Rotation.CLOCKWISE_90);
                ChiselsAndBits.getNetworkChannel().sendToServer(p);
            }
        }

        for (final ChiselMode mode : ChiselMode.values()) {
            final KeyMapping kb = (KeyMapping) mode.binding;
            if (!kb.isUnbound() && kb.isDown()) {
                final ChiselToolType tool = getHeldToolType(lastHand);
                if (tool.isBitOrChisel()) {
                    ChiselModeManager.changeChiselMode(tool, ChiselModeManager.getChiselMode(getPlayer(), tool, lastHand), mode);
                }
            }
        }

        for (final PositivePatternMode mode : PositivePatternMode.values()) {
            final KeyMapping kb = (KeyMapping) mode.binding;
            if (!kb.isUnbound() && kb.isDown()) {
                final ChiselToolType tool = getHeldToolType(lastHand);
                if (tool == ChiselToolType.POSITIVEPATTERN) {
                    ChiselModeManager.changeChiselMode(tool, ChiselModeManager.getChiselMode(getPlayer(), tool, lastHand), mode);
                }
            }
        }

        for (final TapeMeasureModes mode : TapeMeasureModes.values()) {
            final KeyMapping kb = (KeyMapping) mode.binding;
            if (!kb.isUnbound() && kb.isDown()) {
                final ChiselToolType tool = getHeldToolType(lastHand);
                if (tool == ChiselToolType.TAPEMEASURE) {
                    ChiselModeManager.changeChiselMode(tool, ChiselModeManager.getChiselMode(getPlayer(), tool, lastHand), mode);
                }
            }
        }
    }

    boolean wasDrawing = false;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void drawHighlight(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        PoseStack stack = event.getPoseStack();
        stack.pushPose();

        Vec3 renderView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        stack.translate(-renderView.x, -renderView.y, -renderView.z);

        ChiselToolType tool = getHeldToolType(lastHand);
        final IToolMode chMode = ChiselModeManager.getChiselMode(getPlayer(), tool, lastHand);
        if (chMode == ChiselMode.DRAWN_REGION) {
            tool = lastTool;
        }

        tapeMeasures.setPreviewMeasure(null, null, chMode, null);

        if (tool == ChiselToolType.TAPEMEASURE) {
            final Player player = getPlayer();
            final HitResult mop = Minecraft.getInstance().hitResult;
            ;
            final Level theWorld = player.level();

            if (mop != null && mop.getType() == HitResult.Type.BLOCK) {
                final BlockHitResult blockRayTraceResult = (BlockHitResult) mop;
                final BitLocation location = new BitLocation(blockRayTraceResult, BitOperation.CHISEL);
                if (theWorld.getWorldBorder().isWithinBounds(location.blockPos)) {
                    final BitLocation other = getStartPos();
                    if (other != null) {
                        tapeMeasures.setPreviewMeasure(other, location, chMode, getPlayer().getItemInHand(lastHand));

                        if (!getToolKey().isUnbound() && !getToolKey().isDown()) {
                            tapeMeasures.addMeasure(other, location, chMode, getPlayer().getItemInHand(lastHand));
                            drawStart = null;
                            lastHand = InteractionHand.MAIN_HAND;
                        }
                    }
                }
            }
        }

        tapeMeasures.render(stack, event.getPartialTick());

        final boolean isDrawing = (chMode == ChiselMode.DRAWN_REGION || tool == ChiselToolType.TAPEMEASURE) && getStartPos() != null;
        if (isDrawing != wasDrawing) {
            wasDrawing = isDrawing;
            final PacketSuppressInteraction packet = new PacketSuppressInteraction(isDrawing);
            ChiselsAndBits.getNetworkChannel().sendToServer(packet);
        }

        stack.popPose();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void drawHighlight(final RenderHighlightEvent.Block event) {
        try {
            final PoseStack stack = event.getPoseStack();
            stack.pushPose();
            Vec3 renderView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            stack.translate(-renderView.x, -renderView.y, -renderView.z);

            ChiselToolType tool = getHeldToolType(lastHand);
            final IToolMode chMode = ChiselModeManager.getChiselMode(getPlayer(), tool, lastHand);
            if (chMode == ChiselMode.DRAWN_REGION) {
                tool = lastTool;
            }

            if (tool != null && tool.isBitOrChisel() && chMode != null) {
                final Player player = Minecraft.getInstance().player;
                final float partialTicks = event.getPartialTick();
                final HitResult mop = Minecraft.getInstance().hitResult;
                final Level theWorld = player.level();

                if (mop == null || mop.getType() != HitResult.Type.BLOCK) {
                    return;
                }

                boolean showBox = false;
                if (mop.getType() == HitResult.Type.BLOCK) {
                    final BlockHitResult rayTraceResult = (BlockHitResult) mop;
                    final BitLocation location = new BitLocation(rayTraceResult, getLastBitOperation(player, lastHand, getPlayer().getItemInHand(lastHand)));
                    if (theWorld.getWorldBorder().isWithinBounds(location.blockPos)) {
                        // this logic originated in the vanilla bounding box...
                        final BlockState state = theWorld.getBlockState(location.blockPos);

                        final boolean isChisel = getDrawnTool() == ChiselToolType.CHISEL;
                        final boolean isBit = getHeldToolType(InteractionHand.MAIN_HAND) == ChiselToolType.BIT;
                        final BlockEntityChiseledBlock data = ModUtil.getChiseledTileEntity(theWorld, location.blockPos, false);

                        final VoxelRegionSrc region = new VoxelRegionSrc(theWorld, location.blockPos, 1);
                        final VoxelBlob vb = data != null ? data.getBlob() : new VoxelBlob();

                        if (isChisel && data == null) {
                            showBox = true;
                            vb.fill(1);
                        }

                        final BitLocation other = getStartPos();
                        if (chMode == ChiselMode.DRAWN_REGION && other != null) {
                            final ChiselIterator oneEnd = ChiselTypeIterator.create(VoxelBlob.dim,
                                    location.bitX,
                                    location.bitY,
                                    location.bitZ,
                                    VoxelBlob.NULL_BLOB,
                                    ChiselMode.SINGLE,
                                    Direction.UP,
                                    tool == ChiselToolType.BIT);
                            final ChiselIterator otherEnd = ChiselTypeIterator.create(VoxelBlob.dim,
                                    other.bitX,
                                    other.bitY,
                                    other.bitZ,
                                    VoxelBlob.NULL_BLOB,
                                    ChiselMode.SINGLE,
                                    Direction.UP,
                                    tool == ChiselToolType.BIT);

                            final AABB a =
                                    oneEnd.getBoundingBox(VoxelBlob.NULL_BLOB, false).move(location.blockPos.getX(), location.blockPos.getY(), location.blockPos.getZ());
                            final AABB b = otherEnd.getBoundingBox(VoxelBlob.NULL_BLOB, false).move(other.blockPos.getX(), other.blockPos.getY(), other.blockPos.getZ());

                            final AABB bb = a.minmax(b);

                            final double maxChiseSize = ChiselsAndBits.getConfig().getClient().maxDrawnRegionSize.get() + 0.001;
                            if (bb.maxX - bb.minX <= maxChiseSize && bb.maxY - bb.minY <= maxChiseSize && bb.maxZ - bb.minZ <= maxChiseSize) {
                                RenderHelper.drawSelectionBoundingBoxIfExists(event.getPoseStack(), bb, BlockPos.ZERO, player, partialTicks, false);

                                if (!getToolKey().isUnbound() && !getToolKey().isDown()) {
                                    final PacketChisel pc = new PacketChisel(getLastBitOperation(player, lastHand, player.getItemInHand(lastHand)), location, other,
                                            Direction.UP,
                                            ChiselMode.DRAWN_REGION, lastHand);

                                    if (pc.doAction(getPlayer()) > 0) {
                                        ChiselsAndBits.getNetworkChannel().sendToServer(pc);
                                        ClientSide.placeSound(theWorld, location.blockPos, 0);
                                    }

                                    drawStart = null;
                                    lastHand = InteractionHand.MAIN_HAND;
                                    lastTool = ChiselToolType.CHISEL;
                                }
                            }
                        } else {
                            final BlockEntity te = theWorld.getChunkAt(location.blockPos).getBlockEntity(location.blockPos, LevelChunk.EntityCreationType.CHECK);
                            boolean isBitBlock = te instanceof BlockEntityChiseledBlock;
                            final boolean isBlockSupported = BlockBitInfo.canChisel(state);

                            if (!(isBitBlock || isBlockSupported)) {
                                final BlockEntityChiseledBlock tebc = ModUtil.getChiseledTileEntity(theWorld, location.blockPos, false);
                                if (tebc != null) {
                                    final VoxelBlob vx = tebc.getBlob();
                                    if (vx.get(location.bitX, location.bitY, location.bitZ) != 0) {
                                        isBitBlock = true;
                                    }
                                }
                            }

                            if (theWorld.isEmptyBlock(location.blockPos) || isBitBlock || isBlockSupported) {
                                final ChiselIterator i = ChiselTypeIterator.create(VoxelBlob.dim,
                                        location.bitX,
                                        location.bitY,
                                        location.bitZ,
                                        region,
                                        ChiselMode.castMode(chMode),
                                        rayTraceResult.getDirection(),
                                        !isChisel);
                                final AABB bb = i.getBoundingBox(vb, isChisel);
                                RenderHelper.drawSelectionBoundingBoxIfExists(event.getPoseStack(), bb, location.blockPos, player, partialTicks, false);
                                showBox = false;
                            } else if (isBit) {
                                final VoxelBlob j = new VoxelBlob();
                                j.fill(1);
                                final ChiselIterator i = ChiselTypeIterator.create(VoxelBlob.dim,
                                        location.bitX,
                                        location.bitY,
                                        location.bitZ,
                                        j,
                                        ChiselMode.castMode(chMode),
                                        rayTraceResult.getDirection(),
                                        !isChisel);
                                final AABB bb = snapToSide(i.getBoundingBox(j, isChisel), rayTraceResult.getDirection());
                                RenderHelper.drawSelectionBoundingBoxIfExists(event.getPoseStack(), bb, location.blockPos, player, partialTicks, false);
                            }
                        }
                    }

                    if (!showBox) {
                        event.setCanceled(true);
                    }
                }
            }

        } finally {
            event.getPoseStack().popPose();
        }


    }

    private BitOperation getLastBitOperation(Player player, InteractionHand lastHand2, ItemStack heldItem) {
        return lastTool == ChiselToolType.BIT ? ItemChiseledBit.getBitOperation(player, lastHand, player.getItemInHand(lastHand)) : BitOperation.CHISEL;
    }

    private AABB snapToSide(AABB boundingBox, Direction sideHit) {
        if (boundingBox != null) {
            switch (sideHit) {
                case DOWN:
                    return new AABB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
                case EAST:
                    return new AABB(boundingBox.maxX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                case NORTH:
                    return new AABB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
                case SOUTH:
                    return new AABB(boundingBox.minX, boundingBox.minY, boundingBox.maxZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                case UP:
                    return new AABB(boundingBox.minX, boundingBox.maxY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                case WEST:
                    return new AABB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
                default:
                    break;
            }
        }

        return boundingBox;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void drawLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        // important and used for tesr / block rendering.
        ++lastRenderedFrame;

        if (Minecraft.getInstance().options.hideGui) {
            return;
        }

        // now render the ghosts...
        final Player player = Minecraft.getInstance().player;
        final float partialTicks = event.getPartialTick();
        final HitResult mop = Minecraft.getInstance().hitResult;
        final Level theWorld = player.level();
        final ItemStack currentItem = player.getMainHandItem();

        final double x = player.xOld + (player.getX() - player.xOld) * partialTicks;
        final double y = player.yOld + (player.getY() - player.yOld) * partialTicks;
        final double z = player.zOld + (player.getZ() - player.zOld) * partialTicks;

        if (mop == null) {
            return;
        }

        if (ModUtil.isHoldingPattern(player)) {
            if (mop.getType() != HitResult.Type.BLOCK) {
                return;
            }

            final BlockHitResult rayTraceResult = (BlockHitResult) mop;
            final IToolMode mode = ChiselModeManager.getChiselMode(player, ChiselToolType.POSITIVEPATTERN, InteractionHand.MAIN_HAND);

            final BlockPos pos = rayTraceResult.getBlockPos();
            final BlockPos partial = null;

            final BlockState s = theWorld.getBlockState(pos);
            if (!(s.getBlock() instanceof BlockChiseled) && !BlockBitInfo.canChisel(s)) {
                return;
            }

            if (!ModItems.ITEM_NEGATIVE_PRINT.get().isWritten(currentItem)) {
                return;
            }

            final ItemStack item = ModItems.ITEM_NEGATIVE_PRINT.get().getPatternedItem(currentItem, false);
            if (item == null || !item.hasTag()) {
                return;
            }

            final int rotations = ModUtil.getRotations(player, ModUtil.getSide(currentItem));

            if (mode == PositivePatternMode.PLACEMENT) {
                doGhostForChiseledBlock(event.getPoseStack(), x, y, z, theWorld, player, (BlockHitResult) mop, item, item, rotations);
                return;
            }

            if (!item.isEmpty()) {
                final BlockEntityChiseledBlock tebc = ModUtil.getChiseledTileEntity(theWorld, pos, false);
                Object cacheRef = tebc != null ? tebc : s;
                if (cacheRef instanceof BlockEntityChiseledBlock) {
                    cacheRef = ((BlockEntityChiseledBlock) cacheRef).getBlobStateReference();
                }

                RenderSystem.depthFunc(GL11.GL_ALWAYS);
                showGhost(event.getPoseStack(), currentItem, item, rayTraceResult.getBlockPos(), player, rotations, x, y, z, rayTraceResult.getDirection(), partial, cacheRef);
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
            }
        } else if (ModUtil.isHoldingChiseledBlock(player)) {
            if (mop.getType() != HitResult.Type.BLOCK) {
                return;
            }

            if (!currentItem.hasTag()) {
                return;
            }

            final int rotations = ModUtil.getRotations(player, ModUtil.getSide(currentItem));
            doGhostForChiseledBlock(event.getPoseStack(), x, y, z, theWorld, player, (BlockHitResult) mop, currentItem, currentItem, rotations);
        }
    }

    private void doGhostForChiseledBlock(
            final PoseStack matrixStack,
            final double x,
            final double y,
            final double z,
            final Level theWorld,
            final Player player,
            final BlockHitResult mop,
            final ItemStack currentItem,
            final ItemStack item,
            final int rotations) {
        final BlockPos offset = mop.getBlockPos();

        if (ClientSide.offGridPlacement(player)) {
            final BitLocation bl = new BitLocation(mop, BitOperation.PLACE);
            showGhost(matrixStack, currentItem, item, bl.blockPos, player, rotations, x, y, z, mop.getDirection(), new BlockPos(bl.bitX, bl.bitY, bl.bitZ), null);
        } else {
            boolean canMerge = false;
            if (currentItem.hasTag()) {
                final BlockEntityChiseledBlock tebc = ModUtil.getChiseledTileEntity(theWorld, offset, true);

                if (tebc != null) {
                    final VoxelBlob blob = ModUtil.getBlobFromStack(currentItem, player);
                    canMerge = tebc.canMerge(blob);
                }
            }

            BlockPos newOffset = offset;
            final Block block = theWorld.getBlockState(newOffset).getBlock();
            final InteractionHand hand = player.getUsedItemHand() != null ? player.getUsedItemHand() : InteractionHand.MAIN_HAND;
            if (!canMerge && !ClientSide.offGridPlacement(player) && !block.canBeReplaced(theWorld.getBlockState(newOffset),
                    new BlockPlaceContext(player, hand, player.getItemInHand(hand), mop))) {
                newOffset = offset.relative(mop.getDirection());
            }

            final BlockEntity newTarget = theWorld.getBlockEntity(newOffset);

            if (theWorld.isEmptyBlock(newOffset) || newTarget instanceof BlockEntityChiseledBlock || (theWorld.getBlockEntity(newOffset) instanceof BlockEntityChiseledBlock && theWorld.getBlockState(newOffset)
                    .getBlock()
                    .canBeReplaced(theWorld.getBlockState(newOffset),
                            new BlockPlaceContext(player, hand, player.getItemInHand(hand), new BlockHitResult(
                                    mop.getLocation().add(mop.getDirection().getStepX(), mop.getDirection().getStepY(), mop.getDirection().getStepZ()),
                                    mop.getDirection(),
                                    mop.getBlockPos().offset(mop.getDirection().getNormal()),
                                    mop.isInside()
                            )))) || (!(theWorld.getBlockEntity(newOffset) instanceof BlockEntityChiseledBlock) && theWorld.getBlockState(newOffset)
                    .getBlock()
                    .canBeReplaced(theWorld.getBlockState(newOffset),
                            new BlockPlaceContext(player, hand, player.getItemInHand(hand), mop)))

            ) {
                final BlockEntityChiseledBlock test = ModUtil.getChiseledTileEntity(theWorld, newOffset, false);
                showGhost(matrixStack, currentItem, item, newOffset, player, rotations, x, y, z, mop.getDirection(), null, test == null ? null : test.getBlobStateReference());
            }
        }
    }

    private ItemStack previousItem;
    private int previousRotations;
    private Object previousModel;
    private Object previousCacheRef;
    private IntegerBox modelBounds;
    private boolean isVisible = true;
    private boolean isUnplaceable = true;
    private BlockPos lastPartial;
    private BlockPos lastPos;
    int displayStatus = 0;

    private void showGhost(
            final PoseStack matrixStack,
            final ItemStack refItem,
            final ItemStack item,
            final BlockPos blockPos,
            final Player player,
            final int rotationCount,
            final double x,
            final double y,
            final double z,
            final Direction side,
            final BlockPos partial,
            final Object cacheRef) {
        BakedModel baked = null;

        if (previousCacheRef == cacheRef && samePos(lastPos, blockPos) && previousItem == refItem && previousRotations == rotationCount && previousModel != null && samePos(
                lastPartial,
                partial)) {
            baked = (BakedModel) previousModel;
        } else {
            int rotations = rotationCount;

            previousItem = refItem;
            previousRotations = rotations;
            previousCacheRef = cacheRef;
            lastPos = blockPos;
            lastPartial = partial;

            final NBTBlobConverter c = new NBTBlobConverter();
            c.readChisleData(ModUtil.getSubCompound(item, ModUtil.NBT_BLOCKENTITYTAG, false), VoxelBlob.VERSION_ANY);
            VoxelBlob blob = c.getBlob();

            while (rotations-- > 0) {
                blob = blob.spin(Axis.Y);
            }

            modelBounds = blob.getBounds();

            fail:
            if (refItem.getItem() == ModItems.ITEM_NEGATIVE_PRINT.get()) {
                final VoxelBlob pattern = blob;

                if (cacheRef instanceof VoxelBlobStateReference) {
                    blob = ((VoxelBlobStateReference) cacheRef).getVoxelBlob();
                } else if (cacheRef instanceof BlockState) {
                    blob = new VoxelBlob();
                    blob.fill(ModUtil.getStateId((BlockState) cacheRef));
                } else {
                    break fail;
                }

                final BitIterator it = new BitIterator();
                while (it.hasNext()) {
                    if (it.getNext(pattern) == 0) {
                        it.setNext(blob, 0);
                    }
                }
            }

            c.setBlob(blob);

            final Block blk = Block.byItem(item.getItem());
            final ItemStack is = c.getItemStack(false);

            if (is == null || is.isEmpty()) {
                isVisible = false;
            } else {
                baked = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(is);
                previousModel = baked = baked.getOverrides().resolve(baked, is, (ClientLevel) player.getCommandSenderWorld(), player, 0);

                if (refItem.getItem() instanceof IPatternItem) {
                    isVisible = true;
                } else {
                    isVisible = true;
                    //TODO: Figure out the hitvector here. Might need to pass that down stream.
                    isUnplaceable = !ItemBlockChiseled.tryPlaceBlockAt(blk, item, player, player.getCommandSenderWorld(), blockPos, side, InteractionHand.MAIN_HAND, 0.5, 0.5, 0.5, partial, false);
                }
            }
        }

        if (!isVisible) {
            return;
        }

        matrixStack.pushPose();
        matrixStack.translate(blockPos.getX() - x, blockPos.getY() - y - player.getEyeHeight(), blockPos.getZ() - z);
        if (partial != null) {
            final BlockPos t = ModUtil.getPartialOffset(side, partial, modelBounds);
            final double fullScale = 1.0 / VoxelBlob.dim;
            matrixStack.translate(t.getX() * fullScale, t.getY() * fullScale, t.getZ() * fullScale);
        }

        RenderHelper.renderGhostModel(matrixStack, baked, player.getCommandSenderWorld(), blockPos, isUnplaceable, LevelRenderer.getLightColor(player.getCommandSenderWorld(), blockPos),
                OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();
    }

    private boolean samePos(
            final BlockPos lastPartial2,
            final BlockPos partial) {
        if (lastPartial2 == partial) {
            return true;
        }

        if (lastPartial2 == null || partial == null) {
            return false;
        }

        return partial.equals(lastPartial2);
    }

    public Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    public boolean addHitEffects(
            final Level world,
            final BlockHitResult target,
            final BlockState state,
            final ParticleEngine effectRenderer) {
        final ItemStack hitWith = getPlayer().getMainHandItem();
        if (hitWith != null && (hitWith.getItem() instanceof ItemChisel || hitWith.getItem() instanceof ItemChiseledBit)) {
            return true; // no
            // effects!
        }

        final BlockPos pos = target.getBlockPos();
        final float boxOffset = 0.1F;

        AABB bb = world.getBlockState(pos).getBlock().getShape(state, world, pos, CollisionContext.empty()).bounds();
        ;

        double x = RANDOM.nextDouble() * (bb.maxX - bb.minX - boxOffset * 2.0F) + boxOffset + bb.minX;
        double y = RANDOM.nextDouble() * (bb.maxY - bb.minY - boxOffset * 2.0F) + boxOffset + bb.minY;
        double z = RANDOM.nextDouble() * (bb.maxZ - bb.minZ - boxOffset * 2.0F) + boxOffset + bb.minZ;

        switch (target.getDirection()) {
            case DOWN:
                y = bb.minY - boxOffset;
                break;
            case EAST:
                x = bb.maxX + boxOffset;
                break;
            case NORTH:
                z = bb.minZ - boxOffset;
                break;
            case SOUTH:
                z = bb.maxZ + boxOffset;
                break;
            case UP:
                y = bb.maxY + boxOffset;
                break;
            case WEST:
                x = bb.minX - boxOffset;
                break;
            default:
                break;
        }

        effectRenderer.add((new TerrainParticle((ClientLevel) world, x, y, z, 0.0D, 0.0D, 0.0D, state))
                .updateSprite(state, pos)
                .setPower(0.2F)
                .scale(0.6F));

        return true;
    }

    @SubscribeEvent
    public void wheelEvent(
            final InputEvent.MouseScrollingEvent me) {
        final int dwheel = (int) me.getScrollDelta();
        if (me.isCanceled() || dwheel == 0) {
            return;
        }

        final Player player = ClientSide.instance.getPlayer();
        final ItemStack is = player.getMainHandItem();

        if (dwheel != 0 && is != null && is.getItem() instanceof IItemScrollWheel && player.isShiftKeyDown()) {
            ((IItemScrollWheel) is.getItem()).scroll(player, is, dwheel);
            me.setCanceled(true);
        }
    }

    public static void placeSound(
            final Level world,
            final BlockPos pos,
            final int stateID) {
        final BlockState state = ModUtil.getStateById(stateID);
        final Block block = state.getBlock();
        world.playLocalSound(pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                DeprecationHelper.getSoundType(block).getPlaceSound(),
                SoundSource.BLOCKS,
                (DeprecationHelper.getSoundType(block).getVolume() + 1.0F) / 16.0F,
                DeprecationHelper.getSoundType(block).getPitch() * 0.9F,
                false);
    }

    public static void breakSound(
            final Level world,
            final BlockPos pos,
            final int extractedState) {
        final BlockState state = ModUtil.getStateById(extractedState);
        final Block block = state.getBlock();
        world.playLocalSound(pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                DeprecationHelper.getSoundType(block).getBreakSound(),
                SoundSource.BLOCKS,
                (DeprecationHelper.getSoundType(block).getVolume() + 1.0F) / 16.0F,
                DeprecationHelper.getSoundType(block).getPitch() * 0.9F,
                false);
    }

    private BitLocation drawStart;
    private int ticksSinceRelease = 0;
    private int lastRenderedFrame = Integer.MIN_VALUE;

    public int getLastRenderedFrame() {
        return lastRenderedFrame;
    }

    public BitLocation getStartPos() {
        return drawStart;
    }

    public void pointAt(
            @Nonnull final ChiselToolType type,
            @Nonnull final BitLocation pos,
            @Nonnull final InteractionHand hand) {
        if (drawStart == null) {
            drawStart = pos;
            lastTool = type;
            lastHand = hand;
        }
    }

    @Nonnull
    ChiselToolType lastTool = ChiselToolType.CHISEL;

    @Nonnull
    InteractionHand lastHand = InteractionHand.MAIN_HAND;

    public void setLastTool(@Nonnull final ChiselToolType lastTool) {
        this.lastTool = lastTool;
    }

    KeyMapping getToolKey() {
        if (lastTool == ChiselToolType.CHISEL) {
            return Minecraft.getInstance().options.keyAttack;
        } else {
            return Minecraft.getInstance().options.keyUse;
        }
    }

    public boolean addBlockDestroyEffects(
            @Nonnull final Level world,
            @Nonnull final BlockPos pos,
            BlockState state,
            final ParticleEngine effectRenderer) {
        if (!state.isAir()) {
            VoxelShape voxelshape = state.getShape(world, pos);
            double d0 = 0.25D;
            voxelshape.forAllBoxes((p_228348_3_, p_228348_5_, p_228348_7_, p_228348_9_, p_228348_11_, p_228348_13_) -> {
                double d1 = Math.min(1.0D, p_228348_9_ - p_228348_3_);
                double d2 = Math.min(1.0D, p_228348_11_ - p_228348_5_);
                double d3 = Math.min(1.0D, p_228348_13_ - p_228348_7_);
                int i = Math.max(2, Mth.ceil(d1 / 0.25D));
                int j = Math.max(2, Mth.ceil(d2 / 0.25D));
                int k = Math.max(2, Mth.ceil(d3 / 0.25D));

                for (int l = 0; l < i; ++l) {
                    for (int i1 = 0; i1 < j; ++i1) {
                        for (int j1 = 0; j1 < k; ++j1) {
                            double d4 = ((double) l + 0.5D) / (double) i;
                            double d5 = ((double) i1 + 0.5D) / (double) j;
                            double d6 = ((double) j1 + 0.5D) / (double) k;
                            double d7 = d4 * d1 + p_228348_3_;
                            double d8 = d5 * d2 + p_228348_5_;
                            double d9 = d6 * d3 + p_228348_7_;
                            effectRenderer.add((new TerrainParticle((ClientLevel) world,
                                    (double) pos.getX() + d7,
                                    (double) pos.getY() + d8,
                                    (double) pos.getZ() + d9,
                                    d4 - 0.5D,
                                    d5 - 0.5D,
                                    d6 - 0.5D,
                                    state)).updateSprite(state, pos));
                        }
                    }
                }
            });
        }

        return true;
    }

    public TextureAtlasSprite getMissingIcon() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("missingno"));
    }

    public String getModeKey() {
        return getKeyName(modeMenu);
    }

    public ChiselToolType getDrawnTool() {
        return lastTool;
    }

    public boolean holdingShift() {
        return (!Minecraft.getInstance().options.keyShift.isUnbound() && Minecraft.getInstance().options.keyShift.isDown()) || Screen.hasShiftDown();
    }

    public String getKeyName(
            KeyMapping bind) {
        if (bind == null) {
            return LocalStrings.noBind.getLocal();
        }

        if (bind.getKey().getValue() == 0 && bind.getDefaultKey().getValue() != 0) {
            //TODO: This previously changed the resulting string to something easier to understand. Not sure that is still needed.
            return DeprecationHelper.translateToLocal(bind.saveString());
        }

        if (bind.getKey().getValue() == 0) {
            return '"' + DeprecationHelper.translateToLocal(bind.getName());
        }

        return makeMoreFrendly(bind.saveString());
    }

    private String makeMoreFrendly(
            String displayName) {
        return DeprecationHelper.translateToLocal(displayName)
                .replace("LMENU", LocalStrings.leftAlt.getLocal())
                .replace("RMENU", LocalStrings.rightAlt.getLocal())
                .replace("LSHIFT", LocalStrings.leftShift.getLocal())
                .replace("RSHIFT", LocalStrings.rightShift.getLocal())
                .replace("key.keyboard.", "");
    }

    public static boolean offGridPlacement(
            Player player) {
        if (player instanceof FakePlayer) {
            return false;
        }

        if (player.getCommandSenderWorld().isClientSide) {
            return !getOffGridPlacementKey().isUnbound() && getOffGridPlacementKey().isDown();
        }

        throw new RuntimeException("checking keybinds on server.");
    }

    public static KeyMapping getOffGridPlacementKey() {
        if (!ClientSide.instance.offgridPlacement.isUnbound() && ClientSide.instance.offgridPlacement.isDefault()) {
            return Minecraft.getInstance().options.keyShift;
        }

        return ClientSide.instance.offgridPlacement;
    }
}
