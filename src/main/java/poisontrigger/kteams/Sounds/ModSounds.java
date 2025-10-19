package poisontrigger.kteams.Sounds;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = "kteams")
public final class ModSounds {
    public static SoundEvent FLAG_CAPTURE;
    public static SoundEvent FLAG_CAPTURE_COMPLETE;

    @SubscribeEvent
    public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> e) {
        FLAG_CAPTURE = register(e, "flag.flag_capture");
        FLAG_CAPTURE_COMPLETE = register(e, "flag.flag_capture_complete");
    }

    private static SoundEvent register(RegistryEvent.Register<SoundEvent> e, String path) {
        ResourceLocation id = new ResourceLocation("kteams", path);
        SoundEvent ev = new SoundEvent(id);
        ev.setRegistryName(id);
        e.getRegistry().register(ev);
        return ev;
    }
}