package mod.chiselsandbits.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod root configuration.
 */
public class Configuration
{
    private final ClientConfiguration clientConfig;
    private final ServerConfiguration serverConfig;
    private final CommonConfiguration commonConfig;

    private final ForgeConfigSpec clientConfigSpec;
    private final ForgeConfigSpec commonConfigSpec;
    private final ForgeConfigSpec serverConfigSpec;

    /**
     * Builds configuration tree.
     *
     * @param modContainer from event
     */
    public Configuration(final ModContainer modContainer)
    {
        final Pair<ClientConfiguration, ForgeConfigSpec> cli = new ForgeConfigSpec.Builder().configure(ClientConfiguration::new);
        final Pair<ServerConfiguration, ForgeConfigSpec> ser = new ForgeConfigSpec.Builder().configure(ServerConfiguration::new);
        final Pair<CommonConfiguration, ForgeConfigSpec> com = new ForgeConfigSpec.Builder().configure(CommonConfiguration::new);
        /**
         * Loaded clientside, not synced
         */
        final ModConfig client = new ModConfig(ModConfig.Type.CLIENT, cli.getRight(), modContainer);
        /**
         * Loaded serverside, synced on connection
         */
        final ModConfig server = new ModConfig(ModConfig.Type.SERVER, ser.getRight(), modContainer);
        /**
         * Loaded on both sides, not synced. Values might differ.
         */
        final ModConfig common = new ModConfig(ModConfig.Type.COMMON, com.getRight(), modContainer);
        clientConfig = cli.getLeft();
        serverConfig = ser.getLeft();
        commonConfig = com.getLeft();

        clientConfigSpec = cli.getRight();
        serverConfigSpec = ser.getRight();
        commonConfigSpec = com.getRight();

        modContainer.addConfig(client);
        modContainer.addConfig(server);
        modContainer.addConfig(common);
    }

    public ClientConfiguration getClient()
    {
        return clientConfig;
    }

    public ServerConfiguration getServer()
    {
        return serverConfig;
    }

    public CommonConfiguration getCommon()
    {
        return commonConfig;
    }

    public ForgeConfigSpec getClientConfigSpec()
    {
        return clientConfigSpec;
    }

    public ForgeConfigSpec getCommonConfigSpec()
    {
        return commonConfigSpec;
    }

    public ForgeConfigSpec getServerConfigSpec()
    {
        return serverConfigSpec;
    }
}