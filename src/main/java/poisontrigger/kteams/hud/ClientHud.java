package poisontrigger.kteams.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import poisontrigger.kteams.Kteams;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Kteams.MOD_ID)
public final class ClientHud {
    private static String current = "Wilderness";
    private static long lastUpdateMs = 0L;

    public static void setTerritory(String text){
        current = (text == null || text.isEmpty()) ? "Wilderness" : text;
        lastUpdateMs = System.currentTimeMillis();
    }
    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Text e) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;

        String s = current;

        ScaledResolution sr = new ScaledResolution(mc);
        int pad = 4;
        int w = mc.fontRenderer.getStringWidth(s);
        int x = sr.getScaledWidth() - w - pad;
        int y = pad;

        mc.fontRenderer.drawStringWithShadow(s, x, y, 0xFFFFFF);
    }

}
