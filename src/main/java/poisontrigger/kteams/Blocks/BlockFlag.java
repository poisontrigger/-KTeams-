package poisontrigger.kteams.Blocks;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import poisontrigger.kteams.Blocks.Flag.FlagRegistry;
import poisontrigger.kteams.Blocks.Flag.TileEntityFlag;
import poisontrigger.kteams.Kteams;

import java.util.Locale;


public class BlockFlag extends Block {
    public static final AxisAlignedBB AABB = new AxisAlignedBB(0.375, 0, 0.375, 0.625, 1, 0.625);

    public enum Part implements IStringSerializable {
        BASE, MIDDLE, TOP;
        @Override public String getName(){ return name().toLowerCase(Locale.ROOT); }
    }


    public static final PropertyEnum<Part> PART = PropertyEnum.create("part", Part.class);
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockFlag(String name) {
        super(Material.IRON);
        setHardness(1.5F);
        setSoundType(SoundType.METAL);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState()
                .withProperty(PART, Part.BASE)
                .withProperty(FACING, EnumFacing.NORTH));
        setRegistryName(Kteams.MOD_ID, name);
        setTranslationKey(Kteams.MOD_ID + "." + getRegistryName().getPath());
        ModBlocks.ALL_BLOCKS.add(this);
    }



    // ---- rendering ----
    @Override public boolean isOpaqueCube(IBlockState s){ return false; }
    @Override public boolean isFullCube(IBlockState s){ return false; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState s, IBlockAccess w, BlockPos p){ return AABB; }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }

    // Only the TOP has a TE so the flag cloth renders once
    @Override public boolean hasTileEntity(IBlockState state) {
        return state.getValue(PART) == Part.TOP;
    }
    @Override public TileEntity createTileEntity(World world, IBlockState state) {
        return state.getValue(PART) == Part.TOP ? new TileEntityFlag() : null;
    }

    private boolean canReplace(World w, BlockPos p) {
        IBlockState s = w.getBlockState(p);
        return w.isAirBlock(p) || s.getBlock().isReplaceable(w, p) || s.getMaterial().isReplaceable();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        EnumFacing horiz = placer.getHorizontalFacing();
        return getDefaultState().withProperty(PART, Part.BASE).withProperty(FACING, horiz);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        if (world.isRemote) return;



        EnumFacing horiz = placer.getHorizontalFacing(); // keep consistent with getStateForPlacement
        BlockPos p0 = pos, p1 = pos.up(), p2 = pos.up(2); // TE lives at TOP (p2)

        // --- height / replace checks for 3-block tall structure ---
        if (p2.getY() >= world.getHeight() || !canReplace(world, p1) || !canReplace(world, p2)) {
            // can’t place the stack of blocks == cancel & refund
            world.destroyBlock(p0, false);
            if (placer instanceof EntityPlayer && !((EntityPlayer) placer).capabilities.isCreativeMode) {
                ItemStack back = new ItemStack(Item.getItemFromBlock(this));
                if (!((EntityPlayer) placer).inventory.addItemStackToInventory(back)) {
                    placer.entityDropItem(back, 0f);
                }
            }
            return;
        }


        // --- actually place the 3 parts ---
        world.setBlockState(p0, getDefaultState().withProperty(PART, Part.BASE).withProperty(FACING, horiz),   2);
        world.setBlockState(p1, getDefaultState().withProperty(PART, Part.MIDDLE).withProperty(FACING, horiz), 2);
        world.setBlockState(p2, getDefaultState().withProperty(PART, Part.TOP).withProperty(FACING, horiz),    2);

        // --- apply color from stack NBT to the TE at the TOP block ---
        TileEntity te = world.getTileEntity(p2);
        if (te instanceof TileEntityFlag && stack.hasTagCompound() && stack.getTagCompound().hasKey("Color")) {
            ((TileEntityFlag) te).setColor(stack.getTagCompound().getInteger("Color"));
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand, EnumFacing face,
                                    float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty() || !(held.getItem() instanceof ItemDye)) return false;
        if (world.isRemote) return true;

        // find the TOP tile entity no matter which part you clicked
        Part part = state.getValue(PART);
        BlockPos base = (part == Part.BASE) ? pos : (part == Part.MIDDLE ? pos.down() : pos.down(2));
        BlockPos topPos = base.up(2);
        TileEntity te = world.getTileEntity(topPos);
        if (!(te instanceof TileEntityFlag)) return false;

        // vanilla dye colors Thanks chat gpt!
        final int[] PALETTE = new int[]{
                0x000000, // black
                0xB02E26, // red
                0x5E7C16, // green
                0x835432, // brown
                0x3C44AA, // blue
                0x8932B8, // purple
                0x169C9C, // cyan
                0x9D9D97, // light gray
                0x474F52, // gray
                0xF38BAA, // pink
                0x80C71F, // lime
                0xFED83D, // yellow
                0x3AB3DA, // light blue
                0xC74EBD, // magenta
                0xF9801D, // orange
                0xFFFFFF  // white
        };
        int meta = held.getMetadata();
        int rgb = PALETTE[Math.max(0, Math.min(15, meta))];

        ((TileEntityFlag) te).setColor(rgb);

        if (!player.capabilities.isCreativeMode) held.shrink(1);
        return true;
    }


    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (world.isRemote) return;
        BlockPos base = findBase(world, pos, state.getValue(PART));
        for (int i = 2; i >= 0; i--) {
            BlockPos p = base.up(i);
            if (world.getBlockState(p).getBlock() == this) world.destroyBlock(p, false);
        }
        if (!player.capabilities.isCreativeMode) {
            spawnAsEntity(world, base, new ItemStack(Item.getItemFromBlock(this)));
        }
    }

    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!world.isRemote) world.scheduleUpdate(pos, this, 1); // check next tick
    }


    private BlockPos findBase(World w, BlockPos pos, Part part) {
        if (part == Part.BASE) return pos;
        if (part == Part.MIDDLE) return pos.down();
        return pos.down(2); // TOP
    }
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) { super.breakBlock(world, pos, state); return; }

        // If some other part breaks (piston/explosion), quietly remove the rest without drops.
        BlockPos base = findBase(world, pos, state.getValue(PART));
        for (int i = 0; i < 3; i++) {
            BlockPos p = base.up(i);
            if (!p.equals(pos) && world.getBlockState(p).getBlock() == this) {
                world.destroyBlock(p, false);
            }
        }
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityFlag) {
            ((TileEntityFlag) te).endCaptureBossbar();     // <-- hide bossbar
        }
        FlagRegistry.get(world).remove(pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int part = state.getValue(PART).ordinal() & 0b11;
        int face = state.getValue(FACING).getHorizontalIndex() & 0b11;
        return (face << 2) | part;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int part = meta & 0b11;
        int face = (meta >> 2) & 0b11;
        return getDefaultState()
                .withProperty(PART, Part.values()[MathHelper.clamp(part, 0, 2)])
                .withProperty(FACING, EnumFacing.byHorizontalIndex(face));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PART, FACING);
    }

}
