package poisontrigger.kteams.Teams;


import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.bukkit.Bukkit;
import poisontrigger.kteams.Kteams;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public final class ChatTeamPrefix {

    @SideOnly(Side.SERVER)
    public static void apply(String player, String teamTag) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"lp user " + player + " meta set team " + teamTag);
    }
    @SideOnly(Side.SERVER)
    public static void clear (String player){
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"lp user " + player + " meta unset team" );
    }
}