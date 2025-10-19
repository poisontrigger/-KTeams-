package poisontrigger.kteams.Teams;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientTeam {
    private ClientTeam() {}
    private static String current = "wilderness";
    private static String currentO = current;
    public static String get() { return current; }
    public static void set(String id) {
        current = (id == null ? "wilderness" : id.toLowerCase(java.util.Locale.ROOT));
    }
    public static void setOwner(String id){
        currentO = (id == null ? "wilderness" : id.toLowerCase(java.util.Locale.ROOT));
    }
    public static String getOwner() {
        return currentO;
    }
}
