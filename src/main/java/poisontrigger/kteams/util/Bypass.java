package poisontrigger.kteams.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public final class Bypass {
    public Bypass() {}

    // Returns true if the player is OP (permission level ≥ requiredLevel).
    public static boolean isOpBypass(EntityPlayer player, int requiredLevel) {
        if (!(player instanceof EntityPlayerMP)) return false;
        return player.canUseCommand(requiredLevel, "kteams.bypass");
    }

    // Convenience default (level 2 is typical “command” OP)
    public static boolean isOpBypass(EntityPlayer player) {
        return isOpBypass(player, 2);
    }
}
