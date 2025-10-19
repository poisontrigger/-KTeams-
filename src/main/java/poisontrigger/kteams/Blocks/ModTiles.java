package poisontrigger.kteams.Blocks;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.util.ResourceLocation;
import poisontrigger.kteams.Blocks.Flag.TileEntityFlag; // use your actual package
import poisontrigger.kteams.Kteams;

public final class ModTiles {
    private ModTiles() {}

    public static void register() {

        GameRegistry.registerTileEntity(
                TileEntityFlag.class,
                new ResourceLocation(Kteams.MOD_ID, "flag") // "kteams:flag"
        );
    }
}
