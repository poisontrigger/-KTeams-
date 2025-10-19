package poisontrigger.kteams;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


import static poisontrigger.kteams.Kteams.saveLogs;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public class ServerTick {


        private static int tickCounter = 0;
        // 20 tps * 60 s = 1200 ticks
        private static final int FLUSH_EVERY_TICKS = 1200;

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;

            if (++tickCounter >= FLUSH_EVERY_TICKS) {
                tickCounter = 0;
                MinecraftServer srv = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
                if (srv == null) {
                    return;
                }
                try {
                    saveLogs(srv);
                } catch (Throwable ignore) {}
            }
        }
}
