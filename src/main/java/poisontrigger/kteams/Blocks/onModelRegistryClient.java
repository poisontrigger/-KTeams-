package poisontrigger.kteams.Blocks;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import poisontrigger.kteams.Kteams;

@Mod.EventBusSubscriber(modid = Kteams.MOD_ID, value = Side.CLIENT)
public class onModelRegistryClient {

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent e){

        System.out.println("FLAG block = " + ModBlocks.FLAG);
        System.out.println("FLAG regname = " + ModBlocks.FLAG.getRegistryName());
        System.out.println("FLAG item = " + Item.getItemFromBlock(ModBlocks.FLAG));




        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(ModBlocks.k_flag),0,
                new ModelResourceLocation(ModBlocks.k_flag.getRegistryName(),"normal")
        );
        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(ModBlocks.FLAG),
                0,
                new ModelResourceLocation(ModBlocks.FLAG.getRegistryName(), "inventory"));
    }
}
