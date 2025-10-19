package poisontrigger.kteams.Blocks.Flag;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import poisontrigger.kteams.Blocks.BlockFlag;
import poisontrigger.kteams.Teams.ClientTeam;

import java.util.Locale;


public class RenderFlag extends TileEntitySpecialRenderer<TileEntityFlag> {

    private static final ResourceLocation WHITE_TEX  = new ResourceLocation("kteams:textures/blocks/flag_white.png");
    private static final ResourceLocation TEX_SHIELD = new ResourceLocation("kteams:textures/misc/flag_shield.png");
    private static final ResourceLocation TEX_SWORD  = new ResourceLocation("kteams:textures/misc/flag_sword.png");

    // cloth/render constants
    private static final float GAP    = 0.0015f;
    private static final float THICK  = 0.0010f;
    private static final int   SEG    = 12;
    private static final float WIDTH  = 0.90f;
    private static final float HEIGHT = 0.70f;
    private static final float YTOP   = 1.45f;

    // pole east face
    private static final float POLE_FACE_X_EAST = (10f/16f) - 0.5f; // +0.125

    @Override
    @SideOnly(Side.CLIENT)
    public void render(TileEntityFlag te, double x, double y, double z, float pt, int ds, float alpha) {
        if (te == null || te.getWorld() == null) return;
        World w = te.getWorld();
        BlockPos bp = te.getPos();

        GlStateManager.pushMatrix();
        // block center
        GlStateManager.translate(x + 0.5, y - 0.6, z + 0.5);

        // rotate by facing once
        IBlockState st = w.getBlockState(bp);
        EnumFacing f = (st.getBlock() instanceof BlockFlag) ? st.getValue(BlockFlag.FACING) : EnumFacing.NORTH;
        GlStateManager.rotate(-f.getHorizontalAngle(), 0f, 1f, 0f);

        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // brighten so white isn't gray
        int pl = w.getCombinedLight(bp.up(2), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, pl & 0xFFFF, (pl >> 16) & 0xFFFF);

        // set dye color
        int c = te.getColor();
        GlStateManager.color(((c >> 16) & 255) / 255f, ((c >> 8) & 255) / 255f, (c & 255) / 255f, 1f);

        // white cloth tex
        bindTexture(WHITE_TEX);

        final float ATTACH_X = POLE_FACE_X_EAST + GAP;
        final float ZF = +THICK, ZB = -THICK;

        float t = (w.getTotalWorldTime() + pt) * 0.12f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // flag cloth segments
        for (int i = 0; i < SEG; i++) {
            float u0 = i / (float) SEG;
            float u1 = (i + 1) / (float) SEG;

            float x0 = ATTACH_X + u0 * WIDTH;
            float x1 = ATTACH_X + u1 * WIDTH;

            float wave0 = (float) Math.sin(t + u0 * WIDTH * 3.0f) * 0.06f;
            float wave1 = (float) Math.sin(t + u1 * WIDTH * 3.0f) * 0.06f;

            float droop0 = (u0 * WIDTH) * 0.05f;
            float droop1 = (u1 * WIDTH) * 0.05f;

            float y0t = YTOP - droop0 + wave0;
            float y1t = YTOP - droop1 + wave1;
            float y0b = (YTOP - HEIGHT) - droop0 + wave0 * 0.5f;
            float y1b = (YTOP - HEIGHT) - droop1 + wave1 * 0.5f;

            // FRONT (+Z)
            buf.pos(x0, y0b, ZF).tex(u0, 1).endVertex();
            buf.pos(x1, y1b, ZF).tex(u1, 1).endVertex();
            buf.pos(x1, y1t, ZF).tex(u1, 0).endVertex();
            buf.pos(x0, y0t, ZF).tex(u0, 0).endVertex();

            // BACK (-Z)
            buf.pos(x0, y0t, ZB).tex(u0, 0).endVertex();
            buf.pos(x1, y1t, ZB).tex(u1, 0).endVertex();
            buf.pos(x1, y1b, ZB).tex(u1, 1).endVertex();
            buf.pos(x0, y0b, ZB).tex(u0, 1).endVertex();
        }
        tess.draw();

        // reset color for icon
        GlStateManager.color(1f, 1f, 1f, 1f);

// ---------- SWORD/SHIELD OVERLAY (stuck to flag, centered) ----------
        String owner = safeLower(te.getOwnerTeamClient());
        String myTeam = safeLower(ClientTeam.get());
        boolean iOwnThis = java.util.Objects.equals(myTeam,owner);
        ResourceLocation icon = iOwnThis ? TEX_SHIELD : TEX_SWORD;
        this.bindTexture(icon);

// center of the cloth in LOCAL space (u = 0.5)
        final float uC = 0.5f;
        final float xC = ATTACH_X + uC * WIDTH;

// sample the same wave/droop you use for the cloth so the icon rides with it
        float tNow   = (w.getTotalWorldTime() + pt) * 0.12f;
        float waveC  = (float) Math.sin(tNow + uC * WIDTH * 3.0f) * 0.06f;
        float droopC = (uC * WIDTH) * 0.05f;
        float yTopC  = YTOP - droopC + waveC;
        float yBotC  = (YTOP - HEIGHT) - droopC + waveC * 0.5f;
        float yC     = 0.5f * (yTopC + yBotC);

// pick a size relative to the cloth
        float iconW = WIDTH * 0.35f;                       // width along +X
        float iconH = Math.min(HEIGHT * 0.6f, iconW * 1.0f); // keep square-ish

// draw a quad in the cloth's plane (front face Z=+THICK)
        final float zFront = ZF + 0.0008f; // tiny offset to avoid z-fighting

        Tessellator ites = Tessellator.getInstance();
        BufferBuilder ibuf = ites.getBuffer();
        ibuf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

// FRONT (same plane as the cloth front)
        ibuf.pos(xC - iconW * 0.5f, yC - iconH * 0.5f, zFront).tex(0, 1).endVertex();
        ibuf.pos(xC + iconW * 0.5f, yC - iconH * 0.5f, zFront).tex(1, 1).endVertex();
        ibuf.pos(xC + iconW * 0.5f, yC + iconH * 0.5f, zFront).tex(1, 0).endVertex();
        ibuf.pos(xC - iconW * 0.5f, yC + iconH * 0.5f, zFront).tex(0, 0).endVertex();

        ites.draw();

// OPTIONAL: back face so it shows from both sides of the flag
        final float zBack = ZB - 0.0008f;
        ibuf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        ibuf.pos(xC - iconW * 0.5f, yC + iconH * 0.5f, zBack).tex(0, 0).endVertex();
        ibuf.pos(xC + iconW * 0.5f, yC + iconH * 0.5f, zBack).tex(1, 0).endVertex();
        ibuf.pos(xC + iconW * 0.5f, yC - iconH * 0.5f, zBack).tex(1, 1).endVertex();
        ibuf.pos(xC - iconW * 0.5f, yC - iconH * 0.5f, zBack).tex(0, 1).endVertex();
        ites.draw();
// -----------------------------------------

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }


    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }


}