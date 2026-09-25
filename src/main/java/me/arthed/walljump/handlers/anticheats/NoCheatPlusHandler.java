package me.arthed.walljump.handlers.anticheats;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Hooks into NoCheatPlus (or Updated-NoCheatPlus) using reflection, so no compile time dependency is needed.
 */
public class NoCheatPlusHandler implements AntiCheatHandler {

    private final Object movingCheckType;
    private final Method exemptMethod;
    private final Method unexemptMethod;

    public NoCheatPlusHandler(Plugin noCheatPlus) throws ReflectiveOperationException {
        ClassLoader classLoader = noCheatPlus.getClass().getClassLoader();
        Class<?> checkTypeClass = Class.forName("fr.neatmonster.nocheatplus.checks.CheckType", true, classLoader);
        Class<?> exemptionManagerClass = Class.forName("fr.neatmonster.nocheatplus.hooks.NCPExemptionManager", true, classLoader);

        movingCheckType = checkTypeClass.getField("MOVING").get(null);
        exemptMethod = exemptionManagerClass.getMethod("exemptPermanently", Player.class, checkTypeClass);
        unexemptMethod = exemptionManagerClass.getMethod("unexempt", Player.class, checkTypeClass);
    }

    @Override
    public void stopPotentialWallJumpingChecks(Player player) throws ReflectiveOperationException {
        exemptMethod.invoke(null, player, movingCheckType);
    }

    @Override
    public void restartPotentialWallJumpingChecks(Player player) throws ReflectiveOperationException {
        unexemptMethod.invoke(null, player, movingCheckType);
    }
}
