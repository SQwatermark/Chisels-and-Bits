package mod.chiselsandbits.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod root configuration.
 */
public class Configuration {
    private final ClientConfiguration clientConfig;
    private final ServerConfiguration serverConfig;
    private final CommonConfiguration commonConfig;

    /**
     * Builds configuration tree.
     *
     */
    public Configuration() {
        var cli = new ForgeConfigSpec.Builder().configure(ClientConfiguration::new);
        var  ser = new ForgeConfigSpec.Builder().configure(ServerConfiguration::new);
        var  com = new ForgeConfigSpec.Builder().configure(CommonConfiguration::new);
        clientConfig = cli.getLeft();
        serverConfig = ser.getLeft();
        commonConfig = com.getLeft();

        ForgeConfigSpec clientConfigSpec = cli.getRight();
        ForgeConfigSpec serverConfigSpec = ser.getRight();
        ForgeConfigSpec commonConfigSpec = com.getRight();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientConfigSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverConfigSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonConfigSpec);
    }

    public ClientConfiguration getClient() {
        return clientConfig;
    }

    public ServerConfiguration getServer() {
        return serverConfig;
    }

    public CommonConfiguration getCommon() {
        return commonConfig;
    }


}