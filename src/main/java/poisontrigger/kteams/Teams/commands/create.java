package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import poisontrigger.kteams.Teams.TeamAPI;

import java.util.Locale;
import java.util.UUID;

public class create {
    public static void create(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) throw new CommandException("kteams.command.player_only");
        if (args.length < 2) throw new WrongUsageException("/team create <name>");
        {
            String id = args[1].toLowerCase(Locale.ROOT);
            String display = args[1];
            UUID uuid = ((EntityPlayerMP) sender).getUniqueID();
            boolean ok = TeamAPI.createTeam(sender.getEntityWorld(), id, display, uuid);
            sender.sendMessage(new TextComponentString(ok ? ("Created team '" + id + "'") : "team already exists."));
        }
    }
}
