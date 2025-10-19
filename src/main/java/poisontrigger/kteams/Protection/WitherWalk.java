package poisontrigger.kteams.Protection;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import poisontrigger.kteams.Kteams;
import poisontrigger.kteams.util.LogHandler;


@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
    public final class WitherWalk {

        @SubscribeEvent
        public static void onLivingDestroyBlock(net.minecraftforge.event.entity.living.LivingDestroyBlockEvent e) {
            if (e.getEntity().world.isRemote) return;



            // Only care about the Wither / Dragon
            if (!(e.getEntityLiving() instanceof net.minecraft.entity.boss.EntityWither || e.getEntityLiving() instanceof EntityDragon)) return;

            poisontrigger.kteams.Teams.TeamData data = poisontrigger.kteams.Teams.TeamData.get(e.getEntity().world);
            net.minecraft.util.math.ChunkPos cp = new net.minecraft.util.math.ChunkPos(e.getPos());

            // If the chunk is claimed, cancel the destruction
            if (data.getChunkOwner(cp) != null) {
                String l = "[BOSS BLOCK DAMAGE FAILED CLAIMED] " + "at: " + e.getPos();
                LogHandler.get().log(l, "BOSS_DAMAGE_SUCCESS");
                e.setCanceled(true);
            } else {
                String l = "[BOSS BLOCK DAMAGE PREVENTED] " + "at: " + e.getPos();
                LogHandler.get().log(l, "BOSS_DAMAGE_FAILED");
            }

        }
}
