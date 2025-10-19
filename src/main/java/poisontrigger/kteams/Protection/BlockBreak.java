package poisontrigger.kteams.Protection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.util.Bypass;
import poisontrigger.kteams.util.LogHandler;

public class BlockBreak {

        @SubscribeEvent

        public void onBlockBreak(BlockEvent.BreakEvent e){

            EntityPlayerMP p = (EntityPlayerMP) e.getPlayer();

            String l = "[BREAK ATTEMPT] at: " + e.getPos();
            LogHandler.get().log(l, e.getPlayer().getUniqueID(),e.getPlayer().getName(),"BLOCK_BREAK_ATTEMPT");

            if (Bypass.isOpBypass(p)) {logSucess(e); return;}
            ChunkPos cp = new ChunkPos(e.getPos());
            TeamData data = TeamData.get(p.world);
            if (data.getChunkOwner(cp) == null) {logSucess(e); return;}
            if (data.isMemberOf(p.getUniqueID(),data.getChunkOwner(cp))) {logSucess(e); return;}
            e.setCanceled(true);
            l = "[BREAK FAILED CLAIMED] at: " + e.getPos();
            LogHandler.get().log(l, e.getPlayer().getUniqueID(),e.getPlayer().getName(),"BLOCK_BREAK_FAILED_CLAIMED");


        }
    private void logSucess(BlockEvent.BreakEvent e){
        EntityPlayerMP p = (EntityPlayerMP) e.getPlayer();

        String l = "[BREAK SUCCESS] at: " + e.getPos();
        LogHandler.get().log(l,p.getUniqueID(),p.getName(),"BLOCK_BREAK_SUCCESS");
    }
}
