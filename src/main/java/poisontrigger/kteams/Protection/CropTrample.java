package poisontrigger.kteams.Protection;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import poisontrigger.kteams.Kteams;
import poisontrigger.kteams.util.LogHandler;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public class CropTrample {
    @SubscribeEvent
    public static void onTrample(net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent e) {
        if (e.getWorld().isRemote) return;
        String l = "[CROPS TRAMPLE ATTEMPT] at: " + e.getPos();
        LogHandler.get().log(l,e.getEntity().getUniqueID(),e.getEntity().getName(),"CROPS_TRAMPLE_ATTEMPT");
        if (poisontrigger.kteams.Teams.TeamData.get(e.getWorld()).getChunkOwner(new net.minecraft.util.math.ChunkPos(e.getPos())) != null) {
            e.setCanceled(true); // blocks players and mobs from trampling inside claims
             l = "[CROPS TRAMPLE SUCCESS] at: " + e.getPos();
            LogHandler.get().log(l,e.getEntity().getUniqueID(),e.getEntity().getName(),"CROPS_TRAMPLE_SUCCESS");
        }
    }
}