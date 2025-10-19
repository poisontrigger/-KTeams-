package poisontrigger.kteams.Teams;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import poisontrigger.kteams.Kteams;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public final class HomeTeleportManager {

    private HomeTeleportManager() {}

    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    public static void queue(EntityPlayerMP p, int targetDim, BlockPos targetPos, int warmupSeconds) {
        long now = p.world.getTotalWorldTime();
        Pending ex = PENDING.get(p.getUniqueID());
        if (ex != null) { // replace existing
            PENDING.remove(p.getUniqueID());
        }
        Pending pend = new Pending(
                p.getUniqueID(),
                p.dimension,
                new BlockPos(p.posX, p.posY, p.posZ),
                targetDim,
                targetPos,
                now,
                now + Math.max(0, warmupSeconds) * 20L
        );
        PENDING.put(p.getUniqueID(), pend);

        p.sendMessage(new TextComponentString(
                "§7[kTeams] §fTeleporting home in §e" + warmupSeconds + "§fs… §8(Don’t move or take damage)"
        ));
    }

    public static boolean isPending(UUID id) { return PENDING.containsKey(id); }

    private static void cancel(UUID id, EntityPlayerMP p) {
        PENDING.remove(id);
        if (p != null) {
            p.sendMessage(new TextComponentString("§7[kTeams] §cHome teleport cancelled."));
        }
    }

    // --- Events ---

    // Cancel on damage
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        if (!(e.getEntityLiving() instanceof EntityPlayerMP)) return;
        EntityPlayerMP p = (EntityPlayerMP) e.getEntityLiving();
        if (PENDING.containsKey(p.getUniqueID())) {
            cancel(p.getUniqueID(),p);
        }
    }

    // Cancel on logout
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (!(e.player instanceof EntityPlayerMP)) return;
        PENDING.remove(e.player.getUniqueID());
    }

    // Cancel on movement > 2 blocks in ANY direction; execute when time is up
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (!(e.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP p = (EntityPlayerMP) e.player;

        Pending pend = PENDING.get(p.getUniqueID());
        if (pend == null) return;

        // movement check (+/- 2 blocks any axis) or dimension change
        if (p.dimension != pend.startDim) {
            cancel(p.getUniqueID(), p);
            return;
        }
        BlockPos now = new BlockPos(p.posX, p.posY, p.posZ);
        if (Math.abs(now.getX() - pend.startPos.getX()) > 2
                || Math.abs(now.getY() - pend.startPos.getY()) > 2
                || Math.abs(now.getZ() - pend.startPos.getZ()) > 2) {
            cancel(p.getUniqueID(), p);
            return;
        }

        long time = p.world.getTotalWorldTime();
        if (time >= pend.executeTick) {
            // Execute teleport
            PENDING.remove(p.getUniqueID());
            doTeleport(p, pend.targetDim, pend.targetPos);
        }
    }

    private static void doTeleport(EntityPlayerMP p, int dim, BlockPos pos) {
        if (p.dimension != dim) {
            WorldServer target = p.getServer().getWorld(dim);
            if (target == null) {
                p.sendMessage(new TextComponentString("§7[kTeams] §cTarget dimension unavailable."));
                return;
            }
            net.minecraftforge.common.util.ITeleporter tp = (world, entity, yaw) ->
                    entity.setPosition(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
            p.changeDimension(dim, tp);
        } else {
            p.connection.setPlayerLocation(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, p.rotationYaw, p.rotationPitch);
        }
        p.sendMessage(new TextComponentString("§7[kTeams] §aTeleported to team home."));
    }

    private static final class Pending {
        final UUID player;
        final int startDim;
        final BlockPos startPos;
        final int targetDim;
        final BlockPos targetPos;
        final long startTick;
        final long executeTick;

        Pending(UUID player, int startDim, BlockPos startPos,
                int targetDim, BlockPos targetPos, long startTick, long executeTick) {
            this.player = player;
            this.startDim = startDim;
            this.startPos = startPos;
            this.targetDim = targetDim;
            this.targetPos = targetPos;
            this.startTick = startTick;
            this.executeTick = executeTick;
        }
    }
}
