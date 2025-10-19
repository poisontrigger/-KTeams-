package poisontrigger.kteams.util;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class Perms {
    public static final String ROOT = "kteams";
    public static final String ADMIN_ALL     = ROOT + ".*";
    public static final String TEAM_CREATE   = ROOT + ".team.create";
    public static final String TEAM_OWNER   = ROOT + ".team.owner";
    public static final String TEAM_FADD   = ROOT + ".team.fadd";
    public static final String TEAM_FREMOVE   = ROOT + ".team.fremove";
    public static final String TEAM_UNCLAIM   = ROOT + ".team.unclaim";
    public static final String TEAM_LIST   = ROOT + ".team.list";
    public static final String TEAM_MAP   = ROOT + ".team.map";
    public static final String TEAM_INFO   = ROOT + ".team.info";
    public static final String TEAM_FDELETE   = ROOT + ".team.fdelete";
    public static final String TEAM_FCLAIM   = ROOT + ".team.fclaim";
    public static final String TEAM_INVITE   = ROOT + ".team.fclaim";
    public static final String TEAM_ACCEPT   = ROOT + ".team.fclaim";
    public static final String TEAM_DENY   = ROOT + ".team.fclaim";
    public static final String TEAM_PROMOTE = ROOT + ".team.promote";
    public static final String TEAM_DEMOTE = ROOT + ".team.demote";
    public static final String TEAM_KICK = ROOT + ".team.kick";
    public static final String TEAM_BINDFLAG = ROOT + ".team.bindflag";
    public static final String TEAM_BINDFLAG_EVENT = ROOT + ".team.bindflag.event";
    public static final String TEAM_DESCRIPTION = ROOT + ".team.description";
    public static final String TEAM_SETHOME = ROOT + ".team.sethome";
    public static final String TEAM_HOME = ROOT + ".team.home";
    public static final String TEAM_LEAVE = ROOT + ".team.leave";
    public static final String TEAM_F_CLEAR_DESC = ROOT + ".team.fcleardesc";



    public static void registerNodes() {

        PermissionAPI.registerNode(ADMIN_ALL,     DefaultPermissionLevel.OP,  "Allows all KTeams admin actions."); // ADMIN
        PermissionAPI.registerNode(TEAM_CREATE,   DefaultPermissionLevel.ALL, "Allow creating teams.");
        PermissionAPI.registerNode(TEAM_OWNER,   DefaultPermissionLevel.ALL, "Allow checking which team owns the chunk you are in.");
        PermissionAPI.registerNode(TEAM_FADD,   DefaultPermissionLevel.OP, "Force a player onto a team."); // ADMIN
        PermissionAPI.registerNode(TEAM_FREMOVE,   DefaultPermissionLevel.OP, "Force a player off of a team."); // ADMIN
        PermissionAPI.registerNode(TEAM_UNCLAIM,   DefaultPermissionLevel.ALL, "Unclaim some land");
        PermissionAPI.registerNode(TEAM_LIST,   DefaultPermissionLevel.ALL, "List all teams");
        PermissionAPI.registerNode(TEAM_MAP,   DefaultPermissionLevel.ALL, "Shows the player a map");
        PermissionAPI.registerNode(TEAM_INFO,   DefaultPermissionLevel.ALL, "List the members on a team.");
        PermissionAPI.registerNode(TEAM_FCLAIM,   DefaultPermissionLevel.OP, "Force a team to claim a chunk."); // ADMIN
        PermissionAPI.registerNode(TEAM_FDELETE,   DefaultPermissionLevel.OP, "Force a team to be deleted."); // ADMIN
        PermissionAPI.registerNode(TEAM_INVITE,   DefaultPermissionLevel.ALL, "Invite a player to a team.");
        PermissionAPI.registerNode(TEAM_ACCEPT,   DefaultPermissionLevel.ALL, "Accept a team invitation.");
        PermissionAPI.registerNode(TEAM_DENY,   DefaultPermissionLevel.ALL, "Reject a team invitation.");
        PermissionAPI.registerNode(TEAM_PROMOTE, DefaultPermissionLevel.ALL, "Promote a player in a team.");
        PermissionAPI.registerNode(TEAM_DEMOTE, DefaultPermissionLevel.ALL, "Demote a player in a team.");
        PermissionAPI.registerNode(TEAM_KICK, DefaultPermissionLevel.ALL, "Kick a player from a team.");
        PermissionAPI.registerNode(TEAM_BINDFLAG, DefaultPermissionLevel.ALL, "Bind a flag");
        PermissionAPI.registerNode(TEAM_BINDFLAG_EVENT, DefaultPermissionLevel.OP, "Bind a flag to type: Event"); // ADMIN
        PermissionAPI.registerNode(TEAM_DESCRIPTION, DefaultPermissionLevel.ALL, "Sets your team's description");
        PermissionAPI.registerNode(TEAM_SETHOME, DefaultPermissionLevel.ALL, "Set your team's home");
        PermissionAPI.registerNode(TEAM_HOME, DefaultPermissionLevel.ALL, "Teleport to your team's home.");
        PermissionAPI.registerNode(TEAM_LEAVE, DefaultPermissionLevel.ALL, "leave your current team");
        PermissionAPI.registerNode(TEAM_F_CLEAR_DESC, DefaultPermissionLevel.OP, "Force clears a team's description"); // ADMIN
    }
    public Perms(){}
}
