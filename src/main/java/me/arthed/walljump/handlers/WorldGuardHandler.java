package me.arthed.walljump.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.arthed.walljump.WallJump;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class WorldGuardHandler {

    public static StateFlag ALLOW_WALL_JUMP;

    /**
     * Must be called while the plugin is loading, custom flags can't be registered after WorldGuard is enabled.
     */
    public WorldGuardHandler() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("wall-jump", WallJump.getInstance().getWallJumpConfig().getBoolean("worldGuardFlagDefault"));
            registry.register(flag);
            ALLOW_WALL_JUMP = flag;
        } catch(FlagConflictException | IllegalStateException e) {
            Flag<?> existing = registry.get("wall-jump");
            if(existing instanceof StateFlag stateFlag)
                ALLOW_WALL_JUMP = stateFlag;
            else
                WallJump.getInstance().getLogger().log(Level.WARNING, "Failed to register the wall-jump WorldGuard flag", e);
        }
    }

    public boolean canWallJump(Player player) {
        if(ALLOW_WALL_JUMP == null)
            return true;

        //testState is only true when the flag resolves to ALLOW, which includes the flag's default value
        //(unlike queryState, which returns null instead of DENY when the default is false and no region sets the flag)
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.testState(
                BukkitAdapter.adapt(player.getLocation()),
                WorldGuardPlugin.inst().wrapPlayer(player),
                ALLOW_WALL_JUMP
        );
    }

}
