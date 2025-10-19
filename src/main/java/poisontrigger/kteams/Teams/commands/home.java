package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import poisontrigger.kteams.Teams.TeamData;

public class home {
    public static void home(ICommandSender sender) throws CommandException {
        if (!(sender instanceof EntityPlayerMP))
            throw new CommandException("commands.generic.usage");

        EntityPlayerMP p = (EntityPlayerMP) sender;
        TeamData data = TeamData.get(p.world);

        String teamId = data.getTeamIdOf(p.getUniqueID());
        if (teamId == null) throw new CommandException("kteams.command.generic.noteam");

        TeamData.Team team = data.getAllTeamsView().get(teamId);
        if (team == null) throw new CommandException("kteams.command.generic.noteam");
        if (team.home == null) {
            sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §cNo team home set."));
            return;
        }

        int warmup = Math.max(0, poisontrigger.kteams.util.configHandler.homeWarmupSeconds);

        // queue a pending teleport; cancels on damage or movement ±2 blocks
        poisontrigger.kteams.Teams.HomeTeleportManager.queue(p, team.homeDimension, team.home, warmup);
    }
    public static void sethome(ICommandSender sender) throws CommandException{
        if (!(sender instanceof EntityPlayerMP))
            throw new CommandException("commands.generic.usage");

        EntityPlayerMP p = (EntityPlayerMP) sender;
        TeamData data = TeamData.get(p.world);

        String teamId = data.getTeamIdOf(p.getUniqueID());
        if (teamId == null) throw new CommandException("kteams.command.generic.noteam");

        TeamData.Team team = data.getAllTeamsView().get(teamId);
        if (team == null) throw new CommandException("kteams.command.generic.noteam");

        boolean isOwner = p.getUniqueID().equals(team.owner);
        boolean isElder = team.elders.contains(p.getUniqueID());
        if (!isOwner && !isElder) throw new CommandException("kteams.command.generic.elderonly");

        BlockPos here = p.getPosition();
        team.setHome(p.dimension, here); // uses your Team#setHome(int, BlockPos)
        data.markDirty();

        sender.sendMessage(new TextComponentString(
                "§7[§rkTeams§7] §fTeam home set to §b" +
                        here.getX() + "§7, §b" + here.getY() + "§7, §b" + here.getZ() +
                        " §f(dim §e" + p.dimension + "§f)."
        ));
    }
}
