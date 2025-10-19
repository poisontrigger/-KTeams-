package poisontrigger.kteams.Teams;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import poisontrigger.kteams.Kteams;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public final class ChatTeamPrefix {

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent e) {
        EntityPlayerMP p = e.getPlayer();
        if (p == null || p.world == null || p.world.isRemote) return;

        TeamData data = TeamData.get(p.world);
        if (data == null) return;

        String teamId = data.getTeamIdOf(p.getUniqueID());
        if (teamId == null) return;

        TeamData.Team team = data.getAllTeamsView().get(teamId);
        String display = (team != null && team.name != null && !team.name.isEmpty())
                ? team.name : teamId;


        ITextComponent prefix = new TextComponentString("§b[" + display + "] §r");

        ITextComponent wrapped = new TextComponentString("");
        wrapped.appendSibling(prefix).appendSibling(e.getComponent());

        e.setComponent(wrapped);
    }
}