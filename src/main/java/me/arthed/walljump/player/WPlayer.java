package me.arthed.walljump.player;

import me.arthed.walljump.WallJump;
import me.arthed.walljump.api.events.WallJumpEndEvent;
import me.arthed.walljump.api.events.WallJumpResetEvent;
import me.arthed.walljump.api.events.WallJumpStartEvent;
import me.arthed.walljump.config.WallJumpConfiguration;
import me.arthed.walljump.enums.WallFace;
import me.arthed.walljump.handlers.WorldGuardHandler;
import me.arthed.walljump.utils.AntiCheatUtils;
import me.arthed.walljump.utils.EffectUtils;
import me.arthed.walljump.utils.LocationUtils;
import me.arthed.walljump.utils.VelocityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class WPlayer {

    private final Player player;

    private boolean wallJumping;
    private boolean onWall;
    private boolean sliding;

    private WallFace lastFacing;
    private Location lastJumpLocation;
    private Material wallMaterial;
    private int remainingJumps = -1;

    private BukkitTask velocityTask;
    private BukkitTask fallTask;
    private float velocityY;
    private BukkitTask landingTask;

    private final WallJumpConfiguration config;
    private final WorldGuardHandler worldGuard;
    private final NamespacedKey enabledKey;

    protected WPlayer(Player player) {
        this.player = player;

        WallJump wallJump = WallJump.getInstance();
        config = wallJump.getWallJumpConfig();
        worldGuard = wallJump.getWorldGuardHandler();
        enabledKey = new NamespacedKey(wallJump, "enabled");
        remainingJumps = getMaxJumps();
    }

    public void onWallJumpStart() {
        if(!canWallJump())
            return;

        WallJumpStartEvent event = new WallJumpStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())
            return;

        onWall = true;
        wallJumping = true;
        lastFacing = LocationUtils.getPlayerFacing(player);
        lastJumpLocation = player.getLocation();
        wallMaterial = LocationUtils.getBlockPlayerIsStuckOn(player, lastFacing).getType();
        if(remainingJumps > 0)
            remainingJumps--;

        //Stop some anti cheat checks that might be caused by wall-jumping
        AntiCheatUtils.stopPotentialAntiCheatChecks(player);

        //play sound
        EffectUtils.playWallJumpSound(player, lastFacing, 0.3f, 1.2f);

        //stop the player from falling and moving while on the wall
        //or make them slide down
        velocityY = 0;
        if(velocityTask != null)
            velocityTask.cancel();
        velocityTask = Bukkit.getScheduler().runTaskTimer(WallJump.getInstance(), () -> {
            player.setVelocity(new Vector(0, velocityY, 0));
            Block wall = LocationUtils.getBlockPlayerIsStuckOn(player, lastFacing);
            //the wall is gone (broken, or the player slid past it) or the player slid down to the ground
            if (!wall.getType().isSolid() || (sliding && player.isOnGround())) {
                player.setFallDistance(0);
                player.teleport(player.getLocation());
                onWallJumpEnd(false);
                return;
            }
            wallMaterial = wall.getType();
            if(sliding) {
                //the player can slide onto a different block, which may have its own sliding speed
                velocityY = (float) -config.getBlockDouble("slidingSpeed", wallMaterial);
                if (lastJumpLocation.getY() - player.getLocation().getY() >= 1.2) {
                    lastJumpLocation = player.getLocation();
                    EffectUtils.playWallJumpSound(player, lastFacing, 0.2f, 0.6f);
                }
            }
        }, 0, 1);

        //make the player fall | slide when the time runs out
        if(fallTask != null)
            fallTask.cancel();
        fallTask = Bukkit.getScheduler().runTaskLater(WallJump.getInstance(), () -> {
            if(onWall) {
                if (config.getBoolean("slide")) {
                    velocityY = (float) -config.getBlockDouble("slidingSpeed", wallMaterial);
                    sliding = true;
                } else {
                    onWallJumpEnd();
                }
            }
        }, (long)(config.getBlockDouble("timeOnWall", wallMaterial)*20));

        //cancel the task for resetting wall jumping if the player wall jumps
        cancelLandingTask();
    }

    public void onWallJumpEnd() {
        onWallJumpEnd(true);
    }

    public void onWallJumpEnd(boolean jump) {
        AntiCheatUtils.restartPotentialAntiCheatChecks(player);

        boolean wasSliding = sliding;
        onWall = false;
        sliding = false;

        //allow the player to move again
        player.setFallDistance(0);
        if(velocityTask != null) {
            velocityTask.cancel();
            velocityTask = null;
        }

        //call event
        WallJumpEndEvent event = new WallJumpEndEvent(this,
                config.getBlockDouble("horizontalJumpPower", wallMaterial),
                config.getBlockDouble("verticalJumpPower", wallMaterial));
        Bukkit.getPluginManager().callEvent(event);
        //if the player is not sliding or can jump while sliding and is not looking down
        if(jump &&// !event.isCancelled() &&
                ((!wasSliding && player.getLocation().getPitch() < 85) ||
                (config.getBoolean("canJumpWhileSliding") && player.getLocation().getPitch() < 60)))
            //push the player in the direction that they are looking
            VelocityUtils.pushPlayerInFront(player,
                    event.getHorizontalPower(),
                    event.getVerticalPower());

        //once the player lands (and hasn't wall jumped again), reset everything
        cancelLandingTask();
        landingTask = Bukkit.getScheduler().runTaskTimer(WallJump.getInstance(), () -> {
            if(hasLanded())
                reset();
        }, 2, 2);
    }

    /**
     * Immediately stops wall jumping without pushing the player or calling any events.
     * Used when the player leaves, dies or changes world.
     */
    public void stopWallJumping() {
        if(onWall)
            AntiCheatUtils.restartPotentialAntiCheatChecks(player);
        onWall = false;
        sliding = false;
        velocityY = 0;
        if(velocityTask != null) {
            velocityTask.cancel();
            velocityTask = null;
        }
        if(fallTask != null) {
            fallTask.cancel();
            fallTask = null;
        }
        cancelLandingTask();

        wallJumping = false;
        lastFacing = null;
        lastJumpLocation = null;
        remainingJumps = getMaxJumps();
    }

    private void reset() {
        wallJumping = false;

        lastFacing = null;
        lastJumpLocation = null;
        remainingJumps = getMaxJumps();
        cancelLandingTask();
        Bukkit.getPluginManager().callEvent(new WallJumpResetEvent(this));
    }

    private void cancelLandingTask() {
        if(landingTask != null) {
            landingTask.cancel();
            landingTask = null;
        }
    }

    private boolean hasLanded() {
        return player.isOnGround() ||
                LocationUtils.isOnGround(player) ||
                player.isInWater() ||
                player.isClimbing() ||
                player.isFlying() ||
                player.isGliding() ||
                player.isInsideVehicle();
    }

    private int getMaxJumps() {
        int maxJumps = config.getInt("maxJumps");
        //0 means unlimited
        return maxJumps <= 0 ? -1 : maxJumps;
    }

    public boolean canWallJump() {
        WallFace facing = LocationUtils.getPlayerFacing(player);
        //the last jump location is meaningless once the player is in another world
        if(lastJumpLocation != null && lastJumpLocation.getWorld() != player.getWorld())
            lastJumpLocation = null;
        if(lastJumpLocation != null)
            //used so height doesn't matter when calculating distance between the players location and the last jump location
            lastJumpLocation.setY(player.getLocation().getY());
        if(
                        !isEnabled() ||
                        onWall || //player is already stuck to an wall
                        remainingJumps == 0 || //player reached jump limit
                        (lastFacing != null && lastFacing.equals(facing)) || //player is facing the same direction as the last jump
                        (lastJumpLocation != null && player.getLocation().distance(lastJumpLocation) <= config.getDouble("minimumDistance")) ||  //player is too close to the last jump location
                        player.getVelocity().getY() < config.getDouble("maximumVelocity") || //player is falling too fast
                        (config.getBoolean("needPermission") && !player.hasPermission("walljump.use")) || //player does not have the permission to wall-jump
                        (worldGuard != null && !worldGuard.canWallJump(player)) //wall-jumping is not allowed in the region the player is in
        )

            return false;
        //check if the block the player is wall jumping on is blacklisted
        boolean onBlacklistedBlock = config.getMaterialList("blacklistedBlocks").contains(
                player.getLocation().clone().add(facing.xOffset,
                        facing.yOffset,
                        facing.zOffset)
                        .getBlock()
                        .getType());
        boolean reverseBlockBlacklist = config.getBoolean("reversedBlockBlacklist");
        if((!reverseBlockBlacklist && onBlacklistedBlock) ||
                (reverseBlockBlacklist && !onBlacklistedBlock))
            return false;

        //check if the world the player is in is blacklisted
        boolean inBlacklistedWorld = config.getWorldList("blacklistedWorlds").contains(
                player.getWorld());
        boolean reverseWorldBlacklist = config.getBoolean("reversedWorldBlacklist");
        if((!reverseWorldBlacklist && inBlacklistedWorld) ||
                (reverseWorldBlacklist && !inBlacklistedWorld))
            return false;

        return true;
    }

    /**
     * @return whether the player has wall jumping turned on (with /walljump on|off)
     */
    public boolean isEnabled() {
        return player.getPersistentDataContainer().getOrDefault(enabledKey, PersistentDataType.BOOLEAN, true);
    }

    /**
     * Turns wall jumping on or off for this player. The choice is stored in the player's data, so it is kept
     * after they leave or the server restarts.
     */
    public void setEnabled(boolean enabled) {
        if(enabled)
            player.getPersistentDataContainer().remove(enabledKey);
        else
            player.getPersistentDataContainer().set(enabledKey, PersistentDataType.BOOLEAN, false);
    }

    public boolean isOnWall() {
        return onWall;
    }

    public boolean isWallJumping() {
        return wallJumping;
    }

    public boolean isSliding() {
        return sliding;
    }

    public Player getPlayer() {
        return player;
    }

}
