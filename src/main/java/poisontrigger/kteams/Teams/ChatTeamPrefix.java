package poisontrigger.kteams.Teams;


import net.minecraftforge.fml.common.Mod;
import org.bukkit.Bukkit;
import poisontrigger.kteams.Kteams;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public final class ChatTeamPrefix {

    public static void apply(String player, String teamTag) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"lp user " + player + " meta set team " + teamTag);
    }
    public static void clear (String player){
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"lp user " + player + " meta unset team" );
    }
}