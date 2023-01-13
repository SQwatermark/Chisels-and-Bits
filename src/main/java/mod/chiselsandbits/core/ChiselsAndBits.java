package mod.chiselsandbits.core;

import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.client.UndoTracker;
import mod.chiselsandbits.config.Configuration;
import mod.chiselsandbits.core.api.ChiselAndBitsAPI;
import mod.chiselsandbits.core.api.IMCHandler;
import mod.chiselsandbits.events.EventPlayerInteract;
import mod.chiselsandbits.events.VaporizeWater;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.network.NetworkChannel;
import mod.chiselsandbits.registry.*;
import mod.chiselsandbits.render.SmartModelManager;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import mod.chiselsandbits.utils.Constants;
import mod.chiselsandbits.utils.LanguageHandler;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Mod(ChiselsAndBits.MODID)
public class ChiselsAndBits
{
	public static final @Nonnull String MODID = Constants.MOD_ID;

	private static ChiselsAndBits instance;
	private final Configuration config;
	private final IChiselAndBitsAPI api = new ChiselAndBitsAPI();
	private final NetworkChannel networkChannel = new NetworkChannel(MODID);

	List<ICacheClearable> cacheClearables = new ArrayList<>();

	public ChiselsAndBits()
	{
	    instance = this;

        LanguageHandler.loadLangPath("assets/chiselsandbits/lang/%s.json"); // hotfix config comments, it's ugly bcs it's gonna be replaced
        config = new Configuration(ModLoadingContext.get().getActiveContainer());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleIMCEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleIMCEvent);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(ChiselsAndBitsClient::onClientInit));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(ChiselsAndBitsClient::onModelRegistry));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(ClientSide.instance));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(ChiselsAndBitsClient::registerIconTextures));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(ChiselsAndBitsClient::retrieveRegisteredIconSprites));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(SmartModelManager.getInstance()::onModelBakeEvent));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(SmartModelManager.getInstance()::textureStichEvent));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup));

        MinecraftForge.EVENT_BUS.addListener(this::handleIdMapping);
        MinecraftForge.EVENT_BUS.register(new VaporizeWater());
        MinecraftForge.EVENT_BUS.register(new EventPlayerInteract());

        ModBlocks.onModConstruction();
        ModItems.onModConstruction();
        ModRecipeSerializers.onModConstruction();
        ModBlockEntityTypes.onModConstruction();

        networkChannel.registerCommonMessages();
	}

	public static ChiselsAndBits getInstance()
	{
		return instance;
	}

	public static Configuration getConfig()
	{
		return instance.config;
	}

	public static IChiselAndBitsAPI getApi()
	{
		return instance.api;
	}

	public static NetworkChannel getNetworkChannel() {
	    return instance.networkChannel;
    }

	private void handleIMCEvent(
			final InterModProcessEvent event )
	{
		final IMCHandler imcHandler = new IMCHandler();
		imcHandler.handleIMCEvent();
	}

	public void commonSetup(
			final FMLCommonSetupEvent event )
	{
		// merge most of the extra materials into the normal set.
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.SPONGE, Material.CLAY );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.HEAVY_METAL, Material.METAL );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.VEGETABLE, Material.PLANT );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.CACTUS, Material.PLANT );
//		ChiselsAndBits.getApi().addEquivilantMaterial( Material.CORAL, Material.STONE );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.WEB, Material.PLANT );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.EXPLOSIVE, Material.STONE );
	}

    public void clientSetup(final FMLClientSetupEvent event) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Mod.EventBusSubscriber.Bus.MOD.bus().get().addListener(ChiseledBlockSmartModel::onConfigurationReload));
    }

	boolean idsHaveBeenMapped = false;

	public void handleIdMapping(final RegistryEvent.IdMappingEvent event) {
		idsHaveBeenMapped = true;
		BlockBitInfo.recalculate();
		clearCache();
	}

	public void clearCache() {
		if (idsHaveBeenMapped) {
			for (final ICacheClearable clearable : cacheClearables) {
				clearable.clearCache();
			}
			addClearable( UndoTracker.getInstance() );
			VoxelBlob.clearCache();
		}
	}

	public void addClearable(final ICacheClearable cache) {
		if ( !cacheClearables.contains( cache ) )
		{
			cacheClearables.add( cache );
		}
	}

}
