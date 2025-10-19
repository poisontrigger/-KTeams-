package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import poisontrigger.kteams.Teams.TeamData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class list {
    public static void list(ICommandSender sender, String[] args, MinecraftServer server) throws CommandException {
        TeamData data = TeamData.get(sender.getEntityWorld());
        Map<String, TeamData.Team> teams = data.getAllTeamsView();
        if (teams.isEmpty()) {
            sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §o(no teams yet)"));
            return;
        }
        sender.sendMessage(new TextComponentString("[-=-=-=-=-=-=-=-=-=-=-=--=-]"));
        sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §fTeams:"));
        sender.sendMessage(new TextComponentString(""));
        List<String> names = new ArrayList<>(teams.keySet());
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);

        for (String name : names) {
            TextComponentString line = new TextComponentString(" §8- §b" + name);
            line.getStyle().setClickEvent(new net.minecraft.util.text.event.ClickEvent(
                    net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND, "/t info " + name));
            line.getStyle().setHoverEvent(new net.minecraft.util.text.event.HoverEvent(
                    net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT,
                    new TextComponentString("§7[§rkTeams§7] §eto view team info for: " + name)));
            sender.sendMessage(line);
            sender.sendMessage(new TextComponentString(""));

        }
        sender.sendMessage(new TextComponentString("[-=-=-=-=-=-=-=-=-=-=-=--=-]"));
    }
}
