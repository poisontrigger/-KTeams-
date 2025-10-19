package poisontrigger.kteams.Protection;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import poisontrigger.kteams.Kteams;
import poisontrigger.kteams.util.LogHandler;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public class VanillaExplosion {
    @SubscribeEvent
    public static void onExplode(ExplosionEvent.Detonate e) {
        net.minecraft.world.World world = e.getWorld();

        String l = "[EXPLOSION] " + e.getExplosion() ;
        LogHandler.get().log(l,"EXPLOSION");
        BlockPos pos1 = null;
        boolean isProtected = false;
        if (world.isRemote) return;
        poisontrigger.kteams.Teams.TeamData data = poisontrigger.kteams.Teams.TeamData.get(world);
        java.util.HashMap<Long, Boolean> chunkClaimed = new java.util.HashMap<>();

        java.util.Iterator<net.minecraft.util.math.BlockPos> it = e.getAffectedBlocks().iterator();

        while (it.hasNext()) {
            net.minecraft.util.math.BlockPos pos = it.next();
            net.minecraft.util.math.ChunkPos cp = new net.minecraft.util.math.ChunkPos(pos);

            long key = net.minecraft.util.math.ChunkPos.asLong(cp.x, cp.z);
            Boolean claimed = chunkClaimed.get(key);
            if (claimed == null) {

                String ownerId = data.getChunkOwner(cp);
                claimed = (ownerId != null && !ownerId.isEmpty());
                chunkClaimed.put(key, claimed);
            }

            if (claimed) {
                it.remove();
                isProtected = true;
                pos1 = pos;
                
            }
        }
        if (isProtected) {
            l = "[EXPLOSION PREVENTED] " + "at: " + pos1 + e.getExplosion();
            LogHandler.get().log(l, "EXPLOSION");
        }

        e.getAffectedEntities().removeIf(ent -> {
            ChunkPos cp = new ChunkPos(ent.getPosition());
             String ownerId = data.getChunkOwner(cp);
             return ownerId != null && !ownerId.isEmpty();
         });
        

    }
}
