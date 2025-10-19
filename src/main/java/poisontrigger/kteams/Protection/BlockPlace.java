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

public class BlockPlace {

    @SubscribeEvent

    public void onBlockBreak(BlockEvent.EntityPlaceEvent e){

        Entity en = e.getEntity();
        if(! (en instanceof EntityPlayer)) return;
        EntityPlayerMP p = (EntityPlayerMP) e.getEntity();
        String l = "[PLACE ATTEMPT] at: " + e.getPos();
        LogHandler.get().log(l,p.getUniqueID(),p.getName(),"BLOCK_PLACE_ATTEMPT");

        if (Bypass.isOpBypass(p)) {logSucess(e); return;}
        ChunkPos cp = new ChunkPos(e.getPos());
        TeamData data = TeamData.get(p.world);
        if (data.getChunkOwner(cp) == null) {logSucess(e); return;}
        if (data.isMemberOf(p.getUniqueID(),data.getChunkOwner(cp))) {logSucess(e); return;}
        e.setCanceled(true);

        l = "[PLACE FAILED CLAIMED] at: " + e.getPos();
        LogHandler.get().log(l,p.getUniqueID(),p.getName(),"BLOCK_PLACE_FAILED_CLAIMED");


    }

    private void logSucess(BlockEvent.EntityPlaceEvent e){
        Entity en = e.getEntity();
        if(! (en instanceof EntityPlayer)) return;
        EntityPlayerMP p = (EntityPlayerMP) e.getEntity();

        String l = "[PLACE SUCCESS] at: " + e.getPos();
        LogHandler.get().log(l,p.getUniqueID(),p.getName(),"BLOCK_PLACE_SUCCESS");
    }

}
