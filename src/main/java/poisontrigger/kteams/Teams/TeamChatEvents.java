package poisontrigger.kteams.Teams;

import io.netty.util.ResourceLeakDetector;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Mod.EventBusSubscriber
public final class TeamChatEvents{
        // Per-player toggle state (in-memory only)
        private static final Set<UUID> ON = ConcurrentHashMap.newKeySet();

        // either toggle or send chat message (/t chat) or (/t chat (message) )
        public static void handleCommand(ICommandSender sender, String[] args) throws CommandException {
            if (!(sender instanceof EntityPlayerMP)) throw new CommandException("commands.generic.usage");
            EntityPlayerMP p = (EntityPlayerMP) sender;
            if (p.world.isRemote) return;

            TeamData data = TeamData.get(p.world);
            TeamData.Team team = data.getTeamOf(p.getUniqueID());
            if (team == null) throw new CommandException("kteams.command.generic.noteam");

            //  /t chat hello team
            if (args.length >= 2) {
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                sendToTeam(p, team, msg);
                return;
            }

            // Toggle: /t chat
            boolean now;
            if (ON.contains(p.getUniqueID())) { ON.remove(p.getUniqueID()); now = false; }
            else { ON.add(p.getUniqueID()); now = true; }
            p.sendMessage(new TextComponentString("§7[§aKTeams§7] §fTeam chat: " + (now ? "§aON" : "§cOFF")));
        }

        // clear cache when relog
        @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
            ON.remove(e.player.getUniqueID());
        }

        // Intercept normal chat when toggle is on
        @SubscribeEvent public static void onChat(ServerChatEvent e) {
            EntityPlayerMP p = e.getPlayer();
            if (!ON.contains(p.getUniqueID())) return;

            TeamData data = TeamData.get(p.world);
            TeamData.Team team = data.getTeamOf(p.getUniqueID());
            if (team == null) { ON.remove(p.getUniqueID()); return; } // no team → fall back to global next time

            e.setCanceled(true);
            sendToTeam(p, team, e.getMessage());
        }

        // Helper
        private static void sendToTeam(EntityPlayerMP sender, TeamData.Team team, String msg) {
            TeamData data = TeamData.get(sender.world);
            String name = team.name == null ? "Team" : team.name;
            String line = "§7[§bT§7] §e" + name + " §7| §a" + sender.getDisplayNameString() + "§7: §f" + msg;
            data.broadcastToTeam(team.id,new TextComponentString(line));
        }
}

