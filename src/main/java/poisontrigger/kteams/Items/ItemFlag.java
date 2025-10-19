package poisontrigger.kteams.Items;


import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class ItemFlag extends ItemBlock {
    public ItemFlag(Block block) {
        super(block);
        setRegistryName("kteams", "flag");
        setTranslationKey("kteams.flag");
        this.setMaxStackSize(16);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (facing != EnumFacing.UP) return EnumActionResult.FAIL;

        BlockPos place = pos.up();
        BlockPos placeTop = place.up();

        if (!player.canPlayerEdit(place, facing, player.getHeldItem(hand))) return EnumActionResult.FAIL;
        if (!world.isAirBlock(place) || !world.isAirBlock(placeTop)) return EnumActionResult.FAIL;

        // Let the block class handle state & TE. just call the vanilla place flow.
        EnumActionResult res = super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        return res;
    }
}