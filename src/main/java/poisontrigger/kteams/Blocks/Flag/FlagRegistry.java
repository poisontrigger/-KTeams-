package poisontrigger.kteams.Blocks.Flag;


import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FlagRegistry extends WorldSavedData {
    public static final String DATA_NAME = "kTeams.flagRegistry";
    private final Set<Long> flags = new HashSet<>(); // BlockPos.asLong()

    public FlagRegistry() { super(DATA_NAME); }
    public FlagRegistry(String name) { super(name); }

    public static FlagRegistry get(World world) {
        if (world.isRemote) throw new IllegalStateException("Server world only");
        MapStorage storage = world.getPerWorldStorage();
        FlagRegistry data = (FlagRegistry) storage.getOrLoadData(FlagRegistry.class, DATA_NAME);
        if (data == null) {
            data = new FlagRegistry();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    public void add(BlockPos pos) {
        if (flags.add(pos.toLong())) markDirty();
    }
    public void remove(BlockPos pos) {
        if (flags.remove(pos.toLong())) markDirty();
    }

  // Check within radius
    public boolean anyWithin(World world, BlockPos center, double radius, double yHalf) {
        final double cx = center.getX() + 0.5;
        final double cy = center.getY() + 0.5;
        final double cz = center.getZ() + 0.5;
        final double r2 = radius * radius;

        for (Long l : flags) {
            BlockPos p = BlockPos.fromLong(l);
            if (p.equals(center)) continue; // same block

            // Skip decorative or non-flag tiles
            if (!isBlockingFlag(world, p)) continue;

            double dy = Math.abs((p.getY() + 0.5) - cy);
            if (dy > yHalf) continue;

            double dx = (p.getX() + 0.5) - cx;
            double dz = (p.getZ() + 0.5) - cz;
            if (dx*dx + dz*dz <= r2) return true;
        }
        return false;
    }

    // might use later
    public void sweepInvalidAndDecorative(World world) {
        boolean dirty = false;
        for (Iterator<Long> it = flags.iterator(); it.hasNext(); ) {
            BlockPos p = BlockPos.fromLong(it.next());
            TileEntity te = world.getTileEntity(p);
            boolean keep = te instanceof TileEntityFlag &&
                    ((TileEntityFlag) te).getKind() != TileEntityFlag.FlagKind.DECORATIVE;
            if (!keep) { it.remove(); dirty = true; }
        }
        if (dirty) markDirty();
    }

    private boolean isBlockingFlag(World world, BlockPos pos) {
        if (world == null) return false;
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityFlag)) return false;
        TileEntityFlag f = (TileEntityFlag) te;
        TileEntityFlag.FlagKind k = f.getKind();
        return k != TileEntityFlag.FlagKind.DECORATIVE; // ignore decorative flags
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        flags.clear();
        NBTTagList list = nbt.getTagList("flags", 4); // long
        for (int i = 0; i < list.tagCount(); i++) {
            flags.add(((NBTTagLong) list.get(i)).getLong());
        }
    }
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (Long l : flags) list.appendTag(new NBTTagLong(l));
        nbt.setTag("flags", list);
        return nbt;
    }
}