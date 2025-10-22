package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import poisontrigger.kteams.Teams.ChatTeamPrefix;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.util.UuidResolver;

import java.util.UUID;

import static net.minecraft.command.CommandBase.getCommandSenderAsPlayer;

public class kick {
    public static void kick(ICommandSender sender, String[] args, MinecraftServer server) throws CommandException {
        if (!(sender instanceof EntityPlayerMP))
            throw new CommandException("commands.generic.usage");
        EntityPlayerMP p = (EntityPlayerMP) sender;
        TeamData data = TeamData.get(p.world);

        if (args.length != 2) return;
        String targetName = args[1];
        UUID target = UuidResolver.resolve(server,targetName);
        if (target == null) throw new WrongUsageException("/t kick <Player>");
        TeamData.Team team = data.getTeamOf(p.getUniqueID());
        if (team == null) throw new CommandException("kteams.command.generic.noteam");
        TeamData.Team team2 = data.getTeamOf(target);
        if (team2 == null && team != team2) throw new CommandException("kteams.command.kick.notFriendly");
        // Only the owner can kick elders
        if (team.elders.contains(target) && (team.owner != p.getUniqueID())) throw new CommandException("kteams.command.kick.elder");
        // The owner cannot be kicked
        if (team.owner == target) throw new CommandException("kteams.command.kick.owner");
        boolean isOwner = team != null && p.getUniqueID().equals(team.owner);
        boolean isElder = team != null && team.elders.contains(p.getUniqueID());
        if (!isOwner && !isElder) throw new CommandException("kteams.command.generic.elderonly");
        if(!(server.getPlayerList().getPlayerByUsername(targetName) == null)) server.getPlayerList().getPlayerByUsername(targetName).sendMessage(new TextComponentString("§7[kTeams]§c " + "you have been kicked from §e"+team.name+" §cby: §e" +getCommandSenderAsPlayer(sender).getName() + "§c."));
        data.removeMember(team.id,target);
        ChatTeamPrefix.clear(targetName);
        data.broadcastToTeam(team.id,new TextComponentString("§7[§rkTeams§7]§e " + UuidResolver.nameForUuid(server,target,false,"[UNKNOWN]") + "§c has been kicked from the team by: §e" +getCommandSenderAsPlayer(sender).getName() + "§c."));
            // Send message to all online members

    }
}
