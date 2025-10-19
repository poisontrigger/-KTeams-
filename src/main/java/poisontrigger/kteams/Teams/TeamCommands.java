package poisontrigger.kteams.Teams;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import poisontrigger.kteams.Blocks.Flag.TileEntityFlag;
import poisontrigger.kteams.Teams.commands.*;
import poisontrigger.kteams.util.Perms;

import poisontrigger.kteams.util.permUtil;

import java.util.*;



public class TeamCommands extends CommandBase {


    @Override
    public String getName() {return  "team";}

    @Override
    public String getUsage(ICommandSender sender){
        return "/team <create|fdelete|fadd|fremove|fclaim|unclaim|members|list>...";

    }
    @Override
    public java.util.List<String> getAliases() {
        return  java.util.Arrays.asList("t", "f", "faction", "teams", "factions");
    }

    @Override
    public int getRequiredPermissionLevel() {return 0;}

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) throw new WrongUsageException(getUsage(sender));

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sender.getEntityWorld().isRemote) {
            sender.sendMessage(new TextComponentString("Run this on the server side."));
            return;
        }
        // /teams [arg1] ... [arg2] ... [arg3]
        switch (sub) {
            case "create": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_CREATE, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                create.create(sender,args);
                return;
            }
            case "invite": {

                EntityPlayerMP p = getCommandSenderAsPlayer(sender);
                if (!permUtil.hasOrOp(sender, Perms.TEAM_INVITE, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                invite.invite(sender,args,server);
                return;
            }
            case "accept": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_ACCEPT, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                EntityPlayerMP p = getCommandSenderAsPlayer(sender);
                TeamData data = TeamData.get(p.world);
                if (!data.acceptInvite(p)) return;
                return;
            }
            case "deny": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_DENY, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                EntityPlayerMP p = getCommandSenderAsPlayer(sender);
                TeamData data = TeamData.get(p.world);
                if (!data.denyInvite(p)) return;
                return;
            }
            case "fdelete": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_FDELETE, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                if (args.length != 2) throw new WrongUsageException("/team fdelete <id>");
                {
                    TeamData data = TeamData.get(sender.getEntityWorld());
                    boolean ok = data.deleteTeam(args[1]);
                    if (ok) data.markDirty();
                    sender.sendMessage(new TextComponentString(ok ? "§7[kTeams] §cDeleted Team: "+ args[1].toLowerCase(Locale.ROOT)+"." : "§7[§rkTeams§7] §cNo such team ("+ args[1].toLowerCase(Locale.ROOT) +") found."));
                }
                return;
            }

            case "fadd": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_FADD, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                if (args.length != 3) throw new WrongUsageException("/team fadd <player> <teamId>");
                {
                    EntityPlayerMP target = getPlayer(server, sender, args[1]);
                    boolean ok = TeamAPI.addMember(sender.getEntityWorld(), args[2], target.getUniqueID());
                    sender.sendMessage(new TextComponentString(ok ? "§7[§rkTeams§7] §cForcefully added §e" + target.getName() + "§c to §e" + args[2] : "§7[§rkTeams§7] §cNo such team or already a member."));
                }
                return;
            }
            case "fremove": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_FREMOVE, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                if (args.length != 3) throw new WrongUsageException("/team fremove <player> <teamId>");
                {
                    EntityPlayerMP target = getPlayer(server, sender, args[1]);
                    EntityPlayerMP p = getPlayer(server, sender, args[1]);
                    TeamData data = TeamData.get(sender.getEntityWorld());
                    boolean ok = data.removeMember(args[2], p.getUniqueID());
                    if (ok) data.markDirty();
                    sender.sendMessage(new TextComponentString(ok ? "§7[§rkTeams§7] §cForcefully removed §e" + target.getName() + "§c from §e" + args[2] : "§7[§rkTeams§7] §cNo such team or already a member."));
                }
                return;
            }
            case "fcleardesc": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_F_CLEAR_DESC, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                TeamData data = TeamData.get(server.getWorld(0));
                TeamData.Team t = data.getAllTeamsView().get(args[1]);
                if (t == null) throw new CommandException("kteams.command.generic.noteam");
                t.setDescription("");
                sender.sendMessage(new TextComponentString("§7[§rkTeams§7] §cCleared the description of: §e" + t.id+"§c."));

            }
            case "fclaim": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_FCLAIM, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                if (args.length != 2) throw new WrongUsageException("/team fclaim <teamId>");
                {
                    if (!(sender instanceof EntityPlayerMP))
                        throw new CommandException("commands.generic.usage", getUsage(sender));
                    EntityPlayerMP p = (EntityPlayerMP) sender;
                    ChunkPos cp = new ChunkPos(p.getPosition());
                    TeamData data = TeamData.get(p.world);
                    TeamData.ClaimResult res = data.claimChunkChecked(args[1], cp, false, 100000000);
                    if (res == TeamData.ClaimResult.SUCCESS) data.markDirty();
                    sender.sendMessage(new TextComponentString(msgForClaimResult(res, data.getChunkOwner(cp))));
                }
                return;
            }
            case "leave": {
                if (!(sender instanceof EntityPlayerMP)) {
                    throw new CommandException("commands.generic.usage", getUsage(sender));
                }
                leave.leave(sender,args,server);
                return;
            }
            case "claim": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_FCLAIM, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                claim.claim(sender,args,server);
                return;
            }
            case "kick": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_UNCLAIM, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                kick.kick(sender,args,server);
                return;

            }
            case "promote": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_PROMOTE, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                elders.promote(sender,args,server);
                return;
            }
            case "demote": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_DEMOTE, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                elders.demote(sender,args,server);
                return;
            }

            case "unclaim": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_UNCLAIM, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                claim.unclaim(sender,args,server);
                return;
            }
            case "info": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_INFO, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                info.info(sender,args,server);
                return;
            }
            case "list": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_LIST, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                list.list(sender,args,server);
                return;

            }
            case "chat": {
                TeamChatEvents.handleCommand(sender, args);
                return;
            }
            case "bindflag": {
                if (args.length != 2 && args.length != 5) {
                    throw new WrongUsageException("/team bindFlag <event|team|decorative> [x y z]");
                }

                // Parse desired kind first
                TileEntityFlag.FlagKind targetKind = parseFlagKind(args[1]);

                // Permission: EVENT needs a different perm
                if (targetKind == TileEntityFlag.FlagKind.EVENT) {
                    if (!permUtil.hasOrOp(sender, Perms.TEAM_BINDFLAG_EVENT, 2, server)) {
                        throw new CommandException("kteams.command.no_permission");
                    }
                } else {
                    if (!permUtil.hasOrOp(sender, Perms.TEAM_BINDFLAG, 2, server)) {
                        throw new CommandException("kteams.command.no_permission");
                    }
                }
                bindFlag.bindFlag(sender,args,server,targetKind);
                return;
            }
            case "description": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_DESCRIPTION, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                description.description(sender, args);
                return;
            }
            case "home": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_HOME, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                home.home(sender);
                return;
            }
            case "sethome": {
                if (!permUtil.hasOrOp(sender, Perms.TEAM_SETHOME, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
               home.sethome(sender);
                return;
            }
            case "map": {

                if (!permUtil.hasOrOp(sender, Perms.TEAM_MAP, 2, server)) {
                    throw new CommandException("kteams.command.no_permission");
                }
                map.map(sender,args);
            }
            default:
                throw new WrongUsageException(getUsage(sender));
        }
    }

    public static String joinNice(String[] arr, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            if (i > start) sb.append(' ');
            sb.append(arr[i]);
        }
        return sb.toString();
    }


    // ---- Tab completion ----
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        // Helper to check a perm key (null = always allowed)
        java.util.function.Predicate<String> has = (permKey) -> {
            if (permKey == null) return true;
            return permUtil.hasOrOp(sender, permKey, 2, server);
        };

        // map the values to their perms, only show perms you have
        final Map<String, String> permMap = new java.util.HashMap<>();
        permMap.put("kick",        Perms.TEAM_KICK);
        permMap.put("create",      Perms.TEAM_CREATE);
        permMap.put("fdelete",     Perms.TEAM_FDELETE);
        permMap.put("fadd",        Perms.TEAM_FADD);
        permMap.put("fremove",     Perms.TEAM_FREMOVE);
        permMap.put("fclaim",      Perms.TEAM_FCLAIM);
        permMap.put("unclaim",     Perms.TEAM_UNCLAIM);
        permMap.put("info",        Perms.TEAM_INFO);
        permMap.put("list",        Perms.TEAM_LIST);
        permMap.put("map",         Perms.TEAM_MAP);
        permMap.put("invite",      Perms.TEAM_INVITE);
        permMap.put("accept",      Perms.TEAM_ACCEPT);
        permMap.put("deny",        Perms.TEAM_DENY);
        permMap.put("bindflag",    Perms.TEAM_BINDFLAG);
        permMap.put("description", Perms.TEAM_DESCRIPTION);
        permMap.put("sethome",     Perms.TEAM_SETHOME);
        permMap.put("home",        Perms.TEAM_HOME);
        permMap.put("promote",     Perms.TEAM_PROMOTE);
        permMap.put("demote",      Perms.TEAM_DEMOTE);
        permMap.put("leave",      Perms.TEAM_LEAVE);

        if (args.length == 1) {
            // Only include subcommands the user has permission to run
            List<String> all = java.util.Arrays.asList(
                    "kick","create","fdelete","fadd","fremove","fclaim","unclaim","info","list","map","invite","accept","deny",
                    "bindflag","description","sethome","home","promote","demote"
            );
            List<String> allowed = new java.util.ArrayList<>();
            for (String cmd : all) {
                String permKey = permMap.get(cmd); // may be null
                if (has.test(permKey)) allowed.add(cmd);
            }
            return getListOfStringsMatchingLastWord(args, allowed);
        }

        // only offer completions for subcommands the user can run
        String root = args[0].toLowerCase(Locale.ROOT);
        String neededPerm = permMap.get(root);
        if (!has.test(neededPerm)) return java.util.Collections.emptyList();

        TeamData data = sender.getEntityWorld().isRemote ? null : TeamData.get(sender.getEntityWorld());

        switch (root) {
            case "fdelete":
            case "fclaim":
            case "map":
            case "info": {
                if (args.length == 2 && data != null) {
                    return getListOfStringsMatchingLastWord(args, data.getAllTeamsView().keySet());
                }
                break;
            }

            case "bindflag": {
                // Only suggest kinds the user has permission for
                java.util.List<String> kinds = new java.util.ArrayList<>(3);
                if (has.test(Perms.TEAM_BINDFLAG)) {
                    kinds.add("team");
                    kinds.add("decorative");
                }
                if (has.test(Perms.TEAM_BINDFLAG_EVENT)) {
                    kinds.add("event");
                }
                if (args.length == 2) {
                    return getListOfStringsMatchingLastWord(args, kinds);
                } else if (args.length >= 3 && args.length <= 5) {
                    return getTabCompletionCoordinate(args, args.length - 1, pos);
                }
                break;
            }

            case "invite": {
                if (args.length == 2 && data != null) {
                    return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
                }
                break;
            }
            case "promote":
            case "demote":
            case "kick":
            case "fadd":
            case "fremove": {
                if (args.length == 2) {
                    return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
                } else if (args.length == 3 && data != null) {
                    return getListOfStringsMatchingLastWord(args, data.getAllTeamsView().keySet());
                }
                break;
            }
            // commands with no extra args: description/sethome/home/accept/deny/list/unclaim/create
            default:
                break;
        }

        return java.util.Collections.emptyList();
    }

    private static String msgForClaimResult(TeamData.ClaimResult r, String currentOwner) {
        switch (r) {
            case SUCCESS: return "§7[§rkTeams§7] §aChunk claimed.";
            case ALREADY_OWNED_BY_YOU: return "§7[§rkTeams§7] §cYour team already owns this chunk.";
            case ALREADY_OWNED_BY_OTHER: return "§7[§rkTeams§7] §cChunk already owned by §e" + currentOwner + "§c.";
            case STALE_TEAM: return "§7[§rkTeams§7] §cNo such team.";
            default: return r.toString();
        }
    }

    private TileEntityFlag.FlagKind parseFlagKind(String s) throws CommandException {
        String k = s.toLowerCase(Locale.ROOT);
        switch (k) {
            case "event": return TileEntityFlag.FlagKind.EVENT;
            case "team": return TileEntityFlag.FlagKind.TEAM;
            case "decorative": return TileEntityFlag.FlagKind.DECORATIVE;
            default: throw new WrongUsageException("Kind must be event|team|decorative");
        }
    }
    }



