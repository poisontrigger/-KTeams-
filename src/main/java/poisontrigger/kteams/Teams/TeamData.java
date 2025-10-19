package poisontrigger.kteams.Teams;


import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import poisontrigger.kteams.util.configHandler;

import javax.annotation.Nullable;
import java.util.*;

public class TeamData extends WorldSavedData {

public static final String DATA_Name = "kTeams.teams";

private final Map<UUID, String> memberToTeam = new HashMap<>();

private final Map<String, Team> teams = new HashMap<>();

private final Map<String, Integer> claimCounts = new HashMap<>();
private final Map<Long, String> chunkOwners = new HashMap<>();

private final Map<UUID, Invite> pendingInvites = new HashMap<>();

    public TeamData() {super(DATA_Name); }
    public TeamData(String name){ super(name);}

    // API:

    public static TeamData get(World world){
        if (world.isRemote) throw new IllegalStateException("Use Server World");
        MapStorage storage = world.getPerWorldStorage();
        TeamData data = (TeamData) storage.getOrLoadData(TeamData.class, DATA_Name);
        if (data == null) {
            data = new TeamData();
            storage.setData(DATA_Name, data);
        }
        return data;

    }

    // Team Management

    public boolean createTeam(String teamId, String displayName, UUID ownerId){
        if (teams.containsKey(teamId)) return false;
        Team t = new Team(teamId, displayName);
        t.owner = ownerId;
        t.members.add(ownerId);
        teams.put(teamId, t);
        memberToTeam.put(ownerId, teamId);
        markDirty();

        EntityPlayerMP mp = FMLCommonHandler.instance().getMinecraftServerInstance()
                .getPlayerList().getPlayerByUUID(ownerId);
        if (mp != null) poisontrigger.kteams.network.Net.sendClientTeam(mp, teamId);

        return true;
    }

    public boolean deleteTeam(String teamId){
        Team t = teams.remove(teamId);
        if (t == null) return false;


        chunkOwners.values().removeIf(id -> id.equals(teamId));
        for (UUID u : t.members) memberToTeam.remove(u);
        t.elders.clear();
        t.owner = null;

        markDirty();
        return true;
    }

    public Map<String, Team> getAllTeamsView() {
        return Collections.unmodifiableMap(teams);
    }

    // Member Management

    public String getTeamIdOf(UUID player) {
        return memberToTeam.get(player);
    }

    public Team getTeamOf(UUID player) {
        String id = memberToTeam.get(player);
        return id == null ? null : teams.get(id);
    }

    public boolean isMemberOf(UUID player, String teamId) {
        return teamId != null && teamId.equals(memberToTeam.get(player));
    }


    public boolean addMember(String teamId, java.util.UUID player) {
        Team t = teams.get(teamId);
        if (t == null) return false;
        String tid = t.id;

        // one team at a time

        String prevTeam = memberToTeam.get(player);
        if (prevTeam != null && !prevTeam.equals(teamId)) return false;

        boolean added = t.members.add(player);
        memberToTeam.put(player, teamId);
        if (added || (prevTeam != null && !prevTeam.equals(teamId))) {
            markDirty();

            // push the new team to the players client

            EntityPlayerMP mp = FMLCommonHandler.instance()
                    .getMinecraftServerInstance()
                    .getPlayerList()
                    .getPlayerByUUID(player);

            if (mp != null) {
                String id = (tid != null && !tid.isEmpty()) ? tid : "wilderness";
                poisontrigger.kteams.network.Net.sendClientTeam(mp, id);
            }
        return true;
        }

        return false;
    }
    public boolean removeMember(String teamId, UUID player){
        Team t = teams.get(teamId);
        if (t == null) return false;
        if (t.owner == player) return false;
        boolean removed = t.members.remove(player);

        // Clear reverse index if it pointed to this team & remove elder roles

        if (removed && teamId.equals(memberToTeam.get(player))) {
            memberToTeam.remove(player);
            removeElder(teamId, player, false);
        }
        if (removed) {
            markDirty();

        // Send Update Packet

        EntityPlayerMP mp = FMLCommonHandler.instance()
                .getMinecraftServerInstance()
                .getPlayerList()
                .getPlayerByUUID(player);
        if (mp != null) {
            poisontrigger.kteams.network.Net.sendClientTeam(mp, "wilderness");
        }
        }

        return removed;
    }
    public boolean removeElder(String teamId, UUID player, boolean notify) {
        Team t = teams.get(teamId);
        if (t == null) return false;
        if(getElders(teamId).contains(player)){
            boolean removed = t.elders.remove(player);
            if (removed){
                if (notify){markDirty();}
                return true;
            }
        }

    return false;
    }
    public boolean addElder(String teamId, UUID player) {
        Team t = teams.get(teamId);
        if (t == null) return false;
        if(!(getElders(teamId).contains(player))){
            boolean added = t.elders.add(player);
            if (added){
                markDirty();
                return true;
            }
        }
        return false;
    }
    public boolean setOwner(String teamID, UUID player){

        Team t = teams.get(teamID);
        UUID newOwner = player;

        if(t == null)return false;
        if(!t.members.contains(player))return false;

        if(t.elders.contains(newOwner)){
            t.elders.remove(newOwner);
        }

        t.owner = player;
        markDirty();

        return true;

    }
    public UUID getOwner(String teamID){
        Team t = teams.get(teamID);
        return t.owner;
    }

