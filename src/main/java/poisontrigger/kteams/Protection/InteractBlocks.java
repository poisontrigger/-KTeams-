package poisontrigger.kteams.Protection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import poisontrigger.kteams.Kteams;
import poisontrigger.kteams.util.Bypass;

import javax.annotation.Nullable;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID)
public final class InteractBlocks {

    /** Returns the team ID that owns the chunk at pos, or null if wilderness. */
    private static @Nullable
    String ownerAt(World w, BlockPos pos) {
        return poisontrigger.kteams.Teams.TeamData.get(w).getChunkOwner(new net.minecraft.util.math.ChunkPos(pos));
    }

    /** Replace this with your actual membership check. */
    private static boolean isMember(World w, UUID playerId, @Nullable String ownerTeamId) {
        if (ownerTeamId == null) return true; // wilderness
        poisontrigger.kteams.Teams.TeamData data = poisontrigger.kteams.Teams.TeamData.get(w);
        return data.isMemberOf(playerId,ownerTeamId);
    }

    /** Block non-member right-clicks on TileEntities in claimed chunks. */
    @SubscribeEvent
    public static void onRightClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock e) {
        if (e.getWorld().isRemote) return;

        World world = e.getWorld();
        BlockPos pos = e.getPos();
        Block b = e.getWorld().getBlockState(e.getPos()).getBlock();
        net.minecraft.entity.player.EntityPlayer player = e.getEntityPlayer();
        if (Bypass.isOpBypass(player)) return;
        // If the block has a TileEntity, guard it.
        net.minecraft.tileentity.TileEntity te = world.getTileEntity(pos);
        if (te == null) return;

        String owner = ownerAt(world, pos);
        if (owner == null) return; // wilderness OK

        if (!isMember(world, player.getUniqueID(), owner)) {
            // Deny both the block’s onBlockActivated and the item’s use (wrench, bucket, etc.)
            e.setUseBlock(net.minecraftforge.fml.common.eventhandler.Event.Result.DENY);
            e.setUseItem(net.minecraftforge.fml.common.eventhandler.Event.Result.DENY);
            e.setCanceled(true);
            if (b instanceof BlockDoor || b instanceof BlockTrapDoor || b instanceof BlockFenceGate) {
                // hard cancel the activation
                e.setCanceled(true);                                   // stop onBlockActivated
                e.setCancellationResult(EnumActionResult.FAIL);         // tell client "nope"
                e.setUseBlock(net.minecraftforge.fml.common.eventhandler.Event.Result.DENY);
                e.setUseItem(net.minecraftforge.fml.common.eventhandler.Event.Result.DENY);
            }
            // Optional feedback
            player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(
                    "§cYou can’t interact with blocks in another team’s claim."), true);
        }
    }

    /** (Optional) Also block left-click “use” on TE (some mods rotate/wrench on left click). */
    @SubscribeEvent
    public static void onLeftClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock e) {
        if (e.getWorld().isRemote) return;
        World world = e.getWorld();
        BlockPos pos = e.getPos();
        if (world.getTileEntity(pos) == null) return;
        if (Bypass.isOpBypass(e.getEntityPlayer())) return;
        String owner = ownerAt(world, pos);
        if (owner != null && !isMember(world, e.getEntityPlayer().getUniqueID(), owner)) {
            e.setCanceled(true);
        }
    }
}
