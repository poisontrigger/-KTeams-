package poisontrigger.kteams.Blocks.Flag;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import poisontrigger.kteams.util.configHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class TileEntityFlag extends TileEntity implements ITickable {
    private int color = 0x1CA3EC; // default blue
    private long points = 1_000L;
    private boolean capturing = true;
    private float captureProgress = 0;
    private String teamInControl = "wilderness";
    private String binderTeamId = null; // lower-case team id, or null if none
    // client-only mirror of the current owner, updated by S2C packet
    private String ownerTeamClient = "wilderness";
    private FlagKind kind = FlagKind.DECORATIVE;

    private final BossInfoServer bossbar = new BossInfoServer(
            new TextComponentString("Capturing Flag: 0%"), // initial title
            BossInfo.Color.RED,
            BossInfo.Overlay.NOTCHED_10
    );

    public @Nullable String getBinderTeamId() { return binderTeamId; }
    public void setBinderTeamId(@Nullable String id) {
        String v = (id == null || id.isEmpty()) ? null : id.toLowerCase(Locale.ROOT);
        if (!Objects.equals(v, this.binderTeamId)) {
            this.binderTeamId = v;
            markDirty();
            if (world != null && !world.isRemote) {
                IBlockState s = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, s, s, 3);
            }
        }
    }

    // disable dragon stuff
    {
        bossbar.setDarkenSky(false);
        bossbar.setCreateFog(false);
        bossbar.setPlayEndBossMusic(false);
        bossbar.setVisible(false);
        bossbar.setPercent(0.0f);
    }
    public FlagKind getKind() { return kind; }

    public void setKind(FlagKind kind) {
        if (kind == null) kind = FlagKind.DECORATIVE;
        if (this.kind == kind) return;

        this.kind = kind;

        if (world != null && !world.isRemote) {
            if (this.kind == FlagKind.DECORATIVE) {
                // decorative = ensure it never blocks, never shows bossbar
                capturing = false;
                endCaptureBossbar();
                FlagRegistry.get(world).remove(pos); // <-- REMOVE from registry
            } else {
                // TEAM or EVENT = ensure it’s registered
                FlagRegistry.get(world).add(pos);    // <-- ADD to registry
            }

            IBlockState s = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, s, s, 3);
        }
        markDirty();
    }
    @Override
    public void update() {
        if (world == null || world.isRemote ||this.kind == FlagKind.DECORATIVE) return;
        long delay = 10L;
        if ((world.getTotalWorldTime() % delay) == 0L) {

            System.out.println("[Flag] " + pos + " owner=" + getTeamInControl() + " points=" + getPoints());
            CaptureFlag.capture(world, pos, this);
            setCaptureProgress();
            System.out.println(captureProgress);
        }
        if (capturing) {

            if (this.kind == FlagKind.DECORATIVE && world != null && !world.isRemote) {
                capturing = false;
                endCaptureBossbar(); // hides + clears viewers
                return;
            }

            bossbar.setVisible(true);
            float p = captureProgress;
            bossbar.setPercent(p);
            bossbar.setColor(p < 1f ? BossInfo.Color.RED : BossInfo.Color.GREEN);
            int pct = Math.round(p * 100f);
            String teamName = (getTeamInControl() != null ? getTeamInControl() : "Unknown");
            bossbar.setName(new TextComponentString(teamName+ " is Capturing: " +pct + "%"));

            updateBossbarViewers();




        }

    }
    @Override
    public void invalidate() {
        if (world != null && !world.isRemote) {
            FlagRegistry.get(world).remove(pos);
        }
        super.invalidate();
    }
    @Override
    public void onChunkUnload() {
        if (world != null && !world.isRemote) {
            FlagRegistry.get(world).remove(pos);
        }
        super.onChunkUnload();
    }
    @Override
    public void onLoad() {
        super.onLoad();
        if (world != null && !world.isRemote) {
            double R  = configHandler.flagRadius;
            double Yh = configHandler.flagHeight;
            FlagRegistry reg = FlagRegistry.get(world);

            // If non-decorative, ensure it’s registered; if decorative, ensure it’s not
            if (kind == FlagKind.DECORATIVE) {
                reg.remove(pos);
            } else {
                reg.add(pos);
            }
        }
    }

    private void updateBossbarViewers() {
        if (world == null || world.isRemote) return;

        final double R   = configHandler.flagRadius;
        final double YH  = configHandler.flagHeight;
        final double R2  = R * R;
        final double cx  = pos.getX() + 0.5;
        final double cy  = pos.getY() + 0.5;
        final double cz  = pos.getZ() + 0.5;

        // prefilter box, then circle filter in XZ + vertical clamp
        AxisAlignedBB box = new AxisAlignedBB(pos).grow(R, YH, R);
        List<EntityPlayerMP> nearby = world.getEntitiesWithinAABB(EntityPlayerMP.class, box, p -> {
            if (p == null || p.isDead) return false;
            if (p.isSpectator()) return false;
            double dx = p.posX - cx;
            double dz = p.posZ - cz;
            double dy = Math.abs(p.posY - cy);
            return dy <= YH && (dx*dx + dz*dz) <= R2;
        });

        // Add missing
        for (EntityPlayerMP p : nearby) {
            if (!bossbar.getPlayers().contains(p)) {
                bossbar.addPlayer(p);
            }
        }

        // Remove those no longer in range / wrong dim
        for (EntityPlayerMP p : bossbar.getPlayers().toArray(new EntityPlayerMP[0])) {
            if (p == null || p.isDead || p.world != world) {
                bossbar.removePlayer(p);
                continue;
            }
            double dx = p.posX - cx;
            double dz = p.posZ - cz;
            double dy = Math.abs(p.posY - cy);
            if (dy > YH || (dx*dx + dz*dz) > R2) {
                bossbar.removePlayer(p);
            }
        }
    }
    public void endCaptureBossbar() {
        // Hide and clear viewers
        bossbar.setVisible(false);
        for (EntityPlayerMP p : bossbar.getPlayers().toArray(new EntityPlayerMP[0])) {
            bossbar.removePlayer(p);
        }
        // keep percent for a moment if you want, or reset:
        bossbar.setPercent(0.0f);
    }

    public void setCaptureProgress() {
        // safe setter your capture logic can call
        captureProgress = ((float) getPoints()/(float) configHandler.capMaxPoints);

    }

    public int getColor(){ return color; }

    public void setColor(int c){
        color = c;
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState s = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, s, s, 3);
        }
    }

    public String getOwnerTeamClient() {
        return ownerTeamClient;
    }

    public void clientApplyPacket(String ownerTeam) {
        // Map to your existing client fields as you wish; the key part:
        this.ownerTeamClient = (ownerTeam == null || ownerTeam.isEmpty()) ? "wilderness" : ownerTeam;


        if (world != null) world.markBlockRangeForRenderUpdate(pos, pos);
    }
    public long getPoints() { return points; }

    public String getTeamInControl() { return teamInControl; }


    public void setPoints(long v) {
        long clamped = Math.max(0L, Math.min(configHandler.capMaxPoints, v));
        if (clamped == this.points) return;
        this.points = clamped;
        markDirty();
        setCaptureProgress();
    }

    public void pushSync() {
        if (world != null && !world.isRemote) {
            IBlockState st = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, st, st, 3); // sends getUpdateTag()
            markDirty();
        }
    }

    public void setOwner(String newOwner) {
        newOwner = (newOwner == null || newOwner.isEmpty()) ? "wilderness" : newOwner;
        if (java.util.Objects.equals(teamInControl, newOwner)) return;

        teamInControl = newOwner;
        markDirty();

        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            // tell clients the TE NBT changed (even if blockstate didn't)
            world.notifyBlockUpdate(pos, state, state, 3);
            // ensure TESR / render refresh
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    // sync to client
    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt){
        super.writeToNBT(nbt);
        nbt.setInteger("Color", color);
        nbt.setString("Owner", teamInControl);
        nbt.setString("Kind", kind.name());
        if (binderTeamId != null) nbt.setString("BinderTeam", binderTeamId);
        return nbt;
    }
    @Override public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt);
        color = nbt.getInteger("Color");
        teamInControl = nbt.hasKey("Owner") ? nbt.getString("Owner") : "wilderness";
        try { this.kind = FlagKind.valueOf(nbt.getString("Kind")); }
        catch (Exception e) { this.kind = FlagKind.DECORATIVE; }
        this.binderTeamId = nbt.hasKey("BinderTeam") ? nbt.getString("BinderTeam").toLowerCase(Locale.ROOT) : null;
    }
    @Override public NBTTagCompound getUpdateTag(){ return writeToNBT(new NBTTagCompound()); }
    @Override public SPacketUpdateTileEntity getUpdatePacket(){ return new SPacketUpdateTileEntity(pos, 0, getUpdateTag()); }
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());

    }
    public enum FlagKind {
        EVENT,   // e.g., temporary event flags; may be neutral or special rules
        TEAM,    // normal kTeams capture/ownership behavior
        DECORATIVE; // purely cosmetic; no capture/ownership
    }
}