    public Set<UUID> getMembers(String teamId){
        Team t = teams.get(teamId);
        return t == null ? Collections.emptySet() : Collections.unmodifiableSet(t.members);
    }
    public Set<UUID> getElders(String teamId){
        Team t = teams.get(teamId);
        return t == null ? Collections.emptySet() : Collections.unmodifiableSet(t.elders);
    }

    public TeamRole roleOf(String teamId, UUID player) {
        Team t = teams.get(teamId);
        if (t == null || !t.members.contains(player)) return null;
        if (player.equals(t.owner)) return TeamRole.OWNER;
        if (t.elders.contains(player)) return TeamRole.ELDER;
        return TeamRole.MEMBER;
    }
    public boolean isOwner(String teamId, UUID u){ Team t=teams.get(teamId); return t!=null && u.equals(t.owner); }
    public boolean isElder(String teamId, UUID u){ Team t=teams.get(teamId); return t!=null && t.elders.contains(u); }

    // INVITES

    public boolean canInvite(String teamId, UUID actor) {
        Team t = teams.get(teamId);
        if (t == null) return false;
        if (!t.members.contains(actor)) return false;
        if (actor.equals(t.owner)) return true;      // owner can invite
        return t.elders.contains(actor);             // elders can invite
    }


    // INVITE LOGIC
    public boolean invitePlayer(String teamId, UUID sender, UUID target, int ttlSeconds) {
        Team t = teams.get(teamId);
        if (t == null) return false;
        if (!canInvite(teamId, sender)) return false;

        // already in a team?
        String current = memberToTeam.get(target);
        if (teamId.equals(current)) return false;              // already a member of this team
        if (current != null) {
            // Optional policy: disallow inviting players already in another team
            return false;
        }

        // set/replace invite
        long until = System.currentTimeMillis() + Math.max(10, ttlSeconds) * 1000L;
        pendingInvites.put(target, new Invite(teamId, sender, until));

        // notify target (if online) with clickable buttons
        EntityPlayerMP targetMp = FMLCommonHandler.instance().getMinecraftServerInstance()
                .getPlayerList().getPlayerByUUID(target);
        if (targetMp != null) sendInviteMessage(targetMp, t, sender);

        // notify inviter
        EntityPlayerMP invMp = FMLCommonHandler.instance().getMinecraftServerInstance()
                .getPlayerList().getPlayerByUUID(sender);
        if (invMp != null) invMp.sendMessage(new net.minecraft.util.text.TextComponentString(
                "§7[Teams] §rInvite sent to §e" + (targetMp != null ? targetMp.getName() : target.toString()) + "§r."
        ));

        return true;
    }

