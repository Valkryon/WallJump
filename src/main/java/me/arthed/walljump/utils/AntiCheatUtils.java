package me.arthed.walljump.utils;

import me.arthed.walljump.WallJump;
import me.arthed.walljump.handlers.anticheats.AACHandler;
import me.arthed.walljump.handlers.anticheats.AntiCheatHandler;
import me.arthed.walljump.handlers.anticheats.NoCheatPlusHandler;
import me.arthed.walljump.handlers.anticheats.SpartanHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class AntiCheatUtils {

    private static AntiCheatHandler antiCheatHandler;

    public AntiCheatUtils() {
        antiCheatHandler = null;
        try {
            Plugin plugin;
            if((plugin = Bukkit.getPluginManager().getPlugin("Spartan")) != null)
                antiCheatHandler = new SpartanHandler(plugin);
            else if((plugin = Bukkit.getPluginManager().getPlugin("AAC")) != null)
                antiCheatHandler = new AACHandler(plugin);
            else if((plugin = Bukkit.getPluginManager().getPlugin("NoCheatPlus")) != null)
                antiCheatHandler = new NoCheatPlusHandler(plugin);
            else
                return;
            WallJump.getInstance().getLogger().info("Hooked into " + plugin.getName());
        } catch(ReflectiveOperationException | LinkageError e) {
            WallJump.getInstance().getLogger().log(Level.WARNING, "Failed to hook into the installed anti cheat, its checks will not be disabled while wall jumping", e);
        }
    }

    public static void stopPotentialAntiCheatChecks(Player player) {
        if(antiCheatHandler == null)
            return;
        try {
            antiCheatHandler.stopPotentialWallJumpingChecks(player);
        } catch(ReflectiveOperationException | RuntimeException e) {
            disable(e);
        }
    }

    public static void restartPotentialAntiCheatChecks(Player player) {
        if(antiCheatHandler == null)
            return;
        try {
            antiCheatHandler.restartPotentialWallJumpingChecks(player);
        } catch(ReflectiveOperationException | RuntimeException e) {
            disable(e);
        }
    }

    private static void disable(Exception e) {
        antiCheatHandler = null;
        WallJump.getInstance().getLogger().log(Level.WARNING, "An error occurred while using the anti cheat API, the anti cheat hook has been disabled", e);
    }
}
