package poisontrigger.kteams.Teams;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class TeamMap extends Gui {
    // data is row-major: index = (dz+radius)*(2*radius+1) + (dx+radius)
    private static byte[] cells;
    private static int radius;
    private static long expiresMs;

    private static java.util.List<Character> legendSyms = java.util.Collections.emptyList();
    private static java.util.List<String>    legendNames = java.util.Collections.emptyList();
    private static java.util.List<Integer>   legendColors = java.util.Collections.emptyList();

    // call from client packet handler
    public static void show(poisontrigger.kteams.network.Net.MapPacket msg) {
        cells = msg.cells;
        radius = msg.radius;
        expiresMs = System.currentTimeMillis() + msg.seconds * 1000L;
        legendSyms = (msg.legendSyms != null) ? msg.legendSyms : java.util.Collections.emptyList();
        legendNames = (msg.legendNames != null) ? msg.legendNames : java.util.Collections.emptyList();
        legendColors = (msg.legendColors != null) ? msg.legendColors : java.util.Collections.emptyList();
        // quick debug:
        System.out.println("[KTeams] HUD map: cells=" + cells.length + " legend=" + legendNames.size());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRender(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (cells == null) return;
        if (System.currentTimeMillis() > expiresMs) { cells = null; return; }

        final Minecraft mc = Minecraft.getMinecraft();
        final net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(mc);

        // ---- layout ----
        final int cell = 10;            // bigger cell so symbols fit; 8–10 is good
        final int gap  = 1;
        final int size = cell + gap;
        final int dim  = 2 * radius + 1;
        final int w    = dim * size - gap;
        final int h    = dim * size - gap;
        final int x0   = 12;             // where to start drawing the map
        final int y0   = 12;

        // map background
        drawRect(x0 - 3, y0 - 3, x0 + w + 3, y0 + h + 3, 0x80000000);

        // centering
        final int fh = mc.fontRenderer.FONT_HEIGHT;          // ~9
        final int vPad = Math.max(0, (cell - fh) / 2);       // vertical padding to center

        // draw cells
        int idx = 0;
        for (int zi = 0; zi < dim; zi++) {
            for (int xi = 0; xi < dim; xi++) {
                final byte code = cells[idx++];              // 0 = wilderness, else 1..N
                final int x = x0 + xi * size;
                final int y = y0 + zi * size;

                if (code == 0) {
                    // Wilderness: background, no symbol
                    drawRect(x, y, x + cell, y + cell, 0x80222222);
                    continue;
                }

                final int li = code - 1; // legend index
                if (li < 0 || li >= legendColors.size() || li >= legendSyms.size()) {
                    // defensive fallback (packet out of sync)
                    drawRect(x, y, x + cell, y + cell, 0x80FF00FF);
                    continue;
                }

                // Fill with team color semi-opaque
                final int teamARGB = legendColors.get(li);
                final int fillARGB = (teamARGB & 0x00FFFFFF) | 0xB0000000;
                drawRect(x, y, x + cell, y + cell, fillARGB);

                //centered symbol - ish
                final String s = String.valueOf(legendSyms.get(li));
                final int sw = mc.fontRenderer.getStringWidth(s);

                // left pad so text center aligns to cell center
                final int hPad = Math.max(0, (cell - sw) / 2);

                mc.fontRenderer.drawString(s, x + hPad, y + vPad, 0xFFFFFFFF, false);
            }
        }

        // center crosshair
        final int cx = x0 + radius * size, cy = y0 + radius * size;
        drawRect(cx, cy + cell / 2 - 1, cx + cell, cy + cell / 2 + 1, 0xFF0099FF);
        drawRect(cx + cell / 2 - 1, cy, cx + cell / 2 + 1, cy + cell, 0xFF0099FF);

        // north label
        mc.fontRenderer.drawStringWithShadow("N", x0 + w / 2f - 2, y0 - 10, 0xFFFFFF);

        // legend (to the right; clamped inside screen)
        final int boxW = 160;
        final int boxH = 12 + 10 /*wild*/ + legendNames.size() * 10 + 6;
        final int lx = Math.min(x0 + w + 10, sr.getScaledWidth() - boxW - 6);
        int ly = y0;

        drawRect(lx - 3, ly - 3, lx + boxW, ly + boxH, 0x80000000);
        mc.fontRenderer.drawStringWithShadow("Legend", lx, ly, 0xFFFFFFFF);
        ly += 12;

        mc.fontRenderer.drawStringWithShadow("■ Wilderness", lx, ly, 0xFFBBBBBB);
        ly += 10;

        for (int i = 0; i < legendNames.size(); i++) {
            final char sym = legendSyms.get(i);
            final String name = legendNames.get(i);
            final int col = legendColors.get(i);

            // draw symbol (colored) + name (white)
            mc.fontRenderer.drawString(String.valueOf(sym), lx, ly, col, false);
            mc.fontRenderer.drawStringWithShadow(name, lx + 12, ly, 0xFFFFFFFF);
            ly += 10;
        }
    }


}
