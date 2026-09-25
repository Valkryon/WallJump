package me.arthed.walljump.handlers.anticheats;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Hooks into Spartan using reflection, so no compile time dependency is needed.
 */
public class SpartanHandler implements AntiCheatHandler {

    private final Object irregularMovements;
    private final Method isEnabledMethod;
    private final Method stopCheckMethod;
    private final Method startCheckMethod;

    public SpartanHandler(Plugin spartan) throws ReflectiveOperationException {
        ClassLoader classLoader = spartan.getClass().getClassLoader();
        Class<?> apiClass = Class.forName("me.vagdedes.spartan.api.API", true, classLoader);
        Class<?> hackTypeClass = Class.forName("me.vagdedes.spartan.system.Enums$HackType", true, classLoader);

        irregularMovements = hackTypeClass.getField("IrregularMovements").get(null);
        isEnabledMethod = apiClass.getMethod("isEnabled", hackTypeClass);
        stopCheckMethod = apiClass.getMethod("stopCheck", Player.class, hackTypeClass);
        startCheckMethod = apiClass.getMethod("startCheck", Player.class, hackTypeClass);
    }

    @Override
    public void stopPotentialWallJumpingChecks(Player player) throws ReflectiveOperationException {
        if((boolean) isEnabledMethod.invoke(null, irregularMovements))
            stopCheckMethod.invoke(null, player, irregularMovements);
    }

    @Override
    public void restartPotentialWallJumpingChecks(Player player) throws ReflectiveOperationException {
        if((boolean) isEnabledMethod.invoke(null, irregularMovements))
            startCheckMethod.invoke(null, player, irregularMovements);
    }
}
