package poisontrigger.kteams.Raids;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.util.configHandler;

public class RaidRewards {

    public static void giveRewards(MinecraftServer server, Raid raid, TeamData.Team winner) {

        String baseCommand = getBaseCommandForType(raid.type);
        if (baseCommand == null || baseCommand.isEmpty()) return;

        for (EntityPlayerMP p : server.getPlayerList().getPlayers()) {
            TeamData data = TeamData.get(p.world);
            if (data.getTeamOf(p.getUniqueID()) == winner) {
                String cmd = baseCommand
                        .replace("%player%", p.getName())
                        .replace("%team%", winner.name);
                server.getCommandManager().executeCommand(server, cmd);
            }
        }
    }

    private static String getBaseCommandForType(Raid.RaidType type) {
        // TODO: load from config
        switch (type) {
            case TYPE1: return configHandler.raidType1Reward;
            case TYPE2: return configHandler.raidType2Reward;
            case TYPE3: return configHandler.raidType3Reward;
            case TYPE4: return configHandler.raidType4Reward;
            case TYPE5: return configHandler.raidType5Reward;
            default:    return null;
        }
    }
}
