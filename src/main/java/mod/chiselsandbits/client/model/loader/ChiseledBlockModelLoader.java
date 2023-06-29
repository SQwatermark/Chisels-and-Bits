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

// TODO 监听资源重载事件
public final class ChiseledBlockModelLoader implements IGeometryLoader<ChiseledBlockModel>, ResourceManagerReloadListener
{

    private static final ChiseledBlockModelLoader INSTANCE = new ChiseledBlockModelLoader();

    public static ChiseledBlockModelLoader getInstance()
    {
        return INSTANCE;
    }

    private ChiseledBlockModelLoader() {
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager)
    {
        ChiselsAndBits.getInstance().clearCache();
    }

    @Override
    public ChiseledBlockModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new ChiseledBlockModel();
    }

}
