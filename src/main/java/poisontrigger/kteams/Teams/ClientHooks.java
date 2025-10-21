package poisontrigger.kteams.Teams;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import poisontrigger.kteams.Blocks.Flag.TileEntityFlag;

import java.util.UUID;

@SideOnly(Side.CLIENT)
public final class ClientHooks {
    private ClientHooks() {}

    public static void applyFlagOwner(BlockPos pos, String owner) {
        Minecraft mc = Minecraft.getMinecraft();
        World w = mc.world;
        if (w == null) return;
        TileEntity te = w.getTileEntity(pos);
        if (te instanceof TileEntityFlag) {
            ((TileEntityFlag) te).setOwner(owner);
            ClientTeam.setOwner(owner);
        }
    }
}
