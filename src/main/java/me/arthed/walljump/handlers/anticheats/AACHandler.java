package me.arthed.walljump.handlers.anticheats;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Hooks into AAC 5 using reflection, so no compile time dependency is needed.
 */
public class AACHandler implements AntiCheatHandler {

    private final Object api;
    private final Object exemption;
    private final Method addExemptionMethod;
    private final Method removeExemptionMethod;

    public AACHandler(Plugin aac) throws ReflectiveOperationException {
        ClassLoader classLoader = aac.getClass().getClassLoader();
        Class<?> apiClass = Class.forName("me.konsolas.aac.api.AACAPI", true, classLoader);
        Class<?> exemptionClass = Class.forName("me.konsolas.aac.api.AACExemption", true, classLoader);

        api = Bukkit.getServicesManager().load(apiClass);
        if(api == null)
            throw new ClassNotFoundException("The AAC API service is not registered");
        exemption = exemptionClass.getConstructor(String.class).newInstance("WallJumping");
        addExemptionMethod = apiClass.getMethod("addExemption", Player.class, exemptionClass);
        removeExemptionMethod = apiClass.getMethod("removeExemption", Player.class, exemptionClass);
    }

    @Override
    public void stopPotentialWallJumpingChecks(Player player) throws ReflectiveOperationException {
        addExemptionMethod.invoke(api, player, exemption);
    }

    @Override
    public void restartPotentialWallJumpingChecks(Player player) throws ReflectiveOperationException {
        removeExemptionMethod.invoke(api, player, exemption);
    }
}
