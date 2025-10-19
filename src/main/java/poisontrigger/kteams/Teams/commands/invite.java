package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.util.Perms;
import poisontrigger.kteams.util.permUtil;

import static net.minecraft.command.CommandBase.getCommandSenderAsPlayer;
import static net.minecraft.command.CommandBase.getPlayer;

public class invite {
    public static void invite(ICommandSender sender, String[] args, MinecraftServer server) throws CommandException {

        EntityPlayerMP p = getCommandSenderAsPlayer(sender);
        if (args.length != 2) throw new CommandException("kteams.command.invite.args");

        EntityPlayerMP target = getPlayer(server, sender, args[1]);

        if (target.getUniqueID() == getCommandSenderAsPlayer(sender).getUniqueID()) throw new CommandException("kteams.command.invite.self");
        if (TeamData.get(p.world).getTeamOf(p.getUniqueID()) == null) throw new CommandException("kteams.command.generic.noteam");

        TeamData data = TeamData.get(p.world);
        String teamId = data.getTeamIdOf(p.getUniqueID());

        if (!data.canInvite(teamId, p.getUniqueID())) throw new CommandException("kteams.command.generic.elderonly");

        boolean ok = data.invitePlayer(teamId, p.getUniqueID(), target.getUniqueID(), 10000);
        if(!ok) throw new CommandException("kteams.command.invite.failed");
    }
}
