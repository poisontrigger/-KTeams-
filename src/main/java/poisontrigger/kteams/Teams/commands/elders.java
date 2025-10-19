package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.util.UuidResolver;

import java.util.UUID;

public class elders {
    public static void promote(ICommandSender sender, String[] args, MinecraftServer server) throws CommandException {
        if (!(sender instanceof EntityPlayerMP))
            throw new CommandException("commands.generic.usage");
        if (args.length != 2) throw new WrongUsageException("/t promote <Player>");

        EntityPlayerMP p = (EntityPlayerMP) sender;
        TeamData data = TeamData.get(p.world);

        String targetName = args[1];
        UUID target = UuidResolver.resolve(server, targetName); // your resolver
        if (target == null) throw new WrongUsageException("/t promote <Player>");

        TeamData.Team team = data.getTeamOf(p.getUniqueID());
        if (team == null) throw new CommandException("kteams.command.generic.noteam");

        TeamData.Team teamOfTarget = data.getTeamOf(target);
        // must be same team
        if (teamOfTarget == null || teamOfTarget != team)
            throw new CommandException("kteams.command.kick.notFriendly");

        // only owner can promote to elder
        boolean isOwner = p.getUniqueID().equals(team.owner);
        if (!isOwner) throw new CommandException("kteams.command.generic.owneronly");

        // can't promote owner / already elder?
        if (target.equals(team.owner))
            throw new CommandException("kteams.command.promote.owner");
        if (team.elders.contains(target))
            throw new CommandException("kteams.command.promote.already_elder");

        boolean ok = data.addElder(team.id, target);
        String tName = UuidResolver.nameForUuid(server, target, false, "[UNKNOWN]");
        if (ok) {
            // DM target if online
            EntityPlayerMP tMp = server.getPlayerList().getPlayerByUUID(target);
            if (tMp != null) {
                tMp.sendMessage(new TextComponentString(
                        "§7[§rkTeams§7] §aYou were promoted to §dElder§a by §e" + p.getName() + "§a."));
            }
            // Broadcast to team
            data.broadcastToTeamExcept(team.id,target, new TextComponentString(
                    "§7[§rkTeams§7] §e" + tName + " §ahas been promoted to §dElder §aby §e" + p.getName() + "§a."));
        } else {
            throw new CommandException("kteams.command.player_only");
        }
    }
    public static void demote(ICommandSender sender, String[] args, MinecraftServer server) throws CommandException {
        if (!(sender instanceof EntityPlayerMP))
            throw new CommandException("commands.generic.usage");
        if (args.length != 2) throw new WrongUsageException("/t demote <Player>");

        EntityPlayerMP p = (EntityPlayerMP) sender;
        TeamData data = TeamData.get(p.world);

        String targetName = args[1];
        UUID target = UuidResolver.resolve(server, targetName);
        if (target == null) throw new WrongUsageException("/t demote <Player>");

        TeamData.Team team = data.getTeamOf(p.getUniqueID());
        if (team == null) throw new CommandException("kteams.command.generic.noteam");

        TeamData.Team teamOfTarget = data.getTeamOf(target);
        if (teamOfTarget == null || teamOfTarget != team)
            throw new CommandException("kteams.command.kick.notFriendly");

        // only owner can demote elders
        boolean isOwner = p.getUniqueID().equals(team.owner);
        if (!isOwner) throw new CommandException("kteams.command.generic.owneronly");

        // can't demote owner / and target must currently be elder
        if (target.equals(team.owner))
            throw new CommandException("kteams.command.demote.owner");
        if (!team.elders.contains(target))
            throw new CommandException("kteams.command.demote.not_elder");

        boolean ok = data.removeElder(team.id, target, /*notify=*/true);
        String tName = UuidResolver.nameForUuid(server, target, false, "[UNKNOWN]");
        if (ok) {
            EntityPlayerMP tMp = server.getPlayerList().getPlayerByUUID(target);
            if (tMp != null) {
                tMp.sendMessage(new TextComponentString(
                        "§7[§rkTeams§7] §cYou were demoted from §eElder §cby §b" + p.getName() + "§c."));
            }
            data.broadcastToTeamExcept(team.id,target, new TextComponentString(
                    "§7[§rkTeams§7] §e" + tName + " §chas been demoted from §eElder §cby §b" + p.getName() + "§c."));
        } else {
            throw new CommandException("kteams.command.player_only");
        }
    }
}
