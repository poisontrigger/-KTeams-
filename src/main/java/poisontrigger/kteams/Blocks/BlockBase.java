package poisontrigger.kteams.Blocks;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import poisontrigger.kteams.Kteams;

public class BlockBase extends Block {
// Taken From HBM CE - Credit To Them For This Code : https://github.com/MisterNorwood/Hbm-s-Nuclear-Tech-CE/blob/master/src/main/java/com/hbm/blocks/BlockBase.java
        public BlockBase(Material m, String s) {
            super(m);
            this.setTranslationKey(Kteams.MOD_ID+"." + s);
            this.setRegistryName(Kteams.MOD_ID,s);
            this.setHarvestLevel("pickaxe", 0);
            this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
            ModBlocks.ALL_BLOCKS.add(this);
        }
}
