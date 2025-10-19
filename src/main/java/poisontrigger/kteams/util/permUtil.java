package poisontrigger.kteams.util;


import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.permission.PermissionAPI;

public final class permUtil {
    public static boolean has(ICommandSender sender, String node) {
        if (!(sender instanceof EntityPlayerMP)) {
            // console/command blocks get full access by convention
            return true;
        }
        return PermissionAPI.hasPermission((EntityPlayerMP) sender, node);
    }

    // fallback: if no handler installed, OPs pass.
    public static boolean hasOrOp(ICommandSender sender, String node, int opLevel, MinecraftServer server) {
        if (has(sender, node)) return true;
        return sender.canUseCommand(opLevel, ""); // vanilla OP gate as fallback
    }

    private permUtil() {}
}
