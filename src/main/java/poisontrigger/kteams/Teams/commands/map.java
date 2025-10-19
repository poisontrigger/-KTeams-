package poisontrigger.kteams.Teams.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import poisontrigger.kteams.Teams.TeamData;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static net.minecraft.command.CommandBase.parseInt;

public class map {

    private static final char[] TEAM_SYMBOLS = ("▣◆●▲▼◼◻").toCharArray();

    public static void map(ICommandSender sender, String[] args)throws CommandException {
        {
            int radius = 4;
            if (args.length >= 2) try {
                radius = Math.max(2, Math.min(10, parseInt(args[1])));
            } catch (NumberInvalidException ignored) {
            }
            if (!(sender instanceof EntityPlayerMP))
                throw new CommandException("commands.generic.usage");
            EntityPlayerMP p = (EntityPlayerMP) sender;

            BlockPos bp = p.getPosition();
            int cX = bp.getX() >> 4, cZ = bp.getZ() >> 4;

            TeamData data = TeamData.get(p.world);
            String myTeam = data.getTeamIdOf(p.getUniqueID());

            // assign symbols for teams in the view
            Map<String, Character> symMap = assignSymbolsForArea(data, cX, cZ, radius, TEAM_SYMBOLS);

            // build cell codes: 0 = wilderness, else index into 'legendCodes' (1..N)
            int dim = 2 * radius + 1;
            byte[] cells = new byte[dim * dim];
            // legend arrays (parallel)
            java.util.List<Character> legendSyms = new java.util.ArrayList<>();
            java.util.List<String> legendNames = new java.util.ArrayList<>();
            java.util.List<Integer> legendColors = new java.util.ArrayList<>();
            java.util.List<String> legendTeamIds = new java.util.ArrayList<>();

            // stable order for legend: sort by display name
            for (Map.Entry<String, Character> e : symMap.entrySet()) {
                String teamId = e.getKey();
                TeamData.Team t = data.getAllTeamsView().get(teamId);
                String name = (t != null && t.name != null && !t.name.isEmpty()) ? t.name : teamId;
                int color = teamId.equals(myTeam) ? 0xFF00C853 /* green */ : 0xFFE53935 /* red */;
                legendSyms.add(e.getValue());
                legendNames.add(name);
                legendColors.add(color);
                legendTeamIds.add(teamId);
            }

            // map teamId -> code (1..legend size)
            java.util.Map<String, Byte> codeByTeam = new java.util.HashMap<>();
            for (int i = 0; i < legendTeamIds.size(); i++) codeByTeam.put(legendTeamIds.get(i), (byte) (i + 1));

            // fill grid
            int idx = 0;
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    String owner = data.getChunkOwner(new ChunkPos(cX + dx, cZ + dz));
                    cells[idx++] = (owner == null) ? 0 : codeByTeam.getOrDefault(owner, (byte) 0);
                }
            }

            // send
            poisontrigger.kteams.network.Net.CH.sendTo(
                    new poisontrigger.kteams.network.Net.MapPacket(radius, 8, cells, legendSyms, legendNames, legendColors), p);

            sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §rMap shown (r=" + radius + ")."));
            return;
        }

    }
    private static Map<String, Character> assignSymbolsForArea(
            TeamData data, int cx, int cz, int radius, char[] POOL
    ) {
        // collect unique team IDs in view
        LinkedHashSet<String> teams = new LinkedHashSet<>();
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                String owner = data.getChunkOwner(new ChunkPos(cx + dx, cz + dz));
                if (owner != null && !owner.isEmpty()) teams.add(owner);
            }
        }
        // deterministic assignment using hash first then fill
        Map<String, Character> map = new LinkedHashMap<>();
        boolean[] used = new boolean[POOL.length];
        for (String id : teams) {
            int i = Math.abs(id.hashCode()) % POOL.length;
            if (!used[i]) { used[i] = true; map.put(id, POOL[i]); }
        }
        for (String id : teams) {
            if (map.containsKey(id)) continue;
            for (int i = 0; i < POOL.length; i++) {
                if (!used[i]) { used[i] = true; map.put(id, POOL[i]); break; }
            }
            map.putIfAbsent(id, '?'); // ran out (unlikely)
        }
        return map;
    }
}
