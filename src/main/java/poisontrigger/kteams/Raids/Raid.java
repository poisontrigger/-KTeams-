package poisontrigger.kteams.Raids;

import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.util.configHandler;

import javax.annotation.Nullable;
import java.util.UUID;

public class Raid {
    public final TeamData.Team attackers;
    public final TeamData.Team defenders;
    public final RaidType type;

    private RaidStage stage = RaidStage.PREPARE;
    private int ticksRemaining;
    private boolean finished = false;

    // For messages / logging
    private final UUID startedBy; // player who ran the command

    public Raid(TeamData.Team attackers, TeamData.Team defenders, RaidType type,
                UUID startedBy, int prepTicks) {
        this.attackers = attackers;
        this.defenders = defenders;
        this.type = type;
        this.startedBy = startedBy;
        this.stage = RaidStage.PREPARE;
        this.ticksRemaining = prepTicks;
    }

    public RaidStage getStage() {
        return stage;
    }

    public boolean isFinished() {
        return finished;
    }

    public void tick(MinecraftServer server) {
        if (finished) return;

        if (--ticksRemaining > 0) {
            return;
        }

        switch (stage) {
            case PREPARE:
                onPrepFinished(server);
                break;
            case ACTIVE:
                onActiveFinished(server);
                break;
            default:
                break;
        }
    }

    private void onPrepFinished(MinecraftServer server) {
        boolean atkHasFlag = RaidUtil.teamHasRaidFlag(server, attackers);
        boolean defHasFlag = RaidUtil.teamHasRaidFlag(server, defenders);

        if (!atkHasFlag && !defHasFlag) {
            RaidManager.INSTANCE.endRaid(server, this, null, "Raid ended in a draw – neither team had a flag.");
            return;
        }
        if (atkHasFlag && !defHasFlag) {
            RaidManager.INSTANCE.endRaid(server, this, attackers, "Attackers win by default – defenders had no flag.");
            return;
        }
        if (!atkHasFlag && defHasFlag) {
            RaidManager.INSTANCE.endRaid(server, this, defenders, "Defenders win by default – attackers had no flag.");
            return;
        }

        // Both have flags → start ACTIVE stage
        stage = RaidStage.ACTIVE;
        ticksRemaining = configHandler.raidActiveMinutes * 60 * 20; // configurable
        RaidManager.INSTANCE.broadcastToRaid(
                server, this,
                new TextComponentString("§cRaid has begun! Capture the enemy flag! ("
                        + configHandler.raidActiveMinutes + " minutes)")
        );
    }

    private void onActiveFinished(MinecraftServer server) {
        // Time ran out with no capture → defenders win.
        RaidManager.INSTANCE.endRaid(server, this, defenders, "Raid time ended – defenders held their ground.");
    }

    public void forceEnd(MinecraftServer server, @Nullable TeamData.Team winner, String reason) {
        if (finished) return;
        finished = true;
        stage = RaidStage.ENDED;
        RaidManager.INSTANCE.applyWinLogic(server, this, winner, reason);
    }
    public enum RaidStage {
        PREPARE,
        ACTIVE,
        ENDED
    }
    public enum RaidType {
        TYPE1(1, false), // no explosions
        TYPE2(2, true),
        TYPE3(3, true),
        TYPE4(4, true),
        TYPE5(5, true);

        public final int id;
        public final boolean explosionsEnabled;

        RaidType(int id, boolean explosionsEnabled) {
            this.id = id;
            this.explosionsEnabled = explosionsEnabled;
        }

        public static RaidType fromInt(int i) throws CommandException {
            for (RaidType t : values()) {
                if (t.id == i) return t;
            }
            throw new CommandException("Invalid raid type: " + i + " (must be 1-5)");
        }
    }
}

