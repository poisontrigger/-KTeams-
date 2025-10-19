package poisontrigger.kteams.Teams;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.UUID;


// legacy code, TODO: only use TeamData
public class TeamAPI {

    private TeamAPI() {}

    public static boolean createTeam(World world, String teamId, String displayName, UUID ownerId) {
        TeamData data = TeamData.get(world);
        boolean ok = data.createTeam(teamId, displayName, ownerId);
        if (ok) data.markDirty();
        return ok;
    }

    public static boolean addMember(World world, String teamId, UUID playerId) {
        TeamData data = TeamData.get(world);
        boolean ok = data.addMember(teamId, playerId);
        if (ok) data.markDirty();
        return ok;
    }

    public static boolean claimAtPlayerChunk(EntityPlayerMP player, String teamId) {
        World w = player.world;
        ChunkPos cp = new ChunkPos(player.getPosition());
        TeamData data = TeamData.get(w);
        boolean ok = data.claimChunk(teamId, cp);
        if (ok) data.markDirty();
        return ok;
    }

    public static String ownerAt(World world, ChunkPos pos) {
        return TeamData.get(world).getChunkOwner(pos);
    }
}


