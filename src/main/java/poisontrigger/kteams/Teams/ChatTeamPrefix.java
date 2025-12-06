package poisontrigger.kteams.Teams;


import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import poisontrigger.kteams.util.LpDetect;

@SideOnly(Side.SERVER)
public final class ChatTeamPrefix {

    @SideOnly(Side.SERVER)
    public static void apply(String player, String teamTag) {
        if(LpDetect.hasLuckPerms() == false){return;}
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        server.getCommandManager().executeCommand(server, "lp user " + player + " meta set team " + teamTag);
    }
    @SideOnly(Side.SERVER)
    public static void clear (String player){
        if(LpDetect.hasLuckPerms() == false){return;}
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        server.getCommandManager().executeCommand(server,"lp user " + player + " meta unset team" );
    }
}