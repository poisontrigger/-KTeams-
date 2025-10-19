package poisontrigger.kteams.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import poisontrigger.kteams.Blocks.Flag.PacketFlagUpdate;
import poisontrigger.kteams.Blocks.Flag.PacketFlagUpdateHandler;
import poisontrigger.kteams.Blocks.Flag.TileEntityFlag;
import poisontrigger.kteams.Kteams;
import poisontrigger.kteams.Teams.ClientTeam;

public final class Net {
    public static final SimpleNetworkWrapper CH = NetworkRegistry.INSTANCE.newSimpleChannel(Kteams.MOD_ID);

    private static int ID = 0;
    public static void init() {
        CH.registerMessage(HudText.Handler.class, HudText.class, ID++, Side.CLIENT);
        CH.registerMessage(MapPacket.Handler.class, MapPacket.class, ID++, Side.CLIENT);
        CH.registerMessage(S2CSetClientTeam.Handler.class, S2CSetClientTeam.class,ID++, Side.CLIENT);
        CH.registerMessage(S2CFlagOwner.Handler.class, S2CFlagOwner.class, ID++, Side.CLIENT);
        CH.registerMessage(PacketFlagUpdateHandler.class, PacketFlagUpdate.class,ID++,Side.CLIENT);

    }




    public static class HudText implements IMessage {
        public String text;

        public HudText() {
        }

        public HudText(String text) {
            this.text = text;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, text);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            text = ByteBufUtils.readUTF8String(buf);
        }

        public static class Handler implements IMessageHandler<HudText, IMessage> {
            @Override
            public IMessage onMessage(HudText msg, MessageContext ctx) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    poisontrigger.kteams.hud.ClientHud.setTerritory(msg.text);
                });
                return null;
            }
        }
    }

    public static class MapPacket implements IMessage {
        public int radius, seconds;
        public byte[] cells;
        public java.util.List<Character> legendSyms;  // symbols used (size = N)
        public java.util.List<String>   legendNames;  // display names (N)
        public java.util.List<Integer>  legendColors; // ARGB colors (N)


        public MapPacket() {}

        public MapPacket(int radius, int seconds, byte[] cells,
                         java.util.List<Character> legendSyms,
                         java.util.List<String> legendNames,
                         java.util.List<Integer> legendColors) {
            this.radius = radius; this.seconds = seconds; this.cells = cells;
            this.legendSyms = legendSyms; this.legendNames = legendNames; this.legendColors = legendColors;
        }


        @Override public void toBytes(ByteBuf buf) {
            buf.writeInt(radius);
            buf.writeInt(seconds);
            buf.writeInt(cells.length);
            buf.writeBytes(cells);

            buf.writeInt(legendSyms.size());
            for (int i = 0; i < legendSyms.size(); i++) {
                // write symbol as UTF-8 string of length 1
                ByteBufUtils.writeUTF8String(buf, String.valueOf(legendSyms.get(i)));
                ByteBufUtils.writeUTF8String(buf, legendNames.get(i));
                buf.writeInt(legendColors.get(i));
            }
        }
        @Override public void fromBytes(ByteBuf buf) {
            radius = buf.readInt();
            seconds = buf.readInt();
            int nCells = buf.readInt();
            cells = new byte[nCells];
            buf.readBytes(cells);

            int n = buf.readInt();
            legendSyms = new java.util.ArrayList<>(n);
            legendNames = new java.util.ArrayList<>(n);
            legendColors = new java.util.ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                String sym = ByteBufUtils.readUTF8String(buf);
                legendSyms.add(sym.isEmpty() ? '?' : sym.charAt(0));
                legendNames.add(ByteBufUtils.readUTF8String(buf));
                legendColors.add(buf.readInt());
            }
        }

        public static class Handler implements IMessageHandler<MapPacket, IMessage> {
            @Override
            public net.minecraftforge.fml.common.network.simpleimpl.IMessage onMessage(final MapPacket msg, final net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx) {
                net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                    poisontrigger.kteams.Teams.TeamMap.show(msg);
                });
                return null;
            }
        }
    }
    public static void sendClientTeam(EntityPlayerMP player, String teamId) {
        CH.sendTo(new S2CSetClientTeam(teamId), player);
        System.out.println("[Client Team]: " + teamId + ":" + player );
    }

    /** Server->Client packet that sets the client's team id. */
    public static class S2CSetClientTeam implements IMessage {
        public String teamId;

        public S2CSetClientTeam() {}
        public S2CSetClientTeam(String teamId) { this.teamId = teamId; }

        @Override public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, teamId == null ? "" : teamId);
        }
        @Override public void fromBytes(ByteBuf buf) {
            teamId = ByteBufUtils.readUTF8String(buf);
        }


        private static volatile net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper CHANNEL_INSTANCE;
    private static boolean messagesRegistered = false;

    /** Call this to get the channel without creating duplicates. */
    public static SimpleNetworkWrapper channel() {
        if (CHANNEL_INSTANCE == null) {
            synchronized (Net.class) {
                if (CHANNEL_INSTANCE == null) {
                    CHANNEL_INSTANCE = net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE
                            .newSimpleChannel(poisontrigger.kteams.Kteams.MOD_ID);
                    }
                }
            }
            return CHANNEL_INSTANCE;
        }
        @SideOnly(Side.CLIENT)
        public static class Handler implements IMessageHandler<S2CSetClientTeam, IMessage> {
            @Override public IMessage onMessage(S2CSetClientTeam msg, MessageContext ctx) {
                    ClientTeam.set(msg.teamId);

                return null;
            }
        }

    }
    public static class S2CFlagOwner implements IMessage {
        public BlockPos pos;
        public String owner;

        public S2CFlagOwner() {}
        public S2CFlagOwner(BlockPos pos, String owner) { this.pos = pos; this.owner = owner; }

        @Override public void toBytes(ByteBuf buf) {
            buf.writeLong(pos.toLong());
            ByteBufUtils.writeUTF8String(buf, owner == null ? "wilderness" : owner);
        }
        @Override public void fromBytes(ByteBuf buf) {
            pos = BlockPos.fromLong(buf.readLong());
            owner = ByteBufUtils.readUTF8String(buf);
        }

        @SideOnly(Side.CLIENT)
        public static class Handler implements IMessageHandler<S2CFlagOwner, IMessage> {
            @Override public IMessage onMessage(S2CFlagOwner msg, MessageContext ctx) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    World w = Minecraft.getMinecraft().world;
                    if (w == null) return;
                    TileEntity te = w.getTileEntity(msg.pos);
                    if (te instanceof TileEntityFlag) {
                        System.out.println("[Received Packet] :" + msg.owner);
                        ((TileEntityFlag) te).setOwner(msg.owner);
                        ClientTeam.setOwner(msg.owner);
                    }
                });
                return null;
            }
        }
        public static void sendFlagOwnerAround(WorldServer ws, BlockPos pos, String owner) {
            NetworkRegistry.TargetPoint tp = new NetworkRegistry.TargetPoint(ws.provider.getDimension(),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 128);
            CH.sendToAllAround(new S2CFlagOwner(pos, owner), tp);
            System.out.println("[Sent Packet: Flag Owner] "+pos + " : " + owner);
        }
    }
}

