package poisontrigger.kteams;



import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import poisontrigger.kteams.util.LogHandler;

import java.util.UUID;


public final class ChatCommandLogger {



    @SubscribeEvent(receiveCanceled = true)
    public void onChat(ServerChatEvent e) {
        // Should fire for normal chat (no slash)
        EntityPlayerMP p = e.getPlayer();


        LogHandler.get().log("[CHAT] "+e.getMessage(), p.getUniqueID(), p.getName(), "CHAT.MESSAGE");
    }
    @SubscribeEvent(receiveCanceled = true)
    public void onCommand(CommandEvent e) {
        // Should fire for commands (messages starting with / handled server-side)
        try {
            ICommandSender s = e.getSender();
            String full = "[COMMAND] /" + e.getCommand().getName();
            if (e.getParameters() != null && e.getParameters().length > 0) {
                full += " " + String.join(" ", e.getParameters());
            }



            if (s instanceof EntityPlayerMP) {
                EntityPlayerMP p = (EntityPlayerMP) s;
                LogHandler.get().log(full, p.getUniqueID(), p.getName(), "CMD.EXEC");
            } else {
                String name = (s == null) ? "unknown" : s.getName();
                LogHandler.get().log(full, null, name, "CMD.EXEC");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
