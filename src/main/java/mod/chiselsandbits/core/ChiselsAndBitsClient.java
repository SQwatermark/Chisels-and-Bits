package mod.chiselsandbits.core;

import mod.chiselsandbits.client.gui.SpriteIconPositioning;
import mod.chiselsandbits.client.model.loader.ChiseledBlockModelLoader;
import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.modes.PositivePatternMode;
import mod.chiselsandbits.modes.TapeMeasureModes;
import mod.chiselsandbits.utils.TextureUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

public class ChiselsAndBitsClient {

    @OnlyIn(Dist.CLIENT)
    public static void onClientInit(FMLClientSetupEvent event) {
        // load this after items are created...
        //TODO: Load clipboard
        //CreativeClipboardTab.load( new File( configFile.getParent(), MODID + "_clipboard.cfg" ) );

        ClientSide.instance.preinit();
        ClientSide.instance.init();
        ClientSide.instance.postinit(ChiselsAndBits.getInstance());
    }

    @OnlyIn(Dist.CLIENT)
    public static void onModelRegistry(ModelEvent.RegisterGeometryLoaders event) {
        event.register("chiseled_block", ChiseledBlockModelLoader.getInstance());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ChiseledBlockModelLoader.getInstance());
    }

//    @SubscribeEvent
//    @OnlyIn(Dist.CLIENT)
//    public static void registerIconTextures(final TextureStitchEvent.Pre ev) {
//        final TextureAtlas map = ev.getAtlas();
//        if (!map.location().equals(InventoryMenu.BLOCK_ATLAS)) return;
//        ev.addSprite(new ResourceLocation("chiselsandbits", "icons/swap"));
//        ev.addSprite(new ResourceLocation("chiselsandbits", "icons/place"));
//        ev.addSprite(new ResourceLocation("chiselsandbits", "icons/undo"));
//        ev.addSprite(new ResourceLocation("chiselsandbits", "icons/redo"));
//        ev.addSprite(new ResourceLocation("chiselsandbits", "icons/trash"));
//        ev.addSprite(new ResourceLocation("chiselsandbits", "icons/sort"));
//        ev.addSprite(new ResourceLocation("chiselsandbits", "icons/roll_x"));
//        ev.addSprite(new ResourceLocation("chiselsandbits", "icons/roll_z"));
//        ev.addSprite(new ResourceLocation("chiselsandbits", "icons/white"));
//        for (final ChiselMode mode : ChiselMode.values()) {
//            ev.addSprite(new ResourceLocation("chiselsandbits", "icons/" + mode.name().toLowerCase()));
//        }
//
//        for (final PositivePatternMode mode : PositivePatternMode.values()) {
//            ev.addSprite(new ResourceLocation("chiselsandbits", "icons/" + mode.name().toLowerCase()));
//        }
//
//        for (final TapeMeasureModes mode : TapeMeasureModes.values()) {
//            ev.addSprite(new ResourceLocation("chiselsandbits", "icons/" + mode.name().toLowerCase()));
//        }
//    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void retrieveRegisteredIconSprites(TextureStitchEvent.Post ev) {
        TextureAtlas map = ev.getAtlas();
        if (!map.location().equals(InventoryMenu.BLOCK_ATLAS)) return;

        ClientSide.swapIcon = map.getSprite(new ResourceLocation("chiselsandbits", "icons/swap"));
        ClientSide.placeIcon = map.getSprite(new ResourceLocation("chiselsandbits", "icons/place"));
        ClientSide.undoIcon = map.getSprite(new ResourceLocation("chiselsandbits", "icons/undo"));
        ClientSide.redoIcon = map.getSprite(new ResourceLocation("chiselsandbits", "icons/redo"));
        ClientSide.trashIcon = map.getSprite(new ResourceLocation("chiselsandbits", "icons/trash"));
        ClientSide.sortIcon = map.getSprite(new ResourceLocation("chiselsandbits", "icons/sort"));
        ClientSide.roll_x = map.getSprite(new ResourceLocation("chiselsandbits", "icons/roll_x"));
        ClientSide.roll_z = map.getSprite(new ResourceLocation("chiselsandbits", "icons/roll_z"));
        ClientSide.white = map.getSprite(new ResourceLocation("chiselsandbits", "icons/white"));

        for (ChiselMode mode : ChiselMode.values()) {
            loadIcon(map, mode);
        }

        for (PositivePatternMode mode : PositivePatternMode.values()) {
            loadIcon(map, mode);
        }

        for (TapeMeasureModes mode : TapeMeasureModes.values()) {
            loadIcon(map, mode);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void loadIcon(TextureAtlas map, IToolMode mode) {
        SpriteIconPositioning sip = new SpriteIconPositioning();

        ResourceLocation sprite = new ResourceLocation("chiselsandbits", "icons/" + mode.name().toLowerCase());
        ResourceLocation png = new ResourceLocation("chiselsandbits", "textures/icons/" + mode.name().toLowerCase() + ".png");

        sip.sprite = map.getSprite(sprite);

        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(png);
            if (resource.isEmpty()) {
                throw new RuntimeException();
            }
            BufferedImage bi = TextureUtils.readBufferedImage(resource.get().open());

            int bottom = 0;
            int right = 0;
            sip.left = bi.getWidth();
            sip.top = bi.getHeight();

            for (int x = 0; x < bi.getWidth(); x++) {
                for (int y = 0; y < bi.getHeight(); y++) {
                    final int color = bi.getRGB(x, y);
                    final int a = color >> 24 & 0xff;
                    if (a > 0) {
                        sip.left = Math.min(sip.left, x);
                        right = Math.max(right, x);

                        sip.top = Math.min(sip.top, y);
                        bottom = Math.max(bottom, y);
                    }
                }
            }

            sip.height = bottom - sip.top + 1;
            sip.width = right - sip.left + 1;

            sip.left /= bi.getWidth();
            sip.width /= bi.getWidth();
            sip.top /= bi.getHeight();
            sip.height /= bi.getHeight();
        } catch (final IOException e) {
            sip.height = 1;
            sip.width = 1;
            sip.left = 0;
            sip.top = 0;
        }

        ClientSide.instance.setIconForMode(mode, sip);
    }
}
