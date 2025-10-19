package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import poisontrigger.kteams.Teams.TeamCommands;
import poisontrigger.kteams.Teams.TeamData;

public class description {
    public static void description(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP))
            throw new CommandException("commands.generic.usage");

        EntityPlayerMP p = (EntityPlayerMP) sender;
        TeamData data = TeamData.get(p.world);

        // Resolve team
        String teamId = data.getTeamIdOf(p.getUniqueID());
        if (teamId == null) throw new CommandException("kteams.command.generic.noteam");
        TeamData.Team team = data.getAllTeamsView().get(teamId);
        if (team == null) throw new CommandException("kteams.command.generic.noteam");

        // If no args after "description" -> show current description
        if (args.length == 1) {
            String desc = (team.description == null || team.description.isEmpty())
                    ? "§7( no description set )"
                    : team.description;
            sender.sendMessage(new TextComponentString(
                    "§7[§rkTeams§7]] §fDescription for §b" + team.name + "§f:\n§r" + desc));
            return;
        }

        // Setting description requires owner/elder
        boolean isOwner = p.getUniqueID().equals(team.owner);
        boolean isElder = team.elders.contains(p.getUniqueID());
        if (!isOwner && !isElder) throw new CommandException("kteams.command.generic.elderonly");

        // Join the rest of the args as the new description
        String text = TeamCommands.joinNice(args, 1);

        // (Optional) limit length
        int MAX_LEN = 256;
        if (text.length() > MAX_LEN) text = text.substring(0, MAX_LEN);

        team.description = text;
        data.markDirty();

        sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §fTeam description updated."));
    }
}
