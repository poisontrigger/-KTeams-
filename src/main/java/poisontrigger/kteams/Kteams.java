package poisontrigger.kteams;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.logging.Log;
import poisontrigger.kteams.Blocks.ModBlocks;
import poisontrigger.kteams.Blocks.ModTiles;
import poisontrigger.kteams.Blocks.Flag.RenderFlag;
import poisontrigger.kteams.Blocks.Flag.TileEntityFlag;
import poisontrigger.kteams.Protection.BlockBreak;
import poisontrigger.kteams.Protection.BlockPlace;
import poisontrigger.kteams.Teams.TeamCommands;
import poisontrigger.kteams.util.LogHandler;
import poisontrigger.kteams.util.configHandler;
import poisontrigger.kteams.util.Perms;

import java.io.IOException;

@Mod(
        modid = Kteams.MOD_ID,
        name = Kteams.MOD_NAME,
        version = Kteams.VERSION
)
public class Kteams {

    public static final String MOD_ID = "kteams";
    public static final String MOD_NAME = "Kteams";
    public static final String VERSION = "1.0-SNAPSHOT";
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(MOD_ID);
    public static final String NET_CHANNEL = "kteams";

    /** This is the instance of your mod as created by Forge. It will never be null. */
    @Mod.Instance(MOD_ID)
    public static Kteams INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        configHandler.init(event.getSuggestedConfigurationFile());
        ModBlocks.preInit();
        ModTiles.register();
        poisontrigger.kteams.network.Net.init();
    }
    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void preinitC(FMLPreInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFlag.class, new RenderFlag());
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    // Register Event Handlers Here
        MinecraftForge.EVENT_BUS.register(new ChunkLogger());
        MinecraftForge.EVENT_BUS.register(new BlockBreak());
        MinecraftForge.EVENT_BUS.register(new BlockPlace());
        MinecraftForge.EVENT_BUS.register(new ChatCommandLogger());
        Perms.registerNodes();

    }
    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {

        MinecraftForge.EVENT_BUS.register(new poisontrigger.kteams.Teams.TeamMap());

    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {

    }

    /**
     * Forge will automatically look up and bind blocks to the fields in this class
     * based on their registry name.
     */

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event){
        event.registerServerCommand(new TeamCommands());
        LogHandler.get().init(FMLCommonHandler.instance().getSavesDirectory());
    }

    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Blocks {

    }
    @Mod.EventHandler
    public void onServerStopping(net.minecraftforge.fml.common.event.FMLServerStoppingEvent e) {
        net.minecraft.server.MinecraftServer server =
                net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            LOGGER.warn("[KTeams] onServerStopping: server==null; skipping log save.");
            return;
        }
        try {
            saveLogs(server);
        } catch (Throwable t) {
            // never let shutdown crash
            LOGGER.error("[KTeams] Failed to save logs during shutdown", t);
        }
    }


    /**
     * Forge will automatically look up and bind items to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Items {

    }

    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {
       /** Listen for the register event for creating custom items */
       @SubscribeEvent
       public static void addItems(RegistryEvent.Register<Item> event) {

       }
       /** Listen for the register event for creating custom blocks */
       @SubscribeEvent
       public static void addBlocks(RegistryEvent.Register<Block> event) {

       }
    }


    /** Save your mod logs safely; avoids touching worlds that may be unloading. */
    public static void saveLogs(net.minecraft.server.MinecraftServer server) throws IOException {
        java.io.File worldDir = resolveWorldDir(server);
        java.io.File outDir   = new java.io.File(worldDir, "kTeams-logs");
        // write the file (your existing LogHandler)
        poisontrigger.kteams.util.LogHandler.get().saveToFile(outDir, "kteams_logs.csv", /*append*/ true);
        LOGGER.info("[KTeams] Saved logs to {}", new java.io.File(outDir, "kteams_logs.csv").getAbsolutePath());
        LogHandler.get().flushLogs();
    }

    /** Works in dev and prod; doesn’t use getEntityWorld() or logInfo(). */
    private static java.io.File resolveWorldDir(net.minecraft.server.MinecraftServer server) {
        net.minecraft.world.WorldServer overworld = server.getWorld(0);
        if (overworld != null && overworld.getSaveHandler() != null
                && overworld.getSaveHandler().getWorldDirectory() != null) {
            return overworld.getSaveHandler().getWorldDirectory();
        }
        // Fallback path if worlds are already gone
        java.io.File dataDir = server.getDataDirectory(); // run/ or .minecraft/
        String worldName     = server.getFolderName();     // save folder
        return new java.io.File(new java.io.File(dataDir, "saves"), worldName);
    }

}
