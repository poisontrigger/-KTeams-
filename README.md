Made by Poisontrigger

Docs:
  Commands:

  /t | /team | /f | /faction: Main command following commands are sub-commands


   FORMAT: () = Mandatory <> = Optional: 

   GENERAL
  /t create (name) - Creates a team and automatically sets you as the owner of the team.
  
  /t claim <radius> - Claims all chunks in the specified radius if they are not owned.
  
  /t unclaim - unclaims the chunk you are standing in.
  
  /t invite (player) - Invites an ONLINE player to your team if they have no team.
  
  /t accept | /t deny - Accept or Deny an invite to a team. You can also click on the chat message to run these commands.
  
  /t leave - Leave a team, only possible if you are not the owner; if you are the owner, you must either transfer ownership or kick everyone else before leaving. If you kick everyone else, the team is disbanded.
  
  /t description - Sets the team's description.
  
  /t info <team name> - Displays info about a team, including: Description, Active Players and Ranks. If no team is provided, it displays info on your current team, if applicable.
  
  /t chat <message> - Toggles the team chat (private chat amongst team members) or sends a message to the team if a message is provided.
  
  /t map <radius> - Shows a map on the HUD of claims within a radius (default if no radius provided = 4).
  
  /t home - Teleports to the location specified in /t sethome in a time specified, can be interrupted by moving too much or taking damage.
  
  /t list - Lists all the teams on the server, each team is clickable, where if you click on the chat message, it displays their /t info.
  

   TEAM LEADERSHIP: Commands only executable by ranked team players (Elders or Owners*)
   
  /t promote (player)* - Promotes a player to an Elder (Team Moderator).
  
  /t demote (player)* - Demotes an elder to a Member (Default rank).
  
  /t kick (player) - Kicks a player from the team (Elders cannot kick each other, or the owner).
  
  /t sethome - Sets the /t home location.
  
  /t bindflag (TEAM | DECORATIVE) <Location> - Sets the flag at a location to be Capturable or Decorative. If Capturable, only captureable by other teams.
  

  SERVER MODERATION BYPASS COMMANDS: Commands that bypass various checks, only to be used by server staff.
  
  /t fadd (player) (team name) - Forcefully adds a player to a team.
  
  /t fremove (player) (team name) - Forcefully removes a player from a team.
  
  /t fclaim (team name) - Forcefully claims the chunk you are standing in for a team.
  
  /t bindFlag (EVENT) - Sets a flag to an Event Flag (Capturable by Any Team).
  
  /t cleardesc (team name) - Clears a team's description.
  
  /t fdelete (team name) - Deletes a team.


  Permissions:

  kteams.*

kteams.team.create

kteams.team.owner

kteams.team.fadd

kteams.team.fremove

kteams.team.unclaim

kteams.team.list

kteams.team.map

kteams.team.info

kteams.team.fdelete

kteams.team.fclaim

kteams.team.deny 

kteams.team.invite 

kteams.team.accept 

kteams.team.promote

kteams.team.demote

kteams.team.kick

kteams.team.bindflag

kteams.team.bindflag.event

kteams.team.description

kteams.team.sethome

kteams.team.home

kteams.team.leave

kteams.team.fcleardesc
  
