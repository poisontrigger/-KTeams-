package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import poisontrigger.kteams.Teams.TeamData;

import static net.minecraft.command.CommandBase.parseInt;

public class claim {
    public static void claim(ICommandSender sender, String[] args, MinecraftServer server) throws CommandException{

        if (!(sender instanceof EntityPlayerMP))
            throw new CommandException("commands.generic.usage");
        EntityPlayerMP p = (EntityPlayerMP) sender;

        TeamData data = TeamData.get(p.world);
        String teamId = data.getTeamIdOf(p.getUniqueID());
        if (teamId == null) throw new CommandException("kteams.command.generic.noteam");

        TeamData.Team team = data.getAllTeamsView().get(teamId);
        boolean isOwner = team != null && p.getUniqueID().equals(team.owner);
        boolean isElder = team != null && team.elders.contains(p.getUniqueID());
        if (!isOwner && !isElder) throw new CommandException("kteams.command.generic.elderonly");

        // --- radius parsing ---
        final int MAX_RADIUS = 8; // safety cap
        int radius = 0;
        if (args.length >= 2) {
            try {
                radius = parseInt(args[1], 0, MAX_RADIUS); // CommandBase.parseInt
            } catch (NumberInvalidException e) {
                throw new WrongUsageException("/team claim [radius 0-" + MAX_RADIUS + "]");
            }
        }

        ChunkPos base = new ChunkPos(p.getPosition());

        int claimed = 0, yours = 0, blocked = 0, stale = 0, limited = 0;

        // Square area (2r+1)^2 centered on current chunk
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos pos = new ChunkPos(base.x + dx, base.z + dz);
                TeamData.ClaimResult res = data.claimChunkChecked(teamId, pos, false, 0);
                switch (res) {
                    case SUCCESS:                 claimed++; break;
                    case ALREADY_OWNED_BY_YOU:    yours++;   break;
                    case ALREADY_OWNED_BY_OTHER:  blocked++; break;
                    case STALE_TEAM:              stale++;   break;
                    case LIMIT_REACHED:           limited++; break;
                }
            }
        }

        if (claimed > 0) data.markDirty();

        // Build summary message
        int total = (2 * radius + 1) * (2 * radius + 1);
        StringBuilder sb = new StringBuilder();
        sb.append("§7[§rkTeams§7] §fClaim summary for §e")
                .append(radius == 0 ? "this chunk" : (total + "§f chunks (r=" + radius + ")"))
                .append("§f:\n");
        sb.append(" §8• §aClaimed: §f").append(claimed).append("\n");
        sb.append(" §8• §7Already yours: §f").append(yours).append("\n");
        sb.append(" §8• §cBlocked (others): §f").append(blocked).append("\n");
        if (limited > 0) {
            sb.append(" §8• §6Blocked by limit (max ")
                    .append(data.getMaxClaimsPerTeam()).append("): §f").append(limited).append("\n");
            sb.append(" §8• §fNow at: §e")
                    .append(data.getClaimCount(teamId)).append("§f / §e")
                    .append(data.getMaxClaimsPerTeam());
        }
        if (stale > 0) sb.append("\n §8• §eStale team entries: §f").append(stale);

        sender.sendMessage(new TextComponentString(sb.toString()));
    }
    public static void unclaim(ICommandSender sender,String[] args, MinecraftServer server) throws CommandException {
        {
            if (!(sender instanceof EntityPlayerMP))
                throw new CommandException("commands.generic.usage");
            EntityPlayerMP p = (EntityPlayerMP) sender;

            ChunkPos cp = new ChunkPos(p.getPosition());
            TeamData data = TeamData.get(p.world);

            String teamId = data.getTeamIdOf(p.getUniqueID());
            if (teamId == null)
                throw new CommandException("kteams.command.generic.noteam");

            // resolve team & role
            TeamData.Team team = data.getAllTeamsView().get(teamId);
            boolean isOwner = team != null && p.getUniqueID().equals(team.owner);
            boolean isElder = team != null && team.elders.contains(p.getUniqueID());

            if (!isOwner && !isElder)
                throw new CommandException("kteams.command.generic.elderonly");

            // must own this chunk
            String ownerTeam = data.getChunkOwner(cp);
            if (!teamId.equals(ownerTeam)) {
                ownerTeam = "wilderness";
                sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §cYour team does not own this chunk, it is owned by: §e"+ ownerTeam+"§c."));
                return;
            }
            boolean ok = data.removeClaim(teamId, cp);
            if (ok) data.markDirty();

            sender.sendMessage(new TextComponentString(
                    ok ? "§7[§rkTeams§7] §aUnclaimed this chunk."
                            : "§7[§rkTeams§7] §cThis chunk wasn’t claimed."
            ));
        }
    }
}
