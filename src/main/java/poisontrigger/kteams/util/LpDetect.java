package poisontrigger.kteams.util;

public class LpDetect {

    public static boolean hasLuckPerms() {
        try {
            // org.bukkit.Bukkit class present?
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object server = bukkitClass.getMethod("getServer").invoke(null);

            // getPluginManager()
            Object pluginManager = server.getClass()
                    .getMethod("getPluginManager")
                    .invoke(server);

            // call pluginManager.getPlugin("LuckPerms")
            Object plugin = pluginManager.getClass()
                    .getMethod("getPlugin", String.class)
                    .invoke(pluginManager, "LuckPerms");

            if (plugin == null) return false;

            // call plugin.isEnabled()
            Boolean enabled = (Boolean) plugin.getClass()
                    .getMethod("isEnabled")
                    .invoke(plugin);

            return enabled != null && enabled;
        } catch (ClassNotFoundException e) {
            // No Bukkit at all (pure Forge)
            return false;
        } catch (Throwable t) {
            // Something went wrong, be safe and say "no LuckPerms"
            return false;
        }
    }
}
