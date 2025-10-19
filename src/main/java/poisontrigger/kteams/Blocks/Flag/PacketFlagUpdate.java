package poisontrigger.kteams.Blocks.Flag;


import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketFlagUpdate implements IMessage {
    public int dim;               // dimension id
    public BlockPos pos;          // WHICH flag block this update is for
    public String ownerTeam;

    public PacketFlagUpdate() {} // required

    public PacketFlagUpdate(int dim, BlockPos pos, String ownerTeam) {
        this.dim = dim;
        this.pos = pos;
        this.ownerTeam = ownerTeam == null ? "wilderness" : ownerTeam;

    }

    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(dim);
        buf.writeLong(pos.toLong()); // compact BlockPos
        ByteBufUtils.writeUTF8String(buf, ownerTeam);
    }

    @Override public void fromBytes(ByteBuf buf) {
        dim = buf.readInt();
        pos = BlockPos.fromLong(buf.readLong());
        ownerTeam = ByteBufUtils.readUTF8String(buf);
    }
}
