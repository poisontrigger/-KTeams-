package poisontrigger.kteams.Teams.commands;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.UsernameCache;
import poisontrigger.kteams.Teams.TeamData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.command.CommandBase.getCommandSenderAsPlayer;

public class info {
    public static void info(ICommandSender sender, String[] args, MinecraftServer server) throws CommandException{
        String teamId = null;
        TeamData data = TeamData.get(sender.getEntityWorld());
        TeamData.Team t = data.getTeamOf(getCommandSenderAsPlayer(sender).getUniqueID());
        int onlineCount = 0;
        if (args.length == 1) {

            if (t == null) throw new CommandException("kteams.command.generic.noteam");
            teamId = t.id;}
        if (args.length == 2) {teamId = args[1];}
        TeamData.Team team = data.getAllTeamsView().get(teamId);
        if (team == null) {
            sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §cNo such team: §e" + teamId));
            return;
        }

        Set<UUID> members = data.getMembers(teamId);
        if (members.isEmpty()) {
            sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §fMembers of §b" + teamId + "§f:\n§8( none )"));
            return;
        }
        sender.sendMessage(new TextComponentString("\n[-=-=-=-=-=-=["+team.id+"]=-=-=--=-]"));
        if (team.description != "") {sender.sendMessage(new TextComponentString("Description: "+ team.description+ "\n"));} else {sender.sendMessage(new TextComponentString("Description: This team has gotten around to setting a description yet\n"));}
        List<String> lines = new ArrayList<>();
        for (UUID u : members) {
            String name = nameFor(server, u);
            boolean online = server.getPlayerList().getPlayerByUUID(u) != null;
            String badge = u.equals(team.owner) ? "§6[Owner] "
                    : team.elders.contains(u) ? "§d[Elder] "
                    : "§7[Member] ";
            lines.add(badge + (online ? "§a" : "§7") + name + "§r");
            if(online)onlineCount++;
        }
        lines.sort(String.CASE_INSENSITIVE_ORDER);
        sender.sendMessage(new TextComponentString("Online: " + onlineCount + "/" + team.members.size() ));
        sender.sendMessage(new TextComponentString("\n§fMembers:\n"+ "§f\n" + String.join("\n \n", lines)));

        sender.sendMessage(new TextComponentString(""));
        sender.sendMessage(new TextComponentString("[-=-=-=-=-=-=["+team.id+"]=-=-=--=-]"));
    }
    private static String nameFor(MinecraftServer server, UUID uuid) {
        // 1) Online right now?
        EntityPlayerMP online = server.getPlayerList().getPlayerByUUID(uuid);
        if (online != null) return online.getName();

        // 2) Forge's last-known username cache (fast, persists to world/usernamecache.json)
        String cached = UsernameCache.getLastKnownUsername(uuid);
        if (cached != null && !cached.isEmpty()) return cached;

        // 3) Vanilla profile cache (reads from usercache.json)
        GameProfile gp = server.getPlayerProfileCache().getProfileByUUID(uuid);
        if (gp != null && gp.getName() != null && !gp.getName().isEmpty()) return gp.getName();

        // 4) Fallback: short UUID
        return uuid.toString().substring(0, 8);
    }
}
