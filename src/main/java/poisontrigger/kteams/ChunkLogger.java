package poisontrigger.kteams;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.network.Net;
import poisontrigger.kteams.util.LogHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public final class ChunkLogger {
    private static final Map<UUID, String> lastByPlayer = new HashMap<>();

    @SubscribeEvent
    public static void onEnteringChunk(EntityEvent.EnteringChunk e) {
        if (!(e.getEntity() instanceof EntityPlayerMP)) return;
        EntityPlayerMP p = (EntityPlayerMP) e.getEntity();
        if (p.world.isRemote) return;

        ChunkPos cp = new ChunkPos(e.getNewChunkX(), e.getNewChunkZ());
        TeamData data = TeamData.get(p.world);
        if (data == null) return;

        String ownerId = data.getChunkOwner(cp);

        String display = "Wilderness";
        if (ownerId != null) {
            TeamData.Team t = data.getAllTeamsView().get(ownerId);
            display = (t != null && t.name != null && !t.name.isEmpty()) ? t.name : ownerId;
        }

        UUID id = p.getUniqueID();
        String last = lastByPlayer.get(id);
        if (display.equals(last)) return; // unchanged -> no spam

        // update memory and HUD
        lastByPlayer.put(id, display);
        Net.CH.sendTo(new Net.HudText(display), p);

        String msg = "[CHUNK] Entered chunk " + cp.x + ", " + cp.z +
                ": " + display + " (ownerId=" + String.valueOf(ownerId) + ")" +
                (last == null ? "" : " from '" + last + "'");
        LogHandler.get().logPlayer(msg, p, "TERRITORY.ENTER");
    }
}
