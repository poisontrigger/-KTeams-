package poisontrigger.kteams.Blocks.Flag;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import poisontrigger.kteams.Sounds.ModSounds;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.network.Net;
import poisontrigger.kteams.util.LogHandler;
import poisontrigger.kteams.util.configHandler;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CaptureFlag {

    // on Tick :
    public static void capture(World w, BlockPos pos, TileEntityFlag te) {
        if (w == null || pos == null || te == null || w.isRemote || te.getKind() == TileEntityFlag.FlagKind.DECORATIVE) return;

        // --- get team data ---
        TeamData data = TeamData.get(w);
        if (data == null) {
            if ((w.getTotalWorldTime() % 100L) == 0L)
                System.out.println("[Flag] TeamData null; skipping at " + pos);
            return;
        }
        // get players in radius
                List<EntityPlayerMP> players = getPlayersInRadius(w, pos, configHandler.flagRadius,configHandler.flagHeight);
                if (players.isEmpty()) return;

        // owner id (always lower-case for map keys)
        String owner = Optional.ofNullable(te.getTeamInControl())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .orElse("wilderness");

        // count players by team (unknown = "wilderness")
        Map<String, Long> counts = players.stream()
                .map(p -> {
                    TeamData.Team t = data.getTeamOf(p.getUniqueID());
                    String id = (t != null && t.id != null) ? t.id : "wilderness";
                    return id.toLowerCase(Locale.ROOT);
                })
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        long defending = counts.getOrDefault(owner, 0L);
        long total = players.size();
        long opposing = Math.max(0L, total - defending);

        // --- points math ---
        final long MAX_POINTS = configHandler.capMaxPoints;
        long current = Math.max(0L, te.getPoints()); // guard
        // helper: log4(n) + 1, but 0 when n <= 0
        java.util.function.LongToDoubleFunction log4p1 = n -> {
            if (n <= 0L) return 0.0;
            return (Math.log(n) / Math.log(4.0)) + 1.0;
        };

        double termDef = log4p1.applyAsDouble(defending);
        double termOpp = log4p1.applyAsDouble(opposing);

        // delta = 20 * points * (termDef - termOpp)
        double deltaD = configHandler.capDeltaScaler* configHandler.capDelay * (termDef - termOpp);

        long newPoints = current + Math.round(deltaD);
        newPoints = Math.max(0L, Math.min(MAX_POINTS, newPoints));
        te.setPoints(newPoints);
        String prevOwner = te.getTeamInControl();





        // If points are equal to Max then end the capture
        if (newPoints == MAX_POINTS && prevOwner != "wilderness") {
            if(te.getBinderTeamId() == prevOwner && te.getKind() == TileEntityFlag.FlagKind.TEAM){return;}
            //win method
            te.setKind(TileEntityFlag.FlagKind.DECORATIVE);
            te.getWorld().playSound(null, te.getPos(), ModSounds.FLAG_CAPTURE_COMPLETE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            data.broadcastToPlayers(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers(), new TextComponentString("§7[kTeams] §a" + prevOwner + " has won the event!"));
            return;
        }




        // If points hit zero, owner becomes the majority team present
        if (newPoints == 0 && !counts.isEmpty()) {
            String majority = counts.entrySet().stream()
                    .max(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue)
                            .thenComparing(Map.Entry::getKey))
                    .get().getKey();

            if (!Objects.equals(owner, majority)) {
                World world = te.getWorld();
                if (!world.isRemote) {

                    String newOwner = majority; // the team that just captured
                    if (te != null) te.setOwner(newOwner);
                }
                te.setPoints(configHandler.capBonus); // bonus so it doesn’t immediately flip-flop

                te.pushSync();
                notifyUpdate(te.getWorld(),te,majority);
                sendUpdatePacket(te,te.getWorld());
                world.playSound(null, te.getPos(), ModSounds.FLAG_CAPTURE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                String l = ("[FLAG] owner changed from :" + prevOwner + " to: " + majority);
                LogHandler.get().log(l,null,null,"FLAG.CAPTURE_NEW.OWNER");
                TeamData.get(world).broadcastToTeam(majority, new TextComponentString("§7[kTeams] §aYour team gained control of the flag."));
                TeamData.get(world).broadcastToTeam(prevOwner, new TextComponentString("§7[kTeams] §cYour team lost control of the flag."));
            }
        }

        te.markDirty();

    }
    public static List<EntityPlayerMP> getPlayersInRadius(World w, BlockPos pos, double R, double Yh) {
        if (w == null || w.isRemote) return Collections.emptyList();

        AxisAlignedBB aabb = new AxisAlignedBB(pos).grow(R, Yh, R);
        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        double r2 = R * R;

        List<EntityPlayerMP> list = w.getEntitiesWithinAABB(EntityPlayerMP.class, aabb, p -> {
            if (p == null || p.isDead) return false;
            if (configHandler.ignoreSpectators && p.isSpectator()) return false;
             if (configHandler.ignoreCreative && p.capabilities.isCreativeMode) return false;

            double dx = p.posX - cx;
            double dz = p.posZ - cz;
            return (dx*dx + dz*dz) <= r2; //
        });
        return list == null ? Collections.emptyList() : list;

    }

    private static void sendUpdatePacket(TileEntityFlag te, World world){
        WorldServer ws = (WorldServer) world; // ensure server world
        if (te instanceof TileEntityFlag) {
            TileEntityFlag flag = (TileEntityFlag) te;

            PacketFlagUpdate pkt = new PacketFlagUpdate(
                    ws.provider.getDimension(),
                    flag.getPos(),
                    flag.getTeamInControl()
            );

            // send to tracking players around this flag te
            Net.CH.sendToAllAround(pkt,
                    new net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint(
                            ws.provider.getDimension(), flag.getPos().getX() + 0.5,
                            flag.getPos().getY() + 0.5, flag.getPos().getZ() + 0.5, 256));
        }
    }
    public static void notifyUpdate(World world, TileEntityFlag te, String newOwner){
        if (!world.isRemote) {
            BlockPos pos = te.getPos();// SERVER only
            te.setOwner(newOwner);
            te.markDirty();
            IBlockState st = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, st, st, 3); // fires TE update packet

            Net.S2CFlagOwner.sendFlagOwnerAround((WorldServer) world, pos, newOwner);
        } else {
            System.out.println("[CLIENT] tried to send flag owner");
        }
    }
}
