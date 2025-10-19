package poisontrigger.kteams.util;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class configHandler {
    public static Configuration cfg;

    // Cache


    // Flag Capture

    public static float capRadius;
    public static int capMaxPoints;
    public static float capDeltaScaler;
    public static int capBonus;
    public static int capDelay;
    public static float flagRadius = 8.0f;
    public static float flagHeight = 10.0f;
    public static boolean ignoreSpectators = true;
    public static boolean ignoreCreative = false;
    // Team Config
    public static int maxClaimCommandRadius;
    public static int maxClaims;
    public static int homeWarmupSeconds;

    public static void init(File configDir){
        if (cfg == null){
            cfg = new Configuration(new File(configDir, "kteams.cfg"));
            sync();
        }else sync();

    }

    public static void sync(){

        String C_CAPTURE = "capture";
        String C_TEAMS = "teams";

        capDelay   = cfg.getInt("tickInterval", C_CAPTURE, 5, 1, 40, "Ticks between capture updates.");
        capRadius         = cfg.getFloat("radius", C_CAPTURE, 7.0f, 1.0f, 32.0f, "Player check radius (blocks). RENDER");
        capMaxPoints      = cfg.getInt("maxPoints", C_CAPTURE, 20000, 100, 1000000, "Maximum capture points.");
        capDeltaScaler     = cfg.getFloat("deltaScale", C_CAPTURE, 20.0f, 0f, 1000f, "Multiplier for points.");
        capBonus           = cfg.getInt("capBonus", C_CAPTURE, 1000, 0, 100000, "Bonus points after capture.");
        maxClaimCommandRadius           = cfg.getInt("claimMaxRadius", C_TEAMS, 4, 0, 8, "Max Claim Radius.");
        maxClaims           = cfg.getInt("claimMax", C_TEAMS, (4+1)*(4+1), 0, (8+1)*(8+1), "Max Claim Size.");
        flagRadius = cfg.getFloat("flagRadius", C_CAPTURE,8.0f,1,64, "Maximum capturing radius. LOGIC" );
        flagHeight = cfg.getFloat("flagHeight", C_CAPTURE,8.0f,1,64, "Maximum capturing height from flag." );
        ignoreSpectators = cfg.getBoolean("ignoreSpectators", C_CAPTURE,true, "Should spectators be forbidden from capturing points?" );
        ignoreCreative = cfg.getBoolean("ignoreCreative", C_CAPTURE,false, "Should players in creative mode be forbidden from capturing points?" );
        homeWarmupSeconds=cfg.getInt("homeWarmup", C_TEAMS, 5,0, 60,"How long should it take to run the /t home command");
        if (cfg.hasChanged()) cfg.save();
    }

    @Mod.EventBusSubscriber(modid = "kteams")
    public static class SyncEvent {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
            if ("kteams".equals(e.getModID())) {
                cfg.load();
                sync();
            }
        }
    }

}
