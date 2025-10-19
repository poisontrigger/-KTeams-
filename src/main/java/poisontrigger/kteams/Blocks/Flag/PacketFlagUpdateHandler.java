package poisontrigger.kteams.Blocks.Flag;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFlagUpdateHandler implements IMessageHandler<PacketFlagUpdate, IMessage> {

    @Override
    public IMessage onMessage(PacketFlagUpdate msg, MessageContext ctx){
        Minecraft.getMinecraft().addScheduledTask(() -> {
            // dim check
            if (Minecraft.getMinecraft().world == null ||
                    Minecraft.getMinecraft().world.provider.getDimension() != msg.dim) return;

            TileEntity te = Minecraft.getMinecraft().world.getTileEntity(msg.pos);
            if (!(te instanceof TileEntityFlag)) return;
            TileEntityFlag flag = (TileEntityFlag) te;

            // process the received packet
            flag.clientApplyPacket(msg.ownerTeam);
        });
        return null;
    }

}
