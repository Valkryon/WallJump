package me.arthed.walljump.listeners;

import me.arthed.walljump.WallJump;
import me.arthed.walljump.player.PlayerManager;
import me.arthed.walljump.player.WPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final PlayerManager playerManager = WallJump.getInstance().getPlayerManager();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerManager.unregisterPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        WPlayer wplayer = playerManager.getWPlayer(event.getEntity());
        if(wplayer != null)
            wplayer.stopWallJumping();
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        WPlayer wplayer = playerManager.getWPlayer(event.getPlayer());
        if(wplayer != null)
            wplayer.stopWallJumping();
    }

}
