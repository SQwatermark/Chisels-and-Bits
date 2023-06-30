package mod.chiselsandbits.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mod.chiselsandbits.client.model.ChiseledBlockModel;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;

/**
 * 雕刻的几何加载器，用于为雕刻方块使用特殊的模型烘焙方法
 * <p>
 * 在模型的资源文件中指定 "loader": "chiselsandbits:chiseled_block"
 */
public final class ChiseledBlockModelLoader implements IGeometryLoader<ChiseledBlockModel>, ResourceManagerReloadListener {

    private static final ChiseledBlockModelLoader INSTANCE = new ChiseledBlockModelLoader();

    public static ChiseledBlockModelLoader getInstance() {
        return INSTANCE;
    }

    private ChiseledBlockModelLoader() {
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        ChiselsAndBits.getInstance().clearCache();
    }

    @Override
    public ChiseledBlockModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new ChiseledBlockModel();
    }

}
