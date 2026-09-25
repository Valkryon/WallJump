package me.arthed.walljump.utils;

import me.arthed.walljump.enums.WallFace;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class EffectUtils {

    public static void spawnSlidingParticles(Player player, int count, WallFace facing) {
        Location location = player.getLocation();
        Block block = LocationUtils.getBlockPlayerIsStuckOn(player, facing);
        if(block.getType().isAir())
            return;
        player.getWorld().spawnParticle(
                Particle.BLOCK,
                location.clone().add(facing.xOffset*0.3, facing.yOffset*0.3-0.3, facing.zOffset*0.3),
                count,
                0.2f,
                0.2f,
                0.2f,
                block.getBlockData());
    }

    public static void playWallJumpSound(Player player, WallFace facing, float volume, float pitch) {
        Block block = LocationUtils.getBlockPlayerIsStuckOn(player, facing);
        player.getWorld().playSound(
                player.getLocation(),
                block.getBlockData().getSoundGroup().getStepSound(),
                volume,
                pitch
        );
    }

}
