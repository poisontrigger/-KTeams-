package poisontrigger.kteams.Protection;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import poisontrigger.kteams.Kteams;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public class OpenDoors  {
    @SubscribeEvent
    public static void onTrample(net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent e) {
        if (e.getWorld().isRemote) return;
        if (poisontrigger.kteams.Teams.TeamData.get(e.getWorld()).getChunkOwner(new net.minecraft.util.math.ChunkPos(e.getPos())) != null) {
            e.setCanceled(true);
        }
    }
}
