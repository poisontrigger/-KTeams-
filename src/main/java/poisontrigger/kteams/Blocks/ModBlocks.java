package poisontrigger.kteams.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;
@Mod.EventBusSubscriber
public class ModBlocks {
    // Taken From HBM CE - Credit To Them For This Code : https://github.com/MisterNorwood/Hbm-s-Nuclear-Tech-CE/blob/master/src/main/java/com/hbm/blocks/ModBlocks.java
    public static List<Block> ALL_BLOCKS = new ArrayList<>();

    public static final Block k_flag = new BlockBase(Material.CLOTH,"k_flag");
    public static final BlockFlag FLAG = new BlockFlag("flag");

    public static void preInit(){
        for(Block block : ALL_BLOCKS){
            ForgeRegistries.BLOCKS.register(block);
            ForgeRegistries.ITEMS.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
            }

        }



    }



