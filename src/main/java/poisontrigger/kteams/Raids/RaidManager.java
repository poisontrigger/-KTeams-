package poisontrigger.kteams.Raids;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import poisontrigger.kteams.Kteams;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.util.configHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public class RaidManager {
    public static final RaidManager INSTANCE = new RaidManager();

    private final List<Raid> activeRaids = new ArrayList<>();

    private RaidManager() {}

    public boolean hasRaidInvolving(TeamData.Team team) {
        for (Raid r : activeRaids) {
            if (!r.isFinished() && (r.attackers == team || r.defenders == team)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public Raid getRaidBetween(TeamData.Team a, TeamData.Team b) {
        for (Raid r : activeRaids) {
            if (!r.isFinished()
                    && ((r.attackers == a && r.defenders == b)
                    || (r.attackers == b && r.defenders == a))) {
                return r;
            }
        }
        return null;
    }

    public void startRaid(MinecraftServer server, TeamData.Team attackers, TeamData.Team defenders, Raid.RaidType type, EntityPlayerMP starter) {

        int prepTicks = configHandler.raidPrepMinutes * 60 * 20;
        Raid raid = new Raid(attackers, defenders, type, starter.getUniqueID(), prepTicks);
        activeRaids.add(raid);

        broadcastToRaid(server, raid,
                new TextComponentString("§6Raid started between §e" +
                        attackers.name + " §6and §e" + defenders.name +
                        "§6! You have " + configHandler.raidPrepMinutes +
                        " minutes to prepare and place a team flag.")
        );
    }

    public void endRaid(MinecraftServer server,
                        Raid raid,
                        @Nullable TeamData.Team winner,
                        String reason) {
        raid.forceEnd(server, winner, reason);
    }

    public void broadcastToRaid(MinecraftServer server, Raid raid, ITextComponent msg) {
        sendToTeam(server, raid.attackers, msg);
        sendToTeam(server, raid.defenders, msg);
    }

    private void sendToTeam(MinecraftServer server, TeamData.Team team, ITextComponent msg) {
        for (EntityPlayerMP p : server.getPlayerList().getPlayers()) {
            TeamData data = TeamData.get(p.world);
            TeamData.Team t = data.getTeamOf(p.getUniqueID());
            if (t == team) {
                p.sendMessage(msg);
            }
        }
    }

    public void applyWinLogic(MinecraftServer server,
                              Raid raid,
                              @Nullable TeamData.Team winner,
                              String reason) {
        // Announce result
        ITextComponent msg;
        if (winner == null) {
            msg = new TextComponentString("§eRaid between §6" + raid.attackers.name +
                    " §eand §6" + raid.defenders.name + " §eended in a draw. " + reason);
        } else {
            msg = new TextComponentString("§aTeam §2" + winner.name +
                    " §awon the raid! " + reason);
        }
        broadcastToRaid(server, raid, msg);

        // Placeholder reward handling
        if (winner != null) {
            RaidRewards.giveRewards(server, raid, winner);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;

        Iterator<Raid> it = INSTANCE.activeRaids.iterator();
        while (it.hasNext()) {
            Raid raid = it.next();
            if (raid.isFinished()) {
                it.remove();
            } else {
                raid.tick(server);
            }
        }
    }
}
