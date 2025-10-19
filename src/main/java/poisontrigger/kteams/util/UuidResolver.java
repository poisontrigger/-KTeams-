package poisontrigger.kteams.util;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class UuidResolver {
    private UuidResolver() {}


    public static UUID resolve(MinecraftServer server, String name) {
        if (server == null || name == null || name.isEmpty()) return null;

        // If they're online
        EntityPlayerMP online = server.getPlayerList().getPlayerByUsername(name);
        if (online != null) return online.getUniqueID();

        //  Try local cache
        if (server.getPlayerProfileCache() != null) {
            GameProfile cached = server.getPlayerProfileCache().getGameProfileForUsername(name);
            if (cached != null && cached.getId() != null) return cached.getId();
        }



        return null;
    }
    public static String nameForUuid(MinecraftServer server, UUID uuid, boolean allowMojangLookup, String fallback) {
        if (server == null || uuid == null) return fallback;


        EntityPlayerMP online = server.getPlayerList().getPlayerByUUID(uuid);
        if (online != null) return online.getName();

        if (server.getPlayerProfileCache() != null) {
            GameProfile cached = server.getPlayerProfileCache().getProfileByUUID(uuid);
            if (cached != null && cached.getName() != null && !cached.getName().isEmpty()) {
                return cached.getName();
            }
        }


        return fallback;
    }


}
