package me.arthed.walljump.utils;

import me.arthed.walljump.enums.WallFace;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class EffectUtils {

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
