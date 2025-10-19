package poisontrigger.kteams.Teams.commands;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import poisontrigger.kteams.Blocks.BlockFlag;
import poisontrigger.kteams.Blocks.Flag.FlagRegistry;
import poisontrigger.kteams.Blocks.Flag.TileEntityFlag;
import poisontrigger.kteams.Teams.TeamData;
import poisontrigger.kteams.util.configHandler;

import java.util.Locale;

import static net.minecraft.command.CommandBase.parseBlockPos;

public class bindFlag {
    public static void bindFlag(ICommandSender sender, String[] args, MinecraftServer server, TileEntityFlag.FlagKind targetKind) throws CommandException {

        if (sender.getEntityWorld().isRemote) return;
        World world = sender.getEntityWorld();

        // --- resolve target position (same as before) ---
        BlockPos topPos;
        if (args.length == 5) {
            BlockPos any = parseBlockPos(sender, args, 2, false);
            topPos = normalizeToTop(world, any);
        } else {
            if (!(sender instanceof EntityPlayerMP)) {
                throw new CommandException("kteams.command.bind.console_coordinates");
            }
            topPos = raycastFlagTop(world, (EntityPlayerMP) sender);
            if (topPos == null) throw new CommandException("kteams.command.bind.coordinates");
        }

        TileEntity te = world.getTileEntity(topPos);
        if (!(te instanceof TileEntityFlag)) throw new CommandException("No flag at " + topPos);

        TileEntityFlag flag = (TileEntityFlag) te;
        TileEntityFlag.FlagKind current = flag.getKind();
        if (current == targetKind) {
            sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §fKind is already §e" + current + "§f."));
            return;
        }

        // --- proximity rule for blocking kinds (TEAM/EVENT) ---
        boolean targetBlocks = (targetKind != TileEntityFlag.FlagKind.DECORATIVE);
        FlagRegistry reg = FlagRegistry.get(world);
        if (targetBlocks) {
            double R  = configHandler.flagRadius;
            double Yh = configHandler.flagHeight;
            if (reg.anyWithin(world, topPos, 2 * R, Yh)) {
                throw new CommandException("§7[§rkTeams§7] §cToo close to another flag (within " + (int)(2*R) + " blocks).");
            }
        }

        // --- registry + apply ---
        if (targetBlocks) {
            reg.add(topPos);
        } else {
            flag.endCaptureBossbar();
            reg.remove(topPos);
        }
        if (targetKind == TileEntityFlag.FlagKind.TEAM) {
            String binder = null;
            if (sender instanceof EntityPlayerMP) {
                TeamData data = TeamData.get(world);
                binder = data.getTeamIdOf(((EntityPlayerMP) sender).getUniqueID());
                if (binder != null) binder = binder.toLowerCase(Locale.ROOT);
            }
            flag.setBinderTeamId(binder);
        } else {
            flag.setBinderTeamId(null);
        }
        flag.setKind(targetKind);
        flag.pushSync();
        sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §fFlag at §e" + topPos + " §fset to §b" + targetKind + "§f."));
    }
    /** If the position is any part of your 3-block flag, return the TOP pos; else return pos or TE position. */
    private static BlockPos normalizeToTop(World world, BlockPos any) {
        IBlockState st = world.getBlockState(any);
        if (st.getBlock() instanceof BlockFlag) {
            BlockFlag.Part part = st.getValue(BlockFlag.PART);
            BlockPos base = (part == BlockFlag.Part.BASE) ? any
                    : (part == BlockFlag.Part.MIDDLE) ? any.down()
                    : any.down(2);
            return base.up(2);
        }
        TileEntity te = world.getTileEntity(any);
        if (te instanceof TileEntityFlag) return any;
        return any;
    }

    /** Raycast from player eyes to a block, then normalize to TOP if it’s a flag. */
    @javax.annotation.Nullable
    private static BlockPos raycastFlagTop(World world, EntityPlayerMP p) {
        double reach = p.interactionManager.getBlockReachDistance();
        RayTraceResult rt = p.rayTrace(reach, 1.0f);
        if (rt == null || rt.typeOfHit != RayTraceResult.Type.BLOCK) return null;
        return normalizeToTop(world, rt.getBlockPos());
    }
}
