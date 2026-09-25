package me.arthed.walljump.handlers;

import me.arthed.walljump.WallJump;
import me.arthed.walljump.player.PlayerManager;
import me.arthed.walljump.player.WPlayer;
import me.arthed.walljump.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class OtherPluginsHandler implements Listener {

    private final PlayerManager playerManager;

    public OtherPluginsHandler() {
        WallJump wallJump = WallJump.getInstance();
        playerManager = wallJump.getPlayerManager();

        Plugin treysDoubleJump = Bukkit.getPluginManager().getPlugin("TreysDoubleJump");
        if(treysDoubleJump != null) {
            try {
                // registered through reflection so TreysDoubleJump isn't needed to compile or run WallJump
                Class<? extends Event> groundPoundEvent = Class.forName(
                        "me.treyruffy.treysdoublejump.api.GroundPoundEvent",
                        true,
                        treysDoubleJump.getClass().getClassLoader()
                ).asSubclass(Event.class);
                Bukkit.getPluginManager().registerEvent(groundPoundEvent, this, EventPriority.NORMAL,
                        (listener, event) -> onTreysDoubleJumpGroundPound(event), wallJump);
            } catch(ReflectiveOperationException | ClassCastException | LinkageError e) {
                wallJump.getLogger().log(Level.WARNING, "Failed to hook into TreysDoubleJump", e);
            }
        }
    }

    private void onTreysDoubleJumpGroundPound(Event event) {
        if(!(event instanceof PlayerEvent playerEvent) || !(event instanceof Cancellable cancellable))
            return;
        WPlayer wplayer = playerManager.getWPlayer(playerEvent.getPlayer());
        if((wplayer != null && wplayer.isWallJumping()) || LocationUtils.isTouchingAWall(playerEvent.getPlayer()))
            cancellable.setCancelled(true);
    }

}
