package mod.chiselsandbits.render;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.render.bit.BitItemSmartModel;
import mod.chiselsandbits.render.chiseledblock.ChiseledBlockSmartModel;
import mod.chiselsandbits.render.patterns.PrintSmartModel;
import mod.chiselsandbits.utils.Constants;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SmartModelManager {

    private static final SmartModelManager INSTANCE = new SmartModelManager();

    public static SmartModelManager getInstance() {
        return INSTANCE;
    }

    private boolean setup = false;
    private final HashMap<ResourceLocation, BakedModel> models = new HashMap<>();
    private final List<ModelResourceLocation> res = new ArrayList<>();
    private final List<ICacheClearable> clearable = new ArrayList<>();

    private SmartModelManager() {
    }

    private void setup() {
        if (setup)
            return;

        setup = true;
        ChiseledBlockSmartModel smartModel = new ChiseledBlockSmartModel(); // TODO 这是唯一一处实例化，是否具有意义？
        add(Constants.DataGenerator.CHISELED_BLOCK_MODEL, smartModel);

//        for (RegistryObject<BlockChiseled> bc : ModBlocks.getMaterialToBlockConversions().values()) {
//            add(new ResourceLocation(ChiselsAndBits.MODID, bc.get().name), smartModel);
//        }

        ChiselsAndBits.getInstance().addClearable(smartModel);

        add(new ResourceLocation(ChiselsAndBits.MODID, "models/item/block_bit"), new BitItemSmartModel());
        add(new ResourceLocation(ChiselsAndBits.MODID, "models/item/positiveprint_written_preview"), new PrintSmartModel("positiveprint", ModItems.ITEM_POSITIVE_PRINT_WRITTEN.get()));
        add(new ResourceLocation(ChiselsAndBits.MODID, "models/item/negativeprint_written_preview"), new PrintSmartModel("negativeprint", ModItems.ITEM_NEGATIVE_PRINT_WRITTEN.get()));
        add(new ResourceLocation(ChiselsAndBits.MODID, "models/item/mirrorprint_written_preview"), new PrintSmartModel("mirrorprint", ModItems.ITEM_MIRROR_PRINT_WRITTEN.get()));
    }

    private void add(
            final ResourceLocation modelLocation,
            final BakedModel modelGen) {
        final ResourceLocation second = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath().substring(1 + modelLocation.getPath().lastIndexOf('/')));

        if (modelGen instanceof ICacheClearable) {
            clearable.add((ICacheClearable) modelGen);
        }

        res.add(new ModelResourceLocation(modelLocation, "normal"));
        res.add(new ModelResourceLocation(second, "normal"));

        res.add(new ModelResourceLocation(modelLocation, "inventory"));
        res.add(new ModelResourceLocation(second, "inventory"));

        res.add(new ModelResourceLocation(modelLocation, "multipart"));
        res.add(new ModelResourceLocation(second, "multipart"));

        models.put(modelLocation, modelGen);
        models.put(second, modelGen);

        models.put(new ModelResourceLocation(modelLocation, "normal"), modelGen);
        models.put(new ModelResourceLocation(second, "normal"), modelGen);

        models.put(new ModelResourceLocation(modelLocation, "inventory"), modelGen);
        models.put(new ModelResourceLocation(second, "inventory"), modelGen);

        models.put(new ModelResourceLocation(modelLocation, "multipart"), modelGen);
        models.put(new ModelResourceLocation(second, "multipart"), modelGen);
    }

    public void textureStichEvent(TextureStitchEvent.Post stitch) {
        ChiselsAndBits.getInstance().clearCache();
    }

    public void onModelBakeEvent(ModelEvent.ModifyBakingResult event) {
        setup();
        for (ICacheClearable c : clearable) {
            c.clearCache();
        }
		// TODO 这个是否有意义？模型不是通过资源文件设置的吗？
        for (ModelResourceLocation rl : res) {
            event.getModels().put(rl, getModel(rl));
        }
    }

    private BakedModel getModel(
            final ResourceLocation modelLocation) {
        try {
            return models.get(modelLocation);
        } catch (final Exception e) {
            throw new RuntimeException("The Model: " + modelLocation.toString() + " was not available was requested.");
        }
    }

    private static final class Setup {

    }
}
