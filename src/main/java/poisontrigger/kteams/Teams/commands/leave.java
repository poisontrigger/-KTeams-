package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import poisontrigger.kteams.Teams.ChatTeamPrefix;
import poisontrigger.kteams.Teams.TeamData;

import java.util.UUID;

public class leave {
    public static void leave(ICommandSender sender, String[] args, MinecraftServer server) throws CommandException {
        EntityPlayerMP p = (EntityPlayerMP) sender;
        if (p.world.isRemote) return; // server-side only

        TeamData data = TeamData.get(p.world);
        TeamData.Team team = data.getTeamOf(p.getUniqueID());
        if (team == null) {
            throw new CommandException("kteams.command.generic.noteam"); // "You are not in a team."
        }

        final UUID playerId = p.getUniqueID();
        final boolean isOwner = team.owner != null && team.owner.equals(playerId);

        // Count all members - owner: -owner + members
        int memberCount = -1 /*owner*/ + team.members.size();

        if (isOwner && memberCount > 1) {
            // Owner cannot leave if others still in team
            throw new CommandException("kteams.command.leave.not_last");
            // e.g. lang: kteams.command.leave.owner_not_last=You must transfer ownership or remove everyone before leaving.
        }

        if (isOwner && memberCount < 1) {
            // Owner is the last player → disband team
            data.removeMember(team.id, p.getUniqueID());
            data.deleteTeam(team.id);                 // implement or call your existing delete/fdelete logic
            data.markDirty();
            data.broadcastToPlayers(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers(),new TextComponentString("§7[§rKTeams§7] §fTeam §e" + team.name + " §fhas been disbanded."));
            // Clear the client's cached team UI/state if you sync it:
            poisontrigger.kteams.network.Net.sendClientTeam(p, null);
            return;
        }

        // Non-owner (or theoretically owner in a multi-owner model) just leaves:
        // Remove from elders or members set and global maps
        team.elders.remove(playerId);
        team.members.remove(playerId);
        data.getTeamOf(playerId);
        ChatTeamPrefix.clear(p.getName());

        data.markDirty();

        p.sendMessage(new TextComponentString("§7[§rkTeams§7] §fYou left §e" + team.name + "§f."));
        // Notify remaining teammates (optional)
        data.broadcastToTeamExcept(team.id,p.getUniqueID(),
                new TextComponentString("§7[§rkTeams§7] §e" + p.getDisplayNameString() + " §fhas left the team."));

        // Update this player's client team cache
        poisontrigger.kteams.network.Net.sendClientTeam(p, null);
    }
}
