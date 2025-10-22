package poisontrigger.kteams.util;


import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import poisontrigger.kteams.Kteams;
import poisontrigger.kteams.Teams.ChatTeamPrefix;
import poisontrigger.kteams.Teams.TeamData;


@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public final class onPlayerLogin {


@SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        // Server side only

        EntityPlayerMP mp = (net.minecraft.entity.player.EntityPlayerMP) e.player;

        // Grab the player's team
        String id = poisontrigger.kteams.Teams.TeamData.get(mp.world).getTeamIdOf(mp.getUniqueID());
            ChatTeamPrefix.apply(mp.getName(), "&7[&f"+id+"&7]&r");
        // Fallback when no team is set
        String idF = (id != null ? id : "wilderness");

        // Send only to the logging-in player
        System.out.println("[Send Packet to:] "+ mp);
        poisontrigger.kteams.network.Net.sendClientTeam(mp, idF);

    }
}