    // INVITE MESSAGE
    private void sendInviteMessage(EntityPlayerMP target, Team team, UUID inviter) {
        String inviterName = Optional.of(
                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(inviter)
        ).map(EntityPlayerMP::getName).orElse("someone");

        TextComponentString base =
                new TextComponentString("§7[kTeams] §rYou’ve been invited to join: §b" + team.name +"§r.");

        TextComponentString accept =
                new TextComponentString(" §a[ACCEPT]");
        accept.getStyle().setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, "/team accept"));
        accept.getStyle().setUnderlined(false);

        TextComponentString deny =
                new TextComponentString(" §c[DENY]");
        deny.getStyle().setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, "/team deny"));
        deny.getStyle().setUnderlined(false);

        accept.appendSibling(deny);
        target.sendMessage(base);
        target.sendMessage(accept);
    }

    public boolean acceptInvite(EntityPlayerMP target) {
        UUID id = target.getUniqueID();
        Invite inv = pendingInvites.remove(id);
        if (inv == null) {
            target.sendMessage(new TextComponentString("§7[kTeams] §cYou have no pending invites."));
            return false;
        }
        if (inv.expired()) {
            target.sendMessage(new TextComponentString("§7[kTeams] §cYour invite has expired."));
            return false;
        }
        Team t = teams.get(inv.teamId);
        if (t == null) {
            target.sendMessage(new TextComponentString("§7[kTeams] §cThat team no longer exists."));
            return false;
        }
        String cur = memberToTeam.get(id);
        if (cur != null && !cur.equals(inv.teamId)) {
            target.sendMessage(new TextComponentString("§7[kTeams] §cLeave your current team first."));
            return false;
        }

        boolean changed = addMember(inv.teamId, id);
        if (changed) {
            target.sendMessage(new TextComponentString("§7[kTeams] §aJoined §b " + t.name + "§a!"));
            broadcastToTeam(inv.teamId, "§7[kTeams] §e" + target.getName() + "§a joined the team.");
        } else {
            target.sendMessage(new TextComponentString("§7[kTeams] §cJoin failed."));
        }
        return changed;
    }

    public boolean denyInvite(EntityPlayerMP target) {
        UUID id = target.getUniqueID();
        Invite inv = pendingInvites.remove(id);
        if (inv == null) {
            target.sendMessage(new net.minecraft.util.text.TextComponentString("§7[kTeams] §cYou have no pending invites."));
            return false;
        }
        target.sendMessage(new net.minecraft.util.text.TextComponentString("§7[kTeams] §cInvite declined."));
        // optional: notify inviter if online
        EntityPlayerMP invMp = FMLCommonHandler.instance().getMinecraftServerInstance()
                .getPlayerList().getPlayerByUUID(inv.inviter);
        if (invMp != null) invMp.sendMessage(new net.minecraft.util.text.TextComponentString(
                "§7[kTeams] §e" + target.getName() + "§c declined your invite."
        ));
        return true;
    }

    public void purgeExpiredInvites() {
        pendingInvites.entrySet().removeIf(e -> e.getValue().expired());
    }

    private void broadcastToTeam(String teamId, String msg) {
        Team t = teams.get(teamId);
        if (t == null) return;
        for (UUID u : t.members) {
            EntityPlayerMP mp = FMLCommonHandler.instance().getMinecraftServerInstance()
                    .getPlayerList().getPlayerByUUID(u);
            if (mp != null) mp.sendMessage(new net.minecraft.util.text.TextComponentString(msg));
        }
    }

    public int getMaxClaimsPerTeam() {
        return configHandler.maxClaims;
    }

    private static final class Invite {
        final String teamId;
        final UUID inviter;
        final long expiresAtMillis;
        Invite(String teamId, UUID inviter, long expiresAtMillis) {
            this.teamId = teamId; this.inviter = inviter; this.expiresAtMillis = expiresAtMillis;
        }
        boolean expired() { return System.currentTimeMillis() > expiresAtMillis; }
    }

    // CHUNK STUFF

    public String getChunkOwner(ChunkPos pos) {return chunkOwners.get(ChunkPos.asLong(pos.x, pos.z));}

    public boolean claimChunk(String teamId, ChunkPos pos){
        if (!teams.containsKey(teamId)) return false;
        long key = ChunkPos.asLong(pos.x,pos.z);
        String prev = chunkOwners.put(key, teamId);
        if (!Objects.equals(prev, teamId)) {
            // if it was unowned or owned by someone else, update counters
            if (prev != null) {
                claimCounts.put(prev, Math.max(0, claimCounts.getOrDefault(prev, 1) - 1));
            }
            claimCounts.put(teamId, claimCounts.getOrDefault(teamId, 0) + 1);
            markDirty();
        }
        return true;
    }

    public boolean removeClaim(String teamId, ChunkPos pos){
        if (!teams.containsKey(teamId)) return false;
        long key = ChunkPos.asLong(pos.x,pos.z);
        String prev = chunkOwners.remove(key);
        if (prev != null) {
            if (prev.equals(teamId)) {
                // decrement only if your team actually owned it
                claimCounts.put(teamId, Math.max(0, claimCounts.getOrDefault(teamId, 1) - 1));
            } else {
                // It was someone else's; restore it
                chunkOwners.put(key, prev);
                return false;
            }
            markDirty();
        }
        return prev != null;
    }

    public int getClaimCount(String teamId) {
        return claimCounts.getOrDefault(teamId, 0);
    }

    public Map<Long, String> getAllClaimsView() {
        return Collections.unmodifiableMap(chunkOwners);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

        // Prevent Old Data
        teams.clear();
        chunkOwners.clear();
        memberToTeam.clear();
        claimCounts.clear();

        // Teams
        NBTTagList teamList = nbt.getTagList("teams", 10);

        // Every Team Setters
        for (int i = 0; i < teamList.tagCount(); i++) {


            NBTTagCompound cnbt = teamList.getCompoundTagAt(i);
            String id = cnbt.getString("id");
            String name = cnbt.getString("name");

            // Members
            Set<UUID> members = new HashSet<>();
            NBTTagList mlist = cnbt.getTagList("members", 8); // strings
            for (int j = 0; j < mlist.tagCount(); j++) {
                members.add(UUID.fromString(mlist.getStringTagAt(j)));
            }

            Team t = new Team(id, name);
            t.members.addAll(members);

            // Elders - May be missing
            Set<UUID> elders = new HashSet<>();
            NBTTagList elist = cnbt.getTagList("elders", 8); // strings
            for (int j = 0; j < mlist.tagCount(); j++) {
                try {elders.add(UUID.fromString(mlist.getStringTagAt(j)));} catch (Exception ignored){}
            }
            // Owner - May be missing on old saves
            String owner = cnbt.getString("owner");
            if (!owner.isEmpty()) {
                try { t.owner = UUID.fromString(owner); } catch (Exception ignored) {}
            }
            t.description = cnbt.hasKey("desc") ? cnbt.getString("desc") : "";
            if (cnbt.hasKey("home", 10)) {
                NBTTagCompound h = cnbt.getCompoundTag("home");
                t.homeDimension = h.getInteger("dim");
                t.home = new BlockPos(h.getInteger("x"), h.getInteger("y"), h.getInteger("z"));
            }

            t.elders.remove(t.owner);
            teams.put(id, t);


        }


        for (Map.Entry<String, Team> e : teams.entrySet()) {
            String cid = e.getKey();
            for (UUID u : e.getValue().members) {
                memberToTeam.put(u, cid);
            }
        }

        // Claims
        NBTTagList claims = nbt.getTagList("claims", 10);
        for (int i = 0; i < claims.tagCount(); i++) {
            NBTTagCompound cn = claims.getCompoundTagAt(i);
            long key = cn.getLong("chunk");
            String teamId = cn.getString("teams");
            chunkOwners.put(key, teamId);
        }

        for (String tid : teams.keySet()) claimCounts.put(tid, 0);
        for (String tid : chunkOwners.values()) {
            claimCounts.put(tid, claimCounts.getOrDefault(tid, 0) + 1);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        // Teams
        NBTTagList teamList = new NBTTagList();



        for (Team t : teams.values()) {

            //Save Team Ids & Names
            NBTTagCompound cn = new NBTTagCompound();
            cn.setString("id", t.id);
            cn.setString("name", t.name);

            //Save Team Members
            NBTTagList mlist = new NBTTagList();
            for (UUID u : t.members) mlist.appendTag(new NBTTagString(u.toString()));
            cn.setTag("members", mlist);

            //Save Team Owner
            cn.setString("owner", t.owner == null ? "" : t.owner.toString());

            //Save Team Elders
            NBTTagList elist = new NBTTagList();
            for (UUID e : t.elders) elist.appendTag(new NBTTagString(e.toString()));
            cn.setTag("elders", elist);

            cn.setString("desc", t.description == null ? "" : t.description);

            if (t.home != null) {
                NBTTagCompound h = new NBTTagCompound();
                cn.setInteger("dim", t.homeDimension);
                cn.setInteger("x", t.home.getX());
                cn.setInteger("y", t.home.getY());
                cn.setInteger("z", t.home.getZ());
                cn.setTag("home", h);
            }

            teamList.appendTag(cn);
        }

        nbt.setTag("teams", teamList);

        // Claims
        NBTTagList claims = new NBTTagList();
        for (Map.Entry<Long, String> e : chunkOwners.entrySet()) {
            NBTTagCompound cn = new NBTTagCompound();
            cn.setLong("chunk", e.getKey());
            cn.setString("teams", e.getValue());
            claims.appendTag(cn);
        }
        nbt.setTag("claims", claims);

        return nbt;

    }

    public static class Team {
        public final String id;
        public String name;
        public final Set<UUID> members = new HashSet<>();
        public UUID owner;
        public final Set<UUID> elders = new HashSet<>();
        public String description;
        public BlockPos home;
        public int homeDimension;
        public Team(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public boolean hasHome() { return home != null; }

        public void setDescription(String text) {
            this.description = (text == null) ? "" : text;
        }

        public void setHome(int dim, BlockPos pos) {
            if (pos == null) { clearHome(); return; }
            this.home = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
            this.homeDimension = dim;
        }

        public void clearHome() {
            this.home = null;
            this.homeDimension = Integer.MIN_VALUE;
        }
    }


    public enum ClaimResult {
        SUCCESS,                 // claimed an unowned chunk
        ALREADY_OWNED_BY_YOU,    // no change
        ALREADY_OWNED_BY_OTHER,  // blocked (unless allowSteal)
        STALE_TEAM,            // teamId not found
        LIMIT_REACHED
        ;

    }

    public int broadcastToTeam(String teamId, ITextComponent msg) {
        Team t = teams.get(teamId);
        if (t == null || msg == null) return 0;

        int sent = 0;
        net.minecraft.server.MinecraftServer server =
                net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server == null) return 0;

        for (java.util.UUID u : t.members) {
            net.minecraft.entity.player.EntityPlayerMP mp =
                    server.getPlayerList().getPlayerByUUID(u);
            if (mp != null) { // online only
                mp.sendMessage(msg);
                sent++;
            }
        }
        return sent;
    }
    public int broadcastToPlayers(java.util.List<net.minecraft.entity.player.EntityPlayerMP> players,
                                  net.minecraft.util.text.ITextComponent msg) {
        if (players == null || msg == null) return 0;
        int sent = 0;
        for (net.minecraft.entity.player.EntityPlayerMP mp : players) {
            if (mp != null) {
                mp.sendMessage(msg);
                sent++;
            }
        }
        return sent;
    }
    public int broadcastToTeamExcept(String teamId, java.util.UUID except,ITextComponent msg) {
        Team t = teams.get(teamId);
        if (t == null || msg == null) return 0;

        int sent = 0;
        net.minecraft.server.MinecraftServer server =
                net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server == null) return 0;

        for (java.util.UUID u : t.members) {
            if (u.equals(except)) continue;
            net.minecraft.entity.player.EntityPlayerMP mp =
                    server.getPlayerList().getPlayerByUUID(u);
            if (mp != null) {
                mp.sendMessage(msg);
                sent++;
            }
        }
        return sent;
    }

    // might use for elders chat or anonymous report to owner
    public int broadcastToTeamWithMinRole(String teamId, TeamRole minRole, ITextComponent msg) {
        Team t = teams.get(teamId);
        if (t == null || msg == null) return 0;

        int sent = 0;
        net.minecraft.server.MinecraftServer server =
                net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server == null) return 0;

        for (java.util.UUID u : t.members) {
            TeamRole r = roleOf(teamId, u);
            if (r == null || r.priority < minRole.priority) continue;

            net.minecraft.entity.player.EntityPlayerMP mp =
                    server.getPlayerList().getPlayerByUUID(u);
            if (mp != null) {
                mp.sendMessage(msg);
                sent++;
            }
        }
        return sent;
    }



    public ClaimResult claimChunkChecked(String teamId, ChunkPos pos, boolean allowSteal, int bonusClaims) {
        if (!teams.containsKey(teamId)) return ClaimResult.STALE_TEAM;

        long key = ChunkPos.asLong(pos.x, pos.z);
        String current = chunkOwners.get(key);

        if (current == null) {
            // unowned -> check limit before claiming
            if (getClaimCount(teamId) >= configHandler.maxClaims) return ClaimResult.LIMIT_REACHED;
            claimChunk(teamId, pos);
            return ClaimResult.SUCCESS;
        }
        if (current.equals(teamId)) {
            return ClaimResult.ALREADY_OWNED_BY_YOU;
        }
        if (!allowSteal) {
            return ClaimResult.ALREADY_OWNED_BY_OTHER;
        }


        if (getClaimCount(teamId) >= (configHandler.maxClaims + bonusClaims)) return ClaimResult.LIMIT_REACHED;

        claimChunk(teamId, pos);
        return ClaimResult.SUCCESS;
    }


    public enum TeamRole {
        OWNER(3),ELDER(2),MEMBER(1);
        public final int priority;
        TeamRole(int p) {this.priority = p;}

        public boolean higherThan(TeamRole other){ return this.priority > other.priority;}
    }
}
