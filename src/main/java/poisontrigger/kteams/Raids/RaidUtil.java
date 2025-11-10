package poisontrigger.kteams.Raids;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import poisontrigger.kteams.Blocks.Flag.FlagRegistry;
import poisontrigger.kteams.Teams.TeamData;

public class RaidUtil {

    // TODO: add capabilities to list flag's in registry
    public static boolean teamHasRaidFlag(MinecraftServer server, TeamData.Team team) {
        // Example: scan overworld flags for any TEAM kind belonging to this team
        World world = server.getWorld(0); // overworld
        FlagRegistry reg = FlagRegistry.get(world); // whatever your actual call is

        return true;
    }
}